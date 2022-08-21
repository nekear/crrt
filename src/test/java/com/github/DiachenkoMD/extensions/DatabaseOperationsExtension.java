package com.github.DiachenkoMD.extensions;

import com.github.DiachenkoMD.utils.TDBType;
import com.github.DiachenkoMD.utils.TDatabaseManager;
import com.github.DiachenkoMD.utils.H2_TDatasourceManager;
import org.junit.jupiter.api.extension.*;

public class DatabaseOperationsExtension implements BeforeAllCallback, BeforeEachCallback {
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        TDatabaseManager.init(H2_TDatasourceManager.getDataSource(), TDBType.H2);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        TDatabaseManager.destroy();
        TDatabaseManager.setup();
    }

}
