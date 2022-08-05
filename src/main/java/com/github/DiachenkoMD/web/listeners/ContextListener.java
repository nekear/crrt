package com.github.DiachenkoMD.web.listeners;

import com.github.DiachenkoMD.web.services.UsersService;
import com.github.DiachenkoMD.web.services.daos.DBTypes;
import com.github.DiachenkoMD.web.services.daos.factories.DAOFactory;
import com.github.DiachenkoMD.web.services.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.utils.pinger.Pinger;
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
    }

    private static void initServices(ServletContext ctx){
        DAOFactory daoFactory = (DAOFactory) ctx.getAttribute("dao_factory");

        UsersDAO usersDAO = daoFactory.getUsersDAO();

        UsersService usersService = new UsersService(usersDAO);
        ctx.setAttribute("users_service", usersService);
        logger.info("[✓] UsersService -> initialized");
    }

    private static void initPinger(ServletContext ctx){
        Pinger pinger = new Pinger();
        pinger.addListener(Exception.class, logger::error);
        pinger.addListener(String.class, logger::info);

        ctx.setAttribute("pinger", pinger);
        logger.info("[✓] Pinger -> initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContextListener.super.contextDestroyed(sce);
    }
}
