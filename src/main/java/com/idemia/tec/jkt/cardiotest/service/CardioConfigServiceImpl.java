package com.idemia.tec.jkt.cardiotest.service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idemia.tec.jkt.cardiotest.model.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@Service
public class CardioConfigServiceImpl implements CardioConfigService {

    private File runSettingsFile;

    @Override public RunSettings initConfig() {
        runSettingsFile = new File("run-settings.json");
        if (runSettingsFile.exists()) {
            // read saved settings
            ObjectMapper mapper = new ObjectMapper();
            try { return mapper.readValue(runSettingsFile, RunSettings.class); }
            catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        else {
            // constants for default values
            String PROJECT_PATH_DEFAULT = "C:\\";
            String ADV_SAVE_VAR_DEFAULT = "variables.txt";
            int READER_NUMBER_DEFAULT = 0;
            String REQUEST_ID = "EP-7746";
            String REQUEST_NAME = "Mobitel (VEON/RU): eSIM pilot";
            String PROFILE_NAME = "20200124 eSIM Mobitel";
            int PROFILE_VERSION = 1;
            String CARD_IMAGE_ITEM_ID = "ICI-183-2020-0914";
            String CUSTOMER = "VEON/MOBITEL GEORGIA";
            String DEVELOPER_NAME = "";
            String TESTER_NAME = "";

            // initialize default values and save settings
            RunSettings defaultSettings = new RunSettings();
            defaultSettings.setProjectPath(PROJECT_PATH_DEFAULT);
            defaultSettings.setAdvSaveVariablesPath(ADV_SAVE_VAR_DEFAULT);
            defaultSettings.setReaderNumber(READER_NUMBER_DEFAULT);
            defaultSettings.setStopOnError(false);
            defaultSettings.setRequestId(REQUEST_ID);
            defaultSettings.setRequestName(REQUEST_NAME);
            defaultSettings.setProfileName(PROFILE_NAME);
            defaultSettings.setProfileVersion(PROFILE_VERSION);
            defaultSettings.setCardImageItemId(CARD_IMAGE_ITEM_ID);
            defaultSettings.setCustomer(CUSTOMER);
            defaultSettings.setDeveloperName(DEVELOPER_NAME);
            defaultSettings.setTesterName(TESTER_NAME);
            defaultSettings.setVariableMappings(new ArrayList<>());

            defaultSettings.setAtr(new ATR(true, "", "", ""));
            defaultSettings.setSecretCodes(new SecretCodes(true, true, true, false, false, false, false, false));
            defaultSettings.getSecretCodes().setUseIsc2(false);
            defaultSettings.getSecretCodes().setUseIsc3(false);
            defaultSettings.getSecretCodes().setUseIsc4(false);

            CardParameters cardParameters = new CardParameters();
            cardParameters.setCardManagerAid("A000000151000000");
            cardParameters.setUsimAid("A0000000871002FFFFFFFF89");
            cardParameters.setDfUsim("7FF0");
            cardParameters.setDfGsm("3F00");
            cardParameters.setDfGsmAccess("5F3B");
            cardParameters.setDfTelecom("7F10");
            cardParameters.setIsimAid("A0000000871004FFFFFFFF89");
            cardParameters.setDfIsim("7FF2");
            cardParameters.setCsimAid("A0000000871006FFFFFFFF89");
            cardParameters.setDfCsim("7FF3");
            cardParameters.setIccid("ICCID");
            defaultSettings.setCardParameters(cardParameters);

            Authentication authentication = new Authentication();
            authentication.setIncludeDeltaTest(true);
            authentication.setIncludeSqnMax(true);
            authentication.setResLength("08");
            authentication.setAkaC1("C1");
            authentication.setAkaC2("C2");
            authentication.setAkaC3("C3");
            authentication.setAkaC4("C4");
            authentication.setAkaC5("C5");
            authentication.setAkaRi("RI");
            authentication.setRand("12345678 9ABCDEF0 12345678 9ABCDEF0");
            authentication.setSqn("00 00 00 00 00 01");
            authentication.setSqnMax("00 02 00 00 00 00");
            authentication.setDelta("FF FF FF FF FF E0");
            authentication.setAmf("0000");
            authentication.setKi("K1");
            authentication.setOpc("OPC");
            authentication.setComp1282(false);
            authentication.setComp1283(false);
            authentication.setMilenage(true);
            authentication.setIsimAuth(false);
            authentication.setGsmAlgo(false);
            defaultSettings.setAuthentication(authentication);

            defaultSettings.setScp80Keysets(new ArrayList<>());

            defaultSettings.setAppletParams(new ArrayList<>());

            SmsUpdate smsUpdate = new SmsUpdate("44", "07913366003000F0", "41", false, "05811250F3", "PoR as SMS-SUBMIT");
            defaultSettings.setSmsUpdate(smsUpdate);

            MinimumSecurityLevel rfmUsimMsl = new MinimumSecurityLevel(true, "Cryptographic Checksum", "Counter must be higher");
            rfmUsimMsl.setCipherAlgo("3DES - CBC 2 keys");
            rfmUsimMsl.setSigningAlgo("3DES - CBC 2 keys");
            rfmUsimMsl.setPorRequirement("PoR required");
            rfmUsimMsl.setPorSecurity("response with no security");

            RfmUsim rfmUsim = new RfmUsim();
            rfmUsim.setIncludeRfmUsim(true);
            rfmUsim.setTar("B00001");
            rfmUsim.setTargetEf("6F7B");
            rfmUsim.setTargetEfBadCase("6F05");
            rfmUsim.setFullAccess(true);
            rfmUsim.setMinimumSecurityLevel(rfmUsimMsl);
            defaultSettings.setRfmUsim(rfmUsim);

            MinimumSecurityLevel rfmGsmMsl = new MinimumSecurityLevel(true, "Cryptographic Checksum", "Counter must be higher");
            rfmGsmMsl.setCipherAlgo("3DES - CBC 2 keys");
            rfmGsmMsl.setSigningAlgo("3DES - CBC 2 keys");
            rfmGsmMsl.setPorRequirement("PoR required");
            rfmGsmMsl.setPorSecurity("response with no security");

            RfmGsm rfmGsm = new RfmGsm();
            rfmGsm.setIncludeRfmGsm(true);
            rfmGsm.setTar("B00001");
            rfmGsm.setTargetEf("6F7B");
            rfmGsm.setTargetEfBadCase("6F05");
            rfmGsm.setFullAccess(true);
            rfmGsm.setMinimumSecurityLevel(rfmGsmMsl);
            defaultSettings.setRfmGsm(rfmGsm);

            MinimumSecurityLevel rfmIsimMsl = new MinimumSecurityLevel(true, "Cryptographic Checksum", "Counter must be higher");
            rfmIsimMsl.setCipherAlgo("3DES - CBC 2 keys");rfmIsimMsl.setSigningAlgo("3DES - CBC 2 keys");
            rfmIsimMsl.setPorRequirement("PoR required");
            rfmIsimMsl.setPorSecurity("response with no security");

            RfmIsim rfmIsim = new RfmIsim();
            rfmIsim.setIncludeRfmIsim(true);
            rfmIsim.setTar("B00025");
            rfmIsim.setTargetEf("6FAD");
            rfmIsim.setTargetEfBadCase("6F02");
            rfmIsim.setFullAccess(true);
            rfmIsim.setMinimumSecurityLevel(rfmIsimMsl);
            defaultSettings.setRfmIsim(rfmIsim);

            MinimumSecurityLevel ramMsl = new MinimumSecurityLevel(true, "Cryptographic Checksum", "Counter must be higher");
            ramMsl.setCipherAlgo("3DES - CBC 2 keys");ramMsl.setSigningAlgo("3DES - CBC 2 keys");
            ramMsl.setPorRequirement("PoR required");
            ramMsl.setPorSecurity("response with no security");

            Isd isd = new Isd();
            isd.setMethodForGpCommand("with Card Manager Keyset");
            isd.setScLevel("00");
            isd.setScpMode("0255");

            Ram ram = new Ram();
            ram.setIncludeRam(true);
            ram.setTar("000000");
            ram.setMinimumSecurityLevel(ramMsl);

            ram.setIsd(isd);
            defaultSettings.setRam(ram);

            defaultSettings.setCustomScriptsSection1(new ArrayList<>());
            defaultSettings.setCustomScriptsSection2(new ArrayList<>());
            defaultSettings.setCustomScriptsSection3(new ArrayList<>());

            MinimumSecurityLevel rfmCustomMsl = new MinimumSecurityLevel(true, "Cryptographic Checksum", "Counter must be higher");
            rfmCustomMsl.setCipherAlgo("3DES - CBC 2 keys");
            rfmCustomMsl.setSigningAlgo("3DES - CBC 2 keys");
            rfmCustomMsl.setPorRequirement("PoR required");
            rfmCustomMsl.setPorSecurity("response with no security");

            RfmCustom rfmCustom = new RfmCustom();
            rfmCustom.setIncludeRfmCustom(true);
            rfmCustom.setTar("494D45");
            rfmCustom.setTargetDf("7FF0");
            rfmCustom.setTargetEf("6F7B");
            rfmCustom.setTargetEfBadCase("6F05");
            rfmCustom.setFullAccess(true);
            rfmCustom.setMinimumSecurityLevel(rfmCustomMsl);
            defaultSettings.setRfmCustom(rfmCustom);
            rfmCustom.setCustomRfmDesc("RFM ...");

            FileManagement fileManagement = new FileManagement(true, false, null ,true, false, null,true, false, null,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,false,0,0,false,false);
            defaultSettings.setFileManagement(fileManagement);

            ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(runSettingsFile, defaultSettings);
                return defaultSettings;
            } catch (JsonGenerationException e) {
                e.printStackTrace();
                return null;
            } catch (JsonMappingException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    @Override public void saveConfig(RunSettings runSettings) {
        ObjectMapper mapper = new ObjectMapper();
        try { mapper.writerWithDefaultPrettyPrinter().writeValue(runSettingsFile, runSettings); }
        catch (IOException e) { e.printStackTrace(); }
    }
}
