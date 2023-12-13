package framework.database;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public abstract class DatabaseConnection implements ConnectionContract {
    Connection connection;

    public DatabaseConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection connection() {
        return connection;
    }

    protected int bind(PreparedStatement ps, List<?> bindings) throws SQLException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        return bind(ps, bindings, 1);
    }

    protected int bind(PreparedStatement ps, List<?> bindings, int index) throws SQLException {
        for (Object binding : bindings) {
            ps.setObject(index, binding);
            index++;
        }
        return index;
    }
}
