package com.github.DiachenkoMD.extensions;

import com.github.DiachenkoMD.utils.TConnectionManager;
import com.github.DiachenkoMD.utils.TDatabaseManager;
import org.junit.jupiter.api.extension.*;

import java.sql.Connection;
import java.sql.Savepoint;

public class DatabaseOperationsExtension implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private Connection connection;
    private Savepoint savepoint;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        Class<?> testClass = extensionContext.getRequiredTestClass();
        if (testClass.getEnclosingClass() == null) {
            connection = TConnectionManager.openConnection();

            TDatabaseManager.destroy(connection);
            TDatabaseManager.init(connection);
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        connection.setAutoCommit(false);
        savepoint = connection.setSavepoint("extensionSP");
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        Class<?> testClass = extensionContext.getRequiredTestClass();
        if (testClass.getEnclosingClass() == null) {
            TConnectionManager.closeConnection();
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        connection.rollback(savepoint);
    }
}
