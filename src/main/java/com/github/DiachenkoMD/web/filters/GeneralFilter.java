package com.github.DiachenkoMD.web.filters;

import com.github.DiachenkoMD.entities.enums.VisualThemes;
import com.github.DiachenkoMD.web.utils.RightsManager;
import com.github.DiachenkoMD.web.utils.Utils;
import com.github.DiachenkoMD.web.utils.middlewares.Middleware;
import com.github.DiachenkoMD.web.utils.middlewares.guardian.Guardian;
import jakarta.servlet.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Optional;

import static com.github.DiachenkoMD.web.utils.Utils.createCookie;

public class GeneralFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(GeneralFilter.class);


    private Middleware guardian;
    private Middleware warden;
    private RightsManager rightsManager;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        this.guardian = (Middleware) filterConfig.getServletContext().getAttribute("guardian");
        this.warden = (Middleware) filterConfig.getServletContext().getAttribute("warden");
        this.rightsManager = (RightsManager) filterConfig.getServletContext().getAttribute("rights_manager");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        System.out.println("---->  NEW REQUEST  <----   ");

        HttpServletRequest httpRequest = (HttpServletRequest) req;
        HttpServletResponse httpResponse = (HttpServletResponse) res;
        httpResponse.addHeader("Access-Control-Allow-Origin", "*");
        httpResponse.addHeader("Access-Control-Allow-Headers", "*");
        req.setCharacterEncoding("UTF-8");

        rightsManager.manage(httpRequest);

        if(!guardian.process(httpRequest.getServletPath(), httpRequest, httpResponse))
            return;

        warden.process(httpRequest.getServletPath(), httpRequest, httpResponse);

        resolveDefaultLang(httpRequest);

        resolveDefaultTheme(httpRequest, httpResponse);

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
     */
    private void resolveDefaultLang(HttpServletRequest req){
        if(req.getSession().getAttribute("lang") == null){
            String country = req.getLocale().getCountry();

            String defaultLangForCountry;
            if(country.equalsIgnoreCase("UA") || country.equalsIgnoreCase("RU")){
                defaultLangForCountry = req.getServletContext().getInitParameter("ukLocale"); // TODO: change to ua
            }else{
                defaultLangForCountry = req.getServletContext().getInitParameter("enLocale");
            }

            req.getSession().setAttribute("lang", defaultLangForCountry);
        }
    }

    private void resolveDefaultTheme(HttpServletRequest req, HttpServletResponse resp){
        Cookie themeCookie = Utils.getCookieFromArray("theme", req.getCookies()).orElse(null);

        try {
            if(themeCookie == null)
                throw new IllegalArgumentException();

            VisualThemes.valueOf(themeCookie.getValue());
        }catch (IllegalArgumentException e){
            String path = req.getContextPath();
            resp.addCookie(createCookie("theme", VisualThemes.valueOf(req.getServletContext().getInitParameter("default_theme")).toString(), path));
        }
    }

}
