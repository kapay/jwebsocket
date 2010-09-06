package org.jwebsocket.plugins.rpc.rrpc;

import java.util.List;

import javolution.util.FastList;

import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.plugins.rpc.RPCPlugIn;
import org.jwebsocket.security.SecurityFactory;
import org.jwebsocket.token.Token;

/**
 * Class used to call a Rrpc method (S2C)
 * Example: new Rrpc.Call("aClass", "aMethod").send("hello", "it's a rrpc call", 123).from(aConnector).to(anotherConnector)
 *       or new Rrpc.Call("aClass", "aMethod").send(SomethingToSend).to(anotherConnector) (in this case, the sender will be the server)
 * @author Quentin Ambard
 */
//public class Rrpc {
//	public static class Call implements RpcInterface, RpcInterfaceCaller, RpcInterfaceFromCaller{
//		private String mClassname;
//		private String mMethod;
//		private List mArg = null;
//		private WebSocketConnector mConnectorFrom = null;
//		private List<WebSocketConnector> mConnectorsTo ;
//		/**
//		 * @param aClassname the classname to call
//		 * @param aMethod the method to call
//		 */
//		public Call (String aClassname, String aMethod) {
//			mClassname = aClassname;
//			mMethod = aMethod;			
//		}
//		/**
//		 * The token should contains all the necessary informations. 
//		 * Can be usefull to create a direct call from an already-created token
//		 * @param aToken
//		 * @throws RrpcRightNotGrantedException
//		 * @throws RrpcConnectorNotFoundException
//		 */
//		public Call (Token aToken) throws RrpcRightNotGrantedException, RrpcConnectorNotFoundException {
//			mClassname = aToken.getString(RPCPlugIn.RRPC_KEY_CLASSNAME);
//			mMethod = aToken.getString(RPCPlugIn.RRPC_KEY_METHOD);
//			mArg = aToken.getList(RPCPlugIn.RRPC_KEY_ARGS);
//			if (mArg == null) {
//				Object lObject = aToken.getObject(RPCPlugIn.RRPC_KEY_ARGS);
//				if (lObject != null) {
//					mArg = new FastList();
//					mArg.add(lObject);
//				}
//			}
//			String lConnectorFromId= aToken.getString(RPCPlugIn.RRPC_KEY_SOURCE_ID);
//			if(lConnectorFromId != null) {
//				from (lConnectorFromId);
//			}
//			String lConnectorToId= aToken.getString(RPCPlugIn.RRPC_KEY_TARGET_ID);
//			to (lConnectorToId);
//		}
//		/**
//		 * Send the Objects you want to the remote procedure.
//		 * Create a list from these objects.
//		 * @param aArg objects you want to send to the client.
//		 * @return
//		 */
//		public RpcInterfaceCaller send (Object... aArg){
//			if (aArg != null) {
//				for (int i = 0; i<aArg.length; i++) {
//					mArg = new FastList();
//					mArg.add(aArg[i]);
//				}
//			}
//			return this ;
//		}
//		
//		/**
//		 * Directly send this list of object to the remote procedure
//		 * @param aArgs a List of arguments already built
//		 * @return
//		 */
//		public RpcInterfaceCaller sendListOfArgs (List aArgs){
//			mArg = aArgs;
//			return this ;
//		}
//		
//		/**
//		 * Eventually, the connector the rrpc comes from.
//		 * If this method is not called during the rrpc, the server will be the source.
//		 * @param aConnector
//		 * @throws RrpcRightNotGrantedException
//		 */
//		public RpcInterfaceFromCaller from (WebSocketConnector aConnector) throws RrpcRightNotGrantedException {
//			mConnectorFrom = aConnector ; 	
//			// check if user is allowed to run 'rrpc' command		
//			if (mConnectorFrom != null && !SecurityFactory.hasRight(RPCPlugIn.getUsernameStatic(mConnectorFrom), RPCPlugIn.NS_RPC_DEFAULT + "." + RPCPlugIn.RRPC_RIGHT_ID)) {
//				throw new RrpcRightNotGrantedException();
//			}
//			return this ;
//		}
//		
//		/**
//		 * Eventually, the connectorId the rrpc comes from.
//		 * If this method is not called during the rrpc, the server will be the source.
//		 * @param aConnector
//		 * @throws RrpcRightNotGrantedException
//		 */
//		public RpcInterfaceFromCaller from (String aConnectorId) throws RrpcRightNotGrantedException {
//			return from(RPCPlugIn.getConnector("tcp0", aConnectorId));
//		}
//		
//		/**
//		 * The connector you want to send the rrpc
//		 * @param aConnector
//		 */
//		public void to (WebSocketConnector aConnector) {
//			mConnectorsTo = new FastList<WebSocketConnector>();
//			mConnectorsTo.add(aConnector);	
//			call();
//		}
//		/**
//		 * The connectors you want to send the rrpc
//		 * @param aConnector
//		 */
//		public void to (List<WebSocketConnector> aConnectors) {
//			mConnectorsTo = aConnectors;
//			call();
//		}
//		
//		/**
//		 * The connectorId you want to send the rrpc
//		 * @param aConnector
//		 * @throws RrpcConnectorNotFoundException
//		 */
//		public void to (String aConnectorId) throws RrpcConnectorNotFoundException {
//			WebSocketConnector lConnector = RPCPlugIn.getConnector("tcp0", aConnectorId);
//			// check if user is existing
//			if (lConnector == null) {
//				throw new RrpcConnectorNotFoundException();
//			}
//			to(lConnector);
//		}
//		
//		/**
//		 * The connectorsId you want to send the rrpc
//		 * @param aConnector
//		 * @throws RrpcConnectorNotFoundException
//		 */
//		public void toId (List<String> aConnectorsId) throws RrpcConnectorNotFoundException {
//			mConnectorsTo = new FastList<WebSocketConnector>();
//			for (String lConnectorId : aConnectorsId) {
//				WebSocketConnector lConnector = RPCPlugIn.getConnector("tcp0", lConnectorId);
//				// check if user is existing
//				if (lConnector == null) {
//					throw new RrpcConnectorNotFoundException();
//				}
//				mConnectorsTo.add(lConnector);
//			}
//			call();
//		}
//		
//		/**
//		 * Make the call. Will be called afer a to() method or a Call(Token).
//		 */
//		private void call () {
//			RPCPlugIn.rrpc(mConnectorFrom, mConnectorsTo, mClassname, mMethod, mArg);
//		}
//	}
//	
//	/**
//	 * Interface used to make sure the user uses the instructions in the correct order.
//	 * only allows to perform a from or a send.
//	 * Used after a Call()
//	 */
//	public interface RpcInterface  {
//		public RpcInterfaceFromCaller from (String aConnectorId) throws RrpcRightNotGrantedException;
//		public RpcInterfaceFromCaller from (WebSocketConnector aConnector)  throws RrpcRightNotGrantedException;
//		public RpcInterfaceCaller send (Object... aArg);
//		public RpcInterfaceCaller sendListOfArgs (List aArg);
//	}
//	
//	/**
//	 * Interface used to make sure the user uses the instructions in the correct order.
//	 * only allows to perform a to() or a from.
//	 * Used after a send ()
//	 */
//	public interface RpcInterfaceCaller  {
//		public RpcInterfaceFromCaller from (String aConnectorId) throws RrpcRightNotGrantedException;
//		public RpcInterfaceFromCaller from (WebSocketConnector aConnector)  throws RrpcRightNotGrantedException;
//		public void to (String aConnectorId) throws RrpcConnectorNotFoundException;
//		public void to (WebSocketConnector aConnector);
//	}
//	
//	/**
//	 * Interface used to make sure the user uses the instructions in the correct order.
//	 * Final instruction, used after a from(): only allows to perform a to()
//	 */
//	public interface RpcInterfaceFromCaller  {
//		public void to (String aConnectorId) throws RrpcConnectorNotFoundException;
//		public void to (WebSocketConnector aConnector);
//	}
//}
