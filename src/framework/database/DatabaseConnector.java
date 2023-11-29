package framework.database;

import framework.configuration.Env;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnector {

    private static Connection connection;

    public static synchronized Connection getConnection() {
        if (connection == null) {
            connect();
        }
        return connection;
    }

    public static void connect() {
        try {
            String dbConnection = Env.get("DB_CONNECTION");
            String url = "jdbc:" + dbConnection + "://" + Env.get("DB_HOST") + ':' + Env.get("DB_PORT") + '/' + Env.get("DB_DATABASE");

            Properties props = new Properties();
            props.setProperty("user", Env.get("DB_USERNAME"));
            props.setProperty("password", Env.get("DB_PASSWORD"));

            connection = DriverManager.getConnection(url, props);

            System.out.println("\u001B[32m" + "Connected to the " + dbConnection + "\u001B[32m" + " server successfully.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
