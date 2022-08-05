package com.github.DiachenkoMD.extensions;

import com.github.DiachenkoMD.utils.TDatabaseManager;
import com.github.DiachenkoMD.utils.TDatasourceManager;
import org.junit.jupiter.api.extension.*;

public class DatabaseOperationsExtension implements BeforeAllCallback, BeforeEachCallback {

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        TDatabaseManager.init(TDatasourceManager.getDataSource());
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        TDatabaseManager.destroy();
        TDatabaseManager.setup();
    }

}
