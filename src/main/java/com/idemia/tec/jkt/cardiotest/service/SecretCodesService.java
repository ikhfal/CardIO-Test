package com.idemia.tec.jkt.cardiotest.service;

import com.idemia.tec.jkt.cardiotest.model.SecretCodes;
import org.springframework.stereotype.Service;

@Service
public class SecretCodesService {

    public StringBuilder generateSecretCodes2g(SecretCodes secretCodes) {
        StringBuilder codes2gBuffer = new StringBuilder();
        codes2gBuffer.append(
            ".CALL Mapping.txt /LIST_OFF\n"
            + ".CALL Options.txt /LIST_OFF\n\n"
            + ".POWER_ON\n"
        );
        if (secretCodes.isPin1disabled())
            codes2gBuffer.append("A0 28 00 01 08 %" + secretCodes.getChv1() + " (9000) ; enable CHV1\n\n");
        if (secretCodes.isPin2disabled())
            codes2gBuffer.append("A0 28 00 02 08 %" + secretCodes.getChv1() + " (9000) ; enable CHV2\n\n");
        codes2gBuffer.append(
            "; CHV1\n"
            + "; check CHV1 remaining attempts\n"
            + "A0 A4 00 00 02 3F00 (9FXX)\n"
            + "A0 C0 00 00 W(2;1) (9000) [XXXX XXXX 3F00 XX XX XXXXXX XX 15 XX XX XX XX XX (80 | " + String.format("%02X", secretCodes.getChv1Retries()) + ") XX XX XX XX XX XX XX XX XX XX XX XX XX XX XX] ; remaining attempts for CHV1\n"
            + "; verify CHV1\n"
            + "A0 20 00 01 08 %" + secretCodes.getChv1() + " (9000)\n"
            + "; disable CHV1 and verify\n"
            + "A0 26 00 01 08 %" + secretCodes.getChv1() + " (9000)\n"
            + "A0 20 00 01 08 %" + secretCodes.getChv1() + " (9808) ; verification failed\n"
            + "; enable CHV1 and verify\n"
            + "A0 28 00 01 08 %" + secretCodes.getChv1() + " (9000)\n"
            + "A0 20 00 01 08 %" + secretCodes.getChv1() + " (9000)\n"
            + "; change CHV1 and verify new value\n"
            + "A0 24 00 01 10 %" + secretCodes.getChv1() + " %" + secretCodes.getChv2() + " (9000)\n"
            + "A0 20 00 01 08 %" + secretCodes.getChv2() + " (9000)\n"
            + "; restore initial CHV1 and verify\n"
            + "A0 24 00 01 10 %" + secretCodes.getChv2() + " %" + secretCodes.getChv1() + " (9000)\n"
            + "A0 20 00 01 08 %" + secretCodes.getChv1() + " (9000)\n\n"
            + "; PUK1\n"
            + "; block CHV1 and verify\n"
            + "; try verifying with false code " + secretCodes.getChv1Retries() + " times\n"
        );
        for (int i = 0; i < secretCodes.getChv1Retries(); i++)
            codes2gBuffer.append("A0 20 00 01 08 FFFFFFFFFFFFFFFF (98XX) ; try: " + (i + 1) + "\n");
        codes2gBuffer.append(
            "A0 20 00 01 08 %" + secretCodes.getChv1() + " (9840)\n"
            + "; unblock CHV1 using PUK1 and verify\n"
            + "A0 2C 00 00 10 %" + secretCodes.getPuk1() + " %" + secretCodes.getChv1() + " (9000)\n"
            + "A0 20 00 01 08 %" + secretCodes.getChv1() + " (9000)\n"
            + "; unblock CHV1 using wrong PUK1\n"
            + "A0 2C 00 00 10 FFFFFFFFFFFFFFFF %" + secretCodes.getChv1() + " (9804) ; verification failed\n"
            + "; check PUK1 remaining verifications\n"
            + "A0 A4 00 00 02 3F00 (9FXX)\n"
            + "A0 C0 00 00 W(2;1) (9000) [XXXX XXXX 3F00 XX XX XXXXXX XX 15 XX XX XX XX XX XX (80 | " + String.format("%02X", secretCodes.getPuk1Retries()) + ") XX XX XX XX XX XX XX XX XX XX XX XX XX XX] ; remaining attempts for PUK1 because of wrong verification in previous command\n\n"
            + "A0 2C 00 00 10 %" + secretCodes.getPuk1() + " %" + secretCodes.getChv1() + " (9000) ; reset retry\n"
            + "; CHV2\n"
            + "; check CHV2 remaining verifications\n"
            + "A0 A4 00 00 02 3F00 (9FXX)\n"
            + "A0 C0 00 00 W(2;1) (9000) [XXXX XXXX 3F00 XX XX XXXXXX XX 15 XX XX XX XX XX XX XX (80 | " + String.format("%02X", secretCodes.getChv2Retries()) + ") XX XX XX XX XX XX XX XX XX XX XX XX XX] ; remaining attempts for CHV2\n"
            + "; verify CHV2\n"
            + "A0 20 00 02 08 %" + secretCodes.getChv2() + " (9000)\n"
            + "; change CHV2 and verify new value\n"
            + "A0 24 00 02 10 %" + secretCodes.getChv2() + " %" + secretCodes.getChv1() + " (9000)\n"
            + "A0 20 00 02 08 %" + secretCodes.getChv1() + " (9000)\n"
            + "; restore initial CHV2 and verify\n"
            + "A0 24 00 02 10 %" + secretCodes.getChv1() + " %" + secretCodes.getChv2() + " (9000)\n"
            + "A0 20 00 02 08 %" + secretCodes.getChv2() + " (9000)\n\n"
            + "; PUK2\n"
            + "; block CHV2 and verify\n"
            + "; try verifying with false code " + secretCodes.getChv2Retries() + " times\n"
        );
        for (int i = 0; i < secretCodes.getChv2Retries(); i++)
            codes2gBuffer.append("A0 20 00 02 08 FFFFFFFFFFFFFFFF (98XX) ; try: " + (i + 1) + "\n");
        codes2gBuffer.append(
            "A0 20 00 02 08 %" + secretCodes.getChv2() + " (9840)\n"
            + "; unblock CHV2 using PUK2 and verify\n"
            + "A0 2C 00 02 10 %" + secretCodes.getPuk2() + " %" + secretCodes.getChv2() + " (9000)\n"
            + "A0 20 00 02 08 %" + secretCodes.getChv2() + " (9000)\n"
            + "; unblock CHV2 using wrong PUK2\n"
            + "A0 2C 00 02 10 FFFFFFFFFFFFFFFF %" + secretCodes.getChv2() + " (9804) ; verification failed\n"
            + "; check PUK2 remaining verifications\n"
            + "A0 A4 00 00 02 3F00 (9FXX)\n"
            + "A0 C0 00 00 W(2;1) (9000) [XXXX XXXX 3F00 XX XX XXXXXX XX 15 XX XX XX XX XX XX XX XX (80 | " + String.format("%02X", secretCodes.getPuk2Retries()) + ") XX XX XX XX XX XX XX XX XX XX XX XX] ; remaining attempts for PUK2 because of wrong verification in previous command\n\n"
            + "A0 2C 00 02 10 %" + secretCodes.getPuk2() + " %" + secretCodes.getChv2() + " (9000) ; reset retry\n"
            + "; ADM1\n"
            + "; verify ADM1\n"
            + "A0 20 00 00 08 %" + secretCodes.getIsc1() + " (9000)\n"
            + "; change ADM1 and verify\n"
            + "A0 24 00 00 10 %" + secretCodes.getIsc1() + " 0123456789ABCDEF (9000)\n"
            + "A0 20 00 00 08 0123456789ABCDEF (9000)\n"
            + "; restore initial ADM1 and verify\n"
            + "A0 24 00 00 10 0123456789ABCDEF %" + secretCodes.getIsc1() + " (9000)\n"
            + "A0 20 00 00 08 %" + secretCodes.getIsc1() + " (9000)\n\n"
        );
        if (secretCodes.isUseIsc2()) {

            codes2gBuffer.append(
                "; ADM2\n"
                + "; verify ADM2\n"
                + "A0 20 00 05 08 %" + secretCodes.getIsc2() + " (9000)\n");

            if (secretCodes.isBlockIsc2())
            {
                codes2gBuffer.append(
                 "; block ADM2 and verify\n"
                + "; try verifying with false code " + secretCodes.getIsc2Retries() + " times\n"
            );
            for (int i = 0; i < secretCodes.getIsc2Retries(); i++)
                codes2gBuffer.append("A0 20 00 05 08 FFFFFFFFFFFFFFFF (98XX) ; try: " + (i + 1) + "\n");
            codes2gBuffer.append(
                "A0 20 00 05 08 %" + secretCodes.getIsc2() + " (9840) ; verification failed\n"
                + "; unblock ADM2 using ADM1 and verify\n"
                + "A0 2C 00 05 10 %" + secretCodes.getIsc1() + " %" + secretCodes.getIsc2() + " (9000)\n"
                + "A0 20 00 05 08 %" + secretCodes.getIsc2() + " (9000)\n"
            );
            }


            codes2gBuffer.append(
                 "; change ADM2 and verify\n"
                + "A0 24 00 05 10 %" + secretCodes.getIsc2() + " %" + secretCodes.getIsc1() + " (9000)\n"
                + "A0 20 00 05 08 %" + secretCodes.getIsc1() + " (9000)\n"
                + "; restore initial ADM2 and verify\n"
                + "A0 24 00 05 10 %" + secretCodes.getIsc1() + " %" + secretCodes.getIsc2() + " (9000)\n"
                + "A0 20 00 05 08 %" + secretCodes.getIsc2() + " (9000)\n\n"
            );
        }
        if (secretCodes.isUseIsc3()) {
            codes2gBuffer.append(
                "; ADM3\n"
                + "; verify ADM3\n"
                + "A0 20 00 06 08 %" + secretCodes.getIsc3() + " (9000)\n");

            if (secretCodes.isBlockIsc3())
            {
            codes2gBuffer.append(
                "; block ADM3 and verify\n"
                + "; try verifying with false code " + secretCodes.getIsc3Retries() + " times\n"
            );

            for (int i = 0; i < secretCodes.getIsc3Retries(); i++)
                codes2gBuffer.append("A0 20 00 06 08 FFFFFFFFFFFFFFFF (98XX) ; try: " + (i + 1) + "\n");
            codes2gBuffer.append(
                "A0 20 00 06 08 %" + secretCodes.getIsc3() + " (9840) ; verification failed\n"
                + "; unblock ADM3 using ADM1 and verify\n"
                + "A0 2C 00 06 10 %" + secretCodes.getIsc1() + " %" + secretCodes.getIsc3() + " (9000)\n"
                + "A0 20 00 06 08 %" + secretCodes.getIsc3() + " (9000)\n" );
            }


            codes2gBuffer.append(
                "; change ADM3 and verify\n"
                + "A0 24 00 06 10 %" + secretCodes.getIsc3() + " %" + secretCodes.getIsc1() + " (9000)\n"
                + "A0 20 00 06 08 %" + secretCodes.getIsc1() + " (9000)\n"
                + "; restore initial ADM3 and verify\n"
                + "A0 24 00 06 10 %" + secretCodes.getIsc1() + " %" + secretCodes.getIsc3() + " (9000)\n"
                + "A0 20 00 06 08 %" + secretCodes.getIsc3() + " (9000)\n\n"
            );
        }
        if (secretCodes.isUseIsc4()) {
            codes2gBuffer.append(
                "; ADM4\n"
                + "; verify ADM4\n"
                + "A0 20 00 07 08 %" + secretCodes.getIsc4() + " (9000)\n");

            if (secretCodes.isBlockIsc4()) {
            codes2gBuffer.append(
                "; block ADM4 and verify\n"
                + "; try verifying with false code " + secretCodes.getIsc4Retries() + " times\n"
            );
            for (int i = 0; i < secretCodes.getIsc4Retries(); i++)
                codes2gBuffer.append("A0 20 00 07 08 FFFFFFFFFFFFFFFF (98XX) ; try: " + (i + 1) + "\n");
            codes2gBuffer.append(
                "A0 20 00 07 08 %" + secretCodes.getIsc4() + " (9840) ; verification failed\n"
                + "; unblock ADM4 using ADM1 and verify\n"
                + "A0 2C 00 07 10 %" + secretCodes.getIsc1() + " %" + secretCodes.getIsc4() + " (9000)\n"
                + "A0 20 00 07 08 %" + secretCodes.getIsc4() + " (9000)\n");
            }

            codes2gBuffer.append(
                "; change ADM4 and verify\n"
                + "A0 24 00 07 10 %" + secretCodes.getIsc4() + " %" + secretCodes.getIsc1() + " (9000)\n"
                + "A0 20 00 07 08 %" + secretCodes.getIsc1() + " (9000)\n"
                + "; restore initial ADM4 and verify\n"
                + "A0 24 00 07 10 %" + secretCodes.getIsc1() + " %" + secretCodes.getIsc4() + " (9000)\n"
                + "A0 20 00 07 08 %" + secretCodes.getIsc4() + " (9000)\n\n"
            );
        }
        if (secretCodes.isBlockPuk1()) {
            codes2gBuffer.append(
                "; Block PUK1\n"
                + "; verify CHV1\n"
                + "A0 2000 01 08 %" + secretCodes.getChv1() + " (9000)\n"
                + "; verify wrong CHV1\n"
            );
            for (int i = 0; i < secretCodes.getChv1Retries(); i++)
                codes2gBuffer.append("A0 20 00 01 08 3A3A3A3A3A3A3A3A (98XX) ; try: " + (i + 1) + "\n");
            codes2gBuffer.append("; unblock with wrong PUK code\n");
            for (int i = 0; i < secretCodes.getPuk1Retries(); i++)
                codes2gBuffer.append("A0 2C 00 01 10 3A3A3A3A3A3A3A3A39393939FFFFFFFF (98XX) ; try: " + (i + 1) + "\n");
            codes2gBuffer.append(
                "; unblock CHV1 by PUK1 fails as PUK1 blocked\n"
                + "A0 2C 00 00 10 %" + secretCodes.getPuk1() + " %" + secretCodes.getChv1() + " (98XX)\n\n"
            );
        }
        if (secretCodes.isBlockPuk2()) {
            codes2gBuffer.append(
                "; Block PUK2\n"
                + "; verify CHV2\n"
                + "A0 20 00 02 08 %" + secretCodes.getChv2() + " (9000)\n"
                + "; verify wrong CHV2\n"
            );
            for (int i = 0; i < secretCodes.getChv2Retries(); i++)
                codes2gBuffer.append("A0 20 00 02 08 3A3A3A3A3A3A3A3A (98XX) ; try: " + (i + 1) + "\n");
            codes2gBuffer.append("; unblock with wrong PUK code\n");
            for (int i = 0; i < secretCodes.getPuk2Retries(); i++)
                codes2gBuffer.append("A0 2C 00 02 10 3A3A3A3A3A3A3A3A39393939FFFFFFFF (98XX) ; try: " + (i + 1) + "\n");
            codes2gBuffer.append(
                "; unblock CHV2 by PUK2 fails as PUK2 blocked\n"
                + "A0 2C 00 02 10 %" + secretCodes.getPuk2() + " %" + secretCodes.getChv2() + " (98XX)\n\n"
            );
        }
        if (secretCodes.isPin1disabled())
            codes2gBuffer.append("A0 26 00 01 08 %" + secretCodes.getChv1() + " (9000) ; disable CHV1\n\n");
        if (secretCodes.isPin2disabled())
            codes2gBuffer.append("A0 26 00 02 08 %" + secretCodes.getChv1() + " (9000) ; disable CHV2\n\n");
        codes2gBuffer.append(".POWER_OFF\n");
        return codes2gBuffer;
    }

    public StringBuilder generateSecretCodes3g(SecretCodes secretCodes) {
        StringBuilder codes3gBuffer = new StringBuilder();
        codes3gBuffer.append(
            ".CALL Mapping.txt /LIST_OFF\n"
            + ".CALL Options.txt /LIST_OFF\n\n"
            + ".POWER_ON\n"
        );
        if (secretCodes.isPin1disabled())
            codes3gBuffer.append("00 28 00 01 08 %" + secretCodes.getGpin() + " (9000) ; enable GPIN1\n\n");
        if (secretCodes.isPin2disabled()) {
            codes3gBuffer.append(
                "; enable LPIN1\n"
                + "00 A4 04 0C <?> %USIM_AID (9000)\n"
                + "00 28 00 81 08 %" + secretCodes.getLpin() + " (9000)\n\n"
            );
        }
        codes3gBuffer.append(
            "; Global PIN\n"
            + "; check GPIN1 remaining verifications\n"
            + "00 A4 04 0C <?> %USIM_AID (9000)\n"
            + "00 20 00 01 00 (63CX) ; remaining attempts for GPIN1\n"
            + "00 20 00 01 08 %" + secretCodes.getGpin() + " (9000)\n"
            + "; disable GPIN1 and verify\n"
            + "00 26 00 01 08 %" + secretCodes.getGpin() + " (9000)\n"
            + "00 20 00 01 08 %" + secretCodes.getGpin() + " (6984) ; verification failed\n"
            + "; enable GPIN1 and verify\n"
            + "00 28 00 01 08 %" + secretCodes.getGpin() + " (9000)\n"
            + "00 20 00 01 08 %" + secretCodes.getGpin() + " (9000)\n"
            + "; change GPIN1 and verify new value\n"
            + "00 24 00 01 10 %" + secretCodes.getGpin() + " %" + secretCodes.getLpin() + " (9000)\n"
            + "00 20 00 01 08 %" + secretCodes.getLpin() + " (9000)\n"
            + "; restore initial GPIN1 and verify\n"
            + "00 24 00 01 10 %" + secretCodes.getLpin() + " %" + secretCodes.getGpin() + " (9000)\n"
            + "00 20 00 01 08 %" + secretCodes.getGpin() + " (9000)\n\n"
            + "; Global PUK\n"
            + "; block GPIN1 and verify\n"
            + "; try verifying with false code " + secretCodes.getGpinRetries() + " times\n"
        );
        for (int i = 0; i < secretCodes.getGpinRetries(); i++)
            codes3gBuffer.append("00 20 00 01 08 FFFFFFFFFFFFFFFF (63XX) ; try: " + (i + 1) + "\n");
        codes3gBuffer.append(
            "00 20 00 01 08 %" + secretCodes.getGpin() + " (6983) ; verification failed\n"
            + "; unblock GPIN1 using GPUK1 and verify\n"
            + "00 2C 00 01 10 %" + secretCodes.getGpuk() + " %" + secretCodes.getGpin() + " (9000)\n"
            + "00 20 00 01 08 %" + secretCodes.getGpin() + " (9000)\n"
            + "; unblock GPIN1 using wrong GPUK1\n"
            + "00 2C 00 01 10 FFFFFFFFFFFFFFFF %" + secretCodes.getGpin() + " (63CX) ; verification failed\n"
            + "; check GPUK1 remaining verifications\n"
            + "00 A4 04 0C <?> %USIM_AID\n"
            + "00 2C 00 01 00 (63CX) ; remaining attempts for GPUK1 because of wrong verification in previous command\n\n"
            + "00 2C 00 01 10 %" + secretCodes.getGpuk() + " %" + secretCodes.getGpin() + " (9000) ; reset the retry counter\n"
            + "; Local PIN\n"
            + "; verify LPIN1 remaining verifications\n"
            + "00 A4 04 0C <?> %USIM_AID (9000)\n"
            + "00 20 00 81 00 (63CX) ; remaining attempts for LPIN1\n"
            + "00 20 00 81 08 %" + secretCodes.getLpin() + " (9000)\n"
            + "; change LPIN1 and verify new value\n"
            + "00 24 00 81 10 %" + secretCodes.getLpin() + " %" + secretCodes.getGpin() + " (9000)\n"
            + "00 20 00 81 08 %" + secretCodes.getGpin() + " (9000)\n"
            + "; restore initial LPIN1 and verify\n"
            + "00 24 00 81 10 %" + secretCodes.getGpin() + " %" + secretCodes.getLpin() + " (9000)\n"
            + "00 20 00 81 08 %" + secretCodes.getLpin() + " (9000)\n\n"
            + "; Local PUK\n"
            + "; block LPIN1 and verify\n"
            + "; try verifying with false code " + secretCodes.getLpinRetries() + " times\n"
        );
        for (int i = 0; i < secretCodes.getLpinRetries(); i++)
            codes3gBuffer.append("00 20 00 81 08 FFFFFFFFFFFFFFFF (63XX) ; try: " + (i + 1) + "\n");
        codes3gBuffer.append(
            "00 20 00 81 08 %" + secretCodes.getLpin() + " (6983) ; verification failed\n"
            + "; unblock LPIN1 using LPUK1 and verify\n"
            + "00 2C 00 81 10 %" + secretCodes.getLpuk() + " %" + secretCodes.getLpin() + " (9000)\n"
            + "00 20 00 81 08 %" + secretCodes.getLpin() + " (9000)\n"
            + "; unblock LPIN1 using wrong LPUK1\n"
            + "00 2C 00 81 10 FFFFFFFFFFFFFFFF %" + secretCodes.getLpin() + " (63CX) ; verification failed\n"
            + "; check LPUK1 remaining verifications\n"
            + "00 A4 04 0C <?> %USIM_AID\n"
            + "00 2C 00 81 00 (63CX) ; remaining attempts for LPUK1 because of wrong verification in previous command\n\n"
            + "00 2C 00 81 10 %" + secretCodes.getLpuk() + " %" + secretCodes.getLpin() + " (9000) ; reset the retry\n"
            + "; Issuer Security Code 1\n"
            + "; verify ADM1 remaining verifications\n"
            + "00 A4 04 0C <?> %USIM_AID (9000)\n"
            + "00 20 00 0A 00 (63CX) ; remaining attempts for ADM1\n"
            + "; verify ADM1\n"
            + "00 20 00 0A 08 %" + secretCodes.getIsc1() + " (9000)\n"
            + "; change ADM1 and verify new value\n"
            + "00 24 00 0A 10 %" + secretCodes.getIsc1() + " 0123456789ABCDEF (9000)\n"
            + "00 20 00 0A 08 0123456789ABCDEF (9000)\n"
            + "; restore initial ADM1 and verify\n"
            + "00 24 00 0A 10 0123456789ABCDEF %" + secretCodes.getIsc1() + " (9000)\n"
            + "00 20 00 0A 08 %" + secretCodes.getIsc1() + " (9000)\n\n"
        );

        if (secretCodes.isUseIsc2()) {
            codes3gBuffer.append(
                "; Issuer Security Code 2\n"
                + "; check ADM2 remaining verifications\n"
                + "00 A4 04 0C <?> %USIM_AID (9000)\n"
                + "00 20 00 0B 00 (63CX) ; remaining attempts for ADM2\n"
                + "; verify ADM2\n"
                + "00 20 00 0B 08 %" + secretCodes.getIsc2() + " (9000)\n");

            if (secretCodes.isBlockIsc2())
            {
            codes3gBuffer.append(
                "; block ADM2 and verify\n"
                + "; try verifying with false code " + secretCodes.getIsc2Retries() + " times\n"
            );

            for (int i = 0; i < secretCodes.getIsc2Retries(); i++)
                codes3gBuffer.append("00 20 00 0B 08 FFFFFFFFFFFFFFFF (63XX) ; try: " + (i + 1) + "\n");
            codes3gBuffer.append(
                "00 20 00 0B 08 %" + secretCodes.getIsc2() + " (6983) ; verification failed\n"
                + "; unblock ADM2 using ADM1 and verify\n"
                + "00 2C 00 0B 10 %" + secretCodes.getIsc1() + " %" + secretCodes.getIsc2() + " (9000)\n"
                + "00 20 00 0B 08 %" + secretCodes.getIsc2() + " (9000)\n");
            }

            codes3gBuffer.append(
                "; change ADM2 and verify new value\n"
                + "00 24 00 0B 10 %" + secretCodes.getIsc2() + " %" + secretCodes.getIsc1() + " (9000)\n"
                + "00 20 00 0B 08 %" + secretCodes.getIsc1() + " (9000)\n"
                + "; restore initial ADM2 and verify\n"
                + "00 24 00 0B 10 %" + secretCodes.getIsc1() + " %" + secretCodes.getIsc2() + " (9000)\n"
                + "00 20 00 0B 08 %" + secretCodes.getIsc2() + " (9000)\n\n"
            );
        }

        if (secretCodes.isUseIsc3()) {
            codes3gBuffer.append(
                "; Issuer Security Code 3\n"
                + "; check ADM3 remaining verifications\n"
                + "00 A4 04 0C <?> %USIM_AID (9000)\n"
                + "00 20 00 0C 00 (63CX) ; remaining attempts for ADM3\n"
                + "; verify ADM3\n"
                + "00 20 00 0C 08 %" + secretCodes.getIsc3() + " (9000)\n");

            if (secretCodes.isBlockIsc3())
            {
            codes3gBuffer.append(
                "; block ADM3 and verify\n"
                + "; try verifying with false code " + secretCodes.getIsc3Retries() + " times\n"
            );
            for (int i = 0; i < secretCodes.getIsc3Retries(); i++)
                codes3gBuffer.append("00 20 00 0C 08 FFFFFFFFFFFFFFFF (63XX) ; try: " + (i + 1) + "\n");
            codes3gBuffer.append(
                "00 20 00 0C 08 %" + secretCodes.getIsc3() + " (6983) ; verification failed\n"
                + "; unblock ADM3 using ADM1 and verify\n"
                + "00 2C 00 0C 10 %" + secretCodes.getIsc1() + " %" + secretCodes.getIsc3() + " (9000)\n"
                + "00 20 00 0C 08 %" + secretCodes.getIsc3() + " (9000)\n");
            }

            codes3gBuffer.append(
                "; change ADM3 and verify new value\n"
                + "00 24 00 0C 10 %" + secretCodes.getIsc3() + " %" + secretCodes.getIsc1() + " (9000)\n"
                + "00 20 00 0C 08 %" + secretCodes.getIsc1() + " (9000)\n"
                + "; restore initial ADM3 and verify\n"
                + "00 24 00 0C 10 %" + secretCodes.getIsc1() + " %" + secretCodes.getIsc3() + " (9000)\n"
                + "00 20 00 0C 08 %" + secretCodes.getIsc3() + " (9000)\n\n"
            );
        }

        if (secretCodes.isUseIsc4()) {
            codes3gBuffer.append(
                "; Issuer Security Code 4\n"
                + "; check ADM4 remaining verifications\n"
                + "00 A4 04 0C <?> %USIM_AID (9000)\n"
                + "00 20 00 0D 00 (63CX) ; remaining attempts for ADM4\n"
                + "; verify ADM4\n"
                + "00 20 00 0D 08 %" + secretCodes.getIsc4() + " (9000)\n");

            if (secretCodes.isBlockIsc4())
            {
            codes3gBuffer.append(
                "; block ADM4 and verify\n"
                + "; try verifying with false code " + secretCodes.getIsc4Retries() + " times\n"
            );
            for (int i = 0; i < secretCodes.getIsc4Retries(); i++)
                codes3gBuffer.append("00 20 00 0D 08 FFFFFFFFFFFFFFFF (63XX) ; try: " + (i + 1) + "\n");
            codes3gBuffer.append(
                "00 20 00 0D 08 %" + secretCodes.getIsc4() + " (6983) ; verification failed\n"
                + "; unblock ADM4 using ADM1 and verify\n"
                + "00 2C 00 0D 10 %" + secretCodes.getIsc1() + " %" + secretCodes.getIsc4() + " (9000)\n"
                + "00 20 00 0D 08 %" + secretCodes.getIsc4() + " (9000)\n");
            }

            codes3gBuffer.append(
                "; change ADM4 and verify new value\n"
                + "00 24 00 0D 10 %" + secretCodes.getIsc4() + " %" + secretCodes.getIsc1() + " (9000)\n"
                + "00 20 00 0D 08 %" + secretCodes.getIsc1() + " (9000)\n"
                + "; restore initial ADM4 and verify\n"
                + "00 24 00 0D 10 %" + secretCodes.getIsc1() + " %" + secretCodes.getIsc4() + " (9000)\n"
                + "00 20 00 0D 08 %" + secretCodes.getIsc4() + " (9000)\n\n"
            );
        }
        if (secretCodes.isBlockGpuk()) {
            codes3gBuffer.append(
                "; Block GPUK\n"
                + "; verify GPIN1\n"
                + "00 20 00 01 08 %" + secretCodes.getGpin() + " (9000)\n"
                + "; verify wrong GPIN1\n"
            );
            for (int i = 0; i < secretCodes.getGpinRetries(); i++)
                codes3gBuffer.append("00 20 00 01 08 FFFFFFFFFFFFFFFF (63XX) ; try: " + (i + 1) + "\n");
            codes3gBuffer.append("; unblock with wrong PUK code\n");
            for (int i = 0; i < secretCodes.getGpukRetries(); i++)
                codes3gBuffer.append("00 2C 00 01 10 3A3A3A3A3A3A3A3A39393939FFFFFFFF (63XX) ; try: " + (i + 1) + "\n");
            codes3gBuffer.append(
                "; unblock GPIN1 by GPUK1 fails as PUK1 blocked\n"
                + "00 2C 00 01 10 %" + secretCodes.getGpuk() + " %" + secretCodes.getGpin() + " (69XX)\n\n"
            );
        }
        if (secretCodes.isBlockLpuk()) {
            codes3gBuffer.append(
                "; Block LPUK\n"
                + "; verify LPIN1\n"
                + "00 20 00 81 08 %" + secretCodes.getLpin() + " (9000)\n"
                + "; verify wrong LPIN1\n"
            );
            for (int i = 0; i < secretCodes.getLpinRetries(); i++)
                codes3gBuffer.append("00 20 00 81 08 FFFFFFFFFFFFFFFF (63XX) ; try: " + (i + 1) + "\n");
            codes3gBuffer.append("; unblock with wrong PUK code\n");
            for (int i = 0; i < secretCodes.getLpukRetries(); i++)
                codes3gBuffer.append("00 2C 00 81 10 3A3A3A3A3A3A3A3A39393939FFFFFFFF (63XX) ; try: " + (i + 1) + "\n");
            codes3gBuffer.append(
                "; unblock LPIN1 by LPUK1 fails as PUK2 blocked\n"
                + "00 2C 00 81 10 %" + secretCodes.getLpuk() + " %" + secretCodes.getLpin() + " (69XX)\n\n"
            );
        }
        if (secretCodes.isPin1disabled()) {
            codes3gBuffer.append("00 26 00 01 08 %" + secretCodes.getGpin() + " (9000) ; disable GPIN1\n\n");
        }
        if (secretCodes.isPin2disabled()) {
            codes3gBuffer.append(
                "; disable LPIN1\n"
                + "00 A4 04 0C <?> %USIM_AID (9000)\n"
                + "00 26 00 81 08 %" + secretCodes.getLpin() + " (9000)\n\n"
            );
        }
        codes3gBuffer.append(".POWER_OFF\n");
        return codes3gBuffer;
    }

}
