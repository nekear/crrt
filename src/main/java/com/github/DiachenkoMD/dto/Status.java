package com.github.DiachenkoMD.dto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.*;

/**
 * Status class acts more like data transfer object and allows developer to combine statuses with in-text replacement and localization. <br/>
 * For example, used while validating data at {@link com.github.DiachenkoMD.sevices.UsersService#registerUser(HttpServletRequest, HttpServletResponse) UserService.registerUser()}.
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

    /**
     * Stores status` text, state and isTranslation (say whether the {@link #convert(String) convert(String)} method should replace translation keyword or not).
     */
    private class StatusText{
        public StatusStates state = StatusStates.SUCCESS;
        public String text;

        /**
         * "String text" variable might contain some developer specified text or just a keyword from translation properties file. This variable tells the {@link #convert(String) convert(String)} method:
         * "should it replace this text with a translation or not".
         */
        public boolean isTranslation;

        /**
         * Substitutes used to substitute some data into the "String text" variable. <br/>
         * That`s why we can embed some things like "email" into translation files.
         */
        private final HashMap<String, String> substitutes = new HashMap<>();

        public StatusText(String text, boolean isTranslation, HashMap<String, String> substitutes, StatusStates state){
            this.text = text;
            this.isTranslation = isTranslation;
            this.substitutes.putAll(substitutes);
            this.state = state;
        }

        public StatusText(String text, boolean isTranslation, StatusStates state){
            this.text = text;
            this.isTranslation = isTranslation;
            this.state = state;
        }

        /**
         * Converting current StatusText to String representation.
         * @param lang - accepts language (en, ua, etc.) that text will be translated into if needed
         * @return String representation of Status, including replaced in-text variables.
         */
        public String convert(String lang) {
            String preparedText;
            if(isTranslation){
                ResourceBundle translations = ResourceBundle.getBundle("langs.i18n_"+lang);

                preparedText = translations.getString(text);
            }else{
                preparedText = text;
            }

            for(Map.Entry<String, String> subs : substitutes.entrySet()){
                String key = subs.getKey();
                String value = subs.getValue();

                preparedText = preparedText.replaceAll("<"+key+">", value);
            }

            return preparedText;
        }
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
            return st.state;
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
