package com.idemia.tec.jkt.cardiotest.model;

public class ATR {

    private boolean includeAtr;
    private String atrString;
    private String status;
    private String tck;
    private boolean testAtrOk;
    private String testAtrMesssage;

    public ATR() {}

    public ATR(boolean includeAtr, String atrString, String status, String tck) {
        this.includeAtr = includeAtr;
        this.atrString = atrString;
        this.status = status;
        this.tck = tck;
    }

    public boolean isIncludeAtr() {
        return includeAtr;
    }

    public void setIncludeAtr(boolean includeAtr) {
        this.includeAtr = includeAtr;
    }

    public String getAtrString() {
        return atrString;
    }

    public void setAtrString(String atrString) {
        this.atrString = atrString;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTck() {
        return tck;
    }

    public void setTck(String tck) {
        this.tck = tck;
    }

    public boolean isTestAtrOk() {
        return testAtrOk;
    }

    public void setTestAtrOk(boolean testAtrOk) {
        this.testAtrOk = testAtrOk;
    }

    public String getTestAtrMesssage() {
        return testAtrMesssage;
    }

    public void setTestAtrMesssage(String testAtrMesssage) {
        this.testAtrMesssage = testAtrMesssage;
    }

}
