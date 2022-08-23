package com.github.DiachenkoMD.web.listeners;

import com.github.DiachenkoMD.entities.adapters.*;
import com.github.DiachenkoMD.entities.enums.DBCoupled;
import com.github.DiachenkoMD.web.controllers.*;
import com.github.DiachenkoMD.web.daos.prototypes.CarsDAO;
import com.github.DiachenkoMD.web.daos.prototypes.InvoicesDAO;
import com.github.DiachenkoMD.web.services.*;
import com.github.DiachenkoMD.web.daos.DBTypes;
import com.github.DiachenkoMD.web.daos.factories.DAOFactory;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import com.github.DiachenkoMD.web.utils.RightsManager;
import com.github.DiachenkoMD.web.utils.guardian.Guardian;
import com.github.DiachenkoMD.web.utils.guardian.UseGuards;
import com.github.DiachenkoMD.web.utils.guardian.guards.AuthGuard;
import com.github.DiachenkoMD.web.utils.guardian.guards.Guard;
import com.github.DiachenkoMD.web.utils.guardian.guards.PageGuard;
import com.github.DiachenkoMD.web.utils.guardian.guards.roles.ManagerRGuard;
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
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.Set;

import static org.reflections.scanners.Scanners.SubTypes;
import static org.reflections.scanners.Scanners.TypesAnnotated;

@WebListener
public class ContextListener implements ServletContextListener {
    private static final Logger logger = LogManager.getLogger(ContextListener.class);
    @Override
    public void contextInitialized(ServletContextEvent sce){
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

        try{
            initGuardian(ctx);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void initServices(ServletContext ctx){
        DAOFactory daoFactory = (DAOFactory) ctx.getAttribute("dao_factory");

        UsersDAO usersDAO = daoFactory.getUsersDAO();
        CarsDAO carsDAO = daoFactory.getCarsDAO();
        InvoicesDAO invoicesDAO = daoFactory.getInvoicesDAO();

        // Not service, but it is better to init it here just not to create another UsersDAO instance
        ctx.setAttribute("rights_manager", new RightsManager(usersDAO));
        logger.info("[✓] RightsManager -> initialized");


        // Services initialization

        UsersService usersService = new UsersService(usersDAO);
        ctx.setAttribute("users_service", usersService);
        logger.info("[✓] UsersService -> initialized");

        AdminService adminService = new AdminService(usersDAO, carsDAO, invoicesDAO, ctx);
        ctx.setAttribute("admin_service", adminService);
        logger.info("[✓] AdminService -> initialized");

        ManagerService managerService = new ManagerService(usersDAO, invoicesDAO, ctx);
        ctx.setAttribute("manager_service", managerService);
        logger.info("[✓] ManagerService -> initialized");

        ClientService clientService = new ClientService(usersDAO, invoicesDAO, ctx);
        ctx.setAttribute("client_service", clientService);
        logger.info("[✓] ClientService -> initialized");

        IntroService introService = new IntroService(carsDAO, usersDAO, invoicesDAO, ctx);
        ctx.setAttribute("intro_service", introService);
        logger.info("[✓] IntroService -> initialized");

        DriverService driverService = new DriverService(usersDAO, invoicesDAO, ctx);
        ctx.setAttribute("driver_service", driverService);
        logger.info("[✓] DriverService -> initialized");
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
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeHierarchyAdapter(DBCoupled.class, new DBCoupledAdapter())
                .create();

        ctx.setAttribute("gson", gson);

        logger.info("[✓] Gson -> initialized");
    }

    private static void initGuardian(ServletContext ctx) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections("com.github.DiachenkoMD.web.controllers");
        Set<Class<?>> guardedRoutes = reflections.get(SubTypes.of(TypesAnnotated.with(UseGuards.class)).asClass());

        Guardian guardian = new Guardian();
        guardian.init(guardedRoutes);

        ctx.setAttribute("guardian", guardian);

        logger.info("[✓] Guardian -> initialized");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContextListener.super.contextDestroyed(sce);
    }
}
