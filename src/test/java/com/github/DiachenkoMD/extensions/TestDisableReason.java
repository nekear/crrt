package com.github.DiachenkoMD.extensions;

public class TestDisableReason {
    private boolean isDisabled;
    private String reason;

    TestDisableReason(){
        this(null);
    }

    TestDisableReason(String reason){
        this.isDisabled = true;
        this.reason = reason;
    }

    public boolean isDisabled(){
        return this.isDisabled;
    }

    public String getReason(){
        return this.reason == null ? "Test disabled. No reason specified." : this.reason;
    }
}
