package com.idemia.tec.jkt.cardiotest.service;

import com.idemia.tec.jkt.cardiotest.controller.FileManagementController;
import com.idemia.tec.jkt.cardiotest.controller.RootLayoutController;
import com.idemia.tec.jkt.cardiotest.model.FMLinkFiles;
import com.idemia.tec.jkt.cardiotest.model.FileManagement2;
import javafx.collections.ObservableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileManagement2Service {

    @Autowired private RootLayoutController root;
    @Autowired private FileManagementController FMCon;

    ObservableList <FMLinkFiles> allFMLinkFiles;


    public StringBuilder generateFilemanagement2HighStressArea(FileManagement2 fileManagement2) {

        StringBuilder highSTressAreaTestBuffer = new StringBuilder();
        highSTressAreaTestBuffer.append(
                ";===================== \n"
                        + ";High Stress Area Test\n"
                        + ";=====================\n\n"
        );


        highSTressAreaTestBuffer.append(
                        ".CALL Mapping.txt /LIST_OFF\n"
                        + ".CALL Options.txt /LIST_OFF\n\n"
                        + ".POWER_ON\n"
        );

        return highSTressAreaTestBuffer;
    }

}
