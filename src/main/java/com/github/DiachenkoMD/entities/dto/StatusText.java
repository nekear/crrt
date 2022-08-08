package com.github.DiachenkoMD.entities.dto;

import com.github.DiachenkoMD.entities.enums.StatusStates;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Stores status` text, state and isTranslation (say whether the {@link #convert(String) convert(String)} method should replace translation keyword or not).
 */
public class StatusText{
    private StatusStates state = StatusStates.SUCCESS;
    private String text;

    /**
     * "String text" variable might contain some developer specified text or just a keyword from translation properties file. This variable tells the {@link #convert(String) convert(String)} method:
     * "should it replace this text with a translation or not".
     */
    private boolean isTranslation;

    /**
     * Substitutes used to substitute some data into the "String text" variable. <br/>
     * That`s why we can embed some things like "email" into translation files.
     */
    private final HashMap<String, String> substitutes = new HashMap<>();

    public StatusText(String text){
        this.text = text;
        this.isTranslation = true;
        this.state = StatusStates.ERROR;
    }

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

    public StatusStates getState() {
        return state;
    }

    public void setState(StatusStates state) {
        this.state = state;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isTranslation() {
        return isTranslation;
    }

    public void setTranslation(boolean translation) {
        isTranslation = translation;
    }

    public HashMap<String, String> getSubstitutes() {
        return substitutes;
    }
}
