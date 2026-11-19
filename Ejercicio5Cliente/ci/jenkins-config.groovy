import jenkins.model.Jenkins
import hudson.model.JDK
import hudson.tasks.Maven
import hudson.plugins.git.*
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition
import com.cloudbees.plugins.credentials.*
import com.cloudbees.plugins.credentials.domains.Domain
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl

def j = Jenkins.get()
j.setJDKs([new JDK('JDK21', '/opt/java/openjdk')])
j.getDescriptorByType(Maven.DescriptorImpl).setInstallations(new Maven.MavenInstallation('Maven3.9.11', '/var/jenkins_home/tools/apache-maven-3.9.11', []))
def store = SystemCredentialsProvider.getInstance().getStore()
def ciEnv = new File('/var/jenkins_home/.secrets/db-ci.env')
if (ciEnv.exists() && !SystemCredentialsProvider.getInstance().getCredentials().any { it.id == 'gestor-db-ci' }) {
    def props = new Properties(); ciEnv.withInputStream { props.load(it) }
    store.addCredentials(Domain.global(), new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, 'gestor-db-ci', 'MariaDB aislada de pruebas gestor_ci', 'gestor_ci', props.getProperty('MARIADB_PASSWORD')))
}
def sonarTokenFile = new File('/var/jenkins_home/.secrets/sonar-token')
if (sonarTokenFile.exists() && !SystemCredentialsProvider.getInstance().getCredentials().any { it.id == 'gestor-sonar-token' }) {
    store.addCredentials(Domain.global(), new org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl(CredentialsScope.GLOBAL, 'gestor-sonar-token', 'SonarQube - analisis, expira en 30 dias', hudson.util.Secret.fromString(sonarTokenFile.text.trim())))
}
def definitions = [
 'gestor-frontend': 'Ejercicio5Cliente/Jenkinsfile',
 'gestor-backend': 'Ejercicio5MyAPIGestor/Jenkinsfile',
 'gestor-database': 'Ejercicio5MyAPIGestor/database/Jenkinsfile'
]
def scm = new GitSCM([new UserRemoteConfig('https://github.com/elmanee/gestor-proyectos-java21.git', null, null, null)], [new BranchSpec('*/main')], false, [], null, null, [])
definitions.each { name, path ->
    def job = j.getItem(name) ?: j.createProject(WorkflowJob, name)
    job.definition = new CpsScmFlowDefinition(scm, path)
    job.description = 'Practica 3: Java 21, pruebas, cobertura y seguridad. Fuente: ' + path
    job.save()
}
j.save()
println('Herramientas Java 21 y Maven 3.9.11, credencial DB de pruebas y tres pipelines configurados.')


