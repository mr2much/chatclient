package com.lockward.model;

import java.io.Serializable;

public class Message implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 6569200380945433124L;
	/**
	 *
	 */
	private final MessageType messageType;
	private final MessageType subMessageType;
	private final String username;
	private final String msg;
	private final Object attachment;

	public Message(MessageBuilder builder) {
		messageType = builder.messageType;
		subMessageType = builder.subMessageType;
		username = builder.username;
		msg = builder.msg;
		attachment = builder.attachment;
	}

//	public Message(MessageType messageType, String msg) {
//		setMessageType(messageType);
//		setMessage(msg);
//	}
//
//	public Message(MessageType messageType, String msg, String username) {
//		this(messageType, msg);
//		setUsername(username);
//	}

	public MessageType getSubMessageType() {
		return subMessageType;
	}

	public Object getAttachment() {
		return attachment;
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

//	public void setMessageType(MessageType messageType) {
//		this.messageType = messageType;
//	}
//
//	public void setMessage(String msg) {
//		this.msg = msg;
//	}
//
//	public void setUsername(String username) {
//		this.username = username;
//	}
}
