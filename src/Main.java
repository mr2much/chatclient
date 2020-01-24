import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.SocketTimeoutException;

import com.lockward.controller.ChatClientController;
import com.lockward.model.ChatClient;
import com.lockward.model.Message;
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
import javafx.stage.WindowEvent;

public class Main extends Application {

	private ChatClientController clientController = new ChatClientController();
	private ChatClient chatClient;
	private String clientStatus = "Offline";
	private Scene mainScene;
	private ObjectInputStream input;
	private TextArea chatBox;
	private Message outgoing = null;

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

		chatBox = new TextArea();
		chatBox.setEditable(false);

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

					chatClient = clientController.connect(txtUsername.getText(), "172.26.150.23", 5000);

					connectToChat();

					displayConnectionStatus();
				} catch (SocketTimeoutException ex) {
					System.out.println("Socket connection timed out");
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					System.out.println("Client error: " + ex.getMessage());
				} catch (ClassNotFoundException ex) {
					System.out.println("Error reading message: " + ex.getMessage());
				}
			}
		});
		Button btnClose = new Button("Close");

		btnClose.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				try {
					clientController.close(chatClient);
				} catch (IOException ex) {
					System.out.println("Client error: " + ex.getMessage());
				}
				displayConnectionStatus();
			}
		});

		Button btnExit = new Button("Exit");

		btnExit.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				try {
					clientController.close(chatClient);
				} catch (IOException ex) {
					System.out.println("Client error: " + ex.getMessage());
				}
				stage.close();
			}
		});

		hbButtons.getChildren().addAll(btnConnect, btnClose, btnExit);

		grid.add(vbox, 0, 0);
		grid.add(hbButtons, 0, 1);

		VBox msgArea = new VBox(10);

		TextArea messageBox = new TextArea();
		messageBox.setPromptText("Digite su mensaje");

		Button btnSend = new Button("Send");

		btnSend.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				try {
					outgoing = new Message(MessageType.TEXT, messageBox.getText(), chatClient.getUsername());
					chatClient.sendMessage(outgoing);
					Message message = clientController.receiveMessage(chatClient);

					if (message != null) {
						System.out.println("Mensaje recibido");
						chatBox.appendText(message.getUsername() + ": " + message.getMessage() + "\n");
					}

					messageBox.clear();
				} catch (IOException ex) {
					System.out.println(
							"Client Error Sending Message: " + ex.getMessage() + "\nMessage: " + outgoing.getMessage());
				} catch (ClassNotFoundException ex) {
					System.out.println("Error reading message: " + ex.getMessage());
				}
			}

		});

		msgArea.getChildren().addAll(messageBox, btnSend);

		grid.add(msgArea, 0, 2);

		Scene scene = new Scene(grid, 500, 600);
		mainScene = scene;

		stage.setScene(scene);
		stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_HIDING, this::closeConnection);
		stage.show();
	}

	private void connectToChat() throws ClassNotFoundException {
		Message message = null;
		try {
			input = clientController.getObjectInputStream(chatClient);

			while ((message = (Message) input.readObject()) != null) {
				chatBox.appendText(message.getUsername() + ": " + message.getMessage() + "\n");
			}
		} catch (IOException e) {
			System.out.println("Error opening input stream: " + e.getMessage());
		}
	}

	private void displayConnectionStatus() {
		if (chatClient != null && !chatClient.isClosed()) {
			clientStatus = "Online";
			System.out.println(clientStatus);
		} else {
			clientStatus = "Offline";
		}

		Stage stage = (Stage) mainScene.getWindow();
		stage.setTitle("Chat - " + clientStatus);
	}

	private void closeConnection(WindowEvent event) {
		if (chatClient != null) {
			try {
				System.out.println("Closing connection");
				chatClient.sendMessage(new Message(MessageType.LOGOFF,
						chatClient.getUsername() + " has left the building", chatClient.getUsername()));
				chatClient.close();
			} catch (IOException e) {
				System.out.println("Error closing client connection: " + e.getMessage());
			}
		}
	}
}
