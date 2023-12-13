package framework.database.query.where;

public class WhereBitwise extends WhereClause {
    public String column;
    public Object value;
    public WhereBitwise(String column, String operator, Object value) {
        super("Bitwise", operator);
        this.column = column;
        this.value = value;
    }
}
