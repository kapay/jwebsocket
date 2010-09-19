/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jwebsocket.android.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;
import org.jwebsocket.token.TokenFactory;

/**
 *
 * @author prashant
 */
public class RPCDemoActivity extends Activity  implements WebSocketClientTokenListener {

    private EditText classTxt;
    private EditText methodTxt;
    private EditText parameterTxt;
    private EditText resultTxt;
    private Button invokeBtn;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.rpc_demo);
        classTxt = (EditText)findViewById(R.id.classTxt);
        methodTxt = (EditText)findViewById(R.id.methodTxt);
        parameterTxt = (EditText)findViewById(R.id.parameterTxt);
        resultTxt = (EditText)findViewById(R.id.resultTxt);
        invokeBtn = (Button)findViewById(R.id.invokeBtn);
        invokeBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                sendMethodInvokeToken();
            }
        });
    }

    private void sendMethodInvokeToken() {
        //TODO:validate the text fields first
        String className = classTxt.getText().toString().trim();
        String methodName = methodTxt.getText().toString().trim();
        String parameter = parameterTxt.getText().toString().trim();
        Token rpcToken = TokenFactory.createToken("rpc");
        rpcToken.setString("ns", "org.jWebSocket.plugins.rpc");
        rpcToken.setString("classname", className);
        rpcToken.setString("method", methodName);
        rpcToken.setString("args", parameter);
        rpcToken.setBoolean("spawnThread", Boolean.FALSE);
        try {
            JWC.sendToken(rpcToken);
        } catch (WebSocketException ex) {
            //TODO: log exception
        }
    }

    
    @Override
    protected void onResume() {
        super.onResume();
        connect();
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
                //TODO: log exception
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
        if(aToken.getString("result") != null) {
            resultTxt.setText(aToken.getString("result"));
        }else {
            resultTxt.setText(aToken.getString("error"));
        }
    }

    public void processOpened(WebSocketClientEvent aEvent) {

    }

    public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {

    }

    public void processClosed(WebSocketClientEvent aEvent) {
        
    }
    

}
