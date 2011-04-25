//  ---------------------------------------------------------------------------
//  jWebSocket - EventsPlugIn
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//  ---------------------------------------------------------------------------
//  This program is free software; you can redistribute it and/or modify it
//  under the terms of the GNU Lesser General Public License as published by the
//  Free Software Foundation; either version 3 of the License, or (at your
//  option) any later version.
//  This program is distributed in the hope that it will be useful, but WITHOUT
//  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//  more details.
//  You should have received a copy of the GNU Lesser General Public License along
//  with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//  ---------------------------------------------------------------------------
package org.jwebsocket.plugins.api;

import java.util.List;
import javolution.util.FastList;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.FileSystemResource;

/**
 * Plug-in to export the server API
 *
 * @author kyberneees
 */
public class InterfacePlugIn extends TokenPlugIn {

	private String EXPORT_SERVER_API = "server.export.api";
	private String EXPORT_PLUGIN_API = "server.export.plugin.api";
	private String EXPORT_PLUGIN_IDENTIFIERS = "server.export.plugin.ids";
	private String SUPPORT_TOKEN = "server.support.token";
	private String HAS_PLUGIN = "server.has.plugin";
	private BeanFactory mBeanFactory;
	private static final String NS_INTERFACE =
			JWebSocketServerConstants.NS_BASE + ".plugins.api";

	public InterfacePlugIn(PluginConfiguration configuration) throws Exception {
		super(configuration);

		//Creating the Spring Bean Factory
		String lPath = JWebSocketConfig.getConfigFolder(getString("config_file"));
		mBeanFactory = new XmlBeanFactory(new FileSystemResource(lPath));

		//Specify default name space for interface plugin
		this.setNamespace(NS_INTERFACE);
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		if (getNamespace().equals(aToken.getNS())) {
			if (EXPORT_SERVER_API.equals(aToken.getType())) {
				exportServerAPI(aConnector, aToken);
			} else if (EXPORT_PLUGIN_API.equals(aToken.getType())) {
				exportPlugInAPI(aConnector, aToken);
			} else if (EXPORT_PLUGIN_IDENTIFIERS.equals(aToken.getType())) {
				exportPlugInIdentifiers(aConnector, aToken);
			} else if (SUPPORT_TOKEN.equals(aToken.getType())) {
				supportToken(aConnector, aToken);
			} else if (HAS_PLUGIN.equals(aToken.getType())) {
				hasPlugIn(aConnector, aToken);
			}
		}
	}

	/**
	 * Export the server API
	 * 
	 * @param aConnector
	 * @param aToken 
	 */
	public void exportServerAPI(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		List<Token> lPlugIns = new FastList<Token>();
		Token lTempPlugIn;
		for (WebSocketPlugIn p : getPlugInChain().getPlugIns()) {
			if (mBeanFactory.containsBean(p.getId())) {
				lTempPlugIn = TokenFactory.createToken();
				PlugInDefinition pd = (PlugInDefinition) mBeanFactory.getBean(p.getId());
				pd.writeToToken(lTempPlugIn);
				lPlugIns.add(lTempPlugIn);
			}
		}
		lResponse.setList("api", lPlugIns);

		//Sending the response
		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * Export the API for a plug-in giving a custom plug-in identifier
	 * 
	 * @param aConnector
	 * @param aToken 
	 */
	public void exportPlugInAPI(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		String lPlugInId = aToken.getString("plugin_id", null);
		if (null == lPlugInId) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "Missing 'plugInId' parameter value!");

		} else if (!mBeanFactory.containsBean(lPlugInId)) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "Missing '" + lPlugInId + "' plug-in definition!");
		} else {
			PlugInDefinition p = (PlugInDefinition) mBeanFactory.getBean(lPlugInId);
			p.writeToToken(lResponse);
		}

		//Sending the response
		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * Export the plug-ins identifiers
	 * 
	 * @param aConnector
	 * @param aToken 
	 */
	public void exportPlugInIdentifiers(WebSocketConnector aConnector, Token aToken) {
		List<String> lIdentifiers = new FastList<String>();
		for (WebSocketPlugIn lPlugIn : getPlugInChain().getPlugIns()) {
			if (mBeanFactory.containsBean(lPlugIn.getId())) {
				lIdentifiers.add(lPlugIn.getId());
			}
		}

		Token lResponse = createResponse(aToken);
		lResponse.setList("identifiers", lIdentifiers);

		//Sending the response
		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * Giving a custom token type return <tt>TRUE</tt> if it is supported, 
	 * <tt>FALSE</tt> otherwise
	 * 
	 * @param aConnector
	 * @param aToken 
	 */
	public void supportToken(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);

		//Getting the plug-in identifier
		String lType = aToken.getString("token_type", null);
		if (null == lType) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "Missing 'token_type' parameter value!");
		} else {
			lResponse.setBoolean("token_supported", Boolean.FALSE);

			for (WebSocketPlugIn lPlugIn : getPlugInChain().getPlugIns()) {
				if (mBeanFactory.containsBean(lPlugIn.getId())) {
					if (((PlugInDefinition) mBeanFactory.getBean(lPlugIn.getId())).supportToken(lType)) {
						lResponse.setBoolean("token_supported", Boolean.TRUE);
						break;
					}
				}
			}
		}

		//Sending the response
		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * Giving a custom plug-in identifier return <tt>TRUE</tt> if it exists, 
	 * <tt>FALSE</tt> otherwise
	 * 
	 * @param aConnector
	 * @param aToken 
	 */
	public void hasPlugIn(WebSocketConnector aConnector, Token aToken) {
		Token lResponse = createResponse(aToken);
		
		//Getting the plug-in identifier
		String lId = aToken.getString("plugin_id");
		if (null == lId) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", "Missing 'plugin_id' parameter value!");
		} else {
			if (null != getPlugInChain().getPlugIn(lId) && mBeanFactory.containsBean(lId)) {
				lResponse.setBoolean("has", Boolean.TRUE);
			} else {
				lResponse.setBoolean("has", Boolean.FALSE);
			}
		}
		
		//Sending the response
		sendToken(aConnector, aConnector, lResponse);
	}

	/**
	 * @return the beanFactory
	 */
	public BeanFactory getBeanFactory() {
		return mBeanFactory;
	}

	/**
	 * @param aBeanFactory the beanFactory to set
	 */
	public void setBeanFactory(BeanFactory aBeanFactory) {
		this.mBeanFactory = aBeanFactory;
	}
}
