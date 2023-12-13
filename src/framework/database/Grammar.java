package framework.database;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Grammar {
    public String columnize(List<String> columns) {
        return String.join(", ", columns);
    }

    public String toUpperCaseFirstChar(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public String paremeterize(List<Object> values) {
        return values.stream()
                .map(this::parameter)
                .collect(Collectors.joining(", "));
    }

    public String parameter(Object value) {
        // Add Exceptions for Expressions
        return "?";
    }

    public List<Object> flattenMap(Map<String, Object> bindings) {
        List<Object> result = new ArrayList<>();
        for (Map.Entry<String, Object> entry : bindings.entrySet()) {
            result.add(entry.getValue());
        }
        return result;
    }

}
