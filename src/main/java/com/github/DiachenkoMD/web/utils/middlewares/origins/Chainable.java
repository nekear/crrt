package com.github.DiachenkoMD.web.utils.middlewares.origins;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public abstract sealed class Chainable permits Guard, Ward {
    private final static Logger logger = LogManager.getLogger(Chainable.class);

    protected Chainable next;

    public static <T extends Chainable> T pipe(T first, T... pipeline){
        Chainable head = first;

        for(Chainable chainItem : pipeline){
            head.next = chainItem;
            head = chainItem;
        }

        return first;
    }

    public static <T extends Chainable> T pipe(List<T> pipeline){
        Chainable head = pipeline.get(0);

        if(pipeline.size() > 1){
            for(int i = 1; i < pipeline.size(); i++){
                Chainable chainItem = pipeline.get(i);

                head.next = chainItem;
                head = chainItem;
            }
        }


        return pipeline.get(0);
    }

    public abstract boolean process(HttpServletRequest req, HttpServletResponse resp) throws IOException;

    protected boolean devolve(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(next == null)
            return true;


        return next.process(req, resp);
    }
}
