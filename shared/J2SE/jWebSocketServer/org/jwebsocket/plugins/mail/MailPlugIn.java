//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket SMTP Plug-In
//  Copyright (c) 2010 Innotrade GmbH, jWebSocket.org
//	---------------------------------------------------------------------------
//  THIS CODE IS FOR RESEARCH, EVALUATION AND TEST PURPOSES ONLY!
//  THIS CODE MAY BE SUBJECT TO CHANGES WITHOUT ANY NOTIFICATION!
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
package org.jwebsocket.plugins.mail;

import java.util.List;
import java.util.Map;
import javolution.util.FastList;
import javolution.util.FastMap;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class MailPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(MailPlugIn.class);
	private static String SMTP_HOST = null;
	private static final String SMTP_HOST_KEY = "smtp_host";
	private static Integer SMTP_PORT = -1;
	private static final String SMTP_PORT_KEY = "smtp_port";
	private static Boolean SMTP_AUTH = false;
	private static final String SMTP_AUTH_KEY = "smtp_auth";
	private static String SMTP_USER = null;
	private static final String SMTP_USER_KEY = "smtp_user";
	private static String SMTP_PASSWORD = null;
	private static final String SMTP_PASSWORD_KEY = "smtp_password";
	private static Boolean SMTP_POP3BEFORE = false;
	private static final String SMTP_POP3BEFORE_KEY  = "smtp_pop3before";
	private static String POP3_HOST = null;
	private static final String POP3_HOST_KEY = "pop3_host";
	private static Integer POP3_PORT = -1;
	private static final String POP3_PORT_KEY = "pop3_port";
	private static String POP3_USER = null;
	private static String POP3_USER_KEY = "pop3_user";
	private static String POP3_PASSWORD = null;
	private static String POP3_PASSWORD_KEY = "pop3_password";
	// if namespace changed update client plug-in accordingly!
	private static final String NS_MAIL = JWebSocketServerConstants.NS_BASE + ".plugins.mail";

	public MailPlugIn() {
		super(null);
	}

	public MailPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating mail plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_MAIL);
		mGetSettings();
	}

	private void mGetSettings() {
		SMTP_HOST = getSetting(SMTP_HOST_KEY, null);
		SMTP_PORT = Integer.parseInt(getSetting(SMTP_PORT_KEY, "25"));
		SMTP_AUTH = getSetting(SMTP_AUTH_KEY, "false").equals("true");
		SMTP_USER = getSetting(SMTP_USER_KEY, null);
		SMTP_PASSWORD = getSetting(SMTP_PASSWORD_KEY, null);
		SMTP_POP3BEFORE = getSetting(SMTP_POP3BEFORE_KEY, "false").equals("true");
		POP3_HOST = getSetting(POP3_HOST_KEY, null);
		POP3_PORT = Integer.parseInt(getSetting(POP3_PORT_KEY, "110"));
		POP3_USER = getSetting(POP3_USER_KEY, null);
		POP3_PASSWORD = getSetting(POP3_PASSWORD_KEY, null);
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && (lNS == null || lNS.equals(getNamespace()))) {
			// select from database
			if (lType.equals("sendMail")) {
				mGetSettings();
				sendMail(aConnector, aToken);
			}
		}
	}

	private void sendMail(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		String lFrom = aToken.getString("from", "[unknown]");
		String lTo = aToken.getString("to");
		String lCC = aToken.getString("cc");
		String lBCC = aToken.getString("bcc");
		String lSubject = aToken.getString("subject");
		String lBody = aToken.getString("body");
		Boolean lIsHTML = aToken.getBoolean("html", false);

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		Map lMap = new FastMap();

		if (lFrom != null && lFrom.length() > 0) {
			lMap.put("from", lFrom);
		}
		if (lTo != null && lTo.length() > 0) {
			lMap.put("to", lTo);
		}
		if (lCC != null && lCC.length() > 0) {
			lMap.put("cc", lCC);
		}
		if (lBCC != null && lBCC.length() > 0) {
			lMap.put("bcc", lBCC);
		}
		if (lSubject != null && lSubject.length() > 0) {
			lMap.put("subject", lSubject);
		}
		if (lBody != null && lBody.length() > 0) {
			lMap.put("body", lBody);
		}

		// Create the attachment
		List<EmailAttachment> lAttachments = new FastList<EmailAttachment>();
		/*
		if( aAttachments != null  ) {
		for( int lIdx = 0; lIdx < aAttachments.length; lIdx++  ) {
		EmailAttachment lAttachment = new EmailAttachment();
		lAttachment.setPath( aAttachments[ lIdx ] );
		lAttachment.setDisposition( EmailAttachment.ATTACHMENT );
		// lAttachment.setDescription( "Picture of John" );
		// lAttachment.setName( "John" );
		lAttachments.add( lAttachment );
		}
		}
		 */
		// Create the lEmail message
		if (mLog.isDebugEnabled()) {
			mLog.debug("Sending e-mail to " + lTo + " with subject '" + lSubject + "'...");
		}
		try {
			Email lEmail;
			if (lIsHTML) {
				lEmail = new HtmlEmail();
			} else {
				lEmail = new MultiPartEmail();
			}

			lEmail.setHostName(SMTP_HOST);
			lEmail.setSmtpPort(SMTP_PORT);
			if (SMTP_AUTH) {
				lEmail.setAuthentication(
						SMTP_USER,
						SMTP_PASSWORD);
			}
			if (SMTP_POP3BEFORE) {
				lEmail.setPopBeforeSmtp(
						true,
						POP3_HOST,
						POP3_USER,
						POP3_PASSWORD);
			}
			if (lFrom != null && lFrom.length() > 0) {
				lEmail.setFrom(lFrom);
			}
			if (lTo != null && lTo.length() > 0) {
				lEmail.addTo(lTo);
			}
			if (lSubject != null && lSubject.length() > 0) {
				lEmail.setSubject(lSubject);
			}

			if (lBody != null && lBody.length() > 0) {
				if (lIsHTML) {
					HtmlEmail lHTML = ((HtmlEmail) lEmail);
					/*
					URL lURL = new URL("http://five-feet-further.com/aschulze/images/portrait_web_kleiner.jpg");
					String lCID = ((HtmlEmail )lEmail).embed(lURL, "five feet further logo");

					//url = new URL( "http://five-feet-further.com/resources/css/IJX4FWDocu.css" );
					// String css = ((HtmlEmail)lEmail).embed( url, "name of css" );

					((HtmlEmail )lEmail).setHtmlMsg(
					"<html><body>" +
					"<style type=\"text/css\">" +
					"h1 { " +
					" font-family:arial, helvetica, sans-serif;" +
					" font-weight:bold;" +
					" font-size:18pt;" +
					"}" +
					"</style>" +
					// "<link href=\"cid:" + css + "\" type=\"text/css\" rel=\"stylesheet\">" +
					"<p><img src=\"cid:" + lCID + "\"></p>" +
					"<p><img src=\"http://five-feet-further.com/aschulze/images/portrait_web_kleiner.jpg\"></p>" +
					lItem +
					"</body></html>");
					 */

					/*
					// Now the message body.
					Multipart mp = new MimeMultipart();

					BodyPart textPart = new MimeBodyPart();
					// sets type to "text/plain"
					textPart.setText("Kann Ihr Browser keine HTML-Mails darstellen?");

					BodyPart pixPart = new MimeBodyPart();
					pixPart.setContent(lMsg, "text/html");

					// Collect the Parts into the MultiPart
					mp.addBodyPart(textPart);
					mp.addBodyPart(pixPart);

					// Put the MultiPart into the Message
					((HtmlEmail) lEmail).setContent((MimeMultipart)mp);
					((HtmlEmail) lEmail).buildMimeMessage();

					/*
					// ((HtmlEmail) lEmail).setContent(lMsg, Email.TEXT_HTML);

					// lHeaders.put("Innotrade-Id", "4711-0815");
					// lHTML.setHeaders(lHeaders);
					// ((HtmlEmail) lEmail).setCharset("UTF-8");
					// ((HtmlEmail) lEmail).setMsg(lMsg);
					lMM.setHeader("Innotrade-Id", "4711-0815");

					// ((HtmlEmail) lEmail).setContent(lTxtMsg, Email.TEXT_PLAIN);
					 */
					// String lTxtMsg = "Your Email-Client does not support HTML messages.";
					lHTML.setHtmlMsg(lBody);
					// lHTML.setTextMsg(lTxtMsg);
				} else {
					lEmail.setMsg(lBody);
				}
			}

			// add attachment(s), if such
			for (EmailAttachment lAttachment : lAttachments) {
				((MultiPartEmail) lEmail).attach(lAttachment);
			}

			for (int lIdx = 0; lIdx < lAttachments.size(); lIdx++) {
				((MultiPartEmail) lEmail).attach(
						(EmailAttachment) lAttachments.get(lIdx));
			}

			// send the Email
			String lMsgId = lEmail.send();

			if (mLog.isInfoEnabled()) {
				mLog.info(
						"Email successfully sent"
						+ " from " + (lFrom != null ? lFrom : "(no sender)")
						+ " to " + (lTo != null ? lTo : "(no receipient)")
						+ ", subject " + (lSubject != null ? "'" + lSubject + "'" : "(no subject)")
						+ ", Id " + lMsgId);
			}

			lResponse.setString("id", lMsgId);
		} catch (Exception lEx) {
			String lMsg = lEx.getClass().getSimpleName() + ": " + lEx.getMessage();
			mLog.error(lMsg);
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", lMsg);
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
