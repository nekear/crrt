package com.github.DiachenkoMD.web.listeners;

import com.github.DiachenkoMD.entities.adapters.Skip;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.services.AdminService;
import com.github.DiachenkoMD.web.services.UsersService;
import com.github.DiachenkoMD.web.daos.DBTypes;
import com.github.DiachenkoMD.web.daos.factories.DAOFactory;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.utils.pinger.Pinger;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ResourceBundle;

@WebListener
public class ContextListener implements ServletContextListener {
    private static final Logger logger = LogManager.getLogger(ContextListener.class);
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        ResourceBundle appProps = ResourceBundle.getBundle("app");

        String database = appProps.getString("database");
        String lookup = appProps.getString(database+"_lookup");

        DAOFactory.init(DBTypes.valueOf(database.toUpperCase()), lookup);
        DAOFactory factory = DAOFactory.getFactory();

        ctx.setAttribute("dao_factory", factory);

        logger.info("[✓] DAOFactory -> initialized");

        initServices(ctx);

        initPinger(ctx);

        initGson(ctx);
    }

    private static void initServices(ServletContext ctx){
        DAOFactory daoFactory = (DAOFactory) ctx.getAttribute("dao_factory");

        UsersDAO usersDAO = daoFactory.getUsersDAO();

        UsersService usersService = new UsersService(usersDAO);
        ctx.setAttribute("users_service", usersService);
        logger.info("[✓] UsersService -> initialized");

        CarsDAO carsDAO = daoFactory.getCarsDAO();

        AdminService adminService = new AdminService(usersDAO, carsDAO);
        ctx.setAttribute("admin_service", adminService);
        logger.info("[✓] AdminService -> initialized");
    }

    private static void initPinger(ServletContext ctx){
        Pinger pinger = new Pinger();
        pinger.addListener(Exception.class, logger::error);
        pinger.addListener(String.class, logger::info);

        ctx.setAttribute("pinger", pinger);
        logger.info("[✓] Pinger -> initialized");
    }

    private static void initGson(ServletContext ctx){
        Gson gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategy()
                {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f)
                    {
                        return f.getAnnotation(Skip.class) != null;
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz)
                    {
                        return false;
                    }
                })
                .create();

        ctx.setAttribute("gson", gson);

        logger.info("[✓] Gson -> initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContextListener.super.contextDestroyed(sce);
    }
}
