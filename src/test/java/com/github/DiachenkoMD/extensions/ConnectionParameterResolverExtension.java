package com.github.DiachenkoMD.extensions;

import com.github.DiachenkoMD.utils.H2_TDatasourceManager;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import javax.sql.DataSource;

/**
 * Connection parameter resolver is created to allow fancy insert of DataSources to inits in tests. <br/>
 * Almost all tests depend on H2 DataSource, so this connection parameter resolver provides injections for init() methods.
 */
public class ConnectionParameterResolverExtension implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType().equals(DataSource.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return H2_TDatasourceManager.getDataSource();
    }
}
