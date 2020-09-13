package com.idemia.tec.jkt.cardiotest.controller;

import com.idemia.tec.jkt.cardiotest.model.AlertBox;
import com.idemia.tec.jkt.cardiotest.model.FM2HighStressArea;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class FileManagement2Controller {

    @Autowired private RootLayoutController root;
    @Autowired private CardiotestController cardiotest;

    @FXML private CheckBox chkIncludeHighStressAreaTest;
    @FXML private TableView<FM2HighStressArea> tblHighStressArea;
    @FXML private TableColumn<FM2HighStressArea, String> clmHighStressArea;
    @FXML private Button addHighStressAreaButton;
    @FXML private Button deleteHighStressAreaButton;
    @FXML private TextField HighStressAreaTextField;

    private ObservableList<FM2HighStressArea> FM2HighStressAreaTableData = FXCollections.observableArrayList();

    public FileManagement2Controller() {}

    public void initialize() {

        initHighStressAreaTable ();
        loadHighStressAreaData ();

        chkIncludeHighStressAreaTest.setSelected(root.getRunSettings().getFileManagement2().isIncludeHighStressAreaTest());

        handleIncludeHighStressAreaCheck();

        HighStressAreaTextField.setPromptText("HSA File Path");

        SaveHighStressAreaData();

    }

    private void initHighStressAreaTable () {

        tblHighStressArea.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        clmHighStressArea.setCellValueFactory(new PropertyValueFactory<FM2HighStressArea, String>("path_HighStressArea"));

        editableHighStressAreaCols();
    }

    private void editableHighStressAreaCols () {
        clmHighStressArea.setCellFactory(TextFieldTableCell.forTableColumn());
        clmHighStressArea.setOnEditCommit( e -> {
            e.getTableView().getItems().get(e.getTablePosition().getRow()).setPath_HighStressArea(e.getNewValue());
        });

        tblHighStressArea.setEditable(true);

    }

    private void loadHighStressAreaData () {

        if (root.getRunSettings().getFileManagement2().getRowHighStressArea() == 0)
        {
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F106F54"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F106F4F"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F14"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F3A"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F4A"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F4F"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F30"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F09"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F4B"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F11"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F12"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F13"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F34"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F35"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F50"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F21"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F22"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F23"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F105F3A4F24"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F05"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F20"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F31"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F38"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F74"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F78"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F7B"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206FAD"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206FAE"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F30"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F3E"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F3F"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F45"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F48"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F50"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F46"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F52"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F53"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206F54"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206FB7"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206FC7"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206FC9"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206FCA"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F206FCB"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F406F9E"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007F406F10"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F31"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F3E"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F3F"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F46"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F78"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06FAD"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F3B"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F4B"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F3C"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F40"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F42"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F43"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F45"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F48"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F50"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F49"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F47"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F4F"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06FC7"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06FC9"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06FCA"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06FCB"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F05"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F08"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F09"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F38"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F7B"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06FB7"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F56"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F57"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F5B"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F5C"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F61"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06FC4"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F73"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F80"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F82"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F81"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06F83"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06FC5"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06FC6"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06FCD"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF06FC3"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF05F3B4F20"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF05F3B4F52"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF05F3B4F63"));
            FM2HighStressAreaTableData.add(new FM2HighStressArea("3F007FF05F3B4F64"));

            tblHighStressArea.setItems(FM2HighStressAreaTableData);
        }

        else
        {
            for (int i=0 ; i<root.getRunSettings().getFileManagement2().getRowHighStressArea() ; i++ )
            {
                FM2HighStressAreaTableData.add(new FM2HighStressArea( root.getRunSettings().getFileManagement2().getData_highStressArea(i)));
            }
            tblHighStressArea.setItems(FM2HighStressAreaTableData);
        }

    }

    public void changePath_HighStressAreaTableDataCellEvent(TableColumn.CellEditEvent edittedCell) {
        FM2HighStressArea pathSelected = tblHighStressArea.getSelectionModel().getSelectedItem();
        pathSelected.setPath_HighStressArea( edittedCell.getNewValue().toString());
    }

    public void deletePathButtonHighStressAreaPusshed() {

        ObservableList<FM2HighStressArea> selectedRows, allPath;
        selectedRows = tblHighStressArea.getSelectionModel().getSelectedItems();

        allPath = tblHighStressArea.getItems();

        for (FM2HighStressArea FM2HighStressArea : selectedRows)
        {
            allPath.remove(FM2HighStressArea);
        }

    }

    public void newPathButtonHighStressAreaPushed () {
        FM2HighStressArea newhighStressArea = new FM2HighStressArea(HighStressAreaTextField.getText());

        short row=0;
        boolean istxtfieldEmpty=false;
        boolean isDataExisted=false;

        for (FM2HighStressArea hsa : FM2HighStressAreaTableData) {
            FM2HighStressArea item =tblHighStressArea.getItems().get(row);

            String data_highStressArea=clmHighStressArea.getCellObservableValue(item).getValue();

            row++;

            if(data_highStressArea.equals(HighStressAreaTextField.getText()))
            { isDataExisted = true; }
        }

        if (HighStressAreaTextField.getText().equals(""))
        {
            AlertBox.display("Warning", "Text Field Empty ! Please add Path Correctly");
            istxtfieldEmpty=true;
        }

        if(isDataExisted)
        {
            AlertBox.display("Warning", "Path already existed");
        }

        if (!istxtfieldEmpty && !isDataExisted)
        {
            tblHighStressArea.getItems().add(newhighStressArea);
            //path_HighStressAreaTextField.clear();
        }

    }

    public void SaveHighStressAreaData() {

        int row_local=0;

        for (FM2HighStressArea hsa : FM2HighStressAreaTableData) {
            FM2HighStressArea item =tblHighStressArea.getItems().get(row_local);

            root.getRunSettings().getFileManagement2().setData_highStressArea(row_local,clmHighStressArea.getCellObservableValue(item).getValue());
            row_local++;
        }


        for (int row_local_2 = row_local ; row_local_2 < 200 ; row_local_2++)
        {
            root.getRunSettings().getFileManagement2().setData_highStressArea(row_local_2,null);
        }

        root.getRunSettings().getFileManagement2().setRowHighStressArea(row_local);

    }

    @FXML private void handleIncludeHighStressAreaCheck() { root.getMenuHighStressArea().setDisable(!chkIncludeHighStressAreaTest.isSelected()); }


    public void saveControlState() {

        root.getRunSettings().getFileManagement2().setIncludeHighStressAreaTest(chkIncludeHighStressAreaTest.isSelected());
        SaveHighStressAreaData();

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

    public CheckBox getChkIncludeHighStressAreaTest() {
        return chkIncludeHighStressAreaTest;
    }

    public void setChkIncludeHighStressAreaTest(CheckBox chkIncludeHighStressAreaTest) {
        this.chkIncludeHighStressAreaTest = chkIncludeHighStressAreaTest;
    }

    public TableView<FM2HighStressArea> getTblHighStressArea() {
        return tblHighStressArea;
    }

    public void setTblHighStressArea(TableView<FM2HighStressArea> tblHighStressArea) {
        this.tblHighStressArea = tblHighStressArea;
    }

    public TableColumn<FM2HighStressArea, String> getClmHighStressArea() {
        return clmHighStressArea;
    }

    public void setClmHighStressArea(TableColumn<FM2HighStressArea, String> clmHighStressArea) {
        this.clmHighStressArea = clmHighStressArea;
    }

    public Button getAddHighStressAreaButton() {
        return addHighStressAreaButton;
    }

    public void setAddHighStressAreaButton(Button addHighStressAreaButton) {
        this.addHighStressAreaButton = addHighStressAreaButton;
    }

    public Button getDeleteHighStressAreaButton() {
        return deleteHighStressAreaButton;
    }

    public void setDeleteHighStressAreaButton(Button deleteHighStressAreaButton) {
        this.deleteHighStressAreaButton = deleteHighStressAreaButton;
    }

    public TextField getHighStressAreaTextField() {
        return HighStressAreaTextField;
    }

    public void setHighStressAreaTextField(TextField highStressAreaTextField) {
        HighStressAreaTextField = highStressAreaTextField;
    }

    public ObservableList<FM2HighStressArea> getHighStressAreaTableData() {
        return FM2HighStressAreaTableData;
    }

    public void setHighStressAreaTableData(ObservableList<FM2HighStressArea> FM2HighStressAreaTableData) {
        this.FM2HighStressAreaTableData = FM2HighStressAreaTableData;
    }




}
