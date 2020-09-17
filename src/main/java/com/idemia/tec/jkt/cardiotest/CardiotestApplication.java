package com.idemia.tec.jkt.cardiotest;

import com.idemia.tec.jkt.cardiotest.controller.*;
import com.idemia.tec.jkt.cardiotest.model.AdvSaveVariable;
import com.idemia.tec.jkt.cardiotest.model.CardioUser;
import com.idemia.tec.jkt.cardiotest.model.VariableMapping;
import com.idemia.tec.jkt.cardiotest.service.ActiveDirectoryService;
import com.idemia.tec.jkt.cardiotest.service.UserService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.util.Optional;

@SpringBootApplication
public class CardiotestApplication extends Application {

	private ConfigurableApplicationContext springContext;

	private BorderPane rootLayout;
	private Stage primaryStage;
	private Stage selectReaderDialogStage;
	private Stage toolOptionsDialogStage;
	private Stage importDialogStage;
	private Stage aboutStage;

	private ObservableList<AdvSaveVariable> advSaveVariables = FXCollections.observableArrayList();
	private ObservableList<VariableMapping> mappings = FXCollections.observableArrayList();

	public CardiotestApplication() {}

	public static void main(String[] args) { launch(CardiotestApplication.class, args); }


	@Override
	public void start(Stage primaryStage) throws Exception {
		BasicConfigurator.configure();
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("CARDIO");


		/*
		Optional<CardioUser> cardioUser = domainLogin();
		cardioUser.ifPresent(account -> {
			initRootLayout();
			showCardioTest();
		});
		 */

		initRootLayout();
		showCardioTest();


	}



	public ObservableList<AdvSaveVariable> getAdvSaveVariables() { return advSaveVariables; }
	public ObservableList<VariableMapping> getMappings() { return mappings; }

	@Override public void init() throws Exception { springContext = SpringApplication.run(CardiotestApplication.class); }

	@Override public void stop() throws Exception { springContext.stop(); }

	public void initRootLayout() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/RootLayout.fxml"));
			loader.setControllerFactory(springContext::getBean);
			rootLayout = loader.load();

			// give controller access to main application
			RootLayoutController controller = loader.getController();
			controller.setMainApp(this);

			Scene scene = new Scene(rootLayout);

			primaryStage.setScene(scene);
			primaryStage.show();
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	public void showCardioTest() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/Cardiotest.fxml"));
			loader.setControllerFactory(springContext::getBean);
			AnchorPane cardiotest = loader.load();

			rootLayout.setCenter(cardiotest);

			// give controller access to main application
			CardiotestController controller = loader.getController();
			controller.setMainApp(this);

			controller.setObservableList();
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	public void showImportDialog() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ImportSettings.fxml"));
			loader.setControllerFactory(springContext::getBean);
			AnchorPane importSettings = loader.load();

			// give controller access to main app
			ImportSettingsController controller = loader.getController();
			controller.setMainApp(this);

			// create dialog
			importDialogStage = new Stage();
			importDialogStage.setTitle("Select project directory and variables");
			importDialogStage.setResizable(false);
			importDialogStage.initModality(Modality.WINDOW_MODAL);
			importDialogStage.initOwner(primaryStage);
			Scene scene = new Scene(importSettings);
			importDialogStage.setScene(scene);
			importDialogStage.showAndWait();
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	public void showSelectReader() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SelectReader.fxml"));
			loader.setControllerFactory(springContext::getBean);
			AnchorPane selectReader = loader.load();

			// give controller access to main app
			SelectReaderController controller = loader.getController();
			controller.setMainApp(this);

			// create dialog
			selectReaderDialogStage = new Stage();
			selectReaderDialogStage.setTitle("Select Reader");
			selectReaderDialogStage.setResizable(false);
			selectReaderDialogStage.initModality(Modality.WINDOW_MODAL);
			selectReaderDialogStage.initOwner(primaryStage);
			Scene scene = new Scene(selectReader);
			selectReaderDialogStage.setScene(scene);
			selectReaderDialogStage.showAndWait();
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	public void showToolOptions() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/ToolOptions.fxml"));
			loader.setControllerFactory(springContext::getBean);
			AnchorPane toolOptions = loader.load();

			// give controller access to main app
			ToolOptionsController controller = loader.getController();
			controller.setMainApp(this);

			// create dialog
			toolOptionsDialogStage = new Stage();
			toolOptionsDialogStage.setTitle("Options");
			toolOptionsDialogStage.setResizable(false);
			toolOptionsDialogStage.initModality(Modality.WINDOW_MODAL);
			toolOptionsDialogStage.initOwner(primaryStage);
			Scene scene = new Scene(toolOptions);
			toolOptionsDialogStage.setScene(scene);
			toolOptionsDialogStage.showAndWait();
		}
		catch (Exception e) { e.printStackTrace(); }
	}

	public void showAbout() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/About.fxml"));
			loader.setControllerFactory(springContext::getBean);
			AnchorPane about = loader.load();

			// give controller access to main app
			AboutController controller = loader.getController();
			controller.setMainApp(this);

			// create dialog
			aboutStage = new Stage();
			aboutStage.setTitle("About");
			aboutStage.setResizable(false);
			aboutStage.initModality(Modality.WINDOW_MODAL);
			aboutStage.initOwner(primaryStage);
			Scene scene = new Scene(about);
			aboutStage.setScene(scene);
			aboutStage.showAndWait();
		}
		catch (IOException e) { e.printStackTrace(); }
	}

	public Optional<CardioUser> domainLogin() {
		Dialog<CardioUser> loginDialog = new Dialog<>();
		loginDialog.setTitle("CARDIO Login");
		loginDialog.setHeaderText("Connect with Windows account");
		loginDialog.setGraphic(new ImageView(this.getClass().getResource("/baseline_login_black_48dp.png").toString()));
		ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
		loginDialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

		GridPane grid = new GridPane();
		grid.setHgap(18);
		grid.setVgap(6);
		grid.setPadding(new Insets(40, 40, 40, 40));

		TextField txtDomain = new TextField();
		txtDomain.setPromptText("oberthurcs.com");
		txtDomain.setPrefWidth(220);
		TextField txtUserName = new TextField();
		txtUserName.setPromptText("jakarta\\user");
		txtUserName.setPrefWidth(220);
		PasswordField txtPassword = new PasswordField();
		txtPassword.setPromptText("password");
		txtPassword.setPrefWidth(220);

		grid.add(new Label("Domain"), 0, 0);
		grid.add(txtDomain, 1, 0);
		grid.add(new Label("Username"), 0, 1);
		grid.add(txtUserName, 1, 1);
		grid.add(new Label("Password"), 0, 2);
		grid.add(txtPassword, 1, 2);

		// enable/disable login button depending on whether a username was entered
		Node loginButton = loginDialog.getDialogPane().lookupButton(loginButtonType);
		loginButton.setDisable(true);

		txtUserName.textProperty().addListener((observable, oldValue, newValue) -> {
			loginButton.setDisable(newValue.trim().isEmpty());
		});

		loginDialog.getDialogPane().setContent(grid);

		CardioUser cardioUser = UserService.initUser();
		txtDomain.setText(cardioUser.getDomain());
		txtUserName.setText(cardioUser.getUserName());
		if (!cardioUser.getUserName().equals("")) Platform.runLater(() -> txtPassword.requestFocus());

		loginDialog.setResultConverter(dialogButton -> {
			if (dialogButton == loginButtonType) {
				cardioUser.setUserName(txtUserName.getText());
				cardioUser.setSecurityToken(txtPassword.getText());
				cardioUser.setDomain(txtDomain.getText());
				if (ActiveDirectoryService.authenticate(cardioUser)) {
					cardioUser.setSecurityToken(""); // clear password
					cardioUser.setLoginSuccess(true);
					UserService.saveUser(cardioUser);
					return cardioUser;
				}
				else {
					cardioUser.setSecurityToken(""); // clear password
					cardioUser.setLoginSuccess(false);
					UserService.saveUser(cardioUser);
					Alert accountAlert = new Alert(Alert.AlertType.ERROR);
					accountAlert.setTitle("Login error");
					accountAlert.initOwner(loginButton.getScene().getWindow());
					accountAlert.setHeaderText(null);
					accountAlert.setContentText("Failed connecting to IDEMIA network: Bad credential or user is not part of any organizational unit.");
					accountAlert.showAndWait();
				}
			}
			return null;
		});
		return loginDialog.showAndWait();
	}

	public Stage getPrimaryStage() { return primaryStage; }
	public Stage getSelectReaderDialogStage() { return selectReaderDialogStage; }
	public Stage getToolOptionsDialogStage() { return toolOptionsDialogStage; }
	public Stage getImportDialogStage() { return importDialogStage; }
	public Stage getAboutStage() { return aboutStage; }

}
