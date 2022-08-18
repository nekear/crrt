package com.github.DiachenkoMD.web.filters;

import com.github.DiachenkoMD.web.utils.guardian.Guardian;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static com.github.DiachenkoMD.web.utils.Utils.getCookieFromArray;

public class GeneralFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(GeneralFilter.class);

    private Guardian guardian;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        this.guardian = (Guardian) filterConfig.getServletContext().getAttribute("guardian");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        System.out.println("---->  NEW REQUEST  <----   ");

        HttpServletRequest httpRequest = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) res;
        httpResponse.addHeader("Access-Control-Allow-Origin", "*");
        httpResponse.addHeader("Access-Control-Allow-Headers", "*");
        req.setCharacterEncoding("UTF-8");

        if(!guardian.guard(httpRequest.getServletPath(), httpRequest, httpResponse))
            return;

        resolveDefaultLang(httpRequest, httpResponse);

        Optional<String> locale = Optional.ofNullable(req.getParameter("lang"));

        if(locale.isPresent()){
            logger.debug("Setting language to {}", locale.get());
            httpRequest.getSession().setAttribute("lang", locale.get());
            httpResponse.sendRedirect(httpRequest.getRequestURI());
        }else{
            chain.doFilter(httpRequest, httpResponse);
        }
    }

    /**
     * Sets default language (depending on client`s country) if no language selected manually.
     * @param req
     * @param resp
     */
    private void resolveDefaultLang(HttpServletRequest req, HttpServletResponse resp){
        if(req.getSession().getAttribute("lang") == null){
            String country = req.getLocale().getCountry();

            String defaultLangForCountry;
            if(country.equalsIgnoreCase("UA") || country.equalsIgnoreCase("RU")){
                defaultLangForCountry = "en"; // TODO: change to ua
            }else{
                defaultLangForCountry = "en";
            }

            req.getSession().setAttribute("lang", defaultLangForCountry);
        }
    }

}
