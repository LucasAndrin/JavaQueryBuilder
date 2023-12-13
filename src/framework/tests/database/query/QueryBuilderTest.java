package framework.tests.database.query;

import framework.database.DatabaseConnector;
import framework.database.query.QueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QueryBuilderTest {
    private QueryBuilder builder;
    @BeforeEach
    void newQueryBuilder() {
        builder = new QueryBuilder(DatabaseConnector.getConnection());
    }
    @Test
    void compileSelectAllTest() {
        assertEquals(
                "select * from users",
                builder.select().from("users").toSql()
        );
    }
    @Test
    void compileSelectColumnsTest() {
        assertEquals(
                "select id, name from users",
                builder.select(new String[]{
                        "id",
                        "name"
                }).from("users").toSql()
        );
    }
    @Test
    void compileSelectAllWhereTest() {
        assertEquals(
                "select * from users where id = ?",
                builder.select().from("users").where("id", 1).toSql()
        );

        assertEquals(
                "select * from users where id = ? and name ilike ?",
                builder.where("name", "ilike", "%Lucas%").toSql()
        );

        assertEquals(
                "select * from users where id = ? and name ilike ? and (age = ?)",
                builder.where(query -> {
                    query.where("age", 10);
                }).toSql()
        );
    }
    @Test
    void selectAllTest() {
        assertDoesNotThrow(() -> {
            ResultSet rs = builder.from("users").get();
            if (rs != null) {
                while (rs.next()) {
                    System.out.println(rs.getInt(1));
                }
            }
        });
    }
    @Test
    void selectWhereTest() {
        assertDoesNotThrow(() -> {
            ResultSet rs = builder.from("users").where("name", "Lucas").get();
            if (rs != null) {
                rs.next();
                assertEquals(7, rs.getInt(1));
            }
        });
    }

    @Test
    void insertTest() {
        Map<String, Object> data = new HashMap<>();

        assertEquals(0, builder.from("users").insert(data));

        data.put("name", "José");
        data.put("email", "jose@gmail.com");
        data.put("password", "1234");

        assertEquals(1, builder.insert(data));
    }

    @Test
    void updateTest() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", "Curvello");
        data.put("email", "curvello@gmail.com");
        data.put("password", "1234");

        assertEquals(1, builder.from("users").where("id", 7).update(data));
    }

    @Test
    void deleteTest() {
        assertEquals(1, builder.from("users").where("id", 3).delete());
    }
}