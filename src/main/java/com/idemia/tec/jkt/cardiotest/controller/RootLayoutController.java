package com.idemia.tec.jkt.cardiotest.controller;

import com.idemia.tec.jkt.cardiotest.CardiotestApplication;
import com.idemia.tec.jkt.cardiotest.model.AdvSaveVariable;
import com.idemia.tec.jkt.cardiotest.model.RunSettings;
import com.idemia.tec.jkt.cardiotest.model.SCP80Keyset;
import com.idemia.tec.jkt.cardiotest.model.TestCase;
import com.idemia.tec.jkt.cardiotest.response.TestSuiteResponse;
import com.idemia.tec.jkt.cardiotest.service.CardioConfigService;
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
import java.util.Scanner;

@Component
public class RootLayoutController {

    static Logger logger = Logger.getLogger(RootLayoutController.class);

    private RunSettings runSettings;

    private CardiotestApplication application;
    private TerminalFactory terminalFactory;
    private ObservableList<SCP80Keyset> scp80Keysets = FXCollections.observableArrayList();
    private TestSuiteResponse tsResponse;
    private boolean runAtrOk;
    private boolean runDeltaTestOk;
    private boolean runSqnMaxOk;
    private boolean runRfmUsimOk;
    private boolean runRfmUsimUpdateRecordOk;
    private boolean runRfmUsimExpandedModeOk;
    private boolean runRfmIsimOk;
    private boolean runRfmIsimUpdateRecordOk;
    private boolean runRfmIsimExpandedModeOk;
    private boolean runCodes3gOk;
    private boolean runCodes2gOk;

    @Autowired
    private CardiotestController cardiotest;
    @Autowired
    private CardioConfigService cardioConfigService;
    @Autowired
    private RunService runService;
    @Autowired
    private ReportService reportService;

    @FXML
    private BorderPane rootBorderPane;
    @FXML
    private MenuBar menuBar;

    @FXML
    private MenuItem menuAtr;
    @FXML
    private MenuItem menuDeltaTest;
    @FXML
    private MenuItem menuSqnMax;
    @FXML
    private MenuItem menuRfmUsim;
    @FXML
    private MenuItem menuRfmUsimUpdateRecord;
    @FXML
    private MenuItem menuRfmUsimExpandedMode;
    @FXML
    private MenuItem menuRfmIsim;
    @FXML
    private MenuItem menuRfmIsimUpdateRecord;
    @FXML
    private MenuItem menuRfmIsimExpandedMode;
    @FXML
    private MenuItem menuCodes3g;
    @FXML
    private MenuItem menuCodes2g;

    private StatusBar appStatusBar;
    private Label lblTerminalInfo;

    public RootLayoutController() {}

    public void setMainApp(CardiotestApplication application) {
        this.application = application;
    }

    public RunSettings getRunSettings() {
        return runSettings;
    }

    @FXML
    private void initialize() {
        appStatusBar = new StatusBar();
        rootBorderPane.setBottom(appStatusBar);

        // get run settings from 'run-settings.json' or by default values
        runSettings = cardioConfigService.initConfig();

        terminalFactory = TerminalFactory.getDefault();
        lblTerminalInfo = new Label();
        appStatusBar.getRightItems().add(new Separator(Orientation.VERTICAL));
        appStatusBar.getRightItems().add(lblTerminalInfo);
        try {
            // list available readers
            List<CardTerminal> terminals = terminalFactory.terminals().list();
            if (terminals.isEmpty())
                lblTerminalInfo.setText("(no terminal/reader detected)");
            else
                if (runSettings.getReaderNumber() != -1)
                    lblTerminalInfo.setText(terminals.get(runSettings.getReaderNumber()).getName());
        } catch (CardException e) {
            logger.error("Failed to list PCSC terminals");
            lblTerminalInfo.setText("(no terminal/reader detected)");
            lblTerminalInfo.setTextFill(Color.RED);
//            e.printStackTrace();
        }
    }

    @FXML
    private void handleMenuQuit() {
        // quit application
        Platform.exit();
    }

    @FXML
    private void handleMenuLoadVariables() {
        // user select variable file
        FileChooser variableFileChooser = new FileChooser();
        variableFileChooser.setTitle("Select MCC exported variable file");
        variableFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Variables data", "*.txt")
        );
        File selectedVarFile = variableFileChooser.showOpenDialog(application.getPrimaryStage());
        if (selectedVarFile != null) {
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
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleMenuSaveSettings() {
        cardiotest.saveControlState();
        runSettings.setVariableMappings(application.getMappings());
//        runSettings.setScp80Keysets(application.getScp80Keysets());
        runSettings.setScp80Keysets(scp80Keysets); // TODO
        cardioConfigService.saveConfig(runSettings);
    }

    @FXML
    private void handleMenuSelectReader() {
        application.showSelectReader();
    }

    @FXML
    private void handleMenuToolOptions() {
        application.showToolOptions();
    }

    @FXML
    private void handleMenuRunAll() {
        handleMenuSaveSettings();

        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RunAll. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
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
                    Notifications.create().title("CardIO").text(tsResponse.getMessage()).showInformation();

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
                        appendTextFlow("Failures: " + runFailures + "\n\n");
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
                }
                else {
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
        cardioConfigService.saveConfig(runSettings);
    }

    @FXML
    private void handleMenuAtr() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing ATR. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
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

    @FXML
    private void handleMenuDeltaTest() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing MILLENAGE_DELTA_TEST. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
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

    @FXML
    private void handleMenuSqnMax() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing MILLENAGE_SQN_MAX. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
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

    @FXML
    private void handleMenuRfmUsim() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_USIM. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
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

    @FXML
    private void handleMenuRfmUsimUpdateRecord() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_USIM_UpdateRecord. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
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

    @FXML
    private void handleMenuRfmUsimExpandedMode() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_USIM_3G_ExpandedMode. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
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

    @FXML
    private void handleMenuRfmIsim() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_ISIM. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
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
                // update status bar
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

    @FXML
    private void handleMenuRfmIsimUpdateRecord() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_ISIM_UpdateRecord. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
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

    @FXML
    private void handleMenuRfmIsimExpandedMode() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing RFM_ISIM_3G_ExpandedMode. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
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
    private void handleMenuCodes3g() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing SecretCodes_3G. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
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

    @FXML
    private void handleMenuCodes2g() {
        handleMenuSaveSettings();
        // make user wait as verification executes
        cardiotest.getMaskerPane().setText("Executing SecretCodes_2G. Please wait..");
        // display masker pane
        cardiotest.getMaskerPane().setVisible(true);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendTextFlow(String text) {
        Text message = new Text(text);
        cardiotest.getTxtInterpretedLog().getChildren().add(message);
    }

    private void appendTextFlow(String text, int style) {
        Text message = new Text(text);
        if (style == 0)
            message.setStyle("-fx-fill: #4F8A10;-fx-font-weight:bold;");
        if (style == 1)
            message.setStyle("-fx-fill: RED;-fx-font-weight:normal;");
        cardiotest.getTxtInterpretedLog().getChildren().add(message);
    }

    public StatusBar getAppStatusBar() {
        return appStatusBar;
    }

    public TerminalFactory getTerminalFactory() {
        return terminalFactory;
    }

    public Label getLblTerminalInfo() {
        return lblTerminalInfo;
    }

    public MenuItem getMenuAtr() {
        return menuAtr;
    }

    public MenuItem getMenuDeltaTest() {
        return menuDeltaTest;
    }

    public MenuItem getMenuSqnMax() {
        return menuSqnMax;
    }

    public MenuItem getMenuRfmUsim() {
        return menuRfmUsim;
    }

    public MenuItem getMenuRfmUsimUpdateRecord() {
        return menuRfmUsimUpdateRecord;
    }

    public MenuItem getMenuRfmUsimExpandedMode() {
        return menuRfmUsimExpandedMode;
    }

    public MenuItem getMenuRfmIsim() {
        return menuRfmIsim;
    }

    public MenuItem getMenuRfmIsimUpdateRecord() {
        return menuRfmIsimUpdateRecord;
    }

    public MenuItem getMenuRfmIsimExpandedMode() {
        return menuRfmIsimExpandedMode;
    }

    public MenuItem getMenuCodes3g() {
        return menuCodes3g;
    }

    public MenuItem getMenuCodes2g() {
        return menuCodes2g;
    }

    public ObservableList<SCP80Keyset> getScp80Keysets() {
        return scp80Keysets;
    }

}
