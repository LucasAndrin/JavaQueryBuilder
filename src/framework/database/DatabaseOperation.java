package framework.database;

public enum DatabaseOperation {
    SELECT("SELECT"),
    INSERT("INSERT"),
    UPDATE("UPDATE"),
    DELETE("DELETE");

    private final String operation;

    DatabaseOperation(String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return this.operation;
    }
}
