import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import com.lockward.model.Message;
import com.lockward.model.MessageBuilder;
import com.lockward.model.MessageType;
import com.lockward.observer.InputObserver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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

	// private ChatClientController clientController = new
	// ChatClientController();
	// private ChatClient chatClient;
	private String clientStatus = "Offline";
	private Scene mainScene;
	private TextArea chatBox;
	private TextField txtUsername = new TextField();
	private TextField txtMessageBox = new TextField();
	private ListView<String> onlineUsers;
	Button btnClose = new Button("Close");
	// private LinkedHashSet<String> userList = new LinkedHashSet<>();
	private ObservableList<String> users = FXCollections.observableArrayList();
	private Message outgoing = null;
	private MessageHandler messageHandler = new MessageHandler();
	private MessageBuilder builder = new MessageBuilder();
	private static final String host = "172.26.150.23";
	private static final int timeout = 5000;

	BooleanBinding usernameBinding = Bindings.isEmpty(txtUsername.textProperty());
	BooleanProperty clientOfflineStatus = new SimpleBooleanProperty(true);

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		btnClose.disableProperty().set(true);
		onlineUsers = new ListView<>(users);
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

		hbButtons.getChildren().add(txtUsername);

		Button btnConnect = new Button("Connect");
		btnConnect.disableProperty().bind(usernameBinding);

		btnConnect.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				try {
					messageHandler = new MessageHandler(txtUsername.getText());

					if (messageHandler != null) {
						messageHandler.start();
						messageHandler.sendMessage(builder.messageType(MessageType.REGISTER).msg("Carrier has arrived")
								.username(messageHandler.getUsername()).build());
					}

					displayConnectionStatus();
				} catch (SocketException ex) {
					showErrorDialog("Client Error", ex.getMessage().split("\n")[0], ex.getMessage().split("\n")[1]);

					System.out.println("Client Error: " + ex.getMessage());
				} catch (IOException ex) {
					System.out.println("Client error: " + ex.getMessage());
					ex.printStackTrace();
				}

			}

		});

		btnClose.disableProperty().bind(clientOfflineStatus);

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
				// closeClientConnection();
				stage.close();
			}
		});

		hbButtons.getChildren().addAll(btnConnect, btnClose, btnExit);

		grid.add(vbox, 0, 0);
		grid.add(hbButtons, 0, 1);

		VBox msgArea = new VBox(10);

		txtMessageBox.setPromptText("Digite su mensaje");

		Button btnSend = new Button("Send");

		// sendMessageBinding.or(new
		// SimpleBooleanProperty(!isClientOnline().getValue()));

		// Buscar una forma de que el status del boton Send de habilite acorde a
		// si el cliente
		// esta conectado o no, y tambien a que si el textbox esta vacio o no
		// quizas solo debe habilitarse acorde con el status de la conexion y no
		// enviar nada
		// cuando el boton se presione sin texto.
		btnSend.disableProperty().bind(clientOfflineStatus);

		btnSend.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				try {
					outgoing = builder.messageType(MessageType.TEXT).msg(txtMessageBox.getText())
							.username(messageHandler.getUsername()).build();
					messageHandler.sendMessage(outgoing);
					txtMessageBox.clear();
				} catch (IOException ex) {
					System.out.println(
							"Client Error Sending Message: " + ex.getMessage() + "\nMessage: " + outgoing.getMessage());
				}
			}

		});

		msgArea.getChildren().addAll(txtMessageBox, btnSend);

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

	private void showErrorDialog(String title, String header, String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(message);
		alert.showAndWait();
	}

	private void closeClientConnection() {
		System.out.println("Valor: " + isClientOnline().getValue());
		if (isClientOnline().getValue()) {
			try {
				messageHandler.sendMessage(builder.messageType(MessageType.LOGOFF)
						.msg(messageHandler.getUsername() + " has left the building")
						.username(messageHandler.getUsername()).build());
				messageHandler.close();
				messageHandler.interrupt();

			} catch (IOException ex) {
				System.out.println("Client error: " + ex.getMessage());
			}
		}

	}

	private void displayConnectionStatus() {
		if (isClientOnline().getValue()) {
			clientStatus = "Online";
			System.out.println(clientStatus);
			clientOfflineStatus.set(false);
		} else {
			clientOfflineStatus.set(true);
			clientStatus = "Offline";
		}

		users.clear();
		Stage stage = (Stage) mainScene.getWindow();
		stage.setTitle("Chat - " + clientStatus);
	}

	private BooleanProperty isClientOnline() {
		return new SimpleBooleanProperty(!messageHandler.isClosed());
	}

	private void closeConnection(WindowEvent event) {
		if (isClientOnline().getValue()) {
			try {
				System.out.println("Closing connection");
				messageHandler.sendMessage(builder.messageType(MessageType.LOGOFF)
						.msg(messageHandler.getUsername() + " has left the building")
						.username(messageHandler.getUsername()).build());
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

		public MessageHandler() {
		}

		public MessageHandler(String username) throws SocketException, IOException {
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

			} catch (SocketException e) {
				throw new SocketException(String
						.format("Cannot connect to host: %s\nMake sure the server is running and try again.", host));
			}
		}

		@Override
		public void run() {
			System.out.println("Client running.");

			while (true) {
				try {

					while ((message = (Message) input.readObject()) != null) {
						System.out.println("Mensaje recibido");
						parseMessage(message);
						chatBox.appendText(message.getUsername() + ": " + message.getMessage() + "\n");
					}

				} catch (IOException e) {
					System.out.println("Client error: " + e.getMessage());
					this.interrupt();
					break;
				} catch (ClassNotFoundException e) {
					System.out.println("Invalid message: " + e.getMessage());
					break;
				}
			}

		}

		private void parseMessage(Message message) {
			System.out.println("Parsing input: " + message.getMessage() + " Type: " + message.getMessageType()
					+ " Sub-Type: " + message.getSubMessageType());

			String username = message.getUsername();

			switch (message.getSubMessageType()) {
			case ADD:
				showNewUserOnList(message);
				break;

			case LOGOFF:
				removeUserFromList(message);
				break;
			default:
				break;
			}
		}

		private void showNewUserOnList(Message message) {

			Platform.runLater(new Runnable() {

				@Override
				public void run() {

					String[] src = (String[]) message.getAttachment();

					if (src != null) {
						users.setAll(src);
					}
				}
			});
		}

		private String obtainUsernameFromMessage(String msg, String pattern) {
			return msg.split(pattern)[0].trim();
		}

		private void removeUserFromList(Message message) {
			String username = obtainUsernameFromMessage(message.getMessage(), "has left the building");

			// avoids Not on FX application thread exception
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					users.remove(username);
				}

			});

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
