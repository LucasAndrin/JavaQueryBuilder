package framework.database.eloquent;

import framework.database.DatabaseConnector;

import java.sql.Connection;

public class QueryBuilder {

    Connection connection;

    public QueryBuilder() {
        connection = DatabaseConnector.getConnection();
    }

}
