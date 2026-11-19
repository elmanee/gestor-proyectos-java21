package com.mx.cbtis.gestor;
import org.junit.jupiter.api.Test;
import java.sql.*;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;
class MariaDbIT {
    @Test void esquemaYCicloSqlEnMariaDbAislada() throws Exception {
        String url=System.getenv("DB_CI_URL");
        assertNotNull(url,"DB_CI_URL debe apuntar a la base aislada gestor_ci");
        assertTrue(url.endsWith("/gestor_ci"),"Solo se permite la base gestor_ci para esta prueba");
        try(Connection c=DriverManager.getConnection(url,System.getenv("DB_CI_USER"),System.getenv("DB_CI_PASSWORD"))) {
            try(Statement s=c.createStatement()) {
                for(String table:new String[]{"asignaciones","comentarios","tareas","proyectos","usuario"}) s.execute("DROP TABLE IF EXISTS "+table);
                for(String sql:Files.readString(Path.of("database/schema.sql")).split(";")) if(!sql.isBlank()) s.execute(sql);
            }
            c.setAutoCommit(false);
            try(Statement s=c.createStatement()) {
                s.executeUpdate("INSERT INTO proyectos(nombre) VALUES ('QA SQL')",Statement.RETURN_GENERATED_KEYS);
                int id;try(ResultSet keys=s.getGeneratedKeys()){assertTrue(keys.next());id=keys.getInt(1);}
                try(PreparedStatement q=c.prepareStatement("SELECT nombre FROM proyectos WHERE id_proyecto=?")){q.setInt(1,id);try(ResultSet rs=q.executeQuery()){assertTrue(rs.next());assertEquals("QA SQL",rs.getString(1));}}
                try(PreparedStatement q=c.prepareStatement("UPDATE proyectos SET nombre=? WHERE id_proyecto=?")){q.setString(1,"QA editado");q.setInt(2,id);assertEquals(1,q.executeUpdate());}
                try(PreparedStatement q=c.prepareStatement("DELETE FROM proyectos WHERE id_proyecto=?")){q.setInt(1,id);assertEquals(1,q.executeUpdate());}
                for(String table:new String[]{"asignaciones","comentarios","tareas","proyectos","usuario"}) {
                    try(ResultSet rs=s.executeQuery("SELECT count(*) FROM "+table)){assertTrue(rs.next());assertEquals(0,rs.getInt(1));}
                }
            } finally {c.rollback();}
        }
    }
}
