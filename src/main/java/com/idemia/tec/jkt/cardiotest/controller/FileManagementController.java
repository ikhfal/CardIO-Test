package com.idemia.tec.jkt.cardiotest.controller;

import com.idemia.tec.jkt.cardiotest.model.AlertBox;
import com.idemia.tec.jkt.cardiotest.model.FMRuwi;
import javafx.scene.control.CheckBox;
import com.idemia.tec.jkt.cardiotest.model.FMLinkFiles;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javafx.scene.control.TextField;

@Component
public class FileManagementController  {

    @Autowired private RootLayoutController root;
    @Autowired private CardiotestController cardiotest;

    @FXML private CheckBox LinkFilesSimbiosCtdCheckbox;
    @FXML private CheckBox chkIncludeLinkFileTest;
    @FXML private TableView<FMLinkFiles> tblLinkFileTest;
    @FXML private TableColumn<FMLinkFiles, String> clmMaster;
    @FXML private TableColumn<FMLinkFiles, String> clmGhost;
    @FXML private Button addLinkFileButton;
    @FXML private Button deleteLinkFileButton;
    @FXML private TextField path_MasterTextField;
    @FXML private TextField path_GhostTextField;

    @FXML private CheckBox chkIncludeRuwiTest;
    @FXML private CheckBox RuwiSimbiosCtdCheckbox;
    @FXML private TableView<FMRuwi> tblRuwi;
    @FXML private TableColumn<FMRuwi, String> clmRuwi;
    @FXML private Button addRuwiButton;
    @FXML private Button deleteRuwiButton;
    @FXML private TextField path_RuwiTextField;

    @FXML private CheckBox chkIncludeSfiTest;
    @FXML private CheckBox SFI_Iccid_2FE2_02_Checkbox , SFI_ARR_2F06_06_Checkbox ;
    @FXML private CheckBox SFI_PL_2F05_05_Checkbox , SFI_Dir_2F00_1E_Checkbox;
    @FXML private CheckBox SFI_ECC_6F7B_01_Checkbox , SFI_StartHFN_6F5B_0F_Checkbox;
    @FXML private CheckBox SFI_LI_6F05_02_Checkbox , SFI_TRESHOLD_6F5C_10_Checkbox;
    @FXML private CheckBox SFI_AD_6FAD_03_Checkbox , SFI_OPLMNwACT_6F61_11_Checkbox;
    @FXML private CheckBox SFI_UST_6F38_04_Checkbox , SFI_HPPLMN_6F31_12_Checkbox;
    @FXML private CheckBox SFI_EST_6F56_05_Checkbox , SFI_HPLMNwACT_6F62_13_Checkbox;
    @FXML private CheckBox SFI_ACC_6F78_06_Checkbox , SFI_ICI_6F80_14_Checkbox;
    @FXML private CheckBox SFI_IMSI_6F07_07_Checkbox , SFI_OCI_6F81_15_Checkbox;
    @FXML private CheckBox SFI_KEYS_6F08_08_Checkbox , SFI_CCP2_6F4F_16_Checkbox;
    @FXML private CheckBox SFI_KEYSPS_6F09_09_Checkbox , SFI_ARR_6F06_17_Checkbox;
    @FXML private CheckBox SFI_PLMNwACT_6F60_0A_Checkbox , SFI_ePDGIdEm_6F65_18_Checkbox;
    @FXML private CheckBox SFI_LOCI_6F7E_0B_Checkbox , SFI_PNN_6FC5_19_Checkbox;
    @FXML private CheckBox SFI_PSLOCI_6F73_0C_Checkbox , SFI_OPL_6FC6_1A_Checkbox;
    @FXML private CheckBox SFI_FPLMN_6F7B_0D_Checkbox , SFI_SPDI_6FCD_1B_Checkbox;
    @FXML private CheckBox SFI_CBMID_6F48_0E_Checkbox , SFI_ACM_6F39_1C_Checkbox;
    @FXML private CheckBox SFI_Kc_4F20_01_Checkbox , SFI_KcGPRS_4F52_02_Checkbox;

    public ObservableList<FMLinkFiles> LinkFilesTableData = FXCollections.observableArrayList();
    private ObservableList<FMRuwi> RuwiTableData = FXCollections.observableArrayList();

    public FileManagementController() {}

    @FXML public void initialize() {
        initLinkFilesTable();
        loadLinkFilesData();

        LinkFilesSimbiosCtdCheckbox.setSelected(root.getRunSettings().getFileManagement().isLinkFilesSimbiosCtd_bool());
        chkIncludeLinkFileTest.setSelected(root.getRunSettings().getFileManagement().isIncludeLinkFilesTest());
        handleIncludeLinkFileCheck();

        path_MasterTextField.setPromptText("File Path [MASTER]");
        path_GhostTextField.setPromptText("File Path [GHOST]");

        SaveLinkFiledata();

        initRuwiTable();
        loadRuwiData();

        chkIncludeRuwiTest.setSelected(root.getRunSettings().getFileManagement().isIncludeRuwiTest());
        RuwiSimbiosCtdCheckbox.setSelected(root.getRunSettings().getFileManagement().isRuwiSimbiosCtd_bool());

        handleIncludeRuwiCheck();

        path_RuwiTextField.setPromptText("File Path");

        //tblRuwi.setEditable(true);
        //clmRuwi.setCellFactory(TextFieldTableCell.forTableColumn());

        SaveRuwiData();

        chkIncludeSfiTest.setSelected(root.getRunSettings().getFileManagement().isIncludeSfiTest());
        handleIncludeSfiCheck();

        InitSFI ();
    }

    private void initLinkFilesTable() {
        //[LINK FILE TEST] This will allowed to select multiple table
        tblLinkFileTest.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        //[LINK FILE TEST] This will allowed to select cell table
        //tblLinkFileTest.getSelectionModel().setCellSelectionEnabled(true);

        //[LINK FILE TEST] setup for columns
        clmMaster.setCellValueFactory(new PropertyValueFactory<FMLinkFiles, String>("path_Master"));
        clmGhost.setCellValueFactory(new PropertyValueFactory<FMLinkFiles, String> ("path_Ghost"));

        editableLinkFilesCols();
    }
    private void editableLinkFilesCols() {
        clmMaster.setCellFactory(TextFieldTableCell.forTableColumn());
        clmMaster.setOnEditCommit( e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setPath_Master(e.getNewValue());
        });

        clmGhost.setCellFactory(TextFieldTableCell.forTableColumn());
        clmGhost.setOnEditCommit( e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setPath_Ghost(e.getNewValue());
        });

        //[LINK FILE TEST] Update the table to allow for the path to be edittable
        tblLinkFileTest.setEditable(true);
    }

    private void loadLinkFilesData() {
        if (root.getRunSettings().getFileManagement().getRow() == 0) {
            //USIM-GSM
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F37" , "3F007F206F37"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F32" , "3F007F206F32"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F07" , "3F007F206F07"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F31" , "3F007F206F31"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F39" , "3F007F206F39"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F3E" , "3F007F206F3E"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F3F" , "3F007F206F3F"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F41" , "3F007F206F41"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F45" , "3F007F206F45"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F46" , "3F007F206F46"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F50" , "3F007F206F50"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F61" , "3F007F206F61"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F62" , "3F007F206F62"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F73" , "3F007F206F53"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F78" , "3F007F206F78"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F7B" , "3F007F206F7B"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F7E" , "3F007F206F7E"));

            //ACCESS-GSM
            LinkFilesTableData.add(new FMLinkFiles("3F007FF05F3B4F20" , "3F007F206F20"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF05F3B4F52" , "3F007F206F52"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF05F3B4F63" , "3F007F206F63"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF05F3B4F64" , "3F007F206F64"));

            //TELCO-GSM
            LinkFilesTableData.add(new FMLinkFiles("3F007F106F54" , "3F007F206F54"));

            //TELCO-PHONEBOOK
            LinkFilesTableData.add(new FMLinkFiles("3F007F106F3A" , "3F007F105F3A4F3A"));
            LinkFilesTableData.add(new FMLinkFiles("3F007F106F4A" , "3F007F105F3A4F4A"));
            LinkFilesTableData.add(new FMLinkFiles("3F007F106F4F" , "3F007F105F3A4F4F"));

            //USIM-TELCO
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F3B" , "3F007F106F3B"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F3C" , "3F007F106F3C"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F42" , "3F007F106F42"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F43" , "3F007F106F43"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F47" , "3F007F106F47"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F49" , "3F007F106F49"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F4B" , "3F007F106F4B"));
            LinkFilesTableData.add(new FMLinkFiles("3F007FF06F4C" , "3F007F106F4C"));

            tblLinkFileTest.setItems(LinkFilesTableData);
        }
        else {
            for (int i=0 ; i<root.getRunSettings().getFileManagement().getRow() ; i++ ) {
                LinkFilesTableData.add(new FMLinkFiles(root.getRunSettings().getFileManagement().getData_master(i), root.getRunSettings().getFileManagement().getData_ghost(i)));
            }
            tblLinkFileTest.setItems(LinkFilesTableData);
        }
    }

    public void changePath_MasterCellEvent(TableColumn.CellEditEvent edittedCell) {
        FMLinkFiles pathSelected = tblLinkFileTest.getSelectionModel().getSelectedItem();
        pathSelected.setPath_Master( edittedCell.getNewValue().toString());
    }
    public void changePath_GhostCellEvent(TableColumn.CellEditEvent edittedCell) {
        FMLinkFiles pathSelected = tblLinkFileTest.getSelectionModel().getSelectedItem();
        pathSelected.setPath_Ghost( edittedCell.getNewValue().toString());
    }

    public void deletePathButtonPusshed() {
        ObservableList<FMLinkFiles> selectedRows, allPath;

        //[LINK FILE TEST] This gives us the rows that were selected
        selectedRows = tblLinkFileTest.getSelectionModel().getSelectedItems();

        allPath = tblLinkFileTest.getItems();

        //[LINK FILE TEST] loop over the selected rows and remove the path from table
        for (FMLinkFiles FMLinkFiles : selectedRows) {
            allPath.remove(FMLinkFiles);
        }

    }
    public void newPathButtonPushed() {
        FMLinkFiles newFMLinkFiles = new FMLinkFiles(path_MasterTextField.getText(),
                                            path_GhostTextField.getText());
        short row=0;
        boolean isMasterandGhostsame=false;
        boolean isMasterorGhostEmpty=false;

        for (FMLinkFiles lf : LinkFilesTableData) {
            FMLinkFiles item =tblLinkFileTest.getItems().get(row);
            String data_ghost=clmGhost.getCellObservableValue(item).getValue();
            String data_master=clmMaster.getCellObservableValue(item).getValue();
            row++;

            if(data_master.equals(path_MasterTextField.getText()) && data_ghost.equals(path_GhostTextField.getText()))
                { isMasterandGhostsame = true; }
        }

        if (path_MasterTextField.getText().equals("") || path_GhostTextField.getText().equals("")) {
            AlertBox.display("Warning", "Please add Master and/or Ghost Path");
            isMasterorGhostEmpty=true;
        }

        if(isMasterandGhostsame) {
            AlertBox.display("Warning", "Master and Ghost Path already existed");
        }

        if (!isMasterandGhostsame && !isMasterorGhostEmpty) {
            tblLinkFileTest.getItems().add(newFMLinkFiles);
            //path_MasterTextField.clear();
            //path_GhostTextField.clear();
        }
    }

    public void SaveLinkFiledata() {
        int row_local=0;

        for (FMLinkFiles lf : LinkFilesTableData) {
            FMLinkFiles item =tblLinkFileTest.getItems().get(row_local);

            root.getRunSettings().getFileManagement().setData_ghost(row_local,clmGhost.getCellObservableValue(item).getValue());
            root.getRunSettings().getFileManagement().setData_master(row_local,clmMaster.getCellObservableValue(item).getValue());
            row_local++;
        }

        for (int row_local_2 = row_local ; row_local_2 < 200 ; row_local_2++) {
            root.getRunSettings().getFileManagement().setData_ghost(row_local_2,null);
            root.getRunSettings().getFileManagement().setData_master(row_local_2,null);
        }

        root.getRunSettings().getFileManagement().setRow(row_local);
    }

    @FXML private void handleIncludeLinkFileCheck() { root.getMenuLinkFile().setDisable(!chkIncludeLinkFileTest.isSelected()); }

    @FXML private void HandleLinkFilesSimbiosCtd() {
        RuwiSimbiosCtdCheckbox.setSelected(LinkFilesSimbiosCtdCheckbox.isSelected());
    }

    @FXML private void HandleRuwiSimbiosCtd() {
        LinkFilesSimbiosCtdCheckbox.setSelected(RuwiSimbiosCtdCheckbox.isSelected());
    }

    private void initRuwiTable() {
        tblRuwi.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        clmRuwi.setCellValueFactory(new PropertyValueFactory<FMRuwi, String>("path_Ruwi"));
        editableRuwiCols();
    }
    private void editableRuwiCols() {
        clmRuwi.setCellFactory(TextFieldTableCell.forTableColumn());
        clmRuwi.setOnEditCommit( e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setPath_Ruwi(e.getNewValue());
        });
        tblRuwi.setEditable(true);
    }

    private void loadRuwiData() {
        if (root.getRunSettings().getFileManagement().getRowRuwi() == 0) {
            RuwiTableData.add(new FMRuwi("3F007F106F54"));
            RuwiTableData.add(new FMRuwi("3F007F106F4F"));
            RuwiTableData.add(new FMRuwi("3F007F105F14"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F3A"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F4A"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F4F"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F30"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F09"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F4B"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F11"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F12"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F13"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F34"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F35"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F50"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F21"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F22"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F23"));
            RuwiTableData.add(new FMRuwi("3F007F105F3A4F24"));
            RuwiTableData.add(new FMRuwi("3F007F206F05"));
            RuwiTableData.add(new FMRuwi("3F007F206F20"));
            RuwiTableData.add(new FMRuwi("3F007F206F31"));
            RuwiTableData.add(new FMRuwi("3F007F206F38"));
            RuwiTableData.add(new FMRuwi("3F007F206F74"));
            RuwiTableData.add(new FMRuwi("3F007F206F78"));
            RuwiTableData.add(new FMRuwi("3F007F206F7B"));
            RuwiTableData.add(new FMRuwi("3F007F206FAD"));
            RuwiTableData.add(new FMRuwi("3F007F206FAE"));
            RuwiTableData.add(new FMRuwi("3F007F206F30"));
            RuwiTableData.add(new FMRuwi("3F007F206F3E"));
            RuwiTableData.add(new FMRuwi("3F007F206F3F"));
            RuwiTableData.add(new FMRuwi("3F007F206F45"));
            RuwiTableData.add(new FMRuwi("3F007F206F48"));
            RuwiTableData.add(new FMRuwi("3F007F206F50"));
            RuwiTableData.add(new FMRuwi("3F007F206F46"));
            RuwiTableData.add(new FMRuwi("3F007F206F52"));
            RuwiTableData.add(new FMRuwi("3F007F206F53"));
            RuwiTableData.add(new FMRuwi("3F007F206F54"));
            RuwiTableData.add(new FMRuwi("3F007F206FB7"));
            RuwiTableData.add(new FMRuwi("3F007F206FC7"));
            RuwiTableData.add(new FMRuwi("3F007F206FC9"));
            RuwiTableData.add(new FMRuwi("3F007F206FCA"));
            RuwiTableData.add(new FMRuwi("3F007F206FCB"));
            RuwiTableData.add(new FMRuwi("3F007F406F9E"));
            RuwiTableData.add(new FMRuwi("3F007F406F10"));
            RuwiTableData.add(new FMRuwi("3F007FF06F31"));
            RuwiTableData.add(new FMRuwi("3F007FF06F3E"));
            RuwiTableData.add(new FMRuwi("3F007FF06F3F"));
            RuwiTableData.add(new FMRuwi("3F007FF06F46"));
            RuwiTableData.add(new FMRuwi("3F007FF06F78"));
            RuwiTableData.add(new FMRuwi("3F007FF06FAD"));
            RuwiTableData.add(new FMRuwi("3F007FF06F3B"));
            RuwiTableData.add(new FMRuwi("3F007FF06F4B"));
            RuwiTableData.add(new FMRuwi("3F007FF06F3C"));
            RuwiTableData.add(new FMRuwi("3F007FF06F40"));
            RuwiTableData.add(new FMRuwi("3F007FF06F42"));
            RuwiTableData.add(new FMRuwi("3F007FF06F43"));
            RuwiTableData.add(new FMRuwi("3F007FF06F45"));
            RuwiTableData.add(new FMRuwi("3F007FF06F48"));
            RuwiTableData.add(new FMRuwi("3F007FF06F50"));
            RuwiTableData.add(new FMRuwi("3F007FF06F49"));
            RuwiTableData.add(new FMRuwi("3F007FF06F47"));
            RuwiTableData.add(new FMRuwi("3F007FF06F4F"));
            RuwiTableData.add(new FMRuwi("3F007FF06FC7"));
            RuwiTableData.add(new FMRuwi("3F007FF06FC9"));
            RuwiTableData.add(new FMRuwi("3F007FF06FCA"));
            RuwiTableData.add(new FMRuwi("3F007FF06FCB"));
            RuwiTableData.add(new FMRuwi("3F007FF06F05"));
            RuwiTableData.add(new FMRuwi("3F007FF06F08"));
            RuwiTableData.add(new FMRuwi("3F007FF06F09"));
            RuwiTableData.add(new FMRuwi("3F007FF06F38"));
            RuwiTableData.add(new FMRuwi("3F007FF06F7B"));
            RuwiTableData.add(new FMRuwi("3F007FF06FB7"));
            RuwiTableData.add(new FMRuwi("3F007FF06F56"));
            RuwiTableData.add(new FMRuwi("3F007FF06F57"));
            RuwiTableData.add(new FMRuwi("3F007FF06F5B"));
            RuwiTableData.add(new FMRuwi("3F007FF06F5C"));
            RuwiTableData.add(new FMRuwi("3F007FF06F61"));
            RuwiTableData.add(new FMRuwi("3F007FF06FC4"));
            RuwiTableData.add(new FMRuwi("3F007FF06F73"));
            RuwiTableData.add(new FMRuwi("3F007FF06F80"));
            RuwiTableData.add(new FMRuwi("3F007FF06F82"));
            RuwiTableData.add(new FMRuwi("3F007FF06F81"));
            RuwiTableData.add(new FMRuwi("3F007FF06F83"));
            RuwiTableData.add(new FMRuwi("3F007FF06FC5"));
            RuwiTableData.add(new FMRuwi("3F007FF06FC6"));
            RuwiTableData.add(new FMRuwi("3F007FF06FCD"));
            RuwiTableData.add(new FMRuwi("3F007FF06FC3"));
            RuwiTableData.add(new FMRuwi("3F007FF05F3B4F20"));
            RuwiTableData.add(new FMRuwi("3F007FF05F3B4F52"));
            RuwiTableData.add(new FMRuwi("3F007FF05F3B4F63"));
            RuwiTableData.add(new FMRuwi("3F007FF05F3B4F64"));

            tblRuwi.setItems(RuwiTableData);
        }
        else {
            for (int i=0; i<root.getRunSettings().getFileManagement().getRowRuwi(); i++) {
                RuwiTableData.add(new FMRuwi( root.getRunSettings().getFileManagement().getData_ruwi(i)));
            }
            tblRuwi.setItems(RuwiTableData);
        }
    }

    public void changePath_RuwiCellEvent(TableColumn.CellEditEvent edittedCell) {
        FMRuwi pathSelected = tblRuwi.getSelectionModel().getSelectedItem();
        pathSelected.setPath_Ruwi( edittedCell.getNewValue().toString());
    }

    public void deletePathButtonRuwiPusshed() {
        ObservableList<FMRuwi> selectedRows, allPath;
        selectedRows = tblRuwi.getSelectionModel().getSelectedItems();

        allPath = tblRuwi.getItems();

        for (FMRuwi ruwiandSfi : selectedRows) {
            allPath.remove(ruwiandSfi);
        }
    }

    public void newPathButtonRuwiPushed() {
        FMRuwi newruwi = new FMRuwi(path_RuwiTextField.getText());

        short row=0;
        boolean istxtfieldEmpty=false;
        boolean isDataExisted=false;

        for (FMRuwi rw : RuwiTableData) {
            FMRuwi item =tblRuwi.getItems().get(row);

            String data_ruwi=clmRuwi.getCellObservableValue(item).getValue();

            row++;

            if(data_ruwi.equals(path_RuwiTextField.getText()))
            { isDataExisted = true; }
        }

        if (path_RuwiTextField.getText().equals("")) {
            AlertBox.display("Warning", "Text Field Empty ! Please add Path Correctly");
            istxtfieldEmpty=true;
        }

        if(isDataExisted) {
            AlertBox.display("Warning", "Path already existed");
        }

        if (!istxtfieldEmpty && !isDataExisted) {
            tblRuwi.getItems().add(newruwi);
            //path_RuwiTextField.clear();
        }
    }

    public void SaveRuwiData() {
        int row_local=0;

        for (FMRuwi rw : RuwiTableData) {
            FMRuwi item =tblRuwi.getItems().get(row_local);

            root.getRunSettings().getFileManagement().setData_ruwi(row_local,clmRuwi.getCellObservableValue(item).getValue());
            row_local++;
        }

        for (int row_local_2 = row_local ; row_local_2 < 200 ; row_local_2++) {
            root.getRunSettings().getFileManagement().setData_ruwi(row_local_2,null);
        }

        root.getRunSettings().getFileManagement().setRowRuwi(row_local);
    }

    @FXML private void handleIncludeRuwiCheck() { root.getMenuRuwi().setDisable(!chkIncludeRuwiTest.isSelected()); }

    @FXML private void handleIncludeSfiCheck() { root.getMenuSfi().setDisable(!chkIncludeSfiTest.isSelected()); }

    private void InitSFI() {
        SFI_Iccid_2FE2_02_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_Iccid_2FE2_02_bool());
        SFI_PL_2F05_05_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_PL_2F05_05_bool());
        //SFI_ECC_6F7B_01_Checkbox.setSelected(root.getRunSettings().getFileManagement().
        SFI_LI_6F05_02_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_LI_6F05_02_bool());
        SFI_AD_6FAD_03_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_AD_6FAD_03_bool());
        SFI_UST_6F38_04_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_UST_6F38_04_bool());
        SFI_EST_6F56_05_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_EST_6F56_05_bool());
        SFI_ACC_6F78_06_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_ACC_6F78_06_bool());
        SFI_IMSI_6F07_07_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_IMSI_6F07_07_bool());
        SFI_KEYS_6F08_08_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_KEYS_6F08_08_bool());
        SFI_KEYSPS_6F09_09_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_KEYSPS_6F09_09_bool());
        SFI_PLMNwACT_6F60_0A_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_PLMNwACT_6F60_0A_bool());
        SFI_LOCI_6F7E_0B_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_LOCI_6F7E_0B_bool());
        SFI_PSLOCI_6F73_0C_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_PSLOCI_6F73_0C_bool());
        SFI_FPLMN_6F7B_0D_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_FPLMN_6F7B_0D_bool());
        SFI_CBMID_6F48_0E_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_CBMID_6F48_0E_bool());
        //SFI_Kc_4F20_01_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_Kc_4F20_01_bool());
        SFI_ARR_2F06_06_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_ARR_2F06_06_bool());
        SFI_Dir_2F00_1E_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_Dir_2F00_1E_bool());
        SFI_StartHFN_6F5B_0F_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_StartHFN_6F5B_0F_bool());
        SFI_TRESHOLD_6F5C_10_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_TRESHOLD_6F5C_10_bool());
        SFI_OPLMNwACT_6F61_11_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_OPLMNwACT_6F61_11_bool());
        SFI_HPPLMN_6F31_12_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_HPPLMN_6F31_12_bool());
        SFI_HPLMNwACT_6F62_13_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_HPLMNwACT_6F62_13_bool());
        SFI_ICI_6F80_14_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_ICI_6F80_14_bool());
        SFI_OCI_6F81_15_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_OCI_6F81_15_bool());
        SFI_CCP2_6F4F_16_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_CCP2_6F4F_16_bool());
        SFI_ARR_6F06_17_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_ARR_6F06_17_bool());
        //SFI_ePDGIdEm_6F65_18_Checkbox.setSelected(root.getRunSettings().getFileManagement().
        SFI_PNN_6FC5_19_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_PNN_6FC5_19_bool());
        SFI_OPL_6FC6_1A_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_OPL_6FC6_1A_bool());
        SFI_SPDI_6FCD_1B_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_SPDI_6FCD_1B_bool());
        SFI_ACM_6F39_1C_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_ACM_6F39_1C_bool());
        //SFI_KcGPRS_4F52_02_Checkbox.setSelected(root.getRunSettings().getFileManagement().isSFI_KcGPRS_4F52_02_bool());
    }

    public void saveControlState() {
        root.getRunSettings().getFileManagement().setIncludeLinkFilesTest(chkIncludeLinkFileTest.isSelected());
        SaveLinkFiledata();
        root.getRunSettings().getFileManagement().setLinkFilesSimbiosCtd_bool(LinkFilesSimbiosCtdCheckbox.isSelected());

        root.getRunSettings().getFileManagement().setIncludeRuwiTest(chkIncludeRuwiTest.isSelected());
        SaveRuwiData();
        root.getRunSettings().getFileManagement().setRuwiSimbiosCtd_bool(RuwiSimbiosCtdCheckbox.isSelected());

        root.getRunSettings().getFileManagement().setIncludeSfiTest(chkIncludeSfiTest.isSelected());
        root.getRunSettings().getFileManagement().setSFI_Iccid_2FE2_02_bool(SFI_Iccid_2FE2_02_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_PL_2F05_05_bool(SFI_PL_2F05_05_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_ECC_6F7B_01_bool(SFI_ECC_6F7B_01_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_LI_6F05_02_bool(SFI_LI_6F05_02_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_AD_6FAD_03_bool(SFI_AD_6FAD_03_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_UST_6F38_04_bool(SFI_UST_6F38_04_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_EST_6F56_05_bool(SFI_EST_6F56_05_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_ACC_6F78_06_bool(SFI_ACC_6F78_06_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_IMSI_6F07_07_bool(SFI_IMSI_6F07_07_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_KEYS_6F08_08_bool(SFI_KEYS_6F08_08_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_KEYSPS_6F09_09_bool(SFI_KEYSPS_6F09_09_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_PLMNwACT_6F60_0A_bool(SFI_PLMNwACT_6F60_0A_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_LOCI_6F7E_0B_bool(SFI_LOCI_6F7E_0B_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_PSLOCI_6F73_0C_bool(SFI_PSLOCI_6F73_0C_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_FPLMN_6F7B_0D_bool(SFI_FPLMN_6F7B_0D_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_CBMID_6F48_0E_bool(SFI_CBMID_6F48_0E_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_Kc_4F20_01_bool(SFI_Kc_4F20_01_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_ARR_2F06_06_bool(SFI_ARR_2F06_06_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_Dir_2F00_1E_bool(SFI_Dir_2F00_1E_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_StartHFN_6F5B_0F_bool(SFI_StartHFN_6F5B_0F_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_TRESHOLD_6F5C_10_bool(SFI_TRESHOLD_6F5C_10_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_OPLMNwACT_6F61_11_bool(SFI_OPLMNwACT_6F61_11_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_HPPLMN_6F31_12_bool(SFI_HPPLMN_6F31_12_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_HPLMNwACT_6F62_13_bool(SFI_HPLMNwACT_6F62_13_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_ICI_6F80_14_bool(SFI_ICI_6F80_14_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_OCI_6F81_15_bool(SFI_OCI_6F81_15_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_CCP2_6F4F_16_bool(SFI_CCP2_6F4F_16_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_ARR_6F06_17_bool(SFI_ARR_6F06_17_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_ePDGIdEm_6F65_18_bool(SFI_ePDGIdEm_6F65_18_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_PNN_6FC5_19_bool(SFI_PNN_6FC5_19_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_OPL_6FC6_1A_bool(SFI_OPL_6FC6_1A_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_SPDI_6FCD_1B_bool(SFI_SPDI_6FCD_1B_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_ACM_6F39_1C_bool(SFI_ACM_6F39_1C_Checkbox.isSelected());
        root.getRunSettings().getFileManagement().setSFI_KcGPRS_4F52_02_bool(SFI_KcGPRS_4F52_02_Checkbox.isSelected());
    }

    //getter and setter

    public RootLayoutController getRoot() {
        return root;
    }

    public void setRoot(RootLayoutController root) {
        this.root = root;
    }

    public CardiotestController getCardiotest() {
        return cardiotest;
    }

    public void setCardiotest(CardiotestController cardiotest) {
        this.cardiotest = cardiotest;
    }

    public CheckBox getChkIncludeLinkFileTest() {
        return chkIncludeLinkFileTest;
    }

    public void setChkIncludeLinkFileTest(CheckBox chkIncludeLinkFileTest) {
        this.chkIncludeLinkFileTest = chkIncludeLinkFileTest;
    }

    public TableView<FMLinkFiles> getTblLinkFileTest() {
        return tblLinkFileTest;
    }

    public void setTblLinkFileTest(TableView<FMLinkFiles> tblLinkFileTest) {
        this.tblLinkFileTest = tblLinkFileTest;
    }

    public TableColumn<FMLinkFiles, String> getClmMaster() {
        return clmMaster;
    }

    public void setClmMaster(TableColumn<FMLinkFiles, String> clmMaster) {
        this.clmMaster = clmMaster;
    }

    public TableColumn<FMLinkFiles, String> getClmGhost() {
        return clmGhost;
    }

    public void setClmGhost(TableColumn<FMLinkFiles, String> clmGhost) {
        this.clmGhost = clmGhost;
    }

    public Button getAddLinkFileButton() {
        return addLinkFileButton;
    }

    public void setAddLinkFileButton(Button addLinkFileButton) {
        this.addLinkFileButton = addLinkFileButton;
    }

    public Button getDeleteLinkFileButton() {
        return deleteLinkFileButton;
    }

    public void setDeleteLinkFileButton(Button deleteLinkFileButton) {
        this.deleteLinkFileButton = deleteLinkFileButton;
    }

    public TextField getPath_MasterTextField() {
        return path_MasterTextField;
    }

    public void setPath_MasterTextField(TextField path_MasterTextField) {
        this.path_MasterTextField = path_MasterTextField;
    }

    public TextField getPath_GhostTextField() {
        return path_GhostTextField;
    }

    public void setPath_GhostTextField(TextField path_GhostTextField) {
        this.path_GhostTextField = path_GhostTextField;
    }

    public CheckBox getChkIncludeRuwiTest() {
        return chkIncludeRuwiTest;
    }

    public void setChkIncludeRuwiTest(CheckBox chkIncludeRuwiTest) {
        this.chkIncludeRuwiTest = chkIncludeRuwiTest;
    }

    public TableView<FMRuwi> getTblRuwi() {
        return tblRuwi;
    }

    public void setTblRuwi(TableView<FMRuwi> tblRuwi) {
        this.tblRuwi = tblRuwi;
    }

    public TableColumn<FMRuwi, String> getClmRuwi() {
        return clmRuwi;
    }

    public void setClmRuwi(TableColumn<FMRuwi, String> clmRuwi) {
        this.clmRuwi = clmRuwi;
    }

    public Button getAddRuwiButton() {
        return addRuwiButton;
    }

    public void setAddRuwiButton(Button addRuwiButton) {
        this.addRuwiButton = addRuwiButton;
    }

    public Button getDeleteRuwiButton() {
        return deleteRuwiButton;
    }

    public void setDeleteRuwiButton(Button deleteRuwiButton) {
        this.deleteRuwiButton = deleteRuwiButton;
    }

    public TextField getPath_RuwiTextField() {
        return path_RuwiTextField;
    }

    public void setPath_RuwiTextField(TextField path_RuwiTextField) {
        this.path_RuwiTextField = path_RuwiTextField;
    }

    public CheckBox getChkIncludeSfiTest() {
        return chkIncludeSfiTest;
    }

    public void setChkIncludeSfiTest(CheckBox chkIncludeSfiTest) {
        this.chkIncludeSfiTest = chkIncludeSfiTest;
    }

    public CheckBox getSFI_Iccid_2FE2_02_Checkbox() {
        return SFI_Iccid_2FE2_02_Checkbox;
    }

    public void setSFI_Iccid_2FE2_02_Checkbox(CheckBox SFI_Iccid_2FE2_02_Checkbox) {
        this.SFI_Iccid_2FE2_02_Checkbox = SFI_Iccid_2FE2_02_Checkbox;
    }

    public CheckBox getSFI_ARR_2F06_06_Checkbox() {
        return SFI_ARR_2F06_06_Checkbox;
    }

    public void setSFI_ARR_2F06_06_Checkbox(CheckBox SFI_ARR_2F06_06_Checkbox) {
        this.SFI_ARR_2F06_06_Checkbox = SFI_ARR_2F06_06_Checkbox;
    }

    public CheckBox getSFI_PL_2F05_05_Checkbox() {
        return SFI_PL_2F05_05_Checkbox;
    }

    public void setSFI_PL_2F05_05_Checkbox(CheckBox SFI_PL_2F05_05_Checkbox) {
        this.SFI_PL_2F05_05_Checkbox = SFI_PL_2F05_05_Checkbox;
    }

    public CheckBox getSFI_Dir_2F00_1E_Checkbox() {
        return SFI_Dir_2F00_1E_Checkbox;
    }

    public void setSFI_Dir_2F00_1E_Checkbox(CheckBox SFI_Dir_2F00_1E_Checkbox) {
        this.SFI_Dir_2F00_1E_Checkbox = SFI_Dir_2F00_1E_Checkbox;
    }

    public CheckBox getSFI_ECC_6F7B_01_Checkbox() {
        return SFI_ECC_6F7B_01_Checkbox;
    }

    public void setSFI_ECC_6F7B_01_Checkbox(CheckBox SFI_ECC_6F7B_01_Checkbox) {
        this.SFI_ECC_6F7B_01_Checkbox = SFI_ECC_6F7B_01_Checkbox;
    }

    public CheckBox getSFI_StartHFN_6F5B_0F_Checkbox() {
        return SFI_StartHFN_6F5B_0F_Checkbox;
    }

    public void setSFI_StartHFN_6F5B_0F_Checkbox(CheckBox SFI_StartHFN_6F5B_0F_Checkbox) {
        this.SFI_StartHFN_6F5B_0F_Checkbox = SFI_StartHFN_6F5B_0F_Checkbox;
    }

    public CheckBox getSFI_LI_6F05_02_Checkbox() {
        return SFI_LI_6F05_02_Checkbox;
    }

    public void setSFI_LI_6F05_02_Checkbox(CheckBox SFI_LI_6F05_02_Checkbox) {
        this.SFI_LI_6F05_02_Checkbox = SFI_LI_6F05_02_Checkbox;
    }

    public CheckBox getSFI_TRESHOLD_6F5C_10_Checkbox() {
        return SFI_TRESHOLD_6F5C_10_Checkbox;
    }

    public void setSFI_TRESHOLD_6F5C_10_Checkbox(CheckBox SFI_TRESHOLD_6F5C_10_Checkbox) {
        this.SFI_TRESHOLD_6F5C_10_Checkbox = SFI_TRESHOLD_6F5C_10_Checkbox;
    }

    public CheckBox getSFI_AD_6FAD_03_Checkbox() {
        return SFI_AD_6FAD_03_Checkbox;
    }

    public void setSFI_AD_6FAD_03_Checkbox(CheckBox SFI_AD_6FAD_03_Checkbox) {
        this.SFI_AD_6FAD_03_Checkbox = SFI_AD_6FAD_03_Checkbox;
    }

    public CheckBox getSFI_OPLMNwACT_6F61_11_Checkbox() {
        return SFI_OPLMNwACT_6F61_11_Checkbox;
    }

    public void setSFI_OPLMNwACT_6F61_11_Checkbox(CheckBox SFI_OPLMNwACT_6F61_11_Checkbox) {
        this.SFI_OPLMNwACT_6F61_11_Checkbox = SFI_OPLMNwACT_6F61_11_Checkbox;
    }

    public CheckBox getSFI_UST_6F38_04_Checkbox() {
        return SFI_UST_6F38_04_Checkbox;
    }

    public void setSFI_UST_6F38_04_Checkbox(CheckBox SFI_UST_6F38_04_Checkbox) {
        this.SFI_UST_6F38_04_Checkbox = SFI_UST_6F38_04_Checkbox;
    }

    public CheckBox getSFI_HPPLMN_6F31_12_Checkbox() {
        return SFI_HPPLMN_6F31_12_Checkbox;
    }

    public void setSFI_HPPLMN_6F31_12_Checkbox(CheckBox SFI_HPPLMN_6F31_12_Checkbox) {
        this.SFI_HPPLMN_6F31_12_Checkbox = SFI_HPPLMN_6F31_12_Checkbox;
    }

    public CheckBox getSFI_EST_6F56_05_Checkbox() {
        return SFI_EST_6F56_05_Checkbox;
    }

    public void setSFI_EST_6F56_05_Checkbox(CheckBox SFI_EST_6F56_05_Checkbox) {
        this.SFI_EST_6F56_05_Checkbox = SFI_EST_6F56_05_Checkbox;
    }

    public CheckBox getSFI_HPLMNwACT_6F62_13_Checkbox() {
        return SFI_HPLMNwACT_6F62_13_Checkbox;
    }

    public void setSFI_HPLMNwACT_6F62_13_Checkbox(CheckBox SFI_HPLMNwACT_6F62_13_Checkbox) {
        this.SFI_HPLMNwACT_6F62_13_Checkbox = SFI_HPLMNwACT_6F62_13_Checkbox;
    }

    public CheckBox getSFI_ACC_6F78_06_Checkbox() {
        return SFI_ACC_6F78_06_Checkbox;
    }

    public void setSFI_ACC_6F78_06_Checkbox(CheckBox SFI_ACC_6F78_06_Checkbox) {
        this.SFI_ACC_6F78_06_Checkbox = SFI_ACC_6F78_06_Checkbox;
    }

    public CheckBox getSFI_ICI_6F80_14_Checkbox() {
        return SFI_ICI_6F80_14_Checkbox;
    }

    public void setSFI_ICI_6F80_14_Checkbox(CheckBox SFI_ICI_6F80_14_Checkbox) {
        this.SFI_ICI_6F80_14_Checkbox = SFI_ICI_6F80_14_Checkbox;
    }

    public CheckBox getSFI_IMSI_6F07_07_Checkbox() {
        return SFI_IMSI_6F07_07_Checkbox;
    }

    public void setSFI_IMSI_6F07_07_Checkbox(CheckBox SFI_IMSI_6F07_07_Checkbox) {
        this.SFI_IMSI_6F07_07_Checkbox = SFI_IMSI_6F07_07_Checkbox;
    }

    public CheckBox getSFI_OCI_6F81_15_Checkbox() {
        return SFI_OCI_6F81_15_Checkbox;
    }

    public void setSFI_OCI_6F81_15_Checkbox(CheckBox SFI_OCI_6F81_15_Checkbox) {
        this.SFI_OCI_6F81_15_Checkbox = SFI_OCI_6F81_15_Checkbox;
    }

    public CheckBox getSFI_KEYS_6F08_08_Checkbox() {
        return SFI_KEYS_6F08_08_Checkbox;
    }

    public void setSFI_KEYS_6F08_08_Checkbox(CheckBox SFI_KEYS_6F08_08_Checkbox) {
        this.SFI_KEYS_6F08_08_Checkbox = SFI_KEYS_6F08_08_Checkbox;
    }

    public CheckBox getSFI_CCP2_6F4F_16_Checkbox() {
        return SFI_CCP2_6F4F_16_Checkbox;
    }

    public void setSFI_CCP2_6F4F_16_Checkbox(CheckBox SFI_CCP2_6F4F_16_Checkbox) {
        this.SFI_CCP2_6F4F_16_Checkbox = SFI_CCP2_6F4F_16_Checkbox;
    }

    public CheckBox getSFI_KEYSPS_6F09_09_Checkbox() {
        return SFI_KEYSPS_6F09_09_Checkbox;
    }

    public void setSFI_KEYSPS_6F09_09_Checkbox(CheckBox SFI_KEYSPS_6F09_09_Checkbox) {
        this.SFI_KEYSPS_6F09_09_Checkbox = SFI_KEYSPS_6F09_09_Checkbox;
    }

    public CheckBox getSFI_ARR_6F06_17_Checkbox() {
        return SFI_ARR_6F06_17_Checkbox;
    }

    public void setSFI_ARR_6F06_17_Checkbox(CheckBox SFI_ARR_6F06_17_Checkbox) {
        this.SFI_ARR_6F06_17_Checkbox = SFI_ARR_6F06_17_Checkbox;
    }

    public CheckBox getSFI_PLMNwACT_6F60_0A_Checkbox() {
        return SFI_PLMNwACT_6F60_0A_Checkbox;
    }

    public void setSFI_PLMNwACT_6F60_0A_Checkbox(CheckBox SFI_PLMNwACT_6F60_0A_Checkbox) {
        this.SFI_PLMNwACT_6F60_0A_Checkbox = SFI_PLMNwACT_6F60_0A_Checkbox;
    }

    public CheckBox getSFI_ePDGIdEm_6F65_18_Checkbox() {
        return SFI_ePDGIdEm_6F65_18_Checkbox;
    }

    public void setSFI_ePDGIdEm_6F65_18_Checkbox(CheckBox SFI_ePDGIdEm_6F65_18_Checkbox) {
        this.SFI_ePDGIdEm_6F65_18_Checkbox = SFI_ePDGIdEm_6F65_18_Checkbox;
    }

    public CheckBox getSFI_LOCI_6F7E_0B_Checkbox() {
        return SFI_LOCI_6F7E_0B_Checkbox;
    }

    public void setSFI_LOCI_6F7E_0B_Checkbox(CheckBox SFI_LOCI_6F7E_0B_Checkbox) {
        this.SFI_LOCI_6F7E_0B_Checkbox = SFI_LOCI_6F7E_0B_Checkbox;
    }

    public CheckBox getSFI_PNN_6FC5_19_Checkbox() {
        return SFI_PNN_6FC5_19_Checkbox;
    }

    public void setSFI_PNN_6FC5_19_Checkbox(CheckBox SFI_PNN_6FC5_19_Checkbox) {
        this.SFI_PNN_6FC5_19_Checkbox = SFI_PNN_6FC5_19_Checkbox;
    }

    public CheckBox getSFI_PSLOCI_6F73_0C_Checkbox() {
        return SFI_PSLOCI_6F73_0C_Checkbox;
    }

    public void setSFI_PSLOCI_6F73_0C_Checkbox(CheckBox SFI_PSLOCI_6F73_0C_Checkbox) {
        this.SFI_PSLOCI_6F73_0C_Checkbox = SFI_PSLOCI_6F73_0C_Checkbox;
    }

    public CheckBox getSFI_OPL_6FC6_1A_Checkbox() {
        return SFI_OPL_6FC6_1A_Checkbox;
    }

    public void setSFI_OPL_6FC6_1A_Checkbox(CheckBox SFI_OPL_6FC6_1A_Checkbox) {
        this.SFI_OPL_6FC6_1A_Checkbox = SFI_OPL_6FC6_1A_Checkbox;
    }

    public CheckBox getSFI_FPLMN_6F7B_0D_Checkbox() {
        return SFI_FPLMN_6F7B_0D_Checkbox;
    }

    public void setSFI_FPLMN_6F7B_0D_Checkbox(CheckBox SFI_FPLMN_6F7B_0D_Checkbox) {
        this.SFI_FPLMN_6F7B_0D_Checkbox = SFI_FPLMN_6F7B_0D_Checkbox;
    }

    public CheckBox getSFI_SPDI_6FCD_1B_Checkbox() {
        return SFI_SPDI_6FCD_1B_Checkbox;
    }

    public void setSFI_SPDI_6FCD_1B_Checkbox(CheckBox SFI_SPDI_6FCD_1B_Checkbox) {
        this.SFI_SPDI_6FCD_1B_Checkbox = SFI_SPDI_6FCD_1B_Checkbox;
    }

    public CheckBox getSFI_CBMID_6F48_0E_Checkbox() {
        return SFI_CBMID_6F48_0E_Checkbox;
    }

    public void setSFI_CBMID_6F48_0E_Checkbox(CheckBox SFI_CBMID_6F48_0E_Checkbox) {
        this.SFI_CBMID_6F48_0E_Checkbox = SFI_CBMID_6F48_0E_Checkbox;
    }

    public CheckBox getSFI_ACM_6F39_1C_Checkbox() {
        return SFI_ACM_6F39_1C_Checkbox;
    }

    public void setSFI_ACM_6F39_1C_Checkbox(CheckBox SFI_ACM_6F39_1C_Checkbox) {
        this.SFI_ACM_6F39_1C_Checkbox = SFI_ACM_6F39_1C_Checkbox;
    }

    public CheckBox getSFI_Kc_4F20_01_Checkbox() {
        return SFI_Kc_4F20_01_Checkbox;
    }

    public void setSFI_Kc_4F20_01_Checkbox(CheckBox SFI_Kc_4F20_01_Checkbox) {
        this.SFI_Kc_4F20_01_Checkbox = SFI_Kc_4F20_01_Checkbox;
    }

    public CheckBox getSFI_KcGPRS_4F52_02_Checkbox() {
        return SFI_KcGPRS_4F52_02_Checkbox;
    }

    public void setSFI_KcGPRS_4F52_02_Checkbox(CheckBox SFI_KcGPRS_4F52_02_Checkbox) {
        this.SFI_KcGPRS_4F52_02_Checkbox = SFI_KcGPRS_4F52_02_Checkbox;
    }

    public ObservableList<FMLinkFiles> getLinkFilesTableData() {
        return LinkFilesTableData;
    }

    public void setLinkFilesTableData(ObservableList<FMLinkFiles> linkFilesTableData) {
        LinkFilesTableData = linkFilesTableData;
    }

    public CheckBox getRuwiSimbiosCtdCheckbox() {
        return RuwiSimbiosCtdCheckbox;
    }

    public void setRuwiSimbiosCtdCheckbox(CheckBox ruwiSimbiosCtdCheckbox) {
        RuwiSimbiosCtdCheckbox = ruwiSimbiosCtdCheckbox;
    }

    public CheckBox getLinkFilesSimbiosCtdCheckbox() {
        return LinkFilesSimbiosCtdCheckbox;
    }

    public void setLinkFilesSimbiosCtdCheckbox(CheckBox linkFilesSimbiosCtdCheckbox) {
        LinkFilesSimbiosCtdCheckbox = linkFilesSimbiosCtdCheckbox;
    }

    public ObservableList<FMRuwi> getRuwiTableData() {
        return RuwiTableData;
    }

    public void setRuwiTableData(ObservableList<FMRuwi> ruwiTableData) {
        RuwiTableData = ruwiTableData;
    }
}
