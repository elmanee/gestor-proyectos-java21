# Cliente - Java 21

Requiere JDK 21. Incluye Maven Wrapper 3.9.11.

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd spring-boot:run
```

Interfaz: http://localhost:8082. La API usa http://localhost:8081; se puede cambiar con `API_URL`.

El `Jenkinsfile` ejecuta compilación, pruebas, JaCoCo, Trivy y SonarQube. El directorio de trabajo esperado es la raíz del repositorio padre, que contiene ambos proyectos.

El reporte JaCoCo se genera en `target/site/jacoco/index.html`; `verify` exige 100% de cobertura de métodos. Solo se excluye la clase de arranque `*Application` de la medición de cobertura; no se excluye código funcional. Los DTO y controladores sí se miden.

La entrega y sus evidencias se encuentran en `entrega/`.
