//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
//	---------------------------------------------------------------------------
//	This program is free software; you can redistribute it and/or modify it
//	under the terms of the GNU General Public License as published by the
//	Free Software Foundation; either version 3 of the License, or (at your
//	option) any later version.
//	This program is distributed in the hope that it will be useful, but WITHOUT
//	ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
//	FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
//	more details.
//	You should have received a copy of the GNU General Public License along
//	with this program; if not, see <http://www.gnu.org/licenses/>.
//	---------------------------------------------------------------------------
package org.jwebsocket.netty.engines;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ChildChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.WriteCompletionEvent;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameDecoder;
import org.jboss.netty.handler.codec.http.websocket.WebSocketFrameEncoder;
import org.jboss.netty.util.CharsetUtil;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.Config;
import org.jwebsocket.kit.CloseReason;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.RequestHeader;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.netty.connectors.NettyConnector;

/**
 * Handler class for the <tt>NettyEngine</tt> that recieves the events based on
 * event types and notifies the client connectors. This handler also handles the
 * initial handshaking for WebSocket connection with a appropriate hand shake
 * response. This handler is created for each new connection channel.
 * <p>
 * 		Once the handshaking is successful after sending the handshake {@code HttpResponse} 
 * 		it replaces the {@code HttpRequestDecoder} and {@code HttpResponseEncoder} 
 * 		from the channel pipeline with {@code WebSocketFrameDecoder} as WebSocket frame
 * 		data decoder and {@code WebSocketFrameEncoder} as WebSocket frame data encoder.
 * 		Also it starts the <tt>NettyConnector</tt>.
 * </p>
 * 
 * @author puran
 * @version $Id$
 */
public class NettyEngineHandler extends SimpleChannelUpstreamHandler {

	private static Logger log = Logging.getLogger(NettyEngineHandler.class);

	private NettyEngine engine = null;

	private WebSocketConnector connector = null;

	private ChannelHandlerContext context = null;

	private static final String CONTENT_LENGTH = "Content-Length";
	
	private static final String ARGS = "args";
	private static final String ORIGIN = "origin";
	private static final String LOCATION = "location";
	private static final String PATH = "path";
	private static final String SEARCH_STRING = "searchString";
	private static final String HOST = "host";

	public NettyEngineHandler(NettyEngine aEngine) {
		this.engine = aEngine;
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void channelBound(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		this.context = ctx;
		super.channelBound(ctx, e);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		this.context = ctx;
		super.channelClosed(ctx, e);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		this.context = ctx;
		super.channelConnected(ctx, e);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void channelDisconnected(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		if (log.isDebugEnabled()) {
			log.debug("Channel is disconnected");
		}
		this.context = ctx;
		super.channelDisconnected(ctx, e);
		engine.connectorStopped(connector, CloseReason.CLIENT);
	}

	/**
	 * {@inheritDoc }
	 */
	@Override
	public void channelInterestChanged(ChannelHandlerContext ctx,
			ChannelStateEvent e) throws Exception {
		this.context = ctx;
		super.channelInterestChanged(ctx, e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		this.context = ctx;
		super.channelOpen(ctx, e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void channelUnbound(ChannelHandlerContext ctx, ChannelStateEvent e)
			throws Exception {
		this.context = ctx;
		super.channelUnbound(ctx, e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void childChannelClosed(ChannelHandlerContext ctx,
			ChildChannelStateEvent e) throws Exception {
		this.context = ctx;
		super.childChannelClosed(ctx, e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void childChannelOpen(ChannelHandlerContext ctx,
			ChildChannelStateEvent e) throws Exception {
		this.context = ctx;
		super.childChannelOpen(ctx, e);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		this.context = ctx;
		if (log.isDebugEnabled()) {
			log.debug("Channel is disconnected:"+e.getCause().getLocalizedMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent arg1)
			throws Exception {
		this.context = ctx;
		super.handleUpstream(ctx, arg1);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		this.context = ctx;
		if (log.isDebugEnabled()) {
			log.debug("message received in the engine handler");
		}
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			handleHttpRequest(ctx, (HttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	/**
	 * private method that sends the handshake response for WebSocket connection
	 * 
	 * @param ctx
	 *            the channel context
	 * @param req
	 *            http request object
	 * @param res
	 *            http response object
	 */
	private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req,
			HttpResponse res) {
		// Generate an error page if response status code is not OK (200).
		if (res.getStatus().getCode() != 200) {
			res.setContent(ChannelBuffers.copiedBuffer(res.getStatus()
					.toString(), CharsetUtil.UTF_8));
			setContentLength(res, res.getContent().readableBytes());
		}

		// Send the response and close the connection if necessary.
		ChannelFuture f = ctx.getChannel().write(res);
		if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
			f.addListener(ChannelFutureListener.CLOSE);
		}
	}

	/**
	 * Check if the request header has Keep-Alive
	 * 
	 * @param req
	 *            the http request object
	 * @return {@code true} if keep-alive is set in the header {@code false}
	 *         otherwise
	 */
	private boolean isKeepAlive(HttpRequest req) {
		String keepAlive = req.getHeader(HttpHeaders.Values.KEEP_ALIVE);
		if (keepAlive != null && keepAlive.length() > 0) {
			return true;
		} else {
			// TODO: Keep-Alive value is like 'timeout=15, max=500'
			return false;
		}
	}

	/**
	 * Set the content length in the response
	 * 
	 * @param res
	 *            the http response object
	 * @param readableBytes
	 *            the length of the bytes
	 */
	private void setContentLength(HttpResponse res, int readableBytes) {
		res.setHeader(CONTENT_LENGTH, readableBytes);
	}

	/**
	 * private method that handles the web socket frame data, this method is
	 * used only after the WebSocket connection is established.
	 * 
	 * @param ctx
	 *            the channel handler context
	 * @param msg
	 *            the web socket frame data
	 */
	private void handleWebSocketFrame(ChannelHandlerContext ctx,
			WebSocketFrame msg) {
		engine.processPacket(connector, new RawPacket(msg.getTextData()));
	}

	/**
	 * Handles the initial HTTP request for handshaking if the http request
	 * contains Upgrade header value as WebSocket then this method sends the
	 * handshake response and also fires the events on client connector.
	 * 
	 * @param ctx
	 *            the channel handler context
	 * @param req
	 *            the request message
	 */
	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) {
		// Allow only GET methods.
		if (req.getMethod() != HttpMethod.GET) {
			sendHttpResponse(ctx, req, new DefaultHttpResponse(
					HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
			return;
		}

		// Serve the WebSocket handshake request.
		if (HttpHeaders.Values.UPGRADE.equalsIgnoreCase(req
				.getHeader(HttpHeaders.Names.CONNECTION))
				&& HttpHeaders.Values.WEBSOCKET.equalsIgnoreCase(req
						.getHeader(HttpHeaders.Names.UPGRADE))) {

			// Create the WebSocket handshake response.
			HttpResponse response = constructHandShakeResponse(req, ctx);

			// write the response
			ctx.getChannel().write(response);

			// since handshaking is done, replace the encoder/decoder with
			// web socket data frame encoder/decoder
			ChannelPipeline p = ctx.getChannel().getPipeline();
			p.remove("aggregator");
			p.replace("decoder", "wsdecoder", new WebSocketFrameDecoder());
			p.replace("encoder", "wsencoder", new WebSocketFrameEncoder());

			// initialize the connector
			connector = initializeConnector(ctx, req);

			return;
		}

		// Send an error page otherwise.
		sendHttpResponse(ctx, req, new DefaultHttpResponse(
				HttpVersion.HTTP_1_1, HttpResponseStatus.FORBIDDEN));
	}

	/**
	 * Constructs the <tt>HttpResponse</tt> object for the handshake response
	 * 
	 * @param req
	 *            the http request object
	 * @param ctx
	 *            the channel handler context
	 * @return the http handshake response
	 */
	private HttpResponse constructHandShakeResponse(HttpRequest req,
			ChannelHandlerContext ctx) {
		HttpResponse res = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
				new HttpResponseStatus(101, "Web Socket Protocol Handshake"));
		res.addHeader(HttpHeaders.Names.UPGRADE, HttpHeaders.Values.WEBSOCKET);
		res.addHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.UPGRADE);
		res.addHeader(HttpHeaders.Names.WEBSOCKET_ORIGIN, req
				.getHeader(HttpHeaders.Names.ORIGIN));
		res.addHeader(HttpHeaders.Names.WEBSOCKET_LOCATION,
				getWebSocketLocation(req));
		String protocol = req.getHeader(HttpHeaders.Names.WEBSOCKET_PROTOCOL);
		if (protocol != null) {
			res.addHeader(HttpHeaders.Names.WEBSOCKET_PROTOCOL, protocol);
		}
		return res;

	}

	/**
	 * Initialize the {@code NettyConnector} after initial handshaking is
	 * successfull.
	 * 
	 * @param ctx
	 *            the channel handler context
	 * @param req
	 *            the http request object
	 */
	private WebSocketConnector initializeConnector(ChannelHandlerContext ctx, HttpRequest req) {

		RequestHeader header = getRequestHeader(req);
		
		// TODO: figure out how to use it with netty.
		// set socket timeout to given amount of milliseconds
		// check min and max timeout ranges
		int lSessionTimeout = header.getTimeout(Config.DEFAULT_TIMEOUT);
		if (lSessionTimeout > Config.MAX_TIMEOUT) {
			lSessionTimeout = Config.MAX_TIMEOUT;
		} else if (lSessionTimeout < Config.MIN_TIMEOUT) {
			lSessionTimeout = Config.MIN_TIMEOUT;
		}

		// create connector
		WebSocketConnector theConnector = new NettyConnector(engine, this);
		theConnector.setHeader(header);

		engine.getConnectors().put(theConnector.getId(), theConnector);
		theConnector.startConnector();
		// allow descendant classes to handle connector started event
		engine.connectorStarted(theConnector);
		return theConnector;

	}

	/**
	 * Construct the request header to save it in the connector
	 * @param req the http request header
	 * @return the request header
	 */
	private RequestHeader getRequestHeader(HttpRequest req) {
		RequestHeader header = new RequestHeader();
		Map<String, String> args = new HashMap<String, String>();
		String searchString = "";
		String path = req.getUri();

		// isolate search string
		int pos = path.indexOf(Config.PATHARG_SEPARATOR);
		if (pos >= 0) {
			searchString = path.substring(pos + 1);
			if (searchString.length() > 0) {
				String[] lArgs = searchString.split(Config.ARGARG_SEPARATOR);
				for (int i = 0; i < lArgs.length; i++) {
					String[] lKeyValuePair = lArgs[i].split(
							Config.KEYVAL_SEPARATOR, 2);
					if (lKeyValuePair.length == 2) {
						args.put(lKeyValuePair[0], lKeyValuePair[1]);
						if (log.isDebugEnabled()) {
							log.debug("arg" + i + ": " + lKeyValuePair[0] + "="
									+ lKeyValuePair[1]);
						}
					}
				}
			}
		}
		// set default sub protocol if none passed
		if (args.get("prot") == null) {
			args.put("prot", Config.SUB_PROT_DEFAULT);
		}
		header.put(ARGS, args);
		header.put(ORIGIN, req.getHeader(HttpHeaders.Names.ORIGIN));
		header.put(LOCATION, getWebSocketLocation(req));
		header.put(PATH, req.getUri());

		header.put(SEARCH_STRING, searchString);
		header.put(HOST, req.getHeader(HttpHeaders.Names.HOST));
		return header;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeComplete(ChannelHandlerContext ctx, WriteCompletionEvent e)
			throws Exception {
		super.writeComplete(ctx, e);
	}

	/**
	 * Returns the web socket location URL
	 * 
	 * @param req
	 *            the http request object
	 * @return the location url string
	 */
	private String getWebSocketLocation(HttpRequest req) {
		String location = "ws://" + req.getHeader(HttpHeaders.Names.HOST)
				+ req.getUri();
		return location;
	}

	/**
	 * Returns the channel context
	 * 
	 * @return the channel context
	 */
	public ChannelHandlerContext getChannelHandlerContext() {
		return context;
	}
}
