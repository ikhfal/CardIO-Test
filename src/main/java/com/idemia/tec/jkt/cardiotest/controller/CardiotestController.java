package com.idemia.tec.jkt.cardiotest.controller;

import com.idemia.tec.jkt.cardiotest.CardiotestApplication;
import com.idemia.tec.jkt.cardiotest.model.AdvSaveVariable;
import com.idemia.tec.jkt.cardiotest.model.SCP80Keyset;
import com.idemia.tec.jkt.cardiotest.model.VariableMapping;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextFlow;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.controlsfx.control.MaskerPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Component
public class CardiotestController {

    @FXML private TableView<AdvSaveVariable> tblAdvSave;
    @FXML private TableColumn<AdvSaveVariable, String> clmnDefined;
    @FXML private TableColumn<AdvSaveVariable, String> clmnValue;
    @FXML private TableView<VariableMapping> tblMapping;
    @FXML private TableColumn<VariableMapping, String> clmnMappedTo;
    @FXML private TableColumn<VariableMapping, String> clmnMccVar;
    @FXML private TextField txtMappedTo;
    @FXML private ComboBox<String> cmbMccVar;
    @FXML private StackPane stackPane;

    private MaskerPane maskerPane;

    @FXML private CheckBox chkFixedVal;
    @FXML private TextField txtMccVar;

    // project details tab
    @FXML private TextField txtProjectFolder;
    @FXML private TextField txtRequestId;
    @FXML private TextField txtRequestName;
    @FXML private TextField txtProfileName;
    @FXML private TextField txtProfileVersion;
    @FXML private TextField txtCardImageItemId;
    @FXML private TextField txtCustomer;
    @FXML private TextField txtDeveloperName;
    @FXML private TextField txtTesterName;

    private List<ComboBox<String>> comboPool;
    private ObservableList<String> mappedVariables;

    // ATR box
    @FXML private TextField txtAtr;
    @FXML private CheckBox chkIncludeAtr;
    @FXML private Label lblProtocol;

    // card parameters
    @FXML private TextField txtCardManagerAid;
    @FXML private TextField txtUsimAid;
    @FXML private TextField txtDfUsim;
    @FXML private TextField txtDfGsm;
    @FXML private TextField txtDfGsmAccess;
    @FXML private TextField txtDfTelecom;
    @FXML private TextField txtIsimAid;
    @FXML private TextField txtDfIsim;
    @FXML private TextField txtCsimAid;
    @FXML private TextField txtDfCsim;
    @FXML private ComboBox<String> cmbIccid;

    // OTA settings tab
    @FXML private TableView<SCP80Keyset> tblScp80Keyset;
    @FXML private TableColumn<SCP80Keyset, String> clmnKeysetName;
    @FXML private TextField txtKeysetName;
    @FXML private ComboBox<String> cmbKeysetVersion;
    @FXML private ComboBox<String> cmbKeysetType;
    @FXML private ComboBox<String> cmbKicValuation;
    @FXML private ComboBox<String> cmbKicLength;
    @FXML private ComboBox<String> cmbKicMode;
    @FXML private ComboBox<String> cmbKidValuation;
    @FXML private ComboBox<String> cmbKidLength;
    @FXML private ComboBox<String> cmbKidMode;
    @FXML private Label lblCmacLength;
    @FXML private ComboBox<String> cmbCmacLength;
    @FXML private Label lblAddKeysetErrMsg;
    @FXML private TextField txtUdhiFirstByte;
    @FXML private TextField txtScAddress;
    @FXML private TextField txtTpPid;
    @FXML private CheckBox chkTpOa;
    @FXML private Label lblTpOa;
    @FXML private TextField txtTpOa;
    @FXML private ComboBox<String> cmbPorFormat;

    private ObservableList<String> scp80KeysetLabels;

    // bottom tab pane
    @FXML private TabPane tabBottom;
    @FXML private TextFlow txtInterpretedLog;
    @FXML private TextArea txtCommandResponse;

    static Logger logger = Logger.getLogger(CardiotestController.class);

    private CardiotestApplication application;

    @Autowired private RootLayoutController root;
    @Autowired private CardiotestController cardiotest;
    @Autowired private SecretCodesController secretCodesController;
    @Autowired private AuthenticationController authenticationController;
    @Autowired private RfmUsimController rfmUsimController;
    @Autowired private RfmGsmController rfmGsmController;
    @Autowired private RfmIsimController rfmIsimController;
    @Autowired private RamController ramController;
    @Autowired private RfmCustomController rfmCustomController;

    //----------------------------------
    @Autowired private FileManagementController fileManagementController;

    @Autowired private FileManagement2Controller fileManagement2Controller;
    //----------------------------------

    public CardiotestController() {}

    public void setMainApp(CardiotestApplication application) {
        this.application = application;
    }

    public void setObservableList() {
        // add observable list data to adv save & mapping tables
        tblAdvSave.setItems(application.getAdvSaveVariables());
        tblMapping.setItems(application.getMappings());

        // load variables from saved settings
        File loadedVarFile = new File(root.getRunSettings().getAdvSaveVariablesPath());
        if (loadedVarFile != null) {
            try {
                Scanner scanner = new Scanner(loadedVarFile);
                List<String> definedVariables = new ArrayList<>();
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith(".DEFINE"))
                        definedVariables.add(line);
                }
                root.getRunSettings().setAdvSaveVariablesPath(loadedVarFile.getAbsolutePath());
                logger.info(String.format("Variable file selected: %s", loadedVarFile.getAbsolutePath()));
                root.getAppStatusBar().setText("Variables loaded.");
                for (String line : definedVariables) {
                    String[] components = line.split("\\s+");
                    application.getAdvSaveVariables().add(
                            new AdvSaveVariable(components[1].substring(1), components[2])
                    );
                    cmbMccVar.getItems().add(components[1].substring(1));
                }
            } catch (FileNotFoundException e) {
                logger.warn("Can't find variable file. Please select from MCC Advance Save.");
                root.getAppStatusBar().setText("Can't find variable file. Please select from MCC Advance Save.");
            }
        }

        // load mappings from saved settings
        for (VariableMapping mapping : root.getRunSettings().getVariableMappings())
            application.getMappings().add(mapping);

        // SCP80 keyset table
        tblScp80Keyset.setItems(root.getScp80Keysets());
        // load keysets from saved settings
        for (SCP80Keyset keyset : root.getRunSettings().getScp80Keysets())
            root.getScp80Keysets().add(keyset);
        // select first row initially
        tblScp80Keyset.getSelectionModel().select(0);

        // add authentication RiCi default values
        if (application.getMappings().size() == 0) {
            application.getMappings().add(new VariableMapping("C1", null, "00000000000000000000000000000000", true));
            application.getMappings().add(new VariableMapping("C2", null, "00000000000000000000000000000001", true));
            application.getMappings().add(new VariableMapping("C3", null, "00000000000000000000000000000002", true));
            application.getMappings().add(new VariableMapping("C4", null, "00000000000000000000000000000004", true));
            application.getMappings().add(new VariableMapping("C5", null, "00000000000000000000000000000008", true));
            application.getMappings().add(new VariableMapping("RI", null, "4000204060", true));
            application.getMappings().add(new VariableMapping("K1", null, "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA", true));
            application.getMappings().add(new VariableMapping("OPC", null, "BAEBC618A55C351F25CEDF37BF70F390", true));
            for (VariableMapping mapping : application.getMappings())
                mappedVariables.add(mapping.getMappedVariable());
        }

    }

    @FXML public void initialize() {
        // initialize variable table
        clmnDefined.setCellValueFactory(celldata -> celldata.getValue().definedVariable());
        clmnValue.setCellValueFactory(celldata -> celldata.getValue().value());

        // initialize mapping table
        clmnMappedTo.setCellValueFactory(celldata -> celldata.getValue().mappedVariable());
        clmnMccVar.setCellValueFactory(celldata -> celldata.getValue().mccVariable());

        // clear mapping display
        showMappings(null);

        // listen for selection changes and show mapping detail when changed
        tblMapping.getSelectionModel().selectedItemProperty().addListener(
                ((observable, oldValue, newValue) -> showMappings(newValue))
        );
        chkFixedVal.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (chkFixedVal.isSelected()) {
                cmbMccVar.setDisable(true);
                txtMccVar.setDisable(false);
            } else {
                cmbMccVar.setDisable(false);
                txtMccVar.setDisable(true);
            }
        });

        mappedVariables = FXCollections.observableArrayList();
        for (VariableMapping mapping : root.getRunSettings().getVariableMappings())
            mappedVariables.add(mapping.getMappedVariable());

        comboPool = new ArrayList<>();

        // for masking main window while verification is executing
        maskerPane = new MaskerPane();
        maskerPane.setVisible(false);
        stackPane.getChildren().add(maskerPane);

        tabBottom.getSelectionModel().select(0);

        Font fixedWidthFont;
        if (Font.getFamilies().contains("Consolas"))
            fixedWidthFont = Font.font("Consolas");
        else
            fixedWidthFont = Font.font("Monospaced");
        txtCommandResponse.setFont(fixedWidthFont);
        txtCommandResponse.setEditable(false);
        txtCommandResponse.setDisable(true);

        // restore values of controls

        // project details
        txtProjectFolder.setText(root.getRunSettings().getProjectPath());
        txtRequestId.setText(root.getRunSettings().getRequestId());
        txtRequestName.setText(root.getRunSettings().getRequestName());
        txtProfileName.setText(root.getRunSettings().getProfileName());
        txtProfileVersion.setText(Integer.toString(root.getRunSettings().getProfileVersion()));
        txtCardImageItemId.setText(root.getRunSettings().getCardImageItemId());
        txtCustomer.setText(root.getRunSettings().getCustomer());
        txtDeveloperName.setText(root.getRunSettings().getDeveloperName());
        txtTesterName.setText(root.getRunSettings().getTesterName());

        // ATR box
        chkIncludeAtr.setSelected(root.getRunSettings().getAtr().isIncludeAtr());
        handleIncludeAtrCheck();
        txtAtr.setText(root.getRunSettings().getAtr().getAtrString());

        // card parameters
        txtCardManagerAid.setText(root.getRunSettings().getCardParameters().getCardManagerAid());
        txtUsimAid.setText(root.getRunSettings().getCardParameters().getUsimAid());
        txtDfUsim.setText(root.getRunSettings().getCardParameters().getDfUsim());
        txtDfGsm.setText(root.getRunSettings().getCardParameters().getDfGsm());
        txtDfGsmAccess.setText(root.getRunSettings().getCardParameters().getDfGsmAccess());
        txtDfTelecom.setText(root.getRunSettings().getCardParameters().getDfTelecom());
        txtIsimAid.setText(root.getRunSettings().getCardParameters().getIsimAid());
        txtDfIsim.setText(root.getRunSettings().getCardParameters().getDfIsim());
        txtCsimAid.setText(root.getRunSettings().getCardParameters().getCsimAid());
        txtDfCsim.setText(root.getRunSettings().getCardParameters().getDfCsim());
        cmbIccid.setValue(root.getRunSettings().getCardParameters().getIccid());

        // OTA settings
        clmnKeysetName.setCellValueFactory(celldata -> celldata.getValue().keysetNameProperty());
        // clear keyset fields
        showKeyset(null);
        // listen for selection changes and show keyset detail when changed
        tblScp80Keyset.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showKeyset(newValue)
        );
        // initialize list of versions
        cmbKeysetVersion.getItems().clear();
        for (int i = 0; i < 15; i++)
            cmbKeysetVersion.getItems().add(Integer.toString(i + 1));
        // initialize list of types
        List<String> keysetTypes = new ArrayList<>();
        keysetTypes.add("Algorithm known implicitly by both entities");
        keysetTypes.add("DES");
        keysetTypes.add("AES");
        keysetTypes.add("Proprietary Implementations");
        cmbKeysetType.getItems().clear();
        cmbKeysetType.getItems().addAll(keysetTypes);
        // initialize list of cipher modes
        List<String> cipherBlockModes = new ArrayList<>();
        cipherBlockModes.add("DES - CBC");
        cipherBlockModes.add("3DES - CBC 2 keys");
        cipherBlockModes.add("3DES - CBC 3 keys");
        cipherBlockModes.add("DES - ECB");
        cipherBlockModes.add("AES - CBC");
        cmbKicValuation.setItems(mappedVariables);
        registerForComboUpdate(cmbKicValuation);
        // initialize available key space
        List<String> scp80KeyLengths = new ArrayList<>();
        scp80KeyLengths.add("8");
        scp80KeyLengths.add("16");
        scp80KeyLengths.add("24");
        scp80KeyLengths.add("32");

        cmbKicLength.getItems().clear();
        cmbKicMode.getItems().clear();
        cmbKicLength.getItems().addAll(scp80KeyLengths);
        cmbKicMode.getItems().addAll(cipherBlockModes);

        // initialize list of crypto checksum modes
        List<String> ccBlockModes = new ArrayList<>();
        ccBlockModes.add("DES - CBC");
        ccBlockModes.add("3DES - CBC 2 keys");
        ccBlockModes.add("3DES - CBC 3 keys");
        ccBlockModes.add("DES - ECB");
        ccBlockModes.add("AES - CMAC");

        cmbKidValuation.setItems(mappedVariables);
        registerForComboUpdate(cmbKidValuation);

        cmbKidLength.getItems().clear();
        cmbKidMode.getItems().clear();
        cmbKidLength.getItems().addAll(scp80KeyLengths);
        cmbKidMode.getItems().addAll(ccBlockModes);

        // initialize CMAC lengths
        cmbCmacLength.getItems().clear();
        List<String> cmacLengths = new ArrayList<>();
        cmacLengths.add("4");
        cmacLengths.add("8");
        cmbCmacLength.getItems().addAll(cmacLengths);

        scp80KeysetLabels = FXCollections.observableArrayList();
        for (SCP80Keyset scp80Keyset : root.getRunSettings().getScp80Keysets())
            scp80KeysetLabels.add(scp80Keyset.getKeysetName());

        txtUdhiFirstByte.setText(root.getRunSettings().getSmsUpdate().getUdhiFirstByte());
        txtScAddress.setText(root.getRunSettings().getSmsUpdate().getScAddress());
        txtTpPid.setText(root.getRunSettings().getSmsUpdate().getTpPid());
        chkTpOa.setSelected(root.getRunSettings().getSmsUpdate().isUseWhiteList());
        handleTpOaCheck();
        txtTpOa.setText(root.getRunSettings().getSmsUpdate().getTpOa());

        // initialize list of PoR format
        cmbPorFormat.getItems().clear();
        List<String> porFormats = new ArrayList<>();
        porFormats.add("PoR as SMS-DELIVER-REPORT");
        porFormats.add("PoR as SMS-SUBMIT");
        cmbPorFormat.getItems().addAll(porFormats);
        cmbPorFormat.setValue(root.getRunSettings().getSmsUpdate().getPorFormat());
    }

    private void showMappings(VariableMapping mapping) {
        if (mapping != null) {
            // fill mappings with info from mapping object
            txtMappedTo.setText(mapping.getMappedVariable());
            if (mapping.isFixed()) {
                chkFixedVal.setSelected(true);
                txtMccVar.setText(mapping.getValue());
                cmbMccVar.setValue(null);
            } else {
                chkFixedVal.setSelected(false);
                cmbMccVar.setValue(mapping.getMccVariable());
                txtMccVar.setText("");
            }
        } else {
            txtMappedTo.setText("");
            txtMccVar.setText("");
            cmbMccVar.setValue(null);
        }
    }

    private void showKeyset(SCP80Keyset keyset) {
        if (keyset != null) {
            txtKeysetName.setText(keyset.getKeysetName());
            cmbKeysetVersion.setValue(Integer.toString(keyset.getKeysetVersion()));
            cmbKeysetType.setValue(keyset.getKeysetType());
            handleKeysetTypeSelection();
            cmbKicValuation.setValue(keyset.getKicValuation());
            cmbKicLength.setValue(String.valueOf(keyset.getKicKeyLength()));
            cmbKicMode.setValue(keyset.getKicMode());
            cmbKidValuation.setValue(keyset.getKidValuation());
            cmbKidLength.setValue(String.valueOf(keyset.getKidKeyLength()));
            cmbKidMode.setValue(keyset.getKidMode());
            cmbCmacLength.setValue(String.valueOf(keyset.getCmacLength()));
        }
        else {
            txtKeysetName.setText("");
            cmbKeysetVersion.setValue(null);
            cmbKeysetType.setValue(null);
            cmbKicValuation.setValue(null);
            cmbKicLength.setValue(null);
            cmbKicMode.setValue(null);
            cmbKidValuation.setValue(null);
            cmbKidLength.setValue(null);
            cmbKidMode.setValue(null);
            cmbCmacLength.setValue(null);
        }
    }

    public ComboBox<String> getCmbMccVar() {
        return cmbMccVar;
    }

    public MaskerPane getMaskerPane() {
        return maskerPane;
    }

    public TabPane getTabBottom() {
        return tabBottom;
    }

    public TextFlow getTxtInterpretedLog() {
        return txtInterpretedLog;
    }

    public TextArea getTxtCommandResponse() {
        return txtCommandResponse;
    }

    public ObservableList<String> getMappedVariables() {
        return mappedVariables;
    }

    public ObservableList<String> getScp80KeysetLabels() {
        return scp80KeysetLabels;
    }

    public void registerForComboUpdate(ComboBox<String> comboBox) {
        comboPool.add(comboBox);
    }

    private void updateListForComboBoxes() {
        for (ComboBox<String> comboBox : comboPool) {
            comboBox.setItems(mappedVariables);
        }
    }

    @FXML private void handleBtnUpdateMapping() {
        if (mappedVariableExist(txtMappedTo.getText())) {
            VariableMapping selectedMapping = tblMapping.getSelectionModel().getSelectedItem();
            if (chkFixedVal.isSelected()) {
                selectedMapping.setFixed(true);
                selectedMapping.setMccVariable(null);
                selectedMapping.setValue(txtMccVar.getText());
                logger.info(String.format("Update mapping: %s -> %s", selectedMapping.getMappedVariable(),
                        selectedMapping.getValue()));
            } else {
                selectedMapping.setFixed(false);
                selectedMapping.setMccVariable(cmbMccVar.getValue());
                selectedMapping.setValue(null);
                logger.info(String.format("Update mapping: %s -> %s", selectedMapping.getMappedVariable(),
                        selectedMapping.getMccVariable()));
            }
            tblMapping.getColumns().get(1).setVisible(false);
            tblMapping.getColumns().get(1).setVisible(true);
        } else {
            // add new mapping
            String mappedVariable = txtMappedTo.getText();
            boolean fixed = false;
            String mccVariable = cmbMccVar.getValue();
            String value = null;
            if (chkFixedVal.isSelected()) {
                fixed = true;
                mccVariable = null;
                value = txtMccVar.getText();
            }
            VariableMapping mapping = new VariableMapping(mappedVariable, mccVariable, value, fixed);
            application.getMappings().add(mapping);

            mappedVariables.add(mapping.getMappedVariable());
            updateListForComboBoxes();

            if (mapping.isFixed())
                logger.info(String.format("Add mapping: %s -> %s", mapping.getMappedVariable(),
                        mapping.getValue()));
            else
                logger.info(String.format("Add mapping: %s -> %s", mapping.getMappedVariable(),
                        mapping.getMccVariable()));
        }
    }

    @FXML private void handleBtnDeleteMapping() {
        if (application.getMappings().size() > 0) {
            logger.info(String.format("Delete mapping: %s",
                    tblMapping.getSelectionModel().getSelectedItem().getMappedVariable()));

            mappedVariables.remove(tblMapping.getSelectionModel().getSelectedItem().getMappedVariable());
            updateListForComboBoxes();

            int selectedIndex = tblMapping.getSelectionModel().getSelectedIndex();
            tblMapping.getItems().remove(selectedIndex);
            showMappings(null);
            tblMapping.getSelectionModel().clearSelection();
        }
    }

    @FXML private void handleButtonBrowseProjectFolder() {
        DirectoryChooser projectChooser = new DirectoryChooser();
        projectChooser.setTitle("Select Project Directory");
        String initialDirectory;
        if (root.getRunSettings().getProjectPath() != "C:\\") {
            initialDirectory = new File(root.getRunSettings().getProjectPath()).getAbsolutePath();
            if (initialDirectory == null)
                initialDirectory = "C:\\";
        }
        else
            initialDirectory = "C:\\";
        projectChooser.setInitialDirectory(new File(initialDirectory));
        File projectDir = projectChooser.showDialog(application.getPrimaryStage());
        if (projectDir != null) {
            root.getRunSettings().setProjectPath(projectDir.getAbsolutePath());
            txtProjectFolder.setText(projectDir.getAbsolutePath());
        }
    }

    @FXML private void handleButtonGetAtr() {
        try {
            CardTerminal terminal = root.getTerminalFactory().terminals().list().get(root.getRunSettings().getReaderNumber());
            Card connection = terminal.connect("*");
            javax.smartcardio.ATR atr = connection.getATR();
            byte[] atrBytes = atr.getBytes();
            String atrString = Hex.encodeHexString(atrBytes);
            int bytesLength = atrString.length();
            root.getRunSettings().getAtr().setAtrString(atrString.toUpperCase().substring(0, bytesLength-6)); // ATR
            String statusTck = atrString.substring(bytesLength-6);
            root.getRunSettings().getAtr().setStatus(statusTck.substring(0, 4)); // status
            root.getRunSettings().getAtr().setTck(statusTck.substring(4).toUpperCase()); // TCK
            txtAtr.setText(root.getRunSettings().getAtr().getAtrString());
            lblProtocol.setText("Protocol: " + connection.getProtocol()
                + "; Status: " + root.getRunSettings().getAtr().getStatus()
                + "; TCK: " + root.getRunSettings().getAtr().getTck());
            connection.disconnect(false);

        } catch (CardException e) {
//            e.printStackTrace();
            logger.error(e.getMessage());
            Alert cardAlert = new Alert(Alert.AlertType.ERROR);
            cardAlert.initModality(Modality.APPLICATION_MODAL);
            cardAlert.initOwner(application.getPrimaryStage());
            cardAlert.setTitle("Card Error");
            cardAlert.setHeaderText("Failed to get ATR");
            cardAlert.setContentText(e.getMessage());
            cardAlert.showAndWait();
        }
    }

    @FXML private void handleIncludeAtrCheck() { root.getMenuAtr().setDisable(!chkIncludeAtr.isSelected()); }

    private boolean mappedVariableExist(String testMappedVariable) {
        for (VariableMapping mapping : application.getMappings()) {
            if (mapping.getMappedVariable().equals(testMappedVariable))
                return true;
        }
        return false;
    }

    @FXML private void handleKeysetTypeSelection() {
        if (cmbKeysetType.getSelectionModel().getSelectedItem().equals("AES")) {
            lblCmacLength.setDisable(false);
            cmbCmacLength.setDisable(false);
            cmbKicMode.setValue("AES - CBC");
            cmbKidMode.setValue("AES - CMAC");
        } else {
            lblCmacLength.setDisable(true);
            cmbCmacLength.setDisable(true);
        }
    }

    @FXML private void handleButtonAddScp80Keyset() {
        if (keysetExists(txtKeysetName.getText())) {
            lblAddKeysetErrMsg.setVisible(true);
            lblAddKeysetErrMsg.setText("Keyset with same name already exists.");
        } else {
            // add new keyset
            String name = txtKeysetName.getText();
            int version = Integer.parseInt(cmbKeysetVersion.getValue());
            String type = cmbKeysetType.getValue();
            int cmacLength = 0;
            if (type.equals("AES"))
                cmacLength = Integer.parseInt(cmbCmacLength.getValue());
            String kicVal = cmbKicValuation.getValue();
            int kicLength = Integer.parseInt(cmbKicLength.getValue());
            String kicMode = cmbKicMode.getValue();
            String kidVal = cmbKidValuation.getValue();
            int kidLength = Integer.parseInt(cmbKidLength.getValue());
            String kidMode = cmbKidMode.getValue();
            SCP80Keyset scp80Keyset = new SCP80Keyset(name, version, type, kicVal, kicLength, kicMode, kidVal, kidLength, kidMode, cmacLength);
            root.getScp80Keysets().add(scp80Keyset);

            scp80KeysetLabels.add(scp80Keyset.getKeysetName());

            lblAddKeysetErrMsg.setVisible(false);
            logger.info("Added SCP-80 keyset: " + scp80Keyset.toJson());
        }
    }

    @FXML private void handleButtonDeleteScp80Keyset() {
        if (root.getScp80Keysets().size() > 0) {
            logger.info("Delete SCP-80 keyset: " + tblScp80Keyset.getSelectionModel().getSelectedItem().getKeysetName());

            scp80KeysetLabels.remove(tblScp80Keyset.getSelectionModel().getSelectedItem().getKeysetName());

            int selectedIndex = tblScp80Keyset.getSelectionModel().getSelectedIndex();
            tblScp80Keyset.getItems().remove(selectedIndex);
            showKeyset(null);
            tblScp80Keyset.getSelectionModel().clearSelection();
        }
    }

    private boolean keysetExists(String checkKeysetName) {
        for (SCP80Keyset keyset : root.getScp80Keysets()) {
            if (keyset.getKeysetName().equals(checkKeysetName))
                return true;
        }
        return false;
    }

    @FXML private void handleTpOaCheck() {
        if (chkTpOa.isSelected()) {
            lblTpOa.setDisable(false);
            txtTpOa.setDisable(false);
        } else {
            lblTpOa.setDisable(true);
            txtTpOa.setDisable(true);
        }
    }

    @FXML private void handleIccidContextMenu() { cmbIccid.setItems(cardiotest.getMappedVariables()); }

    public void saveControlState() {
        // project details
        root.getRunSettings().setProjectPath(txtProjectFolder.getText());
        root.getRunSettings().setRequestId(txtRequestId.getText());
        root.getRunSettings().setRequestName(txtRequestName.getText());
        root.getRunSettings().setProfileName(txtProfileName.getText());
        root.getRunSettings().setProfileVersion(Integer.parseInt(txtProfileVersion.getText()));
        root.getRunSettings().setCardImageItemId(txtCardImageItemId.getText());
        root.getRunSettings().setCustomer(txtCustomer.getText());
        root.getRunSettings().setDeveloperName(txtDeveloperName.getText());
        root.getRunSettings().setTesterName(txtTesterName.getText());

        // ATR
        root.getRunSettings().getAtr().setAtrString(txtAtr.getText());
        root.getRunSettings().getAtr().setIncludeAtr(chkIncludeAtr.isSelected());

        // card parameters
        root.getRunSettings().getCardParameters().setCardManagerAid(txtCardManagerAid.getText());
        root.getRunSettings().getCardParameters().setUsimAid(txtUsimAid.getText());
        root.getRunSettings().getCardParameters().setDfUsim(txtDfUsim.getText());
        root.getRunSettings().getCardParameters().setDfGsm(txtDfGsm.getText());
        root.getRunSettings().getCardParameters().setDfGsmAccess(txtDfGsmAccess.getText());
        root.getRunSettings().getCardParameters().setDfTelecom(txtDfTelecom.getText());
        root.getRunSettings().getCardParameters().setIsimAid(txtIsimAid.getText());
        root.getRunSettings().getCardParameters().setDfIsim(txtDfIsim.getText());
        root.getRunSettings().getCardParameters().setCsimAid(txtCsimAid.getText());
        root.getRunSettings().getCardParameters().setDfCsim(txtDfCsim.getText());
        root.getRunSettings().getCardParameters().setIccid(cmbIccid.getSelectionModel().getSelectedItem());

        // SMS update settings
        root.getRunSettings().getSmsUpdate().setUdhiFirstByte(txtUdhiFirstByte.getText());
        root.getRunSettings().getSmsUpdate().setScAddress(txtScAddress.getText());
        root.getRunSettings().getSmsUpdate().setTpPid(txtTpPid.getText());
        root.getRunSettings().getSmsUpdate().setUseWhiteList(chkTpOa.isSelected());
        root.getRunSettings().getSmsUpdate().setTpOa(txtTpOa.getText());
        root.getRunSettings().getSmsUpdate().setPorFormat(cmbPorFormat.getSelectionModel().getSelectedItem());

        // secret codes values
        secretCodesController.saveControlState();

        // authentication settings
        authenticationController.saveControlState();

        // RFM USIM
        rfmUsimController.saveControlState();

        // RFM GSM
        rfmGsmController.saveControlState();

        // RFM Isim
        rfmIsimController.saveControlState();

        //Custom RFM --------------------------------------
        rfmCustomController.saveControlState();

        //RAM
        ramController.saveControlState();

        // file management settings
        fileManagementController.saveControlState();

        // file management settings
        fileManagement2Controller.saveControlState();

    }

}
