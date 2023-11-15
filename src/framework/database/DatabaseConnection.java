package framework.database;

import framework.configuration.Env;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {
    public static Connection connect() {
        Connection connection = null;
        try {
            String dbConnection = Env.get("DB_CONNECTION");
            String url = "jdbc:" + dbConnection + "://" + Env.get("DB_HOST") + ':' + Env.get("DB_PORT") + '/' + Env.get("DB_DATABASE");

            Properties props = new Properties();
            props.setProperty("user", Env.get("DB_USERNAME"));
            props.setProperty("password", Env.get("DB_PASSWORD"));

            connection = DriverManager.getConnection(url, props);

            System.out.println("\u001B[32m" + "Connected to the " + dbConnection + "\u001B[32m" + " server successfully.");
        } catch (SQLException exception) {
            System.out.println(exception.getMessage());
        }

        return connection;
    }
}
