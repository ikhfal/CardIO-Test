package com.idemia.tec.jkt.cardiotest.service;

import com.idemia.tec.jkt.cardiotest.controller.RootLayoutController;
import com.idemia.tec.jkt.cardiotest.model.MinimumSecurityLevel;
import com.idemia.tec.jkt.cardiotest.model.Isd;
import com.idemia.tec.jkt.cardiotest.model.Ram;
import com.idemia.tec.jkt.cardiotest.model.SCP80Keyset;
import com.idemia.tec.jkt.cardiotest.model.AppletParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class RamService {

    @Autowired
    private RootLayoutController root;

    public StringBuilder generateRam(Ram ram) {
        StringBuilder ramBuffer = new StringBuilder();
        // call mappings and load DLLs
        ramBuffer.append(
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
            ramBuffer.append(
                    "\n; initialize counter\n"
                            + ".SET_BUFFER L 00 00 00 00 00\n"
                            + ".EXPORT_BUFFER L COUNTER.bin\n"
            );
        }
        // load anti-replay counter
        ramBuffer.append(
                "\n; buffer L contains the anti-replay counter for OTA message\n"
                        + ".SET_BUFFER L\n"
                        + ".IMPORT_BUFFER L COUNTER.bin\n"
                        + ".INCREASE_BUFFER L(04:05) 0001\n"
                        + ".DISPLAY L\n"
                        + "\n; setup TAR\n"
                        + ".DEFINE %TAR " + ram.getTar() + "\n"
                        + ".DEFINE %RAM_MSL " + ram.getMinimumSecurityLevel().getComputedMsl() + "\n"
        );
        // enable pin if required
        if (root.getRunSettings().getSecretCodes().isPin1disabled())
            ramBuffer.append("\nA0 28 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000) ; enable GPIN1\n");
        // case 1
        ramBuffer.append("\n*********\n; CASE 1: Install Applet via OTA by sending single SMS one by one\n*********\n");
        ramBuffer.append(
                "\n.POWER_ON\n"
                        + "; check initial content of EF\n"
                        + "A0 20 00 00 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
        );
        if (root.getRunSettings().getSecretCodes().isUseIsc2())
            ramBuffer.append("A0 20 00 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc3())
            ramBuffer.append("A0 20 00 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc4())
            ramBuffer.append("A0 20 00 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");

        // some TAR may be configured with specific keyset or use all available keysets
        if (ram.isUseSpecificKeyset())
            ramBuffer.append(ramCase1(ram.getCipheringKeyset(), ram.getAuthKeyset(), ram.getMinimumSecurityLevel(), ram.getIsd(), false));
        else {
            for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                ramBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                ramBuffer.append(ramCase1(keyset, keyset, ram.getMinimumSecurityLevel(), ram.getIsd(), false));
            }
        }
        ramBuffer.append("\n.UNDEFINE %EF_CONTENT\n");

        // case 2
        ramBuffer.append("\n*********\n; CASE 2: Install Applet via OTA by sending concatenated SMS\n*********\n");
        if (ram.isUseSpecificKeyset())
            ramBuffer.append(ramCase2(ram.getCipheringKeyset(), ram.getAuthKeyset(), ram.getMinimumSecurityLevel(), ram.getIsd(), false));
        else {
            for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                ramBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                ramBuffer.append(ramCase2(keyset, keyset, ram.getMinimumSecurityLevel(), ram.getIsd(), false));
            }
        }
        // case 3
        ramBuffer.append("\n*********\n; CASE 3: (Bad Case) Install Applet via OTA by using wrong keyset\n*********\n");
        if (ram.isUseSpecificKeyset())
            ramBuffer.append(ramCase3(ram.getCipheringKeyset(), ram.getAuthKeyset(), ram.getMinimumSecurityLevel(), false));
        else {
            for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                ramBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                ramBuffer.append(ramCase3(keyset, keyset, ram.getMinimumSecurityLevel(), false));
            }
        }
        // case 4
        ramBuffer.append("\n*********\n; CASE 4: (Bad Case) Install Applet via OTA with lower counter value\n*********\n");
        if (Integer.parseInt(ram.getMinimumSecurityLevel().getComputedMsl(), 16) < 16)
            ramBuffer.append("\n; MSL: " + ram.getMinimumSecurityLevel().getComputedMsl() + " -- no need to check counter\n");
        else {
            if (ram.isUseSpecificKeyset())
                ramBuffer.append(ramCase4(ram.getCipheringKeyset(), ram.getAuthKeyset(), ram.getMinimumSecurityLevel(), false));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    ramBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    ramBuffer.append(ramCase4(keyset, keyset, ram.getMinimumSecurityLevel(), false));
                }
            }
        }
        // case 5
        ramBuffer.append("\n*********\n; CASE 5: (Bad Case) Install Applet via OTA with insufficient MSL\n*********\n");
        if (ram.getMinimumSecurityLevel().getComputedMsl().equals("00"))
            ramBuffer.append("\n; MSL: " + ram.getMinimumSecurityLevel().getComputedMsl() + " -- case 5 is not executed\n");
        else {
            MinimumSecurityLevel lowMsl = new MinimumSecurityLevel(false, "No verification", "No counter available");
            lowMsl.setSigningAlgo("no algorithm");
            lowMsl.setCipherAlgo("no cipher");
            lowMsl.setPorRequirement("PoR required");
            lowMsl.setPorSecurity("response with no security");
            lowMsl.setCipherPor(false);
            if (ram.isUseSpecificKeyset())
                ramBuffer.append(ramCase5(ram.getCipheringKeyset(), ram.getAuthKeyset(), lowMsl, true));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    ramBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    ramBuffer.append(ramCase5(keyset, keyset, lowMsl, true));
                }
            }
        }

        // save counter
        ramBuffer.append(
                "\n; save counter state\n"
                        + ".EXPORT_BUFFER L COUNTER.bin\n"
        );
        // disable pin if required
        if (root.getRunSettings().getSecretCodes().isPin1disabled())
            ramBuffer.append("\nA0 26 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000) ; disable GPIN1\n");
        // unload DLLs
        ramBuffer.append(
                "\n.UNLOAD Calcul.dll\n"
                        + ".UNLOAD OTA2.dll\n"
                        + ".UNLOAD Var_Reader.dll\n"
                        + "\n.POWER_OFF\n"
        );

        return ramBuffer;
    }

    public StringBuilder generateRamUpdateRecord(Ram ram) {
        StringBuilder ramBuffer = new StringBuilder();
        // call mappings and load DLLs
        ramBuffer.append(
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
            ramBuffer.append(
                    "\n; initialize counter\n"
                            + ".SET_BUFFER L 00 00 00 00 00\n"
                            + ".EXPORT_BUFFER L COUNTER.bin\n"
            );
        }
        // load anti-replay counter
        ramBuffer.append(
                "\n; buffer L contains the anti-replay counter for OTA message\n"
                        + ".SET_BUFFER L\n"
                        + ".IMPORT_BUFFER L COUNTER.bin\n"
                        + ".INCREASE_BUFFER L(04:05) 0001\n"
                        + ".DISPLAY L\n"
                        + "\n; setup TAR\n"
                        + ".DEFINE %TAR " + ram.getTar() + "\n"
                        + ".DEFINE %RAM_MSL " + ram.getMinimumSecurityLevel().getComputedMsl() + "\n"
        );
        // enable pin if required
        if (root.getRunSettings().getSecretCodes().isPin1disabled())
            ramBuffer.append("\nA0 28 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000) ; enable GPIN1\n");
        // case 1
        ramBuffer.append("\n*********\n; CASE 1: Install Applet via OTA by sending single SMS one by one\n*********\n");
        ramBuffer.append(
                "\n.POWER_ON\n"
                        + "; check initial content of EF\n"
                        + "A0 20 00 00 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
        );
        if (root.getRunSettings().getSecretCodes().isUseIsc2())
            ramBuffer.append("A0 20 00 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc3())
            ramBuffer.append("A0 20 00 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc4())
            ramBuffer.append("A0 20 00 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");

        // some TAR may be configured with specific keyset or use all available keysets
        if (ram.isUseSpecificKeyset())
            ramBuffer.append(ramCase1(ram.getCipheringKeyset(), ram.getAuthKeyset(), ram.getMinimumSecurityLevel(), ram.getIsd(), true));
        else {
            for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                ramBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                ramBuffer.append(ramCase1(keyset, keyset, ram.getMinimumSecurityLevel(), ram.getIsd(), true));
            }
        }
        ramBuffer.append("\n.UNDEFINE %EF_CONTENT\n");

        // case 2
        ramBuffer.append("\n*********\n; CASE 2: Install Applet via OTA by sending concatenated SMS\n*********\n");
        if (ram.isUseSpecificKeyset())
            ramBuffer.append(ramCase2(ram.getCipheringKeyset(), ram.getAuthKeyset(), ram.getMinimumSecurityLevel(), ram.getIsd(), true));
        else {
            for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                ramBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                ramBuffer.append(ramCase2(keyset, keyset, ram.getMinimumSecurityLevel(), ram.getIsd(), true));
            }
        }
        // case 3
        ramBuffer.append("\n*********\n; CASE 3: (Bad Case) Install Applet via OTA by using wrong keyset\n*********\n");
        if (ram.isUseSpecificKeyset())
            ramBuffer.append(ramCase3(ram.getCipheringKeyset(), ram.getAuthKeyset(), ram.getMinimumSecurityLevel(), true));
        else {
            for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                ramBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                ramBuffer.append(ramCase3(keyset, keyset, ram.getMinimumSecurityLevel(), true));
            }
        }
        // case 4
        ramBuffer.append("\n*********\n; CASE 4: (Bad Case) Install Applet via OTA with lower counter value\n*********\n");
        if (Integer.parseInt(ram.getMinimumSecurityLevel().getComputedMsl(), 16) < 16)
            ramBuffer.append("\n; MSL: " + ram.getMinimumSecurityLevel().getComputedMsl() + " -- no need to check counter\n");
        else {
            if (ram.isUseSpecificKeyset())
                ramBuffer.append(ramCase4(ram.getCipheringKeyset(), ram.getAuthKeyset(), ram.getMinimumSecurityLevel(), true));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    ramBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    ramBuffer.append(ramCase4(keyset, keyset, ram.getMinimumSecurityLevel(), true));
                }
            }
        }
        // case 5
        ramBuffer.append("\n*********\n; CASE 5: (Bad Case) Install Applet via OTA with insufficient MSL\n*********\n");
        if (ram.getMinimumSecurityLevel().getComputedMsl().equals("00"))
            ramBuffer.append("\n; MSL: " + ram.getMinimumSecurityLevel().getComputedMsl() + " -- case 5 is not executed\n");
        else {
            MinimumSecurityLevel lowMsl = new MinimumSecurityLevel(false, "No verification", "No counter available");
            lowMsl.setSigningAlgo("no algorithm");
            lowMsl.setCipherAlgo("no cipher");
            lowMsl.setPorRequirement("PoR required");
            lowMsl.setPorSecurity("response with no security");
            lowMsl.setCipherPor(false);
            if (ram.isUseSpecificKeyset())
                ramBuffer.append(ramCase5(ram.getCipheringKeyset(), ram.getAuthKeyset(), lowMsl, true));
            else {
                for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                    ramBuffer.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                    ramBuffer.append(ramCase5(keyset, keyset, lowMsl, true));
                }
            }
        }

        // save counter
        ramBuffer.append(
                "\n; save counter state\n"
                        + ".EXPORT_BUFFER L COUNTER.bin\n"
        );
        // disable pin if required
        if (root.getRunSettings().getSecretCodes().isPin1disabled())
            ramBuffer.append("\nA0 26 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000) ; disable GPIN1\n");
        // unload DLLs
        ramBuffer.append(
                "\n.UNLOAD Calcul.dll\n"
                        + ".UNLOAD OTA2.dll\n"
                        + ".UNLOAD Var_Reader.dll\n"
                        + "\n.POWER_OFF\n"
        );

        return ramBuffer;
    }

    public StringBuilder generateRamExpandedMode(Ram ram) {
        StringBuilder ramExpandedModeBuffer = new StringBuilder();
        // TODO
        return ramExpandedModeBuffer;
    }

    public StringBuilder generateVerifGp(Ram ram) {
        StringBuilder ramVerifGp = new StringBuilder();
        // call mappings and load DLLs
        ramVerifGp.append(
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
            ramVerifGp.append(
                    "\n; initialize counter\n"
                            + ".SET_BUFFER L 00 00 00 00 00\n"
                            + ".EXPORT_BUFFER L COUNTER.bin\n"
            );
        }
        // load anti-replay counter
        ramVerifGp.append(
                "\n; buffer L contains the anti-replay counter for OTA message\n"
                        + ".SET_BUFFER L\n"
                        + ".IMPORT_BUFFER L COUNTER.bin\n"
                        + ".INCREASE_BUFFER L(04:05) 0001\n"
                        + ".DISPLAY L\n"
                        + "\n; setup TAR\n"
                        + ".DEFINE %TAR " + ram.getTar() + "\n"
                        + ".DEFINE %RAM_MSL " + ram.getMinimumSecurityLevel().getComputedMsl() + "\n"
        );
        if (ram.isUseSpecificKeyset())
            ramVerifGp.append(checkApplet(ram.getCipheringKeyset(), ram.getAuthKeyset(), ram.getMinimumSecurityLevel(), ram.getIsd()));
        else {
            for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets()) {
                ramVerifGp.append("\n; using keyset: " + keyset.getKeysetName() + "\n");
                ramVerifGp.append(checkApplet(keyset, keyset, ram.getMinimumSecurityLevel(), ram.getIsd()));
            }
        }
        Isd isd = ram.getIsd();

        if(isd.getMethodForGpCommand().equals("with Card Manager Keyset")  || isd.getMethodForGpCommand().equals("SIMBiOs")) {
            if(!isd.isSecuredState() || isd.getMethodForGpCommand().equals("SIMBiOs")) {
                if(isd.getMethodForGpCommand().equals("SIMBiOs")) {
                    ramVerifGp.append(
                            "00 A4 04 00 <?> " + root.getRunSettings().getCardParameters().getCardManagerAid() + "\n" +
                            "00 20 00 00 08 %" + isd.getCardManagerPin() + "\n"
                    );
                }
                ramVerifGp.append(
                        "; INSTALL FOR LOAD\n" +
                        "80 E6 02 00 1F 0C A00000001853020000000010 00 00 0E EF 0C C6020000 C8020000 C7020000 00 (6101)\n" +
                        "\n" +
                        "; LOAD PACKAGE\n" +
                        "80E8000067C48201CC010016DECAFFED01020400020CA0000000185302000000001002001F0016001F0010001E0046001A00C6000A001E0000009400000000000002010004001E02000107A0000000620101010210A0000000090003FFFFFFFF8910710002030010010CA000 (6101)\n" +
                        "80E800016700001853020000000110001F06001A43800300FF00070300000035003800A4800200810101088100000700C6020048803E008800060093800B00A000060210188C00008D0001058B00027A05318F00033D8C00042E1B181D0441181D258B00057A00207A03221D (6101)\n" +
                        "80E800026775006800020002000D001300588D00072E1B8B0008311B1E8B000910F06B4B1B1E04418B0009100C6B401B1E05418B000961371B1E06418B000910126B2C1B1E07418B00096123188B000A701D3B8D0001103C8B000B7012188B000A8D0001038B000B70053B70 (6101)\n" +
                        "80E8000367027A041110178D000C601A8D000D2C19040310828B000E198B000F10206B06058D00107A08000A00000000000000000000050046001106800300068109000381090901000000060000110380030201810700068108000381080D03810204030000090381090C06 (6101)\n" +
                        "80E880043481030006810A0003810A1503810A160681070009001E0000001A070806030406040C1705060B0B090B0606050603040D05090408 (6101) \n" +
                        "; INSTALL FOR INSTALL\n" +
                        "80E60C0048 0C A00000001853020000000010 0C A00000001853020000000110 0F A00000001853020000000110524648 01001AEF16C7020000C8020000CA0C0100FF000000000003524648C90000 (6101)\n" +
                        "; DELETE INSTANCE\n" +
                        "80E4000011 4F0F A00000001853020000000110524648 (6101)\n" +
                        "; DELETE PACKAGE\n" +
                        "80E400000E 4F0C A00000001853020000000010 (6101)\n"
                );
            } else {
                ramVerifGp.append(
                        "; INSTALL FOR LOAD\n" +
                        ".SET_BUFFER N 80 E6 02 00 1F 0C A00000001853020000000010 00 00 0E EF 0C C6020000 C8020000 C7020000 00\n" +
                        scp0255(isd) +
                        "N (9000,6101)\n" +
                        "\n" +
                        "; LOAD PACKAGE\n" +
                        ".SET_BUFFER N 80E8000067C48201CC010016DECAFFED01020400020CA0000000185302000000001002001F0016001F0010001E0046001A00C6000A001E0000009400000000000002010004001E02000107A0000000620101010210A0000000090003FFFFFFFF8910710002030010010CA000 \n" +
                        scp0255(isd) +
                        "N (9000,6101)\n" +
                        ".SET_BUFFER N 80E800016700001853020000000110001F06001A43800300FF00070300000035003800A4800200810101088100000700C6020048803E008800060093800B00A000060210188C00008D0001058B00027A05318F00033D8C00042E1B181D0441181D258B00057A00207A03221D \n" +
                        scp0255(isd) +
                        "N (9000,6101)\n" +
                        ".SET_BUFFER N 80E800026775006800020002000D001300588D00072E1B8B0008311B1E8B000910F06B4B1B1E04418B0009100C6B401B1E05418B000961371B1E06418B000910126B2C1B1E07418B00096123188B000A701D3B8D0001103C8B000B7012188B000A8D0001038B000B70053B70\n" +
                        scp0255(isd) +
                        "N (9000,6101)\n" +
                        ".SET_BUFFER N 80E8000367027A041110178D000C601A8D000D2C19040310828B000E198B000F10206B06058D00107A08000A00000000000000000000050046001106800300068109000381090901000000060000110380030201810700068108000381080D03810204030000090381090C06\n" +
                        scp0255(isd) +
                        "N (9000,6101)\n" +
                        ".SET_BUFFER N 80E880043481030006810A0003810A1503810A160681070009001E0000001A070806030406040C1705060B0B090B0606050603040D05090408\n" +
                        scp0255(isd) +
                        "N (9000,6101)\n" +
                        "; INSTALL FOR INSTALL\n" +
                        ".SET_BUFFER N 80E60C00480CA000000018530200000000100CA000000018530200000001100FA0000000185302000000011052464801001AEF16C7020000C8020000CA0C0100FF000000000003524648C90000 \n" +
                        scp0255(isd) +
                        "N (9000,6101) \n" +
                        "; DELETE INSTANCE\n" +
                        ".SET_BUFFER N 80E40000114F0FA00000001853020000000110524648  \n" +
                        scp0255(isd) +
                        "N (9000,6101)\n" +
                        "; DELETE PACKAGE\n" +
                        ".SET_BUFFER N 80E400000E4F0CA00000001853020000000010  \n" +
                        scp0255(isd) +
                        "N (9000,6101) \n"
                );
            }
        }
        return ramVerifGp;
    }
    private String checkApplet(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Isd isd) {
        StringBuilder routine = new StringBuilder();
        if(isd.getMethodForGpCommand().equals("no Card Manager Keyset")) {
            routine.append(
                    ".POWER_ON\n" +
                    ".SET_BUFFER I %RAM_MSL \n"
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
                    + "\n; MSL = " + msl.getComputedMsl() + "\n"
                    + ".SET_DLKEY_KIC O\n"
                    + ".SET_DLKEY_KID Q\n"
                    + ".CHANGE_KIC M\n"
                    + ".CHANGE_KID N\n"
                    + spiConfigurator(msl, cipherKeyset, authKeyset)
            );
            if (authKeyset.getKidMode().equals("AES - CMAC"))
                routine.append(".SET_CMAC_LENGTH " + String.format("%02X", authKeyset.getCmacLength()) + "\n");

            int appletno = 1;
            for(AppletParam appletParam : root.getRunSettings().getAppletParams()) {
                routine.append(
                        ";APPLET " + appletno + " \n"
                );
                appletno++ ;
                System.out.println("Here Generate");
                routine.append(
                        ".CHANGE_COUNTER L\n" +
                        ".INCREASE_BUFFER L(04:05) 0001 \n" +
                        ".DEFINE %Package_Aid " + appletParam.getPackageAid() + "\n" +
                        ".SET_BUFFER J 80F2 2000 <?> 4F <%Package_Aid> %Package_Aid \n" +
                        ".APPEND_BUFFER J 00C00000 00 \n" +
                        ".APPEND_SCRIPT J\n" +
                        ".END_MESSAGE G J\n" +
                        "\n" +
                        "A0 C2 00 00 G J (9FXX)\n" +
                        ".CLEAR_SCRIPT \n" +
                        "\n" +
                        "A0 C0 00 00 W(2;1) [XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX <%Package_Aid> %Package_Aid 01 00]\n" +
                        "\n" +
                        "\n" +
                        ".CHANGE_COUNTER L\n" +
                        ".INCREASE_BUFFER L(04:05) 0001 \n" +
                        ".DEFINE %Instance_Aid " + appletParam.getInstanceAid() + "\n" +
                        ".SET_BUFFER J 80F2 4000 <?> 4F <%Instance_Aid> %Instance_Aid \n" +
                        ".APPEND_BUFFER J 00C00000 00 \n" +
                        ".APPEND_SCRIPT J\n" +
                        ".END_MESSAGE G J\n" +
                        "\n" +
                        "A0 C2 00 00 G J (9FXX)\n" +
                        ".CLEAR_SCRIPT \n" +
                        "\n" +
                        "A0 C0 00 00 W(2;1) [XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX <%Instance_Aid> %Instance_Aid " + appletParam.getLifeCycle() + " 00]\n" +
                        ".UNDEFINE %Package_Aid \n" +
                        ".UNDEFINE %Instance_Aid \n"
                );
            }
            routine.append(
                    ".EXPORT_BUFFER L COUNTER.bin \n"
            );
        } else {
            routine.append(
                    openChannel(isd)
            );
            if (!isd.isSecuredState() || isd.getMethodForGpCommand().equals("SIMBiOs")) {
                int appletno = 1;
                for(AppletParam appletParam : root.getRunSettings().getAppletParams()) {
                    routine.append(
                            ";APPLET " + appletno + " \n"
                    );
                    appletno++ ;
                    if(!appletParam.getPackageAid().equals("")) {
                        routine.append(
                                ".DEFINE %Package_Aid " + appletParam.getPackageAid() + "\n" +
                                "80F2 2000 <?> 4F <%Package_Aid> %Package_Aid \n" +
                                "00 C0 00 00 W(2;1) [<%Package_Aid> %Package_Aid 01 00] \n"
                        );
                    }
                    if(!appletParam.getInstanceAid().equals("")) {
                        routine.append(
                                ".DEFINE %Instance_Aid " + appletParam.getInstanceAid() + "\n" +
                                "80F2 4000 <?> 4F <%Instance_Aid> %Instance_Aid \n" +
                                "00 C0 00 00 W(2;1) [<%Instance_Aid> %Instance_Aid " + appletParam.getLifeCycle() + " 00]\n"

                        );
                    }
                    routine.append(
                            ".UNDEFINE %Package_Aid \n" +
                            ".UNDEFINE %Instance_Aid \n" +
                            "\n"
                    );
                }
            } else {
                for(AppletParam appletParam : root.getRunSettings().getAppletParams()) {
                    if(!appletParam.getPackageAid().equals("")) {
                        routine.append(
                                ".DEFINE %Package_Aid " + appletParam.getPackageAid() + "\n" +
                                ".SET_BUFFER N 80F2 2000 <?> 4F <%Package_Aid> %Package_Aid \n" +
                                scp0255(isd) +
                                "N [<%Package_Aid> %Package_Aid 01 00]\n"
                        );
                    }
                    if(!appletParam.getInstanceAid().equals("")) {
                        routine.append(
                            ".DEFINE %Instance_Aid " + appletParam.getInstanceAid() + "\n" +
                            ".SET_BUFFER N 80F2 4000 <?> 4F <%Instance_Aid> %Instance_Aid \n" +
                            scp0255(isd) +
                            "N  [<%Instance_Aid> %Instance_Aid " + appletParam.getLifeCycle() + " 00]\n"
                        );
                    }
                    routine.append(
                            ".UNDEFINE %Package_Aid \n" +
                            ".UNDEFINE %Instance_Aid \n" +
                            "\n"
                    );
                }
            }
        }
        return routine.toString();
    }

    private String scp0255(Isd isd) {
        StringBuilder routine = new StringBuilder();
        if (isd.getScLevel().equals("01") || isd.getScLevel().equals("03")) {
            routine.append(
                    "\t.SET_BUFFER L <N>\n" +
                    "\t.SWITCH L\n" +
                    "\t\t.CASE 04\n" +
                    "\t\t\t.APPEND_BUFFER N 00\n" +
                    "\t\t.DEFAULT\n" +
                    "\t\t.BREAK\n" +
                    "\t.ENDSWITCH\n" +
                    "\n" +
                    "\t; Update command class\n" +
                    "\t.SET_BUFFER L N(1;1)\n" +
                    "\t.INCREASE_BUFFER L 04\n" +
                    "\n" +
                    "\t.SET_BUFFER N(1;1) L\n" +
                    "\n" +
                    "\t; Update command length\n" +
                    "\t.SET_BUFFER L N(5;1)\n" +
                    "\t.INCREASE_BUFFER L 08\n" +
                    "\t.DISPLAY L\n" +
                    "\n" +
                    "\t.SET_BUFFER N(5;1) L\n" +
                    "\n" +
                    "\t; Data for MAC computation (Previous MAC || Command)\n" +
                    "\t.SET_DATA M N\n" +
                    "\t.SET_VECT_INI 0000000000000000\n" +
                    "\t; Use MAC Session Key\n" +
                    "\t.SET_KEY J\n" +
                    "\t; Compute MAC\n" +
                    "\t.MAC3 M 80 /P\n" +
                    "\n" +
                    "\t; Append MAC to command\n" +
                    "\t.APPEND_BUFFER N M\n"
            );
        }
        if (isd.getScLevel().equals("03")) {
            routine.append(
                    "\t; Retrieve the length of the data to encrypt\n" +
                    "\t.SET_BUFFER L <N>\n" +
                    "\t.DECREASE_BUFFER L 0D\n" +
                    "\n" +
                    "\t.SWITCH L\n" +
                    "\t\t.CASE 00\n" +
                    "\t\t\t* No data to encrypt\n" +
                    "\t\t.BREAK\n" +
                    "\n" +
                    "\t\t.DEFAULT\n" +
                    "\t\t\t; Retrieve data to be encrypted\n" +
                    "\t\t\t.SET_DATA N(6;L)\n" +
                    "\t\t\t.SET_VECT_INI 0000000000000000\n" +
                    "\t\t\t; Use Authentication/Encryption Session Key\n" +
                    "\t\t\t.SET_KEY I\n" +
                    "\t\t\t; Encrypt data and put result in L \n" +
                    "\t\t\t.DES3CBC L 80 /P\n" +
                    "\n" +
                    "\t\t\t; Put length of encrypted data in Lc\n" +
                    "\t\t\t.SET_BUFFER N(5;1) <L>\n" +
                    "\t\t\t; Add MAC length to Lc\n" +
                    "\t\t\t.INCREASE_BUFFER N(5;1) 08\n" +
                    "\n" +
                    "\t\t\t; construct command with encrypted data and computed MAC\n" +
                    "\t\t\t.SET_BUFFER N N(1;5) L M\n" +
                    "\t\t.BREAK\n" +
                    "\t.ENDSWITCH\n"
            );
        }
        return routine.toString();
    }

    private String ramCase1(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Isd isd, Boolean isUpdateRecord) {
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
        if(isUpdateRecord) {
            routine.append(
                    ".INIT_SMS_0348\n"
                    + ".CHANGE_FIRST_BYTE 44\n"
                    + ".CHANGE_SC_ADDRESS 07913366003000F0\n"
                    + ".CHANGE_TP_PID 41\n"
                    + ".CHANGE_POR_FORMAT 01\n"
            );
        } else {
            routine.append(
                    ".INIT_ENV_0348\n"
                    + ".CHANGE_TP_PID " + root.getRunSettings().getSmsUpdate().getTpPid() + "\n"
            );
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
                        + ";SMS 1: INSTALL FOR LOAD\n"
                        + ".CHANGE_COUNTER L\n"
                        + ".INCREASE_BUFFER L(04:05) 0001\n"
                        + ".SET_BUFFER J 80E602001F0CA0000000185302000000001000000EEF0CC6020000C8020000C702000000\n"
                        + ".APPEND_SCRIPT J\n"
                        + ".END_MESSAGE G J\n"
                        + sendBuffer(isUpdateRecord)
                        + ";SMS 2: LOAD PACKAGE\n"
                        + ".CHANGE_COUNTER L\n"
                        + ".INCREASE_BUFFER L(04:05) 0001\n"
                        + ".SET_BUFFER J 80E8000067C48201CC010016DECAFFED01020400020CA0000000185302000000001002001F0016001F0010001E0046001A00C6000A001E0000009400000000000002010004001E02000107A0000000620101010210A0000000090003FFFFFFFF8910710002030010010CA000\n"
                        + ".APPEND_SCRIPT J\n"
                        + ".END_MESSAGE G J\n"
                        + getNextMessage(msl)
                        + sendBuffer(isUpdateRecord)
                        + ";SMS 3: LOAD PACKAGE\n"
                        + ".CHANGE_COUNTER L\n"
                        + ".INCREASE_BUFFER L(04:05) 0001\n"
                        + ".SET_BUFFER J 80E800016700001853020000000110001F06001A43800300FF00070300000035003800A4800200810101088100000700C6020048803E008800060093800B00A000060210188C00008D0001058B00027A05318F00033D8C00042E1B181D0441181D258B00057A00207A03221D\n"
                        + ".APPEND_SCRIPT J\n"
                        + ".END_MESSAGE G J\n"
                        + getNextMessage(msl)
                        + sendBuffer(isUpdateRecord)
                        + ";SMS 4: LOAD PACKAGE\n"
                        + ".CHANGE_COUNTER L\n"
                        + ".INCREASE_BUFFER L(04:05) 0001\n"
                        + ".SET_BUFFER J 80E800026775006800020002000D001300588D00072E1B8B0008311B1E8B000910F06B4B1B1E04418B0009100C6B401B1E05418B000961371B1E06418B000910126B2C1B1E07418B00096123188B000A701D3B8D0001103C8B000B7012188B000A8D0001038B000B70053B70\n"
                        + ".APPEND_SCRIPT J\n"
                        + ".END_MESSAGE G J\n"
                        + getNextMessage(msl)
                        + sendBuffer(isUpdateRecord)
                        + ";SMS 5: LOAD PACKAGE\n"
                        + ".CHANGE_COUNTER L\n"
                        + ".INCREASE_BUFFER L(04:05) 0001\n"
                        + ".SET_BUFFER J 80E8000367027A041110178D000C601A8D000D2C19040310828B000E198B000F10206B06058D00107A08000A00000000000000000000050046001106800300068109000381090901000000060000110380030201810700068108000381080D03810204030000090381090C06\n"
                        + ".APPEND_SCRIPT J\n"
                        + ".END_MESSAGE G J\n"
                        + getNextMessage(msl)
                        + sendBuffer(isUpdateRecord)
                        + ";SMS 6: LOAD PACKAGE\n"
                        + ".CHANGE_COUNTER L\n"
                        + ".INCREASE_BUFFER L(04:05) 0001\n"
                        + ".SET_BUFFER J 80E880043481030006810A0003810A1503810A160681070009001E0000001A070806030406040C1705060B0B090B0606050603040D05090408\n"
                        + ".APPEND_SCRIPT J\n"
                        + ".END_MESSAGE G J\n"
                        + sendBuffer(isUpdateRecord)
                        + ".EXPORT_BUFFER L COUNTER.bin\n\n\n"
                        + ";CHECK LOADED PACKAGE\n"
                        + ".POWER_ON\n"
                        + proactiveInitialization()
        );
        if(isd.getMethodForGpCommand().equals("no Card Manager Keyset")) {
            routine.append(
                    ".POWER_ON\n" +
                            ".SET_BUFFER I %RAM_MSL \n"
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
                    ".SET_BUFFER J 80F2 2000 0E 4F 0C  A00000001853020000000010\n" +
                            ".APPEND_SCRIPT J\n" +
                            ".END_MESSAGE G J\n" +
                            "\n" +
                            "A0 C2 00 00 G J (9FXX)\n" +
                            ".CLEAR_SCRIPT \n" +
                            "\n" +
                            "A0 C0 00 00 W(2;1) [XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX0161XX]\n"
            );
        } else {
            routine.append(
                    openChannel(isd)
            );
            if (!isd.isSecuredState()) {
                routine.append(
                        "80F2 2000 0E 4F 0C  A00000001853020000000010 (61 0F)\n" +
                        "00C000000F [0C  A00000001853020000000010 0100]\n"
                );
            } else {
                routine.append(
                        ".SET_BUFFER N 80F2 2000 0E 4F 0C  A00000001853020000000010 (61 0F)\n" +
                        scp0255(isd) +
                        "N [0C  A00000001853020000000010 0100]\n"
                );
            }
        }

        routine.append(
                ".EXPORT_BUFFER L COUNTER.bin\n" +
                ";===========================================================\n" +
                ";Buffer L contains the anti replay counter for OTA message\n" +
                ";===========================================================\n" +
                ".SET_BUFFER L\n" +
                ".IMPORT_BUFFER L COUNTER.bin \n" +
                ".INCREASE_BUFFER L(04:05) 0001\n" +
                ".DISPLAY L\n" +
                ";==================================================================\n" +
                ";delete package\n" +
                ";==================================================================\n" +
                ".POWER_ON\n" +
                ".SET_BUFFER I %RAM_MSL \n"
                + proactiveInitialization()
                + "\n; SPI settings\n"
                + ".SET_BUFFER O %" + cipherKeyset.getKicValuation() + "\n"
                + ".SET_BUFFER Q %" + authKeyset.getKidValuation() + "\n"
                + ".SET_BUFFER M " + cipherKeyset.getComputedKic() + "\n"
                + ".SET_BUFFER N " + authKeyset.getComputedKid() + "\n"
        );
        if(isUpdateRecord) {
            routine.append(
                    ".INIT_SMS_0348\n"
                            + ".CHANGE_FIRST_BYTE 44\n"
                            + ".CHANGE_SC_ADDRESS 07913366003000F0\n"
                            + ".CHANGE_TP_PID 41\n"
                            + ".CHANGE_POR_FORMAT 01\n"
            );
        } else {
            routine.append(
                    ".INIT_ENV_0348\n"
                            + ".CHANGE_TP_PID " + root.getRunSettings().getSmsUpdate().getTpPid() + "\n"
            );
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
                ".SET_BUFFER J 80E400000E4F0CA00000001853020000000010\n" +
                ".APPEND_SCRIPT J\n" +
                ".END_MESSAGE G J\n" +
                "\n" +
                sendBuffer(isUpdateRecord)
        );
        return routine.toString();
    }

    private String ramCase2(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Isd isd, Boolean isUpdateRecord) {
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
        if(isUpdateRecord) {
            routine.append(
                    ".INIT_SMS_0348\n"
                            + ".CHANGE_FIRST_BYTE 44\n"
                            + ".CHANGE_SC_ADDRESS 07913366003000F0\n"
                            + ".CHANGE_TP_PID 41\n"
                            + ".CHANGE_POR_FORMAT 01\n"
            );
        } else {
            routine.append(
                    ".INIT_ENV_0348\n"
                            + ".CHANGE_TP_PID " + root.getRunSettings().getSmsUpdate().getTpPid() + "\n"
            );
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
                ";==================================================================\n" +
                ";send SMS\n" +
                ";==================================================================\n" +
                ";INSTALL FOR LOAD\n" +
                ".SET_BUFFER J 80E602002310A000000077010760110002000000007000000EEF0CC6020000C8020000C702000000\n" +
                ".APPEND_SCRIPT J\n" +
                "\n" +
                ";LOAD PACKAGE\n" +
                ".SET_BUFFER\tJ 80E80000FCC482052A01001ADECAFFED010204000110A000000077010760110002000000007002001F001A001F0014003B00960018032C0012009B0000000300020001000504010004003B04000107A0000000620101060210A0000000090003FFFFFFFF8910710002020210A0000000090003FFFFFFFF8910710001000107A00000006200010300140110A000000077010760110002010000007000310600184380030C00040702000002CD02D08101010881000080020007032C0601C9804F023500240219801A02350024026E804002C9002402AF801302C9002402C3800502C9002402ED803A0328002406338F00223D8C00112E1B181D0441181D258B001C\n" +
                ".APPEND_SCRIPT J\n" +
                "\n" +
                ";LOAD PACKAGE\n" +
                ".SET_BUFFER\tJ 80E80001F81B8D002087048D001F3D2804048B001E1504107F8B001E1B1032048D002187051B0388081B0388091B03880A1B03880B1B117FDE1111597B000C7B000C92038C00103B1B117FDE1111581B83051009048C0010602B1B1B8305048D001889001B1B8305068D001889021B1B8305088D001889011B1B830510078D001889031B1B83051B85001B85028C000F3D2905600D1B1605900B87061B0488081B1B83051B85011B85038C000F3D2905600D1B1605900B87071B0488097A0210188C001D03B70003B70103B70203B7037A061018117FDE111158AD0504048C00106072AD050325103D6A6AAE086032AE0A612E18AF00AF02AD06AD0692\n" +
                ".APPEND_SCRIPT J\n" +
                "\n" +
                ";LOAD PACKAGE\n" +
                ".SET_BUFFER\tJ 80E80002F8048C00103BAD0503AD0692038D001B3B18AF00AF02AD05AD0692038C00103B04B60AAE096032AE0B612E18AF01AF03AD07AD0792048C00103BAD0503AD0792038D001B3B18AF01AF03AD05AD0792038C00103B04B60B7A061218117FDE111158AD0504048C00109800C6AD050325103D6A09101E8D001261037A18117FDE111159AD0508048C001061037A8D00172C1910260410828B0015198B00163B8D00142D1A1014AD05088B00133BAD05083E25100F5538AD0510093E2510F05538AD0503AD0508088D001A602DAD05037B000C037B000C928D001A60037AAD0508AD0503088D00193B18117FDE111159AD0508038C00103B70042C\n" +
                ".APPEND_SCRIPT J\n" +
                "\n" +
                ";LOAD PACKAGE\n" +
                ".SET_BUFFER\tJ 80E80003F87AAE086018AE0A601418AF00AF02AD06AD0692038C00103B03B60AAE096018AE0B601418AF01AF03AD07AD0792038C00103B03B60B7A0542AD04113F008E02002307AD041E8E02002307AD041F190310328E05002306290419032510626B30052905160516046D251916052510806B0B19160505418D0018781605191605044125054141290559050170D9037819058D0018782804037800207A01201D750017000200010013007F000D188C000D7006188C000E7A0561AD04113F008E02002307AD041D8E02002307AD041E8E0200230716056011AD04031B0316048E050023093B700EAD04031B0316048E0500230A0478280603780800\n" +
                ".APPEND_SCRIPT J\n" +
                "\n" +
                ";LOAD PACKAGE\n" +
                ".SET_BUFFER\tJ 80E80004F812000200010001030005FFFFFFFFFF000000000500960025020000040200000502000006020000070200000002000001020000020200000302000008020000090200000A0200000B0500000006000112060001950600026C060002EB060000FF06810300038105050681050003810A1503810A1606810A000680100406801001068010000680100303800302068003000381090906810900068201000680080D01000000018200000183020009009B005B4B1A040404041D0C060406040604070403030F040403030F040A0303030C0908040502020208030902020209020405020202080309020202090C0A172307090A030911030F0C04\n" +
                ".APPEND_SCRIPT J\n" +
                "\n" +
                ";LOAD PACKAGE\n" +
                ".SET_BUFFER\tJ 80E8800552050202020902040502020209050A086D0A080C0F003C07080808080805040C04050707071C0305110A0A0A0B0F1D171D200B0F1A0B0F151011060A04040A1E0804040D0F1B1A10080C231A1C060C0808100F\n" +
                ".APPEND_SCRIPT J\n" +
                "\n" +
                ".END_MESSAGE G J\n" +
                "\n"
        );
        if(isUpdateRecord) {
            routine.append(
                    "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n" +
                    "A0 A4 00 00 02 7F10 (9F22) ;select DF Telecom\n" +
                    "A0 A4 00 00 02 6F3C (9F0F) ;select EF SMS\n" +
                    "A0 DC 01 04 G J (90XX) ;update EF SMS\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 DC 01 04 G J (90XX) ;update EF SMS\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 DC 01 04 G J (90XX) ;update EF SMS\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 DC 01 04 G J (90XX) ;update EF SMS\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 DC 01 04 G J (90XX) ;update EF SMS\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 DC 01 04 G J (90XX) ;update EF SMS\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 DC 01 04 G J (90XX) ;update EF SMS\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 DC 01 04 G J (90XX) ;update EF SMS\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 DC 01 04 G J (90XX) ;update EF SMS\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 DC 01 04 G J (90XX) ;update EF SMS\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 DC 01 04 G J (91XX) ;update EF SMS" +
                    ".CLEAR_SCRIPT \n" +
                    "\n" +
                    "A0 B2 01 04 B0\n" +
                    "\n" +
                    "A0 12 0000 W(2;1) [XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX 07 61XX] (9000)\n" +
                    "A0 14 0000 0C 8103011300 82028183 830100 (9000)\n" +
                    "\n" +
                    ".EXPORT_BUFFER L COUNTER.bin\n"
            );
        } else {
            routine.append(
                    ";SMS 1\n" +
                    "A0 C2 00 00 G J (90XX)\n" +
                    "\n" +
                    ";SMS 2\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 C2 00 00 G J (90XX)\n" +
                    "\n" +
                    ";SMS 3\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 C2 00 00 G J (90XX)\n" +
                    "\n" +
                    ";SMS 4\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 C2 00 00 G J (90XX)\n" +
                    "\n" +
                    "; SMS 5\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 C2 00 00 G J (90XX)\n" +
                    "\n" +
                    ";SMS 6\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 C2 00 00 G J (90XX)\n" +
                    "\n" +
                    ";SMS 7\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 C2 00 00 G J (90XX)\n" +
                    "\n" +
                    ";SMS 8\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 C2 00 00 G J (90XX)\n" +
                    "\n" +
                    ";SMS 9\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 C2 00 00 G J (90XX)\n" +
                    "\n" +
                    ";SMS 10\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 C2 00 00 G J (90XX)\n" +
                    "\n" +
                    ";SMS 11\n" +
                    ".GET_NEXT_MESSAGE G J\n" +
                    "A0 C2 00 00 G J (9FXX)\n" +
                    "\n" +
                    ".CLEAR_SCRIPT \n" +
                    "\n" +
                    "A0 C0 00 00 W(2;1) [XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX0761XX]\n" +
                    "\n" +
                    ".EXPORT_BUFFER L COUNTER.bin \n"
            );
        }
        routine.append(
                ";==================================================================\n" +
                ";check loaded package\n" +
                ";==================================================================\n" +
                ".POWER_ON\n" +
                proactiveInitialization()
        );
        if(isd.getMethodForGpCommand().equals("no Card Manager Keyset")) {
            routine.append(
            ".POWER_ON\n" +
                    ".SET_BUFFER I %RAM_MSL \n"
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
                    ".SET_BUFFER J 80F22000124F10A0000000770107601100020000000070\n" +
                            ".APPEND_SCRIPT J\n" +
                            ".END_MESSAGE G J\n" +
                            "\n" +
                            "A0 C2 00 00 G J (9FXX)\n" +
                            ".CLEAR_SCRIPT \n" +
                            "\n" +
                            "A0 C0 00 00 W(2;1) [XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX0161XX]\n"
            );
        } else {
            routine.append(
                    openChannel(isd)
            );
            if (!isd.isSecuredState()) {
                routine.append(
                        "80F22000124F10A0000000770107601100020000000070 (61 13)\n" +
                        "00C0000013 [10  A0000000770107601100020000000070 0100]\n"
                );
            } else {
                routine.append(
                        ".SET_BUFFER N 80F22000124F10A0000000770107601100020000000070 (61 13)\n" +
                        scp0255(isd) +
                        "N [10  A0000000770107601100020000000070 0100]\n"
                );
            }
        }

        routine.append(
                        ".EXPORT_BUFFER L COUNTER.bin\n" +
                        ";===========================================================\n" +
                        ";Buffer L contains the anti replay counter for OTA message\n" +
                        ";===========================================================\n" +
                        ".SET_BUFFER L\n" +
                        ".IMPORT_BUFFER L COUNTER.bin \n" +
                        ".INCREASE_BUFFER L(04:05) 0001\n" +
                        ".DISPLAY L\n" +
                        ";==================================================================\n" +
                        ";delete package\n" +
                        ";==================================================================\n" +
                        "\n.POWER_ON\n"
                        + proactiveInitialization()
                        + "\n; SPI settings\n"
                        + ".SET_BUFFER O %" + cipherKeyset.getKicValuation() + "\n"
                        + ".SET_BUFFER Q %" + authKeyset.getKidValuation() + "\n"
                        + ".SET_BUFFER M " + cipherKeyset.getComputedKic() + "\n"
                        + ".SET_BUFFER N " + authKeyset.getComputedKid() + "\n"
        );
        if(isUpdateRecord) {
            routine.append(
                    ".INIT_SMS_0348\n"
                            + ".CHANGE_FIRST_BYTE 44\n"
                            + ".CHANGE_SC_ADDRESS 07913366003000F0\n"
                            + ".CHANGE_TP_PID 41\n"
                            + ".CHANGE_POR_FORMAT 01\n"
            );
        } else {
            routine.append(
                    ".INIT_ENV_0348\n"
                            + ".CHANGE_TP_PID " + root.getRunSettings().getSmsUpdate().getTpPid() + "\n"
            );
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
                ".SET_BUFFER J 80E40000124F10A0000000770107601100020000000070\n" +
                        ".APPEND_SCRIPT J\n" +
                        ".END_MESSAGE G J\n" +
                        "\n" +
                        sendBuffer(isUpdateRecord)
        );
        return routine.toString();
    }

    private String ramCase3(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Boolean isUpdateRecord) {
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
        if(isUpdateRecord) {
            routine.append(
                    ".INIT_SMS_0348\n"
                            + ".CHANGE_FIRST_BYTE 44\n"
                            + ".CHANGE_SC_ADDRESS 07913366003000F0\n"
                            + ".CHANGE_TP_PID 41\n"
                            + ".CHANGE_POR_FORMAT 01\n"
            );
        } else {
            routine.append(
                    ".INIT_ENV_0348\n"
                            + ".CHANGE_TP_PID " + root.getRunSettings().getSmsUpdate().getTpPid() + "\n"
            );
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
                        + ".SET_BUFFER J 80E602001F0CA0000000185302000000001000000EEF0CC6020000C8020000C702000000\n"
                        + ".APPEND_SCRIPT J\n"
                        + ".END_MESSAGE G J\n"
        );
        if(isUpdateRecord) {
            routine.append(
                    "\n" +
                    "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n" +
                    "A0 A4 00 00 02 7F10 (9F22) ;select DF Telecom\n" +
                    "A0 A4 00 00 02 6F3C (9F0F) ;select EF SMS\n" +
                    "A0 DC 01 04 G J (91XX) ;update EF SMS\n" +
                    ".CLEAR_SCRIPT\n" +
                    "\n" +
                    "A0 B2 01 04 B0 \n" +
                    "\n" +
                    "A0 12 0000 W(2;1) [XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX06] (9000)\n" +
                    "A0 14 0000 0C 8103011300 82028183 830100 (9000)"
            );
        } else {
            routine.append(
                    "; send envelope\n"
                    + "A0 C2 00 00 G J (9XXX)\n"
                    + ".CLEAR_SCRIPT\n"
                    + "; check PoR\n"
                    + "A0 C0 00 00 W(2;1) [XX XX XX XX XX XX %TAR XX XX XX XX XX XX 06] (9000) ; unidentified security error\n"
                    + "\n; increment counter by one\n"
                    + ".INCREASE_BUFFER L(04:05) 0001\n"
            );
        }
        return routine.toString();
    }

    private String ramCase4(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Boolean isUpdateRecord) {
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
        if(isUpdateRecord) {
            routine.append(
                    ".INIT_SMS_0348\n"
                            + ".CHANGE_FIRST_BYTE 44\n"
                            + ".CHANGE_SC_ADDRESS 07913366003000F0\n"
                            + ".CHANGE_TP_PID 41\n"
                            + ".CHANGE_POR_FORMAT 01\n"
            );
        } else {
            routine.append(
                    ".INIT_ENV_0348\n"
                            + ".CHANGE_TP_PID " + root.getRunSettings().getSmsUpdate().getTpPid() + "\n"
            );
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
                        + ".SET_BUFFER J 80E602001F0CA0000000185302000000001000000EEF0CC6020000C8020000C702000000\n"
                        + ".APPEND_SCRIPT J\n"
                        + ".END_MESSAGE G J\n"
        );
        if(isUpdateRecord) {
            routine.append(
                    "\n" +
                            "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n" +
                            "A0 A4 00 00 02 7F10 (9F22) ;select DF Telecom\n" +
                            "A0 A4 00 00 02 6F3C (9F0F) ;select EF SMS\n" +
                            "A0 DC 01 04 G J (91XX) ;update EF SMS\n" +
                            ".CLEAR_SCRIPT\n" +
                            "\n" +
                            "A0 B2 01 04 B0 \n" +
                            "\n" +
                            "A0 12 0000 W(2;1) [XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX02] (9000)\n" +
                            "A0 14 0000 0C 8103011300 82028183 830100 (9000)"
            );
        } else {
            routine.append(
                    "; send envelope\n"
                            + "A0 C2 00 00 G J (9XXX)\n"
                            + ".CLEAR_SCRIPT\n"
                            + "; check PoR\n"
                            + "A0 C0 00 00 W(2;1) [XX XX XX XX XX XX %TAR XX XX XX XX XX XX 02] (9000) ; unidentified security error\n"
                            + "\n; increment counter by one\n"
                            + ".INCREASE_BUFFER L(04:05) 0001\n"
            );
        }
        return routine.toString();
    }

    private String ramCase5(SCP80Keyset cipherKeyset, SCP80Keyset authKeyset, MinimumSecurityLevel msl, Boolean isUpdateRecord) {
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
        if(isUpdateRecord) {
            routine.append(
                    ".INIT_SMS_0348\n"
                            + ".CHANGE_FIRST_BYTE 44\n"
                            + ".CHANGE_SC_ADDRESS 07913366003000F0\n"
                            + ".CHANGE_TP_PID 41\n"
                            + ".CHANGE_POR_FORMAT 01\n"
            );
        } else {
            routine.append(
                    ".INIT_ENV_0348\n"
                            + ".CHANGE_TP_PID " + root.getRunSettings().getSmsUpdate().getTpPid() + "\n"
            );
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
                        + ".SET_BUFFER J 80E602001F0CA0000000185302000000001000000EEF0CC6020000C8020000C702000000\n"
                        + ".APPEND_SCRIPT J\n"
                        + ".END_MESSAGE G J\n"
        );
        if(isUpdateRecord) {
            routine.append(
                    "\n" +
                            "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n" +
                            "A0 A4 00 00 02 7F10 (9F22) ;select DF Telecom\n" +
                            "A0 A4 00 00 02 6F3C (9F0F) ;select EF SMS\n" +
                            "A0 DC 01 04 G J (91XX) ;update EF SMS\n" +
                            ".CLEAR_SCRIPT\n" +
                            "\n" +
                            "A0 B2 01 04 B0 \n" +
                            "\n" +
                            "A0 12 0000 W(2;1) [XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX0A] (9000)\n" +
                            "A0 14 0000 0C 8103011300 82028183 830100 (9000)"
            );
        } else {
            routine.append(
                    "; send envelope\n"
                            + "A0 C2 00 00 G J (9XXX)\n"
                            + ".CLEAR_SCRIPT\n"
                            + "; check PoR\n"
                            + "A0 C0 00 00 W(2;1) [XX XX XX XX XX XX %TAR XX XX XX XX XX XX 0A] (9000) ; unidentified security error\n"
                            + "\n; increment counter by one\n"
                            + ".INCREASE_BUFFER L(04:05) 0001\n"
            );
        }
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

    private String getNextMessage(MinimumSecurityLevel msl) {
        StringBuilder routine = new StringBuilder();
        String auth_verif = msl.getAuthVerification();
        if(auth_verif.equals("Cryptographic Checksum") && msl.isUseCipher()) {
            routine.append(
                    "A0 C2 00 00 G J (9000)\n"
                        + ".GET_NEXT_MESSAGE G J \n"
            );
        }
        return routine.toString();
    }

    private String openChannel(Isd isd) {
        StringBuilder routine = new StringBuilder();
        if (isd.getMethodForGpCommand().equals("with Card Manager Keyset")) {
            if (!isd.isSecuredState()) {
                routine.append(
                        ".ASCIITOHEX I OT.TEST.GT1000.3GKEY16.01\n"
                                + ".DEFINE %MKEY_LABEL I\n"
                                + ".DEFINE %SCP81_KEY_MASTER 1D8A0C5CB0EBBA42E7AA1E83B0F19217\n"
                                + ".DEFINE %PSK_DEK 11 22 33 44 55 66 77 88 99 00 AA BB CC DD EE FF\n"
                                + ".DEFINE %PSK_DEK_KEY_TYPE 80\n"
                                + ".DEFINE %LEN_CARD_MANAGER_AID 08\n"
                                + ".DEFINE %AID_CARD_MANAGER " + root.getRunSettings().getCardParameters().getCardManagerAid() + "\n"
                                + ".DEFINE %CST_DERIVATION_ENC 0182\n"
                                + ".DEFINE %CST_DERIVATION_RMAC 0102\n"
                                + ".DEFINE %CST_DERIVATION_CMAC 0101\n"
                                + ".DEFINE %CST_DERIVATION_DEK 0181\n" +
                                ".DEFINE %ENC_SECRET_KEY %" + isd.getCardManagerEnc() + "\n" +
                                "\n" +
                                ".DEFINE %MAC_SECRET_KEY %" + isd.getCardManagerMac() + "\n" +
                                "\n" +
                                ".DEFINE %KEY_ENCRYPT_KEY %" + isd.getCardManagerKey() + "\n" +
                                ".DEFINE %OK 9000\n" +
                                ".DEFINE %PUT_KEY 80 D8 #1c #1c #1c #Nc ;(key version; mode and key index; Length, data)\n" +
                                ".DEFINE %GET_RESPONSE_61 61\n" +
                                ".DEFINE %GET_response 00 C0 00 00 #1c \n" +
                                "\t.POWER_ON\n" +
                                "00 A4 04 00 <?> " + root.getRunSettings().getCardParameters().getCardManagerAid() + "\n" +
                                "\t.SET_BUFFER I %ENC_SECRET_KEY\n" +
                                "\t.SET_BUFFER J %MAC_SECRET_KEY\n" +
                                "\t.SET_BUFFER K %KEY_ENCRYPT_KEY\n" +
                                "\t.SET_BUFFER M 00 00 00 ;Open in clear mode\n" +
                                "\t\n" +
                                "\t.IFNDEF T1_PROTOCOL\n" +
                                "\t\t.AUTOMATIC_PROTOCOL_OFF\n" +
                                "\t.ENDIF\n" +
                                "\t.IFNDEF FIRST_OPEN_CHANNEL;\n" +
                                "\t\t; first initialization of variables\n" +
                                "\t\t.APPEND_IFDEF FIRST_OPEN_CHANNEL;\n" +
                                "\t.ELSE\n" +
                                "\t\t; these variables are presistent, and may be used by compute_command.cmd script\n" +
                                "\t\t.UNDEFINE %SECURITY_LEVEL \n" +
                                "\t\t.UNDEFINE %SESSION_KIC\n" +
                                "\t\t.UNDEFINE %SESSION_CMAC\n" +
                                "\t\t.UNDEFINE %SESSION_RMAC\n" +
                                "\t\t.UNDEFINE %SESSION_KIK\n" +
                                "\t\t.UNDEFINE %LAST_COMPUTED_MAC\n" +
                                "\t\t.UNDEFINE %SEQUENCE_COUNTER\n" +
                                "\t\t\n" +
                                "\t.ENDIF\n" +
                                "\t\n" +
                                "\t; set input variables\n" +
                                "\t.DEFINE %KIC_ I\n" +
                                "\t.DEFINE %KID_ J\n" +
                                "\t.DEFINE %KIK_ K\n" +
                                "\t.DEFINE %KEY_VERSION M(2;1)\n" +
                                "\t.DEFINE %SECURITY_LEVEL M(1;1)\n" +
                                "\t.DEFINE %CHANNEL_NB M(3;1)" +

                                "\t*----------------------------------------------\n" +
                                "\t*2 step 1 : prepare INITIALIZE_UPDATE  command\n" +
                                "\t*----------------------------------------------  \n" +
                                "\t.ASCIITOHEX J HostRand     * host challenge\n" +
                                "\t.DEFINE %HOST_CHALLENGE J\n" +
                                "\t\n" +
                                "\t.DEFINE %SAV_I_ I\n" +
                                "\t.DEFINE %SAV_J_ J\n" +
                                "\t\n" +
                                "\t; compute channel number\n" +
                                "\t.SET_BUFFER I %CHANNEL_NB\n" +
                                "\t.INCREASE_BUFFER I 80\n" +
                                "\t\n" +
                                "\t;* prepare Initialize Update command\n" +
                                "\t.DEFINE %INIT_UPDATE_CMD I \\; channel number\n" +
                                "\t\t\t\t\t\t\t50 \\; INS\n" +
                                "\t\t\t\t\t\t\t%KEY_VERSION \\; key version \n" +
                                "\t\t\t\t\t\t\t00 08 \\; P2 = 00 LE = 08 (mandatory)\n" +
                                "\t\t\t\t\t\t\t%HOST_CHALLENGE ; host challenge\n" +
                                "\t\t\t\n" +
                                "\t.SET_BUFFER I %SAV_I_\n" +
                                "\t.SET_BUFFER J %SAV_J_\t\n" +
                                "\t\t\t\n" +
                                "\t.UNDEFINE %SAV_I_\n" +
                                "\t.UNDEFINE %SAV_J_\n" +
                                "\t\t\n" +
                                "\t\n" +
                                "\t%INIT_UPDATE_CMD 1C (%OK)\n" +
                                "\t\n" +
                                "\t.DEFINE %SEQUENCE_COUNTER R(13;2)     \t;Sequence Counter ;\n" +
                                "\t; don't check card cryptogram\n" +
                                "\t.DEFINE %CARD_CRYPTOGRAM  R(21;8)   \t\t; card cryptogramme\n" +
                                "\t.DEFINE %CARD_CHALLENGE   R(15;6)\t\t\t; card CHALLENGE\n" +

                                "\t*----------------------------------------------\n" +
                                "\t*2 step 2 : precompute sessions keys\n" +
                                "\t*----------------------------------------------  \n" +
                                "\t.DEFINE %SAV_K__ K\n" +
                                "\t.DEFINE %SAV_I__ I\n" +
                                "\t" +
                                "*Buffer K contains a authentication/encryption  derivation data\n" +
                                "\t\t.SET_BUFFER K %CST_DERIVATION_ENC %SEQUENCE_COUNTER 00000000 00000000 00000000\n" +
                                "\t\t.SET_DATA K\n" +
                                "\t\t.SET_VECT_INI 00000000 00000000\n" +
                                "\t\t;* static Authentication/Encryption Key\n" +
                                "\t\t.SET_KEY %KIC_\n" +
                                "\t\t;* session Authentication/Encryption Key\n" +
                                "\t\t.DES3CBC I 00\n" +
                                "\t\t.DEFINE %SESSION_KIC I\n" +
                                "\t\t.DISPLAY %SESSION_KIC\n" +
                                "\t\n" +
                                "\t;* compute C_Mac session key\n" +
                                "\t*Buffer K contains a CMAC  derivation data\n" +
                                "\t\t.SET_BUFFER  K %CST_DERIVATION_CMAC %SEQUENCE_COUNTER 00000000 00000000 00000000\n" +
                                "\t\t.SET_DATA K\n" +
                                "\t\t.SET_VECT_INI 00000000 00000000\n" +
                                "\t\t;* static CMac Key\n" +
                                "\t\t.SET_KEY %KID_\n" +
                                "\t\t;* C_Mac session\n" +
                                "\t\t.DES3CBC I 00\n" +
                                "\t\t.DEFINE %SESSION_CMAC I\n" +
                                "\t\t.DISPLAY %SESSION_CMAC\n" +
                                "\t\n" +
                                "\t;* compute R_Mac session key\n" +
                                "\t*Buffer K contains a RMAC  derivation data\n" +
                                "\t\t.SET_BUFFER  K %CST_DERIVATION_RMAC %SEQUENCE_COUNTER 00000000 00000000 00000000\n" +
                                "\t\t.SET_DATA K\n" +
                                "\t\t.SET_VECT_INI 00000000 00000000\n" +
                                "\t\t;* static static Key Encryption Key\n" +
                                "\t\n" +
                                "\t\t.SET_KEY %KID_ ; SAVE_STATIC_MAC_KEY\n" +
                                "\t\t;* session RMac session Key\n" +
                                "\t\t.DES3CBC I 00\n" +
                                "\t\t.DEFINE %SESSION_RMAC I\n" +
                                "\t\t.DISPLAY %SESSION_RMAC\n" +
                                "\t\n" +
                                "\t;* compute DEK session key\n" +
                                "\t*Buffer K contains a DEK  derivation data\n" +
                                "\t\t.SET_BUFFER  K %CST_DERIVATION_DEK %SEQUENCE_COUNTER 00000000 00000000 00000000\n" +
                                "\t\t.SET_DATA K\n" +
                                "\t\t.SET_VECT_INI 00000000 00000000\n" +
                                "\t\t;* static DEK Key\n" +
                                "\t\t.SET_KEY %KIK_\n" +
                                "\t\t;* session DEK Key\n" +
                                "\t\t.DES3CBC I 00\n" +
                                "\t\t.DEFINE %SESSION_KIK I\n" +
                                "\t\t.DISPLAY %SESSION_KIK\n" +
                                "\t\t\t\n" +
                                "\t\t\t\n" +
                                "\t.SET_BUFFER K %SAV_K__\n" +
                                "\t.SET_BUFFER I %SAV_I__\n" +
                                "\t\n" +
                                "\t.UNDEFINE %SAV_K__\n" +
                                "\t.UNDEFINE %SAV_I__\n" +
                                "\t\n" +


                                "\t*----------------------------------------------\n" +
                                "\t*2 step 3 : precompute host cryptogram\n" +
                                "\t*----------------------------------------------  \n" +
                                "\t.SET_BUFFER G %SEQUENCE_COUNTER %CARD_CHALLENGE %HOST_CHALLENGE\n" +
                                "\t\t\t.SET_DATA G\n" +
                                "\t\t\t.SET_VECT_INI 00000000 00000000\n" +
                                "\t\t\t.SET_KEY %SESSION_KIC\n" +
                                "\t\t\t.MAC33 N 80 /P\n" +
                                "\t\t\t.DEFINE %HOST_CRYPTOGRAM N\n" +
                                "\t\t\t.DISPLAY N\n" +
                                "\t\t\t\n" +
                                "\t\n" +


                                "\t*----------------------------------------------\n" +
                                "\t*2 step 4 : compute command mac\n" +
                                "\t*---------------------------------------------- \n" +
                                "\t;* compute command MAC\n" +
                                "\t.SET_BUFFER G 84 82 %SECURITY_LEVEL 00 10 %HOST_CRYPTOGRAM\n" +
                                "\t.SET_DATA G\n" +
                                "\t.SET_VECT_INI 00000000 00000000 ; ICV set to 00\n" +
                                "\t\n" +
                                "\t* session CMac Key\n" +
                                "\t.SET_KEY %SESSION_CMAC\n" +
                                "\t.MAC3 I 80 /P\n" +
                                "\t.DEFINE %LAST_COMPUTED_MAC I\n" +
                                "\t.DISPLAY I\n" +
                                "\t\n" +
                                "\t;* External Authenticate\n" +
                                "\t.DEFINE %EXTERNAL_AUTH_CMD 84 82 %SECURITY_LEVEL 00 10 %HOST_CRYPTOGRAM %LAST_COMPUTED_MAC (%OK)\n" +
                                "\t\n" +
                                "\t\n" +
                                "\t%EXTERNAL_AUTH_CMD (%OK)\n" +
                                "\t\n" +
                                "\t;set output variables and buffers\n" +
                                "\t.SET_BUFFER I %SESSION_KIC\n" +
                                "\t.SET_BUFFER J %SESSION_CMAC\n" +
                                "\t.SET_BUFFER K %SESSION_KIK\n" +
                                "\t.SET_BUFFER M %LAST_COMPUTED_MAC\n" +
                                "\t\n" +
                                "\t.UNDEFINE %KIC_ \n" +
                                "\t.UNDEFINE %KID_ \n" +
                                "\t.UNDEFINE %KIK_\n" +
                                "\t.UNDEFINE %KEY_VERSION \n" +
                                "\t\n" +
                                "\t.UNDEFINE %CHANNEL_NB \n" +
                                "\t\n" +
                                "\t.UNDEFINE %INIT_UPDATE_CMD\n" +
                                "\t.UNDEFINE %EXTERNAL_AUTH_CMD\n" +
                                "\t\n" +
                                "\t.UNDEFINE %HOST_CHALLENGE\n" +
                                "\t\n" +
                                "\t.UNDEFINE %CARD_CRYPTOGRAM\n" +
                                "\t.UNDEFINE %CARD_CHALLENGE\n" +
                                "\t.UNDEFINE %HOST_CRYPTOGRAM\n" +
                                "\t\n" +
                                "\t.IFNDEF T1_PROTOCOL\n" +
                                "\t\t.AUTOMATIC_PROTOCOL_ON\n" +
                                "\t.ENDIF\n" +
                                "\n" +
                                "\t;-----------------------------------------------\n" +
                                "\t; RESET PREDEFINED VARIABLE\n" +
                                "\t;-----------------------------------------------\n" +
                                "\t\n" +
                                "\t.UNDEFINE %MKEY_LABEL \n" +
                                "\t\n" +
                                "\t.UNDEFINE %SCP81_KEY \n" +
                                "\t\n" +
                                "\t.UNDEFINE %PSK_DEK\t\n" +
                                "\t\n" +
                                "\t.UNDEFINE %PSK_DEK_KEY_TYPE\t\t\t\n" +
                                "\t\n" +
                                "\t.UNDEFINE %LEN_CARD_MANAGER_AID         \n" +
                                "\t\n" +
                                "\t.UNDEFINE %AID_CARD_MANAGER\t\t\t\t\n" +
                                "\t\n" +
                                "\t.UNDEFINE %AID_CARD_MANAGER_WITH_LENGTH \n" +
                                "\t\t\t\t\t\t\t\t\t\t\n" +
                                "\t.UNDEFINE %SELECT_AID_CARD_MANAGER\t    \n" +
                                "\t                  \n" +
                                "\t.UNDEFINE %CST_DERIVATION_ENC           \n" +
                                "\t.UNDEFINE %CST_DERIVATION_RMAC          \n" +
                                "\t.UNDEFINE %CST_DERIVATION_CMAC          \n" +
                                "\t.UNDEFINE %CST_DERIVATION_DEK           \n" +
                                "\n" +
                                "\t;.UNDEFINE %ISD_ENC_SECRET_KEY\t \n" +
                                "\t;.UNDEFINE %ISD_MAC_SECRET_KEY\t \n" +
                                "\t;.UNDEFINE %ISD_KEY_ENCRYPT_KEY \t\n" +
                                "\t\n" +
                                "\t.UNDEFINE %ENC_SECRET_KEY\t\t\t\n" +
                                "\t\n" +
                                "\t.UNDEFINE %MAC_SECRET_KEY\t\t\t\n" +
                                "\t\n" +
                                "\t.UNDEFINE %KEY_ENCRYPT_KEY\t\t\n" +
                                "\t\n" +
                                "\t.UNDEFINE %OK \n" +
                                "\t\n" +
                                "\t.UNDEFINE %ICCID_ASCII \n" +
                                "\t\n" +
                                "\t.UNDEFINE %MKEY_LABEL_HASH \n" +
                                "\t\n" +
                                "\t.UNDEFINE %MKEY_LABEL_HASH_ASCII \n" +
                                "\t\n" +
                                "\t.UNDEFINE %SCP81_KEY_ID  \n" +
                                "\t\n" +
                                "\t.UNDEFINE %PUT_KEY  \n" +
                                "\t.UNDEFINE %GET_RESPONSE_61 \n" +
                                "\t.UNDEFINE %GET_response \n" +
                                "\t\n" +
                                "\t.UNDEFINE %SCP81_KEY_MASTER\n" +
                                "\n"
                );
            } else {
                routine.append(
                        ".IFNDEF QUALIFICATION\n" +
                                "\t\t.DEFINE %ISD_ENC %" + isd.getCardManagerEnc() + "\n"+
                                "\t\t.DEFINE %ISD_DEK %" + isd.getCardManagerKey() + "\n" +
                                "\t\t.DEFINE %ISD_MAC %" + isd.getCardManagerMac() + "\n" +
                                "\t.ENDIF\n" +
                                "\n" +
                                "\t.PROTOCOL_LEVEL_APDU\n" +
                                "\n" +
                                "\t.POWER_ON\n" +
                                "\n" +
                                "\t.SET_BUFFER G %CARD_MANAGER_AID\n" +
                                "\t.SET_BUFFER I %ISD_ENC\n" +
                                "\t.SET_BUFFER J %ISD_MAC\n" +
                                "\t.SET_BUFFER K %ISD_DEK\n" +
                                "\n" +
                                "\t.DEFINE %SC_LEVEL " + isd.getScLevel() + "\n" +
                                "\n" +
                                "\t; Select SD\n" +
                                "\t00 A4 04 0C  <?> G (9000)\n" +
                                "\n" +
                                "*----------------------------------------------\n" +
                                "* Initialize Update\n" +
                                "*----------------------------------------------\n" +
                                "\n" +
                                ".ASCIITOHEX H HostRand\t* Host Challenge\n" +
                                "\n" +
                                "* Send the INITIALIZE UPDATE command\n" +
                                ".SET_BUFFER N 80 50 0000 <?> H\n" +
                                "\n" +
                                ".IFDEF SIMULATOR\n" +
                                "\t* Display the command\n" +
                                "\t.DISPLAY N\n" +
                                ".ELSE\n" +
                                "\t.IFDEF OTA\n" +
                                "\t\t* Apprend the command to the OTA script\n" +
                                "\t\t.APPEND_BUFFER Q N\n" +
                                "\t.ELSE\n" +
                                "\t\t* Sends the command to the card\n" +
                                "\t\tN (9000)\n" +
                                "\t.ENDIF\n" +
                                ".ENDIF \n" +
                                "\n" +
                                ".IFDEF (SIMULATOR || OTA)\n" +
                                "    .SET_BUFFER O %COUNTER\n" +
                                ".ELSE\n" +
                                "    .SET_BUFFER O R(13;8)\t* SEQ Counter + Card Challenge\n" +
                                "\t.DISPLAY O\n" +
                                ".ENDIF\n" +
                                "\n" +
                                "*----------------------------------------------\n" +
                                "* External Authenticate\n" +
                                "*----------------------------------------------\n" +
                                "\n" +
                                "* Compute authentication/encryption session key\n" +
                                ".SET_DATA 0182 O(1;2) 000000000000000000000000\n" +
                                ".SET_VECT_INI 0000000000000000\n" +
                                ".SET_KEY I\n" +
                                ".DES3CBC I 00\n" +
                                ".DISPLAY I\n" +
                                "\n" +
                                "* Compute C-MAC session key\n" +
                                ".SET_DATA 0101 O(1;2) 000000000000000000000000\n" +
                                ".SET_VECT_INI 0000000000000000\n" +
                                ".SET_KEY J\n" +
                                ".DES3CBC J 00\n" +
                                ".DISPLAY J\n" +
                                "\n" +
                                "* Compute data encryption session key\n" +
                                ".SET_DATA 0181 O(1;2) 000000000000000000000000\n" +
                                ".SET_VECT_INI 0000000000000000\n" +
                                ".SET_KEY K\n" +
                                ".DES3CBC K 00\n" +
                                ".DISPLAY K\n" +
                                "\n" +
                                "* Compute pseudo-random\n" +
                                ".SET_DATA G\n" +
                                ".SET_VECT_INI 0000000000000000\n" +
                                ".SET_KEY J\n" +
                                ".MAC3 L 80 /P\n" +
                                "\n" +
                                ".IFDEF (SIMULATOR || OTA)\n" +
                                "    .APPEND_BUFFER O L(1;6)\n" +
                                ".ELSE\n" +
                                "    * Verify pseudo-random\n" +
                                "    .SET_DATA L(1;6)\n" +
                                "    .COMPARE O(3;6)\n" +
                                ".ENDIF\n" +
                                "\n" +
                                "* Compute card cryptogram\n" +
                                ".SET_DATA H O\n" +
                                ".SET_VECT_INI 0000000000000000\n" +
                                ".SET_KEY I\n" +
                                ".MAC33 L 80 /P\n" +
                                "\n" +
                                ".IFNDEF (SIMULATOR || OTA)\n" +
                                "    * Verify card cryptogram\n" +
                                "    .SET_DATA R(21;8)\n" +
                                "    .COMPARE L\n" +
                                "    * (Card cryptogram incorrect => wrong keyset) if error\n" +
                                ".ENDIF\n" +
                                "\n" +
                                "* Compute host cryptogram\n" +
                                ".SET_DATA O H\n" +
                                ".SET_VECT_INI 0000000000000000\n" +
                                ".SET_KEY I\n" +
                                ".MAC33 L 80 /P\n" +
                                "\n" +
                                "* Compute command MAC\n" +
                                ".SET_DATA 84 82 %SC_LEVEL 00 10 L\n" +
                                "\n" +
                                ".SET_VECT_INI 0000000000000000\n" +
                                ".SET_KEY J\n" +
                                ".MAC3 M 80 /P\n" +
                                "\n" +
                                "* Send the EXTERNAL AUTHENTICATE command\n" +
                                ".SET_BUFFER N 84 82 %SC_LEVEL 00 10 L M\n" +
                                "\n" +
                                ".IFDEF SIMULATOR\n" +
                                "\t* Display the command\n" +
                                "\t.DISPLAY N\n" +
                                ".ELSE\n" +
                                "\t.IFDEF OTA\n" +
                                "\t\t* Apprend the command to the OTA script\n" +
                                "\t\t.APPEND_BUFFER Q N\n" +
                                "\t.ELSE\n" +
                                "\t\t* Sends the command to the card\n" +
                                "\t\tN (9000)\n" +
                                "\t.ENDIF\n" +
                                ".ENDIF \n" +
                                "\n" +
                                "\t.UNDEFINE %ISD_ENC\n" +
                                "\t.UNDEFINE %ISD_DEK\n" +
                                "\t.UNDEFINE %ISD_MAC\n" +
                                "\t.UNDEFINE %SC_LEVEL\n"
                );

            }

        } else if (isd.getMethodForGpCommand().equals("no Card Manager Keyset")) {
            //TODO
        } else if (isd.getMethodForGpCommand().equals("SIMBiOs")) {
            routine.append(
                    ".POWER_ON\n" +
                    "00 A4 04 00 <?> " + root.getRunSettings().getCardParameters().getCardManagerAid() + "\n" +
                    "00 20 00 00 08 %" + isd.getCardManagerPin() + "\n"
            );
        }
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

    private String sendBuffer(Boolean isUpdateRecord) {
        StringBuilder routine = new StringBuilder();
        if(isUpdateRecord) {
            routine.append(
                    "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n" +
                    "A0 A4 00 00 02 7F10 (9F22) ;select DF Telecom\n" +
                    "A0 A4 00 00 02 6F3C (9F0F) ;select EF SMS\n" +
                    "A0 DC 01 04 G J (91XX) ;update EF SMS\n" +
                    ".CLEAR_SCRIPT\n" +
                    "\n" +
                    "A0 B2 01 04 B0 \n" +
                    "\n" +
                    "A0 12 0000 W(2;1) [XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX016101] (9000)\n" +
                    "A0 14 0000 0C 8103011300 82028183 830100 (9000) \n"
            );
        } else {
            routine.append(
                    "A0 C2 00 00 G J (9FXX)\n" +
                    ".CLEAR_SCRIPT\n" +
                    "\n" +
                    "A0 C0 00 00 W(2;1) [XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX0161XX] \n"
            );

        }
        return routine.toString();
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

}
