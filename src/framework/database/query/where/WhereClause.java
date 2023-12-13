package framework.database.query.where;

public class WhereClause {
    public String type;
    public String whereOperator = "and";
    public String operator;
    public WhereClause(String type, String operator) {
        this.type = type;
        this.operator = operator;
    }
    public WhereClause(String type, String whereOperator, String operator) {
        this.whereOperator = whereOperator;
        this.operator = operator;
    }
}
