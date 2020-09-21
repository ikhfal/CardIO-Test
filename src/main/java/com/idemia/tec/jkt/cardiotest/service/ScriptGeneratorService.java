package com.idemia.tec.jkt.cardiotest.service;


import com.idemia.tec.jkt.cardiotest.model.*;
import com.idemia.tec.jkt.cardiotest.model.Authentication;
import com.idemia.tec.jkt.cardiotest.model.RfmUsim;
import com.idemia.tec.jkt.cardiotest.model.RfmGsm;
import com.idemia.tec.jkt.cardiotest.model.RfmIsim;
import com.idemia.tec.jkt.cardiotest.model.RfmCustom;
import com.idemia.tec.jkt.cardiotest.model.Ram;
import com.idemia.tec.jkt.cardiotest.model.SecretCodes;

public interface ScriptGeneratorService {

    StringBuilder generateAtr();
    StringBuilder generateMapping();
    StringBuilder generateMilenageDeltaTest(Authentication authentication);
    StringBuilder generateMilenageSqnMax(Authentication authentication);
    StringBuilder generateRfmUsim(RfmUsim rfmUsim);
    StringBuilder generateRfmUsimUpdateRecord(RfmUsim rfmUsim);
    StringBuilder generateRfmUsimExpandedMode(RfmUsim rfmUsim);
    StringBuilder generateRfmGsm(RfmGsm rfmGsm);
    StringBuilder generateRfmGsmUpdateRecord(RfmGsm rfmGsm);
    StringBuilder generateRfmGsmExpandedMode(RfmGsm rfmGsm);
    StringBuilder generateRfmIsim(RfmIsim rfmIsim);
    StringBuilder generateRfmIsimUpdateRecord(RfmIsim rfmIsim);
    StringBuilder generateRfmIsimExpandedMode(RfmIsim rfmIsim);
    StringBuilder generateRam(Ram ram);
    StringBuilder generateRamUpdateRecord(Ram ram);
    StringBuilder generateRamExpandedMode(Ram ram);
    StringBuilder generateVerifGp(Ram ram);
    StringBuilder generateSecretCodes2g(SecretCodes secretCodes);
    StringBuilder generateSecretCodes3g(SecretCodes secretCodes);
    StringBuilder generateRfmCustom(RfmCustom rfmCustom);
    StringBuilder generateRfmCustomUpdateRecord(RfmCustom rfmCustom);
    StringBuilder generateRfmCustomExpandedMode(RfmCustom rfmCustom);


    // ------------------------------------------------
    StringBuilder generateFilemanagementLinkFiles(FileManagement fileManagement);
    StringBuilder generateFilemanagementRuwi(FileManagement fileManagement);
        StringBuilder generateFilemanagementRuWI01_OK_To_Go(FileManagement fileManagement);
        StringBuilder generateFilemanagementRuWI02_Method(FileManagement fileManagement);
    StringBuilder generateFilemanagementSfi(FileManagement fileManagement);

    StringBuilder generateFilemanagement2HighStressArea(FileManagement2 fileManagement2);
    // ------------------------------------------------

}
