package com.idemia.tec.jkt.cardiotest.service;

import com.idemia.tec.jkt.cardiotest.controller.RootLayoutController;
import com.idemia.tec.jkt.cardiotest.model.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScriptGeneratorServiceImpl implements ScriptGeneratorService {

    Logger logger = Logger.getLogger(ScriptGeneratorServiceImpl.class);

    @Autowired private RootLayoutController root;
    @Autowired private SecretCodesService secretCodesService;
    @Autowired private AuthenticationService authenticationService;
    @Autowired private RfmUsimService rfmUsimService;
    @Autowired private RfmGsmService rfmGsmService;
    @Autowired private RfmIsimService rfmIsimService;
    @Autowired private RfmCustomService rfmCustomService;
    @Autowired private RamService ramService;
    @Autowired private RamService verifGp;


    //-------------------
    @Autowired private FileManagementService fileManagementService;
    @Autowired private FileManagement2Service fileManagement2Service;
    //-------------------

    @Override public StringBuilder generateAtr() {
        String composeAtrScript = ".CALL Mapping.txt\n"
            + ".CALL Options.txt\n\n"
            + ".POWER_ON /PROTOCOL_ON /NEGOTIATE_PROTOCOL [%ATR] (" + root.getRunSettings().getAtr().getStatus() + ")\n"
            + ".GET_PROTOCOL_PARAMETERS\n"
            + ".POWER_OFF";
        return new StringBuilder().append(composeAtrScript);
    }

    @Override public StringBuilder generateMapping() {
        StringBuilder mappings = new StringBuilder();
        mappings.append(".CALL ..\\variables.txt /LIST_OFF\n\n");
        for (VariableMapping mapping : root.getRunSettings().getVariableMappings()) {
            if (mapping.isFixed())
                mappings.append(String.format(".DEFINE %%%s  %s\n", mapping.getMappedVariable(), mapping.getValue()));
            else
                mappings.append(String.format(".DEFINE %%%s  %%%s\n", mapping.getMappedVariable(), mapping.getMccVariable()));
        }
        return mappings;
    }

    @Override public StringBuilder generateMilenageDeltaTest(Authentication authentication) {
        return authenticationService.generateMilenageDeltaTest(authentication);
    }

    @Override public StringBuilder generateMilenageSqnMax(Authentication authentication) {
        return  authenticationService.generateMilenageSqnMax(authentication);
    }

    @Override public StringBuilder generateRfmUsim(RfmUsim rfmUsim) { return rfmUsimService.generateRfmUsim(rfmUsim); }

    @Override public StringBuilder generateRfmUsimUpdateRecord(RfmUsim rfmUsim) {
        return rfmUsimService.generateRfmUsimUpdateRecord(rfmUsim);
    }

    @Override public StringBuilder generateRfmUsimExpandedMode(RfmUsim rfmUsim) {
        StringBuilder rfmUsimExpandedModeBuffer = new StringBuilder();
        rfmUsimExpandedModeBuffer.append(
            ".CALL Mapping.txt /LIST_OFF\n"
            + ".CALL Options.txt /LIST_OFF\n\n"
            + ".POWER_ON\n"
        );

        // TODO
        rfmUsimExpandedModeBuffer.append("; TODO\n\n");

        rfmUsimExpandedModeBuffer.append(".POWER_OFF\n");
        return rfmUsimExpandedModeBuffer;
    }

    @Override public StringBuilder generateRfmGsm(RfmGsm rfmGsm) { return rfmGsmService.generateRfmGsm(rfmGsm); }

    @Override public StringBuilder generateRfmGsmUpdateRecord(RfmGsm rfmGsm) {
        return rfmGsmService.generateRfmGsmUpdateRecord(rfmGsm);
    }

    @Override public StringBuilder generateRfmGsmExpandedMode(RfmGsm rfmGsm) {
        StringBuilder rfmGsmExpandedModeBuffer = new StringBuilder();
        rfmGsmExpandedModeBuffer.append(
            ".CALL Mapping.txt /LIST_OFF\n"
            + ".CALL Options.txt /LIST_OFF\n\n"
            + ".POWER_ON\n"
        );

        // TODO
        rfmGsmExpandedModeBuffer.append("; TODO\n\n");

        rfmGsmExpandedModeBuffer.append(".POWER_OFF\n");
        return rfmGsmExpandedModeBuffer;
    }

    @Override public StringBuilder generateRfmIsim(RfmIsim rfmIsim) { return rfmIsimService.generateRfmIsim(rfmIsim); }

    @Override public StringBuilder generateRfmIsimUpdateRecord(RfmIsim rfmIsim) {
        return rfmIsimService.generateRfmIsimUpdateRecord(rfmIsim);
    }

    @Override public StringBuilder generateRfmIsimExpandedMode(RfmIsim rfmIsim) {
        StringBuilder rfmIsimExpandedModeBuffer = new StringBuilder();
        rfmIsimExpandedModeBuffer.append(
            ".CALL Mapping.txt /LIST_OFF\n"
            + ".CALL Options.txt /LIST_OFF\n\n"
            + ".POWER_ON\n"
        );

        rfmIsimExpandedModeBuffer.append("; TODO\n\n");

        rfmIsimExpandedModeBuffer.append(".POWER_OFF\n");
        return rfmIsimExpandedModeBuffer;
    }

    @Override public StringBuilder generateSecretCodes2g(SecretCodes secretCodes) {
        return secretCodesService.generateSecretCodes2g(secretCodes); }

    @Override public StringBuilder generateSecretCodes3g(SecretCodes secretCodes) {
        return secretCodesService.generateSecretCodes3g(secretCodes);
    }

    @Override public StringBuilder generateRfmCustom(RfmCustom rfmCustom) {
        return rfmCustomService.generateRfmCustom(rfmCustom);
    }

    @Override public StringBuilder generateRfmCustomUpdateRecord(RfmCustom rfmCustom) {
        return rfmCustomService.generateRfmCustomUpdateRecord(rfmCustom);
    }

    @Override public StringBuilder generateRfmCustomExpandedMode(RfmCustom rfmCustom) {
        StringBuilder rfmCustomExpandedModeBuffer = new StringBuilder();
        rfmCustomExpandedModeBuffer.append(
                ".CALL Mapping.txt /LIST_OFF\n"
                        + ".CALL Options.txt /LIST_OFF\n\n"
                        + ".POWER_ON\n"
        );

        // TODO
        rfmCustomExpandedModeBuffer.append("; TODO\n\n");

        rfmCustomExpandedModeBuffer.append(".POWER_OFF\n");
        return rfmCustomExpandedModeBuffer;
    }


    //-------------------
    @Override public StringBuilder generateFilemanagementLinkFiles(FileManagement fileManagement) {
        return fileManagementService.generateFilemanagementLinkFiles(fileManagement);
    }

    @Override public StringBuilder generateFilemanagementRuwi(FileManagement fileManagement) {
        return fileManagementService.generateFilemanagementRuwi(fileManagement);
    }
        @Override public StringBuilder generateFilemanagementRuWI01_OK_To_Go(FileManagement fileManagement) {
        return fileManagementService.generateFilemanagementRuWI01_OK_To_Go(fileManagement);
    }
        @Override public StringBuilder generateFilemanagementRuWI02_Method(FileManagement fileManagement) {
        return fileManagementService.generateFilemanagementRuWI02_Method(fileManagement);
    }

    @Override public StringBuilder generateFilemanagementSfi(FileManagement fileManagement) {
        return fileManagementService.generateFilemanagementSfi(fileManagement);
    }

    @Override public StringBuilder generateFilemanagement2HighStressArea(FileManagement2 fileManagement2) {
        return fileManagement2Service.generateFilemanagement2HighStressArea(fileManagement2);
    }
    //-------------------

    @Override
    public StringBuilder generateRam(Ram ram) {
        return ramService.generateRam(ram);
    }

    @Override
    public StringBuilder generateRamUpdateRecord(Ram ram) {
        return ramService.generateRamUpdateRecord(ram);
    }

    @Override
    public StringBuilder generateRamExpandedMode(Ram ram) {
        StringBuilder ramExpandedModeBuffer = new StringBuilder();
        ramExpandedModeBuffer.append(
                ".CALL Mapping.txt /LIST_OFF\n"
                        + ".CALL Options.txt /LIST_OFF\n\n"
                        + ".POWER_ON\n"
        );

        ramExpandedModeBuffer.append("; TODO\n\n");

        ramExpandedModeBuffer.append(".POWER_OFF\n");
        return ramExpandedModeBuffer;
    }


    @Override
    public StringBuilder generateVerifGp(Ram ram) {
        return ramService.generateVerifGp(ram);
    }

}
