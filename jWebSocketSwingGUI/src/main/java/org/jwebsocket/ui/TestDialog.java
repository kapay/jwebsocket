//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 Innotrade GmbH
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

/*
 * TestDialog.java
 *
 * Created on Mar 15, 2010, 2:55:47 PM
 */
package org.jWebSocket.ui;

import java.awt.Toolkit;

import javax.swing.ImageIcon;

import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;

/**
 * Java Swing client for jWebSocket
 * @author aschulze
 * @version $Id:$
 */
public class TestDialog extends javax.swing.JFrame implements WebSocketClientTokenListener {

	private static final long serialVersionUID = 1L;
	private BaseTokenClient client = null;
	private int prevStatus = BaseTokenClient.DISCONNECTED;
	private ImageIcon icoDisconnected = null;
	private ImageIcon icoConnected = null;
	private ImageIcon icoAuthenticated = null;

	/** Creates new form TestDialog */
	public TestDialog() {
		initComponents();
		try {
			client = new BaseTokenClient();
			client.addListener(this);
			icoDisconnected = new ImageIcon(getClass().getResource("/images/disconnected.png"));
			icoConnected = new ImageIcon(getClass().getResource("/images/connected.png"));
			icoAuthenticated = new ImageIcon(getClass().getResource("/images/authenticated.png"));
			checkStatusIcon();
		} catch (Exception ex) {
			System.out.println(ex.getClass().getSimpleName() + ": " + ex.getMessage());
		}
	}

	private void checkStatusIcon() {
		int lStatus = BaseTokenClient.DISCONNECTED;
		if (client.getUsername() != null) {
			lStatus = BaseTokenClient.AUTHENTICATED;
		} else if (client.isConnected()) {
			lStatus = BaseTokenClient.CONNECTED;
		}
		String lClientId = client.getClientId();
		lblStatus.setText("Client-Id: " + (lClientId != null ? lClientId : "-"));
		if (lStatus != prevStatus) {
			prevStatus = lStatus;
			if (lStatus == BaseTokenClient.AUTHENTICATED) {
				lblStatus.setIcon(icoAuthenticated);
			} else if (lStatus == BaseTokenClient.CONNECTED) {
				lblStatus.setIcon(icoConnected);
			} else {
				lblStatus.setIcon(icoDisconnected);
			}
		}
	}

	@Override
	public void processOpened(WebSocketClientEvent aEvent) {
		txaLog.append("Opened.\n");
		checkStatusIcon();
	}

	@Override
	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
		// ignore that here
	}

	@Override
	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		txaLog.append("Received Token: " + aToken.toString() + "\n");
		checkStatusIcon();
	}

	@Override
	public void processClosed(WebSocketClientEvent aEvent) {
		txaLog.append("Closed.\n");
		checkStatusIcon();
	}

	/**
	 * This method is called from within the constructor to initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is always
	 * regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        lblTitle = new javax.swing.JLabel();
        bntSend = new javax.swing.JButton();
        scpTextArea = new javax.swing.JScrollPane();
        txaLog = new javax.swing.JTextArea();
        btnClearLog = new javax.swing.JButton();
        txfMessage = new javax.swing.JTextField();
        btnConnect = new javax.swing.JButton();
        btnDisconnect = new javax.swing.JButton();
        btnSend = new javax.swing.JButton();
        btnBroadcast = new javax.swing.JButton();
        btnPing = new javax.swing.JButton();
        btnShutdown = new javax.swing.JButton();
        btnLogin = new javax.swing.JButton();
        btnLogout = new javax.swing.JButton();
        txfTarget = new javax.swing.JTextField();
        lblTarget = new javax.swing.JLabel();
        lblMessage = new javax.swing.JLabel();
        lblStatus = new javax.swing.JLabel();
        btnGetSessions = new javax.swing.JButton();
        mnbMain = new javax.swing.JMenuBar();
        pmnFile = new javax.swing.JMenu();
        mniExit = new javax.swing.JMenuItem();
        pmnTests = new javax.swing.JMenu();
        mniConnect = new javax.swing.JMenuItem();
        mniDisconnect = new javax.swing.JMenuItem();
        mniSend = new javax.swing.JMenuItem();
        mniBroadcast = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("jWebSocket Test UI");
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/Synapso16x16.png")));
        setMinimumSize(new java.awt.Dimension(640, 480));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.GridBagLayout());

        lblTitle.setFont(new java.awt.Font("Arial", 1, 16));
        lblTitle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Synapso32x32.png"))); // NOI18N
        lblTitle.setText("jWebSocket Java Client Tester 0.1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(lblTitle, gridBagConstraints);

        bntSend.setText("Send");
        bntSend.setMaximumSize(new java.awt.Dimension(100, 20));
        bntSend.setMinimumSize(new java.awt.Dimension(100, 20));
        bntSend.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(bntSend, gridBagConstraints);

        txaLog.setColumns(20);
        txaLog.setRows(5);
        scpTextArea.setViewportView(txaLog);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(scpTextArea, gridBagConstraints);

        btnClearLog.setText("Clear");
        btnClearLog.setMaximumSize(new java.awt.Dimension(100, 20));
        btnClearLog.setMinimumSize(new java.awt.Dimension(100, 20));
        btnClearLog.setPreferredSize(new java.awt.Dimension(100, 20));
        btnClearLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearLogActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(btnClearLog, gridBagConstraints);

        txfMessage.setText("Message");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(txfMessage, gridBagConstraints);

        btnConnect.setText("Connect");
        btnConnect.setMaximumSize(new java.awt.Dimension(100, 20));
        btnConnect.setMinimumSize(new java.awt.Dimension(100, 20));
        btnConnect.setPreferredSize(new java.awt.Dimension(100, 20));
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(btnConnect, gridBagConstraints);

        btnDisconnect.setText("Disconnect");
        btnDisconnect.setMaximumSize(new java.awt.Dimension(100, 20));
        btnDisconnect.setMinimumSize(new java.awt.Dimension(100, 20));
        btnDisconnect.setPreferredSize(new java.awt.Dimension(100, 20));
        btnDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisconnectActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        getContentPane().add(btnDisconnect, gridBagConstraints);

        btnSend.setText("Send");
        btnSend.setMaximumSize(new java.awt.Dimension(100, 20));
        btnSend.setMinimumSize(new java.awt.Dimension(100, 20));
        btnSend.setPreferredSize(new java.awt.Dimension(100, 20));
        btnSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSendActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        getContentPane().add(btnSend, gridBagConstraints);

        btnBroadcast.setText("Broadcast");
        btnBroadcast.setMaximumSize(new java.awt.Dimension(100, 20));
        btnBroadcast.setMinimumSize(new java.awt.Dimension(100, 20));
        btnBroadcast.setPreferredSize(new java.awt.Dimension(100, 20));
        btnBroadcast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBroadcastActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        getContentPane().add(btnBroadcast, gridBagConstraints);

        btnPing.setText("Ping");
        btnPing.setMaximumSize(new java.awt.Dimension(100, 20));
        btnPing.setMinimumSize(new java.awt.Dimension(100, 20));
        btnPing.setPreferredSize(new java.awt.Dimension(100, 20));
        btnPing.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPingActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        getContentPane().add(btnPing, gridBagConstraints);

        btnShutdown.setText("Shutdown");
        btnShutdown.setMaximumSize(new java.awt.Dimension(100, 20));
        btnShutdown.setMinimumSize(new java.awt.Dimension(100, 20));
        btnShutdown.setPreferredSize(new java.awt.Dimension(100, 20));
        btnShutdown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShutdownActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(btnShutdown, gridBagConstraints);

        btnLogin.setText("Login");
        btnLogin.setToolTipText("Logout");
        btnLogin.setMaximumSize(new java.awt.Dimension(100, 20));
        btnLogin.setMinimumSize(new java.awt.Dimension(100, 20));
        btnLogin.setPreferredSize(new java.awt.Dimension(100, 20));
        btnLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoginActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        getContentPane().add(btnLogin, gridBagConstraints);

        btnLogout.setText("Logout");
        btnLogout.setMaximumSize(new java.awt.Dimension(100, 20));
        btnLogout.setMinimumSize(new java.awt.Dimension(100, 20));
        btnLogout.setPreferredSize(new java.awt.Dimension(100, 20));
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        getContentPane().add(btnLogout, gridBagConstraints);

        txfTarget.setText("*");
        txfTarget.setMinimumSize(new java.awt.Dimension(100, 20));
        txfTarget.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(txfTarget, gridBagConstraints);

        lblTarget.setText("Target");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        getContentPane().add(lblTarget, gridBagConstraints);

        lblMessage.setText("Message");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 0);
        getContentPane().add(lblMessage, gridBagConstraints);

        lblStatus.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        lblStatus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/disconnected.png"))); // NOI18N
        lblStatus.setText("ID: -");
        lblStatus.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 5);
        getContentPane().add(lblStatus, gridBagConstraints);

        btnGetSessions.setLabel("Get Sessions");
        btnGetSessions.setMaximumSize(new java.awt.Dimension(100, 20));
        btnGetSessions.setMinimumSize(new java.awt.Dimension(100, 20));
        btnGetSessions.setPreferredSize(new java.awt.Dimension(100, 20));
        btnGetSessions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGetSessionsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        getContentPane().add(btnGetSessions, gridBagConstraints);

        pmnFile.setText("File");

        mniExit.setText("Exit");
        mniExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniExitActionPerformed(evt);
            }
        });
        pmnFile.add(mniExit);

        mnbMain.add(pmnFile);

        pmnTests.setText("Edit");

        mniConnect.setText("Connect");
        pmnTests.add(mniConnect);

        mniDisconnect.setText("Disconnect");
        pmnTests.add(mniDisconnect);

        mniSend.setText("Send");
        pmnTests.add(mniSend);

        mniBroadcast.setText("Broadcast");
        pmnTests.add(mniBroadcast);

        mnbMain.add(pmnTests);

        setJMenuBar(mnbMain);

        pack();
    }// </editor-fold>//GEN-END:initComponents

	public void checkDisconnect() {
		try {
			if (client.isConnected()) {
				client.close();
			}
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}

	private void doCloseForm() {
		checkDisconnect();
		dispose();
	}

	private void mniExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniExitActionPerformed
		doCloseForm();
	}//GEN-LAST:event_mniExitActionPerformed

	private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
		checkDisconnect();
	}//GEN-LAST:event_formWindowClosing

	private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnConnectActionPerformed
		try {
			client.open("ws://localhost:8787");
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnConnectActionPerformed

	private void btnDisconnectActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnDisconnectActionPerformed
		try {
			client.close();
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnDisconnectActionPerformed

	private void btnShutdownActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnShutdownActionPerformed
		try {
			client.disconnect();
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnShutdownActionPerformed

	private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnLoginActionPerformed
		try {
			client.login("guest", "guest");
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnLoginActionPerformed

	private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnLogoutActionPerformed
		try {
			client.logout();
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnLogoutActionPerformed

	private void btnClearLogActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnClearLogActionPerformed
		txaLog.setText("");
	}// GEN-LAST:event_btnClearLogActionPerformed

	private void btnBroadcastActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnBroadcastActionPerformed
		try {
			client.broadcastText(txfMessage.getText());
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnBroadcastActionPerformed

	private void btnPingActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnPingActionPerformed
		try {
			client.ping(true);
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnPingActionPerformed

	private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnSendActionPerformed
		try {
			client.sendText(txfTarget.getText(), txfMessage.getText());
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnSendActionPerformed

	private void btnGetSessionsActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnGetSessionsActionPerformed
		try {
			client.getConnections();
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}// GEN-LAST:event_btnGetSessionsActionPerformed

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				new TestDialog().setVisible(true);
			}
		});
	}
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton bntSend;
    private javax.swing.JButton btnBroadcast;
    private javax.swing.JButton btnClearLog;
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnDisconnect;
    private javax.swing.JButton btnGetSessions;
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnPing;
    private javax.swing.JButton btnSend;
    private javax.swing.JButton btnShutdown;
    private javax.swing.JLabel lblMessage;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JLabel lblTarget;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JMenuBar mnbMain;
    private javax.swing.JMenuItem mniBroadcast;
    private javax.swing.JMenuItem mniConnect;
    private javax.swing.JMenuItem mniDisconnect;
    private javax.swing.JMenuItem mniExit;
    private javax.swing.JMenuItem mniSend;
    private javax.swing.JMenu pmnFile;
    private javax.swing.JMenu pmnTests;
    private javax.swing.JScrollPane scpTextArea;
    private javax.swing.JTextArea txaLog;
    private javax.swing.JTextField txfMessage;
    private javax.swing.JTextField txfTarget;
    // End of variables declaration//GEN-END:variables
}
