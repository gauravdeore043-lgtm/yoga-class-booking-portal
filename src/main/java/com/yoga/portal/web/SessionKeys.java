package com.yoga.portal.web;

import jakarta.servlet.http.HttpSession;

/** The signed-in member is kept in the HTTP session by id. */
public final class SessionKeys {

    public static final String MEMBER_ID = "memberId";

    private SessionKeys() {
    }

    public static Long memberId(HttpSession session) {
        Object value = session.getAttribute(MEMBER_ID);
        return value instanceof Long id ? id : null;
    }
}
