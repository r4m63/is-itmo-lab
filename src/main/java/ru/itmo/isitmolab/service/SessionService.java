package ru.itmo.isitmolab.service;

import jakarta.ejb.Stateless;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Stateless
public class SessionService {

    public static final String ATTR_USER_ID = "userId";

    public void startSession(HttpServletRequest req, Long userId, Integer ttlSeconds) {
        var s = req.getSession(true);
        s.setAttribute(ATTR_USER_ID, userId);
        if (ttlSeconds != null && ttlSeconds > 0) {
            s.setMaxInactiveInterval(ttlSeconds);
        }
        // JSESSIONID кука ставится контейнером автоматически
    }

    public boolean isActive(HttpServletRequest req) {
        var s = req.getSession(false);
        if (s == null) return false;
        var uid = s.getAttribute(ATTR_USER_ID);
        return (uid instanceof Number);
    }

    public Long getCurrentUserId(HttpServletRequest req) {
        var s = req.getSession(false);
        if (s == null) return null;
        var uid = s.getAttribute(ATTR_USER_ID);
        return (uid instanceof Number) ? ((Number) uid).longValue() : null;
    }

    public void destroySession(HttpServletRequest req, HttpServletResponse res) {
        var s = req.getSession(false);
        if (s != null) s.invalidate();

        Cookie c = new Cookie("JSESSIONID", "");
        c.setPath("/");
        c.setMaxAge(0);
        c.setHttpOnly(true);
        res.addCookie(c);
    }


}
