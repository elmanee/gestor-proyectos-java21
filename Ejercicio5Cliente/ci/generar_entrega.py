from pathlib import Path

import json,csv,collections,xml.etree.ElementTree as ET,shutil

from reportlab.platypus import SimpleDocTemplate,Paragraph,Spacer,Table,TableStyle,PageBreak,KeepTogether

from reportlab.lib.styles import getSampleStyleSheet,ParagraphStyle

from reportlab.lib import colors

from reportlab.lib.enums import TA_LEFT

from reportlab.lib.pagesizes import A4

from xml.sax.saxutils import escape

R=Path(__file__).resolve().parents[1]; E=R/'entrega'; A=R.parent/'Ejercicio5MyAPIGestor'; S=E/'parte2/seguridad'

(E/'parte1/pom-actualizados').mkdir(parents=True,exist_ok=True)

for name,p in [('cliente',R/'pom.xml'),('api',A/'pom.xml')]:shutil.copy2(p,E/'parte1/pom-actualizados'/f'pom-{name}.xml')

def load(p,default=None):

 return json.loads(p.read_text(encoding='utf-8-sig')) if p.exists() else default



def vulnerabilities(p):return [v for r in (load(p,{}) or {}).get('Results',[]) for v in r.get('Vulnerabilities',[])]

counts={}

for name in ['cliente','api']:

 for phase in ['antes','despues']:

  vals=vulnerabilities(S/f'dependencias-{name}-{phase}.json');counts[name,phase]=collections.Counter(x['Severity'] for x in vals)

with (S/'clasificacion-dependencias.csv').open('w',encoding='utf-8-sig',newline='') as f:

 w=csv.writer(f);w.writerow(['proyecto','hallazgo','componente','version_original','severidad_escaneo','version_corregida_sugerida','estado','justificacion'])

 for name in ['cliente','api']:

  current={(v['VulnerabilityID'],v['PkgName']) for v in vulnerabilities(S/f'dependencias-{name}-despues.json')}

  for v in vulnerabilities(S/f'dependencias-{name}-antes.json'):

   remains=(v['VulnerabilityID'],v['PkgName']) in current

   w.writerow([name,v['VulnerabilityID'],v['PkgName'],v['InstalledVersion'],v['Severity'],v.get('FixedVersion',''), 'No aplicable a esta configuracion' if remains else 'No detectado despues de actualizar', 'Sin XsltView ni SSE con fragmentos; verificar SecurityConfigurationTest y avisos de Spring.' if remains else 'Actualizar Spring Boot y fijar parches Jackson, Tomcat, Log4j y MariaDB. Ver SBOM y reescaneo.'])

coverage={}

for name,p in [('cliente',R),('api',A)]:

 report=E/'parte2/cobertura'/name/'jacoco.xml'
 if not report.exists():report=p/'target/site/jacoco/jacoco.xml'
 t=ET.parse(report).getroot();coverage[name]={c.attrib['type']:(int(c.attrib['covered']),int(c.attrib['missed'])) for c in t.findall('counter')}

 testdir=E/'parte2/jenkins'/('gestor-frontend' if name=='cliente' else 'gestor-backend')/'surefire-reports'
 if not testdir.exists():testdir=p/'target/surefire-reports'
 tests=[ET.parse(q).getroot() for q in testdir.glob('TEST-*.xml')]

 coverage[name]['tests']=sum(int(x.attrib['tests']) for x in tests)

 coverage[name]['failures']=sum(int(x.attrib['failures'])+int(x.attrib['errors']) for x in tests)

jenkins=load(E/'parte2/jenkins/resumen.json',{})

functional=load(E/'parte2/resultado-funcional-java21.json',{})

styles=getSampleStyleSheet();styles.add(ParagraphStyle(name='Body',fontName='Helvetica',fontSize=10,leading=14,spaceAfter=8,textColor=colors.HexColor('#283443')))

styles.add(ParagraphStyle(name='SmallBody',parent=styles['Body'],fontSize=8,leading=11))

styles['Title'].textColor=colors.HexColor('#143454');styles['Title'].fontSize=26;styles['Title'].leading=30

styles['Heading1'].textColor=colors.HexColor('#143454');styles['Heading1'].fontSize=17;styles['Heading1'].spaceBefore=15

styles['Heading2'].textColor=colors.HexColor('#276278');styles['Heading2'].fontSize=12;styles['Heading2'].spaceBefore=10

story=[]; md=[]

def title(s):story.append(Paragraph(escape(s),styles['Title']));md.append('# '+s)

def h(s):story.append(Paragraph(escape(s),styles['Heading1']));md.append('\n## '+s)

def p(s):story.append(Paragraph(escape(s),styles['Body']));md.append(s+'\n')

def table(headers,rows,widths=None):

 data=[[Paragraph(escape(str(x)),styles['SmallBody']) for x in headers]]+[[Paragraph(escape(str(x)),styles['SmallBody']) for x in row] for row in rows]

 t=Table(data,colWidths=widths,repeatRows=1,hAlign='LEFT');t.setStyle(TableStyle([('BACKGROUND',(0,0),(-1,0),colors.HexColor('#dce8f1')),('VALIGN',(0,0),(-1,-1),'TOP'),('ROWBACKGROUNDS',(0,1),(-1,-1),[colors.white,colors.HexColor('#f3f6f8')]),('LEFTPADDING',(0,0),(-1,-1),8),('RIGHTPADDING',(0,0),(-1,-1),8),('TOPPADDING',(0,0),(-1,-1),7),('BOTTOMPADDING',(0,0),(-1,-1),7),('LINEBELOW',(0,0),(-1,0),.7,colors.HexColor('#9cb5c6'))]));story.extend([t,Spacer(1,12)]);md.append('| '+' | '.join(headers)+' |\n| '+' | '.join(['---']*len(headers))+' |\n'+'\n'.join('| '+' | '.join(map(str,row))+' |' for row in rows)+'\n')

def page():story.append(PageBreak())

title('Actualización de microservicios y pipelines CI')

p('Práctica 3 - Recuperación. Informe técnico y evidencias verificables. Fecha de preparación: 19 de noviembre de 2026.')

p('Repositorio del estudiante: https://github.com/elmanee/gestor-proyectos-java21. El proyecto original y parte de las pruebas provienen de Enrique1189/admin-proyectos; se conserva su atribución. Las ligas de entrega apuntan a la copia del estudiante.')

h('1. Actualización tecnológica')

table(['Componente','Estado original local','Estado actualizado'],[

['Cliente','Java 17; Spring Boot 3.2.5','Java 21; Spring Boot 3.5.16'],['API','Java 11; Spring Boot 2.7.17','Java 21; Spring Boot 3.5.16'],['Jenkins','Contenedor existente detenido','Jenkins 2.580.2; Temurin 21.0.13'],['Maven','Wrapper incompleto; Maven de IntelliJ','Maven Wrapper y herramienta Jenkins 3.9.11'],['Plugins','Sin configuración completa en los POM locales','Surefire 3.5.5; JaCoCo 0.8.14; Sonar Maven 5.5.0.6356'],['Seguridad de dependencias','Versiones heredadas originales','Jackson 2.21.7; Tomcat 10.1.59; Log4j 2.25.5; MariaDB JDBC 3.5.11']], [95,165,255])

p('Los dos POM originales se copiaron antes de modificarlos. Los dos actualizados son copias de los POM finales. Se conservan en parte1/pom-originales y parte1/pom-actualizados. Java 21 se fija tanto en java.version como en maven-compiler-plugin release=21; no basta con cambiar el JDK del IDE.')

p('La propiedad de parches evita conservar dependencias vulnerables aun cuando una versión de Spring Boot sea compatible con Java 21. CycloneDX genera un inventario resuelto en ci-reports/bom.json, fuera de target.')

h('2. Justificación de cambios de código')

p('La API migró de javax.persistence a jakarta.persistence. Los identificadores usan GenerationType.IDENTITY para respetar las columnas AUTO_INCREMENT existentes. Se incorporaron convertidores de fechas DATE/DATETIME para mantener el contrato JSON de cadenas y validar el esquema de MariaDB sin recrear tablas.')

p('La conexión usa DB_URL, DB_USER y DB_PASSWORD mediante variables de entorno. Hibernate usa ddl-auto=validate y no modifica la estructura de dbgestor. La contraseña no se incluye en POM, Jenkinsfile ni configuración publicada. El esquema de instalación contiene estructura vacía, sin registros personales.')

page();h('3. Correcciones funcionales y de seguridad')

p('El cliente usa el puerto 8082 para convivir con Jenkins en 8080; la API conserva 8081. La dirección de API se configura mediante API_URL. RestTemplate se inyecta para poder vincular el mismo cliente HTTP a MockRestServiceServer: las pruebas no dependen del backend real.')

p('Se corrigieron las respuestas de comentarios y la ruta /tarea, el envío doble del formulario, fondos sin extensión, referencias a elementos inexistentes y controles iniciales sin validación. Se retiraron tareas de ejemplo que aparecían mientras cargaban las reales. La fecha de comentario utiliza formato ISO compatible con DATETIME.')

p('Las contraseñas nuevas y modificadas usan BCrypt y se omiten de todas las respuestas JSON. Los registros antiguos en texto plano conservan compatibilidad temporal para no invalidar cuentas de la práctica; esto es un riesgo residual identificado. Deben actualizarse esas contraseñas antes de usar el sistema con datos reales.')

p('La API conserva el esquema de autorización original del proyecto educativo: el login de la interfaz no implementa control de acceso por usuario o rol en cada endpoint. Es un riesgo residual que requiere una mejora específica antes de usar datos reales o exponer la aplicación fuera del laboratorio.')

p('Se escapa el texto mostrado por plantillas JavaScript y se evita interpolar nombres directamente como código en acciones de borrado. Los fondos se restringen a recursos locales esperados. Se sustituyeron correos personales del ejemplo por contactos ficticios. El borrado de proyecto y tarea se realiza en una transacción e incluye comentarios y asignaciones relacionados.')

h('4. Pruebas y cobertura')

rows=[]

for name in ['cliente','api']:

 c=coverage[name];m,mm=c['METHOD'];l,lm=c['LINE'];b,bm=c['BRANCH'];rows.append([name,str(c['tests']),str(c['failures']),f'{m}/{m+mm} = {m/(m+mm)*100:.0f}%',f'{l}/{l+lm} = {l/(l+lm)*100:.1f}%',f'{b}/{b+bm} = {b/(b+bm)*100:.1f}%'])

table(['Servicio','Pruebas','Fallos','Métodos','Líneas','Ramas'],rows,[64,46,43,120,120,122])

p('Se mide código Java evaluable: controladores, modelos, persistencia y servicio transaccional. Solo se excluyen de JaCoCo las clases *Application de arranque. No se excluyen métodos funcionales para elevar el porcentaje. El umbral de métodos es 1.00 y Maven verify falla si no se alcanza. La cobertura de ramas se informa por separado: 100% de métodos no significa 100% de ramas ni prueba ausencia de defectos.')

p('Además, 11 pruebas de JavaScript propio ejecutadas con Node, jsdom y c8 verifican formularios, sesión, errores HTTP, tarjetas, tareas y comentarios. Resultado: 100% de líneas y funciones; 96.9% de ramas. Se importa coverage/lcov.info en SonarQube. npm audit reportó cero vulnerabilidades tras fijar glob 14.0.6 y minimatch 11.0.9 en las herramientas de prueba.')
p('Las pruebas verifican CRUD de cinco entidades, filtrado por proyecto/tarea, persistencia de cambios, credenciales válidas e inválidas, protección y conservación de contraseñas, fechas válidas e inválidas, respuestas HTTP y borrado relacionado. H2 en modo MariaDB permite pruebas aisladas. MariaDbIT valida además el esquema y operaciones SQL contra una MariaDB real separada, y solo acepta la base gestor_ci.')

p('El script funcional crea datos con prefijo QA_ y elimina solo sus propios registros. Su último resultado contiene '+str(len(functional.get('pruebas',[])))+' comprobaciones; '+str(sum(x['resultado']=='FALLO' for x in functional.get('pruebas',[])))+' fallos. Los reportes HTML/XML de JaCoCo y JUnit se adjuntan, junto con resultados de Failsafe para MariaDB.')

page();h('5. Análisis de vulnerabilidades')

p('Se ejecutó Trivy 0.67.2 sobre código fuente para secretos y configuración, excluyendo target, .git, .idea, entrega, evidencias, tmp y .secrets. Además se escanearon inventarios CycloneDX completos de antes y después, evitando confundir un POM sin versiones resueltas con ausencia de vulnerabilidades. No se excluye código fuente propio del escaneo de seguridad.')

rows=[]

for name in ['cliente','api']:

 for phase in ['antes','despues']:

  c=counts[name,phase];rows.append([name,phase,c['CRITICAL'],c['HIGH'],c['MEDIUM'],c['LOW'],sum(c.values())])

table(['Servicio','Momento','Críticas','Altas','Medias','Bajas','Total'],rows,[76,78,70,70,70,70,81])

p('Los totales son hallazgos por componente, no CVE únicos entre los dos servicios. Los reportes originales permanecen completos. clasificacion-dependencias.csv documenta cada hallazgo, versión, severidad y atención; los SBOM permiten verificar las versiones efectivamente utilizadas.')

table(['Incidencia','Clasificación y atención'],[['Dependencias de aplicación','Actualizar Spring Boot y los parches específicos de Jackson, Tomcat, Log4j y MariaDB. Confirmar mediante nuevo inventario y escaneo.'],['CVE-2026-47884','Aviso residual por Spring MVC 6.2.19. Requiere XsltView y mapeo de vistas amplio sin nombre explícito. El sistema usa vistas Thymeleaf explícitas y no configura XsltView. No aplicable a la configuración evaluada; no se oculta el hallazgo.'],['CVE-2026-47890','Aviso residual que requiere SSE con fragmentos de vistas y datos controlados por atacante. El sistema no tiene endpoints SSE ni FragmentsRendering. Se verifica en SecurityConfigurationTest. Reevaluar si se añaden esas funciones.'],['Contraseñas y configuración','BCrypt para altas y cambios; password de BD por entorno; no se devuelve contrasena en JSON. La compatibilidad de cuentas antiguas se declara como residual.'],['HTML dinámico','Escapar texto y evitar nombres interpolados como código; contactos ficticios en recursos de demostración.']], [125,390])

p('El analisis final de SonarQube aprueba los tres Quality Gates. Se corrigieron nueve bugs HTML/CSS y las declaraciones var del JavaScript propio. Quedan avisos de mantenibilidad: 183 en frontend, 111 en backend y 3 en persistencia, conservados en los JSON y CSV. El hotspot javascript:S2245 fue revisado como Safe: Math.random solo elige un color de avatar, no un secreto. La interfaz confirma 100% de hotspots revisados. Un Quality Gate aprobado no equivale a ausencia de deuda tecnica.')
p('Los dos avisos residuales se justifican con condiciones publicadas por Spring, inspección del código y pruebas de configuración. No se declaran corregidas las bibliotecas afectadas ni se eliminan sus resultados para aparentar cero hallazgos. El análisis no es una auditoría completa de producción ni de las imágenes Docker del entorno.')

page();h('6. Tres pipelines')

rows=[]

for key in ['frontend','backend','database']:

 state=jenkins.get('gestor-'+key,{});rows.append(['gestor-'+key,state.get('result','En ejecución / por verificar'),f'http://localhost:8080/job/gestor-{key}/'])

table(['Pipeline','Resultado registrado','Liga local'],rows,[115,150,250])

p('Cada Jenkinsfile obtiene el código de GitHub, muestra java -version y mvn -version, ejecuta pruebas y cobertura, escanea fuente y SBOM con Trivy, envía análisis a SonarQube y espera el Quality Gate. Publica resultados JUnit y archiva reportes JaCoCo, inventario y seguridad. El pipeline de base de datos activa además el perfil database-ci de Failsafe.')

p('La primera ejecución del frontend detectó cobertura combinada insuficiente (71.2%). Se corrigió agregando pruebas reales del JavaScript propio y su reporte LCOV, conservando el Quality Gate y el código funcional dentro del análisis.')
p('Jenkins usa las herramientas JDK21 y Maven3.9.11. Node se utiliza para analizar JavaScript propio; las librerías de terceros distribuidas ya minificadas se excluyen de Sonar, no el código escrito para el sistema. JaCoCo mide Java y c8 mide JavaScript; los dos reportes se importan en SonarQube.')

p('El token de SonarQube es de análisis, con expiración de 30 días, y se guarda como credencial gestor-sonar-token. La base aislada usa gestor-db-ci. No se incluyen sus valores en el repositorio ni en las evidencias. La base real sigue en puerto 3307 y la de CI en 13307; MariaDbIT impide apuntar a otra base distinta de gestor_ci.')

p('Archivos de pipeline: Ejercicio5Cliente/Jenkinsfile; Ejercicio5MyAPIGestor/Jenkinsfile; Ejercicio5MyAPIGestor/database/Jenkinsfile. El script ci/jenkins-config.groovy configura herramientas y jobs del laboratorio local. Los registros de compilación muestran si cada etapa se ejecutó realmente; la existencia del Jenkinsfile por sí sola no acredita un pipeline exitoso.')

h('7. Repetición de la práctica')

p('En cada carpeta, con JDK 21: mvnw.cmd clean verify en Windows o ./mvnw clean verify en Linux. Para ejecutar la API, establecer DB_PASSWORD y usar mvnw.cmd spring-boot:run. Para el cliente, usar el mismo comando y abrir http://localhost:8082. El archivo .env.example es documentación: Spring no carga .env automáticamente.')

p('Para la prueba SQL, configurar DB_CI_URL=jdbc:mariadb://localhost:13307/gestor_ci y las credenciales de la base aislada, y ejecutar mvnw.cmd verify -Pdatabase-ci. Ese perfil recrea las tablas de gestor_ci, nunca las de dbgestor. Para repetir las comprobaciones funcionales con los servicios activos: python verificar_funcional.py.')

page();h('8. Qué entregar')
p('Se adjunta Video-evidencia-Practica-3.mp4: resumen de 4 min 46 s con capturas reales y narración sintética en español. No es una grabación continua ni representa la voz o la experiencia personal del estudiante. El guion permite revisar y añadir la explicación personal del aprendizaje si el docente la solicita.')

for x in ['1. Dos POM originales y dos actualizados: carpetas parte1/pom-originales y parte1/pom-actualizados.','2. Este informe técnico, con cambios y justificación.','3. Dos ligas GitHub: carpeta Ejercicio5Cliente y carpeta Ejercicio5MyAPIGestor de la cuenta elmanee.','4. Reportes JaCoCo HTML/XML, resultados JUnit/Failsafe y prueba funcional.','5. SBOM antes/después, reportes Trivy y clasificación de incidencias, sin ocultar los avisos residuales.','6. Evidencia de ejecuciones exitosas de los tres jobs Jenkins y resultados SonarQube, con sus enlaces y capturas.','7. Video que muestre las ejecuciones y explique cambios, problemas y verificación. El guion adjunto sirve como apoyo; la explicación personal del aprendizaje debe revisarla el estudiante.']:

 p(x)

h('Ligas del proyecto')

p('Cliente: https://github.com/elmanee/gestor-proyectos-java21/tree/main/Ejercicio5Cliente')

p('API: https://github.com/elmanee/gestor-proyectos-java21/tree/main/Ejercicio5MyAPIGestor')

p('Los dos enlaces corresponden a dos proyectos independientes dentro de un mismo repositorio. Si la plataforma exige dos repositorios distintos, debe confirmarse ese formato con el docente.')

h('Fuentes técnicas')

for link in ['https://docs.spring.io/spring-boot/3.5/system-requirements.html','https://www.jacoco.org/jacoco/','https://maven.apache.org/surefire/','https://trivy.dev/docs/latest/coverage/language/java/','https://trivy.dev/docs/latest/configuration/skipping/','https://spring.io/security/cve-2026-47884/','https://spring.io/security/cve-2026-47890/']:

 story.append(Paragraph('<link href="'+link+'" color="#276278">'+escape(link)+'</link>',styles['SmallBody']));md.append('- '+link)

p('Criterios de entrega: documento proporcionado por el estudiante, Practica 3.pdf. Este informe distingue resultados verificados de las etapas que aún figuren en ejecución en el resumen de Jenkins.')



def footer(canvas,doc):

 canvas.saveState();canvas.setStrokeColor(colors.HexColor('#bcccd7'));canvas.line(40,40,A4[0]-40,40);canvas.setFont('Helvetica',8);canvas.setFillColor(colors.HexColor('#526775'));canvas.drawString(40,27,'Práctica 3 | Java 21 y CI | 19 noviembre 2026');canvas.drawRightString(A4[0]-40,27,str(doc.page));canvas.restoreState()

out=R/'output/pdf';out.mkdir(parents=True,exist_ok=True)

SimpleDocTemplate(str(out/'Informe-Practica-3.pdf'),pagesize=A4,rightMargin=40,leftMargin=40,topMargin=43,bottomMargin=54,title='Practica 3 - Java 21 y pipelines CI',author='elmanee').build(story,onFirstPage=footer,onLaterPages=footer)

shutil.copy2(out/'Informe-Practica-3.pdf',E/'Informe-Practica-3.pdf');(E/'Informe-tecnico.md').write_text('\n\n'.join(md),encoding='utf-8')

(E/'estado-verificacion.json').write_text(json.dumps({'coverage':coverage,'jenkins':jenkins,'security':{str(k):dict(v) for k,v in counts.items()}},ensure_ascii=False,indent=2),encoding='utf-8')

print('Informe generado:',out/'Informe-Practica-3.pdf')

