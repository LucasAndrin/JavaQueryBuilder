package framework.database.query.grammars;

import framework.database.BuilderContract;
import framework.database.query.QueryBuilder;

import java.util.List;
import java.util.Map;

public interface GrammarContract {
    String compileSelect(QueryBuilder query);
//    public String compileExists(QueryBuilder query);
    String compileInsert(QueryBuilder queryBuilder, Map<String, Object> values);
    Object compileUpdate(QueryBuilder queryBuilder, Map<String, Object> values);

    List<Object> prepareBindingsForUpdate(Map<String, List<Object>> bindings, Map<String, Object> values);
    public String compileDelete(QueryBuilder query);
    List<Object> prepareBindingsForDelete(Map<String, List<Object>> bindings);
}
