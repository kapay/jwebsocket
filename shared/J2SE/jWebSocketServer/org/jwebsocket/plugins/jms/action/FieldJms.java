package org.jwebsocket.plugins.jms.action;

public enum FieldJms {
	CONNECTION_FACTORY_NAME("connectionFactoryName"), DESTINATION_NAME(
			"destinationName"), IS_PUB_SUB_DOMAIN("isPubSubDomain"), STRING_MESSAGE(
			"stringMessage");

	private String mValue;

	private FieldJms(String value) {
		this.mValue = value;
	}

	public String getValue() {
		return mValue;
	}

	public boolean equals(String aField) {
		return mValue.equals(aField);
	}
	
	public String toString() {
		return mValue;
	}

}
