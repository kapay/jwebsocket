package org.jwebsocket.client.plugins.rpc;

import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.plugins.rpc.AbstractRpc;
import org.jwebsocket.token.Token;


/**
 * Class used to call a Rpc method (C2S)
 * Example: new Rpc.Call("com.org.aClass", "aMethod").send("hello", "it's a rrpc call", 123)
 * @author Quentin Ambard
 */
public class Rpc extends AbstractRpc {
	private static BaseTokenClient mDefaultBaseTokenClient ;
	public static void setDefaultBaseTokenClient (BaseTokenClient aBaseTokenClient) {
		mDefaultBaseTokenClient = aBaseTokenClient ;
	}
	private BaseTokenClient mBaseTokenClient;
	
	public Rpc (String aClassname, String aMethod) {
		super(aClassname, aMethod);
	}

	public Rpc (String aClassname, String aMethod, boolean aSpawnTread) {
		super(aClassname, aMethod, aSpawnTread);
	}

	public Rpc (Token aToken) {
		super(aToken);
	}
	
	/**
	 * Usefull if you have 2 jwebsocket connexions in the same client.
	 * @param aBaseTokenClient the baseTokenClient that will be used to make the call.
	 */
	public AbstractRpc using (BaseTokenClient aBaseTokenClient) {
		mBaseTokenClient = aBaseTokenClient;
		return this;
	}
	

	public Token call () {
		//use the default BaseTokenClient if not specified
		if (mBaseTokenClient == null) {
			mBaseTokenClient = mDefaultBaseTokenClient ;
		}
		if (mBaseTokenClient == null) {
			return null;
		} else {
			Token lRpcToken = super.call();
			try {
				mBaseTokenClient.sendToken(lRpcToken);
			} catch (WebSocketException e) {
				return null ;
			}
			return lRpcToken;
		}
	}
}
