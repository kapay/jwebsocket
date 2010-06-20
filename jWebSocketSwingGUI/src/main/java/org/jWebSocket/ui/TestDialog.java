/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * TestDialog.java
 *
 * Created on Mar 15, 2010, 2:55:47 PM
 */
package org.jWebSocket.ui;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import org.jwebsocket.api.WebSocketListener;
import org.jwebsocket.client.BaseClientJ2SE;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.kit.WebSocketEvent;

/**
 *
 * @author aschulze
 */
public class TestDialog extends javax.swing.JFrame implements WebSocketListener {

	private BaseClientJ2SE jwsClient = null;

	/** Creates new form TestDialog */
	public TestDialog() {
		initComponents();
		try {
			URI lURI = new URI("ws://localhost:8787");
			jwsClient = new BaseClientJ2SE(this);

		} catch (Exception ex) {
			System.out.println(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
	}

	@Override
	public void processOpened(WebSocketEvent aEvent) {
		txaLog.append("Opened.\n");
	}

	@Override
	public void processPacket(WebSocketEvent aEvt) {
		try {
			txaLog.append(new String(aEvt.getData(), "US-ASCII") + "\n");
		} catch (UnsupportedEncodingException ex) {
			System.out.println(ex.getClass().getSimpleName() + ":  " + ex.getMessage());
		}
	}

	@Override
	public void processClosed(WebSocketEvent aEvent) {
		txaLog.append("Closed.\n");
	}

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
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
        mnbMain = new javax.swing.JMenuBar();
        pmnFile = new javax.swing.JMenu();
        mniExit = new javax.swing.JMenuItem();
        pmnTests = new javax.swing.JMenu();
        mniConnect = new javax.swing.JMenuItem();
        mniDisconnect = new javax.swing.JMenuItem();
        mniSend = new javax.swing.JMenuItem();
        mniBroadcast = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(640, 480));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        lblTitle.setFont(new java.awt.Font("Arial", 1, 16)); // NOI18N
        lblTitle.setText("Java Client Test");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(lblTitle, gridBagConstraints);

        bntSend.setText("Send");
        bntSend.setMaximumSize(new java.awt.Dimension(100, 20));
        bntSend.setMinimumSize(new java.awt.Dimension(100, 20));
        bntSend.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(bntSend, gridBagConstraints);

        txaLog.setColumns(20);
        txaLog.setRows(5);
        scpTextArea.setViewportView(txaLog);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
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
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTH;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
        getContentPane().add(btnClearLog, gridBagConstraints);

        txfMessage.setText("Message");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
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
        gridBagConstraints.gridx = 1;
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
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        getContentPane().add(btnDisconnect, gridBagConstraints);

        btnSend.setText("Send");
        btnSend.setMaximumSize(new java.awt.Dimension(100, 20));
        btnSend.setMinimumSize(new java.awt.Dimension(100, 20));
        btnSend.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
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
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 5, 5);
        getContentPane().add(btnBroadcast, gridBagConstraints);

        btnPing.setText("Ping");
        btnPing.setMaximumSize(new java.awt.Dimension(100, 20));
        btnPing.setMinimumSize(new java.awt.Dimension(100, 20));
        btnPing.setPreferredSize(new java.awt.Dimension(100, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
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
        gridBagConstraints.gridx = 1;
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
        gridBagConstraints.gridx = 2;
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
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        getContentPane().add(btnLogout, gridBagConstraints);

        pmnFile.setText("File");

        mniExit.setText("Exit");
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

	private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
		try {
			jwsClient.open("ws://localhost:8787");
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}//GEN-LAST:event_btnConnectActionPerformed

	private void btnDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisconnectActionPerformed
		try {
			jwsClient.close();
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}//GEN-LAST:event_btnDisconnectActionPerformed

	private void btnShutdownActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShutdownActionPerformed
		try {
			jwsClient.send("{\"type\":\"shutdown\"; \"ns\":\"org.jWebSocket.plugins.admin\"}\n", "US-ASCII");
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}//GEN-LAST:event_btnShutdownActionPerformed

	private void btnLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoginActionPerformed
		try {
			jwsClient.send("{\"type\":\"login\"; \"ns\":\"org.jWebSocket.plugins.system\"; \"username\":\"aschulze\"}\n", "US-ASCII");
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}//GEN-LAST:event_btnLoginActionPerformed

	private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
		try {
			jwsClient.send("{\"type\":\"logout\"; \"ns\":\"org.jWebSocket.plugins.system\"}\n", "US-ASCII");
		} catch (WebSocketException ex) {
			txaLog.append(ex.getClass().getSimpleName() + ":  " + ex.getMessage() + "\n");
		}
	}//GEN-LAST:event_btnLogoutActionPerformed

	private void btnClearLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearLogActionPerformed
		txaLog.setText("");
	}//GEN-LAST:event_btnClearLogActionPerformed

	private void btnBroadcastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBroadcastActionPerformed
		// TODO add your handling code here:
	}//GEN-LAST:event_btnBroadcastActionPerformed

	/**
	 * @param args the command line arguments
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
    private javax.swing.JButton btnLogin;
    private javax.swing.JButton btnLogout;
    private javax.swing.JButton btnPing;
    private javax.swing.JButton btnSend;
    private javax.swing.JButton btnShutdown;
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
    // End of variables declaration//GEN-END:variables
}
