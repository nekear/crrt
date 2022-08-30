package com.github.DiachenkoMD.entities.dto;

import com.github.DiachenkoMD.entities.enums.JWTErrors;
import com.github.DiachenkoMD.web.utils.JWTManager;
import io.fusionauth.jwt.InvalidJWTException;
import io.fusionauth.jwt.InvalidJWTSignatureException;
import io.fusionauth.jwt.JWTExpiredException;
import io.fusionauth.jwt.domain.JWT;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Utility to allow developer easily get analysis of JWT tokens. <br/>
 * To get token analysis, simply use {@link #of(String token)} method.
 * @implNote The need for such utility came about because the library used under the hood, FusionAuth-JWT, throws errors when decoding tokens. Constant try/catch is not the answer, so I created this thing.
 */
public class JWTAnalysis {
    private JWT token = null;
    private final List<JWTErrors> errors = new LinkedList<>();

    public static JWTAnalysis of(String token){
        JWTAnalysis analysis = new JWTAnalysis();

        JWT jwt = null;

        if(token != null)
            try{
                jwt = JWTManager.decode(token);

                if(!JWTManager.isTokenAlive(jwt))
                    analysis.addError(JWTErrors.TOKEN_DISABLED);
            }catch (InvalidJWTException e){
                analysis.addError(JWTErrors.TOKEN_BAD_FORMAT);
            }catch (JWTExpiredException e){
                analysis.addError(JWTErrors.TOKEN_EXPIRED);
            }catch (InvalidJWTSignatureException e){
                analysis.addError(JWTErrors.TOKEN_INVALID_SIGNATURE);
            }

        analysis.setToken(jwt);

        return analysis;
    }

    public JWT getToken(){
        return token;
    }

    public void setToken(JWT token){
        this.token = token;
    }

    public boolean containsFields(String... fields){
        if(token == null)
            return false;

        Map<String, Object> claims = token.getAllClaims();

        for (String field : fields) {
            if(!claims.containsKey(field))
                return false;
        }

        return true;
    }

    public boolean containsErrors(){
        return errors.size() > 0;
    }

    public boolean containsErrors(JWTErrors error){
        return errors.contains(error);
    }

    public boolean isValid(){
        return token != null && !isDisabled();
    }

    public boolean isBadFormatted(){
        return errors.contains(JWTErrors.TOKEN_BAD_FORMAT);
    }

    public boolean isInvalidSigned(){
        return errors.contains(JWTErrors.TOKEN_INVALID_SIGNATURE);
    }

    public boolean isExpired(){
        return errors.contains(JWTErrors.TOKEN_EXPIRED);
    }

    public boolean isDisabled(){
        return errors.contains(JWTErrors.TOKEN_DISABLED);
    }

    public boolean addError(JWTErrors error){
        return errors.add(error);
    }

    @Override
    public String toString() {
        return "JWTAnalysis{" +
                "token=" + token +
                ", errors=" + errors +
                '}';
    }
}
