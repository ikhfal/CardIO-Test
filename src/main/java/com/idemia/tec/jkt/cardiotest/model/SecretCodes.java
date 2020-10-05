package com.idemia.tec.jkt.cardiotest.model;

public class SecretCodes {

    private boolean include3gScript;
    private boolean include2gScript;

    private String gpin;
    private String lpin;
    private String gpuk;
    private String lpuk;
    private int gpinRetries;
    private int lpinRetries;
    private int gpukRetries;
    private int lpukRetries;

    private String chv1;
    private String chv2;
    private String puk1;
    private String puk2;
    private int chv1Retries;
    private int chv2Retries;
    private int puk1Retries;
    private int puk2Retries;

    private boolean pin1disabled;
    private boolean pin2disabled;

    private String isc1;
    private String isc2;
    private String isc3;
    private String isc4;
    private int isc1Retries;
    private int isc2Retries;
    private int isc3Retries;
    private int isc4Retries;
    private boolean useIsc2;
    private boolean useIsc3;
    private boolean useIsc4;

    private boolean blockGpuk;
    private boolean blockLpuk;
    private boolean blockPuk1;
    private boolean blockPuk2;

    private boolean blockIsc2;
    private boolean blockIsc3;
    private boolean blockIsc4;

    private boolean testCodes3gOk;
    private String testCodes3gMessage;
    private boolean testCodes2gOk;
    private String testCodes2gMessage;

    public SecretCodes() {}

    public SecretCodes(boolean include3gScript, boolean include2gScript, boolean pin1disabled, boolean pin2disabled,
                       boolean blockGpuk, boolean blockLpuk, boolean blockPuk1, boolean blockPuk2, boolean blockIsc2, boolean blockIsc3, boolean blockIsc4) {
        this.include3gScript = include3gScript;
        this.include2gScript = include2gScript;
        this.pin1disabled = pin1disabled;
        this.pin2disabled = pin2disabled;
        this.blockGpuk = blockGpuk;
        this.blockLpuk = blockLpuk;
        this.blockPuk1 = blockPuk1;
        this.blockPuk2 = blockPuk2;

        this.blockIsc2 = blockIsc2;
        this.blockIsc3 = blockIsc3;
        this.blockIsc4 = blockIsc4;
    }

    public SecretCodes(boolean include3gScript, boolean include2gScript, String gpin, String lpin, String gpuk, String lpuk, int gpinRetries, int lpinRetries, int gpukRetries, int lpukRetries, String chv1, String chv2, String puk1, String puk2, int chv1Retries, int chv2Retries, int puk1Retries, int puk2Retries, boolean pin1disabled, boolean pin2disabled, String isc1, String isc2, String isc3, String isc4, int isc1Retries, int isc2Retries, int isc3Retries, int isc4Retries, boolean useIsc2, boolean useIsc3, boolean useIsc4, boolean blockGpuk, boolean blockLpuk, boolean blockPuk1, boolean blockPuk2) {
        this.include3gScript = include3gScript;
        this.include2gScript = include2gScript;
        this.gpin = gpin;
        this.lpin = lpin;
        this.gpuk = gpuk;
        this.lpuk = lpuk;
        this.gpinRetries = gpinRetries;
        this.lpinRetries = lpinRetries;
        this.gpukRetries = gpukRetries;
        this.lpukRetries = lpukRetries;
        this.chv1 = chv1;
        this.chv2 = chv2;
        this.puk1 = puk1;
        this.puk2 = puk2;
        this.chv1Retries = chv1Retries;
        this.chv2Retries = chv2Retries;
        this.puk1Retries = puk1Retries;
        this.puk2Retries = puk2Retries;
        this.pin1disabled = pin1disabled;
        this.pin2disabled = pin2disabled;
        this.isc1 = isc1;
        this.isc2 = isc2;
        this.isc3 = isc3;
        this.isc4 = isc4;
        this.isc1Retries = isc1Retries;
        this.isc2Retries = isc2Retries;
        this.isc3Retries = isc3Retries;
        this.isc4Retries = isc4Retries;
        this.useIsc2 = useIsc2;
        this.useIsc3 = useIsc3;
        this.useIsc4 = useIsc4;
        this.blockGpuk = blockGpuk;
        this.blockLpuk = blockLpuk;
        this.blockPuk1 = blockPuk1;
        this.blockPuk2 = blockPuk2;
    }

    public boolean isInclude3gScript() {
        return include3gScript;
    }

    public void setInclude3gScript(boolean include3gScript) {
        this.include3gScript = include3gScript;
    }

    public boolean isInclude2gScript() {
        return include2gScript;
    }

    public void setInclude2gScript(boolean include2gScript) {
        this.include2gScript = include2gScript;
    }

    public String getGpin() {
        return gpin;
    }

    public void setGpin(String gpin) {
        this.gpin = gpin;
    }

    public String getLpin() {
        return lpin;
    }

    public void setLpin(String lpin) {
        this.lpin = lpin;
    }

    public String getGpuk() {
        return gpuk;
    }

    public void setGpuk(String gpuk) {
        this.gpuk = gpuk;
    }

    public String getLpuk() {
        return lpuk;
    }

    public void setLpuk(String lpuk) {
        this.lpuk = lpuk;
    }

    public int getGpinRetries() {
        return gpinRetries;
    }

    public void setGpinRetries(int gpinRetries) {
        this.gpinRetries = gpinRetries;
    }

    public int getLpinRetries() {
        return lpinRetries;
    }

    public void setLpinRetries(int lpinRetries) {
        this.lpinRetries = lpinRetries;
    }

    public int getGpukRetries() {
        return gpukRetries;
    }

    public void setGpukRetries(int gpukRetries) {
        this.gpukRetries = gpukRetries;
    }

    public int getLpukRetries() {
        return lpukRetries;
    }

    public void setLpukRetries(int lpukRetries) {
        this.lpukRetries = lpukRetries;
    }

    public String getChv1() {
        return chv1;
    }

    public void setChv1(String chv1) {
        this.chv1 = chv1;
    }

    public String getChv2() {
        return chv2;
    }

    public void setChv2(String chv2) {
        this.chv2 = chv2;
    }

    public String getPuk1() {
        return puk1;
    }

    public void setPuk1(String puk1) {
        this.puk1 = puk1;
    }

    public String getPuk2() {
        return puk2;
    }

    public void setPuk2(String puk2) {
        this.puk2 = puk2;
    }

    public int getChv1Retries() {
        return chv1Retries;
    }

    public void setChv1Retries(int chv1Retries) {
        this.chv1Retries = chv1Retries;
    }

    public int getChv2Retries() {
        return chv2Retries;
    }

    public void setChv2Retries(int chv2Retries) {
        this.chv2Retries = chv2Retries;
    }

    public int getPuk1Retries() {
        return puk1Retries;
    }

    public void setPuk1Retries(int puk1Retries) {
        this.puk1Retries = puk1Retries;
    }

    public int getPuk2Retries() {
        return puk2Retries;
    }

    public void setPuk2Retries(int puk2Retries) {
        this.puk2Retries = puk2Retries;
    }

    public boolean isPin1disabled() {
        return pin1disabled;
    }

    public void setPin1disabled(boolean pin1disabled) {
        this.pin1disabled = pin1disabled;
    }

    public boolean isPin2disabled() {
        return pin2disabled;
    }

    public void setPin2disabled(boolean pin2disabled) {
        this.pin2disabled = pin2disabled;
    }

    public String getIsc1() {
        return isc1;
    }

    public void setIsc1(String isc1) {
        this.isc1 = isc1;
    }

    public String getIsc2() {
        return isc2;
    }

    public void setIsc2(String isc2) {
        this.isc2 = isc2;
    }

    public String getIsc3() {
        return isc3;
    }

    public void setIsc3(String isc3) {
        this.isc3 = isc3;
    }

    public String getIsc4() {
        return isc4;
    }

    public void setIsc4(String isc4) {
        this.isc4 = isc4;
    }

    public int getIsc1Retries() {
        return isc1Retries;
    }

    public void setIsc1Retries(int isc1Retries) {
        this.isc1Retries = isc1Retries;
    }

    public int getIsc2Retries() {
        return isc2Retries;
    }

    public void setIsc2Retries(int isc2Retries) {
        this.isc2Retries = isc2Retries;
    }

    public int getIsc3Retries() {
        return isc3Retries;
    }

    public void setIsc3Retries(int isc3Retries) {
        this.isc3Retries = isc3Retries;
    }

    public int getIsc4Retries() {
        return isc4Retries;
    }

    public void setIsc4Retries(int isc4Retries) {
        this.isc4Retries = isc4Retries;
    }

    public boolean isUseIsc2() {
        return useIsc2;
    }

    public void setUseIsc2(boolean useIsc2) {
        this.useIsc2 = useIsc2;
    }

    public boolean isUseIsc3() {
        return useIsc3;
    }

    public void setUseIsc3(boolean useIsc3) {
        this.useIsc3 = useIsc3;
    }

    public boolean isUseIsc4() {
        return useIsc4;
    }

    public void setUseIsc4(boolean useIsc4) {
        this.useIsc4 = useIsc4;
    }

    public boolean isBlockGpuk() {
        return blockGpuk;
    }

    public void setBlockGpuk(boolean blockGpuk) {
        this.blockGpuk = blockGpuk;
    }

    public boolean isBlockLpuk() {
        return blockLpuk;
    }

    public void setBlockLpuk(boolean blockLpuk) {
        this.blockLpuk = blockLpuk;
    }

    public boolean isBlockPuk1() {
        return blockPuk1;
    }

    public void setBlockPuk1(boolean blockPuk1) {
        this.blockPuk1 = blockPuk1;
    }

    public boolean isBlockPuk2() {
        return blockPuk2;
    }

    public void setBlockPuk2(boolean blockPuk2) {
        this.blockPuk2 = blockPuk2;
    }


    public boolean isBlockIsc2() {
        return blockIsc2;
    }

    public void setBlockIsc2(boolean blockIsc2) {
        this.blockIsc2 = blockIsc2;
    }

    public boolean isBlockIsc3() {
        return blockIsc3;
    }

    public void setBlockIsc3(boolean blockIsc3) {
        this.blockIsc3 = blockIsc3;
    }

    public boolean isBlockIsc4() {
        return blockIsc4;
    }

    public void setBlockIsc4(boolean blockIsc4) {
        this.blockIsc4 = blockIsc4;
    }

    public boolean isTestCodes3gOk() {
        return testCodes3gOk;
    }

    public void setTestCodes3gOk(boolean testCodes3gOk) {
        this.testCodes3gOk = testCodes3gOk;
    }

    public String getTestCodes3gMessage() {
        return testCodes3gMessage;
    }

    public void setTestCodes3gMessage(String testCodes3gMessage) {
        this.testCodes3gMessage = testCodes3gMessage;
    }

    public boolean isTestCodes2gOk() {
        return testCodes2gOk;
    }

    public void setTestCodes2gOk(boolean testCodes2gOk) {
        this.testCodes2gOk = testCodes2gOk;
    }

    public String getTestCodes2gMessage() {
        return testCodes2gMessage;
    }

    public void setTestCodes2gMessage(String testCodes2gMessage) {
        this.testCodes2gMessage = testCodes2gMessage;
    }

}
