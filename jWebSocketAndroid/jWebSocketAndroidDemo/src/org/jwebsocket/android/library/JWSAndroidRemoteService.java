//	---------------------------------------------------------------------------
//	jWebSocket - Copyright (c) 2010 jwebsocket.org
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
package org.jwebsocket.android.library;

import java.util.Properties;

import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.token.BaseTokenClient;
import org.jwebsocket.kit.RawPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.widget.Toast;
import android.util.Log;

/**
 * jWebSocket android service that runs in a different process than the
 * application. Because it runs in another process, the client code using this
 * process must use IPC to interact with it. Please follow the sample
 * application to see the usage of this service.
 * <p>
 * Note that the most applications do not need to deal with the complexity shown
 * here. If your application simply has a service running in its own process,
 * the {@code JWC} is a simpler way to interact with it.
 * </p>
 * 
 * @author puran
 */
public class JWSAndroidRemoteService extends Service {
  /**
   * This is the list of callbacks that have been registered with the service.
   */
  final RemoteCallbackList<IJWSAndroidRemoteServiceCallback> jwsCallbackList = new RemoteCallbackList<IJWSAndroidRemoteServiceCallback>();

  private final static int MT_OPENED = 0;
  private final static int MT_PACKET = 1;
  private final static int MT_CLOSED = 2;
  private final static int MT_TOKEN = 3;
  private final static int MT_ERROR = -1;

  private final static String CONFIG_FILE = "jWebSocket";
  private static String jwsURL = "ws://jwebsocket.org:8787";
  private static BaseTokenClient tokenClient;

  @Override
  public void onCreate() {
    tokenClient = new BaseTokenClient();
    Properties lProps = new Properties();
    try {
      lProps.load(openFileInput(CONFIG_FILE));
    } catch (Exception ex) {
      Toast.makeText(getApplicationContext(), ex.getClass().getSimpleName() + ":" + ex.getMessage(), Toast.LENGTH_SHORT).show();
    }
    jwsURL = (String) lProps.getProperty("url", "http://jwebsocket.org:8787/");
  }

  /**
   * Handler used to execute operations on the main thread. This is used to
   * schedule increments of our value.
   */
  private final Handler jwsHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case MT_ERROR: {
        final int N = jwsCallbackList.beginBroadcast();
        for (int i = 0; i < N; i++) {
          try {
            String error = (String) msg.obj;
            jwsCallbackList.getBroadcastItem(i).onError(error);
          } catch (RemoteException e) {
            // The RemoteCallbackList will take care of removing
            // the dead object for us.
          }
        }
        jwsCallbackList.finishBroadcast();
      }
        break;
      
      default:
        super.handleMessage(msg);
      }
    }
  };

  /**
   * This is the actual <tt>jWebSocket</tt> {@code BaseTokenClient} based
   * implementation of the remote {@code JWSAndroidRemoteService} interface.
   * Note that no exception thrown by the remote process can be sent back to the
   * client.
   */
  private final IJWSAndroidRemoteService.Stub mBinder = new IJWSAndroidRemoteService.Stub() {
    @Override
    public void open() throws RemoteException {
      try {
        tokenClient.open(jwsURL);
      } catch (WebSocketException e) {
        Log.e("ERROR", "Error opening the jWebSocket connection", e);
        Message errorMsg = Message.obtain(messageHandler, -1, e.getMessage());
        jwsHandler.sendMessage(errorMsg);
      }
    }

    @Override
    public void close() throws RemoteException {
      // TODO Auto-generated method stub

    }

    @Override
    public void disconnect() throws RemoteException {
      // TODO Auto-generated method stub

    }

    @Override
    public void send(String data) throws RemoteException {
      // TODO Auto-generated method stub

    }

    @Override
    public void sendText(String target, String data) throws RemoteException {
      // TODO Auto-generated method stub

    }

    @Override
    public void broadcastText(String data) throws RemoteException {
      // TODO Auto-generated method stub

    }

    @Override
    public void sendToken(ParcelableToken token) throws RemoteException {
      // TODO Auto-generated method stub

    }

    @Override
    public void saveFile(String fileName, String scope, boolean notify, byte[] data) throws RemoteException {
      // TODO Auto-generated method stub

    }

    @Override
    public String getUsername() throws RemoteException {
      // TODO Auto-generated method stub
      return null;
    }

    @Override
    public void login(String aUsername, String aPassword) throws RemoteException {
      // TODO Auto-generated method stub

    }

    @Override
    public void logout() throws RemoteException {
      // TODO Auto-generated method stub

    }

    @Override
    public void ping(boolean echo) throws RemoteException {
      // TODO Auto-generated method stub

    }

    @Override
    public void getConnections() throws RemoteException {
      // TODO Auto-generated method stub

    }

    @Override
    public boolean isAuthenticated() throws RemoteException {
      // TODO Auto-generated method stub
      return false;
    }

    @Override
    public void registerCallback(IJWSAndroidRemoteServiceCallback cb) throws RemoteException {
      // TODO Auto-generated method stub

    }

    @Override
    public void unregisterCallback(IJWSAndroidRemoteServiceCallback cb) throws RemoteException {
      // TODO Auto-generated method stub

    }
  };

  private static Handler messageHandler = new Handler() {

    @Override
    public void handleMessage(Message message) {

      switch (message.what) {
      case MT_OPENED:
        break;
      case MT_PACKET:
        break;
      case MT_TOKEN:
        break;
      case MT_CLOSED:
        break;
      }
    }
  };

  /**
   * When binding to the service, we return an interface to our messenger for
   * sending messages to the service.
   */
  @Override
  public IBinder onBind(Intent intent) {
    if (IJWSAndroidRemoteService.class.getName().equals(intent.getAction())) {
      return mBinder;
    }
    return null;
  }

  static class Listener implements WebSocketClientTokenListener {

    public void processOpened(WebSocketClientEvent aEvent) {
      Message lMsg = new Message();
      lMsg.what = MT_OPENED;
      messageHandler.sendMessage(lMsg);
    }

    public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
      Message lMsg = new Message();
      lMsg.what = MT_PACKET;
      lMsg.obj = aPacket;
      messageHandler.sendMessage(lMsg);
    }

    public void processToken(WebSocketClientEvent aEvent, Token aToken) {
      Message lMsg = new Message();
      lMsg.what = MT_TOKEN;
      lMsg.obj = aToken;
      messageHandler.sendMessage(lMsg);
    }

    public void processClosed(WebSocketClientEvent aEvent) {
      Message lMsg = new Message();
      lMsg.what = MT_CLOSED;
      messageHandler.sendMessage(lMsg);
    }
  }
}
