package com.lockward.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.lockward.observer.InputObserver;

public class ChatClient extends Thread {
	private ObjectInputStream input;
	private ObjectOutputStream output;
	private Socket client;
	private String username;
	private List<InputObserver> observers = new ArrayList<>();
	private MessageBuilder builder = new MessageBuilder();

	public ChatClient(String username, Socket client) throws IOException {
		this.client = client;
		this.username = username;

		output = new ObjectOutputStream(client.getOutputStream());
		// input = new ObjectInputStream(client.getInputStream());

		sendMessage(builder.messageType(MessageType.REGISTER).msg("Carrier has arrived").username(username).build());
	}

	public ChatClient(String username, String host, int timeout) throws UnknownHostException, IOException {
		this(username, new Socket(host, timeout));
	}

	@Override
	public void run() {
		Message message = null;
		try {
			while (true) {
				input = new ObjectInputStream(client.getInputStream());
				message = (Message) input.readObject();
				notify(message);
				input.close();
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("Client Error: " + e.getMessage());
		}
	}

	public boolean register(InputObserver observer) {
		return observers.add(observer);
	}

	public boolean remove(InputObserver observer) {
		return observers.remove(observer);
	}

	private void notify(Message message) {
		for (InputObserver observer : observers) {
			observer.update(message);
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

	// public void sendMessage(MessageType messageType, String msg) throws
	// IOException {
	// sendMessage(new Message(messageType, msg, this.username));
	// }

	public void close() throws IOException {
		client.close();
		this.interrupt();
	}

	public Message receiveMessage() throws ClassNotFoundException, IOException {
		System.out.println("Receiving transmition...");
		Message message = (Message) input.readObject();
		return message;
	}
}
