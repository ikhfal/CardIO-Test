package com.idemia.tec.jkt.cardiotest.controller;

import com.idemia.tec.jkt.cardiotest.CardiotestApplication;
import com.idemia.tec.jkt.cardiotest.model.*;
import com.idemia.tec.jkt.cardiotest.response.TestSuiteResponse;
import com.idemia.tec.jkt.cardiotest.service.CardioConfigService;
import com.idemia.tec.jkt.cardiotest.service.ExportImportService;
import com.idemia.tec.jkt.cardiotest.service.ReportService;
import com.idemia.tec.jkt.cardiotest.service.RunService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import org.apache.log4j.Logger;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.StatusBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.TerminalFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Component
public class RootLayoutController {

    static Logger logger = Logger.getLogger(RootLayoutController.class);

    private RunSettings runSettings;

    private CardiotestApplication application;
    private TerminalFactory terminalFactory;
    private ObservableList<SCP80Keyset> scp80Keysets = FXCollections.observableArrayList();
    private ObservableList<AppletParam> appletParams = FXCollections.observableArrayList();
    private ObservableList<CustomScript> customScriptsSection1 = FXCollections.observableArrayList();
    private ObservableList<CustomScript> customScriptsSection2 = FXCollections.observableArrayList();
    private ObservableList<CustomScript> customScriptsSection3 = FXCollections.observableArrayList();
    private TestSuiteResponse tsResponse;
    private boolean runAtrOk;
    private boolean runDeltaTestOk;
    private boolean runLinkFilesTestOk;
    private boolean runRuwiTestOk;
    private boolean runSfiTestOk;
    private boolean runSqnMaxOk;
    private boolean runRfmUsimOk;
    private boolean runRfmUsimUpdateRecordOk;
    private boolean runRfmUsimExpandedModeOk;
    private boolean runRfmGsmOk;
    private boolean runRfmGsmUpdateRecordOk;
    private boolean runRfmGsmExpandedModeOk;
    private boolean runRfmIsimOk;
    private boolean runRfmIsimUpdateRecordOk;
    private boolean runRfmIsimExpandedModeOk;
    private boolean runRamOk;
    private boolean runRamUpdateRecordOk;
    private boolean runRamExpandedModeOk;
    private boolean runVerifGpOk;
    private boolean runRfmCustomOk;
    private boolean runRfmCustomUpdateRecordOk;
    private boolean runRfmCustomExpandedModeOk;
    private boolean runCodes3gOk;
    private boolean runCodes2gOk;
    private boolean runHighStressAreaTestOk;

    @Autowired private RootLayoutController root;
    @Autowired private CardiotestController cardiotest;
    @Autowired private AuthenticationController authenticationController;
    @Autowired private SecretCodesController secretCodesController;
    @Autowired private RfmGsmController rfmGsmController;
    @Autowired private RfmUsimController rfmUsimController;
    @Autowired private RfmIsimController rfmIsimController;
    @Autowired private RfmCustomController rfmCustomController;
    @Autowired private CustomTabController customTabController;
    @Autowired private RamController ramController;

    @Autowired private CardioConfigService cardioConfigService;
    @Autowired private RunService runService;
    @Autowired private ReportService reportService;
    @Autowired private ExportImportService eximService;
    @Autowired private FileManagementController fileManagementController;
    @Autowired private FileManagement2Controller fileManagement2Controller;

    @FXML private BorderPane rootBorderPane;
    @FXML private MenuBar menuBar;

    @FXML private MenuItem menuAtr;
    @FXML private MenuItem menuDeltaTest;
    @FXML private MenuItem menuSqnMax;
    @FXML private MenuItem menuRfmUsim;
    @FXML private MenuItem menuRfmUsimUpdateRecord;
    @FXML private MenuItem menuRfmUsimExpandedMode;
    @FXML private MenuItem menuRfmGsm;
    @FXML private MenuItem menuRfmGsmUpdateRecord;
    @FXML private MenuItem menuRfmGsmExpandedMode;
    @FXML private MenuItem menuRfmIsim;
    @FXML private MenuItem menuRfmIsimUpdateRecord;
    @FXML private MenuItem menuRfmIsimExpandedMode;
    @FXML private MenuItem menuRfmCustom;
    @FXML private MenuItem menuRfmCustomUpdateRecord;
    @FXML private MenuItem menuRfmCustomExpandedMode;
    @FXML private MenuItem menuRam;
    @FXML private MenuItem menuRamUpdateRecord;
    @FXML private MenuItem menuRamExpandedMode;
    @FXML private MenuItem menuVerifGp;
    @FXML private MenuItem menuCodes3g;
    @FXML private MenuItem menuCodes2g;
    @FXML private MenuItem menuLinkFiles;
    @FXML private MenuItem menuRuwi;
    @FXML private MenuItem menuSfi;
    @FXML private MenuItem menuHighStressArea;

    private StatusBar appStatusBar;
    private Label lblTerminalInfo;

    private File importProjectDir;
    private File importVarFile;

    public RootLayoutController() {}

    public void setMainApp(CardiotestApplication application) { this.application = application; }

    public RunSettings getRunSettings() { return runSettings; }

    @FXML private void initialize() {
        appStatusBar = new StatusBar();
        rootBorderPane.setBottom(appStatusBar);

        // get run settings from 'run-settings.json' or by default values
        runSettings = cardioConfigService.initConfig();

        terminalFactory = TerminalFactory.getDefault();
        lblTerminalInfo = new Label();
        appStatusBar.getRightItems().add(new Separator(Orientation.VERTICAL));
        appStatusBar.getRightItems().add(lblTerminalInfo);
        // list available readers
        try {
            List<CardTerminal> terminals = terminalFactory.terminals().list();
            if (terminals.isEmpty()) lblTerminalInfo.setText("(no terminal/reader detected)");
            else if (runSettings.getReaderNumber() != -1)
                if (runSettings.getReaderNumber() > terminals.size() - 1) {
                    runSettings.setReaderNumber(0); // set to first reader when terminals have changed
                    logger.info("Card terminals have changed; going default to first reader");
                }
                lblTerminalInfo.setText(terminals.get(runSettings.getReaderNumber()).getName());
        } catch (CardException e) {
            logger.error("Failed to list PCSC terminals");
            lblTerminalInfo.setText("(no terminal/reader detected)");
            lblTerminalInfo.setTextFill(Color.RED);
//            e.printStackTrace();
        }
    }

    @FXML private void handleMenuQuit() { Platform.exit(); } // quit application

    @FXML private void handleMenuLoadVariables() {
        // user select variable file
        FileChooser variableFileChooser = new FileChooser();
        variableFileChooser.setTitle("Select MCC exported variable file");
        variableFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Variables data", "*.txt")
        );
        File selectedVarFile = variableFileChooser.showOpenDialog(application.getPrimaryStage());
        if (selectedVarFile != null) loadVariables(selectedVarFile);
    }

    private void loadVariables(File selectedVarFile) {
        application.getAdvSaveVariables().clear();
        cardiotest.getCmbMccVar().getItems().clear();
        try {
            Scanner scanner = new Scanner(selectedVarFile);
            List<String> definedVariables = new ArrayList<>();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith(".DEFINE"))
                    definedVariables.add(line);
            }
            runSettings.setAdvSaveVariablesPath(selectedVarFile.getAbsolutePath());
            logger.info(String.format("Variable file selected: %s", selectedVarFile.getAbsolutePath()));
            appStatusBar.setText("Variables loaded.");
            for (String line : definedVariables) {
                String[] components = line.split("\\s+");
                application.getAdvSaveVariables().add(
                        new AdvSaveVariable(components[1].substring(1), components[2])
                );
                cardiotest.getCmbMccVar().getItems().add(components[1].substring(1));
            }
        }
        catch (FileNotFoundException e) { e.printStackTrace(); }
    }

    @FXML private void handleMenuSaveSettings() {
        cardiotest.saveControlState();
        runSettings.setVariableMappings(application.getMappings());
        runSettings.setScp80Keysets(scp80Keysets);
        runSettings.setAppletParams(appletParams);
        runSettings.setCustomScriptsSection1(customScriptsSection1);
        runSettings.setCustomScriptsSection2(customScriptsSection2);
        runSettings.setCustomScriptsSection3(customScriptsSection3);
        cardioConfigService.saveConfig(runSettings);
    }

    @FXML private void handleMenuImportSettings() {
        importProjectDir = null;
        importVarFile = null;
        FileChooser importFileChooser = new FileChooser();
        importFileChooser.setTitle("Import Settings");
        importFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Compressed settings", "*.zip")
        );
        File importZipFile = importFileChooser.showOpenDialog(application.getPrimaryStage());
        if (importZipFile != null) {
            application.showImportDialog();
            if (importProjectDir!= null && importVarFile != null) {
                logger.info("Project directory: " + importProjectDir.getAbsolutePath());
                logger.info("Adv save variables: " + importVarFile.getAbsolutePath());
                try {
                    if (eximService.importSettings(importZipFile, importProjectDir, importVarFile)) {
                        logger.info("Import success");
                        reinitConfig();
                    }
                    else appStatusBar.setText("Failed importing " + importZipFile.getAbsolutePath());
                }
                catch (IOException e) { e.printStackTrace(); }
            }
            else {
                String statusBarMsg = "";
                if (importProjectDir == null) statusBarMsg += "Project directory has not been selected; ";
                if (importVarFile == null) statusBarMsg += "Adv save variables has not been selected";
                appStatusBar.setText(statusBarMsg);
            }
        }
    }

    private void reinitConfig() {
        runSettings = cardioConfigService.initConfig();
        application.getAdvSaveVariables().clear();
        application.getMappings().clear();
        cardiotest.getCmbMccVar().getItems().clear();
        cardiotest.getMappedVariables().clear();
        scp80Keysets.clear();
        appletParams.clear();
        customScriptsSection1.clear();
        customScriptsSection2.clear();
        customScriptsSection3.clear();
        fileManagementController.getLinkFilesTableData().clear();
        fileManagementController.getRuwiTableData().clear();
        cardiotest.initialize();
        cardiotest.setObservableList();
        authenticationController.initialize();
        rfmGsmController.initialize();
        rfmUsimController.initialize();
        rfmIsimController.initialize();
        rfmCustomController.initialize();
        ramController.initialize();
        secretCodesController.initialize();
        customTabController.initialize();
        fileManagementController.initialize();
    }

    @FXML private void handleMenuExportSettings() {
        FileChooser exportFileChooser = new FileChooser();
        exportFileChooser.setTitle("Export Settings");
        exportFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Compressed settings", "*.zip")
        );
        File exportFile = exportFileChooser.showSaveDialog(application.getPrimaryStage());
        if (exportFile != null) {
            try { appStatusBar.setText(eximService.exportSettings(runSettings, exportFile)); }
            catch (IOException e) { logger.error("Failed exporting settings: " + e.getMessage()); }
        }
    }

    @FXML private void handleMenuSelectReader() { application.showSelectReader(); }

    @FXML private void handleMenuToolOptions() { application.showToolOptions(); }

    @FXML private void handleMenuAbout() { application.showAbout(); }

    @FXML private void handleMenuRunAll() {
        handleMenuSaveSettings();

        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RunAll. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing RunAll..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                tsResponse = runService.runAll();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // dismiss masker pane
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (tsResponse.isSuccess()) {
                    appStatusBar.setText(tsResponse.getMessage());
                    // show notification
                    Notifications.create().title("CARDIO").text(tsResponse.getMessage()).showInformation();

                    // display log
                    int testModules = Integer.parseInt(tsResponse.getTestSuite().getTests());
                    List<TestCase> testCases = tsResponse.getTestSuite().getTestCases();
                    int runErrors = Integer.parseInt(tsResponse.getTestSuite().getErrors());
                    int runFailures = 0;
                    if (tsResponse.getTestSuite().getFailures() != null)
                        runFailures = Integer.parseInt(tsResponse.getTestSuite().getFailures());
                    if (runErrors != 0) {
                        appendTextFlow(">> NOT OK\n", 1);
                        appendTextFlow("System error: " + tsResponse.getTestSuite().getSystemErr() + "\n\n");
                    }
                    if (runFailures != 0) {
                        appendTextFlow(">> NOT OK\n", 1);
                        appendTextFlow("Module(s) with failure: " + runFailures + "\n\n");
                    }
                    if (runErrors == 0 & runFailures == 0)
                        appendTextFlow(">> OK\n\n", 0);
                    appendTextFlow("Test modules: " + testModules + "\n\n");
                    if (testCases != null) {
                        for (TestCase module : testCases) {
                            setTestStatus(module);
                            appendTextFlow("Name: " + module.getName() + "\n");
                            appendTextFlow("Execution time: " + Float.parseFloat(module.getTime()) + " s\n");
                            if (module.getError() != null)
                                appendTextFlow("Error: \n" + module.getError() + "\n", 1);
                            if (module.getFailure() != null)
                                appendTextFlow("Failure: \n" + module.getFailure() + "\n", 1);
                            appendTextFlow("\n");
                        }
                    }
                    reportService.createReportFromSettings(runSettings);
                    cardiotest.getTxtCommandResponse().clear();
                    cardiotest.getTabBottom().getSelectionModel().select(0);
                } else {
                    appStatusBar.setText(tsResponse.getMessage());
                    Notifications.create().title("CardIO").text(tsResponse.getMessage()).showError();
                    logger.error(tsResponse.getMessage());
                    appendTextFlow(">> ABORTED");
                    Alert pcomAlert = new Alert(Alert.AlertType.ERROR);
                    pcomAlert.initModality(Modality.APPLICATION_MODAL);
                    pcomAlert.initOwner(application.getPrimaryStage());
                    pcomAlert.setTitle("Execution error");
                    pcomAlert.setHeaderText("Failed to execute");
                    pcomAlert.setContentText(tsResponse.getMessage());
                    pcomAlert.showAndWait();
                }
            }
        };
        Thread runAllThread = new Thread(task);
        runAllThread.start(); // run in background
    }

    private void setTestStatus(TestCase module) {
        if (module.getName().equals("ATR")) {
            runSettings.getAtr().setTestAtrOk(true);
            runSettings.getAtr().setTestAtrMesssage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getAtr().setTestAtrOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getAtr().setTestAtrMesssage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getAtr().setTestAtrOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getAtr().setTestAtrMesssage(errFailure);
            }
        }
        if (module.getName().equals("Authentication_MILLENAGE_DELTA_TEST")) {
            runSettings.getAuthentication().setTestDeltaOk(true);
            runSettings.getAuthentication().setTestDeltaMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getAuthentication().setTestDeltaOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getAuthentication().setTestDeltaMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getAuthentication().setTestDeltaOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getAuthentication().setTestDeltaMessage(errFailure);
            }
        }
        if (module.getName().equals("Authentication_MILLENAGE_SQN_MAX")) {
            runSettings.getAuthentication().setTestSqnMaxOk(true);
            runSettings.getAuthentication().setTestSqnMaxMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getAuthentication().setTestSqnMaxOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getAuthentication().setTestSqnMaxMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getAuthentication().setTestSqnMaxOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getAuthentication().setTestSqnMaxMessage(errFailure);
            }
        }
        if (module.getName().equals("SecretCodes_3G")) {
            runSettings.getSecretCodes().setTestCodes3gOk(true);
            runSettings.getSecretCodes().setTestCodes3gMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getSecretCodes().setTestCodes3gOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getSecretCodes().setTestCodes3gMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getSecretCodes().setTestCodes3gOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getSecretCodes().setTestCodes3gMessage(errFailure);
            }
        }
        if (module.getName().equals("SecretCodes_2G")) {
            runSettings.getSecretCodes().setTestCodes2gOk(true);
            runSettings.getSecretCodes().setTestCodes2gMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getSecretCodes().setTestCodes2gOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getSecretCodes().setTestCodes2gMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getSecretCodes().setTestCodes2gOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getSecretCodes().setTestCodes2gMessage(errFailure);
            }
        }
        if (module.getName().equals("RFM_USIM")) {
            runSettings.getRfmUsim().setTestRfmUsimOk(true);
            runSettings.getRfmUsim().setTestRfmUsimMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRfmUsim().setTestRfmUsimOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRfmUsim().setTestRfmUsimMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRfmUsim().setTestRfmUsimOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRfmUsim().setTestRfmUsimMessage(errFailure);
            }
        }
        if (module.getName().equals("RFM_USIM_UpdateRecord")) {
            runSettings.getRfmUsim().setTestRfmUsimUpdateRecordOk(true);
            runSettings.getRfmUsim().setTestRfmUsimUpdateRecordMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRfmUsim().setTestRfmUsimUpdateRecordOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRfmUsim().setTestRfmUsimUpdateRecordMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRfmUsim().setTestRfmUsimUpdateRecordOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRfmUsim().setTestRfmUsimUpdateRecordMessage(errFailure);
            }
        }
        if (module.getName().equals("RFM_USIM_3G_ExpandedMode")) {
            runSettings.getRfmUsim().setTestRfmUsimExpandedModeOk(true);
            runSettings.getRfmUsim().setTestRfmUsimExpandedModeMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRfmUsim().setTestRfmUsimExpandedModeOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRfmUsim().setTestRfmUsimExpandedModeMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRfmUsim().setTestRfmUsimExpandedModeOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRfmUsim().setTestRfmUsimExpandedModeMessage(errFailure);
            }
        }
        if (module.getName().equals("RFM_GSM")) {
            runSettings.getRfmGsm().setTestRfmGsmOk(true);
            runSettings.getRfmGsm().setTestRfmGsmMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRfmGsm().setTestRfmGsmOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRfmGsm().setTestRfmGsmMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRfmGsm().setTestRfmGsmOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRfmGsm().setTestRfmGsmMessage(errFailure);
            }
        }
        if (module.getName().equals("RFM_GSM_UpdateRecord")) {
            runSettings.getRfmGsm().setTestRfmGsmUpdateRecordOk(true);
            runSettings.getRfmGsm().setTestRfmGsmUpdateRecordMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRfmGsm().setTestRfmGsmUpdateRecordOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRfmGsm().setTestRfmGsmUpdateRecordMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRfmGsm().setTestRfmGsmUpdateRecordOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRfmGsm().setTestRfmGsmUpdateRecordMessage(errFailure);
            }
        }
        if (module.getName().equals("RFM_GSM_3G_ExpandedMode")) {
            runSettings.getRfmGsm().setTestRfmGsmExpandedModeOk(true);
            runSettings.getRfmGsm().setTestRfmGsmExpandedModeMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRfmGsm().setTestRfmGsmExpandedModeOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRfmGsm().setTestRfmGsmExpandedModeMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRfmGsm().setTestRfmGsmExpandedModeOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRfmGsm().setTestRfmGsmExpandedModeMessage(errFailure);
            }
        }
        if (module.getName().equals("RFM_ISIM")) {
            runSettings.getRfmIsim().setTestRfmIsimOk(true);
            runSettings.getRfmIsim().setTestRfmIsimMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRfmIsim().setTestRfmIsimOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRfmIsim().setTestRfmIsimMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRfmIsim().setTestRfmIsimOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRfmIsim().setTestRfmIsimMessage(errFailure);
            }
        }
        if (module.getName().equals("RFM_ISIM_UpdateRecord")) {
            runSettings.getRfmIsim().setTestRfmIsimUpdateRecordOk(true);
            runSettings.getRfmIsim().setTestRfmIsimUpdateRecordMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRfmIsim().setTestRfmIsimUpdateRecordOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRfmIsim().setTestRfmIsimUpdateRecordMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRfmIsim().setTestRfmIsimUpdateRecordOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRfmIsim().setTestRfmIsimUpdateRecordMessage(errFailure);
            }
        }
        if (module.getName().equals("RFM_ISIM_3G_ExpandedMode")) {
            runSettings.getRfmIsim().setTestRfmIsimExpandedModeOk(true);
            runSettings.getRfmIsim().setTestRfmIsimExpandedModeMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRfmIsim().setTestRfmIsimExpandedModeOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRfmIsim().setTestRfmIsimExpandedModeMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRfmIsim().setTestRfmIsimExpandedModeOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRfmIsim().setTestRfmIsimExpandedModeMessage(errFailure);
            }
        }
        if (module.getName().equals("RAM")) {
            runSettings.getRam().setTestRamOk(true);
            runSettings.getRam().setTestRamMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRam().setTestRamOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRam().setTestRamMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRam().setTestRamOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRam().setTestRamMessage(errFailure);
            }
        }
        if (module.getName().equals("RAM_UpdateRecord")) {
            runSettings.getRam().setTestRamUpdateRecordOk(true);
            runSettings.getRam().setTestRamUpdateRecordMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRam().setTestRamUpdateRecordOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRam().setTestRamUpdateRecordMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRam().setTestRamUpdateRecordOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRam().setTestRamUpdateRecordMessage(errFailure);
            }
        }
        if (module.getName().equals("RAM_3G_ExpandedMode")) {
            runSettings.getRam().setTestRamExpandedModeOk(true);
            runSettings.getRam().setTestRamExpandedModeMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRam().setTestRamExpandedModeOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRam().setTestRamExpandedModeMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRam().setTestRamExpandedModeOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRam().setTestRamExpandedModeMessage(errFailure);
            }
        }
        if (module.getName().equals("Verif_GP")) {
            runSettings.getRam().setTestVerifGpOk(true);
            runSettings.getRam().setTestVerifGpMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRam().setTestVerifGpOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRam().setTestVerifGpMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRam().setTestVerifGpOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRam().setTestVerifGpMessage(errFailure);
            }
        }

        if (runSettings.getCustomScriptsSection1().size() > 0)
            setCustomScriptsTestStatus(module, runSettings.getCustomScriptsSection1());
        if (runSettings.getCustomScriptsSection2().size() > 0)
            setCustomScriptsTestStatus(module, runSettings.getCustomScriptsSection2());
        if (runSettings.getCustomScriptsSection3().size() > 0)
            setCustomScriptsTestStatus(module, runSettings.getCustomScriptsSection3());
        if (module.getName().equals("RFM_CUSTOM")) {
            runSettings.getRfmCustom().setTestRfmCustomOk(true);
            runSettings.getRfmCustom().setTestRfmCustomMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRfmCustom().setTestRfmCustomOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRfmCustom().setTestRfmCustomMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRfmCustom().setTestRfmCustomOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRfmCustom().setTestRfmCustomMessage(errFailure);
            }
        }
        if (module.getName().equals("RFM_CUSTOM_UpdateRecord")) {
            runSettings.getRfmCustom().setTestRfmCustomUpdateRecordOk(true);
            runSettings.getRfmCustom().setTestRfmCustomUpdateRecordMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRfmCustom().setTestRfmCustomUpdateRecordOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRfmCustom().setTestRfmCustomUpdateRecordMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRfmCustom().setTestRfmCustomUpdateRecordOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRfmCustom().setTestRfmCustomUpdateRecordMessage(errFailure);
            }
        }
        if (module.getName().equals("RFM_CUSTOM_3G_ExpandedMode")) {
            runSettings.getRfmCustom().setTestRfmCustomExpandedModeOk(true);
            runSettings.getRfmCustom().setTestRfmCustomExpandedModeMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getRfmCustom().setTestRfmCustomExpandedModeOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getRfmCustom().setTestRfmCustomExpandedModeMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getRfmCustom().setTestRfmCustomExpandedModeOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getRfmCustom().setTestRfmCustomExpandedModeMessage(errFailure);
            }
        }
        if (module.getName().equals("FileManagement_LinkFiles_TEST")) {
            runSettings.getFileManagement().setTestLinkFilesOk(true);
            runSettings.getFileManagement().setTestLinkFilesMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getFileManagement().setTestLinkFilesOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getFileManagement().setTestLinkFilesMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getFileManagement().setTestLinkFilesOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getFileManagement().setTestLinkFilesMessage(errFailure);
            }
        }
        if (module.getName().equals("FileManagement_Readable&UpdateablewhenInvalidated_TEST")) {
            runSettings.getFileManagement().setTestRuwiOk(true);
            runSettings.getFileManagement().setTestRuwiMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getFileManagement().setTestRuwiOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getFileManagement().setTestRuwiMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getFileManagement().setTestRuwiOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getFileManagement().setTestRuwiMessage(errFailure);
            }
        }
        if (module.getName().equals("FileManagement_SFI_TEST")) {
            runSettings.getFileManagement().setTestSfiOk(true);
            runSettings.getFileManagement().setTestSfiMessage("OK");
            String errFailure = "";
            if (module.getError() != null) {
                runSettings.getFileManagement().setTestSfiOk(false);
                errFailure += module.getError().replace("\n", ";");
                runSettings.getFileManagement().setTestSfiMessage(errFailure);
            }
            if (module.getFailure() != null) {
                runSettings.getFileManagement().setTestSfiOk(false);
                errFailure += module.getFailure().replace("\n", ";");
                runSettings.getFileManagement().setTestSfiMessage(errFailure);
            }
        }
        cardioConfigService.saveConfig(runSettings);
    }

    private void setCustomScriptsTestStatus(TestCase module, List<CustomScript> customScripts) {
        for (CustomScript customScript : customScripts) {
            if (module.getName().equals(getScriptBaseName(customScript.getCustomScriptName()))) {
                customScript.setRunCustomScriptOk(true);
                customScript.setRunCustomScriptMessage("OK");
                String errFailure = "";
                if (module.getError() != null) {
                    customScript.setRunCustomScriptOk(false);
                    errFailure += module.getError().replace("\n", ";");
                    customScript.setRunCustomScriptMessage(errFailure);
                }
                if (module.getFailure() != null) {
                    customScript.setRunCustomScriptOk(false);
                    errFailure += module.getFailure().replace("\n", ";");
                    customScript.setRunCustomScriptMessage(errFailure);
                }
            }
        }
    }

    @FXML private void handleMenuAtr() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing ATR. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing ATR..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runAtrOk = runService.runAtr();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // dismiss masker pane
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runAtrOk) {
                    appStatusBar.setText("Executed ATR: OK");
                    Notifications.create().title("CardIO").text("Executed ATR: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed ATR: NOK");
                    Notifications.create().title("CardIO").text("Executed ATR: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\ATR.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runAtrThread = new Thread(task);
        runAtrThread.start();
    }

    @FXML private void handleMenuDeltaTest() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing MILLENAGE_DELTA_TEST. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing MILLENAGE_DELTA_TEST..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runDeltaTestOk = runService.runDeltaTest();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // dismiss masker pane
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runDeltaTestOk) {
                    appStatusBar.setText("Executed MILLENAGE_DELTA_TEST: OK");
                    Notifications.create().title("CardIO").text("Executed MILLENAGE_DELTA_TEST: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed MILLENAGE_DELTA_TEST: NOK");
                    Notifications.create().title("CardIO").text("Executed MILLENAGE_DELTA_TEST: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\Authentication_MILLENAGE_DELTA_TEST.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runDeltaTestThread = new Thread(task);
        runDeltaTestThread.start();
    }

    @FXML private void handleMenuSqnMax() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing MILLENAGE_SQN_MAX. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing MILLENAGE_SQN_MAX..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runSqnMaxOk = runService.runSqnMax();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // dismiss masker pane
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runSqnMaxOk) {
                    appStatusBar.setText("Executed MILLENAGE_SQN_MAX: OK");
                    Notifications.create().title("CardIO").text("Executed MILLENAGE_SQN_MAX: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed MILLENAGE_SQN_MAX: NOK");
                    Notifications.create().title("CardIO").text("Executed MILLENAGE_SQN_MAX: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\Authentication_MILLENAGE_SQN_MAX.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runSqnMaxThread = new Thread(task);
        runSqnMaxThread.start();
    }

    @FXML private void handleMenuLinkFilesTest() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing FileManagement_LinkFile_TEST. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing FileManagement_LinkFile_TEST..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runLinkFilesTestOk = runService.runLinkFilesTest();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // dismiss masker pane
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runLinkFilesTestOk) {
                    appStatusBar.setText("Executed FileManagement_LinkFiles_TEST: OK");
                    Notifications.create().title("CardIO").text("Executed FileManagement_LinkFiles_TEST: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed FileManagement_LinkFiles_TEST: NOK");
                    Notifications.create().title("CardIO").text("Executed FileManagement_LinkFiles_TEST: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\FileManagement_LinkFiles_TEST.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runLinkFileTestThread = new Thread(task);
        runLinkFileTestThread.start();
    }

    @FXML private void handleMenuRuwiTest() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing FileManagement_Readable&UpdateablewhenInvalidated_TEST. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing FileManagement_Readable&UpdateablewhenInvalidated_TEST..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRuwiTestOk = runService.runRuwiTest();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // dismiss masker pane
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRuwiTestOk) {
                    appStatusBar.setText("Executed FileManagement_Readable&UpdateablewhenInvalidated_TEST: OK");
                    Notifications.create().title("CardIO").text("Executed FileManagement_Readable&UpdateablewhenInvalidated_TEST: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed FileManagement_Readable&_Updateable_when_Invalidated_TEST: NOK");
                    Notifications.create().title("CardIO").text("Executed FileManagement_Readable&UpdateablewhenInvalidated_TEST: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\FileManagement_Readable&UpdateablewhenInvalidated_TEST.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRuwiTestThread = new Thread(task);
        runRuwiTestThread.start();
    }

    @FXML private void handleMenuSfiTest() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing FileManagement_SFI_TEST. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing FileManagement_SFI_TEST..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runSfiTestOk = runService.runSfiTest();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // dismiss masker pane
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runSfiTestOk) {
                    appStatusBar.setText("Executed FileManagement_SFI_TEST: OK");
                    Notifications.create().title("CardIO").text("Executed FileManagement_SFI_TEST: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed FileManagement_SFI_TEST: NOK");
                    Notifications.create().title("CardIO").text("Executed FileManagement_SFI_TEST: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\FileManagement_SFI_TEST.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runSfiTestThread = new Thread(task);
        runSfiTestThread.start();
    }


    @FXML private void handleMenuHighStressAreaTest() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing HighStressArea. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing HighStressArea..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runHighStressAreaTestOk = runService.runHighStressAreaTest();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // dismiss masker pane
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runHighStressAreaTestOk) {
                    appStatusBar.setText("Executed HighStressArea: OK");
                    Notifications.create().title("CardIO").text("Executed HighStressArea: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed HighStressArea: NOK");
                    Notifications.create().title("CardIO").text("Executed HighStressArea: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\HighStressArea.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runHighStressAreaTestThread = new Thread(task);
        runHighStressAreaTestThread.start();
    }


    @FXML private void handleMenuRfmUsim() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_USIM. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing RFM_USIM..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRfmUsimOk = runService.runRfmUsim();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRfmUsimOk) {
                    appStatusBar.setText("Executed RFM_USIM: OK");
                    Notifications.create().title("CardIO").text("Executed RFM_USIM: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed RFM_USIM: NOK");
                    Notifications.create().title("CardIO").text("Executed RFM_USIM: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RFM_USIM.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRfmUsimThread = new Thread(task);
        runRfmUsimThread.start();
    }

    @FXML private void handleMenuRfmUsimUpdateRecord() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_USIM_UpdateRecord. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing RFM_USIM_UpdateRecord..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRfmUsimUpdateRecordOk = runService.runRfmUsimUpdateRecord();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRfmUsimUpdateRecordOk) {
                    appStatusBar.setText("Executed RFM_USIM_UpdateRecord: OK");
                    Notifications.create().title("CardIO").text("Executed RFM_USIM_UpdateRecord: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed RFM_USIM_UpdateRecord: NOK");
                    Notifications.create().title("CardIO").text("Executed RFM_USIM_UpdateRecord: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RFM_USIM_UpdateRecord.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRfmUsimUpdateRecordThread = new Thread(task);
        runRfmUsimUpdateRecordThread.start();
    }

    @FXML private void handleMenuRfmUsimExpandedMode() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_USIM_3G_ExpandedMode. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing RFM_USIM_3G_ExpandedMode..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRfmUsimExpandedModeOk = runService.runRfmUsimExpandedMode();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRfmUsimExpandedModeOk) {
                    appStatusBar.setText("Executed RFM_USIM_3G_ExpandedMode: OK");
                    Notifications.create().title("CardIO").text("Executed RFM_USIM_3G_ExpandedMode: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed RFM_USIM_3G_ExpandedMode: NOK");
                    Notifications.create().title("CardIO").text("Executed RFM_USIM_3G_ExpandedMode: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RFM_USIM_3G_ExpandedMode.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRfmUsimExpandedModeThread = new Thread(task);
        runRfmUsimExpandedModeThread.start();
    }

    @FXML private void handleMenuRfmGsm() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_Gsm. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing RFM_Gsm..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRfmGsmOk = runService.runRfmGsm();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRfmGsmOk) {
                    appStatusBar.setText("Executed RFM_Gsm: OK");
                    Notifications.create().title("CardIO").text("Executed RFM_Gsm: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed RFM_Gsm: NOK");
                    Notifications.create().title("CardIO").text("Executed RFM_Gsm: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RFM_Gsm.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRfmGsmThread = new Thread(task);
        runRfmGsmThread.start();
    }

    @FXML private void handleMenuRfmGsmUpdateRecord() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_Gsm_UpdateRecord. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing RFM_Gsm_UpdateRecord..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRfmGsmUpdateRecordOk = runService.runRfmGsmUpdateRecord();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                    if (runRfmGsmUpdateRecordOk) {
                        appStatusBar.setText("Executed RFM_Gsm_UpdateRecord: OK");
                        Notifications.create().title("CardIO").text("Executed RFM_Gsm_UpdateRecord: OK").showInformation();
                        appendTextFlow(">> OK\n\n", 0);
                    }
                    else {
                        appStatusBar.setText("Executed RFM_Gsm_UpdateRecord: NOK");
                        Notifications.create().title("CardIO").text("Executed RFM_Gsm_UpdateRecord: NOK").showError();
                        appendTextFlow(">> NOT OK\n", 1);
                    }
                    // display commmand-response
                    cardiotest.getTxtCommandResponse().setDisable(false);
                    String logFileName = runSettings.getProjectPath() + "\\scripts\\RFM_Gsm_UpdateRecord.L00";
                    showCommandResponseLog(logFileName);
                }
            };
        Thread runRfmGsmUpdateRecordThread = new Thread(task);
        runRfmGsmUpdateRecordThread.start();
    }

    @FXML private void handleMenuRfmGsmExpandedMode() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_Gsm_3G_ExpandedMode. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();

        appendTextFlow("Executing RFM_Gsm_3G_ExpandedMode..\n\n");
        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRfmGsmExpandedModeOk = runService.runRfmGsmExpandedMode();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRfmGsmExpandedModeOk) {
                    appStatusBar.setText("Executed RFM_Gsm_3G_ExpandedMode: OK");
                    Notifications.create().title("CardIO").text("Executed RFM_Gsm_3G_ExpandedMode: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                } else {
                    appStatusBar.setText("Executed RFM_Gsm_3G_ExpandedMode: NOK");
                    Notifications.create().title("CardIO").text("Executed RFM_Gsm_3G_ExpandedMode: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RFM_Gsm_3G_ExpandedMode.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRfmGsmExpandedModeThread = new Thread(task);
        runRfmGsmExpandedModeThread.start();
    }

    @FXML private void handleMenuRfmIsim() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_ISIM. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();

        appendTextFlow("Executing RFM_ISIM..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRfmIsimOk = runService.runRfmIsim();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar=
                if (runRfmIsimOk) {
                    appStatusBar.setText("Executed RFM_ISIM: OK");
                    Notifications.create().title("CardIO").text("Executed RFM_ISIM: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed RFM_ISIM: NOK");
                    Notifications.create().title("CardIO").text("Executed RFM_ISIM: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RFM_ISIM.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRfmIsimThread = new Thread(task);
        runRfmIsimThread.start();
    }

    @FXML private void handleMenuRfmIsimUpdateRecord() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_ISIM_UpdateRecord. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing RFM_ISIM_UpdateRecord..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRfmIsimUpdateRecordOk = runService.runRfmIsimUpdateRecord();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRfmIsimUpdateRecordOk) {
                    appStatusBar.setText("Executed RFM_ISIM_UpdateRecord: OK");
                    Notifications.create().title("CardIO").text("Executed RFM_ISIM_UpdateRecord: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed RFM_ISIM_UpdateRecord: NOK");
                    Notifications.create().title("CardIO").text("Executed RFM_ISIM_UpdateRecord: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RFM_ISIM_UpdateRecord.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRfmIsimUpdateRecordThread = new Thread(task);
        runRfmIsimUpdateRecordThread.start();
    }

    @FXML private void handleMenuRfmIsimExpandedMode() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_ISIM_3G_ExpandedMode. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();

        appendTextFlow("Executing RFM_ISIM_3G_ExpandedMode..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRfmIsimExpandedModeOk = runService.runRfmIsimExpandedMode();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRfmIsimExpandedModeOk) {
                    appStatusBar.setText("Executed RFM_ISIM_3G_ExpandedMode: OK");
                    Notifications.create().title("CardIO").text("Executed RFM_ISIM_3G_ExpandedMode: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed RFM_ISIM_3G_ExpandedMode: NOK");
                    Notifications.create().title("CardIO").text("Executed RFM_ISIM_3G_ExpandedMode: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RFM_ISIM_3G_ExpandedMode.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRfmIsimExpandedModeThread = new Thread(task);
        runRfmIsimExpandedModeThread.start();
    }

    @FXML
    private void handleMenuRam() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RAM. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();

        appendTextFlow("Executing RAM..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRamOk = runService.runRam();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar=
                if (runRamOk) {
                    appStatusBar.setText("Executed RAM: OK");
                    Notifications.create().title("CardIO").text("Executed RAM: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed RAM: NOK");
                    Notifications.create().title("CardIO").text("Executed RAM: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RAM.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRamThread = new Thread(task);
        runRamThread.start();
    }

    @FXML
    private void handleMenuRamUpdateRecord() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RAM_UpdateRecord. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing RAM_UpdateRecord..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRamUpdateRecordOk = runService.runRamUpdateRecord();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRamUpdateRecordOk) {
                    appStatusBar.setText("Executed RAM_UpdateRecord: OK");
                    Notifications.create().title("CardIO").text("Executed RAM_UpdateRecord: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed RAM_UpdateRecord: NOK");
                    Notifications.create().title("CardIO").text("Executed RAM_UpdateRecord: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RAM_UpdateRecord.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRamUpdateRecordThread = new Thread(task);
        runRamUpdateRecordThread.start();
    }

    @FXML
    private void handleMenuRamExpandedMode() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RAM_3G_ExpandedMode. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();

        appendTextFlow("Executing RAM_3G_ExpandedMode..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRamExpandedModeOk = runService.runRamExpandedMode();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRamExpandedModeOk) {
                    appStatusBar.setText("Executed RAM_3G_ExpandedMode: OK");
                    Notifications.create().title("CardIO").text("Executed RAM_3G_ExpandedMode: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed RAM_3G_ExpandedMode: NOK");
                    Notifications.create().title("CardIO").text("Executed RAM_3G_ExpandedMode: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RAM_3G_ExpandedMode.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRamExpandedModeThread = new Thread(task);
        runRamExpandedModeThread.start();
    }

    @FXML
    private void handleMenuRfmCustom() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_CUSTOM. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing RFM_CUSTOM..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRfmCustomOk = runService.runRfmCustom();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRfmCustomOk) {
                    appStatusBar.setText("Executed RFM_CUSTOM: OK");
                    Notifications.create().title("CardIO").text("Executed RFM_CUSTOM: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                } else {
                    appStatusBar.setText("Executed RFM_CUSTOM: NOK");
                    Notifications.create().title("CardIO").text("Executed RFM_CUSTOM: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RFM_CUSTOM.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRfmCustomThread = new Thread(task);
        runRfmCustomThread.start();
    }

    @FXML private void handleMenuRfmCustomUpdateRecord() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_CUSTOM_UpdateRecord. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing RFM_CUSTOM_UpdateRecord..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRfmCustomUpdateRecordOk = runService.runRfmCustomUpdateRecord();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRfmCustomUpdateRecordOk) {
                    appStatusBar.setText("Executed RFM_CUSTOM_UpdateRecord: OK");
                    Notifications.create().title("CardIO").text("Executed RFM_CUSTOM_UpdateRecord: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                } else {
                    appStatusBar.setText("Executed RFM_CUSTOM_UpdateRecord: NOK");
                    Notifications.create().title("CardIO").text("Executed RFM_CUSTOM_UpdateRecord: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RFM_CUSTOM_UpdateRecord.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRfmCustomUpdateRecordThread = new Thread(task);
        runRfmCustomUpdateRecordThread.start();
    }

    @FXML private void handleMenuRfmCustomExpandedMode() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_CUSTOM_3G_ExpandedMode. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing RFM_CUSTOM_3G_ExpandedMode..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runRfmCustomExpandedModeOk = runService.runRfmCustomExpandedMode();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runRfmCustomExpandedModeOk) {
                    appStatusBar.setText("Executed RFM_CUSTOM_3G_ExpandedMode: OK");
                    Notifications.create().title("CardIO").text("Executed RFM_CUSTOM_3G_ExpandedMode: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                } else {
                    appStatusBar.setText("Executed RFM_CUSTOM_3G_ExpandedMode: NOK");
                    Notifications.create().title("CardIO").text("Executed RFM_CUSTOM_3G_ExpandedMode: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\RFM_CUSTOM_3G_ExpandedMode.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runRfmCustomExpandedModeThread = new Thread(task);
        runRfmCustomExpandedModeThread.start();
    }

    @FXML private void handleMenuVerifGp() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing Verif_GP. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing Verif_GP..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runVerifGpOk = runService.runVerifGp();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runVerifGpOk) {
                    appStatusBar.setText("Executed Verif_GP: OK");
                    Notifications.create().title("CardIO").text("Executed Verif_GP: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed Verif_GP: NOK");
                    Notifications.create().title("CardIO").text("Executed Verif_GP: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\Verif_GP.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runVerifGp = new Thread(task);
        runVerifGp.start();
    }

    @FXML private void handleMenuCodes3g() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing SecretCodes_3G. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing SecretCodes_3G..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runCodes3gOk = runService.runSecretCodes3g();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // dismiss masker pane
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runCodes3gOk) {
                    appStatusBar.setText("Executed SecretCodes_3G: OK");
                    Notifications.create().title("CardIO").text("Executed SecretCodes_3G: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed SecretCodes_3G: NOK");
                    Notifications.create().title("CardIO").text("Executed SecretCodes_3G: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\SecretCodes_3G.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runCodes3gThread = new Thread(task);
        runCodes3gThread.start();
    }

    @FXML private void handleMenuCodes2g() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing SecretCodes_2G. Please wait..");
        cardiotest.getMaskerPane().setVisible(true); // display masker pane
        menuBar.setDisable(true);
        appStatusBar.setDisable(true);

        cardiotest.getTxtInterpretedLog().getChildren().clear();
        appendTextFlow("Executing SecretCodes_2G..\n\n");

        // use threads to avoid application freeze
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                runCodes2gOk = runService.runSecretCodes2g();
                return null;
            }

            @Override
            protected void succeeded() {
                super.succeeded();
                // dismiss masker pane
                cardiotest.getMaskerPane().setVisible(false);
                menuBar.setDisable(false);
                appStatusBar.setDisable(false);
                // update status bar
                if (runCodes2gOk) {
                    appStatusBar.setText("Executed SecretCodes_2G: OK");
                    Notifications.create().title("CardIO").text("Executed SecretCodes_2G: OK").showInformation();
                    appendTextFlow(">> OK\n\n", 0);
                }
                else {
                    appStatusBar.setText("Executed SecretCodes_2G: NOK");
                    Notifications.create().title("CardIO").text("Executed SecretCodes_2G: NOK").showError();
                    appendTextFlow(">> NOT OK\n", 1);
                }
                // display commmand-response
                cardiotest.getTxtCommandResponse().setDisable(false);
                String logFileName = runSettings.getProjectPath() + "\\scripts\\SecretCodes_2G.L00";
                showCommandResponseLog(logFileName);
            }
        };
        Thread runCodes2gThread = new Thread(task);
        runCodes2gThread.start();
    }

    private void showCommandResponseLog(String logFileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(logFileName))) {
            StringBuffer sb = new StringBuffer();
            String currentLine;
            while ((currentLine = br.readLine()) != null)
                sb.append(currentLine + "\n");
            cardiotest.getTxtCommandResponse().setText(sb.toString());
            cardiotest.getTabBottom().getSelectionModel().select(0);
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    private void appendTextFlow(String text) {
        Text message = new Text(text);
        cardiotest.getTxtInterpretedLog().getChildren().add(message);
    }

    private void appendTextFlow(String text, int style) {
        Text message = new Text(text);
        if (style == 0) message.setStyle("-fx-fill: #4F8A10;-fx-font-weight:bold;");
        if (style == 1) message.setStyle("-fx-fill: RED;-fx-font-weight:normal;");
        cardiotest.getTxtInterpretedLog().getChildren().add(message);
    }

    private String getScriptBaseName(String scriptName) {
        if (getScriptExtension(scriptName).get().equals("cmd") || getScriptExtension(scriptName).get().equals("txt"))
            return scriptName.substring(0, scriptName.length() - 4);
        if (getScriptExtension(scriptName).get().equals("pcom"))
            return scriptName.substring(0, scriptName.length() - 5);
        return null;
    }

    private Optional<String> getScriptExtension(String scriptName) {
        return Optional.ofNullable(scriptName)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(scriptName.lastIndexOf(".") + 1));
    }

    public StatusBar getAppStatusBar() { return appStatusBar; }
    public TerminalFactory getTerminalFactory() { return terminalFactory; }
    public Label getLblTerminalInfo() { return lblTerminalInfo; }
    public MenuItem getMenuAtr() { return menuAtr; }
    public MenuItem getMenuDeltaTest() { return menuDeltaTest; }
    public MenuItem getMenuSqnMax() { return menuSqnMax; }
    public MenuItem getMenuRfmUsim() { return menuRfmUsim; }
    public MenuItem getMenuRfmUsimUpdateRecord() { return menuRfmUsimUpdateRecord; }
    public MenuItem getMenuRfmUsimExpandedMode() { return menuRfmUsimExpandedMode; }
    public MenuItem getMenuRfmGsm() { return menuRfmGsm; }
    public MenuItem getMenuRfmGsmUpdateRecord() { return menuRfmGsmUpdateRecord; }
    public MenuItem getMenuRfmGsmExpandedMode() { return menuRfmGsmExpandedMode; }
    public MenuItem getMenuRfmIsim() { return menuRfmIsim; }
    public MenuItem getMenuRfmIsimUpdateRecord() { return menuRfmIsimUpdateRecord; }
    public MenuItem getMenuRfmIsimExpandedMode() { return menuRfmIsimExpandedMode; }
    public MenuItem getMenuRfmCustom() { return menuRfmCustom; }
    public MenuItem getMenuRfmCustomUpdateRecord() { return menuRfmCustomUpdateRecord; }
    public MenuItem getMenuRfmCustomExpandedMode() { return menuRfmCustomExpandedMode; }
    public MenuItem getMenuRam() { return menuRam; }
    public MenuItem getMenuRamUpdateRecord() { return menuRamUpdateRecord; }
    public MenuItem getMenuRamExpandedMode() { return menuRamExpandedMode; }
    public MenuItem getMenuVerifGp(){ return menuVerifGp; }
    public MenuItem getMenuCodes3g() { return menuCodes3g; }
    public MenuItem getMenuCodes2g() { return menuCodes2g; }
    public ObservableList<SCP80Keyset> getScp80Keysets() { return scp80Keysets; }
    public ObservableList<AppletParam> getAppletParams() { return appletParams; }
    public ObservableList<CustomScript> getCustomScriptsSection1() { return customScriptsSection1; }
    public ObservableList<CustomScript> getCustomScriptsSection2() { return customScriptsSection2; }
    public ObservableList<CustomScript> getCustomScriptsSection3() { return customScriptsSection3; }


    public MenuItem getMenuLinkFile() { return menuLinkFiles; }
    public MenuItem getMenuRuwi() { return menuRuwi; }
    public MenuItem getMenuSfi() { return menuSfi; }
    public MenuItem getMenuHighStressArea() { return menuHighStressArea; }

    public void setImportProjectDir(File importProjectDir) { this.importProjectDir = importProjectDir; }
    public void setImportVarFile(File importVarFile) { this.importVarFile = importVarFile; }
}
