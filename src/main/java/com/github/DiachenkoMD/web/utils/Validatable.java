package com.github.DiachenkoMD.web.utils;

import com.github.DiachenkoMD.entities.enums.ValidationParameters;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.regex.Pattern;

import static com.github.DiachenkoMD.web.utils.Utils.multieq;

/**
 * Utility for easily validating data. All available validation criteria could be viewed at {@link ValidationParameters}.<br/>
 * To validate something, you should firstly obtain {@link Validatable} object from {@link #of(Object, ValidationParameters)} method and then call {@link #validate()} on it.
 */
public class Validatable {
    private static HashMap<ValidationParameters, Pattern> patterns = new HashMap<>();

    static {
        patterns.put(ValidationParameters.NAME, Pattern.compile("[a-zA-ZА-ЩЬЮЯҐЄІЇа-щьюяґєії'`]+"));
        patterns.put(ValidationParameters.EMAIL, Pattern.compile("\\w+@[a-zA-Z0-9]+\\.[a-z]+"));
        patterns.put(ValidationParameters.PASSWORD, Pattern.compile("(?=.*\\d).{4,}$"));
        patterns.put(ValidationParameters.DOC_NUMBER, Pattern.compile("^\\d{9}$"));
        patterns.put(ValidationParameters.RNTRC, Pattern.compile("^\\d{10}$"));
        patterns.put(ValidationParameters.AUTHORITY, Pattern.compile("^\\d{4}$"));
    }

    private Object data;
    private ValidationParameters validationParameter;

    private boolean isNullAllowed = false;

    /**
     * Builder of Validatable object. Has {@link #of(Object, ValidationParameters, boolean) overloaded method} for specifying if NULL value allowed or not.
     * @param data any value that should be validated.
     * @param validationParameter
     * @return Validatable entity.
     */
    public static Validatable of(Object data, ValidationParameters validationParameter){
        Validatable validatable = new Validatable();

        if(multieq(validationParameter, ValidationParameters.DATE_OF_BIRTH, ValidationParameters.DATE_OF_ISSUE) && !(data instanceof LocalDate)){
            try {
                data = LocalDate.parse((String) data, Utils.localDateFormatter);
            }catch (Exception e){
                throw new IllegalArgumentException(String.format("With {} you should pass LocalDate object or string, which can be parsed to LocalDate! Current value: {}.", validationParameter, data));
            }
        }

        validatable.setData(data);
        validatable.setValidationParameter(validationParameter);

        return validatable;
    }

    public static Validatable of(Object data, ValidationParameters validationParameter, boolean isNullAllowed){
        Validatable val = Validatable.of(data, validationParameter);
        val.setNullAllowed(isNullAllowed);

        return val;
    }

    public boolean validate(){
        if(data == null)
            return isNullAllowed;

        Pattern pattern = patterns.get(validationParameter);

        if(pattern != null){
            String str = String.valueOf(data);
            return pattern.matcher(str).matches();
        }else{
            if(data instanceof LocalDate ld){
                return switch (validationParameter){
                    case DATE_OF_BIRTH -> ld.isAfter(LocalDate.of(1910, Month.JANUARY, 1)) && ld.isBefore(LocalDate.now());
                    case DATE_OF_ISSUE -> ld.isAfter(LocalDate.of(1991, Month.JANUARY, 1)) && ld.isBefore(LocalDate.now().plusDays(1));
                    default -> false;
                };
            }
            return false;
        }


    }


    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public ValidationParameters getValidationParameter() {
        return validationParameter;
    }

    public void setValidationParameter(ValidationParameters validationParameter) {
        this.validationParameter = validationParameter;
    }

    public boolean isNullAllowed() {
        return isNullAllowed;
    }

    public void setNullAllowed(boolean nullAllowed) {
        isNullAllowed = nullAllowed;
    }


}
