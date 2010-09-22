package com.mirth.connect.server.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.mirth.connect.model.User;


public class UserSessionCache {
    private Logger logger = Logger.getLogger(this.getClass());
    private Map<HttpSession, User> userSessionMap = new ConcurrentHashMap<HttpSession, User>();
    private static UserSessionCache instance = null;
    
    private UserSessionCache() {

    }

    public static UserSessionCache getInstance() {
        synchronized (UserSessionCache.class) {
            if (instance == null) {
                instance = new UserSessionCache();
            }

            return instance;
        }
    }
    
    public void registerSessionForUser(HttpSession session, User user) {
        logger.debug("registering session: user=" + user.getId() + ", session=" + session.getId());
        userSessionMap.put(session, user);
    }

    public void invalidateAllSessionsForUser(User user) {
        for (Entry<HttpSession, User> entry : userSessionMap.entrySet()) {
            HttpSession entrySession = entry.getKey();
            User entryUser = entry.getValue();
            
            if (entryUser.getId().equals(user.getId())) {
                logger.debug("invalidating session: user=" + entryUser.getId() + ", session=" + entrySession.getId());
                entrySession.removeAttribute("authorized");
                userSessionMap.remove(entrySession);
            }
        }
    }
}
