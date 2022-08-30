package com.github.DiachenkoMD.web.utils.middlewares.warden.wards;

import com.github.DiachenkoMD.entities.dto.JWTAnalysis;
import com.github.DiachenkoMD.entities.enums.JWTErrors;
import com.github.DiachenkoMD.web.utils.JWTManager;
import com.github.DiachenkoMD.web.utils.middlewares.origins.Ward;
import io.fusionauth.jwt.InvalidJWTException;
import io.fusionauth.jwt.InvalidJWTSignatureException;
import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.domain.JWT;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Ward, that process JWT tokens from url parameters, decodes them and
 */
public class JWTWard extends Ward {
    private final static Logger logger = LogManager.getLogger(JWTWard.class);
    private String awaitJWTOn = "token";

    @Override
    public boolean process(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String jwtToken = req.getParameter(awaitJWTOn);

        req.setAttribute("jwtAnalysis", JWTAnalysis.of(jwtToken));

        return devolve(req, resp);
    }

    public void setAwaitJWTOn(String awaitJWTOn) {
        this.awaitJWTOn = awaitJWTOn;
    }
}