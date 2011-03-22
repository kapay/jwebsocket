// ---------------------------------------------------------------------------
// jWebSocket - Copyright (c) 2010 Innotrade GmbH
// ---------------------------------------------------------------------------
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published by the
// Free Software Foundation; either version 3 of the License, or (at your
// option) any later version.
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
// for more details.
// You should have received a copy of the GNU Lesser General Public License
// along with this program; if not, see <http://www.gnu.org/licenses/lgpl.html>.
// ---------------------------------------------------------------------------
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
public class TwitterStreamSettingsActivity extends Dialog {

	private TwitterSettingsListener listener;
	private EditText keywordsText;
	private Button setBtn;

	public TwitterStreamSettingsActivity(Context context, TwitterSettingsListener listener) {
		super(context);
		this.listener = listener;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.twitter_stream_settings);
		setTitle("Twitter strea settings");
		keywordsText = (EditText) findViewById(R.id.keywordsTxt);
		setBtn = (Button) findViewById(R.id.setButton);
		setBtn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View view) {
				listener.setSettings(keywordsText.getText().toString());
				TwitterStreamSettingsActivity.this.dismiss();
			}
		});
	}

	public interface TwitterSettingsListener {

		public void setSettings(String keywords);
	}
}
