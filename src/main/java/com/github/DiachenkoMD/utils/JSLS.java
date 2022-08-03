package com.github.DiachenkoMD.utils;

import com.github.DiachenkoMD.dto.JSi18n;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Stands for JavaScript Localizations Storage. Contains js objects with transitions (in json). <br>
 * The motivation for creating these objects lies in the active use of Vue.js for the front-end.
 */
public class JSLS {
    public static String forLoginPage(String lang){
        ResourceBundle langPack = bundle(lang);

        JSi18n jSi18n = new JSi18n(langPack);
        jSi18n.addNotiflix("login_success", "page.login.notiflix.login_success");
        jSi18n.addInputLocalization("email", "pages.sign.input.email.placeholder", new LinkedList<>(List.of("pages.sign.input.email.checks.0")));
        jSi18n.addInputLocalization("password", "pages.sign.input.password.placeholder", null);

        return jSi18n.toJSON();
    }

    public static String forRegisterPage(String lang){

        ResourceBundle langPack = bundle(lang);

        JSi18n jSi18n = new JSi18n(langPack);
        jSi18n.addInputLocalization("email", "pages.sign.input.email.placeholder", new LinkedList<>(List.of("pages.sign.input.email.checks.0")));
        jSi18n.addInputLocalization("firstname", "pages.sign.input.firstname.placeholder", new LinkedList<>(List.of("pages.sign.input.firstname.checks.0")));
        jSi18n.addInputLocalization("surname", "pages.sign.input.surname.placeholder", new LinkedList<>(List.of("pages.sign.input.surname.checks.0")));
        jSi18n.addInputLocalization("patronymic", "pages.sign.input.patronymic.placeholder", new LinkedList<>(List.of("pages.sign.input.patronymic.checks.0")));
        jSi18n.addInputLocalization("password", "pages.sign.input.password.placeholder", new LinkedList<>(List.of("pages.sign.input.password.checks.0", "pages.sign.input.password.checks.1")));

        return jSi18n.toJSON();
    }

    private static ResourceBundle bundle(String lang){
        return ResourceBundle.getBundle( "langs.i18n_"+lang);
    }
}
