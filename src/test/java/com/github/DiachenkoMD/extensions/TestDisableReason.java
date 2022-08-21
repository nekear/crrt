package com.github.DiachenkoMD.extensions;

public class TestDisableReason {
    private final String reason;

    public TestDisableReason(String reason){
        this.reason = reason;
    }

    public String getReason(){
        return this.reason == null ? "Test disabled. No reason specified." : this.reason;
    }
}
