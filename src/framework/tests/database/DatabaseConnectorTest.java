package framework.tests.database;

import framework.database.DatabaseConnector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectorTest {
    @Test
    void getConnectionTest() {
        assertEquals(DatabaseConnector.getConnection(), DatabaseConnector.getConnection());
    }
}