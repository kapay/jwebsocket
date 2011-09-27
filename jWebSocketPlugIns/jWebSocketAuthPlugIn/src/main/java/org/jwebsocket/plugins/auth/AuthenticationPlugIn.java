//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2011 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU Lesser General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
//	more details.
//	You should have received a copy of the GNU Lesser General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
//	---------------------------------------------------------------------------
package org.jwebsocket.plugins.auth;

import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.jwebsocket.api.IUserUniqueIdentifierContainer;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketConfig;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.spring.ServerXmlBeanFactory;
import org.jwebsocket.token.Token;
import org.jwebsocket.util.Tools;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;


/**
 * Spring AuthenticationManager compatible plug-in to handle authentication
 * 
 * @author kyberneees
 */
public class AuthenticationPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(AuthenticationPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static final String NS_AUTH = JWebSocketServerConstants.NS_BASE + ".plugins.auth";
	private AuthenticationProvider mAuthProv;
	private ProviderManager mAuthProvMgr;
	private static Log logger = LogFactory.getLog(AuthenticationPlugIn.class);
	public static final String USERNAME = "$username";
	public static final String AUTHORITIES = "$authorities";
	public static final String UUID = "$uuid";
	public static final String IS_AUTHENTICATED = "$is_authenticated";
	private static ServerXmlBeanFactory mBeanFactory;

	/**
	 * 
	 * @param aConfiguration
	 */
	public AuthenticationPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating Authentication plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_AUTH);

		try {
			String lSpringConfig = getString("spring_config");
			lSpringConfig = Tools.expandEnvVars(lSpringConfig);
			String lPath = FilenameUtils.getPath(lSpringConfig);
			if (lPath == null || lPath.length() <= 0) {
				lPath = JWebSocketConfig.getConfigFolder(lSpringConfig);
			} else {
				lPath = lSpringConfig;
			}
			FileSystemResource lFSRes = new FileSystemResource(lPath);

			mBeanFactory = new ServerXmlBeanFactory(lFSRes, getClass().getClassLoader());

			Object lObj = mBeanFactory.getBean("authenticationManager");
			mAuthProvMgr = (ProviderManager) lObj;
			List<AuthenticationProvider> lProviders = mAuthProvMgr.getProviders();
			mAuthProv = lProviders.get(0);
			
			// give a success message to the administrator
			if (mLog.isInfoEnabled()) {
				mLog.info("Authentication plug-in successfully loaded.");
			}
		} catch (Exception lEx) {
			mLog.error(lEx.getClass().getSimpleName() + " at Authentication plug-in instantiation: " + lEx.getMessage());
		}
	}

	public AuthenticationProvider getAuthProvider() {
		return mAuthProv;
	}

	public void setAuthManager(AuthenticationProvider aAuthMgr) {
		mAuthProv = aAuthMgr;
	}

	@Override
	public void processToken(PlugInResponse aResponse, WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if (aToken.getType().equals("logon")) {
				logon(aConnector, aToken);
			} else if (aToken.getType().equals("logoff")) {
				logoff(aConnector, aToken);
			}
		}
	}

	void logon(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		if (SecurityHelper.isUserAuthenticated(aConnector)) {
			lServer.sendToken(aConnector,
					lServer.createErrorToken(
					aToken, -1, "Is authenticated already, logoff first!"));
			return;
		}

		String lUsername = aToken.getString("username");
		String lPassword = aToken.getString("password");

		if (logger.isDebugEnabled()) {
			logger.debug("Starting authentication ...");
		}
		Authentication lAuthRequest = new UsernamePasswordAuthenticationToken(lUsername, lPassword);
		Authentication lAuthResult = null;
		try {
			AuthenticationProvider lAuthProvider = getAuthProvider();
			lAuthResult = lAuthProvider.authenticate(lAuthRequest);
		} catch (Exception ex) {
			String lMsg = ex.getClass().getSimpleName() + ": " + ex.getMessage();
			Token lResponse = getServer().createErrorToken(aToken, -1, lMsg);
			sendToken(aConnector, aConnector, lResponse);

			if (logger.isDebugEnabled()) {
				logger.debug(lMsg);
			}
			return; //Stop the executon flow
		}

		if (true) {
			// Creating the response
			Token lResponse = createResponse(aToken);
			Object lObj;
			lObj = lAuthResult.getPrincipal();
			lResponse.setString("principal", (lObj == null ? "null" : lObj.toString()));
			lObj = lAuthResult.getDetails();
			lResponse.setString("details", (lObj == null ? "null" : lObj.toString()));
			lObj = lAuthResult.getName();
			lResponse.setString("name", (lObj == null ? "null" : lObj.toString()));
			lObj = lAuthResult.getCredentials();
			lResponse.setString("credentials", (lObj == null ? "null" : lObj.toString()));
			lObj = lAuthResult.getAuthorities();
			lResponse.setString("authorities", (lObj == null ? "null" : lObj.toString()));
			// Sending the response
			sendToken(aConnector, aConnector, lResponse);
			return;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Updating the user session...");
		}

		//Getting the session
		Map<String, Object> lSession = aConnector.getSession().getStorage();

		//Setting the is_authenticated flag
		lSession.put(IS_AUTHENTICATED, lAuthResult.isAuthenticated());

		//Setting the username
		lSession.put(USERNAME, lUsername);
		aConnector.setUsername(lUsername);

		//Setting the uuid
		String uuid;
		Object details = lAuthResult.getDetails();
		if (null != details && details instanceof IUserUniqueIdentifierContainer) {
			uuid = ((IUserUniqueIdentifierContainer) details).getUUID();
		} else {
			uuid = lUsername;
		}
		lSession.put(UUID, uuid);

		//Setting the authorities
		String authorities = "";
		for (GrantedAuthority ga : lAuthResult.getAuthorities()) {
			authorities = authorities.concat(ga.getAuthority() + " ");
		}
		//Storing the user authorities as a string to avoid serialization problems
		lSession.put(AUTHORITIES, authorities);

		//Creating the response
		Token response = createResponse(aToken);
		response.setString("uuid", uuid);
		response.setString("username", lUsername);
		response.setList("authorities", Tools.parseStringArrayToList(authorities.split(" ")));

		//Sending the response
		getServer().sendToken(aConnector, response);
		if (logger.isDebugEnabled()) {
			logger.debug("Logon process finished successfully!");
		}

		try {
			//Notifying the UserLogon event to available listeners
			// TODO: broadcast message until events are fully merged
			// notify(new UserLogon(result), null, false);
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

	void logoff(WebSocketConnector aConnector, Token aToken) {
		if (!SecurityHelper.isUserAuthenticated(aConnector)) {
			getServer().sendToken(aConnector, getServer().createNotAuthToken(aToken));
			return;
		}

		//Getting the username
		String lUsername = aConnector.getUsername();

		//Cleaning the session
		aConnector.getSession().getStorage().clear();
		aConnector.removeUsername();

		//Sending the response
		getServer().sendToken(aConnector, createResponse(aToken));
		if (logger.isDebugEnabled()) {
			logger.debug("Logoff process finished successfully!");
		}

		try {
			// Notifying the UserLogoff event to available listeners
			// TODO: broadcast message until events are fully merged
			// notify(new UserLogoff(username), null, false);
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}
}
