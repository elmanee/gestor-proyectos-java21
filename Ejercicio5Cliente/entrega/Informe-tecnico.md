# Actualización de microservicios y pipelines CI

Práctica 3 - Recuperación. Informe técnico y evidencias verificables. Fecha de preparación: 19 de noviembre de 2026.


Repositorio del estudiante: https://github.com/elmanee/gestor-proyectos-java21. El proyecto original y parte de las pruebas provienen de Enrique1189/admin-proyectos; se conserva su atribución. Las ligas de entrega apuntan a la copia del estudiante.



## 1. Actualización tecnológica

| Componente | Estado original local | Estado actualizado |
| --- | --- | --- |
| Cliente | Java 17; Spring Boot 3.2.5 | Java 21; Spring Boot 3.5.16 |
| API | Java 11; Spring Boot 2.7.17 | Java 21; Spring Boot 3.5.16 |
| Jenkins | Contenedor existente detenido | Jenkins 2.580.2; Temurin 21.0.13 |
| Maven | Wrapper incompleto; Maven de IntelliJ | Maven Wrapper y herramienta Jenkins 3.9.11 |
| Plugins | Sin configuración completa en los POM locales | Surefire 3.5.5; JaCoCo 0.8.14; Sonar Maven 5.5.0.6356 |
| Seguridad de dependencias | Versiones heredadas originales | Jackson 2.21.7; Tomcat 10.1.59; Log4j 2.25.5; MariaDB JDBC 3.5.11 |


Los dos POM originales se copiaron antes de modificarlos. Los dos actualizados son copias de los POM finales. Se conservan en parte1/pom-originales y parte1/pom-actualizados. Java 21 se fija tanto en java.version como en maven-compiler-plugin release=21; no basta con cambiar el JDK del IDE.


La propiedad de parches evita conservar dependencias vulnerables aun cuando una versión de Spring Boot sea compatible con Java 21. CycloneDX genera un inventario resuelto en ci-reports/bom.json, fuera de target.



## 2. Justificación de cambios de código

La API migró de javax.persistence a jakarta.persistence. Los identificadores usan GenerationType.IDENTITY para respetar las columnas AUTO_INCREMENT existentes. Se incorporaron convertidores de fechas DATE/DATETIME para mantener el contrato JSON de cadenas y validar el esquema de MariaDB sin recrear tablas.


La conexión usa DB_URL, DB_USER y DB_PASSWORD mediante variables de entorno. Hibernate usa ddl-auto=validate y no modifica la estructura de dbgestor. La contraseña no se incluye en POM, Jenkinsfile ni configuración publicada. El esquema de instalación contiene estructura vacía, sin registros personales.



## 3. Correcciones funcionales y de seguridad

El cliente usa el puerto 8082 para convivir con Jenkins en 8080; la API conserva 8081. La dirección de API se configura mediante API_URL. RestTemplate se inyecta para poder vincular el mismo cliente HTTP a MockRestServiceServer: las pruebas no dependen del backend real.


Se corrigieron las respuestas de comentarios y la ruta /tarea, el envío doble del formulario, fondos sin extensión, referencias a elementos inexistentes y controles iniciales sin validación. Se retiraron tareas de ejemplo que aparecían mientras cargaban las reales. La fecha de comentario utiliza formato ISO compatible con DATETIME.


Las contraseñas nuevas y modificadas usan BCrypt y se omiten de todas las respuestas JSON. Los registros antiguos en texto plano conservan compatibilidad temporal para no invalidar cuentas de la práctica; esto es un riesgo residual identificado. Deben actualizarse esas contraseñas antes de usar el sistema con datos reales.


La API conserva el esquema de autorización original del proyecto educativo: el login de la interfaz no implementa control de acceso por usuario o rol en cada endpoint. Es un riesgo residual que requiere una mejora específica antes de usar datos reales o exponer la aplicación fuera del laboratorio.


Se escapa el texto mostrado por plantillas JavaScript y se evita interpolar nombres directamente como código en acciones de borrado. Los fondos se restringen a recursos locales esperados. Se sustituyeron correos personales del ejemplo por contactos ficticios. El borrado de proyecto y tarea se realiza en una transacción e incluye comentarios y asignaciones relacionados.



## 4. Pruebas y cobertura

| Servicio | Pruebas | Fallos | Métodos | Líneas | Ramas |
| --- | --- | --- | --- | --- | --- |
| cliente | 52 | 0 | 94/94 = 100% | 271/271 = 100.0% | 6/6 = 100.0% |
| api | 16 | 0 | 114/114 = 100% | 304/304 = 100.0% | 62/82 = 75.6% |


Se mide código Java evaluable: controladores, modelos, persistencia y servicio transaccional. Solo se excluyen de JaCoCo las clases *Application de arranque. No se excluyen métodos funcionales para elevar el porcentaje. El umbral de métodos es 1.00 y Maven verify falla si no se alcanza. La cobertura de ramas se informa por separado: 100% de métodos no significa 100% de ramas ni prueba ausencia de defectos.


Además, 11 pruebas de JavaScript propio ejecutadas con Node, jsdom y c8 verifican formularios, sesión, errores HTTP, tarjetas, tareas y comentarios. Resultado: 100% de líneas y funciones; 96.9% de ramas. Se importa coverage/lcov.info en SonarQube. npm audit reportó cero vulnerabilidades tras fijar glob 14.0.6 y minimatch 11.0.9 en las herramientas de prueba.


Las pruebas verifican CRUD de cinco entidades, filtrado por proyecto/tarea, persistencia de cambios, credenciales válidas e inválidas, protección y conservación de contraseñas, fechas válidas e inválidas, respuestas HTTP y borrado relacionado. H2 en modo MariaDB permite pruebas aisladas. MariaDbIT valida además el esquema y operaciones SQL contra una MariaDB real separada, y solo acepta la base gestor_ci.


El script funcional crea datos con prefijo QA_ y elimina solo sus propios registros. Su último resultado contiene 87 comprobaciones; 0 fallos. Los reportes HTML/XML de JaCoCo y JUnit se adjuntan, junto con resultados de Failsafe para MariaDB.



## 5. Análisis de vulnerabilidades

Se ejecutó Trivy 0.67.2 sobre código fuente para secretos y configuración, excluyendo target, .git, .idea, entrega, evidencias, tmp y .secrets. Además se escanearon inventarios CycloneDX completos de antes y después, evitando confundir un POM sin versiones resueltas con ausencia de vulnerabilidades. No se excluye código fuente propio del escaneo de seguridad.


| Servicio | Momento | Críticas | Altas | Medias | Bajas | Total |
| --- | --- | --- | --- | --- | --- | --- |
| cliente | antes | 15 | 32 | 31 | 14 | 92 |
| cliente | despues | 2 | 0 | 0 | 0 | 2 |
| api | antes | 10 | 43 | 49 | 16 | 118 |
| api | despues | 2 | 0 | 0 | 0 | 2 |


Los totales son hallazgos por componente, no CVE únicos entre los dos servicios. Los reportes originales permanecen completos. clasificacion-dependencias.csv documenta cada hallazgo, versión, severidad y atención; los SBOM permiten verificar las versiones efectivamente utilizadas.


| Incidencia | Clasificación y atención |
| --- | --- |
| Dependencias de aplicación | Actualizar Spring Boot y los parches específicos de Jackson, Tomcat, Log4j y MariaDB. Confirmar mediante nuevo inventario y escaneo. |
| CVE-2026-47884 | Aviso residual por Spring MVC 6.2.19. Requiere XsltView y mapeo de vistas amplio sin nombre explícito. El sistema usa vistas Thymeleaf explícitas y no configura XsltView. No aplicable a la configuración evaluada; no se oculta el hallazgo. |
| CVE-2026-47890 | Aviso residual que requiere SSE con fragmentos de vistas y datos controlados por atacante. El sistema no tiene endpoints SSE ni FragmentsRendering. Se verifica en SecurityConfigurationTest. Reevaluar si se añaden esas funciones. |
| Contraseñas y configuración | BCrypt para altas y cambios; password de BD por entorno; no se devuelve contrasena en JSON. La compatibilidad de cuentas antiguas se declara como residual. |
| HTML dinámico | Escapar texto y evitar nombres interpolados como código; contactos ficticios en recursos de demostración. |


El analisis final de SonarQube aprueba los tres Quality Gates. Se corrigieron nueve bugs HTML/CSS y las declaraciones var del JavaScript propio. Quedan avisos de mantenibilidad: 183 en frontend, 111 en backend y 3 en persistencia, conservados en los JSON y CSV. El hotspot javascript:S2245 fue revisado como Safe: Math.random solo elige un color de avatar, no un secreto. La interfaz confirma 100% de hotspots revisados. Un Quality Gate aprobado no equivale a ausencia de deuda tecnica.


Los dos avisos residuales se justifican con condiciones publicadas por Spring, inspección del código y pruebas de configuración. No se declaran corregidas las bibliotecas afectadas ni se eliminan sus resultados para aparentar cero hallazgos. El análisis no es una auditoría completa de producción ni de las imágenes Docker del entorno.



## 6. Tres pipelines

| Pipeline | Resultado registrado | Liga local |
| --- | --- | --- |
| gestor-frontend | SUCCESS | http://localhost:8080/job/gestor-frontend/ |
| gestor-backend | SUCCESS | http://localhost:8080/job/gestor-backend/ |
| gestor-database | SUCCESS | http://localhost:8080/job/gestor-database/ |


Cada Jenkinsfile obtiene el código de GitHub, muestra java -version y mvn -version, ejecuta pruebas y cobertura, escanea fuente y SBOM con Trivy, envía análisis a SonarQube y espera el Quality Gate. Publica resultados JUnit y archiva reportes JaCoCo, inventario y seguridad. El pipeline de base de datos activa además el perfil database-ci de Failsafe.


La primera ejecución del frontend detectó cobertura combinada insuficiente (71.2%). Se corrigió agregando pruebas reales del JavaScript propio y su reporte LCOV, conservando el Quality Gate y el código funcional dentro del análisis.


Jenkins usa las herramientas JDK21 y Maven3.9.11. Node se utiliza para analizar JavaScript propio; las librerías de terceros distribuidas ya minificadas se excluyen de Sonar, no el código escrito para el sistema. JaCoCo mide Java y c8 mide JavaScript; los dos reportes se importan en SonarQube.


El token de SonarQube es de análisis, con expiración de 30 días, y se guarda como credencial gestor-sonar-token. La base aislada usa gestor-db-ci. No se incluyen sus valores en el repositorio ni en las evidencias. La base real sigue en puerto 3307 y la de CI en 13307; MariaDbIT impide apuntar a otra base distinta de gestor_ci.


Archivos de pipeline: Ejercicio5Cliente/Jenkinsfile; Ejercicio5MyAPIGestor/Jenkinsfile; Ejercicio5MyAPIGestor/database/Jenkinsfile. El script ci/jenkins-config.groovy configura herramientas y jobs del laboratorio local. Los registros de compilación muestran si cada etapa se ejecutó realmente; la existencia del Jenkinsfile por sí sola no acredita un pipeline exitoso.



## 7. Repetición de la práctica

En cada carpeta, con JDK 21: mvnw.cmd clean verify en Windows o ./mvnw clean verify en Linux. Para ejecutar la API, establecer DB_PASSWORD y usar mvnw.cmd spring-boot:run. Para el cliente, usar el mismo comando y abrir http://localhost:8082. El archivo .env.example es documentación: Spring no carga .env automáticamente.


Para la prueba SQL, configurar DB_CI_URL=jdbc:mariadb://localhost:13307/gestor_ci y las credenciales de la base aislada, y ejecutar mvnw.cmd verify -Pdatabase-ci. Ese perfil recrea las tablas de gestor_ci, nunca las de dbgestor. Para repetir las comprobaciones funcionales con los servicios activos: python verificar_funcional.py.



## 8. Qué entregar

Se adjunta Video-evidencia-Practica-3.mp4: resumen de 4 min 46 s con capturas reales y narración sintética en español. No es una grabación continua ni representa la voz o la experiencia personal del estudiante. El guion permite revisar y añadir la explicación personal del aprendizaje si el docente la solicita.


1. Dos POM originales y dos actualizados: carpetas parte1/pom-originales y parte1/pom-actualizados.


2. Este informe técnico, con cambios y justificación.


3. Dos ligas GitHub: carpeta Ejercicio5Cliente y carpeta Ejercicio5MyAPIGestor de la cuenta elmanee.


4. Reportes JaCoCo HTML/XML, resultados JUnit/Failsafe y prueba funcional.


5. SBOM antes/después, reportes Trivy y clasificación de incidencias, sin ocultar los avisos residuales.


6. Evidencia de ejecuciones exitosas de los tres jobs Jenkins y resultados SonarQube, con sus enlaces y capturas.


7. Video que muestre las ejecuciones y explique cambios, problemas y verificación. El guion adjunto sirve como apoyo; la explicación personal del aprendizaje debe revisarla el estudiante.



## Ligas del proyecto

Cliente: https://github.com/elmanee/gestor-proyectos-java21/tree/main/Ejercicio5Cliente


API: https://github.com/elmanee/gestor-proyectos-java21/tree/main/Ejercicio5MyAPIGestor


Los dos enlaces corresponden a dos proyectos independientes dentro de un mismo repositorio. Si la plataforma exige dos repositorios distintos, debe confirmarse ese formato con el docente.



## Fuentes técnicas

- https://docs.spring.io/spring-boot/3.5/system-requirements.html

- https://www.jacoco.org/jacoco/

- https://maven.apache.org/surefire/

- https://trivy.dev/docs/latest/coverage/language/java/

- https://trivy.dev/docs/latest/configuration/skipping/

- https://spring.io/security/cve-2026-47884/

- https://spring.io/security/cve-2026-47890/

Criterios de entrega: documento proporcionado por el estudiante, Practica 3.pdf. Este informe distingue resultados verificados de las etapas que aún figuren en ejecución en el resumen de Jenkins.
