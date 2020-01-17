package com.lockward.controller;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.lockward.model.ChatClient;
import com.lockward.model.MessageType;

public class ChatClientController {

	private ChatClient chatClient;

	public void close() {
		System.out.println("Connection closed");
	}

	public void connect(ChatClient chatClient) {
		this.chatClient = chatClient;
	}

	public void connect(String username, String host, int timeout) throws UnknownHostException, IOException {
		this.chatClient = new ChatClient(username, host, timeout);
	}

	public void sendMessage(MessageType messageType, String msg, ChatClient chatClient) {

	}

	public void sendMessage(MessageType messageType, String msg) {
		this.chatClient.sendMessage(messageType, msg);
	}

}
