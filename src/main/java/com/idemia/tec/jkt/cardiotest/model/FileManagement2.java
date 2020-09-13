package com.idemia.tec.jkt.cardiotest.model;

public class FileManagement2 {

    private boolean includeHighStressAreaTest;
    private boolean testHighStressAreaOk;
    private String testHighStressAreaMessage;

    public int rowHighStressArea=0;
    public String[] data_highStressArea = new String[200];

    public FileManagement2() {}

    public FileManagement2(boolean includeHighStressAreaTest, boolean testHighStressAreaOk, String testHighStressAreaMessage, int rowHighStressArea) {
        this.includeHighStressAreaTest = includeHighStressAreaTest;
        this.testHighStressAreaOk = testHighStressAreaOk;
        this.testHighStressAreaMessage = testHighStressAreaMessage;
        this.rowHighStressArea = rowHighStressArea;
    }

    public boolean isIncludeHighStressAreaTest() {
        return includeHighStressAreaTest;
    }

    public void setIncludeHighStressAreaTest(boolean includeHighStressAreaTest) {
        this.includeHighStressAreaTest = includeHighStressAreaTest;
    }

    public int getRowHighStressArea() {
        return rowHighStressArea;
    }

    public void setRowHighStressArea(int rowHighStressArea) {
        this.rowHighStressArea = rowHighStressArea;
    }

    public String getData_highStressArea(int i) {
        return data_highStressArea[i];
    }

    public void setData_highStressArea(int i, String Data_highStressArea) {
        this.data_highStressArea[i] = Data_highStressArea;
    }

    public boolean isTestHighStressAreaOk() {
        return testHighStressAreaOk;
    }

    public void setTestHighStressAreaOk(boolean testHighStressAreaOk) {
        this.testHighStressAreaOk = testHighStressAreaOk;
    }

    public String getTestHighStressAreaMessage() {
        return testHighStressAreaMessage;
    }

    public void setTestHighStressAreaMessage(String testHighStressAreaMessage) {
        this.testHighStressAreaMessage = testHighStressAreaMessage;
    }
}
