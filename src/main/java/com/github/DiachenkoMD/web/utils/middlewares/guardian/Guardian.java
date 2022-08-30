package com.github.DiachenkoMD.web.utils.middlewares.guardian;

import com.github.DiachenkoMD.web.utils.middlewares.origins.Guard;
import com.github.DiachenkoMD.web.utils.middlewares.Middleware;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.guards.StateGuard;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Processor realization for {@link Guard} classes. Depends on {@link UseGuards @UseGuards}. For more information on how middlewares work, checkout @see reference.
 * @see com.github.DiachenkoMD.web.utils.middlewares.Middleware
 * @see Middleware#init(Set)
 * @see Guard
 */
public class Guardian implements Middleware {

    private final static Logger logger = LogManager.getLogger(Guardian.class);

    private final HashMap<String, Guard> guards = new HashMap<>();

    public void init(Class<?>... classes) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        if(classes == null || classes.length == 0)
            return;

        this.init(Set.of(classes));
    }

    @Override
    public void init(Set<Class<?>> classes) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<String> protectedRoutes = new LinkedList<>();

        for(Class<?> guarded : classes){
            UseGuards guardAnno = guarded.getAnnotation(UseGuards.class);

            Class<? extends Guard>[] connectedGuards = guardAnno.value();

            LinkedList<Guard> guardsList = new LinkedList<>();

            for(Class<? extends Guard> guardClass : connectedGuards){
                guardsList.add(guardClass.getConstructor().newInstance());

                // Default block protection on every route with AuthGuard
                if(guardClass == AuthGuard.class)
                    guardsList.add(new StateGuard());
            }

            WebServlet webServletAnno = guarded.getAnnotation(WebServlet.class);

            String path = webServletAnno.value()[0];

            Guard pipelinedGuard = Guard.pipe(guardsList);

            guards.put(path, pipelinedGuard);

            protectedRoutes.add(path);
        }

        logger.info("Guardian will protect: {}", protectedRoutes);
    }

    @Override
    public boolean process(String path, HttpServletRequest req, HttpServletResponse resp) {
        try {
            Guard relatedGuard = guards.get(path);

            if(relatedGuard != null)
                return relatedGuard.process(req, resp);

            return true;
        }catch (Exception e){
            logger.error(e);
            return false;
        }
    }
}
