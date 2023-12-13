package framework.database.query.connections;

import framework.database.DatabaseConnection;
import framework.database.DatabaseConnector;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PostgreSQL extends DatabaseConnection {

    public PostgreSQL(Connection connection) {
        super(connection);
    }

    public ResultSet select(String query, List<Object> bindings) {
        try {
            Connection connection = DatabaseConnector.getConnection().connection();
            PreparedStatement ps = connection.prepareStatement(query);
            bind(ps, bindings);
            return ps.executeQuery();
        } catch (NullPointerException | SQLException e) {
            e.printStackTrace();
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    @Override
    public int insert(String query, List<Object> bindings) {
        try {
            Connection connection = DatabaseConnector.getConnection().connection();
            PreparedStatement ps = connection.prepareStatement(query);
            bind(ps, bindings);
            return ps.executeUpdate();
        } catch (NullPointerException | SQLException e) {
            e.printStackTrace();
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public int update(String query, List<Object> bindings) {
        try {
            Connection connection = DatabaseConnector.getConnection().connection();
            PreparedStatement ps = connection.prepareStatement(query);
            bind(ps, bindings);
            return ps.executeUpdate();
        } catch (NullPointerException | SQLException e) {
            e.printStackTrace();
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    @Override
    public int delete(String query, List<Object> bindings) {
        try {
            Connection connection = DatabaseConnector.getConnection().connection();
            PreparedStatement ps = connection.prepareStatement(query);
            bind(ps, bindings);
            return ps.executeUpdate();
        } catch (NullPointerException | SQLException e) {
            e.printStackTrace();
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}
