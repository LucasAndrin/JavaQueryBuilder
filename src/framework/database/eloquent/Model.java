package framework.database.eloquent;

import java.sql.Connection;

public abstract class Model {

    protected Connection connection;
    protected String table;
    protected String primaryKey = "id";
    protected boolean exists = false;

}
