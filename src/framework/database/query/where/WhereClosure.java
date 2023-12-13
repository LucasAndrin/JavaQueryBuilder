package framework.database.query.where;

import framework.database.query.QueryBuilder;

public class WhereClosure extends WhereClause {
    public QueryBuilder query;
    public WhereClosure(QueryBuilder query, String operator) {
        super("Closure", operator);
        this.query = query;
    }
}
