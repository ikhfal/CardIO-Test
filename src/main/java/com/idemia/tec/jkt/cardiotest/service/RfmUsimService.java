package com.idemia.tec.jkt.cardiotest.service;

import com.idemia.tec.jkt.cardiotest.controller.RootLayoutController;
import com.idemia.tec.jkt.cardiotest.model.MinimumSecurityLevel;
import com.idemia.tec.jkt.cardiotest.model.RfmUsim;
import com.idemia.tec.jkt.cardiotest.model.SCP80Keyset;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class RfmUsimService {

    @Autowired private RootLayoutController root;

    private String headerScriptRfmUsim(RfmUsim rfmUsim){
        StringBuilder headerScript = new StringBuilder();

        // call mappings and load DLLs
        headerScript.append(
                ".CALL Mapping.txt /LIST_OFF\n"
                + ".CALL Options.txt /LIST_OFF\n\n"
                + ".POWER_ON\n"
                + ".LOAD dll\\Calcul.dll\n"
                + ".LOAD dll\\OTA2.dll\n"
                + ".LOAD dll\\Var_Reader.dll\n"
        );

        // create counter and initialize for first-time run
        File counterBin = new File(root.getRunSettings().getProjectPath() + "\\scripts\\COUNTER.bin");
        if (!counterBin.exists()) {
            headerScript.append(
                "\n; initialize counter\n"
                + ".SET_BUFFER L 00 00 00 00 00\n"
                + ".EXPORT_BUFFER L COUNTER.bin\n"
            );
        }

        // load anti-replay counter
        headerScript.append(
                "\n; buffer L contains the anti-replay counter for OTA message\n"
                        + ".SET_BUFFER L\n"
                        + ".IMPORT_BUFFER L COUNTER.bin\n"
                        + ".INCREASE_BUFFER L(04:05) 0001\n"
                        + ".DISPLAY L\n"
                        + "\n; setup TAR\n"
                        + ".DEFINE %TAR " + rfmUsim.getTar() + "\n"
        );

        // enable pin if required
        if (root.getRunSettings().getSecretCodes().isPin1disabled())
            headerScript.append("\nA0 28 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000) ; enable GPIN1\n");

        return headerScript.toString();
    }

    public StringBuilder generateRfmUsim(RfmUsim rfmUsim) {
        StringBuilder rfmUsimBuffer = new StringBuilder();

        // Generate Header Script
        rfmUsimBuffer.append(this.headerScriptRfmUsim(rfmUsim));

        // case 1
        rfmUsimBuffer.append("\n*********\n; CASE 1: RFM USIM with correct security settings\n*********\n");

            // Target Files and Check Initial Content EFs isFullAccess
            if(rfmUsim.isFullAccess()){
                rfmUsimBuffer.append(this.rfmUsimDefineTargetFiles(rfmUsim)); // define Target Files
                rfmUsimBuffer.append(this.rfmUsimCheckInitialContentEf()); // check Initial Content of EF
            }
            else{
                rfmUsimBuffer.append(this.useRfmUsimDefineTargetFilesAccessDomain(rfmUsim));  // define Target Files Access Domain
                rfmUsimBuffer.append(this.useRfmUsimCheckInitialContentEfAccessDomain(rfmUsim)); // check Initial Content of EF Access Domain
            }

            // some TAR may be configured with specific keyset or use all available keysets
            if (rfmUsim.isUseSpecificKeyset())
                rfmUsimBuffer.append(rfmUsimCase1(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel(), false));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    rfmUsimBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    rfmUsimBuffer.append(rfmUsimCase1(keyset, keyset, rfmUsim.getMinimumSecurityLevel(), false));
                }
            }

            // perform negative test if not full access
            rfmUsimBuffer.append(this.usePerformNegativeTest(rfmUsim));

        //end of case 1

        // case 2
        rfmUsimBuffer.append("\n*********\n; CASE 2: (Bad Case) RFM with keyset which is not allowed in USIM TAR\n*********\n");
            if (rfmUsim.isUseSpecificKeyset())
                rfmUsimBuffer.append(rfmUsimCase2(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel(), false));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    rfmUsimBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    rfmUsimBuffer.append(rfmUsimCase2(keyset, keyset, rfmUsim.getMinimumSecurityLevel(), false));
                }
            }
        //end of case 2

        // case 3
        rfmUsimBuffer.append("\n*********\n; CASE 3: (Bad Case) send 2G command to USIM TAR\n*********\n");
            if (rfmUsim.isUseSpecificKeyset())
                rfmUsimBuffer.append(rfmUsimCase3(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel(), false));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    rfmUsimBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    rfmUsimBuffer.append(rfmUsimCase3(keyset, keyset, rfmUsim.getMinimumSecurityLevel(), false));
                }
            }
        //end of case 3

        // case 4
        rfmUsimBuffer.append("\n*********\n; CASE 4: (Bad Case) use unknown TAR\n*********\n");
            if (rfmUsim.isUseSpecificKeyset())
                rfmUsimBuffer.append(rfmUsimCase4(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel(), false));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    rfmUsimBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    rfmUsimBuffer.append(rfmUsimCase4(keyset, keyset, rfmUsim.getMinimumSecurityLevel(), false));
                }
            }
        //end of case 4

        // case 5
        rfmUsimBuffer.append("\n*********\n; CASE 5: (Bad Case) counter is low\n*********\n");
            if (Integer.parseInt(rfmUsim.getMinimumSecurityLevel().getComputedMsl(), 16) < 16)
                rfmUsimBuffer.append("\n; MSL: " + rfmUsim.getMinimumSecurityLevel().getComputedMsl() + " -- no need to check counter\n");
            else {
                if (rfmUsim.isUseSpecificKeyset())
                    rfmUsimBuffer.append(rfmUsimCase5(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel(), false));
                else {
                    for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                        rfmUsimBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                        rfmUsimBuffer.append(rfmUsimCase5(keyset, keyset, rfmUsim.getMinimumSecurityLevel(), false));
                    }
                }
            }
        //end of case 5

        // case 6
        rfmUsimBuffer.append("\n*********\n; CASE 6: (Bad Case) use bad key for authentication\n*********\n");
            if (rfmUsim.isUseSpecificKeyset())
                rfmUsimBuffer.append(rfmUsimCase6(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel(), false));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    rfmUsimBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    rfmUsimBuffer.append(rfmUsimCase6(keyset, keyset, rfmUsim.getMinimumSecurityLevel(), false));
                }
            }
        //end of case 6

        // case 7
        rfmUsimBuffer.append("\n*********\n; CASE 7: (Bad Case) insufficient MSL\n*********\n");
            if (rfmUsim.getMinimumSecurityLevel().getComputedMsl().equals("00"))
                rfmUsimBuffer.append("\n; MSL: " + rfmUsim.getMinimumSecurityLevel().getComputedMsl() + " -- case 7 is not executed\n");
            else {
                MinimumSecurityLevel lowMsl = new MinimumSecurityLevel(false, "No verification", "No counter available");
                lowMsl.setSigningAlgo("no algorithm");
                lowMsl.setCipherAlgo("no cipher");
                lowMsl.setPorRequirement("PoR required");
                lowMsl.setPorSecurity("response with no security");
                lowMsl.setCipherPor(false);
                if (rfmUsim.isUseSpecificKeyset())
                    rfmUsimBuffer.append(rfmUsimCase7(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), lowMsl, false));
                else {
                    for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                        rfmUsimBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                        rfmUsimBuffer.append(rfmUsimCase7(keyset, keyset, lowMsl, false));
                    }
                }
            }
        //end of case 7

        // case 8
        rfmUsimBuffer.append("\n*********\n; CASE 8: Bad TP-OA value\n*********\n");
            if (!root.getRunSettings().getSmsUpdate().isUseWhiteList())
                rfmUsimBuffer.append("\n; profile does not have white list configuration -- case 8 is not executed\n");
            else {
                if (rfmUsim.isUseSpecificKeyset())
                    rfmUsimBuffer.append(rfmUsimCase8(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel()));
                else {
                    for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                        rfmUsimBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                        rfmUsimBuffer.append(rfmUsimCase8(keyset, keyset, rfmUsim.getMinimumSecurityLevel()));
                    }
                }
            }
        //end of case 8

        // save counter
        rfmUsimBuffer.append(
            "\n; save counter state\n"
            + ".EXPORT_BUFFER L COUNTER.bin\n"
        );

        // disable pin if required
        if (root.getRunSettings().getSecretCodes().isPin1disabled())
            rfmUsimBuffer.append("\nA0 26 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000) ; disable GPIN1\n");

        // unload DLLs
        rfmUsimBuffer.append(
            "\n.UNLOAD Calcul.dll\n"
            + ".UNLOAD OTA2.dll\n"
            + ".UNLOAD Var_Reader.dll\n"
            + "\n.POWER_OFF\n"
        );

        return rfmUsimBuffer;
    }

    public StringBuilder generateRfmUsimUpdateRecord(RfmUsim rfmUsim) {
        StringBuilder rfmUsimUpdateRecordBuffer = new StringBuilder();

        // Generate Header Script RFM USIM
        rfmUsimUpdateRecordBuffer.append(this.headerScriptRfmUsim(rfmUsim));

        // case 1
        rfmUsimUpdateRecordBuffer.append("\n*********\n; CASE 1: RFM USIM Update Record with correct security settings\n*********\n");

        // Target Files and Check Initial Content EFs isFullAccess
            if(rfmUsim.isFullAccess()){
                rfmUsimUpdateRecordBuffer.append(this.rfmUsimDefineTargetFiles(rfmUsim)); // define Target Files
                rfmUsimUpdateRecordBuffer.append(this.rfmUsimCheckInitialContentEf()); // check Initial Content of EF
            }
            else{
                rfmUsimUpdateRecordBuffer.append(this.useRfmUsimDefineTargetFilesAccessDomain(rfmUsim));  // define Target Files Access Domain
                rfmUsimUpdateRecordBuffer.append(this.useRfmUsimCheckInitialContentEfAccessDomain(rfmUsim)); // check Initial Content of EF Access Domain
            }

        // some TAR may be configured with specific keyset or use all available keysets
        if (rfmUsim.isUseSpecificKeyset())
            rfmUsimUpdateRecordBuffer.append(rfmUsimCase1(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel(), true));
        else {
            for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                rfmUsimUpdateRecordBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                rfmUsimUpdateRecordBuffer.append(rfmUsimCase1(keyset, keyset, rfmUsim.getMinimumSecurityLevel(), true));
            }
        }

        //end of case 1

        // case 2
        rfmUsimUpdateRecordBuffer.append("\n*********\n; CASE 2: (Bad Case) RFM with keyset which is not allowed in USIM TAR\n*********\n");
            if (rfmUsim.isUseSpecificKeyset())
                rfmUsimUpdateRecordBuffer.append(rfmUsimCase2(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel(), true));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    rfmUsimUpdateRecordBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    rfmUsimUpdateRecordBuffer.append(rfmUsimCase2(keyset, keyset, rfmUsim.getMinimumSecurityLevel(), true));
                }
            }
        //end of case 2

        // case 3
        rfmUsimUpdateRecordBuffer.append("\n*********\n; CASE 3: (Bad Case) send 2G command to USIM TAR\n*********\n");
            if (rfmUsim.isUseSpecificKeyset())
                rfmUsimUpdateRecordBuffer.append(rfmUsimCase3(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel(), true));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    rfmUsimUpdateRecordBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    rfmUsimUpdateRecordBuffer.append(rfmUsimCase3(keyset, keyset, rfmUsim.getMinimumSecurityLevel(), true));
                }
            }
        //end of case 3

        // case 4
        rfmUsimUpdateRecordBuffer.append("\n*********\n; CASE 4: (Bad Case) use unknown TAR\n*********\n");
            if (rfmUsim.isUseSpecificKeyset())
                rfmUsimUpdateRecordBuffer.append(rfmUsimCase4(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel(), true));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    rfmUsimUpdateRecordBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    rfmUsimUpdateRecordBuffer.append(rfmUsimCase4(keyset, keyset, rfmUsim.getMinimumSecurityLevel(), true));
                }
            }
        //end of case 4

        // case 5
        rfmUsimUpdateRecordBuffer.append("\n*********\n; CASE 5: (Bad Case) counter is low\n*********\n");
            if (Integer.parseInt(rfmUsim.getMinimumSecurityLevel().getComputedMsl(), 16) < 16)
                rfmUsimUpdateRecordBuffer.append("\n; MSL: " + rfmUsim.getMinimumSecurityLevel().getComputedMsl() + " -- no need to check counter\n");
            else {
                if (rfmUsim.isUseSpecificKeyset())
                    rfmUsimUpdateRecordBuffer.append(rfmUsimCase5(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel(), true));
                else {
                    for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                        rfmUsimUpdateRecordBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                        rfmUsimUpdateRecordBuffer.append(rfmUsimCase5(keyset, keyset, rfmUsim.getMinimumSecurityLevel(), true));
                    }
                }
            }
        //end of case 5

        // case 6
        rfmUsimUpdateRecordBuffer.append("\n*********\n; CASE 6: (Bad Case) use bad key for authentication\n*********\n");
            if (rfmUsim.isUseSpecificKeyset())
                rfmUsimUpdateRecordBuffer.append(rfmUsimCase6(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel(), true));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    rfmUsimUpdateRecordBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    rfmUsimUpdateRecordBuffer.append(rfmUsimCase6(keyset, keyset, rfmUsim.getMinimumSecurityLevel(), true));
                }
            }
        //end of case 6

        // case 7
        rfmUsimUpdateRecordBuffer.append("\n*********\n; CASE 7: (Bad Case) insufficient MSL\n*********\n");
            if (rfmUsim.getMinimumSecurityLevel().getComputedMsl().equals("00"))
                rfmUsimUpdateRecordBuffer.append("\n; MSL: " + rfmUsim.getMinimumSecurityLevel().getComputedMsl() + " -- case 7 is not executed\n");
            else {
                MinimumSecurityLevel lowMsl = new MinimumSecurityLevel(false, "No verification", "No counter available");
                lowMsl.setSigningAlgo("no algorithm");
                lowMsl.setCipherAlgo("no cipher");
                lowMsl.setPorRequirement("PoR required");
                lowMsl.setPorSecurity("response with no security");
                lowMsl.setCipherPor(false);
                if (rfmUsim.isUseSpecificKeyset())
                    rfmUsimUpdateRecordBuffer.append(rfmUsimCase7(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), lowMsl, true));
                else {
                    for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                        rfmUsimUpdateRecordBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                        rfmUsimUpdateRecordBuffer.append(rfmUsimCase7(keyset, keyset, lowMsl, true));
                    }
                }
            }
        //end of case 7

        // save counter
        rfmUsimUpdateRecordBuffer.append(
                "\n; save counter state\n"
                + ".EXPORT_BUFFER L COUNTER.bin\n"
        );

        // disable pin if required
        if (root.getRunSettings().getSecretCodes().isPin1disabled())
            rfmUsimUpdateRecordBuffer.append("\nA0 26 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000) ; disable GPIN1\n");

        // unload DLLs
        rfmUsimUpdateRecordBuffer.append(
                "\n.UNLOAD Calcul.dll\n"
                + ".UNLOAD OTA2.dll\n"
                + ".UNLOAD Var_Reader.dll\n"
                + "\n.POWER_OFF\n"
        );

        return rfmUsimUpdateRecordBuffer;
    }

    public StringBuilder generateRfmUsimExpandedMode(RfmUsim rfmUsim) {
        StringBuilder rfmUsimExpandedModeBuffer = new StringBuilder();
        // TODO
        return rfmUsimExpandedModeBuffer;
    }

    private String rfmUsimCase1(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Boolean isUpdateRecord) {
        StringBuilder routine = new StringBuilder();
        routine.append(
            "\n.POWER_ON\n"
            + proactiveInitialization()
            + "\n; SPI settings\n"
            + ".SET_BUFFER O %" + cipherKeyset.getKicValuation() + "\n"
            + ".SET_BUFFER Q %" + authKeyset.getKidValuation() + "\n"
            + ".SET_BUFFER M " + cipherKeyset.getComputedKic() + "\n"
            + ".SET_BUFFER N " + authKeyset.getComputedKid() + "\n"
        );

        // check 0348 isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.init_ENV_0348RfmUsim());
        }
        else {
            routine.append(this.init_SMS_0348RfmUsim());
        }

        if (root.getRunSettings().getSmsUpdate().isUseWhiteList())
            routine.append(".CHANGE_TP_OA " + root.getRunSettings().getSmsUpdate().getTpOa() + "\n");
        routine.append(
            ".CHANGE_TAR %TAR\n"
            + ".CHANGE_COUNTER L\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
            + "\n; MSL = " + msl.getComputedMsl() + "\n"
            + ".SET_DLKEY_KIC O\n"
            + ".SET_DLKEY_KID Q\n"
            + ".CHANGE_KIC M\n"
            + ".CHANGE_KID N\n"
            + spiConfigurator(msl, cipherKeyset, authKeyset)
        );
        if (authKeyset.getKidMode().equals("AES - CMAC"))
            routine.append(".SET_CMAC_LENGTH " + String.format("%02X", authKeyset.getCmacLength()) + "\n");

            // Send OTA Command isFullAccess
            if(root.getRunSettings().getRfmUsim().isFullAccess()){
                routine.append(this.rfmUsimCommandViaOta());  // command(s) sent via OTA
            }
            else{
                routine.append(this.useRfmUsimCommandViaOtaAccessDomain(root.getRunSettings().getRfmUsim())); // command(s) sent via OTA Access Domain
            }

            routine.append(
                ".END_MESSAGE G J\n"
                + "; show OTA message details\n"
                + ".DISPLAY_MESSAGE J\n"
            );

            // check isUpdateRecord
            if(!isUpdateRecord){
                routine.append(this.sendEnvelopeRfmUsim("rfmUsimCase1"));
            }
            else {
                routine.append(this.updateSMSRecordRfmUsim("rfmUsimUpdateRecordCase1"));
            }


        // Check Update EF and Restore EF isFullAccess
        if (root.getRunSettings().getRfmUsim().isFullAccess()){
            routine.append(this.rfmUsimCheckUpdateEfDone()); //check update has been done on EF
            routine.append(this.rfmUsimRestoreRfmUsimInitialContentEf()); //restore initial content of EF
        }
        else {
            routine.append(this.useRfmUsimCheckUpdateEfDoneAccessDomain(root.getRunSettings().getRfmUsim())); //check update has been done on EF Access Domain
            routine.append(this.useRfmUsimRestoreRfmUsimInitialContentEfAccessDomain(root.getRunSettings().getRfmUsim())); //restore initial content of EF Access Domain
        }

        routine.append(
            "\n; increment counter by one\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
        );

        return routine.toString();
    }

    private String rfmUsimCase1NegativeTest(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl) {
        StringBuilder routine = new StringBuilder();
        routine.append(
            "\n.POWER_ON\n"
            + proactiveInitialization()
            + "\n; SPI settings\n"
            + ".SET_BUFFER O %" + cipherKeyset.getKicValuation() + "\n"
            + ".SET_BUFFER Q %" + authKeyset.getKidValuation() + "\n"
            + ".SET_BUFFER M " + cipherKeyset.getComputedKic() + "\n"
            + ".SET_BUFFER N " + authKeyset.getComputedKid() + "\n"
            + ".INIT_ENV_0348\n"
            + ".CHANGE_TP_PID " + root.getRunSettings().getSmsUpdate().getTpPid() + "\n"
        );
        if (root.getRunSettings().getSmsUpdate().isUseWhiteList())
            routine.append(".CHANGE_TP_OA " + root.getRunSettings().getSmsUpdate().getTpOa() + "\n");
        routine.append(
            ".CHANGE_TAR %TAR\n"
            + ".CHANGE_COUNTER L\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
            + "\n; MSL = " + msl.getComputedMsl() + "\n"
            + ".SET_DLKEY_KIC O\n"
            + ".SET_DLKEY_KID Q\n"
            + ".CHANGE_KIC M\n"
            + ".CHANGE_KID N\n"
            + spiConfigurator(msl, cipherKeyset, authKeyset)
        );
        if (authKeyset.getKidMode().equals("AES - CMAC"))
            routine.append(".SET_CMAC_LENGTH " + String.format("%02X", authKeyset.getCmacLength()) + "\n");
        routine.append(
            "\n; command(s) sent via OTA\n"
            + ".SET_BUFFER J 00 A4 00 00 02 %EF_ID_ERR\n"
            + ".APPEND_SCRIPT J\n"
            + ".SET_BUFFER J 00 D6 00 00 <?> AA ; update binary\n"
            + ".APPEND_SCRIPT J\n"
            + ".END_MESSAGE G J\n"
            + "; show OTA message details\n"
            + ".DISPLAY_MESSAGE J\n"
            + "; send envelope\n"
            + "A0 C2 00 00 G J (9FXX)\n"
            + ".CLEAR_SCRIPT\n"
            + "; check PoR\n"
            + "A0 C0 00 00 W(2;1) [XX XX XX XX XX XX %TAR XX XX XX XX XX XX 00 XX 69 82] (9000) ; PoR OK, but failed to update\n"
            + "\n; check update has failed\n"
            + ".POWER_ON\n"
            + "A0 20 00 00 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
        );
        if (root.getRunSettings().getSecretCodes().isUseIsc2())
            routine.append("A0 20 00 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc3())
            routine.append("A0 20 00 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc4())
            routine.append("A0 20 00 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");
        routine.append(
            "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n"
            + "A0 20 00 02 08 %" + root.getRunSettings().getSecretCodes().getChv2() + " (9000)\n"
            + "A0 A4 00 00 02 %DF_ID (9F22)\n"
            + "A0 A4 00 00 02 %EF_ID_ERR (9F0F)\n"
            + "A0 B0 00 00 01 [%EF_CONTENT_ERR] (9000)\n"
            + "\n; increment counter by one\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
        );
        return routine.toString();
    }

    private String rfmUsimCase2(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Boolean isUpdateRecord) {
        StringBuilder routine = new StringBuilder();
        routine.append(
            "\n.POWER_ON\n"
            + proactiveInitialization()
            + "\n; SPI settings\n"
            + ".SET_BUFFER O " + createFakeCipherKey(cipherKeyset) + " ; bad key\n"
            + ".SET_BUFFER Q " + createFakeAuthKey(authKeyset) + " ; bad key\n"
            + ".SET_BUFFER M 99 ; bad keyset\n"
            + ".SET_BUFFER N 99 ; bad keyset\n"
        );

        // check 0348 isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.init_ENV_0348RfmUsim());
        }
        else {
            routine.append(this.init_SMS_0348RfmUsim());
        }

        if (root.getRunSettings().getSmsUpdate().isUseWhiteList())
            routine.append(".CHANGE_TP_OA " + root.getRunSettings().getSmsUpdate().getTpOa() + "\n");
        routine.append(
            ".CHANGE_TAR %TAR\n"
            + ".CHANGE_COUNTER L\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
            + "\n; MSL = " + msl.getComputedMsl() + "\n"
            + ".SET_DLKEY_KIC O\n"
            + ".SET_DLKEY_KID Q\n"
            + ".CHANGE_KIC M\n"
            + ".CHANGE_KID N\n"
            + spiConfigurator(msl, cipherKeyset, authKeyset)
        );
        if (authKeyset.getKidMode().equals("AES - CMAC"))
            routine.append(".SET_CMAC_LENGTH " + String.format("%02X", authKeyset.getCmacLength()) + "\n");
        routine.append(
            "\n; command(s) sent via OTA\n"
            + ".SET_BUFFER J 00 A4 00 00 02 3F00\n"
            + ".APPEND_SCRIPT J\n"
            + ".END_MESSAGE G J\n"
        );

        // check isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.sendEnvelopeRfmUsim("rfmUsimCase2"));
        }
        else {
            routine.append(this.updateSMSRecordRfmUsim("rfmUsimUpdateRecordCase2"));
        }

        routine.append(
            "\n; increment counter by one\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
        );


        return routine.toString();
    }

    private String rfmUsimCase3(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Boolean isUpdateRecord) {
        StringBuilder routine = new StringBuilder();
        routine.append(
            "\n.POWER_ON\n"
            + proactiveInitialization()
            + "\n; SPI settings\n"
            + ".SET_BUFFER O %" + cipherKeyset.getKicValuation() + "\n"
            + ".SET_BUFFER Q %" + authKeyset.getKidValuation() + "\n"
            + ".SET_BUFFER M " + cipherKeyset.getComputedKic() + "\n"
            + ".SET_BUFFER N " + authKeyset.getComputedKid() + "\n"
        );

        // check 0348 isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.init_ENV_0348RfmUsim());
        }
        else {
            routine.append(this.init_SMS_0348RfmUsim());
        }

        if (root.getRunSettings().getSmsUpdate().isUseWhiteList())
            routine.append(".CHANGE_TP_OA " + root.getRunSettings().getSmsUpdate().getTpOa() + "\n");
        routine.append(
            ".CHANGE_TAR %TAR\n"
            + ".CHANGE_COUNTER L\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
            + "\n; MSL = " + msl.getComputedMsl() + "\n"
            + ".SET_DLKEY_KIC O\n"
            + ".SET_DLKEY_KID Q\n"
            + ".CHANGE_KIC M\n"
            + ".CHANGE_KID N\n"
            + spiConfigurator(msl, cipherKeyset, authKeyset)
        );
        if (authKeyset.getKidMode().equals("AES - CMAC"))
            routine.append(".SET_CMAC_LENGTH " + String.format("%02X", authKeyset.getCmacLength()) + "\n");
        routine.append(
            "\n; command(s) sent via OTA\n"
            + ".SET_BUFFER J A0 A4 00 00 02 3F00 ; this command isn't supported by USIM\n"
            + ".APPEND_SCRIPT J\n"
            + ".END_MESSAGE G J\n"
        );

        // check isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.sendEnvelopeRfmUsim("rfmUsimCase3"));
        }
        else {
            routine.append(this.updateSMSRecordRfmUsim("rfmUsimUpdateRecordCase3"));
        }

        routine.append(
            "\n; increment counter by one\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
        );

        return routine.toString();
    }

    private String rfmUsimCase4(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Boolean isUpdateRecord) {
        StringBuilder routine = new StringBuilder();
        routine.append(
            "\n.POWER_ON\n"
            + proactiveInitialization()
            + "\n; SPI settings\n"
            + ".SET_BUFFER O %" + cipherKeyset.getKicValuation() + "\n"
            + ".SET_BUFFER Q %" + authKeyset.getKidValuation() + "\n"
            + ".SET_BUFFER M " + cipherKeyset.getComputedKic() + "\n"
            + ".SET_BUFFER N " + authKeyset.getComputedKid() + "\n"
        );

        // check 0348 isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.init_ENV_0348RfmUsim());
        }
        else {
            routine.append(this.init_SMS_0348RfmUsim());
        }

        if (root.getRunSettings().getSmsUpdate().isUseWhiteList())
            routine.append(".CHANGE_TP_OA " + root.getRunSettings().getSmsUpdate().getTpOa() + "\n");
        routine.append(
            ".CHANGE_TAR B0FFFF ; this TAR isn't registered in profile\n"
            + ".CHANGE_COUNTER L\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
            + "\n; MSL = " + msl.getComputedMsl() + "\n"
            + ".SET_DLKEY_KIC O\n"
            + ".SET_DLKEY_KID Q\n"
            + ".CHANGE_KIC M\n"
            + ".CHANGE_KID N\n"
            + spiConfigurator(msl, cipherKeyset, authKeyset)
        );
        if (authKeyset.getKidMode().equals("AES - CMAC"))
            routine.append(".SET_CMAC_LENGTH " + String.format("%02X", authKeyset.getCmacLength()) + "\n");
        routine.append(
            "\n; command(s) sent via OTA\n"
            + ".SET_BUFFER J 00 A4 00 00 02 3F00\n"
            + ".APPEND_SCRIPT J\n"
            + ".END_MESSAGE G J\n"
        );

        // check isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.sendEnvelopeRfmUsim("rfmUsimCase4"));
        }
        else {
            routine.append(
                "\n; update EF SMS record\n" // Case 4 (Bad Case) use unknown TAR, code manually
                + "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000)\n"
                + "A0 A4 00 00 02 7F10 (9F22) ;select DF Telecom\n"
                + "A0 A4 00 00 02 6F3C (9F0F) ;select EF SMS\n"
                + "A0 DC 01 04 G J (9000) ;update EF SMS\n"
                + ".CLEAR_SCRIPT\n"
                + "\n;Check SMS Content\n"
                + "A0 B2 01 04 B0\n"
            );
        }

        routine.append(
            "\n; increment counter by one\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
        );

        return routine.toString();
    }

    private String rfmUsimCase5(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Boolean isUpdateRecord) {
        StringBuilder routine = new StringBuilder();
        routine.append(
            "\n.POWER_ON\n"
            + proactiveInitialization()
            + "\n; SPI settings\n"
            + ".SET_BUFFER O %" + cipherKeyset.getKicValuation() + "\n"
            + ".SET_BUFFER Q %" + authKeyset.getKidValuation() + "\n"
            + ".SET_BUFFER M " + cipherKeyset.getComputedKic() + "\n"
            + ".SET_BUFFER N " + authKeyset.getComputedKid() + "\n"
        );

        // check 0348 isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.init_ENV_0348RfmUsim());
        }
        else {
            routine.append(this.init_SMS_0348RfmUsim());
        }

        if (root.getRunSettings().getSmsUpdate().isUseWhiteList())
            routine.append(".CHANGE_TP_OA " + root.getRunSettings().getSmsUpdate().getTpOa() + "\n");
        routine.append(
            ".CHANGE_TAR %TAR\n"
            + ".CHANGE_COUNTER 0000000001 ; this value is lower than previous case\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
            + "\n; MSL = " + msl.getComputedMsl() + "\n"
            + ".SET_DLKEY_KIC O\n"
            + ".SET_DLKEY_KID Q\n"
            + ".CHANGE_KIC M\n"
            + ".CHANGE_KID N\n"
            + spiConfigurator(msl, cipherKeyset, authKeyset)
        );
        if (authKeyset.getKidMode().equals("AES - CMAC"))
            routine.append(".SET_CMAC_LENGTH " + String.format("%02X", authKeyset.getCmacLength()) + "\n");
        routine.append(
            "\n; command(s) sent via OTA\n"
            + ".SET_BUFFER J 00 A4 00 00 02 3F00\n"
            + ".APPEND_SCRIPT J\n"
            + ".END_MESSAGE G J\n"
        );

        // check isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.sendEnvelopeRfmUsim("rfmUsimCase5"));
        }
        else {
            routine.append(this.updateSMSRecordRfmUsim("rfmUsimUpdateRecordCase5"));
        }

        routine.append(
            "\n; increment counter by one\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
        );

        return routine.toString();
    }

    private String rfmUsimCase6(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Boolean isUpdateRecord) {
        StringBuilder routine = new StringBuilder();
        routine.append(
            "\n.POWER_ON\n"
            + proactiveInitialization()
            + "\n; SPI settings\n"
            + ".SET_BUFFER O %" + cipherKeyset.getKicValuation() + "\n"
            + ".SET_BUFFER Q " + createFakeAuthKey(authKeyset) + " ; bad authentication key\n"
            + ".SET_BUFFER M " + cipherKeyset.getComputedKic() + "\n"
            + ".SET_BUFFER N " + authKeyset.getComputedKid() + "\n"
        );

        // check 0348 isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.init_ENV_0348RfmUsim());
        }
        else {
            routine.append(this.init_SMS_0348RfmUsim());
        }

        if (root.getRunSettings().getSmsUpdate().isUseWhiteList())
            routine.append(".CHANGE_TP_OA " + root.getRunSettings().getSmsUpdate().getTpOa() + "\n");
        routine.append(
            ".CHANGE_TAR %TAR\n"
            + ".CHANGE_COUNTER L\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
            + "\n; MSL = " + msl.getComputedMsl() + "\n"
            + ".SET_DLKEY_KIC O\n"
            + ".SET_DLKEY_KID Q\n"
            + ".CHANGE_KIC M\n"
            + ".CHANGE_KID N\n"
            + spiConfigurator(msl, cipherKeyset, authKeyset)
        );
        if (authKeyset.getKidMode().equals("AES - CMAC"))
            routine.append(".SET_CMAC_LENGTH " + String.format("%02X", authKeyset.getCmacLength()) + "\n");
        routine.append(
            "\n; command(s) sent via OTA\n"
            + ".SET_BUFFER J 00 A4 00 00 02 3F00\n"
            + ".APPEND_SCRIPT J\n"
            + ".END_MESSAGE G J\n"
        );

        // check isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.sendEnvelopeRfmUsim("rfmUsimCase6"));
        }
        else {
            routine.append(this.updateSMSRecordRfmUsim("rfmUsimUpdateRecordCase6"));
        }

        routine.append(
            "\n; increment counter by one\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
        );

        return routine.toString();
    }

    private String rfmUsimCase7(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Boolean isUpdateRecord) {
        StringBuilder routine = new StringBuilder();
        routine.append(
            "\n.POWER_ON\n"
            + proactiveInitialization()
            + "\n; SPI settings\n"
            + ".SET_BUFFER O %" + cipherKeyset.getKicValuation() + "\n"
            + ".SET_BUFFER Q %" + authKeyset.getKidValuation() + "\n"
            + ".SET_BUFFER M " + cipherKeyset.getComputedKic() + "\n"
            + ".SET_BUFFER N " + authKeyset.getComputedKid() + "\n"
        );

        // check 0348 isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.init_ENV_0348RfmUsim());
        }
        else {
            routine.append(this.init_SMS_0348RfmUsim());
        }

        if (root.getRunSettings().getSmsUpdate().isUseWhiteList())
            routine.append(".CHANGE_TP_OA " + root.getRunSettings().getSmsUpdate().getTpOa() + "\n");
        routine.append(
            ".CHANGE_TAR %TAR\n"
            + ".CHANGE_COUNTER L\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
            + "\n; MSL = " + msl.getComputedMsl() + "\n"
            + ".SET_DLKEY_KIC O\n"
            + ".SET_DLKEY_KID Q\n"
            + ".CHANGE_KIC M\n"
            + ".CHANGE_KID N\n"
            + spiConfigurator(msl, cipherKeyset, authKeyset)
        );
        if (authKeyset.getKidMode().equals("AES - CMAC"))
            routine.append(".SET_CMAC_LENGTH " + String.format("%02X", authKeyset.getCmacLength()) + "\n");
        routine.append(
            "\n; command(s) sent via OTA\n"
            + ".SET_BUFFER J 00 A4 00 00 02 3F00\n"
            + ".APPEND_SCRIPT J\n"
            + ".END_MESSAGE G J\n"
        );

        // check isUpdateRecord
        if(!isUpdateRecord){
            routine.append(this.sendEnvelopeRfmUsim("rfmUsimCase7"));
        }
        else {
            routine.append(this.updateSMSRecordRfmUsim("rfmUsimUpdateRecordCase7"));
        }

        routine.append(
            "\n; increment counter by one\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
        );

        return routine.toString();
    }

    private String rfmUsimCase8(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl) {
        StringBuilder routine = new StringBuilder();
        routine.append(
            "\n.POWER_ON\n"
            + proactiveInitialization()
            + "\n; SPI settings\n"
            + ".SET_BUFFER O %" + cipherKeyset.getKicValuation() + "\n"
            + ".SET_BUFFER Q %" + authKeyset.getKidValuation() + "\n"
            + ".SET_BUFFER M " + cipherKeyset.getComputedKic() + "\n"
            + ".SET_BUFFER N " + authKeyset.getComputedKid() + "\n"
            + ".INIT_ENV_0348\n"
            + ".CHANGE_TP_PID " + root.getRunSettings().getSmsUpdate().getTpPid() + "\n"
        );
        if (root.getRunSettings().getSmsUpdate().isUseWhiteList())
            routine.append(".CHANGE_TP_OA FFFFFFFFFF ; bad TP-OA value\n");
        routine.append(
            ".CHANGE_TAR %TAR\n"
            + ".CHANGE_COUNTER L\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
            + "\n; MSL = " + msl.getComputedMsl() + "\n"
            + ".SET_DLKEY_KIC O\n"
            + ".SET_DLKEY_KID Q\n"
            + ".CHANGE_KIC M\n"
            + ".CHANGE_KID N\n"
            + spiConfigurator(msl, cipherKeyset, authKeyset)
        );
        if (authKeyset.getKidMode().equals("AES - CMAC"))
            routine.append(".SET_CMAC_LENGTH " + String.format("%02X", authKeyset.getCmacLength()) + "\n");
        routine.append(
            "\n; command(s) sent via OTA\n"
            + ".SET_BUFFER J 00 A4 00 00 02 3F00\n"
            + ".APPEND_SCRIPT J\n"
            + ".END_MESSAGE G J\n"
            + "; send envelope\n"
            + "A0 C2 00 00 G J (9000) ; no PoR returned\n"
            + ".CLEAR_SCRIPT\n"
            + "\n; increment counter by one\n"
            + ".INCREASE_BUFFER L(04:05) 0001\n"
        );
        return routine.toString();
    }

    private String proactiveInitialization() {
        StringBuilder routine = new StringBuilder();
        routine.append(
                "; proactive initialization\n"
                        + "A010000013 FFFFFFFF7F3F00DFFF00001FE28A0D02030900 (9XXX)\n"
                        + ".BEGIN_LOOP\n"
                        + "\t.SWITCH W(1:1)\n"
                        + "\t.CASE 91\n"
                        + "\t\tA0 12 00 00 W(2:2) ; fetch\n"
                        + "\t\tA0 14 00 00 0C 010301 R(6;1) 00 02028281 030100 ; terminal response\n"
                        + "\t\t.BREAK\n"
                        + "\t.DEFAULT\n"
                        + "\t\t.QUITLOOP\n"
                        + "\t\t.BREAK\n"
                        + "\t.ENDSWITCH\n"
                        + ".LOOP 100\n"
        );
        return routine.toString();
    }

    private String spiConfigurator(MinimumSecurityLevel msl, SCP80Keyset cipherKeyset, SCP80Keyset authKeyset) {
        StringBuilder spiConf = new StringBuilder();

        if (msl.getAuthVerification().equals("No verification"))
            spiConf.append(".CHANGE_CRYPTO_VERIF 00 ; No verification\n");
        if (msl.getAuthVerification().equals("Redundancy Check"))
            spiConf.append(".CHANGE_CRYPTO_VERIF 01 ; Redundancy Check\n");
        if (msl.getAuthVerification().equals("Cryptographic Checksum"))
            spiConf.append(".CHANGE_CRYPTO_VERIF 02 ; Cryptographic Checksum\n");
        if (msl.getAuthVerification().equals("Digital Signature"))
            spiConf.append(".CHANGE_CRYPTO_VERIF 03 ; Digital Signature\n");

        String signingAlgo;
        if (msl.getSigningAlgo().equals("as defined in keyset"))
            signingAlgo = authKeyset.getKidMode();
        else
            signingAlgo = msl.getSigningAlgo();
        if (signingAlgo.equals("no algorithm"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 00 ; no algorithm\n");
        if (signingAlgo.equals("DES - CBC"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 01 ; DES - CBC\n");
        if (signingAlgo.equals("AES - CMAC"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 02 ; AES - CMAC\n");
        if (signingAlgo.equals("XOR"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 03 ; XOR\n");
        if (signingAlgo.equals("3DES - CBC 2 keys"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 05 ; 3DES - CBC 2 keys\n");
        if (signingAlgo.equals("3DES - CBC 3 keys"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 09 ; 3DES - CBC 3 keys\n");
        if (signingAlgo.equals("DES - ECB"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 0D ; DES - ECB\n");
        if (signingAlgo.equals("CRC32 (may be X5h)"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 0B ; CRC32 (may be X5h)\n");
        if (signingAlgo.equals("CRC32 (may be X0h)"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 0C ; CRC32 (may be X0h)\n");
        if (signingAlgo.equals("ISO9797 Algo 3 (auth value 8 byte)"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 0F ; ISO9797 Algo 3 (auth value 8 byte)\n");
        if (signingAlgo.equals("ISO9797 Algo 3 (auth value 4 byte)"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 10 ; ISO9797 Algo 3 (auth value 4 byte)\n");
        if (signingAlgo.equals("ISO9797 Algo 4 (auth value 4 byte)"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 11 ; ISO9797 Algo 4 (auth value 4 byte)\n");
        if (signingAlgo.equals("ISO9797 Algo 4 (auth value 8 byte)"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 12 ; ISO9797 Algo 4 (auth value 8 byte)\n");
        if (signingAlgo.equals("CRC16"))
            spiConf.append(".CHANGE_ALGO_CRYPTO_VERIF 13 ; CRC16\n");

        if (msl.isUseCipher())
            spiConf.append(".CHANGE_CIPHER 01 ; use cipher\n");
        else
            spiConf.append(".CHANGE_CIPHER 00 ; no cipher\n");

        String cipherAlgo;
        if (msl.getCipherAlgo().equals("as defined in keyset"))
            cipherAlgo = cipherKeyset.getKicMode();
        else
            cipherAlgo = msl.getCipherAlgo();
        if (cipherAlgo.equals("no cipher"))
            spiConf.append(".CHANGE_ALGO_CIPHER 00 ; no cipher\n");
        if (cipherAlgo.equals("DES - CBC"))
            spiConf.append(".CHANGE_ALGO_CIPHER 01 ; DES - CBC\n");
        if (cipherAlgo.equals("AES - CBC"))
            spiConf.append(".CHANGE_ALGO_CIPHER 02 ; AES - CBC\n");
        if (cipherAlgo.equals("XOR"))
            spiConf.append(".CHANGE_ALGO_CIPHER 03 ; XOR\n");
        if (cipherAlgo.equals("3DES - CBC 2 keys"))
            spiConf.append(".CHANGE_ALGO_CIPHER 05 ; 3DES - CBC 2 keys\n");
        if (cipherAlgo.equals("3DES - CBC 3 keys"))
            spiConf.append(".CHANGE_ALGO_CIPHER 09 ; 3DES - CBC 3 keys\n");
        if (cipherAlgo.equals("DES - ECB"))
            spiConf.append(".CHANGE_ALGO_CIPHER 0D ; DES - ECB\n");

        if (msl.getCounterChecking().equals("No counter available"))
            spiConf.append(".CHANGE_CNT_CHK 00 ; No counter available\n");
        if (msl.getCounterChecking().equals("Counter available no checking"))
            spiConf.append(".CHANGE_CNT_CHK 01 ; Counter available no checking\n");
        if (msl.getCounterChecking().equals("Counter must be higher"))
            spiConf.append(".CHANGE_CNT_CHK 02 ; Counter must be higher\n");
        if (msl.getCounterChecking().equals("Counter must be one higher"))
            spiConf.append(".CHANGE_CNT_CHK 03 ; Counter must be one higher\n");

        if (msl.getPorRequirement().equals("No PoR"))
            spiConf.append(".CHANGE_POR 00 ; No PoR\n");
        if (msl.getPorRequirement().equals("PoR required"))
            spiConf.append(".CHANGE_POR 01 ; PoR required\n");
        if (msl.getPorRequirement().equals("PoR only if error"))
            spiConf.append(".CHANGE_POR 02 ; PoR only if error\n");

        if (msl.getPorSecurity().equals("response with no security"))
            spiConf.append(".CHANGE_POR_SECURITY 00 ; response with no security\n");
        if (msl.getPorSecurity().equals("response with RC"))
            spiConf.append(".CHANGE_POR_SECURITY 01 ; response with RC\n");
        if (msl.getPorSecurity().equals("response with CC"))
            spiConf.append(".CHANGE_POR_SECURITY 02 ; response with CC\n");
        if (msl.getPorSecurity().equals("response with DS"))
            spiConf.append(".CHANGE_POR_SECURITY 03 ; response with DS\n");

        if (msl.isCipherPor())
            spiConf.append(".CHANGE_POR_CIPHER 01\n");
        else
            spiConf.append(".CHANGE_POR_CIPHER 00\n");

        return spiConf.toString();
    }

    private String createFakeCipherKey(SCP80Keyset keyset) {
        if (keyset.getKicKeyLength() == 8)
            return "0102030405060708";
        if (keyset.getKicKeyLength() == 16)
            return "0102030405060708090A0B0C0D0E0F10";
        if (keyset.getKicKeyLength() == 24)
            return "0102030405060708090A0B0C0D0E0F101112131415161718";
        if (keyset.getKicKeyLength() == 32)
            return "0102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F20";
        return null; // intentionally raise syntax error in pcom
    }

    private String createFakeAuthKey(SCP80Keyset keyset) {
        if (keyset.getKidKeyLength() == 8)
            return "0102030405060708";
        if (keyset.getKidKeyLength() == 16)
            return "0102030405060708090A0B0C0D0E0F10";
        if (keyset.getKidKeyLength() == 24)
            return "0102030405060708090A0B0C0D0E0F101112131415161718";
        if (keyset.getKidKeyLength() == 32)
            return "0102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F20";
        return null; // intentionally raise syntax error in pcom
    }

    private String select2gWithAbsolutePath(String fid) {
        StringBuilder routine = new StringBuilder();
        int step = fid.length() / 4;
        int index = 0;
        for (int i = 0; i < step; i++) {
            routine.append("A0A40000 02 " + fid.substring(index, index + 4) + " (9FXX)\n");
            index += 4;
        }
        return routine.toString();
    }

    private String appendScriptSelect3g(String fid) {
        StringBuilder routine = new StringBuilder();
        int step = fid.length() / 4;
        int index = 0;
        for (int i = 0; i < step; i++) {
            routine.append(
                    ".SET_BUFFER J 00 A4 00 00 02 " + fid.substring(index, index + 4) + "\n"
                    + ".APPEND_SCRIPT J\n"
            );
            index += 4;
        }
        return routine.toString();
    }

    private String sendEnvelopeRfmUsim(String rfmUsimCases){

        StringBuilder routine = new StringBuilder();

        routine.append(
            "; send envelope\n"
            + "A0 C2 00 00 G J (9XXX)\n"
            + ".CLEAR_SCRIPT\n"
            + "; check PoR\n"
            + "A0 C0 00 00 W(2;1) [" + this.otaPorSettingRfmUsim(rfmUsimCases)+ "] (9000) ; PoR OK\n"
        );

        return routine.toString();
    }

    private String updateSMSRecordRfmUsim(String rfmUsimCases) {
        StringBuilder updateSMSRecord = new StringBuilder();

        updateSMSRecord.append(
            "\n; update EF SMS record\n"
            + "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000)\n"
            + "A0 A4 00 00 02 7F10 (9F22) ;select DF Telecom\n"
            + "A0 A4 00 00 02 6F3C (9F0F) ;select EF SMS\n"
            + "A0 DC 01 04 G J (91XX) ;update EF SMS\n"
            + ".CLEAR_SCRIPT\n"
            + "\n;Check SMS Content\n" // CHECK_SMS_CONTENT
            + "A0 B2 01 04 B0\n" // READ_SMS
            + "; check PoR\n"
            + "A0 12 00 00 W(2;1) [" + this.otaPorSettingRfmUsimUpdateRecord(rfmUsimCases) + " ] (9000)\n"
            + "A0 14 00 00 0C 8103011300 82028183 830100 (9000)\n"
        );

        return updateSMSRecord.toString();
    }

    private String otaPorSettingRfmUsim(String rfmUsimCases){
        String  POR_OK, POR_NOT_OK, BAD_CASE_WRONG_KEYSET,
                BAD_CASE_WRONG_CLASS_3G, BAD_CASE_WRONG_CLASS_2G,
                BAD_CASE_COUNTER_LOW,
                BAD_CASE_WRONG_KEY_VALUE, BAD_CASE_INSUFFICIENT_MSL, BAD_CASE_WRONG_TAR;

        String result_set = "";

        POR_OK                      = "XX XX XX XX XX XX %TAR XX XX XX XX XX XX 00 XX 90 00";
        POR_NOT_OK                  = "";
        BAD_CASE_WRONG_KEYSET       = "XX XX XX XX XX XX %TAR XX XX XX XX XX XX 06";
        BAD_CASE_WRONG_CLASS_3G     = "XX XX XX XX XX XX %TAR XX XX XX XX XX XX 00 XX 6E 00";
        BAD_CASE_WRONG_CLASS_2G     = "";
        BAD_CASE_COUNTER_LOW        = "XX XX XX XX XX XX %TAR XX XX XX XX XX XX 02";
        BAD_CASE_WRONG_KEY_VALUE    = "XX XX XX XX XX XX %TAR XX XX XX XX XX XX 01";
        BAD_CASE_INSUFFICIENT_MSL   = "XX XX XX XX XX XX %TAR XX XX XX XX XX XX 0A";
        BAD_CASE_WRONG_TAR          = "XX XX XX XX XX XX B0 FF FF XX XX XX XX XX XX 09";

        switch (rfmUsimCases){
            case "rfmUsimCase1" :
                result_set = POR_OK;
                break;
            case "rfmUsimCase2" :
                result_set = BAD_CASE_WRONG_KEYSET;
                break;
            case "rfmUsimCase3" :
                result_set =  BAD_CASE_WRONG_CLASS_3G;
                break;
            case "rfmUsimCase4" :
                result_set =  BAD_CASE_WRONG_TAR;
                break;
            case "rfmUsimCase5" :
                result_set = BAD_CASE_COUNTER_LOW;
                break;
            case "rfmUsimCase6" :
                result_set = BAD_CASE_WRONG_KEY_VALUE;
                break;
            case "rfmUsimCase7" :
                result_set = BAD_CASE_INSUFFICIENT_MSL;
                break;
        }

        return result_set;

    }

    private String otaPorSettingRfmUsimUpdateRecord(String rfmUsimCases){
        String  POR_OK, POR_NOT_OK, BAD_CASE_WRONG_KEYSET,
                BAD_CASE_WRONG_CLASS_3G, BAD_CASE_WRONG_CLASS_2G,
                BAD_CASE_COUNTER_LOW,
                BAD_CASE_WRONG_KEY_VALUE, BAD_CASE_INSUFFICIENT_MSL; //BAD_CASE_WRONG_TAR;

        String result_set = "";

        String scAddress            = root.getRunSettings().getSmsUpdate().getScAddress();
        String tag33UpdateRecord    = "D0 33 81 XX XX 13 XX 82 02 81 83 85 XX 86 " + scAddress + " 8B XX XX XX XX XX XX XX XX XX XX XX XX XX XX XX XX XX %TAR XX XX XX XX XX XX";
        String tag30UpdateRecord    = "D0 30 81 XX XX 13 XX 82 02 81 83 85 XX 86 " + scAddress + " 8B XX XX XX XX XX XX XX XX XX XX XX XX XX XX XX XX XX %TAR XX XX XX XX XX XX";

        POR_OK                      = tag33UpdateRecord+" XX XX 90 00";
        POR_NOT_OK                  = tag33UpdateRecord+" XX XX 98 04";
        BAD_CASE_WRONG_KEYSET       = tag30UpdateRecord+" 06";
        BAD_CASE_WRONG_CLASS_3G     = tag33UpdateRecord+" XX XX 6E 00";
        BAD_CASE_WRONG_CLASS_2G     = tag33UpdateRecord+" XX XX 6D 00";
        BAD_CASE_COUNTER_LOW        = tag30UpdateRecord+" 02";
        BAD_CASE_WRONG_KEY_VALUE    = tag30UpdateRecord+" 01";
        BAD_CASE_INSUFFICIENT_MSL   = tag30UpdateRecord+" 0A";
        //BAD_CASE_WRONG_TAR          = "D0 30 81 XX XX 13 XX 82 02 81 83 85 XX 86 "+scAddress+" 8B XX XX XX XX XX XX XX XX XX XX XX XX XX XX XX XX XX B0 FF FF XX XX XX XX XX XX 09";

        switch (rfmUsimCases){
            case "rfmUsimUpdateRecordCase1" :
                result_set = POR_OK;
                break;
            case "rfmUsimUpdateRecordCase2" :
                result_set = BAD_CASE_WRONG_KEYSET;
                break;
            case "rfmUsimUpdateRecordCase3" :
                result_set =  BAD_CASE_WRONG_CLASS_3G;
                break;
            case "rfmUsimUpdateRecordCase5" :
                result_set = BAD_CASE_COUNTER_LOW;
                break;
            case "rfmUsimUpdateRecordCase6" :
                result_set = BAD_CASE_WRONG_KEY_VALUE;
                break;
            case "rfmUsimUpdateRecordCase7" :
                result_set = BAD_CASE_INSUFFICIENT_MSL;
                break;
        }

        return result_set;

    }

    private String porFormatRfmUsim(String porformat){
        String result_set = "";

        switch (porformat) {
            case "PoR as SMS-SUBMIT":
                result_set = "01";
                break;
            case "PoR as SMS-DELIVER-REPORT":
                result_set = "00";
                break;
        }

        return result_set;
    }

    private String init_SMS_0348RfmUsim(){
        StringBuilder routine = new StringBuilder();

        routine.append(
                ".INIT_SMS_0348\n"
                + ".CHANGE_FIRST_BYTE " + root.getRunSettings().getSmsUpdate().getUdhiFirstByte() + "\n"
                + ".CHANGE_SC_ADDRESS " + root.getRunSettings().getSmsUpdate().getScAddress() + "\n"
                + ".CHANGE_TP_PID " + root.getRunSettings().getSmsUpdate().getTpPid() + "\n"
                + ".CHANGE_POR_FORMAT " + this.porFormatRfmUsim(root.getRunSettings().getSmsUpdate().getPorFormat()) + "\n"
        );

        return routine.toString();

    }

    private String init_ENV_0348RfmUsim(){

        StringBuilder routine = new StringBuilder();

        routine.append(
            ".INIT_ENV_0348\n"
            + ".CHANGE_TP_PID " + root.getRunSettings().getSmsUpdate().getTpPid() + "\n"
        );

        return routine.toString();
    }

    // Separate some looped on method()

    private String useRfmUsimEfAccessDomain(RfmUsim rfmUsim){

        StringBuilder accessDomain = new StringBuilder();

        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc1()){
            accessDomain.append(".DEFINE %EF_ID_USIM_ADM1 " + rfmUsim.getCustomTargetEfIsc1() + "; EF protected by ADM1\n");
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc2()){
            accessDomain.append(".DEFINE %EF_ID_USIM_ADM2 " + rfmUsim.getCustomTargetEfIsc2() + "; EF protected by ADM2\n");
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc3()){
            accessDomain.append(".DEFINE %EF_ID_USIM_ADM3 " + rfmUsim.getCustomTargetEfIsc3() + "; EF protected by ADM3\n");
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc4()){
            accessDomain.append(".DEFINE %EF_ID_USIM_ADM4 " + rfmUsim.getCustomTargetEfIsc4() + "; EF protected by ADM4\n");
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseGPin1()){
            accessDomain.append(".DEFINE %EF_ID_USIM_PIN1 " + rfmUsim.getCustomTargetEfGPin1() + "; EF protected by PIN1\n");
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseLPin1()){
            accessDomain.append(".DEFINE %EF_ID_USIM_PIN2 " + rfmUsim.getCustomTargetEfLPin1() + "; EF protected by PIN2\n");
        }

        return accessDomain.toString();
    }

    private String rfmUsimDefineTargetFiles(RfmUsim rfmUsim){

        StringBuilder targetFiles = new StringBuilder();

        targetFiles.append(
                "\n; TAR is configured for full access\n"
                        + ".DEFINE %DF_ID " + root.getRunSettings().getCardParameters().getDfUsim() + "\n"
                        + ".DEFINE %EF_ID " + rfmUsim.getTargetEf() + "\n"
                        + ".DEFINE %EF_ID_ERR " + rfmUsim.getTargetEfBadCase() + "\n"
        );

        return targetFiles.toString();
    }

    private String useRfmUsimDefineTargetFilesAccessDomain(RfmUsim rfmUsim){

        StringBuilder targetFiles = new StringBuilder();

        targetFiles.append(
                "\n; TAR is configured with access domain\n"
                + ".DEFINE %DF_ID " + root.getRunSettings().getCardParameters().getDfUsim() + "\n"
                + (this.useRfmUsimEfAccessDomain(rfmUsim))
                +".DEFINE %EF_ID_ERR " + rfmUsim.getCustomTargetEfBadCase() + "\n"
        );

        return targetFiles.toString();
    }

    private String rfmUsimCheckInitialContentEf(){

        StringBuilder checkInitialContent = new StringBuilder();

        checkInitialContent.append(
                "\n.POWER_ON\n"
                + "; check initial content\n"
                + "A0 20 00 00 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
        );

        if (root.getRunSettings().getSecretCodes().isUseIsc2())
            checkInitialContent.append("A0 20 00 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc3())
            checkInitialContent.append("A0 20 00 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc4())
            checkInitialContent.append("A0 20 00 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");
        checkInitialContent.append(
                "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n"
                + "A0 20 00 02 08 %" + root.getRunSettings().getSecretCodes().getChv2() + " (9000)\n"
                + "A0 A4 00 00 02 %DF_ID (9F22)\n"
                +"A0 A4 00 00 02 %EF_ID (9F0F)\n"
                +"A0 B0 00 00 01 (9000)\n"
                +".DEFINE %EF_CONTENT R\n"
        );

        return checkInitialContent.toString();

    }

    private String useRfmUsimCheckInitialContentEfAccessDomain(RfmUsim rfmUsim){

        StringBuilder checkInitialContent = new StringBuilder();

        checkInitialContent.append(
            "\n.POWER_ON\n"
            + "; check initial content\n"
            + "A0 20 00 00 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
        );

        if (root.getRunSettings().getSecretCodes().isUseIsc2())
            checkInitialContent.append("A0 20 00 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc3())
            checkInitialContent.append("A0 20 00 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc4())
            checkInitialContent.append("A0 20 00 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");
        checkInitialContent.append(
            "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n"
            + "A0 20 00 02 08 %" + root.getRunSettings().getSecretCodes().getChv2() + " (9000)\n"
            + "A0 A4 00 00 02 %DF_ID (9F22)\n"
        );

        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc1()){
            checkInitialContent.append(
                "; check content EF-" + rfmUsim.getCustomTargetEfIsc1() +  "\n"
                +"A0 A4 00 00 02 %EF_ID_USIM_ADM1 (9F0F)\n"
                + "A0 B0 00 00 01 (9000)\n"
                + ".DEFINE %EF_CONTENT_ADM1 R\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc2()){
            checkInitialContent.append(
                "; check content EF-" + rfmUsim.getCustomTargetEfIsc2() +  "\n"
                +"A0 A4 00 00 02 %EF_ID_USIM_ADM2 (9F0F)\n"
                +"A0 B0 00 00 01 (9000)\n"
                +".DEFINE %EF_CONTENT_ADM2 R\n"

            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc3()){
            checkInitialContent.append(
                "; check content EF-" + rfmUsim.getCustomTargetEfIsc3() +  "\n"
                +"A0 A4 00 00 02 %EF_ID_USIM_ADM3 (9F0F)\n"
                +"A0 B0 00 00 01 (9000)\n"
                +".DEFINE %EF_CONTENT_ADM3 R\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc4()){
            checkInitialContent.append(
                "; check content EF-" + rfmUsim.getCustomTargetEfIsc4() +  "\n"
                +"A0 A4 00 00 02 %EF_ID_USIM_ADM4 (9F0F)\n"
                +"A0 B0 00 00 01 (9000)\n"
                +".DEFINE %EF_CONTENT_ADM4 R\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseGPin1()){
            checkInitialContent.append(
                "; check content EF-" + rfmUsim.getCustomTargetEfGPin1() +  "\n"
                +"A0 A4 00 00 02 %EF_ID_USIM_PIN1 (9F0F)\n"
                +"A0 B0 00 00 01 (9000)\n"
                +".DEFINE %EF_CONTENT_PIN1 R\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseLPin1()){
            checkInitialContent.append(
                    "; check content EF-" + rfmUsim.getCustomTargetEfLPin1() +  "\n"
                            +"A0 A4 00 00 02 %EF_ID_USIM_PIN2 (9F0F)\n"
                            +"A0 B0 00 00 01 (9000)\n"
                            +".DEFINE %EF_CONTENT_PIN2 R\n"

            );
        }

        return checkInitialContent.toString();
    }

    private String rfmUsimCommandViaOta(){

        StringBuilder commandOta = new StringBuilder();

        commandOta.append("\n; command(s) sent via OTA\n");

        commandOta.append(
            ".SET_BUFFER J 00 A4 00 00 02 %EF_ID ; select EF\n"
            + ".APPEND_SCRIPT J\n"
            + ".SET_BUFFER J 00 D6 00 00 <?> AA ; update binary\n"
            + ".APPEND_SCRIPT J\n"
        );

        return commandOta.toString();
    }

    private String useRfmUsimCommandViaOtaAccessDomain(RfmUsim rfmUsim){

        StringBuilder commandOta = new StringBuilder();

        commandOta.append("\n; command(s) sent via OTA\n");

        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc1()){
            commandOta.append(
                ".SET_BUFFER J 00 A4 00 00 02 %EF_ID_USIM_ADM1 ; select EF on ADM1\n"
                + ".APPEND_SCRIPT J\n"
                + ".SET_BUFFER J 00 D6 00 00 <?> A1 ; update binary\n"
                + ".APPEND_SCRIPT J\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc2()){
            commandOta.append(
                ".SET_BUFFER J 00 A4 00 00 02 %EF_ID_USIM_ADM2 ; select EF on ADM2\n"
                + ".APPEND_SCRIPT J\n"
                + ".SET_BUFFER J 00 D6 00 00 <?> A2 ; update binary\n"
                + ".APPEND_SCRIPT J\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc3()){
            commandOta.append(
                ".SET_BUFFER J 00 A4 00 00 02 %EF_ID_USIM_ADM3 ; select EF on ADM3\n"
                + ".APPEND_SCRIPT J\n"
                + ".SET_BUFFER J 00 D6 00 00 <?> A3 ; update binary\n"
                + ".APPEND_SCRIPT J\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc4()){
            commandOta.append(
                ".SET_BUFFER J 00 A4 00 00 02 %EF_ID_USIM_ADM4 ; select EF on ADM4\n"
                + ".APPEND_SCRIPT J\n"
                + ".SET_BUFFER J 00 D6 00 00 <?> A4 ; update binary\n"
                + ".APPEND_SCRIPT J\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseGPin1()){
            commandOta.append(
                ".SET_BUFFER J 00 A4 00 00 02 %EF_ID_USIM_PIN1 ; select EF on PIN1\n"
                + ".APPEND_SCRIPT J\n"
                + ".SET_BUFFER J 00 D6 00 00 <?> A5 ; update binary\n"
                + ".APPEND_SCRIPT J\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseLPin1()){
            commandOta.append(
                ".SET_BUFFER J 00 A4 00 00 02 %EF_ID_USIM_PIN2 ; select EF on PIN2\n"
                + ".APPEND_SCRIPT J\n"
                + ".SET_BUFFER J 00 D6 00 00 <?> A6 ; update binary\n"
                + ".APPEND_SCRIPT J\n"
            );
        }

        return commandOta.toString();
    }

    private String rfmUsimCheckUpdateEfDone(){

        StringBuilder checkUpdateHasBeenDone = new StringBuilder();

        checkUpdateHasBeenDone.append(
            "\n; check update has been done on EF\n"
            + ".POWER_ON\n"
            + "A0 20 00 00 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
        );

        if (root.getRunSettings().getSecretCodes().isUseIsc2())
            checkUpdateHasBeenDone.append("A0 20 00 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc3())
            checkUpdateHasBeenDone.append("A0 20 00 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc4())
            checkUpdateHasBeenDone.append("A0 20 00 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");
        checkUpdateHasBeenDone.append(
            "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n"
            + "A0 20 00 02 08 %" + root.getRunSettings().getSecretCodes().getChv2() + " (9000)\n"
            + "A0 A4 00 00 02 %DF_ID (9F22)\n"
            +"A0 A4 00 00 02 %EF_ID (9F0F)\n"
            + "A0 B0 00 00 01 [AA] (9000)\n"
        );

        return checkUpdateHasBeenDone.toString();

    }

    private String useRfmUsimCheckUpdateEfDoneAccessDomain(RfmUsim rfmUsim){

        StringBuilder checkUpdateHasBeenDone = new StringBuilder();

        checkUpdateHasBeenDone.append(
            "\n; check update has been done on EF\n"
            + ".POWER_ON\n"
            + "A0 20 00 00 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
        );

        if (root.getRunSettings().getSecretCodes().isUseIsc2())
            checkUpdateHasBeenDone.append("A0 20 00 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc3())
            checkUpdateHasBeenDone.append("A0 20 00 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc4())
            checkUpdateHasBeenDone.append("A0 20 00 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");
        checkUpdateHasBeenDone.append(
            "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n"
            + "A0 20 00 02 08 %" + root.getRunSettings().getSecretCodes().getChv2() + " (9000)\n"
            + "A0 A4 00 00 02 %DF_ID (9F22)\n"
        );


        if(rfmUsim.getRfmUsimAccessDomain().isUseIsc1()){
            checkUpdateHasBeenDone.append(
                "; check update on EF-" + rfmUsim.getCustomTargetEfIsc1() +  "\n"
                +"A0 A4 00 00 02 %EF_ID_USIM_ADM1 (9F0F)\n"
                + "A0 B0 00 00 01 [A1] (9000)\n"
            );
        }
        if(rfmUsim.getRfmUsimAccessDomain().isUseIsc2()){
            checkUpdateHasBeenDone.append(
                "; check update on EF-" + rfmUsim.getCustomTargetEfIsc2() +  "\n"
                +"A0 A4 00 00 02 %EF_ID_USIM_ADM2 (9F0F)\n"
                + "A0 B0 00 00 01 [A2] (9000)\n"
            );
        }
        if(rfmUsim.getRfmUsimAccessDomain().isUseIsc3()){
            checkUpdateHasBeenDone.append(
                "; check update on EF-" + rfmUsim.getCustomTargetEfIsc3() +  "\n"
                +"A0 A4 00 00 02 %EF_ID_USIM_ADM3 (9F0F)\n"
                + "A0 B0 00 00 01 [A3] (9000)\n"
            );
        }
        if(rfmUsim.getRfmUsimAccessDomain().isUseIsc4()){
            checkUpdateHasBeenDone.append(
                "; check update on EF-" + rfmUsim.getCustomTargetEfIsc4() +  "\n"
                +"A0 A4 00 00 02 %EF_ID_USIM_ADM4 (9F0F)\n"
                + "A0 B0 00 00 01 [A4] (9000)\n"
            );
        }
        if(rfmUsim.getRfmUsimAccessDomain().isUseGPin1()){
            checkUpdateHasBeenDone.append(
                "; check update on EF-" + rfmUsim.getCustomTargetEfGPin1() +  "\n"
                +"A0 A4 00 00 02 %EF_ID_USIM_PIN1 (9F0F)\n"
                + "A0 B0 00 00 01 [A5] (9000)\n"
            );
        }
        if(rfmUsim.getRfmUsimAccessDomain().isUseLPin1()){
            checkUpdateHasBeenDone.append(
                "; check update on EF-" + rfmUsim.getCustomTargetEfLPin1() +  "\n"
                +"A0 A4 00 00 02 %EF_ID_USIM_PIN2 (9F0F)\n"
                + "A0 B0 00 00 01 [A6] (9000)\n"
            );
        }

        return checkUpdateHasBeenDone.toString();
    }

    private String rfmUsimRestoreRfmUsimInitialContentEf(){

        StringBuilder restoreInitialContent = new StringBuilder();

        restoreInitialContent.append(
            "\n; restore initial content of EF\n"
            + ".POWER_ON\n"
            + "A0 20 00 00 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
        );

        if (root.getRunSettings().getSecretCodes().isUseIsc2())
            restoreInitialContent.append("A0 20 00 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc3())
            restoreInitialContent.append("A0 20 00 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc4())
            restoreInitialContent.append("A0 20 00 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");
        restoreInitialContent.append(
            "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n"
            + "A0 20 00 02 08 %" + root.getRunSettings().getSecretCodes().getChv2() + " (9000)\n"
            + "A0 A4 00 00 02 %DF_ID (9F22)\n"
            +"A0 A4 00 00 02 %EF_ID (9F0F)\n"
            + "A0 D6 00 00 01 %EF_CONTENT (9000)\n"
        );

        return restoreInitialContent.toString();

    }

    private String useRfmUsimRestoreRfmUsimInitialContentEfAccessDomain(RfmUsim rfmUsim){

        StringBuilder restoreInitialContent = new StringBuilder();

        restoreInitialContent.append(
            "\n; restore initial content of EF\n"
            + ".POWER_ON\n"
            + "A0 20 00 00 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
        );

        if (root.getRunSettings().getSecretCodes().isUseIsc2())
            restoreInitialContent.append("A0 20 00 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc3())
            restoreInitialContent.append("A0 20 00 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc4())
            restoreInitialContent.append("A0 20 00 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");
        restoreInitialContent.append(
                "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n"
                        + "A0 20 00 02 08 %" + root.getRunSettings().getSecretCodes().getChv2() + " (9000)\n"
                        + "A0 A4 00 00 02 %DF_ID (9F22)\n"
        );


        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc1()){
            restoreInitialContent.append(
                    "; restore content EF-" + rfmUsim.getCustomTargetEfIsc1() +  "\n"
                            +"A0 A4 00 00 02 %EF_ID_USIM_ADM1 (9F0F)\n"
                            + "A0 D6 00 00 01 %EF_CONTENT_ADM1 (9000)\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc2()){
            restoreInitialContent.append(
                    "; restore content EF-" + rfmUsim.getCustomTargetEfIsc2() +  "\n"
                            +"A0 A4 00 00 02 %EF_ID_USIM_ADM2 (9F0F)\n"
                            + "A0 D6 00 00 01 %EF_CONTENT_ADM2 (9000)\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc3()){
            restoreInitialContent.append(
                    "; restore content EF-" + rfmUsim.getCustomTargetEfIsc3() +  "\n"
                            +"A0 A4 00 00 02 %EF_ID_USIM_ADM3 (9F0F)\n"
                            + "A0 D6 00 00 01 %EF_CONTENT_ADM3 (9000)\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseIsc4()){
            restoreInitialContent.append(
                    "; restore content EF-" + rfmUsim.getCustomTargetEfIsc4() +  "\n"
                            +"A0 A4 00 00 02 %EF_ID_USIM_ADM4 (9F0F)\n"
                            + "A0 D6 00 00 01 %EF_CONTENT_ADM4 (9000)\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseGPin1()){
            restoreInitialContent.append(
                    "; restore content EF-" + rfmUsim.getCustomTargetEfGPin1() +  "\n"
                            +"A0 A4 00 00 02 %EF_ID_USIM_PIN1 (9F0F)\n"
                            + "A0 D6 00 00 01 %EF_CONTENT_PIN1 (9000)\n"
            );
        }
        if (rfmUsim.getRfmUsimAccessDomain().isUseLPin1()){
            restoreInitialContent.append(
                    "; restore content EF-" + rfmUsim.getCustomTargetEfLPin1() +  "\n"
                            +"A0 A4 00 00 02 %EF_ID_USIM_PIN2 (9F0F)\n"
                            + "A0 D6 00 00 01 %EF_CONTENT_PIN2 (9000)\n"
            );
        }

        return restoreInitialContent.toString();
    }

    private String usePerformNegativeTest(RfmUsim rfmUsim){

        StringBuilder performNegativeTest = new StringBuilder();

        if (!rfmUsim.isFullAccess()) {
            performNegativeTest.append(
                "\n; perform negative test: updating " + rfmUsim.getCustomTargetEfBadCase() + " Out of Access Domain\n"
                + "\n.POWER_ON\n"
                + "; check initial content of EF\n"
                + "A0 20 00 00 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
            );

            if (root.getRunSettings().getSecretCodes().isUseIsc2())
                performNegativeTest.append("A0 20 00 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
            if (root.getRunSettings().getSecretCodes().isUseIsc3())
                performNegativeTest.append("A0 20 00 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
            if (root.getRunSettings().getSecretCodes().isUseIsc4())
                performNegativeTest.append("A0 20 00 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");

            performNegativeTest.append(
                "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n"
                + "A0 20 00 02 08 %" + root.getRunSettings().getSecretCodes().getChv2() + " (9000)\n"
                + "A0 A4 00 00 02 %DF_ID (9F22)\n"
                + "A0 A4 00 00 02 %EF_ID_ERR (9FXX)\n"
                + "A0 B0 00 00 01 (9000)\n"
                + ".DEFINE %EF_CONTENT_ERR R\n"
            );

            if (rfmUsim.isUseSpecificKeyset())
                performNegativeTest.append(rfmUsimCase1NegativeTest(rfmUsim.getCipheringKeyset(), rfmUsim.getAuthKeyset(), rfmUsim.getMinimumSecurityLevel()));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    performNegativeTest.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    performNegativeTest.append(rfmUsimCase1NegativeTest(keyset, keyset, rfmUsim.getMinimumSecurityLevel()));
                }
            }

            performNegativeTest.append("\n.UNDEFINE %EF_CONTENT_ERR \n");
        }

        return performNegativeTest.toString();
    }

//end of script
}
