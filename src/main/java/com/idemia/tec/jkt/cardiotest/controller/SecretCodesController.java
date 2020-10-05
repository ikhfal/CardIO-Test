package com.idemia.tec.jkt.cardiotest.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SecretCodesController {

    static Logger logger = Logger.getLogger(SecretCodesController.class);

    @FXML private CheckBox chkPin1Disabled;
    @FXML private CheckBox chkPin2Disabled;
    @FXML private CheckBox chkInclude3gScript;
    @FXML private CheckBox chkInclude2gScript;
    @FXML private ComboBox<String> cmbGpin;
    @FXML private ComboBox<String> cmbLpin;
    @FXML private ComboBox<String> cmbGpuk;
    @FXML private ComboBox<String> cmbLpuk;
    @FXML private TextField txtGpinRetries;
    @FXML private TextField txtLpinRetries;
    @FXML private TextField txtGpukRetries;
    @FXML private TextField txtLpukRetries;
    @FXML private CheckBox chkBlockGpuk;
    @FXML private CheckBox chkBlockLpuk;
    @FXML private ComboBox<String> cmbChv1;
    @FXML private ComboBox<String> cmbChv2;
    @FXML private ComboBox<String> cmbPuk1;
    @FXML private ComboBox<String> cmbPuk2;
    @FXML private TextField txtChv1Retries;
    @FXML private TextField txtChv2Retries;
    @FXML private TextField txtPuk1Retries;
    @FXML private TextField txtPuk2Retries;
    @FXML private CheckBox chkBlockPuk1;
    @FXML private CheckBox chkBlockPuk2;

    @FXML private CheckBox chkBlockIsc2;
    @FXML private CheckBox chkBlockIsc3;
    @FXML private CheckBox chkBlockIsc4;

    @FXML private Label lblIsc2;
    @FXML private Label lblIsc3;
    @FXML private Label lblIsc4;
    @FXML private ComboBox<String> cmbIsc1;
    @FXML private ComboBox<String> cmbIsc2;
    @FXML private ComboBox<String> cmbIsc3;
    @FXML private ComboBox<String> cmbIsc4;
    @FXML private Label lblIsc2Retries;
    @FXML private Label lblIsc3Retries;
    @FXML private Label lblIsc4Retries;
    @FXML private TextField txtIsc1Retries;
    @FXML private TextField txtIsc2Retries;
    @FXML private TextField txtIsc3Retries;
    @FXML private TextField txtIsc4Retries;
    @FXML private CheckBox chkUseIsc2;
    @FXML private CheckBox chkUseIsc3;
    @FXML private CheckBox chkUseIsc4;

    @Autowired private RootLayoutController root;
    @Autowired private CardiotestController cardiotest;

    public SecretCodesController() {}

    @FXML public void initialize() {
        chkPin1Disabled.setSelected(root.getRunSettings().getSecretCodes().isPin1disabled());
        chkPin2Disabled.setSelected(root.getRunSettings().getSecretCodes().isPin2disabled());

        chkInclude3gScript.setSelected(root.getRunSettings().getSecretCodes().isInclude3gScript());
        handleInclude3gScriptCheck();

        chkInclude2gScript.setSelected(root.getRunSettings().getSecretCodes().isInclude2gScript());
        handleInclude2gScriptCheck();

        if (root.getRunSettings().getSecretCodes().getGpin() != null)
            cmbGpin.getSelectionModel().select(root.getRunSettings().getSecretCodes().getGpin());
        if (root.getRunSettings().getSecretCodes().getGpinRetries() != 0)
            txtGpinRetries.setText(Integer.toString(root.getRunSettings().getSecretCodes().getGpinRetries()));

        if (root.getRunSettings().getSecretCodes().getLpin() != null)
            cmbLpin.getSelectionModel().select(root.getRunSettings().getSecretCodes().getLpin());
        if (root.getRunSettings().getSecretCodes().getLpinRetries() != 0)
            txtLpinRetries.setText(Integer.toString(root.getRunSettings().getSecretCodes().getLpinRetries()));

        if (root.getRunSettings().getSecretCodes().getGpuk() != null)
            cmbGpuk.getSelectionModel().select(root.getRunSettings().getSecretCodes().getGpuk());
        if (root.getRunSettings().getSecretCodes().getGpukRetries() != 0)
            txtGpukRetries.setText(Integer.toString(root.getRunSettings().getSecretCodes().getGpukRetries()));

        if (root.getRunSettings().getSecretCodes().getLpuk() != null)
            cmbLpuk.getSelectionModel().select(root.getRunSettings().getSecretCodes().getLpuk());
        if (root.getRunSettings().getSecretCodes().getLpukRetries() != 0)
            txtLpukRetries.setText(Integer.toString(root.getRunSettings().getSecretCodes().getLpukRetries()));

        if (root.getRunSettings().getSecretCodes().getChv1() != null)
            cmbChv1.getSelectionModel().select(root.getRunSettings().getSecretCodes().getChv1());
        if (root.getRunSettings().getSecretCodes().getChv1Retries() != 0)
            txtChv1Retries.setText(Integer.toString(root.getRunSettings().getSecretCodes().getChv1Retries()));

        if (root.getRunSettings().getSecretCodes().getChv2() != null)
            cmbChv2.getSelectionModel().select(root.getRunSettings().getSecretCodes().getChv2());
        if (root.getRunSettings().getSecretCodes().getChv2Retries() != 0)
            txtChv2Retries.setText(Integer.toString(root.getRunSettings().getSecretCodes().getChv2Retries()));

        if (root.getRunSettings().getSecretCodes().getPuk1() != null)
            cmbPuk1.getSelectionModel().select(root.getRunSettings().getSecretCodes().getPuk1());
        if (root.getRunSettings().getSecretCodes().getPuk1Retries() != 0)
            txtPuk1Retries.setText(Integer.toString(root.getRunSettings().getSecretCodes().getPuk1Retries()));

        if (root.getRunSettings().getSecretCodes().getPuk2() != null)
            cmbPuk2.getSelectionModel().select(root.getRunSettings().getSecretCodes().getPuk2());
        if (root.getRunSettings().getSecretCodes().getPuk2Retries() != 0)
            txtPuk2Retries.setText(Integer.toString(root.getRunSettings().getSecretCodes().getPuk2Retries()));

        chkBlockGpuk.setSelected(root.getRunSettings().getSecretCodes().isBlockGpuk());
        chkBlockLpuk.setSelected(root.getRunSettings().getSecretCodes().isBlockLpuk());
        chkBlockPuk1.setSelected(root.getRunSettings().getSecretCodes().isBlockPuk1());
        chkBlockPuk2.setSelected(root.getRunSettings().getSecretCodes().isBlockPuk2());

        chkBlockIsc2.setSelected(root.getRunSettings().getSecretCodes().isBlockIsc2());
        chkBlockIsc3.setSelected(root.getRunSettings().getSecretCodes().isBlockIsc3());
        chkBlockIsc4.setSelected(root.getRunSettings().getSecretCodes().isBlockIsc4());

        if (root.getRunSettings().getSecretCodes().getIsc1() != null)
            cmbIsc1.getSelectionModel().select(root.getRunSettings().getSecretCodes().getIsc1());
        if (root.getRunSettings().getSecretCodes().getIsc1Retries() != 0)
            txtIsc1Retries.setText(Integer.toString(root.getRunSettings().getSecretCodes().getIsc1Retries()));

        if (root.getRunSettings().getSecretCodes().getIsc2() != null)
            cmbIsc2.getSelectionModel().select(root.getRunSettings().getSecretCodes().getIsc2());
        if (root.getRunSettings().getSecretCodes().getIsc2Retries() != 0)
            txtIsc2Retries.setText(Integer.toString(root.getRunSettings().getSecretCodes().getIsc2Retries()));
        chkUseIsc2.setSelected(root.getRunSettings().getSecretCodes().isUseIsc2());
        handleUseIsc2Check();

        if (root.getRunSettings().getSecretCodes().getIsc3() != null)
            cmbIsc3.getSelectionModel().select(root.getRunSettings().getSecretCodes().getIsc3());
        if (root.getRunSettings().getSecretCodes().getIsc3Retries() != 0)
            txtIsc3Retries.setText(Integer.toString(root.getRunSettings().getSecretCodes().getIsc3Retries()));
        chkUseIsc3.setSelected(root.getRunSettings().getSecretCodes().isUseIsc3());
        handleUseIsc3Check();

        if (root.getRunSettings().getSecretCodes().getIsc4() != null)
            cmbIsc4.getSelectionModel().select(root.getRunSettings().getSecretCodes().getIsc4());
        if (root.getRunSettings().getSecretCodes().getIsc4Retries() != 0)
            txtIsc4Retries.setText(Integer.toString(root.getRunSettings().getSecretCodes().getIsc4Retries()));
        chkUseIsc4.setSelected(root.getRunSettings().getSecretCodes().isUseIsc4());
        handleUseIsc4Check();
    }

    @FXML private void handleGpinContextMenu() { cmbGpin.setItems(cardiotest.getMappedVariables()); }
    @FXML private void handleLpinContextMenu() { cmbLpin.setItems(cardiotest.getMappedVariables()); }
    @FXML private void handleGpukContextMenu() { cmbGpuk.setItems(cardiotest.getMappedVariables()); }
    @FXML private void handleLpukContextMenu() { cmbLpuk.setItems(cardiotest.getMappedVariables()); }
    @FXML private void handleChv1ContextMenu() { cmbChv1.setItems(cardiotest.getMappedVariables()); }
    @FXML private void handleChv2ContextMenu() { cmbChv2.setItems(cardiotest.getMappedVariables()); }
    @FXML private void handlePuk1ContextMenu() { cmbPuk1.setItems(cardiotest.getMappedVariables()); }
    @FXML private void handlePuk2ContextMenu() { cmbPuk2.setItems(cardiotest.getMappedVariables()); }
    @FXML private void handleIsc1ContextMenu() { cmbIsc1.setItems(cardiotest.getMappedVariables()); }
    @FXML private void handleIsc2ContextMenu() { cmbIsc2.setItems(cardiotest.getMappedVariables()); }
    @FXML private void handleIsc3ContextMenu() { cmbIsc3.setItems(cardiotest.getMappedVariables()); }
    @FXML private void handleIsc4ContextMenu() { cmbIsc4.setItems(cardiotest.getMappedVariables()); }

    @FXML private void handleInclude3gScriptCheck() { root.getMenuCodes3g().setDisable(!chkInclude3gScript.isSelected()); }
    @FXML private void handleInclude2gScriptCheck() { root.getMenuCodes2g().setDisable(!chkInclude2gScript.isSelected()); }

    @FXML private void handleUseIsc2Check() {
        if (chkUseIsc2.isSelected()) {
            lblIsc2.setDisable(false);
            cmbIsc2.setDisable(false);
            lblIsc2Retries.setDisable(false);
            txtIsc2Retries.setDisable(false);
            chkBlockIsc2.setDisable(false);
        } else {
            lblIsc2.setDisable(true);
            cmbIsc2.setDisable(true);
            lblIsc2Retries.setDisable(true);
            txtIsc2Retries.setDisable(true);
            chkBlockIsc2.setDisable(true);
        }
    }

    @FXML private void handleUseIsc3Check() {
        if (chkUseIsc3.isSelected()) {
            lblIsc3.setDisable(false);
            cmbIsc3.setDisable(false);
            lblIsc3Retries.setDisable(false);
            txtIsc3Retries.setDisable(false);
            chkBlockIsc3.setDisable(false);
        } else {
            lblIsc3.setDisable(true);
            cmbIsc3.setDisable(true);
            lblIsc3Retries.setDisable(true);
            txtIsc3Retries.setDisable(true);
            chkBlockIsc3.setDisable(true);
        }
    }

    @FXML private void handleUseIsc4Check() {
        if (chkUseIsc4.isSelected()) {
            lblIsc4.setDisable(false);
            cmbIsc4.setDisable(false);
            lblIsc4Retries.setDisable(false);
            txtIsc4Retries.setDisable(false);
            chkBlockIsc4.setDisable(false);
        } else {
            lblIsc4.setDisable(true);
            cmbIsc4.setDisable(true);
            lblIsc4Retries.setDisable(true);
            txtIsc4Retries.setDisable(true);
            chkBlockIsc4.setDisable(true);
        }
    }

    public void saveControlState() {
        root.getRunSettings().getSecretCodes().setInclude3gScript(chkInclude3gScript.isSelected());
        root.getRunSettings().getSecretCodes().setInclude2gScript(chkInclude2gScript.isSelected());

        root.getRunSettings().getSecretCodes().setPin1disabled(chkPin1Disabled.isSelected());
        root.getRunSettings().getSecretCodes().setPin2disabled(chkPin2Disabled.isSelected());

        root.getRunSettings().getSecretCodes().setGpin(cmbGpin.getSelectionModel().getSelectedItem());
        root.getRunSettings().getSecretCodes().setGpinRetries(Integer.parseInt(txtGpinRetries.getText()));
        root.getRunSettings().getSecretCodes().setLpin(cmbLpin.getSelectionModel().getSelectedItem());
        root.getRunSettings().getSecretCodes().setLpinRetries(Integer.parseInt(txtLpinRetries.getText()));
        root.getRunSettings().getSecretCodes().setGpuk(cmbGpuk.getSelectionModel().getSelectedItem());
        root.getRunSettings().getSecretCodes().setGpukRetries(Integer.parseInt(txtGpukRetries.getText()));
        root.getRunSettings().getSecretCodes().setLpuk(cmbLpuk.getSelectionModel().getSelectedItem());
        root.getRunSettings().getSecretCodes().setLpukRetries(Integer.parseInt(txtLpukRetries.getText()));
        root.getRunSettings().getSecretCodes().setChv1(cmbChv1.getSelectionModel().getSelectedItem());
        root.getRunSettings().getSecretCodes().setChv1Retries(Integer.parseInt(txtChv1Retries.getText()));
        root.getRunSettings().getSecretCodes().setChv2(cmbChv2.getSelectionModel().getSelectedItem());
        root.getRunSettings().getSecretCodes().setChv2Retries(Integer.parseInt(txtChv2Retries.getText()));
        root.getRunSettings().getSecretCodes().setPuk1(cmbPuk1.getSelectionModel().getSelectedItem());
        root.getRunSettings().getSecretCodes().setPuk1Retries(Integer.parseInt(txtPuk1Retries.getText()));
        root.getRunSettings().getSecretCodes().setPuk2(cmbPuk2.getSelectionModel().getSelectedItem());
        root.getRunSettings().getSecretCodes().setPuk2Retries(Integer.parseInt(txtPuk2Retries.getText()));

        root.getRunSettings().getSecretCodes().setIsc1(cmbIsc1.getSelectionModel().getSelectedItem());
        root.getRunSettings().getSecretCodes().setIsc1Retries(Integer.parseInt(txtIsc1Retries.getText()));
        root.getRunSettings().getSecretCodes().setIsc2(cmbIsc2.getSelectionModel().getSelectedItem());
        root.getRunSettings().getSecretCodes().setIsc2Retries(Integer.parseInt(txtIsc2Retries.getText()));
        root.getRunSettings().getSecretCodes().setUseIsc2(chkUseIsc2.isSelected());
        root.getRunSettings().getSecretCodes().setIsc3(cmbIsc3.getSelectionModel().getSelectedItem());
        root.getRunSettings().getSecretCodes().setIsc3Retries(Integer.parseInt(txtIsc3Retries.getText()));
        root.getRunSettings().getSecretCodes().setUseIsc3(chkUseIsc3.isSelected());
        root.getRunSettings().getSecretCodes().setIsc4(cmbIsc4.getSelectionModel().getSelectedItem());
        root.getRunSettings().getSecretCodes().setIsc4Retries(Integer.parseInt(txtIsc4Retries.getText()));
        root.getRunSettings().getSecretCodes().setUseIsc4(chkUseIsc4.isSelected());
        root.getRunSettings().getSecretCodes().setBlockGpuk(chkBlockGpuk.isSelected());
        root.getRunSettings().getSecretCodes().setBlockLpuk(chkBlockLpuk.isSelected());
        root.getRunSettings().getSecretCodes().setBlockPuk1(chkBlockPuk1.isSelected());
        root.getRunSettings().getSecretCodes().setBlockPuk2(chkBlockPuk2.isSelected());

        root.getRunSettings().getSecretCodes().setBlockIsc2(chkBlockIsc2.isSelected());
        root.getRunSettings().getSecretCodes().setBlockIsc3(chkBlockIsc3.isSelected());
        root.getRunSettings().getSecretCodes().setBlockIsc4(chkBlockIsc4.isSelected());
    }

}
