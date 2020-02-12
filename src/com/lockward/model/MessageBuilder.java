package com.lockward.model;

public class MessageBuilder {

	MessageType messageType;
	MessageType subMessageType;
	String username;
	String msg;
	Object attachment;

	public MessageBuilder messageType(MessageType messageType) {
		this.messageType = messageType;
		return this;
	}

	public MessageBuilder username(String user) {
		this.username = user;
		return this;
	}

	public MessageBuilder subMessageType(MessageType messageType) {
		subMessageType = messageType;
		return this;
	}

	public MessageBuilder msg(String msg) {
		this.msg = msg;
		return this;
	}

	public MessageBuilder attachment(Object attachment) {
		this.attachment = attachment;
		return this;
	}

	public Message build() {
		return new Message(this);
	}

}
