//created for extra form field role type i.e admin, doctor , patient
package com.smartclinicsystem.demo.security;

import org.springframework.security.web.authentication.WebAuthenticationDetails;

import jakarta.servlet.http.HttpServletRequest;

public class LoginAuthenticationDetails extends WebAuthenticationDetails {

    private final String userType;

    public LoginAuthenticationDetails(HttpServletRequest request) {
        super(request);
        this.userType = request.getParameter("userType");
    }

    public String getUserType() {
        return userType;
    }
}
/*What is HttpServletRequest?

HttpServletRequest is a Java Servlet object that represents the HTTP request sent by the browser to the server. */
