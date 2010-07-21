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
package org.jwebsocket.android.canvasdemo;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

/**
 *
 * @author aschulze
 */
public class Fundamentals extends Activity {

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.fundamentals_hvga_p);

        Toast.makeText(getApplicationContext(), "CONNECTING...",
                Toast.LENGTH_SHORT).show();
        // JWC.open();
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(), "DISCONNECTING...",
                Toast.LENGTH_SHORT).show();
        // JWC.close();

        super.onDestroy();

    }
}
