package com.github.DiachenkoMD.web.utils.middlewares.guardian.guards;

import com.github.DiachenkoMD.web.utils.middlewares.origins.Guard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.GuardingTypes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class PageGuard extends Guard {
    private static final Logger logger = LogManager.getLogger(PageGuard.class);

    @Override
    public boolean process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setAttribute("guardingType", GuardingTypes.PAGE);

        return devolve(req, resp);
    }
}
