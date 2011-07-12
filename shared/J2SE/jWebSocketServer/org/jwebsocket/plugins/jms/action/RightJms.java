package org.jwebsocket.plugins.jms.action;

public enum RightJms {

	SEND("send"), LISTEN("listen"), SEND_AND_LISTEN("sendAndListen");

	private String mValue;

	private RightJms(String value) {
		this.mValue = value;
	}

	public String getValue() {
		return mValue;
	}

	public boolean equals(String aAction) {
		return mValue.equals(aAction);
	}

	public String toString() {
		return mValue;
	}

}
