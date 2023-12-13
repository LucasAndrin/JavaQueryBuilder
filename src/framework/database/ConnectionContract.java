package framework.database;

import java.sql.ResultSet;
import java.util.List;

public interface ConnectionContract {
    ResultSet select(String query, List<Object> bindings);
    int insert(String query, List<Object> bindings);
    int update(String query, List<Object> bindings);
    int delete(String query, List<Object> bindings);
}
