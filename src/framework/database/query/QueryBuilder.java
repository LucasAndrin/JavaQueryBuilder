package framework.database.query;

import framework.database.BuilderContract;
import framework.database.DatabaseConnection;
import framework.database.query.grammars.Grammar;
import framework.database.query.where.WhereBitwise;
import framework.database.query.where.WhereClause;
import framework.database.query.where.WhereClosure;

import javax.xml.transform.Result;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class QueryBuilder implements BuilderContract {

    /**
     * The database connecton instance.
     */
    DatabaseConnection connection;

    /**
     * The database query grammar instance.
     */
    public Grammar grammar;

    /**
     * The valid keys of bindings.
     */
    private final String[] bindingKeys = {
            "select",
            "from",
            "join",
            "where",
            "groupBy",
            "having",
            "order",
            "union",
            "unionOrder"
   };

    /**
     * The current query value bidings.
     */
    private Map<String, List<Object>> bindings = new HashMap<>();

    /**
     * The columns that should be returned.
     */
    public List<String> columns;

    /**
     * Indicates if the query returns distinct results.
     */
    public boolean distinct = false;

    /**
     * The table which the query is targeting.
     */
    public String from;

    /**
     * The table joins for the query.
     */
    public List<Object> joins = new ArrayList<>();

    /**
     * The where constraints for the query.
     */
    public List<WhereClause> wheres = new ArrayList<>();

    /**
     * The groupings for the query.
     */
    public List<Object> groups = new ArrayList<>();

    /**
     * The having constraints for the query.
     */
    public List<Object> havings = new ArrayList<>();


    /**
     * The orderings for the query.
     */
    public List<Object> orders = new ArrayList<>();

    /**
     * The oderings for the query
     */
    public int limit;

    /**
     * The number of records to skip.
     */
    public int offset;

    /**
     * The query union statements.
     */
    public List<Object> unions = new ArrayList<>();

    /**
     * The maximum number of union records to return.
     */
    public int unionLimit;

    /**
     * The number of union records to skip.
     */
    public int unionOffset;

    /**
     * The orderings for the union query.
     */
    public List<Object> unionOrders = new ArrayList<>();

    /**
     * Indicates whether row locking is being used.
     */
    public boolean $lock;

    public String[] operators = {
        "=", "<", ">", "<=", ">=", "<>", "!=", "<=>",
        "like", "like binary", "not like", "ilike",
        "&", "|", "^", "<<", ">>", "&~", "is", "is not",
        "rlike", "not rlike", "regexp", "not regexp",
        "~", "~*", "!~", "!~*", "similar to",
        "not similar to", "not ilike", "~~*", "!~~*",
    };

    public QueryBuilder(DatabaseConnection connection) {
        this.connection = connection;
        grammar = new Grammar();
        initializeDefaultBindings();
    }



    private void initializeDefaultBindings() {
        for (String key : bindingKeys) {
            bindings.put(key, new ArrayList<>());
        }
    }

    /**
     * Set columns to be selected as *
     * @return QueryBuilder
     */
    public QueryBuilder select() {
        return select(new String[]{"*"});
    }

    public QueryBuilder select(String column) {
        return select(new String[]{column});
    }

    /**
     * Set the columns to be selected.
     * @param columns String[]
     * @return QueryBuilder
     */
    public QueryBuilder select(String[] columns) {
        this.columns = new ArrayList<>(Arrays.asList(columns));
        return this;
    }


    /**
     * Set the table which the query is targeting.
     * @param table String
     * @return QueryBuilder
     */
    public QueryBuilder from(String table) {
        return from(table, null);
    }

    /**
     * Set the table which the query is targeting.
     * @param table String
     * @param as String
     * @return QueryBuilder
     */
    public QueryBuilder from(String table, String as) {
        from = as == null ? table : table + " as " + as;
        return this;
    }

    public QueryBuilder where(String column, Object value) {
        return where(column, "=", value);
    }

    public QueryBuilder where(String column, String operator, Object value) {
        checkOperator(operator);
        checkOperatorAndValue(operator, value);

        wheres.add(new WhereBitwise(column, operator, value));

        addBinding(value, "where");

        return this;
    }

    public QueryBuilder where(QueryClosure callback) {
        return whereNested(callback, "and");
    }

    public QueryBuilder whereNested(QueryClosure callback, String operator) {
        QueryBuilder query = forNestedWhere();
        callback.run(query);

        return addNestedWhereQuery(query, operator);
    }

    public QueryBuilder forNestedWhere() {
        return newQuery().from(from);
    }

    public QueryBuilder addNestedWhereQuery(QueryBuilder query, String operator) {
        if (!query.wheres.isEmpty()) {
            wheres.add(new WhereClosure(query, operator));

            addBinding(query.getBinding("where"), "where");
        }

        return this;
    }

    public QueryBuilder orWhere(QueryClosure callback) {
        return whereNested(callback, "or");
    }

    public QueryBuilder newQuery() {
        return new QueryBuilder(connection);
    }

    public Map<String, List<Object>> getBindings() {
        return bindings;
    }



    public List<Object> getBinding(String key) {
        return bindings.get(key);
    }

    public QueryBuilder addBinding(Object value, String type) {
        if (!bindings.containsKey(type)) {
            throw new IllegalArgumentException("Invalid binding type: " + type);
        }

        if (value != null && value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(value, i);
                getBinding(type).add(element);
            }
        } else {
            getBinding(type).add(value);
        }

        return this;
    }

    public ResultSet get() {
        return get(new String[]{"*"});
    }

    public ResultSet get(String[] columns) {
        return oneWithColumns(Arrays.asList(columns), this::runSelect);
    }

    public int insert(Map<String, Object> values) {
        if (values.isEmpty()) {
            return 0;
        }

        return connection.insert(
                grammar.compileInsert(this, values),
                flattenMap(values)
        );
    }

    public int update(Map<String, Object> values) {
        if (values.isEmpty()) {
            return 0;
        }

        String sql = grammar.compileUpdate(this, values);

        System.out.println(sql);

        return connection.update(sql,
                grammar.prepareBindingsForUpdate(bindings, values)
        );
    }


    public int delete() {
        return connection.delete(
                grammar.compileDelete(this),
                grammar.prepareBindingsForDelete(bindings)
        );
    }

    public int delete(long id) {
        where(from + ".id", id);
        return delete();
    }

    protected ResultSet runSelect() {
        return connection.select(
                toSql(),
                getFlattenedBindings()
        );
    }

    /**
     * Get the SQL representation of the query.
     * @return String
     */
    public String toSql() {
        return grammar.compileSelect(this);
    }

    protected <T> T oneWithColumns(List<String> columns, Supplier<T> callback) {
        List<String> original = this.columns;

        if (original == null || original.isEmpty()) {
            this.columns = columns;
        }

        T result = callback.get();

        this.columns = original;

        return result;
    }

    protected void checkOperator(String operator) {
        if (operatorIsInvalid(operator)) {
            throw new IllegalArgumentException("Illegal operator " + operator);
        }
    }

    protected boolean operatorIsInvalid(String operator) {
        return !Arrays.asList(operators).contains(operator);
    }

    protected void checkOperatorAndValue(String operator, Object value) {
        if (invalidOperatorAndValue(operator, value)) {
            throw new IllegalArgumentException("Illegal operator and value combination.");
        }
    }

    protected boolean invalidOperatorAndValue(String operator, Object value) {
        return value == null && Arrays.asList(operators).contains(operator) && !Arrays.asList(new String[]{"=", "<>", "!="}).contains(operator);
    }

    protected List<Object> getFlattenedBindings() {
        return bindings.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<Object> flattenMap(Map<String, Object> bindings) {
        List<Object> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : bindings.entrySet()) {
            result.add(entry.getValue());
        }
        return result;
    }
}
