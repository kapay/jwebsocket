package org.jwebsocket.plugins.jms;

import org.jwebsocket.plugins.jms.action.FieldJms;
import org.jwebsocket.token.Token;

/**
 * Class that represents an identifier for a jms destination. This class
 * identifies a destination by the name of the connection factory by which the
 * destination gets connected to, the name of the destination and a flag which
 * indicates whether the destination adheres to the publish-subscribe or the
 * point-to-point domain.
 * 
 * @author johannessmutny
 * 
 */
public class DestinationIdentifier {

	private final Boolean mPubSubDomain;
	private final String mDestinationName;
	private final String mConnectionFactoryName;

	private DestinationIdentifier(String aConnectionFactoryName,
			String aDestinationName, Boolean aPubSubDomain) {
		this.mConnectionFactoryName = aConnectionFactoryName;
		this.mDestinationName = aDestinationName;
		this.mPubSubDomain = aPubSubDomain;
	}

	public Boolean isPubSubDomain() {
		return mPubSubDomain;
	}

	public String getDestinationName() {
		return mDestinationName;
	}

	public String getConnectionFactoryName() {
		return mConnectionFactoryName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((mConnectionFactoryName == null) ? 0
						: mConnectionFactoryName.hashCode());
		result = prime
				* result
				+ ((mDestinationName == null) ? 0 : mDestinationName.hashCode());
		result = prime * result + (mPubSubDomain ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DestinationIdentifier other = (DestinationIdentifier) obj;
		if (mConnectionFactoryName == null) {
			if (other.mConnectionFactoryName != null)
				return false;
		} else if (!mConnectionFactoryName.equals(other.mConnectionFactoryName))
			return false;
		if (mDestinationName == null) {
			if (other.mDestinationName != null)
				return false;
		} else if (!mDestinationName.equals(other.mDestinationName))
			return false;
		if (mPubSubDomain != other.mPubSubDomain)
			return false;
		return true;
	}

	public static DestinationIdentifier valueOf(String aConnectionFactoryName,
			String aDestinationName, Boolean aPubSubDomain) {
		return new DestinationIdentifier(aConnectionFactoryName,
				aDestinationName, aPubSubDomain);
	}

	public static DestinationIdentifier valueOf(Token aToken) {
		String lConnectionFactoryName = aToken
				.getString(FieldJms.CONNECTION_FACTORY_NAME.getValue());
		String lDestinationName = aToken.getString(FieldJms.DESTINATION_NAME
				.getValue());
		Boolean lPubSubDomain = aToken.getBoolean(FieldJms.IS_PUB_SUB_DOMAIN
				.getValue());

		return valueOf(lConnectionFactoryName, lDestinationName, lPubSubDomain);
	}

	public boolean isMissingData() {
		return null == mConnectionFactoryName
				|| mConnectionFactoryName.trim().length() == 0
				|| null == mDestinationName
				|| mDestinationName.trim().length() == 0
				|| null == mPubSubDomain;
	}

}
