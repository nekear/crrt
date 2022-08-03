package com.github.DiachenkoMD.dto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ResourceBundle;

public class JSi18n {

    private final transient ResourceBundle rs;
    public HashMap<String, String> notiflix;
    public HashMap<String, InputLocalization> inputs;

    public JSi18n(ResourceBundle rs){
        this.rs = rs;
    }

    public static class InputLocalization{
        public String placeholder;
        public LinkedList<String> checks;
    }

    public void addNotiflix(String key, String value){
        if(notiflix == null)
            notiflix = new HashMap<>();

        notiflix.put(key, rs.getString(value));
    }

    public void addInputLocalization(String input_name, String placeholder, LinkedList<String> checks){
        InputLocalization il = new InputLocalization();
        il.placeholder = rs.getString(placeholder);

        if(checks != null && !checks.isEmpty()){
            il.checks = new LinkedList<>();
            for(String checkTranslation : checks){
                il.checks.add(rs.getString(checkTranslation));
            }
        }

        if(inputs == null)
            inputs = new HashMap<>();

        inputs.put(input_name, il);
    }

    public String toJSON(){
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

}
