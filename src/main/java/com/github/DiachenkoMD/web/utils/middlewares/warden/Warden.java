package com.github.DiachenkoMD.web.utils.middlewares.warden;

import com.github.DiachenkoMD.web.utils.middlewares.Middleware;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.Guardian;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.middlewares.origins.Guard;
import com.github.DiachenkoMD.web.utils.middlewares.origins.Ward;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * Processor realization for {@link Ward} classes. Depends on {@link UseWards @UseWards}. For more information on how middlewares work, checkout @see reference.
 * @see com.github.DiachenkoMD.web.utils.middlewares.Middleware
 * @see Middleware#init(Set)
 * @see Ward
 */
public class Warden implements Middleware {
    private final static Logger logger = LogManager.getLogger(Guardian.class);

    private final HashMap<String, Ward> wards = new HashMap<>();

    @Override
    public void init(Set<Class<?>> classes) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        for(Class<?> warded : classes){
            UseWards wardAnno = warded.getAnnotation(UseWards.class);

            Class<? extends Ward>[] connectedWards = wardAnno.value();

            LinkedList<Ward> wardsList = new LinkedList<>();

            for(Class<? extends Ward> wardClass : connectedWards){
                wardsList.add(wardClass.getConstructor().newInstance());
            }

            WebServlet webServletAnno = warded.getAnnotation(WebServlet.class);

            String path = webServletAnno.value()[0];

            Ward pipelinedWard = Ward.pipe(wardsList);

            wards.put(path, pipelinedWard);
        }

        logger.info("Warden will process incoming data on next routes: {}", wards.keySet());
    }

    @Override
    public boolean process(String path, HttpServletRequest req, HttpServletResponse resp){
        try {
            Ward relatedWard = wards.get(path);

            if(relatedWard == null)
                return true;

            return relatedWard.process(req, resp);
        }catch (Exception e){
            logger.error(e);
            return false;
        }
    }
}
