package com.idemia.tec.jkt.cardiotest.model;

import java.util.List;

public class RunSettings {

    private String projectPath;
    private String advSaveVariablesPath;
    private int readerNumber;
    private boolean stopOnError;
    private String requestId;
    private String requestName;
    private String profileName;
    private int profileVersion;
    private String cardImageItemId;
    private String customer;
    private String developerName;
    private String testerName;
    private List<VariableMapping> variableMappings;
    private ATR atr;
    private SecretCodes secretCodes;
    private CardParameters cardParameters;
    private Authentication authentication;
    private List<SCP80Keyset> scp80Keysets;
    private List<AppletParam> appletParams;
    private SmsUpdate smsUpdate;
    private RfmUsim rfmUsim;
    private RfmGsm rfmGsm;
    private RfmIsim rfmIsim;
    private RfmCustom rfmCustom;
    private Ram ram;
    private List<CustomScript> customScriptsSection1;
    private List<CustomScript> customScriptsSection2;
    private List<CustomScript> customScriptsSection3;
    private FileManagement fileManagement;
    private FMLinkFiles fMLinkFiles;
    private FMRuwi fMRuwi;
    private FileManagement2 fileManagement2;
    private FM2HighStressArea FM2HighStressArea;


    public RunSettings() { }

    public RunSettings(String projectPath, String advSaveVariablesPath, int readerNumber, boolean stopOnError,
                       String requestId, String requestName, String profileName, int profileVersion,
                       String cardImageItemId, String customer, String developerName, String testerName,
                       List<VariableMapping> variableMappings, ATR atr, SecretCodes secretCodes,
                       CardParameters cardParameters, Authentication authentication, List<SCP80Keyset> scp80Keysets,
                       SmsUpdate smsUpdate, RfmUsim rfmUsim, RfmGsm rfmGsm, RfmIsim rfmIsim, RfmCustom rfmCustom,
                       Ram ram, List<CustomScript> customScriptsSection1, List<CustomScript> customScriptsSection2,
                       List<CustomScript> customScriptsSection3, FileManagement fileManagement, FMLinkFiles fMLinkFiles,
                       FMRuwi fMRuwi, List<AppletParam> appletParams,FileManagement2 fileManagement2, FM2HighStressArea FM2HighStressArea) {

        this.projectPath = projectPath;
        this.advSaveVariablesPath = advSaveVariablesPath;
        this.readerNumber = readerNumber;
        this.stopOnError = stopOnError;
        this.requestId = requestId;
        this.requestName = requestName;
        this.profileName = profileName;
        this.profileVersion = profileVersion;
        this.cardImageItemId = cardImageItemId;
        this.customer = customer;
        this.developerName = developerName;
        this.testerName = testerName;
        this.variableMappings = variableMappings;
        this.atr = atr;
        this.secretCodes = secretCodes;
        this.cardParameters = cardParameters;
        this.authentication = authentication;
        this.scp80Keysets = scp80Keysets;
        this.appletParams = appletParams;
        this.smsUpdate = smsUpdate;
        this.rfmUsim = rfmUsim;
        this.rfmGsm = rfmGsm;
        this.rfmIsim = rfmIsim;
        this.rfmCustom = rfmCustom;
        this.ram = ram;
        this.customScriptsSection1 = customScriptsSection1;
        this.customScriptsSection2 = customScriptsSection2;
        this.customScriptsSection3 = customScriptsSection3;
        this.fileManagement = fileManagement;
        this.fMLinkFiles = fMLinkFiles;
        this.fMRuwi = fMRuwi;
        this.fileManagement2 = fileManagement2;
        this.FM2HighStressArea = FM2HighStressArea;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getAdvSaveVariablesPath() {
        return advSaveVariablesPath;
    }

    public void setAdvSaveVariablesPath(String advSaveVariablesPath) {
        this.advSaveVariablesPath = advSaveVariablesPath;
    }

    public int getReaderNumber() {
        return readerNumber;
    }

    public void setReaderNumber(int readerNumber) {
        this.readerNumber = readerNumber;
    }

    public boolean isStopOnError() {
        return stopOnError;
    }

    public void setStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestName() {
        return requestName;
    }

    public void setRequestName(String requestName) {
        this.requestName = requestName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public int getProfileVersion() {
        return profileVersion;
    }

    public void setProfileVersion(int profileVersion) {
        this.profileVersion = profileVersion;
    }

    public String getCardImageItemId() {
        return cardImageItemId;
    }

    public void setCardImageItemId(String cardImageItemId) {
        this.cardImageItemId = cardImageItemId;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getTesterName() {
        return testerName;
    }

    public void setTesterName(String testerName) {
        this.testerName = testerName;
    }

    public List<VariableMapping> getVariableMappings() {
        return variableMappings;
    }

    public void setVariableMappings(List<VariableMapping> variableMappings) {
        this.variableMappings = variableMappings;
    }

    public ATR getAtr() {
        return atr;
    }

    public void setAtr(ATR atr) {
        this.atr = atr;
    }

    public SecretCodes getSecretCodes() {
        return secretCodes;
    }

    public void setSecretCodes(SecretCodes secretCodes) {
        this.secretCodes = secretCodes;
    }

    public CardParameters getCardParameters() {
        return cardParameters;
    }

    public void setCardParameters(CardParameters cardParameters) {
        this.cardParameters = cardParameters;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public List<SCP80Keyset> getScp80Keysets() {
        return scp80Keysets;
    }

    public void setScp80Keysets(List<SCP80Keyset> scp80Keysets) {
        this.scp80Keysets = scp80Keysets;
    }

    public List<AppletParam> getAppletParams() {
        return appletParams;
    }

    public void setAppletParams(List<AppletParam> appletParams) {
        this.appletParams = appletParams;
    }

    public SmsUpdate getSmsUpdate() {
        return smsUpdate;
    }

    public void setSmsUpdate(SmsUpdate smsUpdate) {
        this.smsUpdate = smsUpdate;
    }

    public RfmUsim getRfmUsim() {
        return rfmUsim;
    }

    public void setRfmUsim(RfmUsim rfmUsim) {
        this.rfmUsim = rfmUsim;
    }

    public RfmGsm getRfmGsm() {
        return rfmGsm;
    }

    public void setRfmGsm(RfmGsm rfmGsm) {
        this.rfmGsm = rfmGsm;
    }

    public RfmIsim getRfmIsim() {
        return rfmIsim;
    }

    public void setRfmIsim(RfmIsim rfmIsim) {
        this.rfmIsim = rfmIsim;
    }

    public RfmCustom getRfmCustom() {
        return rfmCustom;
    }

    public void setRfmCustom(RfmCustom rfmCustom) {
        this.rfmCustom = rfmCustom; }

    public Ram getRam() {
        return ram;
    }

    public void setRam(Ram ram) {
        this.ram = ram;
    }

    public List<CustomScript> getCustomScriptsSection1() {
        return customScriptsSection1;
    }

    public void setCustomScriptsSection1(List<CustomScript> customScriptsSection1) {
        this.customScriptsSection1 = customScriptsSection1;
    }

    public List<CustomScript> getCustomScriptsSection2() {
        return customScriptsSection2;
    }

    public void setCustomScriptsSection2(List<CustomScript> customScriptsSection2) {
        this.customScriptsSection2 = customScriptsSection2;
    }

    public List<CustomScript> getCustomScriptsSection3() {
        return customScriptsSection3;
    }

    public void setCustomScriptsSection3(List<CustomScript> customScriptsSection3) {
        this.customScriptsSection3 = customScriptsSection3;

    }


    public FileManagement getFileManagement() {
        return fileManagement;
    }

    public void setFileManagement(FileManagement fileManagement) {
        this.fileManagement = fileManagement;
    }

    public FMLinkFiles getfMLinkFiles() {
        return fMLinkFiles;
    }

    public void setfMLinkFiles(FMLinkFiles fMLinkFiles) {
        this.fMLinkFiles = fMLinkFiles;
    }

    public FMRuwi getfMRuwi() {
        return fMRuwi;
    }

    public void setfMRuwi(FMRuwi fMRuwi) {
        this.fMRuwi = fMRuwi;
    }

    public FM2HighStressArea getHighStressArea() {
        return FM2HighStressArea;
    }

    public void setHighStressArea(FM2HighStressArea FM2HighStressArea) {
        this.FM2HighStressArea = FM2HighStressArea;
    }

    public FileManagement2 getFileManagement2() {
        return fileManagement2;
    }

    public void setFileManagement2(FileManagement2 fileManagement2) {
        this.fileManagement2 = fileManagement2;
    }
}
