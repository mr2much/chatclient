package com.lockward.model;

import java.io.Serializable;

public class Message implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	private MessageType messageType;
	private String username;
	private String msg;

	public Message(MessageType messageType, String msg) {
		setMessageType(messageType);
		setMessage(msg);
	}

	public Message(MessageType messageType, String msg, String username) {
		this(messageType, msg);
		setUsername(username);
	}

	public String getMessage() {
		return msg;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public String getUsername() {
		return username;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public void setMessage(String msg) {
		this.msg = msg;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}
