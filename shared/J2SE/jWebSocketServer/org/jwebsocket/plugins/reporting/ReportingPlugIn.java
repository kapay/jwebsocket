//	---------------------------------------------------------------------------
//	jWebSocket - jWebSocket Reporting Plug-In
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
package org.jwebsocket.plugins.reporting;

import java.io.File;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import javolution.util.FastList;
import javolution.util.FastMap;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.jwebsocket.api.PluginConfiguration;
import org.jwebsocket.api.WebSocketConnector;
import org.jwebsocket.config.JWebSocketServerConstants;
import org.jwebsocket.kit.PlugInResponse;
import org.jwebsocket.logging.Logging;
import org.jwebsocket.plugins.TokenPlugIn;
import org.jwebsocket.server.TokenServer;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;
import org.jwebsocket.util.Tools;

/**
 *
 * @author aschulze
 */
public class ReportingPlugIn extends TokenPlugIn {

	private static Logger mLog = Logging.getLogger(ReportingPlugIn.class);
	// if namespace changed update client plug-in accordingly!
	private static final String NS_TEST = JWebSocketServerConstants.NS_BASE + ".plugins.reporting";
	private String mReportFolder = null;
	private String mOutputFolder = null;
	private String mOutputURL = null;

	/**
	 *
	 * @param aConfiguration
	 */
	public ReportingPlugIn(PluginConfiguration aConfiguration) {
		super(aConfiguration);
		if (mLog.isDebugEnabled()) {
			mLog.debug("Instantiating reporting plug-in...");
		}
		// specify default name space for admin plugin
		this.setNamespace(NS_TEST);
		mGetSettings();
	}

	private void mGetSettings() {
		mReportFolder = getString("reportFolder", "${JWEBSOCKET_HOME}/reports");
		mReportFolder = FilenameUtils.separatorsToUnix(Tools.expandEnvVars(mReportFolder));
		if (!mReportFolder.endsWith("/")) {
			mReportFolder += "/";
		}
		mOutputFolder = getString("outputFolder", "${JWEBSOCKET_HOME}/reports");
		mOutputFolder = FilenameUtils.separatorsToUnix(Tools.expandEnvVars(mOutputFolder));
		if (!mOutputFolder.endsWith("/")) {
			mOutputFolder += "/";
		}
		mOutputURL = getString("outputURL", "http://localhost/");
		mOutputURL = FilenameUtils.separatorsToUnix(Tools.expandEnvVars(mOutputURL));
		if (!mOutputURL.endsWith("/")) {
			mOutputURL += "/";
		}
	}

	@Override
	public void processToken(PlugInResponse aResponse,
			WebSocketConnector aConnector, Token aToken) {
		String lType = aToken.getType();
		String lNS = aToken.getNS();

		if (lType != null && getNamespace().equals(lNS)) {
			if ("createReport".equals(lType)) {
				createReport(aConnector, aToken);
			} else if ("getReports".equals(lType)) {
				getReports(aConnector, aToken);
			} else if ("getReportParams".equals(lType)) {
				getReportParams(aConnector, aToken);
			} else if ("uploadReport".equals(lType)) {
				uploadReport(aConnector, aToken);
			} else if ("downloadReport".equals(lType)) {
				downloadReport(aConnector, aToken);
			}
		}
	}

	private String getReportPath(String aReportId) {
		return mReportFolder + aReportId + ".jrxml";
	}

	private void createReport(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse;

		TokenPlugIn lJDBCPlugIn = (TokenPlugIn) lServer.getPlugInById("jws.jdbc");
		if (lJDBCPlugIn == null) {
			// send response to requester
			lResponse = lServer.createErrorToken(aToken, -1, "JDBC plug-in not loaded.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		String lReportId = aToken.getString("reportId");
		if (lReportId == null) {
			lResponse = lServer.createErrorToken(aToken, -1, "No report id passed.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		String lOutputType = aToken.getString("outputType");
		if (lOutputType == null) {
			lOutputType = "pdf";
		}
		if (!("pdf".equals(lOutputType)
				|| "html".equals(lOutputType))) {
			lResponse = lServer.createErrorToken(aToken, -1, "Invalid report export type passed.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}
		try {
			FileUtils.forceMkdir(new File(mOutputFolder));
		} catch (Exception ex) {
			lResponse = lServer.createErrorToken(aToken, -1, ex.getClass().getSimpleName() + ": " + ex.getMessage());
			lServer.sendToken(aConnector, lResponse);
			return;
		}
		JasperReport lReport;
		JasperPrint lPrint;
		Map<String, String> lParms = new FastMap<String, String>();

		// instantiate response token
		lResponse = lServer.createResponse(aToken);

		try {
			DataSource lDataSource = (DataSource) Tools.invoke(
					lJDBCPlugIn, "getNativeDataSource", null);

			lReport = JasperCompileManager.compileReport(
					getReportPath(lReportId));
			lPrint = JasperFillManager.fillReport(lReport,
					lParms, lDataSource.getConnection());
			if ("pdf".equals(lOutputType)) {
				JasperExportManager.exportReportToPdfFile(lPrint,
						mOutputFolder + lReportId + ".pdf");
				lResponse.setString("url", mOutputURL + lReportId + ".pdf");
			} else {
				JasperExportManager.exportReportToHtmlFile(lPrint,
						mOutputFolder + lReportId + ".html");
				lResponse.setString("url", mOutputURL + lReportId + ".html");
			}
		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void getReports(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();
		Token lResponse;

		TokenPlugIn lFileSystemPlugIn = (TokenPlugIn) lServer.getPlugInById("jws.filesystem");
		if (lFileSystemPlugIn == null) {
			// send response to requester
			lResponse = lServer.createErrorToken(aToken, -1, "Filesystem plug-in not loaded.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		// instantiate response token
		lResponse = lServer.createResponse(aToken);

		Token lGetFilelist = TokenFactory.createToken(
				lFileSystemPlugIn.getNamespace(), "getFilelist");
		lGetFilelist.setString("alias", "reportRoot");
		lGetFilelist.setBoolean("recursive", false);
		List<String> lFilemasks = new FastList<String>();
		lFilemasks.add("*.jrxml");
		lGetFilelist.setList("filemasks", lFilemasks);

		lResponse = lFileSystemPlugIn.invoke(aConnector, lGetFilelist);
		lServer.setResponseFields(aToken, lResponse);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void getReportParams(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		String lReportId = aToken.getString("reportId");
		if (lReportId == null) {
			lResponse = lServer.createErrorToken(aToken, -1, "No report id passed.");
			lServer.sendToken(aConnector, lResponse);
			return;
		}

		try {
			JasperReport lReport = JasperCompileManager.compileReport(
					getReportPath(lReportId));

			JRParameter[] lParams = lReport.getParameters();
			Map lParamMap = new FastMap<String, String>();
			for (int lIdx = 0; lIdx < lParams.length; lIdx++) {
				JRParameter lParam = lParams[lIdx];
				Map lParamData = new FastMap<String, String>();
				lParamData.put("className", lParam.getValueClassName());
				lParamData.put("description", lParam.getDescription());
				lParamData.put("isForPrompting", lParam.isForPrompting());
				lParamMap.put(lParam.getName(), lParamData);
			}
			lResponse.setMap("params", lParamMap);
		} catch (Exception ex) {
			lResponse.setInteger("code", -1);
			lResponse.setString("msg", ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void uploadReport(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}

	private void downloadReport(WebSocketConnector aConnector, Token aToken) {
		TokenServer lServer = getServer();

		// instantiate response token
		Token lResponse = lServer.createResponse(aToken);

		// send response to requester
		lServer.sendToken(aConnector, lResponse);
	}
}
