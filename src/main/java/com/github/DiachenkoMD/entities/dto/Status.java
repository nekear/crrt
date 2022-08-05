package com.github.DiachenkoMD.entities.dto;

import com.github.DiachenkoMD.web.services.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.*;

/**
 * Status class acts more like data transfer object and allows developer to combine statuses with in-text replacement and localization. <br/>
 * For example, used while validating data at {@link UsersService#registerUser(HttpServletRequest, HttpServletResponse) UserService.registerUser()}.
 */
public class Status {

    private final LinkedList<StatusText> data = new LinkedList<>();

    public Status(String text){
        this(text, false);
    }
    public Status(String text, boolean isTranslation){
        data.add(new StatusText(text, isTranslation, StatusStates.SUCCESS));
    }

    public Status(String text, boolean isTranslation, HashMap<String, String> substitutes, StatusStates state){
        data.add(new StatusText(text, isTranslation, substitutes, state));
    }

    public Status(String text, boolean isTranslation, StatusStates state) {
        data.add(new StatusText(text, isTranslation, state));
    }

    public boolean add(String text, boolean isTranslation, HashMap<String, String> substitutes, StatusStates state){
        return data.add(new StatusText(text, isTranslation, substitutes, state));
    }
    // related methods

    /**
     * Returns String representation of Status gotten by specified index and can translate to specified language if needed
     * @param index - index of status in array (starts from 0)
     * @param lang - language system has translate into
     * @return String representation of Status entity
     */
    public String get(int index, String lang){
        if(index < data.size()){
            StatusText st = data.get(index);

            return st.convert(lang);
        }else{
            return null;
        }
    }

    /**
     * Allows to get a state of specified Status
     * @param index - index of status in array (starts from 0)
     * @return - Status state
     */
    public StatusStates getState(int index){
        if(index < data.size()){
            StatusText st = data.get(index);
            return st.getState();
        }else{
            return null;
        }
    }

    public StatusText getClean(int index){
        if(index < data.size()){
            return data.get(index);
        }else{
            return null;
        }
    }

    /**
     * Allows to get text representations of all statuses specified in the current object
     * @param lang - language to translate into
     * @return - list of text representations of statuses
     */
    public LinkedList<String> getList(String lang){
        LinkedList<String> res = new LinkedList<>();
        for(StatusText st : data)
            res.add(st.convert(lang));

        return res;
    }
}
