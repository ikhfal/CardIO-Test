package com.idemia.tec.jkt.cardiotest.service;

import com.idemia.tec.jkt.cardiotest.controller.RootLayoutController;
import com.idemia.tec.jkt.cardiotest.controller.FileManagementController;
import com.idemia.tec.jkt.cardiotest.model.FileManagement;
import com.idemia.tec.jkt.cardiotest.model.FMLinkFiles;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileManagementService {

    @Autowired private RootLayoutController root;
    @Autowired private FileManagementController FMCon;

    ObservableList <FMLinkFiles> allFMLinkFiles;

    public StringBuilder generateFilemanagementLinkFiles(FileManagement fileManagement) {

        StringBuilder linkFileTestBuffer = new StringBuilder();
        linkFileTestBuffer.append(
                ";===================== \n"
                        + ";Link File Test\n"
                        + ";=====================\n\n"
        );

        linkFileTestBuffer.append(
                ".LOAD Calcul.dll\n"
                        //+ ".LOAD Var_Reader.dll\n\n"
        );

        linkFileTestBuffer.append(
                        ".CALL Mapping.txt /LIST_OFF\n"
                        + ".CALL Options.txt /LIST_OFF\n\n"
                        + ".POWER_ON\n"
        );

        if (root.getRunSettings().getSecretCodes().isPin1disabled())
        {
            linkFileTestBuffer.append(
                    ";Enabled PIN1\n"
                    +"00 28 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000)\n\n"
            );
        }


        if (root.getRunSettings().getSecretCodes().isPin2disabled())
        {
            linkFileTestBuffer.append(
                    ";Enabled PIN2\n"
                            +"00 28 00 81 08 %"+ root.getRunSettings().getSecretCodes().getLpin() +" (9000)\n\n"
            );
        }


        linkFileTestBuffer.append(
                         "00 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000)\n"
                        + "00 20 00 0A 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
                );

        linkFileTestBuffer.append(
            "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                    + "00 20 00 81 08 %" + root.getRunSettings().getSecretCodes().getLpin() + " (9000)\n\n"
        );


        if (root.getRunSettings().getSecretCodes().isUseIsc2())
            linkFileTestBuffer.append("00 20 00 0B 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc3())
            linkFileTestBuffer.append("00 20 00 0C 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc4())
            linkFileTestBuffer.append("00 20 00 0D 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");


        for (int i=0;i < root.getRunSettings().getFileManagement().getRow() ;i++)
        {
            linkFileTestBuffer.append(
                    "\n; -------------------------\n"
                            + ";== Link Files Test - " + (i+1) + " ==\n"
                            + "; -------------------------\n\n"
            );

            linkFileTestBuffer.append(
                    "\n; -------------------------\n"
                            + "* \tSELECT MASTER EF \t*\n"
                            + "; -------------------------\n\n"
            );


            linkFileTestBuffer.append(SelectEFMaster(i));


            linkFileTestBuffer.append(
                    "00C00000 W(2;1) (9000)\n\n"
                    + ".SET_BUFFER M R(5;1)\n\n"
            );

            linkFileTestBuffer.append(
                    "; -------------------------\n"
                            + "* \tSELECT LINK EF \t*\n"
                            + "; -------------------------\n\n"
            );


            linkFileTestBuffer.append(SelectEFGhost(i));

            linkFileTestBuffer.append(
                    "00C00000 W(2;1) (9000)\n\n"
                            + ".SET_BUFFER N R(5;1)\n"
                            + ".SET_DATA N\n\n"
                            + ".COMPARE M\n\n"
            );


            linkFileTestBuffer.append(
                    ".SWITCH M\n\n"
                            + "; ---------------------\n"
                            + "* \tTRANSPARENT\t\t*\n"
                            + "; ---------------------\n"
                            + ".CASE 41\n\n"
            );

            linkFileTestBuffer.append(
                            "; ---------------------\n"
                            + "* \tUPDATE 1 BYTE ON LINK EF\t*\n"
                            + "; ---------------------\n\n"
                            + "00 B0 00 00 01 (9000)\n"
                            + ".DEFINE %CONTENT_OR R\n"
                            + "00 D6 00 00 01 AA (9000)\n\n"
            );

            linkFileTestBuffer.append(
                            "; ---------------------\n"
                            + "* \tSELECT MASTER EF\t*\n"
                            + "; ---------------------\n\n"
            );

            linkFileTestBuffer.append(SelectEFMaster(i));

            linkFileTestBuffer.append(
                    "00 B0 00 00 01 [AA] (9000)\n\n"
            );

            linkFileTestBuffer.append(
                    "; ---------------------\n"
                            + "* \tRESTORE EF\t*\n"
                            + "; ---------------------\n\n"
            );

            linkFileTestBuffer.append(
                    "00D6000001 %CONTENT_OR (9000)\n"
                            + ".UNDEFINE %CONTENT_OR\n\n"
                            + ".BREAK\n\n"
            );

            linkFileTestBuffer.append(
                    "; ---------------------\n"
                            + "* \tCYCLIC\t*\n"
                            + "; ---------------------\n"
                    + ".CASE 46\n\n"
            );

            linkFileTestBuffer.append(
                    "; ---------------------\n"
                            + "* \tGET RECORD LENGTH\t*\n"
                            + "; ---------------------\n"
                            +".SET_BUFFER N R(8;1)\n"
                            +"00 B2 01 04 N (9000)\n\n"
            );

            linkFileTestBuffer.append(
                    "; ---------------------\n"
                            + "* \tUPDATE LINK EF\t*\n"
                            + "; ---------------------\n\n"
                            +".DEFINE %RECORD_OR R\n"
                            +".DEFINE %RECORD AA R(2:)\n"
                            +"00 DC 01 03 N %RECORD (9000)\n\n"
            );

            linkFileTestBuffer.append(
                    "; -------------------------\n"
                            + "* \tSELECT MASTER EF \t*\n"
                            + "; -------------------------\n\n"
            );


            linkFileTestBuffer.append(SelectEFMaster(i));

            linkFileTestBuffer.append(
                    "; -------------------------\n"
                            + "* \tREAD LINK EF COMPARE \t*\n"
                            + "; -------------------------\n"
                    +"00 B2 01 04 N [%RECORD ] (9000)\n\n"
            );

            linkFileTestBuffer.append(
                    "; -------------------------\n"
                            + "* \tRESTORE EF \t*\n"
                            + "; -------------------------\n"
                            +"00 DC 01 03 N %RECORD_OR (9000)\n"
                    +".UNDEFINE %RECORD\n"+".UNDEFINE %RECORD_OR\n"+".BREAK\n\n"
            );

            linkFileTestBuffer.append(
                    "; ---------------------\n"
                            + "* \tLINEAR FIXED\t*\n"
                            + "; ---------------------\n"
                            + ".DEFAULT\n\n"
            );

            linkFileTestBuffer.append(
                    "; ---------------------\n"
                            + "* \tGET RECORD LENGTH\t*\n"
                            + "; ---------------------\n"
                            +".SET_BUFFER N R(8;1)\n"
                            +"00 B2 01 04 N (90 00)\n\n"
            );

            linkFileTestBuffer.append(
                    "; ---------------------\n"
                            + "* \tUPDATE LINK EF\t*\n"
                            + "; ---------------------\n"
                            +".DEFINE %RECORD_OR R\n"
                            +".DEFINE %RECORD AA R(2:)\n"
                            +"00DC0104 N %RECORD (9000)\n\n"
            );



            linkFileTestBuffer.append(
                    "; ---------------------\n"
                            + "* \tSELECT MASTER MF\t*\n"
                            + "; ---------------------\n"
            );

            linkFileTestBuffer.append(SelectEFMaster(i));

            linkFileTestBuffer.append(
                    "; ---------------------\n"
                            + "* \tREAD LINK EF COMPARE\t*\n"
                            + "; ---------------------\n"
                    + "00 B2 01 04 N [%RECORD ] (9000)\n\n"
            );

            linkFileTestBuffer.append(
                    "; ---------------------\n"
                            + "* \tRESTORE EF\t*\n"
                            + "; ---------------------\n"
                            + "00 DC 01 04 N %RECORD_OR (9000)\n\n"
                            +".UNDEFINE %RECORD\n"
                            +".UNDEFINE %RECORD_OR\n\n"
                            +".BREAK\n.ENDSWITCH\n"
            );

            //System.out.println("TEST-"+length);
        }


        linkFileTestBuffer.append(
                "\n;\n"
                        + ";\n\n"
                        + ";\n"
        );

        linkFileTestBuffer.append(".POWER_OFF\n");

        if (root.getRunSettings().getSecretCodes().isPin1disabled())
        {
            linkFileTestBuffer.append(
                    ".POWER_ON\n\n"
                    +";Disabled PIN1\n"
                            +"00 26 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000)\n\n"
                            +".POWER_OFF\n"
            );
        }

        if (root.getRunSettings().getSecretCodes().isPin2disabled())
        {
            linkFileTestBuffer.append(
                    ".POWER_ON\n\n"
                    +";Disabled PIN2\n"
                            +"00 26 00 81 08 "+ root.getRunSettings().getSecretCodes().getLpin() +" (9000)\n\n"
                            +".POWER_OFF\n"
            );
        }

        return linkFileTestBuffer;
    }

        private String SelectEFMaster (int j) {

        StringBuilder routine = new StringBuilder();

        int length_master;

        length_master = root.getRunSettings().getFileManagement().getData_master(j).length();

        if (length_master == 8)
        {
            if (FMCon.getSimbiosCtdCheckbox().isSelected())
            {
                if (root.getRunSettings().getFileManagement().getData_master(j).substring(0, 4).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(4,8) + " (61xx)\n"
                    );
                }

                else
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(0,4) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(4,8) + " (61xx)\n"
                    );
                }
            }

            else
            {
                routine.append(
                        "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(0,4) + " (61xx)\n"
                                + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(4,8) + " (61xx)\n"
                );
            }

        }

        else if (length_master == 12)
        {
            if (FMCon.getSimbiosCtdCheckbox().isSelected())
            {
                if (root.getRunSettings().getFileManagement().getData_master(j).substring(0,4).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(4,8) + " (61xx)\n\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(8,12) + " (61xx)\n\n"
                    );
                }

                else if (root.getRunSettings().getFileManagement().getData_master(j).substring(4, 8).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(0,4) + " (61xx)\n"
                                    + "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(8,12) + " (61xx)\n\n"
                    );
                }

                else
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(0,4) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(4,8) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(8,12) + " (61xx)\n\n"
                    );
                }

            }

            else
            {
                routine.append(
                        "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(0,4) + " (61xx)\n"
                                + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(4,8) + " (61xx)\n"
                                + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(8,12) + " (61xx)\n\n"
                );
            }

        }

        else if (length_master == 16)
        {


            if (FMCon.getSimbiosCtdCheckbox().isSelected())
            {
                if (root.getRunSettings().getFileManagement().getData_master(j).substring(0,4).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(4,8) + " (61xx)\n\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(8,12) + " (61xx)\n\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(12,16) + " (61xx)\n"
                    );
                }

                else if (root.getRunSettings().getFileManagement().getData_master(j).substring(4, 8).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(0,4) + " (61xx)\n"
                                    + "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(8,12) + " (61xx)\n\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(12,16) + " (61xx)\n"
                    );
                }

                else
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(0,4) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(4,8) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(8,12) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(12,16) + " (61xx)\n"
                    );
                }

            }

            else
            {
                routine.append(
                        "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(0,4) + " (61xx)\n"
                                + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(4,8) + " (61xx)\n"
                                + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(8,12) + " (61xx)\n"
                                + "00A4000402 " + root.getRunSettings().getFileManagement().getData_master(j).substring(12,16) + " (61xx)\n"
                );
            }


        }

        return routine.toString();
    }

        private String SelectEFGhost (int j) {

        StringBuilder routine = new StringBuilder();

        int length_ghost;

        length_ghost = root.getRunSettings().getFileManagement().getData_ghost(j).length();

        if (length_ghost == 8)
        {
            if (root.getRunSettings().getFileManagement().isSimbiosCtd_bool())
            {
                if (root.getRunSettings().getFileManagement().getData_ghost(j).substring(0, 4).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(4,8) + " (61xx)\n"
                    );
                }
                else
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(0,4) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(4,8) + " (61xx)\n"
                    );
                }
            }

            else
            {
                routine.append(
                        "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(0,4) + " (61xx)\n"
                                + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(4,8) + " (61xx)\n"
                );
            }

        }

        else if (length_ghost == 12)
        {
            if (root.getRunSettings().getFileManagement().isSimbiosCtd_bool())
            {
                if (root.getRunSettings().getFileManagement().getData_ghost(j).substring(0,4).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(4,8) + " (61xx)\n\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(8,12) + " (61xx)\n\n"
                    );
                }

                else if (root.getRunSettings().getFileManagement().getData_ghost(j).substring(4, 8).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(0,4) + " (61xx)\n"
                                    + "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(8,12) + " (61xx)\n\n"
                    );
                }

                else
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(0,4) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(4,8) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(8,12) + " (61xx)\n\n"
                    );
                }
            }

            else
            {
                routine.append(
                        "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(0,4) + " (61xx)\n"
                                + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(4,8) + " (61xx)\n"
                                + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(8,12) + " (61xx)\n\n"
                );
            }

        }

        else if (length_ghost == 16)
        {


            if (root.getRunSettings().getFileManagement().isSimbiosCtd_bool())
            {
                if (root.getRunSettings().getFileManagement().getData_ghost(j).substring(0,4).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(4,8) + " (61xx)\n\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(8,12) + " (61xx)\n\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(12,16) + " (61xx)\n"
                    );
                }

                else if (root.getRunSettings().getFileManagement().getData_ghost(j).substring(4, 8).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(0,4) + " (61xx)\n"
                                    + "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(8,12) + " (61xx)\n\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(12,16) + " (61xx)\n"
                    );
                }


                else
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(0,4) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(4,8) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(8,12) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(12,16) + " (61xx)\n"
                    );
                }

            }

            else
            {
                routine.append(
                        "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(0,4) + " (61xx)\n"
                                + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(4,8) + " (61xx)\n"
                                + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(8,12) + " (61xx)\n"
                                + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ghost(j).substring(12,16) + " (61xx)\n"
                );
            }

        }

        return routine.toString();
    }

    public StringBuilder generateFilemanagementRuwi(FileManagement fileManagement) {

        StringBuilder ruwiTestBuffer = new StringBuilder();


                ruwiTestBuffer.append(
                ";=====================\n"
                        + ";Readable and Updateable when Invalidated Test\n"
                        + ";=====================\n\n"
        );
        ruwiTestBuffer.append(
                ".LOAD dll\\BOTS.dll\n"
                +".POWER_ON /PROTOCOL_ON\n\n"
                + ".CALL Mapping.txt /LIST_OFF\n"
                        + ".CALL Options.txt /LIST_OFF\n\n"
        );

        ruwiTestBuffer.append(
                "\n"
        );

        //SIMBIOS
        if (fileManagement.isSimbiosCtd_bool())
            {
                if (root.getRunSettings().getSecretCodes().isPin1disabled())
                {
                    ruwiTestBuffer.append(
                            ";Enable PIN1\n"
                                    +"00 28 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000)\n\n"
                    );
                }

                if (root.getRunSettings().getSecretCodes().isPin2disabled())
                {
                    ruwiTestBuffer.append(
                            ";Enable PIN2\n"
                                    +"00 28 00 81 08 %"+ root.getRunSettings().getSecretCodes().getLpin() +" (9000)\n\n"
                    );
                }


                ruwiTestBuffer.append(
                        "00 20 00 0A 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
                                + "00 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000)\n\n"
                );

                ruwiTestBuffer.append(
                        "00A40400<?> %USIM_AID\t(9000, 61xx)\n"
                                + "00 20 00 81 08 %" + root.getRunSettings().getSecretCodes().getLpin() + " (9000)\n\n"
                );


                if (root.getRunSettings().getSecretCodes().isUseIsc2())
                    ruwiTestBuffer.append("00 20 00 0B 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
                if (root.getRunSettings().getSecretCodes().isUseIsc3())
                    ruwiTestBuffer.append("00 20 00 0C 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
                if (root.getRunSettings().getSecretCodes().isUseIsc4())
                    ruwiTestBuffer.append("00 20 00 0D 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");


                ruwiTestBuffer.append(
                        ".DEFINE %_VERIFY_ADM1_ 00 2000 0A 08  %" + root.getRunSettings().getSecretCodes().getIsc1() + "\n"
                                + ".DEFINE %_VERIFY_CHV1_ 00 2000 01 08 %" + root.getRunSettings().getSecretCodes().getGpin()+ "\n"
                                + ".DEFINE %_VERIFY_CHV2_ 00 2000 81 08 %" + root.getRunSettings().getSecretCodes().getLpin()+ "\n"
                );

                if (root.getRunSettings().getSecretCodes().isUseIsc2())
                    ruwiTestBuffer.append(".DEFINE %_VERIFY_ADM2_ 00 2000 0B 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
                if (root.getRunSettings().getSecretCodes().isUseIsc3())
                    ruwiTestBuffer.append(".DEFINE %_VERIFY_ADM3_ 00 2000 0C 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
                if (root.getRunSettings().getSecretCodes().isUseIsc4())
                    ruwiTestBuffer.append(".DEFINE %_VERIFY_ADM4_ 00 2000 0D 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");

                ruwiTestBuffer.append(
                        "\n.DEFINE %_SELECT_ \t\t\t00 A4 00 04 02\n"
                                + ".DEFINE %_GET_RESPONSE_ \t00 C00000\n"
                                + ".DEFINE %_READ_BINARY_ \t\t00 B0 0000\n"
                                + ".DEFINE %_READ_RECORD_ \t\t00 B2 0104  ; read 1st reacord\n"
                                + ".DEFINE %_UPDATE_BINARY_ \t00 D6 0000\n"
                                + ".DEFINE %_UPDATE_RECORD_ \t00 DC 0104\n"
                                + ".DEFINE %_UPDATE_RECORD_C_ \t00 DC 0003\n"
                                + ".DEFINE %_INVALIDATE_ \t\t00 04 0000\n"
                                + ".DEFINE %_REHABILITATE_ \t00 44 0000\n\n"
                                + "%_SELECT_ 3F00 (61 XX)\n"
                );


                for (int i=0;i < root.getRunSettings().getFileManagement().getRowRuwi() ;i++) {
                    ruwiTestBuffer.append(
                            "\n; --------------------------------\n"
                                    + "; ====== RUwI Test File - " + (i + 1) + " ======\n"
                                    + "; --------------------------------\n\n"
                    );


                    ruwiTestBuffer.append(SelectPathRuwi(i));

                    ruwiTestBuffer.append(
                            ".UNDEFINE %VBUF\n"
                                    +".DEFINE %VBUF W(2;1)\n"
                                    +"%_GET_RESPONSE_ %VBUF\n\n"
                                    +"\n"
                                    +".SET_BUFFER H 00\n"
                                    +".SET_BUFFER G R\n"
                                    +".SET_BUFFER M A5\t;Tag\n"
                                    +".SET_BUFFER J 01\t;length of tag\n"
                                    +"\n"
                    );

                    ruwiTestBuffer.append(SearchTagFromBufferMSimbiois());

                    ruwiTestBuffer.append(
                            ".SET_BUFFER G G(I;H)\t\t;UPDATE BUFFER G\n"
                                    +".SET_BUFFER M C0\t;Tag\n"
                                    +".SET_BUFFER J 01\t;length of tag\n"
                                    +"\n"
                    );

                    ruwiTestBuffer.append(SearchTagFromBufferMSimbiois());

                    ruwiTestBuffer.append(
                            ".SET_BUFFER L G(I;H)\n"
                            +".SHIFT_LEFT J L 01\n\n"
                    );

                    ruwiTestBuffer.append(
                            ".SWITCH J\n"
                                    +"\t.CASE 80 ;this is TR for SIMBiOS mode\n"
                                    +"\n\t\t;Validate the EF has SET for ruwi (byte 15th for TR , byte 18th = 40)\n"
                                    + "\t\t.MESSAGE \"ruwi set\"\n"
                                    +"\t.BREAK\n\n"
                                    +"\n"
                                    +"\t.CASE 00 ;this is LF/CY for SIMBiOS mode\n"
                                    +"\n\t\t;Validate the EF has SET for ruwi (byte 15th for TR , byte 18th = 40)\n"
                                    +"\t\t.MESSAGE \"ruwi NOT set ERROR\"\n"
                                    +"\t.BREAK\n"
                                    +"\n"
                                    +"\t.DEFAULT ;not TR/LF/CY\n"
                                    +"\t\t.MESSAGE \"NOT AN EF during validate ruwi set\"\n"
                                    +"\t.BREAK\t\n"
                                    +".ENDSWITCH\n\n"
                    );

                    ruwiTestBuffer.append(
                            ";Check is TR or NOT(LF,CY) byte 04th this is actually the length\n\n"
                                    +".SET_BUFFER J R(05;1)\n"
                                    +".SHIFT_LEFT I J 05\n"
                                    +".SET_BUFFER J I\n"
                                    +".SET_BUFFER I R(08;1) ;EF size, used for UPDATE RECORD only, this version max 255 (in fact must be 2 byte)\n"

                    );

                    //+"\t\t.CALL RuWI02_Method.txt\n"
                    ruwiTestBuffer.append(
                            ";FOR EF\n"
                            +" .SWITCH J\n"
                                    +"\t.CASE 00 ;00=TR\n"
                                    +"\t.CASE 20 ;this is TR for SIMBiOS mode\t\n\n"
                                    +"\t\t.INCREASE_BUFFER M 01\n"
                                    +"\t\t%_UPDATE_BINARY_ 01  M  (9000)\n"
                                    +"\t\t%_READ_BINARY_   01 [M] (9000)\n\n"
                                    +"\t\t%_INVALIDATE_ (9000)\n\n"
                                    +"\t\t.INCREASE_BUFFER M 01\n"
                                    +"\t\t%_UPDATE_BINARY_ 01  M  (9000)\n"
                                    +"\t\t%_READ_BINARY_   01 [M] (9000)\n\n"
                                    +"\t\t%_REHABILITATE_ (9000)\n\n"
                                    +"\t\t.INCREASE_BUFFER M 01\n"
                                    +"\t\t%_UPDATE_BINARY_ 01  M  (9000)\n"
                                    +"\t\t%_READ_BINARY_   01 [M] (9000)\n\n"
                                    +"\t\t.INCREASE_BUFFER J 01\n"
                                    +"\t.BREAK\t\n\n\n"
                                    +"\t.CASE C0\n"
                                    +"\t.CASE 03 ;CY\n"
                                    +"\t\t;.SET_BUFFER I R(15;1)\n"
                                    +"\t\t.MESSAGE I\n"
                                    +"\t\t%_READ_RECORD_   I (9000)\n"
                                    +"\t\t;.DEFINE %EF_CONTENT R\n"
                                    +"\t\t.SET_BUFFER L R\n\n"
                                    +"\t\t.INCREASE_BUFFER M 01\n"
                                    +"\t\t.DEFINE %RECORD M L(2:)\n"
                                    +"\t\t%_UPDATE_RECORD_C_ I  %RECORD  (9000)\n"
                                    +"\t\t%_READ_RECORD_   I [%RECORD] (9000)\n"
                                    +"\t\t.UNDEFINE %RECORD\n\n"
                                    +"\t\t%_INVALIDATE_ (9000)\n"
                                    +"\t\t.INCREASE_BUFFER M 01\n"
                                    +"\t\t.DEFINE %RECORD M L(2:)\n"
                                    +"\t\t%_UPDATE_RECORD_C_ I  %RECORD  (9000)\n"
                                    +"\t\t%_READ_RECORD_   I [%RECORD] (9000)\n"
                                    +"\t\t.UNDEFINE %RECORD\n\n"
                                    +"\t\t%_REHABILITATE_ (9000)\n\n"
                                    +"\t\t.INCREASE_BUFFER M 01\n"
                                    +"\t\t.DEFINE %RECORD M L(2:)\n"
                                    +"\t\t%_UPDATE_RECORD_C_ I  %RECORD  (9000)\n"
                                    +"\t\t%_READ_RECORD_   I [%RECORD] (9000)\n"
                                    +"\t\t.UNDEFINE %RECORD\n\n"
                                    +"\t\t.INCREASE_BUFFER J 01\n"
                                    +"\t\t.UNDEFINE %RECORD\n"
                                    +"\t.BREAK\n\n\n"
                                    +"\t\t\n"
                                    +"\t.CASE 40 ;this is LF/CY for SIMBiOS\tmode\n"
                                    +"\t.CASE 01 ;LF\t\n"
                                    +"\t\t.MESSAGE I\n"
                                    +"\t\t%_READ_RECORD_   I (9000)\n"
                                    +"\t\t.SET_BUFFER L R\n\n"
                                    +"\t\t.INCREASE_BUFFER M 01\n"
                                    +"\t\t.DEFINE %RECORD M L(2:)\n"
                                    +"\t\t%_UPDATE_RECORD_ I  %RECORD  (9000)\n"
                                    +"\t\t%_READ_RECORD_   I [%RECORD] (9000)\n"
                                    +"\t\t.UNDEFINE %RECORD\n\n"
                                    +"\t\t%_INVALIDATE_ (9000)\n\n"
                                    +"\t\t.INCREASE_BUFFER M 01\n"
                                    +"\t\t.DEFINE %RECORD M L(2:)\n"
                                    +"\t\t%_UPDATE_RECORD_ I  %RECORD  (9000)\n"
                                    +"\t\t%_READ_RECORD_   I [%RECORD] (9000)\n"
                                    +"\t\t.UNDEFINE %RECORD\n\n"
                                    +"\t\t%_REHABILITATE_ (9000)\n\n"
                                    +"\t\t.INCREASE_BUFFER M 01\n"
                                    +"\t\t.DEFINE %RECORD M L(2:)\n"
                                    +"\t\t%_UPDATE_RECORD_ I  %RECORD  (9000)\n"
                                    +"\t\t%_READ_RECORD_   I [%RECORD] (9000)\n"
                                    +"\t\t.UNDEFINE %RECORD\n\n"
                                    +"\t\t.INCREASE_BUFFER J 01\n"
                                    +"\t\t.UNDEFINE %RECORD\n"
                                    +"\t.BREAK\n\n"
                                    +"\t.DEFAULT ;not TR/LF/CY\n"
                                    +"\t\t.MESSAGE \"NOT AN EF ERROR\"\n"
                                    +"\t.BREAK\n"
                                    +".ENDSWITCH\n"

                    );

                }

                if (root.getRunSettings().getSecretCodes().isPin1disabled())
                {
                    ruwiTestBuffer.append(
                            ";DISABLE PIN1\n"
                                    +"00 26 00 01 08 %"+ root.getRunSettings().getSecretCodes().getGpin() +" (9000)\n\n"
                    );
                }

                if (root.getRunSettings().getSecretCodes().isPin2disabled())
                {
                    ruwiTestBuffer.append(
                            ";DISABLE PIN2\n"
                                    +"00 26 00 81 08 "+ root.getRunSettings().getSecretCodes().getLpin() +" (9000)\n\n"
                    );
                }


            }

        //NON-SIMBIOS
        else
            {
                if (root.getRunSettings().getSecretCodes().isPin1disabled())
                {
                    ruwiTestBuffer.append(
                            ";Enable PIN1\n"
                                    +"A0 28 00 01 08 %"+ root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n\n"
                    );
                }

                if (root.getRunSettings().getSecretCodes().isPin2disabled())
                {
                    ruwiTestBuffer.append(
                            ";Enable PIN2\n"
                                    +"A0 28 00 02 08 %"+ root.getRunSettings().getSecretCodes().getChv2() + " (9000)\n\n"
                    );
                }


                ruwiTestBuffer.append(
                        "A0 20 00 00 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n"
                                + "A0 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getChv1() + " (9000)\n"
                                + "A0 20 00 02 08 %" + root.getRunSettings().getSecretCodes().getChv2() + " (9000)\n"
                );

                if (root.getRunSettings().getSecretCodes().isUseIsc2())
                    ruwiTestBuffer.append("A0 20 00 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
                if (root.getRunSettings().getSecretCodes().isUseIsc3())
                    ruwiTestBuffer.append("A0 20 00 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
                if (root.getRunSettings().getSecretCodes().isUseIsc4())
                    ruwiTestBuffer.append("A0 20 00 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n\n");


                ruwiTestBuffer.append(
                        ".DEFINE %_VERIFY_ADM1_ A0 2000 00 08  %" + root.getRunSettings().getSecretCodes().getIsc1() + "\n"
                                + ".DEFINE %_VERIFY_CHV1_ A0 2000 01 08 %" + root.getRunSettings().getSecretCodes().getChv1()+ "\n"
                                + ".DEFINE %_VERIFY_CHV2_ A0 2000 02 08 %" + root.getRunSettings().getSecretCodes().getChv2()+ "\n"
                );

                if (root.getRunSettings().getSecretCodes().isUseIsc2())
                    ruwiTestBuffer.append(".DEFINE %_VERIFY_ADM2_ A0 2000 05 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
                if (root.getRunSettings().getSecretCodes().isUseIsc3())
                    ruwiTestBuffer.append(".DEFINE %_VERIFY_ADM3_ A0 2000 06 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
                if (root.getRunSettings().getSecretCodes().isUseIsc4())
                    ruwiTestBuffer.append(".DEFINE %_VERIFY_ADM4_ A0 2000 07 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");


                ruwiTestBuffer.append(
                        "\n.DEFINE %_SELECT_ \t\t\tA0 A4 00 00 02\n"
                                + ".DEFINE %_GET_RESPONSE_ \tA0 C00000\n"
                                + ".DEFINE %_READ_BINARY_ \t\tA0 B0 0000\n"
                                + ".DEFINE %_READ_RECORD_ \t\tA0 B2 0104  ; read 1st reacord\n"
                                + ".DEFINE %_UPDATE_BINARY_ \tA0 D6 0000\n"
                                + ".DEFINE %_UPDATE_RECORD_ \tA0 DC 0104\n"
                                + ".DEFINE %_UPDATE_RECORD_CY_ \tA0 DC 0103\n"
                                + ".DEFINE %_INVALIDATE_ \t\tA0 04 0000\n"
                                + ".DEFINE %_REHABILITATE_ \tA0 44 0000\n\n"
                );


                ruwiTestBuffer.append(
                        ".SET_BUFFER M 00 ;value of EF\n"
                );

                for (int i=0;i < root.getRunSettings().getFileManagement().getRowRuwi() ;i++)
                {
                    ruwiTestBuffer.append(
                            "\n; -------------------\n"
                                    + "; == RUwI Test - " + (i+1) + " ==\n"
                                    + "; -------------------\n\n"
                    );


                    ruwiTestBuffer.append(SelectPathRuwi(i));

                    ruwiTestBuffer.append(
                            ".DEFINE %VBUF W(2;1)\n\n"
                                    + ";Validate the EF has SET for ruwi (byte 12th = 05)\n"
                                    + "%_GET_RESPONSE_ %VBUF [XXXXXXXXXXXX XX XXXXXXXX 05 XX XX XX] (9000)\n\n"
                                    + ".SET_BUFFER O R(09;1) ;get READ/UPDATE ACC\n"
                                    + ".SET_BUFFER Q R(11;1) ;get INVALIDATE/REHABILITATE ACC\n\n"
                                    + ";-- ruwi03_execute.txt --\n"
                                    + ".SWITCH O\n"
                                    + "\t.CASE FF\n"
                                    + "\t.CASE XF\n"
                                    + "\t.CASE FX\n"
                                    + "\t\t.MESSAGE \"READ/UPDATE NEVER\"\n"
                                    + "\t.BREAK\n\n"
                                    + "\t.DEFAULT\n"
                                    + "\t\t.CALL RuWI01_OK_To_Go.txt\n"
                                    + "\t.BREAK\n"
                                    + ".ENDSWITCH\n"
                                    + ";-- END ruwi03_execute.txt --\n"
                                    + ".UNDEFINE %VBUF W(2;1)\n\n"
                    );

                }


                if (root.getRunSettings().getSecretCodes().isPin1disabled())
                {
                    ruwiTestBuffer.append(
                            ".POWER_ON\n"
                                    +";Disable PIN1\n"
                                    +"A0 26 00 01 08 %"+ root.getRunSettings().getSecretCodes().getChv1() +" (9000)\n\n"
                                    + ".POWER_OFF\n"
                    );
                }

                if (root.getRunSettings().getSecretCodes().isPin2disabled())
                {
                    ruwiTestBuffer.append(
                            ".POWER_ON\n"
                                    +";Disable PIN2\n"
                                    +"A0 26 00 02 08 %"+ root.getRunSettings().getSecretCodes().getChv2() +"  (9000)\n\n"
                                    + ".POWER_OFF\n"
                    );
                }

            }

        ruwiTestBuffer.append(".POWER_OFF\n");
        return ruwiTestBuffer;
    }

        private String SelectPathRuwi (int j) {

        StringBuilder routine = new StringBuilder();

            int length_pathruwi;

            length_pathruwi = root.getRunSettings().getFileManagement().getData_ruwi(j).length();

        if (length_pathruwi == 8)
        {
            if (FMCon.getSimbiosCtdCheckbox().isSelected())
            {
                if (root.getRunSettings().getFileManagement().getData_ruwi(j).substring(0, 4).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(4,8) + " (61xx)\n"
                    );
                }

                else
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(0,4) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(4,8) + " (61xx)\n"
                    );
                }
            }

            else
            {
                routine.append(
                        "A0A4000002 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(0,4) + " (9FXX)\n"
                                + "A0A4000002 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(4,8) + " (9FXX)\n"
                );
            }

        }

        else if (length_pathruwi == 12)
        {
            if (FMCon.getSimbiosCtdCheckbox().isSelected())
            {
                if (root.getRunSettings().getFileManagement().getData_ruwi(j).substring(0,4).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(4,8) + " (61xx)\n\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(8,12) + " (61xx)\n\n"
                    );
                }

                else if (root.getRunSettings().getFileManagement().getData_ruwi(j).substring(4, 8).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(0,4) + " (61xx)\n"
                                    + "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(8,12) + " (61xx)\n\n"
                    );
                }

                else
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(0,4) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(4,8) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(8,12) + " (61xx)\n\n"
                    );
                }

            }

            else
            {
                routine.append(
                        "A0A4000002 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(0,4) + " (9FXX)\n"
                                + "A0A4000002 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(4,8) + " (9FXX)\n"
                                + "A0A4000002 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(8,12) + " (9FXX)\n\n"
                );
            }

        }

        else if (length_pathruwi == 16)
        {

            if (FMCon.getSimbiosCtdCheckbox().isSelected())
            {
                if (root.getRunSettings().getFileManagement().getData_ruwi(j).substring(0,4).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(4,8) + " (61xx)\n\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(8,12) + " (61xx)\n\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(12,16) + " (61xx)\n"
                    );
                }

                else if (root.getRunSettings().getFileManagement().getData_ruwi(j).substring(4, 8).equals(root.getRunSettings().getCardParameters().getDfUsim()))
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(0,4) + " (61xx)\n"
                                    + "00 A4 04 00 <?> %USIM_AID\t(9000, 61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(8,12) + " (61xx)\n\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(12,16) + " (61xx)\n"
                    );
                }

                else
                {
                    routine.append(
                            "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(0,4) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(4,8) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(8,12) + " (61xx)\n"
                                    + "00A4000402 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(12,16) + " (61xx)\n"
                    );
                }

            }

            else
            {
                routine.append(
                        "A0A4000002 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(0,4) + " (9FXX)\n"
                                + "A0A4000002 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(4,8) + " (9FXX)\n"
                                + "A0A4000002 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(8,12) + " (9FXX)\n"
                                + "A0A4000002 " + root.getRunSettings().getFileManagement().getData_ruwi(j).substring(12,16) + " (9FXX)\n"
                );
            }


        }

        return routine.toString();
    }

        private String SearchTagFromBufferMSimbiois () {
            StringBuilder routine = new StringBuilder();

            routine.append(
                    ";====== Search_Tag_From_Buffer_M =====\n\n"
                            +";;;;;RESET BUFFER H -> FOR LENGTH CONTAINER\n"
                            +".SET_BUFFER H 00 00\n\n"
                            +";;;;SET OFFSET\n"
                            +";;;FOR RESPONSE CONTENT\n"
                            +".SET_BUFFER I 00 01\n\n"
                            +"***SEARCH TAG 85***\n\n"
                            +".BEGIN_LOOP\n"
                            +"\t.SWITCH G(I;J)\n"
                            +"\t\t.CASE M\n"
                            +"\t\t\t.INCREASE_BUFFER I J\n"
                            +"\t\t\t.MESSAGE ****************\n"
                            +"\t\t\t.PRINT M\n"
                            +"\t\t\t.MESSAGE TAG Found!\n"
                            +"\t\t\t.APPEND_IFDEF PARSE_TAG_85_ON;\n"
                            +"\t\t\t.QUITLOOP\n"
                            +"\t\t\t.BREAK\n"
                            +"\t\t.DEFAULT\n"
                            +"\t\t\t.PRINT G(I;J)\n"
                            +"\t\t\t.INCREASE_BUFFER I 01\n"
                            +"\t\t.BREAK\n"
                            +"\t.ENDSWITCH\n"
                            +".LOOP 500\n\n"
                            +"***END SEARCH TAG 85***\n\n"
                            +"\n"
                            +"****EXTRACT CONTENT LENGTH****\n\n"
                            +".DISPLAY G(I;1)\n"
                            +"\n"
                            +".SWITCH G(I;1)\n"
                            +"\t.CASE 81\n"
                            +"\t\t.INCREASE_BUFFER I 01\n"
                            +"\t\t.SET_BUFFER H G(I;1)\n"
                            +"\t\t.INCREASE_BUFFER I 01\n"
                            +"\t\t.BREAK\n"
                            +"\n"
                            +"\t.CASE 82\n"
                            +"\t\t.INCREASE_BUFFER I 01\n"
                            +"\t\t.SET_BUFFER H G(I;2)\n"
                            +"\t\t.INCREASE_BUFFER I 02\n"
                            +"\t\t.BREAK\n"
                            +"\n"
                            +"\t.CASE 83\n"
                            +"\t\t.INCREASE_BUFFER I 01\n"
                            +"\t\t.SET_BUFFER H G(I;3)\n"
                            +"\t\t.INCREASE_BUFFER I 03\n"
                            +"\t\t.BREAK\n"
                            +"\n"
                            +"\t.DEFAULT\n"
                            +"\t\t.SET_BUFFER H G(I;1)\n"
                            +"\t\t.INCREASE_BUFFER I 01\n"
                            +"\t\t.BREAK\n"
                            +".ENDSWITCH\n\n"
                            +";====== END Search_Tag_From_Buffer_M =====\n\n\n"
            );

            return routine.toString();
        }

        public StringBuilder generateFilemanagementRuWI01_OK_To_Go(FileManagement fileManagement) {
            StringBuilder RuWI01_OK_To_GoBuffer = new StringBuilder();
            RuWI01_OK_To_GoBuffer.append(";RuWI Helper_1\n");

            RuWI01_OK_To_GoBuffer.append(
                    ".SWITCH Q\n"
                            +"\t.CASE FF\n"
                            +"\t.CASE XF\n"
                            +"\t.CASE FX\n"
                            +"\t\t.MESSAGE \"INVALIDATE/REHABILITATE\"\n"
                            +"\t.BREAK\n"
                            +"\n\t.DEFAULT\n"
                            +"\t\t.MESSAGE \"OK to Proceed\"\n"
                            +"\t\t;Check is TR or NOT(LF,CY) byte 14th\n"
                            +"\t\t;00=TR 01=LF 03=CY\n"
                            +"\t\t.SET_BUFFER J R(14;1)\n"
                            +"\t\t.SET_BUFFER I R(15;1) ;EF size, used for UPDATE RECORD only\n"
                            +"\t\t.CALL RuWI02_Method.txt\n"
                            +"\t.BREAK\n"
                            +".ENDSWITCH\n"
            );

            return RuWI01_OK_To_GoBuffer;
        }

        public StringBuilder generateFilemanagementRuWI02_Method(FileManagement fileManagement) {
        StringBuilder RuWI02_MethodBuffer = new StringBuilder();
            RuWI02_MethodBuffer.append(";RuWI Helper_2 - Method\n");

            RuWI02_MethodBuffer.append(
                "\n"
                        +";FOR EF\n"
                        +" .SWITCH J\n"
                        +"\t.CASE 00 ;00=TR\n"
                        +"\t.CASE 02 ;this is TR for SIMBiOS mode\n"
                        +"\t\t.INCREASE_BUFFER M 01\n"
                        +"\t\t%_UPDATE_BINARY_ 01  M  (9000)\n"
                        +"\t\t%_READ_BINARY_   01 [M] (9000)\n\n"
                        +"\t\t%_INVALIDATE_ (9000)\n\n"
                        +"\t\t.INCREASE_BUFFER M 01\n"
                        +"\t\t%_UPDATE_BINARY_ 01  M  (9000)\n"
                        +"\t\t%_READ_BINARY_   01 [M] (9000)\n\n"
                        +"\t\t%_REHABILITATE_ (9000)\n\n"
                        +"\t\t.INCREASE_BUFFER M 01\n"
                        +"\t\t%_UPDATE_BINARY_ 01  M  (9000)\n"
                        +"\t\t%_READ_BINARY_   01 [M] (9000)\n\n"
                        +"\t\t.INCREASE_BUFFER J 01\n"
                        +"\t.BREAK\n\n\n"
                        +"\t.CASE 03 ;CY\n"
                        +"\t\t.MESSAGE I\n"
                        +"\t\t%_READ_RECORD_   I (9000)\n"
                        +"\t\t.SET_BUFFER L R\n\n"
                        +"\t\t.INCREASE_BUFFER M 01\n"
                        +"\t\t.DEFINE %RECORD M L(2:)\n"
                        +"\t\t%_UPDATE_RECORD_CY_ I  %RECORD  (9000)\n"
                        +"\t\t%_READ_RECORD_   I [%RECORD] (9000)\n"
                        +"\t\t.UNDEFINE %RECORD\n\n"
                        +"\t\t%_INVALIDATE_ (9000)\n\n"
                        +"\t\t.INCREASE_BUFFER M 01\n"
                        +"\t\t.DEFINE %RECORD M L(2:)\n"
                        +"\t\t%_UPDATE_RECORD_CY_ I  %RECORD  (9000)\n"
                        +"\t\t%_READ_RECORD_   I [%RECORD] (9000)\n"
                        +"\t\t.UNDEFINE %RECORD\n\n"
                        +"\t\t%_REHABILITATE_ (9000)\n\n"
                        +"\t\t.INCREASE_BUFFER M 01\n"
                        +"\t\t.DEFINE %RECORD M L(2:)\n"
                        +"\t\t%_UPDATE_RECORD_CY_ I  %RECORD  (9000)\n"
                        +"\t\t%_READ_RECORD_   I [%RECORD] (9000)\n"
                        +"\t\t.UNDEFINE %RECORD\n\n"
                        +"\t\t.INCREASE_BUFFER J 01\n"
                        +"\t\t.UNDEFINE %RECORD\n"
                        +"\t.BREAK\n\n\n"
                        +"\t.CASE 01 ;LF\n"
                        +"\t.CASE 05 ;this is LF/CY for SIMBiOS\tmode\n"
                        +"\t\t.MESSAGE I\n"
                        +"\t\t%_READ_RECORD_   I (9000)\n"
                        +"\t\t.SET_BUFFER L R\n\n"
                        +"\t\t.INCREASE_BUFFER M 01\n"
                        +"\t\t.DEFINE %RECORD M L(2:)\n"
                        +"\t\t%_UPDATE_RECORD_ I  %RECORD  (9000)\n"
                        +"\t\t%_READ_RECORD_   I [%RECORD] (9000)\n"
                        +"\t\t.UNDEFINE %RECORD\n\n"
                        +"\t\t%_INVALIDATE_ (9000)\n\n"
                        +"\t\t.INCREASE_BUFFER M 01\n"
                        +"\t\t.DEFINE %RECORD M L(2:)\n"
                        +"\t\t%_UPDATE_RECORD_ I  %RECORD  (9000)\n"
                        +"\t\t%_READ_RECORD_   I [%RECORD] (9000)\n"
                        +"\t\t.UNDEFINE %RECORD\n\n"
                        +"\t\t%_REHABILITATE_ (9000)\n\n"
                        +"\t\t.INCREASE_BUFFER M 01\n"
                        +"\t\t.DEFINE %RECORD M L(2:)\n"
                        +"\t\t%_UPDATE_RECORD_ I  %RECORD  (9000)\n"
                        +"\t\t%_READ_RECORD_   I [%RECORD] (9000)\n"
                        +"\t\t.UNDEFINE %RECORD\n\n"
                        +"\t\t.INCREASE_BUFFER J 01\n"
                        +"\t\t.UNDEFINE %RECORD\n"
                        +"\t.BREAK\n"
                        +"\t.DEFAULT ;not TR/LF/CY\n"
                        +"\t\t.MESSAGE \"NOT AN EF\"\n"
                        +"\t.BREAK\n"
                        +".ENDSWITCH\n"
                        +"\n"
        );

        return RuWI02_MethodBuffer;
    }

    public StringBuilder generateFilemanagementSfi(FileManagement fileManagement) {
        StringBuilder sfiTestBuffer = new StringBuilder();
        sfiTestBuffer.append(
                ";=====================\n"
                        + ";SFI Check Test\n"
                        + ";=====================\n\n"
        );

        sfiTestBuffer.append(
                        "; This is script to check SFI values according to ETSI Standard\n"
                        + "; A Short File Identifier (SFI) is coded as 5 bits valued in the range from 1 to 30.\n"
                        + "; No two files under the same parent shall have the same SFI\n"
                        + "; Note: DF Phonebook is not covered yet\n"
                        + "; How to decode SFI>check tag 88, SFI is coded from bits b8 to b4, see TS 102 221 for further reference\n"
                        + "; In case you found error like 6A82 (file not found), please check the profile in case SFI is set as 00.\n"
                        + "; Then pls compare to spec to know whether it's Optional or Mandatory \n\n"
        );

        sfiTestBuffer.append(
                ";APDU Mapping\n;=====================\n\n"
                        +".DEFINE %SELECT 00 A4 00 00 02 #2C\n"
                        + ".DEFINE %SELECT_AID 00 A4 04 00 #NC\n"
                        + ".DEFINE %READ_BINARY 00 B0 #3C\n"
                        + ".DEFINE %READ_RECORD 00 B2 #3C\n\n"
        );

        sfiTestBuffer.append(
                ".CALL Mapping.txt /LIST_OFF\n"
                        + ".CALL Options.txt /LIST_OFF\n\n"
                        + ".POWER_ON\n"
        );

        if (root.getRunSettings().getSecretCodes().isPin1disabled())
        {
            sfiTestBuffer.append(
                    ";Enabled PIN1\n"
                            +"00 28 00 01 08 %"+ root.getRunSettings().getSecretCodes().getGpin() +" (9000)\n\n"
            );
        }

        if (root.getRunSettings().getSecretCodes().isPin2disabled())
        {
            sfiTestBuffer.append(
                    ";Enabled PIN2\n"
                            +"00 28 00 81 08 "+ root.getRunSettings().getSecretCodes().getLpin() +" (9000)\n\n"
            );
        }


        sfiTestBuffer.append(
                //"00A40400<?> %USIM_AID\t(61xx)\n"
                        "00 20 00 01 08 %" + root.getRunSettings().getSecretCodes().getGpin() + " (9000)\n"
                        //+ "00 20 00 81 08 %" + root.getRunSettings().getSecretCodes().getLpin() + " (9000)\n"
                        + "00 20 00 0A 08 %" + root.getRunSettings().getSecretCodes().getIsc1() + " (9000)\n");

        if (root.getRunSettings().getSecretCodes().isUseIsc2())
            sfiTestBuffer.append("00 20 00 0B 08 %" + root.getRunSettings().getSecretCodes().getIsc2() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc3())
            sfiTestBuffer.append("00 20 00 0C 08 %" + root.getRunSettings().getSecretCodes().getIsc3() + " (9000)\n");
        if (root.getRunSettings().getSecretCodes().isUseIsc4())
            sfiTestBuffer.append("00 20 00 0D 08 %" + root.getRunSettings().getSecretCodes().getIsc4() + " (9000)\n");


            sfiTestBuffer.append(
                    "00 A4 04 00 <?> %USIM_AID\t(9000,61xx)\n"
                            + "00 20 00 81 08 %" + root.getRunSettings().getSecretCodes().getLpin() + " (9000)\n\n"
                            + "00 A4 00 04 02 3F00 \t(61xx,9000)\n"
            );


        sfiTestBuffer.append("\n******** MF SFI CHECKING ******** \n\n");
        sfiTestBuffer.append("; EF ICCID\n");
        if (fileManagement.isSFI_Iccid_2FE2_02_bool())
            sfiTestBuffer.append("%READ_BINARY $ 82 00 0A (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 82 00 0A (9000)\n\n");

        sfiTestBuffer.append("; EF ARR\n");
        if (fileManagement.isSFI_ARR_2F06_06_bool())
            sfiTestBuffer.append("%READ_RECORD $ 01 34 00\n%READ_RECORD $ 01 34 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_RECORD $ 01 34 00\n;%READ_RECORD $ 01 34 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF PL \n");
        if (fileManagement.isSFI_PL_2F05_05_bool())
            sfiTestBuffer.append("%READ_BINARY $ 85 00 00\n%READ_BINARY $ 85 00 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 85 00 00\n;%READ_BINARY $ 85 00 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF DIR \n");
        if (fileManagement.isSFI_Dir_2F00_1E_bool())
            sfiTestBuffer.append("%READ_RECORD $ 01 F4 00\n%READ_RECORD $ 01 F4 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_RECORD $ 01 F4 00\n;%READ_RECORD $ 01 F4 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("******* USIM SFI CHECKING ******* \n\n");
        sfiTestBuffer.append("%SELECT_AID $ <?> %USIM_AID (61xx,9000) \n\n");

//EF_ECC

        sfiTestBuffer.append("; EF LI\n");
        if (fileManagement.isSFI_LI_6F05_02_bool())
            sfiTestBuffer.append("%READ_BINARY $ 82 00 00\n%READ_BINARY $ 82 00 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 82 00 00\n;%READ_BINARY $ 82 00 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF AD\n");
        if (fileManagement.isSFI_AD_6FAD_03_bool())
            sfiTestBuffer.append("%READ_BINARY $ 83 00 00\n%READ_BINARY $ 83 00 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 83 00 00\n;%READ_BINARY $ 83 00 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF UST\n");
        if (fileManagement.isSFI_UST_6F38_04_bool())
            sfiTestBuffer.append("%READ_BINARY $ 84 00 00\n%READ_BINARY $ 84 00 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 84 00 00\n;%READ_BINARY $ 84 00 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF EST\n");
        if (fileManagement.isSFI_EST_6F56_05_bool())
            sfiTestBuffer.append("%READ_BINARY $ 85 00 00\n%READ_BINARY $ 85 00 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 85 00 00\n;%READ_BINARY $ 85 00 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF ACC\n");
        if (fileManagement.isSFI_ACC_6F78_06_bool())
            sfiTestBuffer.append("%READ_BINARY $ 86 00 02 (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 86 00 02 (9000)\n\n");

        sfiTestBuffer.append("; EF IMSI\n");
        if (fileManagement.isSFI_IMSI_6F07_07_bool())
            sfiTestBuffer.append("%READ_BINARY $ 87 00 09 (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 87 00 09 (9000)\n\n");

        sfiTestBuffer.append("; EF KEYS\n");
        if (fileManagement.isSFI_KEYS_6F08_08_bool())
            sfiTestBuffer.append("%READ_BINARY $ 88 00 21 (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 88 00 21 (9000)\n\n");

        sfiTestBuffer.append("; EF KEYSPS\n");
        if (fileManagement.isSFI_KEYSPS_6F09_09_bool())
            sfiTestBuffer.append("%READ_BINARY $ 89 00 21 (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 89 00 21 (9000)\n\n");

        sfiTestBuffer.append("; EF PLMNwACT **\n");
        if (fileManagement.isSFI_PLMNwACT_6F60_0A_bool())
            sfiTestBuffer.append("%READ_BINARY $ 8A 00 00\n%READ_BINARY $ 8A 00 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 8A 00 00\n;%READ_BINARY $ 8A 00 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF LOCI\n");
        if (fileManagement.isSFI_LOCI_6F7E_0B_bool())
            sfiTestBuffer.append("%READ_BINARY $ 8B 00 0B (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 8B 00 0B (9000)\n\n");

        sfiTestBuffer.append("; EF PSLOCI\n");
        if (fileManagement.isSFI_PSLOCI_6F73_0C_bool())
            sfiTestBuffer.append("%READ_BINARY $ 8C 00 0E (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 8C 00 0E (9000)\n\n");

        sfiTestBuffer.append("; EF FPLMN\n");
        if (fileManagement.isSFI_FPLMN_6F7B_0D_bool())
            sfiTestBuffer.append("%READ_BINARY $ 8D 00 00\n%READ_BINARY $ 8D 00 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 8D 00 00\n;%READ_BINARY $ 8D 00 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF CBMID\n");
        if (fileManagement.isSFI_CBMID_6F48_0E_bool())
            sfiTestBuffer.append("%READ_BINARY $ 8E 00 00\n%READ_BINARY $ 8E 00 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 8E 00 00\n;%READ_BINARY $ 8E 00 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF START-HFN\n");
        if (fileManagement.isSFI_StartHFN_6F5B_0F_bool())
            sfiTestBuffer.append("%READ_BINARY $ 8F 00 06 (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 8F 00 06 (9000)\n\n");

        sfiTestBuffer.append("; EF THRESHOLD\n");
        if (fileManagement.isSFI_TRESHOLD_6F5C_10_bool())
            sfiTestBuffer.append("%READ_BINARY $ 90 00 03 (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 90 00 03 (9000)\n\n");

        sfiTestBuffer.append("; EF OPLMNwACT\n");
        if (fileManagement.isSFI_OPLMNwACT_6F61_11_bool())
            sfiTestBuffer.append("%READ_BINARY $ 91 00 00\n%READ_BINARY $ 91 00 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 91 00 00\n;%READ_BINARY $ 91 00 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF HPPLMN\n");
        if (fileManagement.isSFI_HPPLMN_6F31_12_bool())
            sfiTestBuffer.append("%READ_BINARY $ 92 00 01 (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 92 00 01 (9000)\n\n");

        sfiTestBuffer.append("; EF HPLMNwACT\n");
        if (fileManagement.isSFI_HPLMNwACT_6F62_13_bool())
            sfiTestBuffer.append("%READ_BINARY $ 93 00 00\n%READ_BINARY $ 93 00 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 93 00 00\n;%READ_BINARY $ 93 00 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF ICI\n");
        if (fileManagement.isSFI_ICI_6F80_14_bool())
            sfiTestBuffer.append("%READ_RECORD $ 01 A2 00\n%READ_RECORD $ 01 A2 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_RECORD $ 01 A2 00\n;%READ_RECORD $ 01 A2 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF OCI\n");
        if (fileManagement.isSFI_OCI_6F81_15_bool())
            sfiTestBuffer.append("%READ_RECORD $ 01 AA 00\n%READ_RECORD $ 01 AA W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_RECORD $ 01 AA 00\n;%READ_RECORD $ 01 AA W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF CCP2\n");
        if (fileManagement.isSFI_CCP2_6F4F_16_bool())
            sfiTestBuffer.append("%READ_RECORD $ 01 B4 00\n%READ_RECORD $ 01 B4 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_RECORD $ 01 B4 00\n;%READ_RECORD $ 01 B4 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF ARR\n");
        if (fileManagement.isSFI_ARR_6F06_17_bool())
            sfiTestBuffer.append("%READ_RECORD $ 01 BC 00\n%READ_RECORD $ 01 BC W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_RECORD $ 01 BC 00\n;%READ_RECORD $ 01 BC W(2;1) (9000)\n\n");

//SFI_ePDGIdEm_6F65_18_bool

        sfiTestBuffer.append("; EF PNN\n");
        if (fileManagement.isSFI_PNN_6FC5_19_bool())
            sfiTestBuffer.append("%READ_RECORD $ 01 CC 00\n%READ_RECORD $ 01 CC W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_RECORD $ 01 CC 00\n;%READ_RECORD $ 01 CC W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF OPL\n");
        if (fileManagement.isSFI_OPL_6FC6_1A_bool())
            sfiTestBuffer.append("%READ_RECORD $ 01 D4 00\n%READ_RECORD $ 01 D4 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_RECORD $ 01 D4 00\n;%READ_RECORD $ 01 D4 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF SPDI\n");
        if (fileManagement.isSFI_SPDI_6FCD_1B_bool())
            sfiTestBuffer.append("%READ_BINARY $ 9B 00 00\n%READ_BINARY $ 9B 00 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_BINARY $ 9B 00 00\n;%READ_BINARY $ 9B 00 W(2;1) (9000)\n\n");

        sfiTestBuffer.append("; EF ACM\n");
        if (fileManagement.isSFI_ACM_6F39_1C_bool())
            sfiTestBuffer.append("%READ_RECORD $ 01 E2 00\n%READ_RECORD $ 01 E2 W(2;1) (9000)\n\n");
        else
            sfiTestBuffer.append(";%READ_RECORD $ 01 E2 00\n;%READ_RECORD $ 01 E2 W(2;1) (9000)\n\n");

//Kc
//KcGPRS

        if (root.getRunSettings().getSecretCodes().isPin1disabled())
        {
            sfiTestBuffer.append(
                    ".POWER_ON\n\n"
                            +";Disabled PIN1\n"
                            +"00 26 00 01 08 %"+ root.getRunSettings().getSecretCodes().getGpin() +" (9000)\n\n"
                            +".POWER_OFF\n"
            );
        }

        if (root.getRunSettings().getSecretCodes().isPin2disabled())
        {
            sfiTestBuffer.append(
                    ".POWER_ON\n\n"
                            +";Disabled PIN2\n"
                            +"00 26 00 81 08 %"+ root.getRunSettings().getSecretCodes().getLpin() +"  (9000)\n\n"
                            +".POWER_OFF\n"
            );
        }
        
        return sfiTestBuffer;
    }

}
