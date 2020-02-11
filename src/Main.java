import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import com.lockward.controller.ChatClientController;
import com.lockward.model.ChatClient;
import com.lockward.model.Message;
import com.lockward.model.MessageType;
import com.lockward.observer.InputObserver;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application implements InputObserver {

	private ChatClientController clientController = new ChatClientController();
	private ChatClient chatClient;
	private String clientStatus = "Offline";
	private Scene mainScene;
	private TextArea chatBox;
	private ListView<String> onlineUsers = new ListView<>();
	private Message outgoing = null;
	private MessageHandler messageHandler;
	private static final String host = "172.26.150.23";
	private static final int timeout = 5000;

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

		VBox vbox = new VBox(2);
		vbox.setAlignment(Pos.CENTER_LEFT);

		Label lblChatBox = new Label("Mensajes:");

		chatBox = new TextArea();
		chatBox.setPrefRowCount(40);
		chatBox.setEditable(false);

		vbox.getChildren().addAll(lblChatBox, chatBox);

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
					messageHandler = new MessageHandler(txtUsername.getText());

					if (messageHandler != null) {
						messageHandler.start();

					}

					messageHandler.sendMessage(
							new Message(MessageType.REGISTER, "carrier has arrived", messageHandler.getUsername()));

					displayConnectionStatus();
				} catch (IOException ex) {
					System.out.println("Client error: " + ex.getMessage());
				}

			}
		});
		Button btnClose = new Button("Close");

		btnClose.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				closeClientConnection();

				displayConnectionStatus();

			}
		});

		Button btnExit = new Button("Exit");

		btnExit.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				closeClientConnection();
				stage.close();
			}
		});

		hbButtons.getChildren().addAll(btnConnect, btnClose, btnExit);

		grid.add(vbox, 0, 0);
		grid.add(hbButtons, 0, 1);

		VBox msgArea = new VBox(10);

		TextField messageBox = new TextField();
		messageBox.setPromptText("Digite su mensaje");

		Button btnSend = new Button("Send");

		btnSend.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				try {
					outgoing = new Message(MessageType.TEXT, messageBox.getText(), messageHandler.getUsername());
					messageHandler.sendMessage(outgoing);
					messageBox.clear();
				} catch (IOException ex) {
					System.out.println(
							"Client Error Sending Message: " + ex.getMessage() + "\nMessage: " + outgoing.getMessage());
				}
			}

		});

		msgArea.getChildren().addAll(messageBox, btnSend);

		grid.add(msgArea, 0, 2);

		BorderPane vbUsers = new BorderPane();

		Label lblUsers = new Label("En Linea:");

		vbUsers.setTop(lblUsers);
		vbUsers.setCenter(onlineUsers);

		grid.add(vbUsers, 1, 0, 1, 3);

		Scene scene = new Scene(grid, 580, 600);
		mainScene = scene;

		stage.setScene(scene);
		stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_HIDING, this::closeConnection);
		stage.show();
	}

	private void closeClientConnection() {
		if (messageHandler != null && !messageHandler.isClosed()) {
			try {
				messageHandler.sendMessage(new Message(MessageType.LOGOFF,
						messageHandler.getUsername() + " has left the building", messageHandler.getUsername()));
				messageHandler.close();
				messageHandler.interrupt();

			} catch (IOException ex) {
				System.out.println("Client error: " + ex.getMessage());
			}
		}

	}

	private void displayConnectionStatus() {
		if (messageHandler != null && !messageHandler.isClosed()) {
			clientStatus = "Online";
			System.out.println(clientStatus);
		} else {
			clientStatus = "Offline";
		}

		Stage stage = (Stage) mainScene.getWindow();
		stage.setTitle("Chat - " + clientStatus);
	}

	private void closeConnection(WindowEvent event) {
		if (messageHandler != null) {
			try {
				System.out.println("Closing connection");
				messageHandler.sendMessage(new Message(MessageType.LOGOFF,
						messageHandler.getUsername() + " has left the building", messageHandler.getUsername()));
				messageHandler.close();
			} catch (IOException e) {
				System.out.println("Error closing client connection: " + e.getMessage());
			}
		}
	}

	@Override
	public void update(Message message) {
		System.out.println("Mensaje recibido: " + message.getMessage());
		chatBox.appendText(message.getUsername() + ": " + message.getMessage() + "\n");
	}

	private class MessageHandler extends Thread {

		private ObjectInputStream input;
		private ObjectOutputStream output;
		private Message message;
		private String username;
		private Socket socket;

		public MessageHandler(String username) {
			this.username = username;

			try {
				if (socket == null) {

					socket = new Socket(host, timeout);

					if (output == null) {
						output = new ObjectOutputStream(socket.getOutputStream());
					}

					if (input == null) {
						input = new ObjectInputStream(socket.getInputStream());
					}

				}

			} catch (IOException e) {
				System.out.println("Client Error: " + e.getMessage());
			}
		}

		@Override
		public void run() {

			System.out.println("Client running.");
			try {
				while (true) {
					while ((message = (Message) input.readObject()) != null) {
						System.out.println("Mensaje recibido");
						chatBox.appendText(message.getUsername() + ": " + message.getMessage() + "\n");
					}
				}
			} catch (IOException | ClassNotFoundException e) {
				System.out.println("Client error: " + e.getMessage());
			}

		}

		public void sendMessage(Message message) throws IOException {
			System.out.println("Sending message: " + message.getMessage());
			output.writeObject(message);
			output.flush();
		}

		public void close() throws IOException {
			socket.close();
		}

		public boolean isClosed() {
			if (socket != null) {
				return socket.isClosed();
			}

			return true;

		}

		public String getUsername() {
			return username;
		}
	}
}
