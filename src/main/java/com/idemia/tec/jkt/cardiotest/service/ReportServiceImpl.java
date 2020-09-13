package com.idemia.tec.jkt.cardiotest.service;

import com.idemia.tec.jkt.cardiotest.model.CustomScript;
import com.idemia.tec.jkt.cardiotest.model.RunSettings;
import com.idemia.tec.jkt.cardiotest.model.SCP80Keyset;
import com.idemia.tec.jkt.cardiotest.model.VariableMapping;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.*;
import java.sql.Timestamp;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    Logger logger = Logger.getLogger(ReportServiceImpl.class);

    private RunSettings runSettings;
    private int testPass;
    private int testFail;
    private boolean testResultOk;

    @Override public void createReportFromSettings(RunSettings runSettings) {
        this.runSettings = runSettings;
        setTestResult(runSettings);
        logger.info("Test OK: " + testPass);
        logger.info("Test NOK: " + testFail);
        if (testResultOk) logger.info("Test result: PASSED");
        else logger.info("Test result: FAILED");
        try {
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream(runSettings.getProjectPath() + "\\RunAll.pdf"));
            document.open();
            XMLWorkerHelper.getInstance().parseXHtml(writer, document, new StringReader(composeHtml().toString()));
            document.close();
        }
        catch (DocumentException | IOException e) { e.printStackTrace(); }
    }

    private StringBuilder composeHtml() {
        StringBuilder html = new StringBuilder();
        html.append(createDocumentHeader());
        html.append("\n<div><h1>TEC Testing Report</h1></div>");
        // project details
        html.append(
            "\n<div><h2>Project Details</h2></div>"
            + createTableHeaderModule()
            + "\n<tr><td class=\"item\">Request ID</td>"
            + "<td>" + runSettings.getRequestId() + "</td></tr>"
            + "\n<tr><td class=\"item\">Request name</td>"
            + "<td>" + runSettings.getRequestName() + "</td></tr>"
            + "\n<tr><td class=\"item\">Profile name</td>"
            + "<td>" + runSettings.getProfileName() + "</td></tr>"
            + "\n<tr><td class=\"item\">Profile version</td>"
            + "<td>" + runSettings.getProfileVersion() + "</td></tr>"
            + "\n<tr><td class=\"item\">Image item ID</td>"
            + "<td>" + runSettings.getCardImageItemId() + "</td></tr>"
            + "\n<tr><td class=\"item\">Customer</td>"
            + "<td>" + runSettings.getCustomer() + "</td></tr>"
            + "\n<tr><td class=\"item\">Developer</td>"
            + "<td>" + runSettings.getDeveloperName() + "</td></tr>"
            + "\n<tr><td class=\"item\">Tester</td>"
            + "<td>" + runSettings.getTesterName() + "</td></tr>"
            + createTableFooter()
        );

        // test summary
        html.append(
            "\n<div><h2>Test Summary</h2></div>"
            + createTableHeaderModule()
            + "\n<tr><td class=\"item\">Test modules</td>"
            + "<td>" + Integer.toString(testPass + testFail) + "</td></tr>"
            + "\n<tr><td class=\"item\">OK</td>"
            + "<td>" + testPass + "</td></tr>"
            + "\n<tr><td class=\"item\">NOK</td>"
            + "<td>" + testFail + "</td></tr>"
            + "\n<tr><td class=\"item\">Test result</td>"
        );
        if (testResultOk) html.append("<td class=\"ok\">PASSED</td></tr>");
        else html.append("<td class=\"error\">FAILED</td></tr>");
        html.append(
            "\n<tr><td class=\"item\">Completed on</td>"
            + "<td>" + new Timestamp(System.currentTimeMillis()) + "</td></tr>"
            + createTableFooter()
        );

        // testing configurations
        html.append("\n<div><h2>Testing Configurations</h2></div>");

        // variable mappings
        html.append("\n<div><h3>Variable Mappings</h3></div>");
        html.append(createTableHeaderModule());
        html.append(
            "\n<tr><th class=\"item\">Mapped variable</th>"
            + "<th>Value / MCC variable</th></tr>"
        );
        for (VariableMapping mapping : runSettings.getVariableMappings()) {
            html.append("\n<tr><td class=\"item\">" + mapping.getMappedVariable() + "</td>");
            if (mapping.isFixed())
                html.append("<td>" + mapping.getValue() + "</td></tr>");
            else
                html.append("<td>%" + mapping.getMccVariable() + "</td></tr>");
        }
        html.append(createTableFooter());

        // OTA settings
        html.append("\n<div><h3>SCP-80 Keysets</h3></div>");
        for (SCP80Keyset keyset : runSettings.getScp80Keysets()) {
            html.append("\n<div><h4>" + keyset.getKeysetName() + "</h4></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">Version</td>"
                + "<td>" + keyset.getKeysetVersion() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Type</td>"
                + "<td>" + keyset.getKeysetType() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">KIC valuation</td>"
                + "<td>" + getValue(keyset.getKicValuation()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">KIC key length</td>"
                + "<td>" + keyset.getKicKeyLength() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">KIC mode</td>"
                + "<td>" + keyset.getKicMode() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">KID valuation</td>"
                + "<td>" + getValue(keyset.getKidValuation()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">KID key length</td>"
                + "<td>" + keyset.getKidKeyLength() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">KID mode</td>"
                + "<td>" + keyset.getKidMode() + "</td></tr>"
            );
            if (keyset.getKidMode().equals("AES - CMAC")) {
                html.append(
                    "\n<tr><td class=\"item\">CMAC length</td>"
                    + "<td>" + keyset.getCmacLength() + "</td></tr>"
                );
            }
            html.append(createTableFooter());
        }
        html.append("\n<div><h3>Envelope & SMS Update Parameters</h3></div>");
        html.append(createTableHeaderModule());
        html.append(
            "\n<tr><td class=\"item\">UDHI first byte</td>"
            + "<td>" + runSettings.getSmsUpdate().getUdhiFirstByte() + "</td></tr>"
        );
        html.append(
            "\n<tr><td class=\"item\">SC address</td>"
            + "<td>" + runSettings.getSmsUpdate().getScAddress() + "</td></tr>"
        );
        html.append(
            "\n<tr><td class=\"item\">TP-PID</td>"
            + "<td>" + runSettings.getSmsUpdate().getTpPid() + "</td></tr>"
        );
        if (runSettings.getSmsUpdate().isUseWhiteList()) {
            html.append(
                "\n<tr><td class=\"item\">TP-OA</td>"
                + "<td>" + runSettings.getSmsUpdate().getTpOa() + "</td></tr>"
            );
        }
        html.append(
            "\n<tr><td class=\"item\">PoR format</td>"
            + "<td>" + runSettings.getSmsUpdate().getPorFormat() + "</td></tr>"
        );
        html.append(createTableFooter());

        // card parameters
//        html.append("\n<div><h2><b>Card Parameters</b></h2></div>");
//        html.append(createTableHeaderModule());
//        if (!runSettings.getCardParameters().getCardManagerAid().equals(""))
//            html.append(
//                "\n<tr><td>Card manager AID</td>"
//                + "<td>" + runSettings.getCardParameters().getCardManagerAid() + "</td></tr>"
//            );
//        if (!runSettings.getCardParameters().getUsimAid().equals(""))
//            html.append(
//                "\n<tr><td>USIM AID</td>"
//                + "<td>" + runSettings.getCardParameters().getUsimAid() + "</td></tr>"
//            );
//        if (!runSettings.getCardParameters().getDfUsim().equals(""))
//            html.append(
//                "\n<tr><td>DF USIM</td>"
//                + "<td>" + runSettings.getCardParameters().getDfUsim() + "</td></tr>"
//            );
//        if (!runSettings.getCardParameters().getDfGsmAccess().equals(""))
//            html.append(
//                "\n<tr><td>DF GSM Access</td>"
//                + "<td>" + runSettings.getCardParameters().getDfGsmAccess() + "</td></tr>"
//            );
//        if (!runSettings.getCardParameters().getDfTelecom().equals(""))
//            html.append(
//                "\n<tr><td>DF Telecom</td>"
//                + "<td>" + runSettings.getCardParameters().getDfTelecom() + "</td></tr>"
//            );
//        if (!runSettings.getCardParameters().getIsimAid().equals(""))
//            html.append(
//                "\n<tr><td>ISIM AID</td>"
//                + "<td>" + runSettings.getCardParameters().getIsimAid() + "</td></tr>"
//            );
//        if (!runSettings.getCardParameters().getDfIsim().equals(""))
//            html.append(
//                "\n<tr><td>DF ISIM</td>"
//                + "<td>" + runSettings.getCardParameters().getDfIsim() + "</td></tr>"
//            );
//        if (!runSettings.getCardParameters().getCsimAid().equals(""))
//            html.append(
//                "\n<tr><td>CSIM AID</td>"
//                + "<td>" + runSettings.getCardParameters().getCsimAid() + "</td></tr>"
//            );
//        if (!runSettings.getCardParameters().getDfCsim().equals(""))
//            html.append(
//                "\n<tr><td>DF CSIM</td>"
//                + "<td>" + runSettings.getCardParameters().getDfCsim() + "</td></tr>"
//            );
//        html.append(createTableFooter());

        // ATR
        if (runSettings.getAtr().isIncludeAtr()) {
            html.append("\n<div><h2>Answer To Reset</h2></div>");
            html.append(createTableHeaderModule());
            html.append("\n<tr><td class=\"item\">ATR check</td>");
            if (runSettings.getAtr().isTestAtrOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
            else {
                String[] messages = runSettings.getAtr().getTestAtrMesssage().split(";");
                html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
            }
            html.append(
                "\n<tr><td class=\"item\">ATR</td>"
                + "<td>" + runSettings.getAtr().getAtrString() + "</td></tr>"
                + "\n<tr><td class=\"item\">Status</td>"
                + "<td>" + runSettings.getAtr().getStatus() + "</td></tr>"
                + "\n<tr><td class=\"item\">TCK</td>"
                + "<td>" + runSettings.getAtr().getTck() + "</td></tr>"
            );
            html.append(createTableFooter());
        }

        // authentication
        if (runSettings.getAuthentication().isIncludeDeltaTest() || runSettings.getAuthentication().isIncludeSqnMax()) {
            html.append("\n<div><h2>Authentication</h2></div>");
            html.append("\n<div><h3>Test modules</h3></div>");
            html.append(createTableHeaderModule());
            if (runSettings.getAuthentication().isIncludeDeltaTest()) {
                html.append("\n<tr><td class=\"item\">Milenage delta test</td>");
                if (runSettings.getAuthentication().isTestDeltaOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getAuthentication().getTestDeltaMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">Milenage delta test</td><td>(not included)</td></tr>");
            if (runSettings.getAuthentication().isIncludeSqnMax()) {
                html.append("\n<tr><td class=\"item\">Milenage SQN max</td>");
                if (runSettings.getAuthentication().isTestSqnMaxOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getAuthentication().getTestSqnMaxMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">Milenage SQN max</td><td>(not included)</td></tr>");
            html.append(createTableFooter());

            html.append("\n<div><h3>Authentication options</h3></div>");
            html.append(createTableHeaderModule());
            html.append("\n<tr><td class=\"item\">COMP128-2</td>");
            if (runSettings.getAuthentication().isComp1282()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append("\n<tr><td class=\"item\">COMP128-3</td>");
            if (runSettings.getAuthentication().isComp1283()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append("\n<tr><td class=\"item\">Milenage</td>");
            if (runSettings.getAuthentication().isMilenage()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append("\n<tr><td class=\"item\">ISIM auth</td>");
            if (runSettings.getAuthentication().isIsimAuth()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append("\n<tr><td class=\"item\">GSM algo</td>");
            if (runSettings.getAuthentication().isGsmAlgo()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(createTableFooter());

            html.append("\n<div><h3>Algo parameters</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">Authentication AKA - Ci Value 1</td>"
                + "<td>" + getValue(runSettings.getAuthentication().getAkaC1()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Authentication AKA - Ci Value 2</td>"
                + "<td>" + getValue(runSettings.getAuthentication().getAkaC2()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Authentication AKA - Ci Value 3</td>"
                + "<td>" + getValue(runSettings.getAuthentication().getAkaC3()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Authentication AKA - Ci Value 4</td>"
                + "<td>" + getValue(runSettings.getAuthentication().getAkaC4()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Authentication AKA - Ci Value 5</td>"
                + "<td>" + getValue(runSettings.getAuthentication().getAkaC5()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Rotation constants (Ri)</td>"
                + "<td>" + getValue(runSettings.getAuthentication().getAkaRi()) + "</td></tr>"
            );
            html.append(createTableFooter());

            html.append("\n<div><h3>Test parameters</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">Subscriber key (K)</td>"
                + "<td>" + getValue(runSettings.getAuthentication().getKi()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">OPc</td>"
                + "<td>" + getValue(runSettings.getAuthentication().getOpc()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Random challenge (RAND)</td>"
                + "<td>" + runSettings.getAuthentication().getRand() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Signed response (RES) length</td>"
                + "<td>" + Integer.parseInt(runSettings.getAuthentication().getResLength()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">SQN init</td>"
                + "<td>" + runSettings.getAuthentication().getSqn() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">SQN max</td>"
                + "<td>" + runSettings.getAuthentication().getSqnMax() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Authentication Management Field (AMF)</td>"
                + "<td>" + runSettings.getAuthentication().getAmf() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Delta</td>"
                + "<td>" + runSettings.getAuthentication().getDelta() + "</td></tr>"
            );
            html.append(createTableFooter());
        }

        // RFM USIM
        if (runSettings.getRfmUsim().isIncludeRfmUsim() || runSettings.getRfmUsim().isIncludeRfmUsimUpdateRecord() || runSettings.getRfmUsim().isIncludeRfmUsimExpandedMode()) {
            html.append("\n<div><h2>RFM USIM</h2></div>");
            html.append("\n<div><h3>Test modules</h3></div>");
            html.append(createTableHeaderModule());
            if (runSettings.getRfmUsim().isIncludeRfmUsim()) {
                html.append("\n<tr><td class=\"item\">RFM USIM</td>");
                if (runSettings.getRfmUsim().isTestRfmUsimOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getRfmUsim().getTestRfmUsimMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">RFM USIM</td><td>(not included)</td></tr>");
            if (runSettings.getRfmUsim().isIncludeRfmUsimUpdateRecord()) {
                html.append("\n<tr><td class=\"item\">RFM USIM update record</td>");
                if (runSettings.getRfmUsim().isTestRfmUsimUpdateRecordOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getRfmUsim().getTestRfmUsimUpdateRecordMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">RFM USIM update record</td><td>(not included)</td></tr>");
            if (runSettings.getRfmUsim().isIncludeRfmUsimExpandedMode()) {
                html.append("\n<tr><td class=\"item\">RFM USIM expanded mode</td>");
                if (runSettings.getRfmUsim().isTestRfmUsimExpandedModeOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getRfmUsim().getTestRfmUsimExpandedModeMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">RFM USIM expanded mode</td><td>(not included)</td></tr>");
            html.append(createTableFooter());

            html.append("\n<div><h3>Test parameters</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">TAR</td>"
                + "<td>" + runSettings.getRfmUsim().getTar() + "</td></tr>"
            );
            if (runSettings.getRfmUsim().isFullAccess()) {
                html.append(
                    "\n<tr><td class=\"item\">Target EF</td>"
                    + "<td>" + runSettings.getRfmUsim().getTargetEf() + "</td></tr>"
                );
            }
            else {
                html.append(
                    "\n<tr><td class=\"item\">Target EF</td>"
                    + "<td>" + runSettings.getRfmUsim().getCustomTargetEf() + "&nbsp;(" + runSettings.getRfmUsim().getCustomTargetAcc() + ")</td></tr>"
                    + "\n<tr><td class=\"item\">Target EF (negative case)</td>"
                    + "<td>" + runSettings.getRfmUsim().getCustomTargetEfBadCase() + "&nbsp;(" + runSettings.getRfmUsim().getCustomTargetAccBadCase() + ")</td></tr>"
                );
            }
            if (runSettings.getRfmUsim().isUseSpecificKeyset()) {
                html.append(
                    "\n<tr><td class=\"item\">Specific cipher keyset</td>"
                    + "<td>" + runSettings.getRfmUsim().getCipheringKeyset().getKeysetName() + "</td></tr>"
                    + "\n<tr><td class=\"item\">Specific auth keyset</td>"
                    + "<td>" + runSettings.getRfmUsim().getAuthKeyset().getKeysetName() + "</td></tr>"
                );
            }
            html.append(createTableFooter());

            html.append("\n<div><h3>Minimum Security Level</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">Computed MSL</td>"
                + "<td>" + runSettings.getRfmUsim().getMinimumSecurityLevel().getComputedMsl() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Use cipher</td>");
            if (runSettings.getRfmUsim().getMinimumSecurityLevel().isUseCipher()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(
                "\n<tr><td class=\"item\">Cipher algo</td>"
                + "<td>" + runSettings.getRfmUsim().getMinimumSecurityLevel().getCipherAlgo() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Auth verification</td>"
                + "<td>" + runSettings.getRfmUsim().getMinimumSecurityLevel().getAuthVerification() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Signing algo</td>"
                + "<td>" + runSettings.getRfmUsim().getMinimumSecurityLevel().getSigningAlgo() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Counter checking</td>"
                + "<td>" + runSettings.getRfmUsim().getMinimumSecurityLevel().getCounterChecking() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">PoR requirement</td>"
                + "<td>" + runSettings.getRfmUsim().getMinimumSecurityLevel().getPorRequirement() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">PoR security</td>"
                + "<td>" + runSettings.getRfmUsim().getMinimumSecurityLevel().getPorSecurity() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Cipher PoR</td>");
            if (runSettings.getRfmUsim().getMinimumSecurityLevel().isCipherPor()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(createTableFooter());
        }

        // RFM GSM
        if (runSettings.getRfmGsm().isIncludeRfmGsm() || runSettings.getRfmGsm().isIncludeRfmGsmUpdateRecord() || runSettings.getRfmGsm().isIncludeRfmGsmExpandedMode()) {
            html.append("\n<div><h2>RFM GSM</h2></div>");
            html.append("\n<div><h3>Test modules</h3></div>");
            html.append(createTableHeaderModule());
            if (runSettings.getRfmGsm().isIncludeRfmGsm()) {
                html.append("\n<tr><td class=\"item\">RFM GSM</td>");
                if (runSettings.getRfmGsm().isTestRfmGsmOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getRfmGsm().getTestRfmGsmMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">RFM GSM</td><td>(not included)</td></tr>");
            if (runSettings.getRfmGsm().isIncludeRfmGsmUpdateRecord()) {
                html.append("\n<tr><td class=\"item\">RFM GSM update record</td>");
                if (runSettings.getRfmGsm().isTestRfmGsmUpdateRecordOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getRfmGsm().getTestRfmGsmUpdateRecordMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">RFM GSM update record</td><td>(not included)</td></tr>");
            if (runSettings.getRfmGsm().isIncludeRfmGsmExpandedMode()) {
                html.append("\n<tr><td class=\"item\">RFM GSM expanded mode</td>");
                if (runSettings.getRfmGsm().isTestRfmGsmExpandedModeOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getRfmGsm().getTestRfmGsmExpandedModeMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">RFM GSM expanded mode</td><td>(not included)</td></tr>");
            html.append(createTableFooter());

            html.append("\n<div><h3>Test parameters</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">TAR</td>"
                + "<td>" + runSettings.getRfmGsm().getTar() + "</td></tr>"
            );
            if (runSettings.getRfmGsm().isFullAccess()) {
                html.append(
                    "\n<tr><td class=\"item\">Target EF</td>"
                    + "<td>" + runSettings.getRfmGsm().getTargetEf() + "</td></tr>"
                );
            }
            else {
                html.append(
                    "\n<tr><td class=\"item\">Target EF</td>"
                    + "<td>" + runSettings.getRfmGsm().getCustomTargetEf() + "&nbsp;(" + runSettings.getRfmGsm().getCustomTargetAcc() + ")</td></tr>"
                    + "\n<tr><td class=\"item\">Target EF (negative case)</td>"
                    + "<td>" + runSettings.getRfmGsm().getCustomTargetEfBadCase() + "&nbsp;(" + runSettings.getRfmGsm().getCustomTargetAccBadCase() + ")</td></tr>"
                );
            }
            if (runSettings.getRfmGsm().isUseSpecificKeyset()) {
                html.append(
                    "\n<tr><td class=\"item\">Specific cipher keyset</td>"
                    + "<td>" + runSettings.getRfmGsm().getCipheringKeyset().getKeysetName() + "</td></tr>"
                    + "\n<tr><td class=\"item\">Specific auth keyset</td>"
                    + "<td>" + runSettings.getRfmGsm().getAuthKeyset().getKeysetName() + "</td></tr>"
                );
            }
            html.append(createTableFooter());

            html.append("\n<div><h3>Minimum Security Level</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">Computed MSL</td>"
                + "<td>" + runSettings.getRfmGsm().getMinimumSecurityLevel().getComputedMsl() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Use cipher</td>");
            if (runSettings.getRfmGsm().getMinimumSecurityLevel().isUseCipher()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(
                "\n<tr><td class=\"item\">Cipher algo</td>"
                + "<td>" + runSettings.getRfmGsm().getMinimumSecurityLevel().getCipherAlgo() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Auth verification</td>"
                + "<td>" + runSettings.getRfmGsm().getMinimumSecurityLevel().getAuthVerification() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Signing algo</td>"
                + "<td>" + runSettings.getRfmGsm().getMinimumSecurityLevel().getSigningAlgo() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Counter checking</td>"
                + "<td>" + runSettings.getRfmGsm().getMinimumSecurityLevel().getCounterChecking() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">PoR requirement</td>"
                + "<td>" + runSettings.getRfmGsm().getMinimumSecurityLevel().getPorRequirement() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">PoR security</td>"
                + "<td>" + runSettings.getRfmGsm().getMinimumSecurityLevel().getPorSecurity() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Cipher PoR</td>");
            if (runSettings.getRfmGsm().getMinimumSecurityLevel().isCipherPor()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(createTableFooter());
        }

        // RFM ISIM
        if (runSettings.getRfmIsim().isIncludeRfmIsim() || runSettings.getRfmIsim().isIncludeRfmIsimUpdateRecord() || runSettings.getRfmIsim().isIncludeRfmIsimExpandedMode()) {
            html.append("\n<div><h2>RFM ISIM</h2></div>");
            html.append("\n<div><h3>Test modules</h3></div>");
            html.append(createTableHeaderModule());
            if (runSettings.getRfmIsim().isIncludeRfmIsim()) {
                html.append("\n<tr><td class=\"item\">RFM ISIM</td>");
                if (runSettings.getRfmIsim().isTestRfmIsimOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getRfmIsim().getTestRfmIsimMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">RFM ISIM</td><td>(not included)</td></tr>");
            if (runSettings.getRfmIsim().isIncludeRfmIsimUpdateRecord()) {
                html.append("\n<tr><td class=\"item\">RFM ISIM update record</td>");
                if (runSettings.getRfmIsim().isTestRfmIsimUpdateRecordOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getRfmIsim().getTestRfmIsimUpdateRecordMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">RFM ISIM update record</td><td>(not included)</td></tr>");
            if (runSettings.getRfmIsim().isIncludeRfmIsimExpandedMode()) {
                html.append("\n<tr><td class=\"item\">RFM ISIM expanded mode</td>");
                if (runSettings.getRfmIsim().isTestRfmIsimExpandedModeOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getRfmIsim().getTestRfmIsimExpandedModeMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">RFM ISIM expanded mode</td><td>(not included)</td></tr>");
            html.append(createTableFooter());

            html.append("\n<div><h3>Test parameters</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">TAR</td>"
                + "<td>" + runSettings.getRfmIsim().getTar() + "</td></tr>"
            );
            if (runSettings.getRfmIsim().isFullAccess()) {
                html.append(
                    "\n<tr><td class=\"item\">Target EF</td>"
                    + "<td>" + runSettings.getRfmIsim().getTargetEf() + "</td></tr>"
                );
            }
            else {
                html.append(
                    "\n<tr><td class=\"item\">Target EF</td>"
                    + "<td>" + runSettings.getRfmIsim().getCustomTargetEf() + "&nbsp;(" + runSettings.getRfmIsim().getCustomTargetAcc() + ")</td></tr>"
                    + "\n<tr><td class=\"item\">Target EF (negative case)</td>"
                    + "<td>" + runSettings.getRfmIsim().getCustomTargetEfBadCase() + "&nbsp;(" + runSettings.getRfmIsim().getCustomTargetAccBadCase() + ")</td></tr>"
                );
            }
            if (runSettings.getRfmIsim().isUseSpecificKeyset()) {
                html.append(
                    "\n<tr><td class=\"item\">Specific cipher keyset</td>"
                    + "<td>" + runSettings.getRfmIsim().getCipheringKeyset().getKeysetName() + "</td></tr>"
                    + "\n<tr><td class=\"item\">Specific auth keyset</td>"
                    + "<td>" + runSettings.getRfmIsim().getAuthKeyset().getKeysetName() + "</td></tr>"
                );
            }
            html.append(createTableFooter());

            html.append("\n<div><h3>Minimum Security Level</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">Computed MSL</td>"
                + "<td>" + runSettings.getRfmIsim().getMinimumSecurityLevel().getComputedMsl() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Use cipher</td>");
            if (runSettings.getRfmIsim().getMinimumSecurityLevel().isUseCipher()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(
                "\n<tr><td class=\"item\">Cipher algo</td>"
                + "<td>" + runSettings.getRfmIsim().getMinimumSecurityLevel().getCipherAlgo() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Auth verification</td>"
                + "<td>" + runSettings.getRfmIsim().getMinimumSecurityLevel().getAuthVerification() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Signing algo</td>"
                + "<td>" + runSettings.getRfmIsim().getMinimumSecurityLevel().getSigningAlgo() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Counter checking</td>"
                + "<td>" + runSettings.getRfmIsim().getMinimumSecurityLevel().getCounterChecking() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">PoR requirement</td>"
                + "<td>" + runSettings.getRfmIsim().getMinimumSecurityLevel().getPorRequirement() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">PoR security</td>"
                + "<td>" + runSettings.getRfmIsim().getMinimumSecurityLevel().getPorSecurity() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Cipher PoR</td>");
            if (runSettings.getRfmIsim().getMinimumSecurityLevel().isCipherPor()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(createTableFooter());
        }

        if (runSettings.getRam().isIncludeRam() || runSettings.getRam().isIncludeRamUpdateRecord() || runSettings.getRam().isIncludeRamExpandedMode()) {
            html.append("\n<div><h2>RAM</h2></div>");
            html.append("\n<div><h3>Test modules</h3></div>");
            html.append(createTableHeaderModule());
            if (runSettings.getRam().isIncludeRam()) {
                html.append("\n<tr><td class=\"item\">RAM</td>");
                if (runSettings.getRam().isTestRamOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getRam().getTestRamMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">RAM</td><td>(not included)</td></tr>");
            if (runSettings.getRam().isIncludeRamUpdateRecord()) {
                html.append("\n<tr><td class=\"item\">RAM update record</td>");
                if (runSettings.getRam().isTestRamUpdateRecordOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getRam().getTestRamUpdateRecordMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">RAM update record</td><td>(not included)</td></tr>");
            if (runSettings.getRam().isIncludeRamExpandedMode()) {
                html.append("\n<tr><td class=\"item\">RAM expanded mode</td>");
                if (runSettings.getRam().isTestRamExpandedModeOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getRam().getTestRamExpandedModeMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">RAM expanded mode</td><td>(not included)</td></tr>");
            html.append(createTableFooter());

            html.append("\n<div><h3>Test parameters</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">TAR</td>"
                + "<td>" + runSettings.getRam().getTar() + "</td></tr>"
            );
            if (runSettings.getRam().isUseSpecificKeyset()) {
                html.append(
                    "\n<tr><td class=\"item\">Specific cipher keyset</td>"
                    + "<td>" + runSettings.getRam().getCipheringKeyset().getKeysetName() + "</td></tr>"
                    + "\n<tr><td class=\"item\">Specific auth keyset</td>"
                    + "<td>" + runSettings.getRam().getAuthKeyset().getKeysetName() + "</td></tr>"
                );
            }
            html.append(createTableFooter());

            html.append("\n<div><h3>ISD settings</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">Method for GP command</td>"
                + "<td>" + runSettings.getRam().getIsd().getMethodForGpCommand() + "</td></tr>"
                + "\n<tr><td class=\"item\">SCP mode</td>"
                + "<td>" + runSettings.getRam().getIsd().getScpMode() + "</td></tr>"
                + "\n<tr><td class=\"item\">SC level</td>"
                + "<td>" + runSettings.getRam().getIsd().getScLevel() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Secured state</td>");
            if (runSettings.getRam().getIsd().isSecuredState()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            if (runSettings.getRam().getIsd().getMethodForGpCommand().equals("with Card Manager Keyset")) {
                html.append(
                    "\n<tr><td class=\"item\">Encryption key (ENC)</td>"
                    + "<td>" + getValue(runSettings.getRam().getIsd().getCardManagerEnc()) + "</td></tr>"
                    + "\n<tr><td class=\"item\">Message Auth Code key (MAC)</td>"
                    + "<td>" + getValue(runSettings.getRam().getIsd().getCardManagerMac()) + "</td></tr>"
                    + "\n<tr><td class=\"item\">Data Encryption key (KEK)</td>"
                    + "<td>" + getValue(runSettings.getRam().getIsd().getCardManagerKey()) + "</td></tr>"
                );
            }
            if (runSettings.getRam().getIsd().getMethodForGpCommand().equals("SIMBiOs")) {
                html.append(
                    "\n<tr><td class=\"item\">Card manager PIN</td>"
                    + "<td>" + getValue(runSettings.getRam().getIsd().getCardManagerPin()) + "</td></tr>"
                );
            }
            html.append(createTableFooter());

            html.append("\n<div><h3>Minimum Security Level</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">Computed MSL</td>"
                + "<td>" + runSettings.getRam().getMinimumSecurityLevel().getComputedMsl() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Use cipher</td>");
            if (runSettings.getRam().getMinimumSecurityLevel().isUseCipher()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(
                "\n<tr><td class=\"item\">Cipher algo</td>"
                + "<td>" + runSettings.getRam().getMinimumSecurityLevel().getCipherAlgo() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Auth verification</td>"
                + "<td>" + runSettings.getRam().getMinimumSecurityLevel().getAuthVerification() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Signing algo</td>"
                + "<td>" + runSettings.getRam().getMinimumSecurityLevel().getSigningAlgo() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Counter checking</td>"
                + "<td>" + runSettings.getRam().getMinimumSecurityLevel().getCounterChecking() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">PoR requirement</td>"
                + "<td>" + runSettings.getRam().getMinimumSecurityLevel().getPorRequirement() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">PoR security</td>"
                + "<td>" + runSettings.getRam().getMinimumSecurityLevel().getPorSecurity() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Cipher PoR</td>");
            if (runSettings.getRam().getMinimumSecurityLevel().isCipherPor()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(createTableFooter());
        }

        // secret codes
        if (runSettings.getSecretCodes().isInclude3gScript() || runSettings.getSecretCodes().isInclude2gScript()) {
            html.append("\n<div><h2>Secret Codes</h2></div>");

            html.append("\n<div><h3>Test modules</h3></div>");
            html.append(createTableHeaderModule());
            if (runSettings.getSecretCodes().isInclude3gScript()) {
                html.append("\n<tr><td class=\"item\">Secret codes 3G</td>");
                if (runSettings.getSecretCodes().isTestCodes3gOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getSecretCodes().getTestCodes3gMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">Secret codes 3G</td><td>(not included)</td></tr>");
            if (runSettings.getSecretCodes().isInclude2gScript()) {
                html.append("\n<tr><td class=\"item\">Secret codes 2G</td>");
                if (runSettings.getSecretCodes().isTestCodes2gOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getSecretCodes().getTestCodes2gMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">Secret codes 2G</td><td>(not included)</td></tr>");
            html.append(createTableFooter());

            html.append("\n<div><h3>Global PIN</h3></div>");
            html.append(createTableHeaderModule());
            html.append("\n<tr><td class=\"item\">PIN1 disabled</td>");
            if (runSettings.getSecretCodes().isPin1disabled()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(
                "\n<tr><td class=\"item\">Value</td>"
                + "<td>" + getValue(runSettings.getSecretCodes().getGpin()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Retries</td>"
                + "<td>" + runSettings.getSecretCodes().getGpinRetries() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Unblock key</td>"
                + "<td>" + getValue(runSettings.getSecretCodes().getGpuk()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Unblock retries</td>"
                + "<td>" + runSettings.getSecretCodes().getGpukRetries() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Block Global PUK</td>");
            if (runSettings.getSecretCodes().isBlockGpuk()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(createTableFooter());

            html.append("\n<div><h3>Local PIN</h3></div>");
            html.append(createTableHeaderModule());
            html.append("\n<tr><td class=\"item\">PIN2 disabled</td>");
            if (runSettings.getSecretCodes().isPin2disabled()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(
                "\n<tr><td class=\"item\">Value</td>"
                + "<td>" + getValue(runSettings.getSecretCodes().getLpin()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Retries</td>"
                + "<td>" + runSettings.getSecretCodes().getLpinRetries() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Unblock key</td>"
                + "<td>" + getValue(runSettings.getSecretCodes().getLpuk()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Unblock retries</td>"
                + "<td>" + runSettings.getSecretCodes().getLpukRetries() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Block Local PUK</td>");
            if (runSettings.getSecretCodes().isBlockLpuk()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(createTableFooter());

            html.append("\n<div><h3>CHV1</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">Value</td>"
                + "<td>" + getValue(runSettings.getSecretCodes().getChv1()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Retries</td>"
                + "<td>" + runSettings.getSecretCodes().getChv1Retries() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Unblock key</td>"
                + "<td>" + getValue(runSettings.getSecretCodes().getPuk1()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Unblock retries</td>"
                + "<td>" + runSettings.getSecretCodes().getPuk1Retries() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Block PUK1</td>");
            if (runSettings.getSecretCodes().isBlockPuk1()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(createTableFooter());

            html.append("\n<div><h3>CHV2</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">Value</td>"
                + "<td>" + getValue(runSettings.getSecretCodes().getChv2()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Retries</td>"
                + "<td>" + runSettings.getSecretCodes().getChv2Retries() + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Unblock key</td>"
                + "<td>" + getValue(runSettings.getSecretCodes().getPuk2()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">Unblock retries</td>"
                + "<td>" + runSettings.getSecretCodes().getPuk2Retries() + "</td></tr>"
            );
            html.append("\n<tr><td class=\"item\">Block PUK2</td>");
            if (runSettings.getSecretCodes().isBlockPuk2()) html.append("<td>YES</td></tr>");
            else html.append("<td>NO</td></tr>");
            html.append(createTableFooter());

            html.append("\n<div><h3>Issuer Secret Codes</h3></div>");
            html.append(createTableHeaderModule());
            html.append(
                "\n<tr><td class=\"item\">Issuer Secret Code 1</td>"
                + "<td>" + getValue(runSettings.getSecretCodes().getIsc1()) + "</td></tr>"
            );
            html.append(
                "\n<tr><td class=\"item\">ISC1 retries</td>"
                + "<td>" + runSettings.getSecretCodes().getIsc1Retries() + "</td></tr>"
            );
            if (runSettings.getSecretCodes().isUseIsc2()) {
                html.append(
                    "\n<tr><td class=\"item\">Issuer Secret Code 2</td>"
                    + "<td>" + getValue(runSettings.getSecretCodes().getIsc2()) + "</td></tr>"
                );
                html.append(
                    "\n<tr><td class=\"item\">ISC2 retries</td>"
                    + "<td>" + runSettings.getSecretCodes().getIsc2Retries() + "</td></tr>"
                );
            }
            if (runSettings.getSecretCodes().isUseIsc3()) {
                html.append(
                    "\n<tr><td class=\"item\">Issuer Secret Code 3</td>"
                    + "<td>" + getValue(runSettings.getSecretCodes().getIsc3()) + "</td></tr>"
                );
                html.append(
                    "\n<tr><td class=\"item\">ISC3 retries</td>"
                    + "<td>" + runSettings.getSecretCodes().getIsc3Retries() + "</td></tr>"
                );
            }
            if (runSettings.getSecretCodes().isUseIsc4()) {
                html.append(
                    "\n<tr><td class=\"item\">Issuer Secret Code 4</td>"
                    + "<td>" + getValue(runSettings.getSecretCodes().getIsc4()) + "</td></tr>"
                );
                html.append(
                    "\n<tr><td class=\"item\">ISC4 retries</td>"
                    + "<td>" + runSettings.getSecretCodes().getIsc4Retries() + "</td></tr>"
                );
            }
            html.append(createTableFooter());
        }
        html.append("\n<div><h2>Other Tests</h2></div>");
        if (runSettings.getCustomScriptsSection1().size() > 0) printCustomScriptsReport(html, runSettings.getCustomScriptsSection1());
        if (runSettings.getCustomScriptsSection2().size() > 0) printCustomScriptsReport(html, runSettings.getCustomScriptsSection2());
        if (runSettings.getCustomScriptsSection3().size() > 0) printCustomScriptsReport(html, runSettings.getCustomScriptsSection3());

        // file management
        if (runSettings.getFileManagement().isIncludeLinkFilesTest() || runSettings.getFileManagement().isIncludeRuwiTest() || runSettings.getFileManagement().isIncludeSfiTest()) {
            html.append("\n<div><h2>File Management</h2></div>");
            html.append("\n<div><h3>Test modules</h3></div>");


            if (runSettings.getFileManagement().isIncludeLinkFilesTest()) {

                html.append(createTableHeaderModule());
                html.append("\n<tr><td class=\"item\">Link File Test</td>");
                if (runSettings.getFileManagement().isTestLinkFilesOk())
                    html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getFileManagement().getTestLinkFilesMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">Link File Test</td><td>(not included)</td></tr>");

            if (runSettings.getFileManagement().isIncludeRuwiTest()) {
                html.append("\n<tr><td class=\"item\">Readable & Updateable when Invalidated</td>");

                if (runSettings.getFileManagement().isTestRuwiOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getFileManagement().getTestRuwiMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }

            }
            else html.append("\n<tr><td class=\"item\">Readable & Updateable when Invalidated</td><td>(not included)</td></tr>");
            
            if (runSettings.getFileManagement().isIncludeSfiTest()) {
                html.append("\n<tr><td class=\"item\">SFI TEST</td>");

                if (runSettings.getFileManagement().isTestSfiOk()) html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getFileManagement().getTestSfiMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }

            }
            else html.append("\n<tr><td class=\"item\">SFI Check</td><td>(not included)</td></tr>");

            html.append(createTableFooter());

        }

        /*
        // HSA
        if (runSettings.getFileManagement2().isIncludeHighStressAreaTest()) {
            html.append("\n<div><h2>File Management 2</h2></div>");
            html.append("\n<div><h3>Test modules</h3></div>");

            if (runSettings.getFileManagement2().isIncludeHighStressAreaTest()) {

                html.append(createTableHeaderModule());
                html.append("\n<tr><td class=\"item\">High Stress Area Test</td>");

                if (runSettings.getFileManagement2().isTestHighStressAreaOk())
                    html.append("<td class=\"ok\">PASSED</td></tr>");
                else {
                    String[] messages = runSettings.getFileManagement2().getTestHighStressAreaMessage().split(";");
                    html.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
                }
            }
            else html.append("\n<tr><td class=\"item\">High Stress Area Test</td><td>(not included)</td></tr>");

            html.append(createTableFooter());

        }
         */


        html.append(createDocumentFooter());
        return html;
    }

    private void printCustomScriptsReport(StringBuilder htmlBuffer, List<CustomScript> customScripts) {
        for (CustomScript customScript : customScripts) {
            htmlBuffer.append("\n<div><h3>" + customScript.getDescription() + "</h3></div>");
            htmlBuffer.append(createTableHeaderModule());
            htmlBuffer.append("\n<tr><td class=\"item\">Test result</td>");
            if (customScript.isRunCustomScriptOk()) htmlBuffer.append("<td class=\"ok\">PASSED</td></tr>");
            else {
                String[] messages = customScript.getRunCustomScriptMessage().split(";");
                htmlBuffer.append("<td class=\"error\">" + String.join("<br/>", messages) + "</td></tr>");
            }
            htmlBuffer.append(
                "\n<tr><td class=\"item\">Script name</td>"
                + "<td>" + customScript.getCustomScriptName() + "</td></tr>"
            );
            htmlBuffer.append(createTableFooter());
        }
    }

    private String createDocumentHeader() {
        StringBuilder header = new StringBuilder();
        header.append(
            "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n" +
            "<html>\n" +
            "<head>\n" +
            "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n" +
            "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\" />\n" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0\" />\n" +
            "<title>Run All Report</title>\n" +
            "<style type=\"text/css\">\n" +
            "html,\n" +
            "body {\n" +
            "\theight: 100%;\n" +
            "}\n" +
            "html {\n" +
            "\tfont-size: 16px;\n" +
            "}\n" +
            "body {\n" +
            "\tmargin: 0px;\n" +
            "\tpadding: 0px;\n" +
            "\toverflow-x: hidden;\n" +
            "\tmin-width: 320px;\n" +
            "\tfont-family: Arial, Helvetica, sans-serif;\n" +
            "\tfont-size: 13px;\n" +
            "\tline-height: 1.33;\n" +
            "\tcolor: #212121;\n" +
            "\tfont-smoothing: antialiased;\n" +
            "}\n" +
            "div {\n" +
            "\tmargin-top: 10px;\n" +
            "\tmargin-left: 40px;\n" +
            "\tmargin-right: 40px;\n" +
            "}\n" +
            "h1,\n" +
            "h2,\n" +
            "h3,\n" +
            "h4,\n" +
            "h5 {\n" +
            "\tfont-family: Arial, Helvetica, sans-serif;\n" +
            "\tline-height: 1.33em;\n" +
            "\tmargin: calc(2rem -  0.165em ) 0em 1rem;\n" +
            "\tfont-weight: 400;\n" +
            "\tpadding: 0em;\n" +
            "}\n" +
            "table {\n" +
            "\tborder-collapse: collapse;\n" +
//            "\twidth: 100%;\n" +
            "\tborder: 2px solid #430099;\n" +
            "}\n" +
            "table.module {\n" +
            "\twidth: 80%;\n" +
            "}\n" +
            "th,\n" +
            "td {\n" +
            "\twidth: 50%;\n" +
            "\ttext-align: left;\n" +
            "\tpadding: 4px;\n" +
//            "\tborder-bottom: 1px solid #ddd;\n" +
            "\tborder-bottom: 1px solid #430099;\n" +
            "}\n" +
            "th.error {\n" +
            "\twidth: 50%;\n" +
            "\tbackground-color: firebrick;\n" +
            "\tcolor: #F9F9F9;\n" +
            "}\n" +
            "th.warning {\n" +
            "\twidth: 50%;\n" +
            "\tbackground-color: darkorange;\n" +
            "\tcolor: #F9F9F9;\n" +
            "}\n" +
//            "tr:hover {background-color: #f5f5f5;}\n" +
            "th.item {\n" +
            "\twidth: 50%;\n" +
            "\tbackground-color: #EEE8FB;\n" +
            "\tcolor: #17202A;\n" +
            "}\n" +
            "td.item {\n" +
            "\twidth: 50%;\n" +
//            "\tfont-weight: bold;\n" +
            "\tbackground-color: #EEE8FB;\n" +
            "\tcolor: #17202A;\n" +
            "}\n" +
            "td.error {\n" +
            "\twidth: 50%;\n" +
            "\tbackground-color: #FDEDEC;\n" +
            "\tcolor: #17202A;\n" +
            "}\n" +
            "td.warning {\n" +
            "\twidth: 50%;\n" +
            "\tbackground-color: #FEF9E7;\n" +
            "\tcolor: #17202A;\n" +
            "}\n" +
            "td.ok {\n" +
            "\twidth: 50%;\n" +
            "\tbackground-color: #EDFBEE;\n" +
            "\tcolor: #17202A;\n" +
            "}" +
            "td.data {\n" +
            "\twidth: 50%;\n" +
            "\tfont-family: consolas, Monaco, monospace;\n" +
            "\tfont-size: 13px;\n" +
            "}\n" +
            "ul {\n" +
            "\tmargin: 0px;\n" +
            "\tpadding: 15px;\n" +
            "}\n" +
            "</style>\n" +
            "</head>\n" +
            "<body>\n"
        );
        return header.toString();
    }

    private String createDocumentFooter() { return "\n<div><i>Created by CARDIO</i></div>\n</body>\n</html>"; }

    private String createTableHeader() { return "\n<div>\n" + "<table>\n" + "<tbody>"; }

    private String createTableHeaderModule() { return "\n<div>\n" + "<table class=\"module\">\n" + "<tbody>"; }

    private String createTableFooter() { return "\n</tbody>\n" + "</table>\n" + "</div>\n<br/>"; }

    private String getValue(String mappedVar) {
        String value = "";
        for (VariableMapping mapping : runSettings.getVariableMappings()) {
            if (mapping.getMappedVariable().equals(mappedVar)) {
                if (mapping.isFixed()) value = mapping.getValue();
                else value = "<i>" + mapping.getMccVariable() + "</i>";
                break;
            }
        }
        return value;
    }

    private void setTestResult(RunSettings runSettings) {
        testPass = 0;
        testFail = 0;
        testResultOk = true;

        if (runSettings.getAtr().isIncludeAtr()) {
            if (runSettings.getAtr().isTestAtrOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getAuthentication().isIncludeDeltaTest()) {
            if (runSettings.getAuthentication().isTestDeltaOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getAuthentication().isIncludeSqnMax()) {
            if (runSettings.getAuthentication().isTestSqnMaxOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRfmUsim().isIncludeRfmUsim()) {
            if (runSettings.getRfmUsim().isTestRfmUsimOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRfmUsim().isIncludeRfmUsimUpdateRecord()) {
            if (runSettings.getRfmUsim().isTestRfmUsimUpdateRecordOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRfmUsim().isIncludeRfmUsimExpandedMode()) {
            if (runSettings.getRfmUsim().isTestRfmUsimExpandedModeOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRfmGsm().isIncludeRfmGsm()) {
            if (runSettings.getRfmGsm().isTestRfmGsmOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRfmGsm().isIncludeRfmGsmUpdateRecord()) {
            if (runSettings.getRfmGsm().isTestRfmGsmUpdateRecordOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRfmGsm().isIncludeRfmGsmExpandedMode()) {
            if (runSettings.getRfmGsm().isTestRfmGsmExpandedModeOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRfmIsim().isIncludeRfmIsim()) {
            if (runSettings.getRfmIsim().isTestRfmIsimOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRfmIsim().isIncludeRfmIsimUpdateRecord()) {
            if (runSettings.getRfmIsim().isTestRfmIsimUpdateRecordOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRfmIsim().isIncludeRfmIsimExpandedMode()) {
            if (runSettings.getRfmIsim().isTestRfmIsimExpandedModeOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRfmCustom().isIncludeRfmCustom()) {
            if (runSettings.getRfmCustom().isTestRfmCustomOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRfmCustom().isIncludeRfmCustomUpdateRecord()) {
            if (runSettings.getRfmCustom().isTestRfmCustomUpdateRecordOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRfmCustom().isIncludeRfmCustomExpandedMode()) {
            if (runSettings.getRfmCustom().isTestRfmCustomExpandedModeOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRam().isIncludeRam()) {
            if (runSettings.getRam().isTestRamOk()) testPass ++;
            else testFail++;
        }
        if (runSettings.getRam().isIncludeRamUpdateRecord()) {
            if (runSettings.getRam().isTestRamUpdateRecordOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getRam().isIncludeRamExpandedMode()) {
            if (runSettings.getRam().isTestRamExpandedModeOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getSecretCodes().isInclude3gScript()) {
            if (runSettings.getSecretCodes().isTestCodes3gOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getSecretCodes().isInclude2gScript()) {
            if (runSettings.getSecretCodes().isTestCodes2gOk()) testPass++;
            else testFail++;
        }
        if (runSettings.getCustomScriptsSection1().size() > 0) {
            for (CustomScript customScript : runSettings.getCustomScriptsSection1()) {
                if (customScript.isRunCustomScriptOk()) testPass++;
                else testFail++;
            }
        }
        if (runSettings.getCustomScriptsSection2().size() > 0) {
            for (CustomScript customScript : runSettings.getCustomScriptsSection2()) {
                if (customScript.isRunCustomScriptOk()) testPass++;
                else testFail++;
            }
        }
        if (runSettings.getCustomScriptsSection3().size() > 0) {
            for (CustomScript customScript : runSettings.getCustomScriptsSection3()) {
                if (customScript.isRunCustomScriptOk()) testPass++;
                else testFail++;
            }
        }
        if (testFail > 0) testResultOk = false;
    }

}
