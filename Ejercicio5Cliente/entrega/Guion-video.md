# Guion de video - Practica 3

Duración sugerida: 3 a 4 minutos. Revisa el texto y explica con tus propias palabras lo que comprendes. No muestres contraseñas ni tokens.

1. Presentación (20 s): abre tu repositorio `elmanee/gestor-proyectos-java21`. Muestra las carpetas de ambos microservicios y sus POM originales y actualizados. Explica que el cliente estaba en Java 17 y la API en Java 11.
2. Configuración (25 s): en Jenkins muestra las herramientas JDK21 y Maven3.9.11. En una consola de build señala `Java version: 21` y `javac release 21`.
3. Pipelines (60 s): muestra `gestor-frontend`, `gestor-backend` y `gestor-database`. Entra a una ejecución real, sus etapas y resultado final. Explica checkout, pruebas/cobertura, Trivy y SonarQube. No afirmes que un job pasó si aparece rojo o sigue ejecutándose.
4. Pruebas (30 s): abre el reporte JaCoCo de cada servicio. Señala 100% de métodos Java evaluables y aclara que la cobertura de ramas es una métrica distinta. La exclusión corresponde únicamente a la clase de arranque, no a controladores o servicios.
5. Seguridad (40 s): muestra los SBOM y el resumen de Trivy. El inventario original tenía 92 hallazgos en cliente y 118 en API, por componente. Explica las actualizaciones de dependencias. Los dos avisos residuales de Spring requieren XSLT/SSE, ausentes en esta configuración; se mantienen visibles y justificados.
6. Base de datos (20 s): enseña el resultado de MariaDbIT. Explica que `gestor_ci` se prueba en una MariaDB aparte y que `dbgestor` no se recrea durante las pruebas.
7. Problemas y aprendizaje (30 s): explica que cambiar solo la versión del IDE no alcanza; hubo que actualizar POM, Jakarta, mapeo de fechas y plugins. También se corrigió la prueba que simulaba un RestTemplate diferente al usado por el controlador. Comenta la diferencia entre prueba, cobertura, análisis estático y vulnerabilidades de dependencias.

Ligas de evidencia:
- http://localhost:8080/job/gestor-frontend/
- http://localhost:8080/job/gestor-backend/
- http://localhost:8080/job/gestor-database/
- http://localhost:9000/dashboard?id=gestor-frontend
- http://localhost:9000/dashboard?id=gestor-backend
- http://localhost:9000/dashboard?id=gestor-database

Los enlaces localhost funcionan en tu equipo. Para Classroom adjunta el video y las capturas/exportaciones; no dependas únicamente de esos enlaces.


Además se adjunta un video narrado con voz sintética, capturas reales y resultados exportados (4 min 46 s). No es captura continua ni testimonio personal. Revisa los conceptos y añade tu explicación si el docente exige voz o participación propia.

Pruebas JavaScript: 11 casos, 100% líneas y funciones, 96.9% ramas. La cobertura combinada pasó a 99.7% al incorporar LCOV; no se ocultó el JavaScript propio.
