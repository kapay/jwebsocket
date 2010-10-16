//	---------------------------------------------------------------------------
//	jWebSocket - WebSocket Token Server (manages JSON, CSV and XML Tokens)
//	Copyright (c) 2010 Alexander Schulze, Innotrade GmbH
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
package org.jwebsocket.server;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javolution.util.FastMap;
import org.apache.log4j.Logger;
import org.jwebsocket.api.ServerConfiguration;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.api.WebSocketPlugIn;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.api.WebSocketEngine;
import org.jwebsocket.api.WebSocketServerListener;
import org.jwebsocket.async.IOFuture;
import org.jwebsocket.config.JWebSocketCommonConstants;
import org.jwebsocket.filter.TokenFilterChain;
import org.jwebsocket.kit.BroadcastOptions;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.FilterResponse;
import org.jwebsocket.listener.WebSocketServerTokenEvent;
import org.jwebsocket.listener.WebSocketServerTokenListener;
import org.jwebsocket.packetProcessors.CSVProcessor;
import org.jwebsocket.packetProcessors.JSONProcessor;
import org.jwebsocket.plugins.TokenPlugInChain;
import org.jwebsocket.packetProcessors.XMLProcessor;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 * @author aschulze
 */
public class TokenServer extends BaseServer {

    private static Logger mLog = Logging.getLogger(TokenServer.class);
    // specify name space for token server
    private static final String NS_TOKENSERVER = JWebSocketServerConstants.NS_BASE + ".tokenserver";
    // specify shared connector variables
    public static final String VAR_IS_TOKENSERVER = NS_TOKENSERVER + ".isTS";
    private volatile boolean mIsAlive = false;
    private static ExecutorService mCachedThreadPool;
    private final static int TIME_OUT_TERMINATION_THREAD = 10;

    public TokenServer(ServerConfiguration aServerConfig) {
        super(aServerConfig);
        plugInChain = new TokenPlugInChain(this);
        filterChain = new TokenFilterChain(this);
    }

    @Override
    public void startServer() throws WebSocketException {
        // Create the thread pool.
        mCachedThreadPool = Executors.newCachedThreadPool();
        mIsAlive = true;
        if (mLog.isInfoEnabled()) {
            mLog.info("Token server '" + getId() + "' started.");
        }
    }

    @Override
    public boolean isAlive() {
        // nothing special to do here.
        // Token server does not contain any thread or similar.
        return mIsAlive;
    }

    @Override
    public void stopServer() throws WebSocketException {
        mIsAlive = false;
        // Shutdown the thread pool
        if (mCachedThreadPool != null) {
            if (mLog.isInfoEnabled()) {
                mLog.info("Shuting down token server threadPool.");
            }
            mCachedThreadPool.shutdown(); // Disable new tasks from being
                                          // submitted
            try {
                // Wait a while for existing tasks to terminate
                if (!mCachedThreadPool.awaitTermination(TIME_OUT_TERMINATION_THREAD, TimeUnit.SECONDS)) {
                    mCachedThreadPool.shutdownNow(); // Cancel currently
                                                     // executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!mCachedThreadPool.awaitTermination(TIME_OUT_TERMINATION_THREAD, TimeUnit.SECONDS)) {
                        mLog.error("Pool did not terminate");
                        mCachedThreadPool.shutdownNow();
                    }
                }
            } catch (InterruptedException ie) {
                // (Re-)Cancel if current thread also interrupted
                mCachedThreadPool.shutdownNow();
            }
        }
        if (mLog.isInfoEnabled()) {
            mLog.info("Token server '" + getId() + "' stopped.");
        }
    }

    /**
     * removes a plug-in from the plug-in chain of the server.
     * 
     * @param aPlugIn
     */
    public void removePlugIn(WebSocketPlugIn aPlugIn) {
        plugInChain.removePlugIn(aPlugIn);
    }

    @Override
    public void engineStarted(WebSocketEngine aEngine) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Processing engine '" + aEngine.getId() + "' started...");
        }
        plugInChain.engineStarted(aEngine);
    }

    @Override
    public void engineStopped(WebSocketEngine aEngine) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Processing engine '" + aEngine.getId() + "' stopped...");
        }
        plugInChain.engineStopped(aEngine);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void connectorStarted(WebSocketConnector aConnector) {
        String lSubProt = aConnector.getHeader().getSubProtocol(null);
        if ((lSubProt != null)
                && (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_JSON)
                        || lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_CSV) || lSubProt
                        .equals(JWebSocketCommonConstants.SUB_PROT_XML))) {

            aConnector.setBoolean(VAR_IS_TOKENSERVER, true);

            if (mLog.isDebugEnabled()) {
                mLog.debug("Processing connector '" + aConnector.getId() + "' started...");
            }
            // notify plugins that a connector has started,
            // i.e. a client was sconnected.
            plugInChain.connectorStarted(aConnector);
        }
        super.connectorStarted(aConnector);
    }

    @Override
    public void connectorStopped(WebSocketConnector aConnector, CloseReason aCloseReason) {
        // notify plugins that a connector has stopped,
        // i.e. a client was disconnected.
        if (aConnector.getBool(VAR_IS_TOKENSERVER)) {
            if (mLog.isDebugEnabled()) {
                mLog.debug("Processing connector '" + aConnector.getId() + "' stopped...");
            }
            plugInChain.connectorStopped(aConnector, aCloseReason);
        }
        super.connectorStopped(aConnector, aCloseReason);
    }

    public Token packetToToken(WebSocketConnector aConnector, WebSocketPacket aDataPacket) {
        String lSubProt = aConnector.getHeader().getSubProtocol(JWebSocketCommonConstants.SUB_PROT_DEFAULT);
        Token lToken = null;
        if (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_JSON)) {
            lToken = JSONProcessor.packetToToken(aDataPacket);
        } else if (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_CSV)) {
            lToken = CSVProcessor.packetToToken(aDataPacket);
        } else if (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_XML)) {
            lToken = XMLProcessor.packetToToken(aDataPacket);
        }
        return lToken;
    }

    public WebSocketPacket tokenToPacket(WebSocketConnector aConnector, Token aToken) {
        String lSubProt = aConnector.getHeader().getSubProtocol(JWebSocketCommonConstants.SUB_PROT_DEFAULT);
        WebSocketPacket lPacket = null;
        if (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_JSON)) {
            lPacket = JSONProcessor.tokenToPacket(aToken);
        } else if (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_CSV)) {
            lPacket = CSVProcessor.tokenToPacket(aToken);
        } else if (lSubProt.equals(JWebSocketCommonConstants.SUB_PROT_XML)) {
            lPacket = XMLProcessor.tokenToPacket(aToken);
        }
        return lPacket;
    }

    private void processToken(WebSocketConnector aConnector, Token aToken) {
        // before forwarding the token to the plug-ins push it through filter
        // chain
        FilterResponse filterResponse = getFilterChain().processTokenIn(aConnector, aToken);

        // only forward the token to the plug-in chain
        // if filter chain does not response "aborted"
        if (!filterResponse.isRejected()) {
            getPlugInChain().processToken(aConnector, aToken);
            // forward the token to the listener chain
            List<WebSocketServerListener> lListeners = getListeners();
            WebSocketServerTokenEvent lEvent = new WebSocketServerTokenEvent(aConnector, this);
            for (WebSocketServerListener lListener : lListeners) {
                if (lListener != null && lListener instanceof WebSocketServerTokenListener) {
                    ((WebSocketServerTokenListener) lListener).processToken(lEvent, aToken);
                }
            }
        }
    }

    @Override
    public void processPacket(WebSocketEngine aEngine, final WebSocketConnector aConnector, WebSocketPacket aDataPacket) {
        // is the data packet supposed to be interpreted as token?
        if (aConnector.getBool(VAR_IS_TOKENSERVER)) {
            final Token lToken = packetToToken(aConnector, aDataPacket);
            if (lToken != null) {
                boolean lRunReqInOwnThread = "true".equals(lToken.getString("spawnThread"));
                // TODO: create list of running threads and close all properly
                // on shutdown
                if (lRunReqInOwnThread) {
                    if (mLog.isDebugEnabled()) {
                        mLog.debug("Processing threaded token '" + lToken.toString() + "' from '" + aConnector + "'...");
                    }
                    mCachedThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            processToken(aConnector, lToken);
                        }
                    });
                } else {
                    if (mLog.isDebugEnabled()) {
                        mLog.debug("Processing token '" + lToken.toString() + "' from '" + aConnector + "'...");
                    }
                    processToken(aConnector, lToken);
                }
            } else {
                mLog.error("Packet '" + aDataPacket.toString() + "' could not be converted into token.");
            }
        }
        super.processPacket(aEngine, aConnector, aDataPacket);
    }

    public void sendToken(WebSocketConnector aSource, WebSocketConnector aTarget, Token aToken) {
        sendTokenData(aSource, aTarget, aToken, false);
    }

    public void sendToken(WebSocketConnector aTarget, Token aToken) {
        sendToken(null, aTarget, aToken);
    }

    public IOFuture sendTokenAsync(WebSocketConnector aTarget, Token aToken) {
        return sendTokenData(null, aTarget, aToken, true);
    }

    public IOFuture sendTokenAsync(WebSocketConnector aSource, WebSocketConnector aTarget, Token aToken) {
        return sendTokenData(aSource, aTarget, aToken, true);
    }

    public void sendToken(String aEngineId, String aConnectorId, Token aToken) {
        // TODO: return meaningful result here.
        WebSocketConnector lTargetConnector = getConnector(aEngineId, aConnectorId);
        if (lTargetConnector != null) {
            if (lTargetConnector.getBool(VAR_IS_TOKENSERVER)) {
                // before sending the token push it through filter chain
                FilterResponse filterResponse = getFilterChain().processTokenOut(null, lTargetConnector, aToken);

                if (mLog.isDebugEnabled()) {
                    mLog.debug("Sending token '" + aToken + "' to '" + lTargetConnector + "'...");
                }
                sendPacketData(lTargetConnector, tokenToPacket(lTargetConnector, aToken), false);
            } else {
                mLog.warn("Connector not supposed to handle tokens.");
            }
        } else {
            mLog.warn("Target connector '" + aConnectorId + "' not found.");
        }
    }

    private IOFuture sendTokenData(WebSocketConnector aSource, WebSocketConnector aTarget, Token aToken, boolean async) {
        if (aTarget.getBool(VAR_IS_TOKENSERVER)) {
            // before sending the token push it through filter chain
            FilterResponse filterResponse = getFilterChain().processTokenOut(aSource, aTarget, aToken);

            // only forward the token to the plug-in chain
            // if filter chain does not response "aborted"
            if (!filterResponse.isRejected()) {
                if (mLog.isDebugEnabled()) {
                    mLog.debug("Sending token '" + aToken + "' to '" + aTarget + "'...");
                }
                WebSocketPacket aPacket = tokenToPacket(aTarget, aToken);
                return sendPacketData(aTarget, aPacket, false);
            } else {
                if (mLog.isDebugEnabled()) {
                    mLog.debug("");
                }
            }
        } else {
            mLog.warn("Connector not supposed to handle tokens.");
        }
        return null;
    }

    private IOFuture sendPacketData(WebSocketConnector target, WebSocketPacket dataPacket, boolean isAsync) {
        if (isAsync) {
            return super.sendPacketAsync(target, dataPacket);
        } else {
            super.sendPacket(target, dataPacket);
            return null;
        }
    }

    /**
     * Broadcasts the passed token to all token based connectors of the
     * underlying engines that belong to the specified group.
     * 
     * @param aToken
     *            - token to broadcast
     */
    public void broadcastGroup(Token aToken) {
        String lGroup = aToken.getString("group");
        // if the group is not specified in the token then noone gets the
        // message:
        if (lGroup == null || lGroup.length() <= 0) {
            mLog.debug("Token '" + aToken + "' has no group specified...");
            return;
        }
        broadcastFiltered(aToken, "group", lGroup);
    }

    /**
     * Broadcasts the passed token to all token based connectors of the
     * underlying engines that belong to the specified filter and its name.
     * 
     * @param aToken
     * @param aFilterID
     * @param aFilterName
     */
    public void broadcastFiltered(Token aToken, String aFilterID, String aFilterName) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Broadcasting token '" + aToken + "' to all token based "
                    + "connectors that belong to the filter '" + aFilterID + "' called '" + aFilterName + "'...");
        }
        FastMap<String, Object> lFilter = new FastMap<String, Object>();
        lFilter.put(aFilterID, aFilterName);
        broadcastFiltered(aToken, lFilter);
    }

    /**
     * Broadcasts the passed token to all token based connectors of the
     * underlying engines that belong to the specified filters.
     * 
     * @param aToken
     * @param aFilter
     */
    public void broadcastFiltered(Token aToken, FastMap<String, Object> aFilter) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Broadcasting token '" + aToken + "' to all token based "
                    + "connectors that belong to the filters...");
        }

        // before sending the token push it through filter chain
        FilterResponse filterResponse = getFilterChain().processTokenOut(null, null, aToken);

        aFilter.put(VAR_IS_TOKENSERVER, true);

        // converting the token within the loop is removed in this method!
        WebSocketPacket lPacket;
        // lPackets maps protocols to appropriate converted packets:
        FastMap<String, WebSocketPacket> lPackets = new FastMap<String, WebSocketPacket>();
        String lSubProt;
        for (WebSocketConnector lConnector : selectConnectors(aFilter).values()) {
            lSubProt = lConnector.getHeader().getSubProtocol(JWebSocketCommonConstants.SUB_PROT_DEFAULT);
            lPacket = lPackets.get(lSubProt);
            // if there is no packet for this protocol already, make one and
            // store it in the map
            if (lPacket == null) {
                lPacket = tokenToPacket(lConnector, aToken);
                lPackets.put(lSubProt, lPacket);
            }
            sendPacket(lConnector, lPacket);
        }
    }

    /**
     * Broadcasts the passed token to all token based connectors of the
     * underlying engines.
     * 
     * @param aToken
     */
    public void broadcastToken(Token aToken) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Broadcasting token '" + aToken + " to all token based connectors...");
        }

        // before sending the token push it through filter chain
        FilterResponse filterResponse = getFilterChain().processTokenOut(null, null, aToken);

        FastMap<String, Object> lFilter = new FastMap<String, Object>();
        lFilter.put(VAR_IS_TOKENSERVER, true);

        // converting the token within the loop is removed in this method!
        WebSocketPacket lPacket;
        // lPackets maps protocols to appropriate converted packets:
        FastMap<String, WebSocketPacket> lPackets = new FastMap<String, WebSocketPacket>();
        String lSubProt;
        for (WebSocketConnector lConnector : selectConnectors(lFilter).values()) {
            lSubProt = lConnector.getHeader().getSubProtocol(JWebSocketCommonConstants.SUB_PROT_DEFAULT);
            lPacket = lPackets.get(lSubProt);
            // if there is no packet for this protocol already, make one and
            // store it in the map
            if (lPacket == null) {
                lPacket = tokenToPacket(lConnector, aToken);
                lPackets.put(lSubProt, lPacket);
            }
            sendPacket(lConnector, lPacket);
        }
    }

    /**
     * Broadcasts to all connector, except the sender (aSource).
     * 
     * @param aSource
     * @param aToken
     */
    public void broadcastToken(WebSocketConnector aSource, Token aToken) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Broadcasting token '" + aToken + " to all token based connectors...");
        }

        // before sending the token push it through filter chain
        FilterResponse filterResponse = getFilterChain().processTokenOut(aSource, null, aToken);

        FastMap<String, Object> lFilter = new FastMap<String, Object>();
        lFilter.put(VAR_IS_TOKENSERVER, true);

        // converting the token within the loop is removed in this method!
        WebSocketPacket lPacket;
        // lPackets maps protocols to appropriate converted packets:
        FastMap<String, WebSocketPacket> lPackets = new FastMap<String, WebSocketPacket>();
        String lSubProt;
        for (WebSocketConnector lConnector : selectConnectors(lFilter).values()) {
            if (!aSource.equals(lConnector)) {
                lSubProt = lConnector.getHeader().getSubProtocol(JWebSocketCommonConstants.SUB_PROT_DEFAULT);
                lPacket = lPackets.get(lSubProt);
                // if there is no packet for this protocol already, make one and
                // store it in the map
                if (lPacket == null) {
                    lPacket = tokenToPacket(lConnector, aToken);
                    lPackets.put(lSubProt, lPacket);
                }
                sendPacket(lConnector, lPacket);
            }
        }
    }

    /**
     * iterates through all connectors of all engines and sends the token to
     * each connector. The token format is considered for each connection
     * individually so that the application can broadcast a token to all kinds
     * of clients.
     * 
     * @param aSource
     * @param aToken
     * @param aBroadcastOptions
     */
    public void broadcastToken(WebSocketConnector aSource, Token aToken, BroadcastOptions aBroadcastOptions) {
        if (mLog.isDebugEnabled()) {
            mLog.debug("Broadcasting token '" + aToken + " to all token based connectors...");
        }

        // before sending the token push it through filter chain
        FilterResponse filterResponse = getFilterChain().processTokenOut(aSource, null, aToken);

        FastMap<String, Object> lFilter = new FastMap<String, Object>();
        lFilter.put(VAR_IS_TOKENSERVER, true);

        // converting the token within the loop is removed in this method!
        WebSocketPacket lPacket;
        // lPackets maps protocols to appropriate converted packets:
        FastMap<String, WebSocketPacket> lPackets = new FastMap<String, WebSocketPacket>();
        String lSubProt;
        for (WebSocketConnector lConnector : selectConnectors(lFilter).values()) {
            if (!aSource.equals(lConnector) || aBroadcastOptions.isSenderIncluded()) {
                lSubProt = lConnector.getHeader().getSubProtocol(JWebSocketCommonConstants.SUB_PROT_DEFAULT);
                lPacket = lPackets.get(lSubProt);
                // if there is no packet for this protocol already, make one and
                // store it in the map
                if (lPacket == null) {
                    lPacket = tokenToPacket(lConnector, aToken);
                    lPackets.put(lSubProt, lPacket);
                }
                sendPacket(lConnector, lPacket);
            }
        }
    }

    /**
     * creates a standard response
     * 
     * @param aInToken
     * @return
     */
    public Token createResponse(Token aInToken) {
        Integer lTokenId = aInToken.getInteger("utid", -1);
        String lType = aInToken.getString("type");
        String lNS = aInToken.getString("ns");
        Token lResToken = TokenFactory.createToken("response");
        lResToken.setInteger("code", 0);
        lResToken.setString("msg", "ok");
        if (lTokenId != null) {
            lResToken.setInteger("utid", lTokenId);
        }
        if (lNS != null) {
            lResToken.setString("ns", lNS);
        }
        if (lType != null) {
            lResToken.setString("reqType", lType);
        }
        return lResToken;
    }

    /**
     * creates a response with the standard "not authenticated" message
     * 
     * @param aInToken
     * @return
     */
    public Token createNotAuthToken(Token aInToken) {
        Token lResToken = createResponse(aInToken);
        lResToken.setInteger("code", -1);
        lResToken.setString("msg", "not authenticated");
        return lResToken;
    }

    /**
     * creates a response with the standard "not granted" message
     * 
     * @param aInToken
     * @return
     */
    public Token createAccessDenied(Token aInToken) {
        Token lResToken = createResponse(aInToken);
        lResToken.setInteger("code", -1);
        lResToken.setString("msg", "access denied");
        return lResToken;
    }

    /**
     * @return the plugInChain
     */
    @Override
    public TokenPlugInChain getPlugInChain() {
        return (TokenPlugInChain) plugInChain;
    }

    /**
     * @return the filterChain
     */
    @Override
    public TokenFilterChain getFilterChain() {
        return (TokenFilterChain) filterChain;
    }
}
