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
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
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

	private static Logger mLog = Logging.getLogger(InterfacePlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static final String NS_INTERFACE =
			JWebSocketServerConstants.NS_BASE + ".plugins.api";
	private String EXPORT_SERVER_API = "server.export.api";
	private String EXPORT_PLUGIN_API = "server.export.plugin.api";
	private String EXPORT_PLUGIN_IDENTIFIERS = "server.export.plugin.ids";
	private String SUPPORT_TOKEN = "server.support.token";
	private String HAS_PLUGIN = "server.has.plugin";
	private BeanFactory beanFactory;

	public InterfacePlugIn(PluginConfiguration configuration) throws Exception {
		super(configuration);

		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating automated test plug-in...");
		}
		// specify default name space for interface plugin
		this.setNamespace(NS_INTERFACE);

		// Creating the Spring Bean Factory
		String lPath = JWebSocketConfig.getConfigFolder((String) getSettings().get("config_file"));
		beanFactory = new XmlBeanFactory(new FileSystemResource(lPath));
	}

	/**
	 *
	 * {@inheritDoc }
	 */
	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
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

	/**
	 * Export the server API
	 * 
	 * @param aConnector
	 * @param aToken 
	 */
	public void exportServerAPI(WebSocketConnector aConnector, Token aToken) {
		Token response = createResponse(aToken);

		List<Token> plugIns = new FastList<Token>();
		Token tempPlugIn;
		for (WebSocketPlugIn p : getPlugInChain().getPlugIns()) {
			if (beanFactory.containsBean(p.getId())) {
				tempPlugIn = TokenFactory.createToken();
				PlugInDefinition pd = (PlugInDefinition) beanFactory.getBean(p.getId());
				pd.writeToToken(tempPlugIn);
				plugIns.add(tempPlugIn);
			}
		}
		response.setList("api", plugIns);

		//Sending the response
		sendToken(aConnector, aConnector, response);
	}

	/**
	 * Export the API for a plug-in giving a custom plug-in identifier
	 * 
	 * @param aConnector
	 * @param aToken 
	 */
	public void exportPlugInAPI(WebSocketConnector aConnector, Token aToken) {
		Token response = createResponse(aToken);

		String plugInId = aToken.getString("plugin_id", null);
		if (null == plugInId) {
			response.setInteger("code", -1);
			response.setString("msg", "Missing 'plugInId' parameter value!");

		} else if (!beanFactory.containsBean(plugInId)) {
			response.setInteger("code", -1);
			response.setString("msg", "Missing '" + plugInId + "' plug-in definition!");
		} else {
			PlugInDefinition p = (PlugInDefinition) beanFactory.getBean(plugInId);
			p.writeToToken(response);
		}

		//Sending the response
		sendToken(aConnector, aConnector, response);
	}

	/**
	 * Export the plug-ins identifiers
	 * 
	 * @param aConnector
	 * @param aToken 
	 */
	public void exportPlugInIdentifiers(WebSocketConnector aConnector, Token aToken) {
		List<String> identifiers = new FastList<String>();
		for (WebSocketPlugIn p : getPlugInChain().getPlugIns()) {
			if (beanFactory.containsBean(p.getId())) {
				identifiers.add(p.getId());
			}
		}

		Token response = createResponse(aToken);
		response.setList("identifiers", identifiers);

		//Sending the response
		sendToken(aConnector, aConnector, response);
	}

	/**
	 * Giving a custom token type return <tt>TRUE</tt> if it is supported, 
	 * <tt>FALSE</tt> otherwise
	 * 
	 * @param aConnector
	 * @param aToken 
	 */
	public void supportToken(WebSocketConnector aConnector, Token aToken) {
		Token response = createResponse(aToken);

		//Getting the plug-in identifier
		String token_type = aToken.getString("token_type", null);
		if (null == token_type) {
			response.setInteger("code", -1);
			response.setString("msg", "Missing 'token_type' parameter value!");
		} else {
			response.setBoolean("token_supported", Boolean.FALSE);

			for (WebSocketPlugIn p : getPlugInChain().getPlugIns()) {
				if (beanFactory.containsBean(p.getId())) {
					if (((PlugInDefinition) beanFactory.getBean(p.getId())).supportToken(token_type)) {
						response.setBoolean("token_supported", Boolean.TRUE);
						break;
					}
				}
			}
		}

		//Sending the response
		sendToken(aConnector, aConnector, response);
	}

	/**
	 * Giving a custom plug-in identifier return <tt>TRUE</tt> if it exists, 
	 * <tt>FALSE</tt> otherwise
	 * 
	 * @param aConnector
	 * @param aToken 
	 */
	public void hasPlugIn(WebSocketConnector aConnector, Token aToken) {
		Token response = createResponse(aToken);

		//Getting the plug-in identifier
		String id = aToken.getString("plugin_id", null);
		if (null == id) {
			response.setInteger("code", -1);
			response.setString("msg", "Missing 'plugin_id' parameter value!");
		} else {
			if (null != getPlugInChain().getPlugIn(id) && beanFactory.containsBean(id)) {
				response.setBoolean("has", Boolean.TRUE);
			} else {
				response.setBoolean("has", Boolean.FALSE);
			}
		}

		//Sending the response
		sendToken(aConnector, aConnector, response);
	}

	/**
	 * @return the beanFactory
	 */
	public BeanFactory getBeanFactory() {
		return beanFactory;
	}

	/**
	 * @param beanFactory the beanFactory to set
	 */
	public void setBeanFactory(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}
}
