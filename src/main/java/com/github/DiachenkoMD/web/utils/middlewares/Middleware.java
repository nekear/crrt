package com.github.DiachenkoMD.web.utils.middlewares;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Set;

public interface Middleware {
    void init(Set<Class<?>> classes) throws Exception;

    boolean process(String path, HttpServletRequest req, HttpServletResponse resp);
}
