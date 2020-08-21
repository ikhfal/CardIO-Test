package com.idemia.tec.jkt.cardiotest.service;

import com.idemia.tec.jkt.cardiotest.controller.RootLayoutController;
import com.idemia.tec.jkt.cardiotest.model.*;
import com.idemia.tec.jkt.cardiotest.response.TestSuiteResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class RunServiceImpl implements RunService {

    static Logger logger = Logger.getLogger(RunServiceImpl.class);

    private StringBuilder optionsBuffer;
    private String scriptsDirectory;
    private int exitVal;

    @Autowired private RootLayoutController root;
    @Autowired private ScriptGeneratorService scriptGenerator;

    @Override public TestSuiteResponse runAll() {
        composeScripts();
        runShellCommand("pcomconsole", root.getRunSettings().getProjectPath() + "\\RunAll.pcom");
        // parse result xml
        TestSuite runAllResult = parseRunAllXml();
        if (exitVal == 0) {
            if (runAllResult != null) {
                logger.info(runAllResult.toJson());
                return new TestSuiteResponse(true, "RunAll executed normally", runAllResult);
            } else return new TestSuiteResponse(false, "Failed parsing RunAll output", null);
        }
        else {
            logger.error("Exit value: " + exitVal);
            logger.error(runAllResult.toJson());
            return new TestSuiteResponse(true, "Some error in execution", runAllResult);
        }
    }

    private void composeScripts() {
        optionsBuffer = new StringBuilder();
        optionsBuffer.append("; Options generated by CardIO Tool on ")
                .append(new Timestamp(System.currentTimeMillis())).append("\n\n");
        StringBuilder runAllBuffer = new StringBuilder();

        createFullStructure();

        // Run All header
        runAllBuffer.append(createRunAllHeader());

        // ATR
        if (root.getRunSettings().getAtr().isIncludeAtr()) runAllBuffer.append(addAtr(root.getRunSettings().getAtr()));

        // custom scripts section 1
        if (root.getRunSettings().getCustomScriptsSection1().size() > 0)
            runAllBuffer.append(addCustomScripts(root.getRunSettings().getCustomScriptsSection1()));

        // authentication
        if (root.getRunSettings().getAuthentication().isIncludeDeltaTest() || root.getRunSettings().getAuthentication().isIncludeSqnMax())
            runAllBuffer.append(addAuthentication(root.getRunSettings().getAuthentication()));

        // custom scripts section 2
        if (root.getRunSettings().getCustomScriptsSection2().size() > 0) {
            runAllBuffer.append(addCustomScripts(root.getRunSettings().getCustomScriptsSection2()));
        }

        // RFM USIM
        if (root.getRunSettings().getRfmUsim().isIncludeRfmUsim() ||
                root.getRunSettings().getRfmUsim().isIncludeRfmUsimUpdateRecord() ||
                root.getRunSettings().getRfmUsim().isIncludeRfmUsimExpandedMode()) {
            runAllBuffer.append(addRfmUsim(root.getRunSettings().getRfmUsim()));
        }

        // RFM GSM
        if (root.getRunSettings().getRfmGsm().isIncludeRfmGsm() ||
                root.getRunSettings().getRfmGsm().isIncludeRfmGsmUpdateRecord() ||
                root.getRunSettings().getRfmGsm().isIncludeRfmGsmExpandedMode()) {
            runAllBuffer.append(addRfmGsm(root.getRunSettings().getRfmGsm()));
        }

        // RFM ISIM
        if (root.getRunSettings().getRfmIsim().isIncludeRfmIsim() ||
                root.getRunSettings().getRfmIsim().isIncludeRfmIsimUpdateRecord() ||
                root.getRunSettings().getRfmIsim().isIncludeRfmIsimExpandedMode()) {
            runAllBuffer.append(addRfmIsim(root.getRunSettings().getRfmIsim()));
        }

        // custom scripts section 3
        if (root.getRunSettings().getCustomScriptsSection3().size() > 0) {
            runAllBuffer.append(addCustomScripts(root.getRunSettings().getCustomScriptsSection3()));
        }

        // secret codes
        if (root.getRunSettings().getSecretCodes().isInclude3gScript() || root.getRunSettings().getSecretCodes().isInclude2gScript())
            runAllBuffer.append(addSecretCodes(root.getRunSettings().getSecretCodes()));

        runAllBuffer.append(endRunAll());

        // save mappings to file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "Mapping.txt"))) {
            bw.append(scriptGenerator.generateMapping());
        }
        catch (IOException e) { logger.error("Failed writing mapping file"); }

        // save options to file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "Options.txt"))) {
            bw.append(optionsBuffer);
        }
        catch (IOException e) { logger.error("Failed writing options file"); }

        // save runall to file
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(root.getRunSettings().getProjectPath() + "\\RunAll.pcom"))) {
            bw.append(runAllBuffer);
        }
        catch (IOException e) { logger.error(e.getMessage()); }
    }

    private void createFullStructure() {
        // create folder 'scripts'
        scriptsDirectory = root.getRunSettings().getProjectPath() + "\\scripts\\";
        File scriptDir = new File(scriptsDirectory);
        scriptDir.mkdir();

        // copy DLLs
        File dllDir = new File(scriptsDirectory + "dll\\");
        if (!dllDir.exists()) {
            logger.info("Copy DLLs to " + scriptsDirectory);
            dllDir.mkdir();
            try (Stream<Path> walk = Files.walk(Paths.get("dll"))) {
                List<String> dllFiles = walk.filter(Files::isRegularFile).map(Path::toString).collect(Collectors.toList());
                for (String dll : dllFiles) {
                    File sourceDll = new File(dll);
                    File targetDll = new File(scriptsDirectory + dll);
                    Files.copy(sourceDll.toPath(), targetDll.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            catch (IOException e) { e.printStackTrace(); }
        }
        // copy variable file to project directory
        File sourceVarFile = new File(root.getRunSettings().getAdvSaveVariablesPath());
        File targetVarFile = new File(root.getRunSettings().getProjectPath() + "\\variables.txt");
        try { Files.copy(sourceVarFile.toPath(), targetVarFile.toPath(), StandardCopyOption.REPLACE_EXISTING); }
        catch (IOException e) { logger.error(e.getMessage() + " - failed copying variable file to project directory"); }
    }

    private String createRunAllHeader() {
        return "; Generated by CardIO Tool on " + new Timestamp(System.currentTimeMillis()) + "\n\n"
                + "; EP-ID: " + root.getRunSettings().getRequestId() + "\n"
                + "; Request name: " + root.getRunSettings().getRequestName() + "\n"
                + "; Profile name: " + root.getRunSettings().getProfileName() + "\n"
                + "; Version: " + root.getRunSettings().getProfileVersion() + "\n"
                + "; Image Item ID: " + root.getRunSettings().getCardImageItemId() + "\n"
                + "; Customer: " + root.getRunSettings().getCustomer() + "\n"
                + "; Developer: " + root.getRunSettings().getDeveloperName() + "\n"
                + "; Tester: " + root.getRunSettings().getTesterName() + "\n\n"
                + ".LIBRARY_PATH /ADD logs\n"
                + ".LIBRARY_PATH /DISP\n\n";
    }

    private String endRunAll() {
        return ".LIBRARY_PATH /DEL";
    }

    private String addAtr(ATR atr) {
        // add ATR to options
        optionsBuffer.append(
            "; card parameters\n"
            + ".DEFINE %ATR " + atr.getAtrString() + "\n"
            + "; TCK: " + atr.getTck() + "\n\n"
        );
        if (!root.getRunSettings().getCardParameters().getCardManagerAid().equals(""))
            optionsBuffer.append(".DEFINE %CARD_MANAGER_AID " + root.getRunSettings().getCardParameters().getCardManagerAid() + "\n\n");
        if (!root.getRunSettings().getCardParameters().getUsimAid().equals(""))
            optionsBuffer.append(".DEFINE %USIM_AID " + root.getRunSettings().getCardParameters().getUsimAid() + "\n");
        if (!root.getRunSettings().getCardParameters().getDfUsim().equals(""))
            optionsBuffer.append(".DEFINE %DF_USIM " + root.getRunSettings().getCardParameters().getDfUsim() + "\n\n");
        if (!root.getRunSettings().getCardParameters().getDfGsmAccess().equals(""))
            optionsBuffer.append(".DEFINE %DF_GSM_AC " + root.getRunSettings().getCardParameters().getDfGsmAccess() + "\n");
        if (!root.getRunSettings().getCardParameters().getDfTelecom().equals(""))
            optionsBuffer.append(".DEFINE %DF_TELECOM " + root.getRunSettings().getCardParameters().getDfTelecom() + "\n\n");
        if (!root.getRunSettings().getCardParameters().getIsimAid().equals(""))
            optionsBuffer.append(".DEFINE %ISIM_AID " + root.getRunSettings().getCardParameters().getIsimAid() + "\n");
        if (!root.getRunSettings().getCardParameters().getDfIsim().equals(""))
            optionsBuffer.append(".DEFINE %DF_ISIM " + root.getRunSettings().getCardParameters().getDfIsim() + "\n\n");
        if (!root.getRunSettings().getCardParameters().getCsimAid().equals(""))
            optionsBuffer.append(".DEFINE %CSIM_AID " + root.getRunSettings().getCardParameters().getCsimAid() + "\n");
        if (!root.getRunSettings().getCardParameters().getDfCsim().equals(""))
            optionsBuffer.append(".DEFINE %DF_CSIM " + root.getRunSettings().getCardParameters().getDfCsim() + "\n\n");

        // add ATR script to structure
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "ATR.txt"))) {
            bw.append(scriptGenerator.generateAtr());
        }
        catch (IOException e) { logger.error("Failed writing ATR script"); }

        return "; ATR\n"
                + ".EXECUTE scripts\\ATR.txt /PATH logs\n"
                + ".ALLUNDEFINE\n\n";
    }

    private String addAuthentication(Authentication authentication) {
        // add Authentication to options
        String optionAuth = "; authentication\n"
                + "; algo parameters:\n"
                + ".SET_BUFFER I %" + authentication.getAkaRi() + "\n"
                + ".DEFINE %R1 I(1:1)\n"
                + ".DEFINE %R2 I(2;1)\n"
                + ".DEFINE %R3 I(3;1)\n"
                + ".DEFINE %R4 I(4;1)\n"
                + ".DEFINE %R5 I(5;1)\n";
        optionsBuffer.append(optionAuth);

        StringBuilder authRunAllString = new StringBuilder();
        authRunAllString.append("; Authentication\n");

        // add authentication script to structure
        if (authentication.isIncludeDeltaTest()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "Authentication_MILLENAGE_DELTA_TEST.txt"))) {
                bw.append(scriptGenerator.generateMilenageDeltaTest(authentication));
            }
            catch (IOException e) { logger.error("Failed writing MILLENAGE_DELTA_TEST script"); }
            authRunAllString.append(".EXECUTE scripts\\Authentication_MILLENAGE_DELTA_TEST.txt /PATH logs\n");
            authRunAllString.append(".ALLUNDEFINE\n\n");
        }

        if (authentication.isIncludeSqnMax()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "Authentication_MILLENAGE_SQN_MAX.txt"))) {
                bw.append(scriptGenerator.generateMilenageSqnMax(authentication));
            }
            catch (IOException e) { logger.error("Failed writing MILLENAGE_SQN_MAX script"); }
            authRunAllString.append(".EXECUTE scripts\\Authentication_MILLENAGE_SQN_MAX.txt /PATH logs\n");
            authRunAllString.append(".ALLUNDEFINE\n\n");
        }

        return authRunAllString.toString();
    }

    private String addRfmUsim(RfmUsim rfmUsim) {
        StringBuilder rfmUsimRunAllString = new StringBuilder();
        rfmUsimRunAllString.append("; RFM USIM\n");

        // add RFM USIM script to structure

        if (rfmUsim.isIncludeRfmUsim()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "RFM_USIM.txt"))) {
                bw.append(scriptGenerator.generateRfmUsim(rfmUsim));
            }
            catch (IOException e) { logger.error("Failed writing RFM_USIM script"); }
            rfmUsimRunAllString.append(".EXECUTE scripts\\RFM_USIM.txt /PATH logs\n");
            rfmUsimRunAllString.append(".ALLUNDEFINE\n\n");
        }

        if (rfmUsim.isIncludeRfmUsimUpdateRecord()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "RFM_USIM_UpdateRecord.txt"))) {
                bw.append(scriptGenerator.generateRfmUsimUpdateRecord(rfmUsim));
            }
            catch (IOException e) { logger.error("Failed writing RFM_USIM_UpdateRecord script"); }
            rfmUsimRunAllString.append(".EXECUTE scripts\\RFM_USIM_UpdateRecord.txt /PATH logs\n");
            rfmUsimRunAllString.append(".ALLUNDEFINE\n\n");
        }

        if (rfmUsim.isIncludeRfmUsimExpandedMode()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "RFM_USIM_3G_ExpandedMode.txt"))) {
                bw.append(scriptGenerator.generateRfmUsimExpandedMode(rfmUsim));
            }
            catch (IOException e) { logger.error("Failed writing RFM_USIM_3G_ExpandedMode script"); }
            rfmUsimRunAllString.append(".EXECUTE scripts\\RFM_USIM_3G_ExpandedMode.txt /PATH logs\n");
            rfmUsimRunAllString.append(".ALLUNDEFINE\n\n");
        }

        return rfmUsimRunAllString.toString();
    }

    private String addRfmGsm(RfmGsm rfmGsm) {
        // TODO: options buffer (if required)

        StringBuilder rfmGsmRunAllString = new StringBuilder();
        rfmGsmRunAllString.append("; RFM GSM\n");

        // add RFM GSM script to structure

        if (rfmGsm.isIncludeRfmGsm()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "RFM_GSM.txt"))) {
                bw.append(scriptGenerator.generateRfmGsm(rfmGsm));
            }
            catch (IOException e) { logger.error("Failed writing RFM_GSM script"); }
            rfmGsmRunAllString.append(".EXECUTE scripts\\RFM_GSM.txt /PATH logs\n");
            rfmGsmRunAllString.append(".ALLUNDEFINE\n\n");
        }

        if (rfmGsm.isIncludeRfmGsmUpdateRecord()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "RFM_GSM_UpdateRecord.txt"))) {
                bw.append(scriptGenerator.generateRfmGsmUpdateRecord(rfmGsm));
            }
            catch (IOException e) { logger.error("Failed writing RFM_GSM_UpdateRecord script"); }
            rfmGsmRunAllString.append(".EXECUTE scripts\\RFM_GSM_UpdateRecord.txt /PATH logs\n");
            rfmGsmRunAllString.append(".ALLUNDEFINE\n\n");
        }

        if (rfmGsm.isIncludeRfmGsmExpandedMode()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "RFM_GSM_3G_ExpandedMode.txt"))) {
                bw.append(scriptGenerator.generateRfmGsmExpandedMode(rfmGsm));
            }
            catch (IOException e) { logger.error("Failed writing RFM_GSM_3G_ExpandedMode script"); }
            rfmGsmRunAllString.append(".EXECUTE scripts\\RFM_GSM_3G_ExpandedMode.txt /PATH logs\n");
            rfmGsmRunAllString.append(".ALLUNDEFINE\n\n");
        }

        return rfmGsmRunAllString.toString();
    }

    private String addRfmIsim(RfmIsim rfmIsim) {
        // TODO: options buffer (if required)

        StringBuilder rfmIsimRunAllString = new StringBuilder();
        rfmIsimRunAllString.append("; RFM ISIM\n");

        // add RFM ISIM script to structure

        if (rfmIsim.isIncludeRfmIsim()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "RFM_ISIM.txt"))) {
                bw.append(scriptGenerator.generateRfmIsim(rfmIsim));
            }
            catch (IOException e) { logger.error("Failed writing RFM_ISIM script"); }
            rfmIsimRunAllString.append(".EXECUTE scripts\\RFM_ISIM.txt /PATH logs\n");
            rfmIsimRunAllString.append(".ALLUNDEFINE\n\n");
        }

        if (rfmIsim.isIncludeRfmIsimUpdateRecord()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "RFM_ISIM_UpdateRecord.txt"))) {
                bw.append(scriptGenerator.generateRfmIsimUpdateRecord(rfmIsim));
            }
            catch (IOException e) { logger.error("Failed writing RFM_ISIM_UpdateRecord script"); }
            rfmIsimRunAllString.append(".EXECUTE scripts\\RFM_ISIM_UpdateRecord.txt /PATH logs\n");
            rfmIsimRunAllString.append(".ALLUNDEFINE\n\n");
        }

        if (rfmIsim.isIncludeRfmIsimExpandedMode()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "RFM_ISIM_3G_ExpandedMode.txt"))) {
                bw.append(scriptGenerator.generateRfmIsimExpandedMode(rfmIsim));
            }
            catch (IOException e) { logger.error("Failed writing RFM_ISIM_3G_ExpandedMode script"); }
            rfmIsimRunAllString.append(".EXECUTE scripts\\RFM_ISIM_3G_ExpandedMode.txt /PATH logs\n");
            rfmIsimRunAllString.append(".ALLUNDEFINE\n\n");
        }

        return rfmIsimRunAllString.toString();
    }

    private String addSecretCodes(SecretCodes secretCodes) {
        StringBuilder secretCodesRunAll = new StringBuilder();
        secretCodesRunAll.append("; Secret Codes\n");

        // add secret codes scripts to structure
        if (secretCodes.isInclude3gScript()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "SecretCodes_3G.txt"))) {
                bw.append(scriptGenerator.generateSecretCodes3g(secretCodes));
            }
            catch (IOException e) { logger.error("Failed writing SecretCodes_3G script"); }
            secretCodesRunAll.append(".EXECUTE scripts\\SecretCodes_3G.txt /PATH logs\n");
            secretCodesRunAll.append(".ALLUNDEFINE\n\n");
        }

        if (secretCodes.isInclude2gScript()) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(scriptsDirectory + "SecretCodes_2G.txt"))) {
                bw.append(scriptGenerator.generateSecretCodes2g(secretCodes));
            }
            catch (IOException e) { logger.error("Failed writing SecretCodes_2G script"); }
            secretCodesRunAll.append(".EXECUTE scripts\\SecretCodes_2G.txt /PATH logs\n");
            secretCodesRunAll.append(".ALLUNDEFINE\n\n");
        }

        return secretCodesRunAll.toString();
    }

    private String addCustomScripts(List<CustomScript> customScripts) {
        StringBuilder csRunAllString = new StringBuilder();
        for (CustomScript cScript : customScripts) {
            csRunAllString.append("; " + cScript.getDescription() + "\n");
            csRunAllString.append(".EXECUTE scripts\\" + cScript.getCustomScriptName() + " /PATH logs\n");
            csRunAllString.append(".ALLUNDEFINE\n\n");
        }
        return csRunAllString.toString();
    }

    private TestSuite parseRunAllXml() {
        File runXml = new File(root.getRunSettings().getProjectPath()+ "\\RunAll_L00.xml");
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TestSuite.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return (TestSuite) unmarshaller.unmarshal(runXml);
        }
        catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void launchProcess(List<String> cmdArray) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(cmdArray);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        exitVal = process.waitFor();
    }

    private void runShellCommand(String pcomExecutable, String scriptName) {
        String readerName = "";
        try {
            List<CardTerminal> terminals = root.getTerminalFactory().terminals().list();
            readerName = terminals.get(root.getRunSettings().getReaderNumber()).getName();
        }
        catch (CardException e) { logger.error(e.getMessage()); }
        List<String> cmdArray = new ArrayList<>();
        cmdArray.add(pcomExecutable);
        cmdArray.add("-script");
        cmdArray.add(scriptName);
        cmdArray.add("-xmllogpath");
        cmdArray.add(".");
        cmdArray.add("-logpath");
        cmdArray.add(".");
        cmdArray.add("-reader");
        cmdArray.add(readerName);
        cmdArray.add("-protocol");
        cmdArray.add("TPDUMode");
        if (root.getRunSettings().isStopOnError()) cmdArray.add("-stoponerror");
        try {
            logger.info("Executing.. " + scriptName);
            launchProcess(cmdArray);
        }
        catch (IOException | InterruptedException e) { e.printStackTrace(); }
    }

    @Override public boolean runAtr() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "ATR.txt");
        return exitVal == 0;
    }

    @Override public boolean runDeltaTest() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "Authentication_MILLENAGE_DELTA_TEST.txt");
        return exitVal == 0;
    }

    @Override public boolean runSqnMax() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "Authentication_MILLENAGE_SQN_MAX.txt");
        return exitVal == 0;
    }

    @Override public boolean runRfmUsim() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "RFM_USIM.txt");
        return exitVal == 0;
    }

    @Override public boolean runRfmUsimUpdateRecord() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "RFM_USIM_UpdateRecord.txt");
        return exitVal == 0;
    }

    @Override public boolean runRfmUsimExpandedMode() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "RFM_USIM_3G_ExpandedMode.txt");
        return exitVal == 0;
    }

    @Override public boolean runRfmGsm() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "RFM_GSM.txt");
        return exitVal == 0;
    }

    @Override public boolean runRfmGsmUpdateRecord() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "RFM_GSM_UpdateRecord.txt");
        return exitVal == 0;
    }

    @Override public boolean runRfmGsmExpandedMode() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "RFM_GSM_3G_ExpandedMode.txt");
        return exitVal == 0;
    }

    @Override public boolean runRfmIsim() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "RFM_ISIM.txt");
        return exitVal == 0;
    }

    @Override public boolean runRfmIsimUpdateRecord() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "RFM_ISIM_UpdateRecord.txt");
        return exitVal == 0;
    }

    @Override public boolean runRfmIsimExpandedMode() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "RFM_ISIM_3G_ExpandedMode.txt");
        return exitVal == 0;
    }

    @Override public boolean runSecretCodes3g() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "SecretCodes_3G.txt");
        return exitVal == 0;
    }

    @Override public boolean runSecretCodes2g() {
        composeScripts();
        runShellCommand("pcomconsole", scriptsDirectory + "SecretCodes_2G.txt");
        return exitVal == 0;
    }

}
