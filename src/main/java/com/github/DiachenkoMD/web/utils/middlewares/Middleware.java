package com.github.DiachenkoMD.web.utils.middlewares;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Set;

/**
 * Every middleware can be initialized with set of classes and can process incoming data depending on connected Middlewares to specific url. <br/>
 * > Examples of usage can be seen on many controllers in this project.
 * @implSpec The principle behind such Middleware is that it can be used to braid the necessary servlets.
 * Middleware will be executed before transmitting data further to the servlet and can, for example like {@link com.github.DiachenkoMD.web.utils.middlewares.guardian.Guardian Guardian},
 * pass or reject requests depending on specified by {@link com.github.DiachenkoMD.web.utils.middlewares.origins.Guard Guard} in {@link com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards @UseGuards}.
 * @implNote Motivation behind creating such utilities is that I really want to reduce boilerplate code and make it easily to extend functionality if needed.
 */
public interface Middleware {
    /**
     * Method for initializing specific middleware processor.
     * @param classes set of classes that contains, for example, {@link com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards @UseGuards} or {@link com.github.DiachenkoMD.web.utils.middlewares.warden.UseWards @UseWards}
     * annotations.
     * @implSpec method realization should get url path from @WebServlet annotation wire it with connected Guard or Ward (depending on processor).
     */
    void init(Set<Class<?>> classes) throws Exception;

    /**
     * Method for processing Guard or Ward wired with specified path (if end-point servlet contains {@link com.github.DiachenkoMD.web.utils.middlewares.origins.Guard Guard} or {@link com.github.DiachenkoMD.web.utils.middlewares.origins.Ward Ward} annotations.
     * @param path target URL
     * @param req
     * @param resp
     * @return <i>true</i> if everything was okay and <i>false</i>, if, for example, {@link com.github.DiachenkoMD.web.utils.middlewares.origins.Guard Guard} done some actions (for example, redirected).
     */
    boolean process(String path, HttpServletRequest req, HttpServletResponse resp);
}
