/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jwebsocket.android.demo;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 *
 * @author Prashant
 */
public class TwitterStreamSettingsActivity extends Dialog{

    private TwitterSettingsListener listener;
    private EditText keywordsText;
    private Button setBtn;

    public TwitterStreamSettingsActivity(Context context, TwitterSettingsListener listener)
    {
        super(context);
        this.listener = listener;
    }
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.twitter_stream_settings);
        setTitle("Twitter strea settings");
        keywordsText = (EditText) findViewById(R.id.keywordsTxt);
        setBtn = (Button)findViewById(R.id.setButton);
        setBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                listener.setSettings(keywordsText.getText().toString());
                TwitterStreamSettingsActivity.this.dismiss();
            }
        });
    }
    
    public interface TwitterSettingsListener
    {
        public void setSettings(String keywords);
    }

}
