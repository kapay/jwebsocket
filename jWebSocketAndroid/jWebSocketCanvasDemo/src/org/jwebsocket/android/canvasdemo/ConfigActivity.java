/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.android.canvasdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

/**
 *
 * @author aschulze
 */
public class ConfigActivity extends Activity {

    private Button lBtnCancel;
    private Button lBtnSave;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.config_hvga_p);

        lBtnCancel = (Button) findViewById(R.id.btnCancel);
        lBtnSave = (Button) findViewById(R.id.btnSave);

        lBtnCancel.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "DISCARDING...",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        lBtnSave.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "SAVING...",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
