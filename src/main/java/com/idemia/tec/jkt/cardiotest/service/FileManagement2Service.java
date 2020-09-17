package com.idemia.tec.jkt.cardiotest.service;

import com.idemia.tec.jkt.cardiotest.controller.FileManagement2Controller;
import com.idemia.tec.jkt.cardiotest.controller.RootLayoutController;
import com.idemia.tec.jkt.cardiotest.model.FMLinkFiles;
import com.idemia.tec.jkt.cardiotest.model.FileManagement2;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileManagement2Service {

    @Autowired private RootLayoutController root;
    @Autowired private FileManagement2Controller FMCon2;

    ObservableList <FMLinkFiles> allFMLinkFiles;


    public StringBuilder generateFilemanagement2HighStressArea(FileManagement2 fileManagement2) {

        StringBuilder highStressAreaTestBuffer = new StringBuilder();
        highStressAreaTestBuffer.append(
                ";===================== \n"
                        + ";High Stress Area Test\n"
                        + ";=====================\n\n"
        );


        highStressAreaTestBuffer.append(
                        ".CALL Mapping.txt /LIST_OFF\n"
                        + ".CALL Options.txt /LIST_OFF\n\n"

        );

        if (root.getRunSettings().getSecretCodes().isPin1disabled())
        {
            highStressAreaTestBuffer.append(
                    ";Enable PIN1\n"
                            +"A0 28 00 01 08 %"+ root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n\n"
            );
        }

        if (root.getRunSettings().getSecretCodes().isPin2disabled())
        {
            highStressAreaTestBuffer.append(
                    ";Enable PIN2\n"
                            +"A0 28 00 02 08 %"+ root.getRunSettings().getSecretCodes().getChv2() + " (9000)\n\n"
            );
        }

        highStressAreaTestBuffer.append(
                ".SET_BUFFER J 01 ;file offset while audit\n\n"
        );



        for (int i=0;i < root.getRunSettings().getFileManagement2().getRowHighStressArea() ;i++)
        {

            highStressAreaTestBuffer.append(
                     ".POWER_ON\n"
                             +"A0 20 00 00 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
                             + "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n"
                             + "A0 20 00 02 08 %" + root.getRunSettings().getSecretCodes().getChv2() + " (9000)\n"
            );

            if (root.getRunSettings().getSecretCodes().isUseIsc2())
                highStressAreaTestBuffer.append("A0 20 00 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
            if (root.getRunSettings().getSecretCodes().isUseIsc3())
                highStressAreaTestBuffer.append("A0 20 00 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
            if (root.getRunSettings().getSecretCodes().isUseIsc4())
                highStressAreaTestBuffer.append("A0 20 00 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n\n");


            int length_HighStressArea;

            length_HighStressArea = root.getRunSettings().getFileManagement2().getData_highStressArea(i).length();


            if (length_HighStressArea == 8)
            {
                highStressAreaTestBuffer.append(
                        "A0A4000002 " + root.getRunSettings().getFileManagement2().getData_highStressArea(i).substring(0,4) + " (9Fxx)\n"
                                + "A0A4000002 " + root.getRunSettings().getFileManagement2().getData_highStressArea(i).substring(4,8) + " (9Fxx)\n\n"
                                + ".SET_BUFFER K " + root.getRunSettings().getFileManagement2().getData_highStressArea(i).substring(4,8) + "\n\n"

                );
            }

            else if (length_HighStressArea == 12)
            {
                highStressAreaTestBuffer.append(
                        "A0A4000002 " + root.getRunSettings().getFileManagement2().getData_highStressArea(i).substring(0,4) + " (9Fxx)\n"
                                + "A0A4000002 " + root.getRunSettings().getFileManagement2().getData_highStressArea(i).substring(4,8) + " (9Fxx)\n"
                                + "A0A4000002 " + root.getRunSettings().getFileManagement2().getData_highStressArea(i).substring(8,12) + " (9Fxx)\n\n"
                                + ".SET_BUFFER K " + root.getRunSettings().getFileManagement2().getData_highStressArea(i).substring(8,12) + "\n\n"
                );
            }

            else if (length_HighStressArea == 16)
            {
                highStressAreaTestBuffer.append(
                        "A0A4000002 " + root.getRunSettings().getFileManagement2().getData_highStressArea(i).substring(0,4) + " (9Fxx)\n"
                                + "A0A4000002 " + root.getRunSettings().getFileManagement2().getData_highStressArea(i).substring(4,8) + " (9Fxx)\n"
                                + "A0A4000002 " + root.getRunSettings().getFileManagement2().getData_highStressArea(i).substring(8,12) + " (9Fxx)\n"
                                + "A0A4000002 " + root.getRunSettings().getFileManagement2().getData_highStressArea(i).substring(12,16) + " (9Fxx)\n\n"
                                + ".SET_BUFFER K " + root.getRunSettings().getFileManagement2().getData_highStressArea(i).substring(12,16) + "\n\n"
                );
            }

            highStressAreaTestBuffer.append(

                             ".SWITCH K\n\n"
                                     + "\t.DEFAULT\n"
                                     + "\t\t.SET_BUFFER I 01 ;counter starting from 01\n"
                                     + "\t\t.SET_BUFFER M 01 ;value of EF\n"
                                     + "\t\t.BEGIN_LOOP\n"
                                     + "\t\t.INCREASE_BUFFER M 01\n"
                                     + "\t\t.DEFINE %VAL M\n"
                                     + "\t\t\tA0D6000001 %VAL (9000); Update EF\n"
                                     + "\t\t\t.INCREASE_BUFFER I 01 ;increase counter\n"
                                     + "\t\t\tA0DB01 J 06 [K 00 00  00 I]  (9000) ;-->check FID and counter\n"
                                     + "\t\t.UNDEFINE %VAL\n"
                                     + "\t\t.LOOP 5\n"
                                     + "\t\t.INCREASE_BUFFER J 01\n"
                                     + "\t.BREAK\n\n"
                                     + ".ENDSWITCH\n"
                                     + "\n"
                                     + "\n"
                                     + "\n"
                                     + "\n"

            );


            highStressAreaTestBuffer.append(
                    "\n"
                            + "\n"
                            + "\n"
                            + "\n"
            );



        }

        return highStressAreaTestBuffer;
    }

}
