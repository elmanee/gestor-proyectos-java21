# Gestor de proyectos — Java 21 y CI

Práctica 3 de elmanee. Código original: [Enrique1189/admin-proyectos](https://github.com/Enrique1189/admin-proyectos).

## Proyectos

- [Cliente](Ejercicio5Cliente): Java 21, interfaz en http://localhost:8082.
- [API](Ejercicio5MyAPIGestor): Java 21, servicio en http://localhost:8081.

En cada carpeta: `mvnw.cmd clean verify` (Windows) o `./mvnw clean verify` (Linux), con JDK 21. Para el JavaScript del cliente: Node 22 compatible, `npm ci --ignore-scripts` y `npm test`.

## Evidencias

- [Informe técnico PDF](Ejercicio5Cliente/entrega/Informe-Practica-3.pdf)
- [Explicación de cambios](Ejercicio5Cliente/entrega/Informe-tecnico.md)
- [Dos POM originales y dos actualizados](Ejercicio5Cliente/entrega/parte1)
- [Resultados de Jenkins](Ejercicio5Cliente/entrega/parte2/jenkins/resumen.json): frontend #3, backend #2 y database #2 en SUCCESS.
- [Resultados de SonarQube](Ejercicio5Cliente/entrega/parte2/seguridad/resumen-sonar.json): tres Quality Gates OK.
- [Qué entregar](Ejercicio5Cliente/entrega/QUE-ENTREGAR.md)

Pruebas verificadas: cliente 52 Java + 11 JavaScript; API 16; prueba SQL en MariaDB aislada; 87 comprobaciones funcionales HTTP. JaCoCo exige 100% de métodos Java evaluables, con exclusión de la clase de arranque. Se declaran por separado las ramas, la deuda de mantenibilidad y los avisos residuales de dependencias.

Los tres Jenkinsfiles están en cliente, API y API/database. El laboratorio usa JDK21, Maven3.9.11, Node 22, Trivy y credenciales Jenkins `gestor-sonar-token` y `gestor-db-ci`. Sus valores no se publican. Los nombres y rutas de herramientas corresponden al Jenkins local documentado; deben configurarse al repetirlo en otro equipo.

La API toma DB_URL, DB_USER y DB_PASSWORD del entorno y valida el esquema sin recrear dbgestor. Consulte su README y database/schema.sql para una instalación vacía. El perfil database-ci solo admite la base gestor_ci: recrea sus tablas de prueba. El archivo .env.example documenta variables, no se carga automáticamente.

El ZIP de entrega y el video narrado se conservan localmente para entregar al docente. El video usa capturas reales y voz sintética; el estudiante debe revisar y añadir su explicación personal si se requiere. Los informes conservan los riesgos residuales y no presentan el laboratorio como listo para producción.
