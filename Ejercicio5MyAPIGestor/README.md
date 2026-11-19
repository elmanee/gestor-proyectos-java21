# API Gestor - Java 21

Requiere JDK 21. El Maven Wrapper fija Maven 3.9.11.

## Pruebas aisladas

```powershell
.\mvnw.cmd clean verify
```

Las pruebas usan H2 con compatibilidad MariaDB y no tocan `dbgestor`. JaCoCo exige 100% de cobertura de métodos, excluyendo únicamente la clase de arranque.

## Ejecutar contra MariaDB existente

Configura `DB_PASSWORD` en tu terminal. `DB_URL` y `DB_USER` son opcionales; sus valores predeterminados apuntan a MariaDB local en 3307, base `dbgestor`, usuario `root`. Para otro entorno usa una cuenta dedicada. `.env.example` documenta las variables, pero Spring no carga automáticamente un archivo `.env`.

```powershell
$env:DB_PASSWORD = Read-Host 'Contraseña de MariaDB' -MaskInput
.\mvnw.cmd spring-boot:run
```

API: http://localhost:8081. Hibernate valida el esquema sin modificarlo. Para una base vacía importa primero `database/schema.sql`, que solo contiene estructura.

## Pipeline de base de datos

`database/Jenkinsfile` usa una MariaDB separada en puerto 13307. La prueba `MariaDbIT` solo acepta la base `gestor_ci`. Requiere las variables `DB_CI_URL`, `DB_CI_USER`, `DB_CI_PASSWORD`.

```powershell
.\mvnw.cmd verify -Pdatabase-ci
```

Este perfil recrea únicamente las tablas de la base de pruebas `gestor_ci`. No debe apuntarse a datos que quieras conservar.

## Jenkins

El repositorio padre contiene ambos proyectos. El pipeline usa JDK21, Maven3.9.11, Trivy y las credenciales Jenkins `gestor-db-ci` y `gestor-sonar-token`. No guardes contraseñas ni tokens en los Jenkinsfiles.

Las nuevas contraseñas se almacenan con BCrypt y no se devuelven en JSON. Se mantiene compatibilidad temporal con cuentas antiguas de la práctica cuyos registros aún tienen texto plano; su contraseña se protege al actualizarla.
