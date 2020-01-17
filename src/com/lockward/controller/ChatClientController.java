package com.lockward.controller;

import java.io.IOException;
import java.net.UnknownHostException;

import com.lockward.model.ChatClient;
import com.lockward.model.MessageType;

public class ChatClientController {

	// private ChatClient chatClient;

	public void close(ChatClient chatClient) throws IOException {
		if (chatClient != null && chatClient.isClosed()) {
			System.out.println("Connection closed");
			chatClient.close();
		}
	}

	public ChatClient connect(String username, String host, int timeout) throws UnknownHostException, IOException {
		ChatClient chatClient = new ChatClient(username, host, timeout);
		chatClient.setSoTimeout(5000);

		return chatClient;
	}

	public void sendMessage(MessageType messageType, String msg, ChatClient chatClient) throws IOException {
		chatClient.sendMessage(messageType, msg);
	}
}
