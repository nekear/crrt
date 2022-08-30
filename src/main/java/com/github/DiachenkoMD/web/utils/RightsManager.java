package com.github.DiachenkoMD.web.utils;

import com.github.DiachenkoMD.entities.dto.users.AuthUser;
import com.github.DiachenkoMD.entities.exceptions.DBException;
import com.github.DiachenkoMD.web.daos.prototypes.UsersDAO;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static com.github.DiachenkoMD.entities.Constants.SESSION_AUTH;

/**
 * Utility for updating user`s rights, balance and other stuff.
 */
public class RightsManager {
    private static final Logger logger = LogManager.getLogger(RightsManager.class);
    /**
     * LinkedList, which is needed to store ids of users, whose data should be reloaded from db to their session object.
     */
    private static final Set<Integer> rightsToUpdateStorage = new HashSet<>();

    private final UsersDAO usersDAO;

    public RightsManager(UsersDAO usersDAO) {
        this.usersDAO = usersDAO;
    }

    /**
     * Method for managing user rights. Gets user id from session and reloads data from db. <br/>
     * Needed to update user`s state (BLOCKED / UNBLOCKED), balance, role, general info and other things, when changed at admin panel.
     * @param req
     */
    public void manage(HttpServletRequest req){
        try{
            if(req.getSession().getAttribute(SESSION_AUTH) != null){
                AuthUser user = (AuthUser) req.getSession().getAttribute(SESSION_AUTH);

                int userId = (Integer) user.getId();

                if(rightsToUpdateStorage.contains(userId)){
                    Optional<AuthUser> updatedUserInfo = Optional.ofNullable(usersDAO.get(userId)); // the place where it would be better to use Optional

                    logger.info("Updating user #[{}] data", updatedUserInfo.orElse(null));

                    req.getSession().setAttribute(SESSION_AUTH, updatedUserInfo.orElse(null));

                    this.remove(userId);

                    if(updatedUserInfo.isEmpty())
                        logger.warn("Could reload users data from db. Maybe it`s bug? Anyways, session was set to null and user was forced to re-login.");
                }
            }
        }catch (Exception e){
            logger.error(e);
        }
    }

    public void add(int userId){
        rightsToUpdateStorage.add(userId);
    }

    public void remove(int userId){
        rightsToUpdateStorage.removeIf(x -> x == userId);
    }
}
