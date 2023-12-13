package framework.database.query.grammars;

import framework.database.query.QueryBuilder;
import framework.database.query.where.WhereBitwise;
import framework.database.query.where.WhereClosure;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Grammar extends framework.database.Grammar implements GrammarContract {

    protected String[] selectComponents = {
//        "aggregate",
        "columns",
        "from",
//        "joins",
        "wheres",
//        "groups",
//        "havings",
//        "orders",
//        "limit",
//        "offset",
//        "lock"
    };

    @Override
    public String compileSelect(QueryBuilder query) {
        List<String> original = query.columns;

        if (query.columns.isEmpty()) {
            query.columns = Arrays.asList(new String[]{"*"});
        }

        String sql;
        try {
            sql = concatenate(
                    compileComponents(query)
            ).trim();
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        query.columns = original;

        return sql;
    }

    @Override
    public String compileInsert(QueryBuilder query, Map<String, Object> values) {
        if (values.isEmpty()) {
            return concatenate(Arrays.asList("insert into",
                    query.from,
                    "default values"));
        }

        String columns = columnize(new ArrayList<>(values.keySet()));
        String parameters = "(" + paremeterize(flattenMap(values)) + ")";

        return concatenate(Arrays.asList(
                "insert into",
                query.from,
                "(" + columns + ")",
                "values",
                parameters
        ));
//                "insert into " + query.from + " (" + columns + ") values"
    }

    @Override
    public String compileUpdate(QueryBuilder query, Map<String, Object> values) {
        String columns = compileUpdateColumns(query, values);

        String where = compileWheres(query);

        return compileUpdateWithoutJoins(query, columns, where);
    }

    @Override
    public List<Object> prepareBindingsForUpdate(Map<String, List<Object>> bindings, Map<String, Object> values) {
        HashMap<String, List<Object>> cleanBindings = new HashMap<>(bindings);
        cleanBindings.remove("select");
        List<Object> joins = cleanBindings.remove("join");

        List<Object> result = new ArrayList<>(values.values());
        result.addAll(joins);
        result.addAll(getFlattenedValues(cleanBindings));

        return result;
    }

    @Override
    public String compileDelete(QueryBuilder query) {
        String where = compileWheres(query);

        return compileDeleteWithoutJoins(query, where);
    }

    @Override
    public List<Object> prepareBindingsForDelete(Map<String, List<Object>> bindings) {
        HashMap<String, List<Object>> cleanBindings = new HashMap<>(bindings);
        cleanBindings.remove("select");
        return getFlattenedValues(cleanBindings);
    }

    private String compileDeleteWithoutJoins(QueryBuilder query, String where) {
        return concatenate(Arrays.asList(
                "delete from",
                query.from,
                where
        ));
    }

    private List<Object> getFlattenedValues(Map<String, List<Object>> map) {
        List<Object> flattenedValues = new ArrayList<>();
        map.forEach((k, v) -> {
            if (valueIsNotEmpty(v)) {
                flattenedValues.addAll(v);
            }
        });
        return flattenedValues;
    }

    protected List<String> compileComponents(QueryBuilder query) throws NoSuchFieldException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<String> sql = new ArrayList<>();

        Class<? extends QueryBuilder> builderClass = query.getClass();

        for (String component : selectComponents) {
            Field field = builderClass.getDeclaredField(component);
            field.setAccessible(true);
            Class<?> fieldType = field.getType();
            Object fieldValue = field.get(query);

            if (valueIsNotEmpty(fieldValue)) {
                String methodName = "compile" + toUpperCaseFirstChar(component);
                Method method;
                String result;
                try {
                    method = getClass().getDeclaredMethod(methodName, QueryBuilder.class, fieldType);
                    result = (String) method.invoke(this, query, fieldValue);
                } catch (NoSuchMethodException e) {
                    method = getClass().getDeclaredMethod(methodName, QueryBuilder.class);
                    result = (String) method.invoke(this, query);
                }

                sql.add(result);
            }
        }

        return sql;
    }

    public String compileColumns(QueryBuilder query, List<String> columns) {
        String select;
        if (query.distinct) {
            select = "select distinct ";
        } else {
            select = "select ";
        }

        return select + columnize(columns);
    }

    public String compileFrom(QueryBuilder query, String table) {
        return "from " + table;
    }

    protected String compileUpdateColumns(QueryBuilder query, Map<String, Object> values)
    {
        return values.entrySet().stream()
                .map(entry -> entry.getKey() + " = " + parameter(entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    protected String compileUpdateWithoutJoins(QueryBuilder query, String columns, String where) {
        return concatenate(Arrays.asList(
                "update",
                query.from,
                "set",
                columns,
                where
        ));
    }

    public String compileWheres(QueryBuilder query) {
        if (!query.wheres.isEmpty()) {
            List<String> sql = compileWheresToArray(query);
            if (!sql.isEmpty()) {
                return concatenateWhereClauses(query, sql);
            }
        }

        return "";
    }

    protected List<String> compileWheresToArray(QueryBuilder query) {
        return query.wheres.stream()
                .map(where -> {
                    String methodName = "where" + where.type;
                    try {
                        Method method = getClass().getDeclaredMethod(methodName, QueryBuilder.class, where.getClass());
                        return where.whereOperator + " " + method.invoke(this, query, where);
                    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
    }

    protected String whereBitwise(QueryBuilder query, WhereBitwise where) {
        return where.column + " " + where.operator + " ?";
    }

    protected String whereClosure(QueryBuilder query, WhereClosure where) {
        return "(" + compileWheres(where.query).substring(6) + ")";
    }

    protected String concatenate(List<String> segments) {
        return String.join(" ", segments);
    }

    protected String concatenateWhereClauses(QueryBuilder query, List<String> sql) {
        return "where" + removeLeadingBoolean(concatenate(sql));
    }

    protected String removeLeadingBoolean(String value) {
        Pattern pattern = Pattern.compile("(?i)\\b(?:and|or)\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(value);

        return matcher.replaceFirst("");
    }

    protected boolean valueIsNotEmpty(Object value) {
        if (value instanceof Collection<?>) {
            return !((Collection<?>) value).isEmpty();
        }
        return value != null;
    }
}
