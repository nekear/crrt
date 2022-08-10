package com.github.DiachenkoMD.web.utils;

import com.github.DiachenkoMD.entities.dto.JSi18n;
import com.github.DiachenkoMD.entities.enums.AccountStates;
import com.github.DiachenkoMD.entities.enums.CarSegments;
import com.github.DiachenkoMD.entities.enums.Cities;
import com.github.DiachenkoMD.entities.enums.Roles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;

/**
 * Stands for JavaScript JSON Storage. Contains js objects in json format. <br>
 * The motivation for creating these objects lies in the active use of Vue.js for the front-end.
 */
public class JSJS {
    public static String transForLoginPage(String lang){
        ResourceBundle langPack = bundle(lang);

        JSi18n jSi18n = new JSi18n(langPack);
        jSi18n.addNotiflix("login_success", "page.login.notiflix.login_success");
        jSi18n.addInputLocalization("email", "pages.sign.input.email.placeholder", new LinkedList<>(List.of("pages.sign.input.email.checks.0")));
        jSi18n.addInputLocalization("password", "pages.sign.input.password.placeholder", null);

        return jSi18n.toJSON();
    }

    public static String transForRegisterPage(String lang){

        ResourceBundle langPack = bundle(lang);

        JSi18n jSi18n = new JSi18n(langPack);
        jSi18n.addInputLocalization("email", "pages.sign.input.email.placeholder", new LinkedList<>(List.of("pages.sign.input.email.checks.0")));
        jSi18n.addInputLocalization("firstname", "pages.sign.input.firstname.placeholder", new LinkedList<>(List.of("pages.sign.input.firstname.checks.0")));
        jSi18n.addInputLocalization("surname", "pages.sign.input.surname.placeholder", new LinkedList<>(List.of("pages.sign.input.surname.checks.0")));
        jSi18n.addInputLocalization("patronymic", "pages.sign.input.patronymic.placeholder", new LinkedList<>(List.of("pages.sign.input.patronymic.checks.0")));
        jSi18n.addInputLocalization("password", "pages.sign.input.password.placeholder", new LinkedList<>(List.of("pages.sign.input.password.checks.0", "pages.sign.input.password.checks.1")));

        return jSi18n.toJSON();
    }

    public static String CitiesList(String lang){
        ResourceBundle langPack = bundle(lang);

        HashMap<Integer, HashMap<String, String>> res = new HashMap<>();

        res.put(0, new HashMap<>(Map.of("name", langPack.getString("cities.all"))));

        for(Cities city : Cities.values()){
            res.put(city.id(), new HashMap<>(Map.of("name", langPack.getString("cities."+city.keyword()))));
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(res);
    }

    public static String SegmentsList(String lang){
        ResourceBundle langPack = bundle(lang);

        HashMap<Integer, HashMap<String, String>> res = new HashMap<>();

        res.put(0, new HashMap<>(Map.of("name", langPack.getString("segments.all"))));

        for(CarSegments segment : CarSegments.values()){
            res.put(segment.id(), new HashMap<>(Map.of("name", langPack.getString("segments."+segment.keyword()))));
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(res);
    }

    public static String RolesList(String lang){
        ResourceBundle langPack = bundle(lang);

        HashMap<Integer, HashMap<String, String>> res = new HashMap<>();

        for(Roles role : Roles.values()){
            res.put(role.id(), new HashMap<>(Map.of("name", langPack.getString("roles."+role.keyword()))));
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(res);
    }

    public static String AccountStatesList(String lang){
        ResourceBundle langPack = bundle(lang);

        HashMap<Integer, HashMap<String, String>> res = new HashMap<>();

        for(AccountStates state : AccountStates.values()){
            res.put(state.id(), new HashMap<>(Map.of("name", langPack.getString("ac_state."+state.keyword()))));
        }

        return new GsonBuilder().setPrettyPrinting().create().toJson(res);
    }

    private static ResourceBundle bundle(String lang){
        return ResourceBundle.getBundle( "langs.i18n_"+lang);
    }
}
