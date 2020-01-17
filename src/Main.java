import java.io.IOException;
import java.net.SocketTimeoutException;

import com.lockward.controller.ChatClientController;
import com.lockward.model.ChatClient;
import com.lockward.model.MessageType;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

	private ChatClientController clientController = new ChatClientController();
	private ChatClient chatClient;
	private String clientStatus = "Offline";
	private Scene mainScene;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		GridPane grid = new GridPane();
		grid.setPadding(new Insets(15, 15, 15, 15));
		grid.setVgap(10);
		grid.setHgap(10);
		grid.setAlignment(Pos.CENTER);
		// grid.setGridLinesVisible(true);

		stage.setTitle("Chat - " + clientStatus);

		VBox vbox = new VBox(10);
		vbox.setAlignment(Pos.CENTER);

		TextArea chatBox = new TextArea();

		vbox.getChildren().addAll(chatBox);

		HBox hbButtons = new HBox(10);
		hbButtons.setAlignment(Pos.CENTER_RIGHT);

		Label lblUsername = new Label("Username:");

		hbButtons.getChildren().add(lblUsername);

		TextField txtUsername = new TextField();

		hbButtons.getChildren().add(txtUsername);

		Button btnConnect = new Button("Connect");

		btnConnect.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				try {
//					ChatClient chatClient = new ChatClient(txtUsername.getText(), "172.26.150.23", 5000);
//					chatClient.setSoTimeout(5000);
					clientController.connect(txtUsername.getText(), "172.26.150.23", 5000);
//					clientController.sendMessage(MessageType.REGISTER, "carrier has arrived");
					// chatClient = new ChatClient(new Socket("172.26.150.23",
					// 5000));
					// chatClient.setSoTimeout(5000);
					displayConnectionStatus();
				} catch (SocketTimeoutException ex) {
					System.out.println("Socket connection timed out");
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					System.out.println("Client error: " + ex.getMessage());
				}
			}
		});
		Button btnClose = new Button("Close");

		btnClose.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				clientController.close();
				displayConnectionStatus();
			}
		});

		Button btnExit = new Button("Exit");

		btnExit.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				clientController.close();
				stage.close();
			}
		});

		hbButtons.getChildren().addAll(btnConnect, btnClose, btnExit);

		grid.add(vbox, 0, 0);
		grid.add(hbButtons, 0, 1);

		Scene scene = new Scene(grid, 500, 600);
		mainScene = scene;

		stage.setScene(scene);
		stage.show();
	}

	private void displayConnectionStatus() {
		if (chatClient != null && chatClient.isConnected()) {
			clientStatus = "Online";
		} else {
			clientStatus = "Offline";
		}

		Stage stage = (Stage) mainScene.getWindow();
		stage.setTitle("Chat - " + clientStatus);
	}
}
