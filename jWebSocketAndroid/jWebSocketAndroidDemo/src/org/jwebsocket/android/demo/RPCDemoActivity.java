/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.android.demo;

import java.util.List;


import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.client.plugins.rpc.RPCCallable;
import org.jwebsocket.client.plugins.rpc.Rpc;
import org.jwebsocket.client.plugins.rpc.Rrpc;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.plugins.rpc.CommonRpcPlugin;
import org.jwebsocket.token.Token;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 *
 * @author prashant
 */
public class RPCDemoActivity extends Activity implements WebSocketClientTokenListener {

	private EditText classTxt;
	private EditText methodTxt;
	private EditText parameterTxt;
	private EditText targetTxt;
	private EditText resultTxt;
	private Button invokeBtn;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.rpc_demo);
		classTxt = (EditText) findViewById(R.id.classTxt);
		methodTxt = (EditText) findViewById(R.id.methodTxt);
		parameterTxt = (EditText) findViewById(R.id.parameterTxt);
		resultTxt = (EditText) findViewById(R.id.resultTxt);
		targetTxt = (EditText) findViewById(R.id.targetTxt);
		invokeBtn = (Button) findViewById(R.id.invokeBtn);
		invokeBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				sendMethodInvokeToken();
			}
		});
	}

	private void sendMethodInvokeToken() {
		//TODO:validate the text fields first
		String lClassName = classTxt.getText().toString().trim();
		String lMethodName = methodTxt.getText().toString().trim();
		String lParameter = parameterTxt.getText().toString().trim();
		String lTarget = targetTxt.getText().toString().trim();

		//If we make a simple rpc
		if ("server".equals(lTarget)) {
			new Rpc(lClassName, lMethodName).send(lParameter).call();
		} else {
			new Rrpc(lClassName, lMethodName).to(lTarget).send(lParameter).call();
		}
//      	new Rrpc("org.jwebsocket.android.demo.RPCDemoActivity", "rrpcTest1").to(lTarget).send(lParameter).call();

//        Token rpcToken = TokenFactory.createToken("rpc");
//        rpcToken.setString("ns", CommonRpcPlugin.NS_RPC_DEFAULT);
//        rpcToken.setString("classname", className);
//        rpcToken.setString("method", methodName);
//        rpcToken.setString("args", parameter);
//        rpcToken.setBoolean("spawnThread", Boolean.FALSE);
	}

	@Override
	protected void onResume() {
		super.onResume();
		connect();
		RPCDemoActivity.mContext = getApplicationContext();
	}

	@Override
	protected void onPause() {
		super.onPause();
		disConnect();
	}

	private void connect() {
		try {
			JWC.addListener(this);
			JWC.open();
		} catch (WebSocketException ex) {
			resultTxt.setText(ex.getMessage());
		}
	}

	private void disConnect() {
		try {
			JWC.removeListener(this);
			JWC.close();
		} catch (WebSocketException ex) {
			//TODO: log exception
		}
	}

	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		if ((CommonRpcPlugin.RPC_TYPE).equals(aToken.getString("reqType"))
				&& ("response").equals(aToken.getType())) {
			if (aToken.getInteger("code") == 0) {
				resultTxt.setText(aToken.getString("result"));
			} else if (aToken.getInteger("code") == -1) {
				resultTxt.setText(aToken.getString("msg"));
			}
		}
	}

	public void processOpened(WebSocketClientEvent aEvent) {
	}

	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
	}

	public void processClosed(WebSocketClientEvent aEvent) {
	}
	private static Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Toast.makeText(mContext, msg.getData().get("method")
					+ " has been called by the server (args "
					+ msg.getData().get("args"), Toast.LENGTH_SHORT).show();
		}
	};
	private static Context mContext;

	@RPCCallable
	public static void rrpcTest1() {
		Bundle b = new Bundle();
		b.putString("method", "rrpcTest1");
		b.putString("args", "null");
		Message msg = new Message();
		msg.setData(b);
		handler.sendMessage(msg);
	}

	@RPCCallable
	public static void rrpcTest1(String arg1) {
		Bundle b = new Bundle();
		b.putString("method", "rrpcTest1");
		b.putString("args", arg1);
		Message msg = new Message();
		msg.setData(b);
		handler.sendMessage(msg);
	}

	@RPCCallable
	public static void rrpcTest1(int arg1) {
		Bundle b = new Bundle();
		b.putString("method", "rrpcTest1");
		b.putString("args", String.valueOf(arg1));
		Message msg = new Message();
		msg.setData(b);
		handler.sendMessage(msg);
	}

	@RPCCallable
	public static void rrpcTest2(List<String> aList, List<List<Integer>> aList2) {
		Bundle b = new Bundle();
		b.putString("method", "rrpcTest2");
		b.putString("args", aList.toString() + ", " + aList2.toString());
		Message msg = new Message();
		msg.setData(b);
		handler.sendMessage(msg);
	}

	public static void rrpcTest3() {
		Bundle b = new Bundle();
		b.putString("method", "rrpcTest3");
		b.putString("args", "null");
		Message msg = new Message();
		msg.setData(b);
		handler.sendMessage(msg);
	}
}
