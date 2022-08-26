package com.github.DiachenkoMD.tag_handlers;

import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;

import java.io.IOException;

public class LangTagHandler extends SimpleTagSupport {
    private String prefix;
    private boolean clean = false;

    public void setPrefix(String prefix){
        this.prefix = prefix;
    }

    public void setClean(boolean clean){
        this.clean = clean;
    }

    @Override
    public void doTag() throws IOException {
        PageContext ctx = (PageContext) getJspContext();
        String finalLangForm;

        if(prefix == null){
            if(ctx.getSession().getAttribute("lang") != null) {
                finalLangForm = (String) ctx.getSession().getAttribute("lang");
            }else{
                finalLangForm = ctx.getServletContext().getInitParameter("enLocale");
            }
        }else{
            finalLangForm = ctx.getServletContext().getInitParameter(prefix+"Locale");
        }

        if(!clean)
            finalLangForm = finalLangForm.replaceAll("_", "-");

        ctx.getOut().print(finalLangForm);
    }
}

