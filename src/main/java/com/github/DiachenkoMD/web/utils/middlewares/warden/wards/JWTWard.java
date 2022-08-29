package com.github.DiachenkoMD.web.utils.middlewares.warden.wards;

import com.github.DiachenkoMD.web.utils.JWTManager;
import com.github.DiachenkoMD.web.utils.middlewares.origins.Ward;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class JWTWard extends Ward {
    private final static Logger logger = LogManager.getLogger(JWTWard.class);
    private String awaitJWTOn = "token";

    @Override
    public boolean process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String jwtToken = req.getParameter(awaitJWTOn);

        if(jwtToken != null)
            req.setAttribute("jwtToken", JWTManager.decode(jwtToken));

        return devolve(req, resp);
    }

    public void setAwaitJWTOn(String awaitJWTOn) {
        this.awaitJWTOn = awaitJWTOn;
    }
}
