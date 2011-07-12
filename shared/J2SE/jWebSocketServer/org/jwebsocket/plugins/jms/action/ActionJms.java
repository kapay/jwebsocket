package org.jwebsocket.plugins.jms.action;

public enum ActionJms {

	LISTEN("listen"), UNLISTEN("unlisten"), SEND("send"), SEND_STRING_MESSAGE(
			"sendStringMessage");

	private String mValue;

	private ActionJms(String value) {
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
