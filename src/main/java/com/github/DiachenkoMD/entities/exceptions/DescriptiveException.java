package com.github.DiachenkoMD.entities.exceptions;

import com.github.DiachenkoMD.web.services.UsersService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;

/**
 * Custom exception to use, for example, in validations at {@link UsersService#registerUser(HttpServletRequest, HttpServletResponse)}.
 */
public class DescriptiveException extends Exception{
    /**
     * Contains reason of the exception
     */
    private final ExceptionReason reason;
    /**
     * Developer may optionally pass some parameters (arguments), to use them in {@link #execute(ExceptionReason, Runnable) execute} method.
     */
    private HashMap<String, String> arguments;
    public DescriptiveException(ExceptionReason reason){
        this.reason = reason;
    }
    public DescriptiveException(Exception exception, ExceptionReason reason){
        super(exception);
        this.reason = reason;
    }
    public DescriptiveException(String message, ExceptionReason reason) {
        super(message);
        this.reason = reason;
    }

    public DescriptiveException(final HashMap<String, String> arguments, ExceptionReason reason){
           this.arguments = arguments;
           this.reason = reason;
    }

    public ExceptionReason getReason(){
        return this.reason;
    }

    /**
     * Allow to execute some actions with matching the reasons (designed to get rid of switch-cases).
     * @param conditionalReason reason, which runs passed method, if matches with this exception`s reason
     * @param runnable method, which will run if conditionalReason equals this exception`s reason
     */
    public void execute(ExceptionReason conditionalReason, Runnable runnable){
        if(conditionalReason.equals(reason))
            runnable.run();
    }

    public String getArg(String key){
        return this.arguments.get(key);
    }

    public HashMap<String, String> getArguments(){
        return this.arguments;
    }
}
