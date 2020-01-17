package com.lockward.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class ChatClient extends Thread {
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private Socket client;
	private String username;

	public ChatClient(String username, Socket client) throws IOException {
		this.client = client;
		this.username = username;

		output = new ObjectOutputStream(client.getOutputStream());
		input = new ObjectInputStream(client.getInputStream());

		sendMessage(new Message(MessageType.REGISTER, "carrier has arrived", username));
	}

	public ChatClient(String username, String host, int timeout) throws UnknownHostException, IOException {
		this(username, new Socket(host, timeout));
	}

	@Override
	public void run() {
		String line;
		System.out.println("I'm running");

		try {
			while (true) {
				line = (String) input.readObject();

				if (line.equalsIgnoreCase("exit")) {
					break;
				}

				// output.println(line);
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Client Error: " + e.getMessage());
		}
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setSoTimeout(int millis) throws SocketException {
		if (client != null && client.isConnected()) {
			client.setSoTimeout(millis);
		}
	}

	public boolean isClosed() {
		return client.isClosed();
	}

	public String getUsername() {
		return this.username;
	}

	public void sendMessage(Message message) throws IOException {
		System.out.println("Sending message: " + message.getMessage());
		output.writeObject(message);
		output.flush();
	}

	public void sendMessage(MessageType messageType, String msg) throws IOException {
		sendMessage(new Message(messageType, msg, this.username));
	}

	public void close() throws IOException {
		client.close();
		this.interrupt();
	}
}
