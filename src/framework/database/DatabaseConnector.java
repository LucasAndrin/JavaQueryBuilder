package framework.database;

import framework.configuration.Env;
import framework.database.query.connections.PostgreSQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnector {

    private static DatabaseConnection connection;

    public static synchronized DatabaseConnection getConnection() {
        if (connection == null) {
            connect();
        }
        return connection;
    }

    private static void connect() {
        try {
            String dbConnection = Env.get("DB_CONNECTION");
            String url = "jdbc:" + dbConnection + "://" + Env.get("DB_HOST") + ':' + Env.get("DB_PORT") + '/' + Env.get("DB_DATABASE");

            Properties props = new Properties();
            props.setProperty("user", Env.get("DB_USERNAME"));
            props.setProperty("password", Env.get("DB_PASSWORD"));

            Connection databaseConnection = DriverManager.getConnection(url, props);
            connection = new PostgreSQL(databaseConnection);

            System.out.println("Connected to the " + dbConnection + " server successfully.");
        } catch (SQLException e) {
            connection = null;
            System.out.println(e.getMessage());
        }
    }
}
