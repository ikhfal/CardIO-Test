package com.idemia.tec.jkt.cardiotest.service;

import com.idemia.tec.jkt.cardiotest.response.TestSuiteResponse;

public interface RunService {

    TestSuiteResponse runAll();
    boolean runAtr();
    boolean runDeltaTest();

    boolean runSqnMax();
    boolean runRfmUsim();
    boolean runRfmUsimUpdateRecord();
    boolean runRfmUsimExpandedMode();
    boolean runRfmGsm();
    boolean runRfmGsmUpdateRecord();
    boolean runRfmGsmExpandedMode();
    boolean runRfmIsim();
    boolean runRfmIsimUpdateRecord();
    boolean runRfmIsimExpandedMode();
    boolean runRam();
    boolean runRamUpdateRecord();
    boolean runRamExpandedMode();
    boolean runVerifGp();
    boolean runSecretCodes3g();
    boolean runSecretCodes2g();
    boolean runRfmCustom();
    boolean runRfmCustomUpdateRecord();
    boolean runRfmCustomExpandedMode();

    // --------
    boolean runLinkFilesTest();
    boolean runRuwiTest();
    boolean runSfiTest();

    boolean runHighStressAreaTest();
    // --------



}
