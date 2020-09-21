package com.idemia.tec.jkt.cardiotest.model;

public class Ram {

    private boolean includeRam;
    private boolean includeRamUpdateRecord;
    private boolean includeRamExpandedMode;
    private boolean includeVerifGp;
    private String tar;
    private boolean useSpecificKeyset;
    private SCP80Keyset cipheringKeyset;
    private SCP80Keyset authKeyset;
    private MinimumSecurityLevel minimumSecurityLevel;
    private Isd isdSettings;
    private boolean testRamOk;
    private String testRamMessage;
    private boolean testRamUpdateRecordOk;
    private String testRamUpdateRecordMessage;
    private boolean testRamExpandedModeOk;
    private String testRamExpandedModeMessage;
    private boolean testVerifGpOk;
    private String testVerifGpMessage;

    public Ram() {}

    public Ram(boolean includeRam, boolean includeRamUpdateRecord, boolean includeRamExpandedMode,
               String tar, boolean useSpecificKeyset, SCP80Keyset cipheringKeyset, SCP80Keyset authKeyset,
               MinimumSecurityLevel minimumSecurityLevel, Isd isdSettings, boolean includeVerifGp ) {
        this.includeRam = includeRam;
        this.includeRamUpdateRecord = includeRamUpdateRecord;
        this.includeRamExpandedMode = includeRamExpandedMode;
        this.includeVerifGp = includeVerifGp;
        this.tar = tar;
        this.useSpecificKeyset = useSpecificKeyset;
        this.cipheringKeyset = cipheringKeyset;
        this.authKeyset = authKeyset;
        this.minimumSecurityLevel = minimumSecurityLevel;
        this.isdSettings = isdSettings;
    }

    public boolean isIncludeRam() {
        return includeRam;
    }

    public void setIncludeRam(boolean includeRam) {
        this.includeRam = includeRam;
    }

    public boolean isIncludeRamUpdateRecord() {
        return includeRamUpdateRecord;
    }

    public void setIncludeRamUpdateRecord(boolean includeRamUpdateRecord) {
        this.includeRamUpdateRecord = includeRamUpdateRecord;
    }

    public boolean isIncludeRamExpandedMode() {
        return includeRamExpandedMode;
    }

    public void setIncludeRamExpandedMode(boolean includeRamExpandedMode) {
        this.includeRamExpandedMode = includeRamExpandedMode;
    }

    public boolean isIncludeVerifGp() {
        return includeVerifGp;
    }

    public void setIncludeVerifGp (boolean includeVerifGp) {
        this.includeVerifGp = includeVerifGp;
    }

    public String getTar() {
        return tar;
    }

    public void setTar(String tar) {
        this.tar = tar;
    }

    public boolean isUseSpecificKeyset() {
        return useSpecificKeyset;
    }

    public void setUseSpecificKeyset(boolean useSpecificKeyset) {
        this.useSpecificKeyset = useSpecificKeyset;
    }

    public SCP80Keyset getCipheringKeyset() {
        return cipheringKeyset;
    }

    public void setCipheringKeyset(SCP80Keyset cipheringKeyset) {
        this.cipheringKeyset = cipheringKeyset;
    }

    public SCP80Keyset getAuthKeyset() {
        return authKeyset;
    }

    public void setAuthKeyset(SCP80Keyset authKeyset) {
        this.authKeyset = authKeyset;
    }

    public MinimumSecurityLevel getMinimumSecurityLevel() {
        return minimumSecurityLevel;
    }

    public void setMinimumSecurityLevel(MinimumSecurityLevel minimumSecurityLevel) {
        this.minimumSecurityLevel = minimumSecurityLevel;
    }

    public boolean isTestRamOk() {
        return testRamOk;
    }

    public void setTestRamOk(boolean testRamOk) {
        this.testRamOk = testRamOk;
    }

    public String getTestRamMessage() {
        return testRamMessage;
    }

    public void setTestRamMessage(String testRamMessage) {
        this.testRamMessage = testRamMessage;
    }

    public boolean isTestRamUpdateRecordOk() {
        return testRamUpdateRecordOk;
    }

    public void setTestRamUpdateRecordOk(boolean testRamUpdateRecordOk) {
        this.testRamUpdateRecordOk = testRamUpdateRecordOk;
    }

    public String getTestRamUpdateRecordMessage() {
        return testRamUpdateRecordMessage;
    }

    public void setTestRamUpdateRecordMessage(String testRamUpdateRecordMessage) {
        this.testRamUpdateRecordMessage = testRamUpdateRecordMessage;
    }

    public boolean isTestRamExpandedModeOk() {
        return testRamExpandedModeOk;
    }

    public void setTestRamExpandedModeOk(boolean testRamExpandedModeOk) {
        this.testRamExpandedModeOk = testRamExpandedModeOk;
    }

    public String getTestRamExpandedModeMessage() {
        return testRamExpandedModeMessage;
    }

    public void setTestRamExpandedModeMessage(String testRamExpandedModeMessage) {
        this.testRamExpandedModeMessage = testRamExpandedModeMessage;
    }

    public boolean isTestVerifGpOk() {
        return testVerifGpOk;
    }

    public void setTestVerifGpOk(boolean testVerifGpOk) {
        this.testVerifGpOk = testVerifGpOk;
    }

    public String getTestVerifGpMessage() {
        return testVerifGpMessage;
    }

    public void setTestVerifGpMessage(String testVerifGpMessage) {
        this.testVerifGpMessage = testVerifGpMessage;
    }

    public Isd getIsd() {
        return isdSettings;
    }

    public void setIsd(Isd isdSettings){
        this.isdSettings = isdSettings;
    }
}
