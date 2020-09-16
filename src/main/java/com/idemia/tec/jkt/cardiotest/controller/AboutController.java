package com.idemia.tec.jkt.cardiotest.controller;

import com.idemia.tec.jkt.cardiotest.CardiotestApplication;
import com.idemia.tec.jkt.cardiotest.response.RandomQuoteResponse;
import com.idemia.tec.jkt.cardiotest.service.RandomQuoteService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class AboutController {

    Logger logger = Logger.getLogger(AboutController.class);

    private CardiotestApplication application;
    private RandomQuoteResponse response;
    private ResourceLoader resourceLoader;
    private String exceptionMsg;

    @Autowired RandomQuoteService quoteService;

    @FXML private Label lblContent;
    @FXML private Label lblAuthor;
    @FXML private TextArea txtChangeLog;
    @FXML private TextArea txtCredits;

    public AboutController() {}

    public void setMainApp(CardiotestApplication application) { this.application = application; }

    @Autowired public AboutController(ResourceLoader resourceLoader) { this.resourceLoader = resourceLoader; }

    @FXML private void initialize() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try { response = quoteService.getRandomQuote(); }
                catch (Exception e) { exceptionMsg = e.getMessage(); }
                return null;
            }
            @Override
            protected void succeeded() {
                super.succeeded();
                lblContent.setVisible(true);
                lblContent.setText(response.getContent());
                lblAuthor.setVisible(true);
                lblAuthor.setText("- " + response.getAuthor());
            }
            @Override
            protected void failed() {
                super.failed();
                logger.error(exceptionMsg);
            }
        };
        new Thread(task).start();

        Resource logResource = resourceLoader.getResource("classpath:change.log");
        Resource credResource = resourceLoader.getResource("classpath:credits.txt");
        try {
            InputStream logStream = logResource.getInputStream();
            String logStr = IOUtils.toString(logStream);
            txtChangeLog.setText(logStr);
            IOUtils.closeQuietly(logStream);

            InputStream credStream = credResource.getInputStream();
            String credStr = IOUtils.toString(credStream);
            txtCredits.setText(credStr);
            IOUtils.closeQuietly(credStream);
        }
        catch (IOException e) { e.printStackTrace(); }
    }

}
