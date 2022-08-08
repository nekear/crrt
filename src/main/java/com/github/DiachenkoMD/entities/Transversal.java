package com.github.DiachenkoMD.entities;

import com.github.DiachenkoMD.entities.adapters.Skip;
import com.github.DiachenkoMD.entities.exceptions.DescriptiveException;
import com.github.DiachenkoMD.web.utils.CryptoStore;

import java.util.Optional;

public abstract class Transversal {
    @Skip
    protected Object transversalO;

    protected void setObject(Object o){
        this.transversalO = o;
    }

    protected Object getObject(){
       return this.transversalO;
    }

    public boolean encrypt() throws DescriptiveException {
        if(transversalO instanceof Integer decryptedId){
            transversalO = CryptoStore.encrypt(String.valueOf(decryptedId));
            return true;
        }

        return false;
    }

    public boolean decrypt() throws DescriptiveException {
        if(transversalO instanceof String encryptedId) {
            transversalO = CryptoStore.decrypt(encryptedId);
            return true;
        }

        return false;
    }

    public Optional<Integer> getCleanId() throws DescriptiveException {
        if(transversalO == null)
            return Optional.empty();

        if(transversalO instanceof String encryptedId)
            return Optional.of(Integer.valueOf(CryptoStore.decrypt(encryptedId)));

        return Optional.of((Integer) transversalO);
    }
}
