/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.android.canvasdemo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 *
 * @author aschulze
 */
public class MainActivity extends ListActivity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // setContentView(R.layout.config_hvga_p);

        String[] ITEMS = {"Fundamentals", "Canvas Demo", "Setup"};

        setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, ITEMS));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        startActivity(new Intent(MainActivity.this, CanvasActivity.class));
                        break;
                    case 2:
                        MainActivity.this.startActivity(new Intent(MainActivity.this, ConfigActivity.class));
                        break;
                }
                //Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                //       Toast.LENGTH_SHORT).show();
            }
        });
    }
}
