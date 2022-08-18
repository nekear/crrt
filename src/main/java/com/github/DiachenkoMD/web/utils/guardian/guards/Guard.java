package com.github.DiachenkoMD.web.utils.guardian.guards;

import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.guardian.GuardingTypes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;

public abstract class Guard {
    private final static Logger logger = LogManager.getLogger(Guard.class);

    private Guard next;

    public static Guard pipe(Guard first, Guard... pipeline){
        Guard head = first;

        for(Guard guard : pipeline){
            head.next = guard;
            head = guard;
        }

        return first;
    }

    public static Guard pipe(List<Guard> pipeline){
        Guard head = pipeline.get(0);

        if(pipeline.size() > 1){
            for(int i = 1; i < pipeline.size(); i++){
                Guard guard = pipeline.get(i);

                head.next = guard;
                head = guard;
            }
        }


        return pipeline.get(0);
    }

    public abstract boolean check(HttpServletRequest req, HttpServletResponse resp) throws IOException;

    protected boolean devolve(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if(next == null)
            return true;


        return next.check(req, resp);
    }

    public static GuardingTypes getGuardianType(HttpServletRequest req){
        Object guardianTypeObj = req.getAttribute("guardingType");

        if(guardianTypeObj == null)
            return GuardingTypes.API;

        return (GuardingTypes) guardianTypeObj;
    }

}
