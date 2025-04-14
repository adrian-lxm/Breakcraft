package de.breakcraft.proxy.db;

import javax.sql.DataSource;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final DataSource dataSource;

    private DatabaseManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static void initialize(DataSource dataSource) {
        instance = new DatabaseManager(dataSource);
    }

    public static DatabaseManager get() {
        return instance;
    }
}
