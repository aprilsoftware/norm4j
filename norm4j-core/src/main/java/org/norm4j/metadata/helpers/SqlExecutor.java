package org.norm4j.metadata.helpers;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlExecutor {

    void execute(Connection connection, String sql) throws SQLException;
}
