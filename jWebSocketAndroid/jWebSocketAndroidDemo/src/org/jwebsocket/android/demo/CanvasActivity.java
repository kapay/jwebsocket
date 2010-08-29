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

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import org.jwebsocket.api.WebSocketClientEvent;
import org.jwebsocket.api.WebSocketClientTokenListener;
import org.jwebsocket.api.WebSocketPacket;
import org.jwebsocket.kit.WebSocketException;
import org.jwebsocket.token.Token;

/**
 *
 * @author aschulze
 */
public class CanvasActivity extends Activity implements WebSocketClientTokenListener {

	private LinearLayout mLinearLayout = null;
	private Canvas lCanvas = null;
	private Paint lPaint = null;
	private String CANVAS_ID = "c1";
	private float lSX = 0, lSY = 0;
	private ImageView lImgView = null;
        int lWidth;
	int lHeight;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Create a LinearLayout in which to add the ImageView
		mLinearLayout = new LinearLayout(this);

		// get the display metric (width and height)
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		lWidth = metrics.widthPixels;
		lHeight = metrics.heightPixels;

		final Bitmap lBmp = Bitmap.createBitmap(lWidth, lHeight, Bitmap.Config.ARGB_8888);
		lCanvas = new Canvas(lBmp);
		// final ImageView lImgView = new ImageView(this);
		lImgView = new ImageView(this);

		lImgView.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		lImgView.setImageBitmap(lBmp);
		lImgView.setScaleType(ImageView.ScaleType.CENTER);
		lImgView.setPadding(0, 0, 0, 0);

		final Paint lBck = new Paint();
		lBck.setARGB(0xff, 0xff, 0xff, 0xff);
		lCanvas.drawRect(0, 0, lWidth, lHeight, lBck);

		lPaint = new Paint();
		lPaint.setARGB(0xff, 0x00, 0x00, 0x00);

		mLinearLayout.addView(lImgView);
		setContentView(mLinearLayout);

		lImgView.setOnTouchListener(new OnTouchListener() {

			// start and end coordinates for a single line
			float lSX, lSY, lEX, lEY;

			public boolean onTouch(View aView, MotionEvent aME) {

				Rect lRect = new Rect();
				Window lWindow = getWindow();
				lWindow.getDecorView().getWindowVisibleDisplayFrame(lRect);
				int lStatusBarHeight = lRect.top;
				int lContentViewTop =
						lWindow.findViewById(Window.ID_ANDROID_CONTENT).getTop();
				final int lTitleBarHeight = lContentViewTop - lStatusBarHeight;

				int lAction = aME.getAction();

				float lX = aME.getX();
				float lY = aME.getY();

				switch (lAction) {
					case MotionEvent.ACTION_DOWN:
						lEX = lX;
						lEY = lY + lTitleBarHeight;
						sendBeginPath(lEX, lEY);
						break;
					case MotionEvent.ACTION_MOVE:
						lSX = lEX;
						lSY = lEY;
						lEX = lX;
						lEY = lY + lTitleBarHeight;
						lCanvas.drawLine(lSX, lSY, lEX, lEY, lPaint);
						// updated by Alex 2010-08-10
						sendLineTo(lEX, lEY);
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_CANCEL:
						sendClosePath();
						break;
				}
				lImgView.invalidate();
				return true;
			}
		});

	}

	// added by Alex: 2010-08-20
	public void sendBeginPath(float ax, float ay) {
		Token lCanvasToken = new Token();

		// use broadcast of system plug-in
		// use namespace and type for server's broadcast "command"
		lCanvasToken.put("ns", "org.jWebSocket.plugins.system");
		lCanvasToken.put("type", "broadcast");

		// pass namespace and type
		// for client's canvas "command"
		lCanvasToken.put("reqNS", "org.jWebSocket.plugins.canvas");
		lCanvasToken.put("reqType", "beginPath");
		lCanvasToken.put("x", ax);
		lCanvasToken.put("y", ay);
		lCanvasToken.put("id", CANVAS_ID);

		try {
			JWC.sendToken(lCanvasToken);
		} catch (WebSocketException e) {
			//TODO: log exception
		}
	}

	public void sendLineTo(float aX, float aY) {
		Token lCanvasToken = new Token();

		// added by Alex: 2010-08-20
		// use broadcast of system plug-in
		lCanvasToken.put("ns", "org.jWebSocket.plugins.system");
		lCanvasToken.put("type", "broadcast");

		lCanvasToken.put("reqNS", "org.jWebSocket.plugins.canvas");
		lCanvasToken.put("reqType", "lineTo");
		lCanvasToken.put("x", aX);
		lCanvasToken.put("y", aY);
		lCanvasToken.put("id", CANVAS_ID);		
		try {
			JWC.sendToken(lCanvasToken);
		} catch (WebSocketException ex) {
			//TODO: log exception
		}
	}

	// added by Alex: 2010-08-20
	public void sendClosePath() {
		Token lCanvasToken = new Token();

		// use broadcast of system plug-in
		// use namespace and type for server's broadcast "command"
		lCanvasToken.put("ns", "org.jWebSocket.plugins.system");
		lCanvasToken.put("type", "broadcast");
		lCanvasToken.put("id", CANVAS_ID);

		// pass namespace and type
		// for client's canvas "command"
		lCanvasToken.put("reqNS", "org.jWebSocket.plugins.canvas");
		lCanvasToken.put("reqType", "closePath");

		try {
			JWC.sendToken(lCanvasToken);
		} catch (WebSocketException e) {
			//TODO: log exception
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu aMenu) {
		MenuInflater lMenInfl = getMenuInflater();
		lMenInfl.inflate(R.menu.canvas_menu, aMenu);
		return true;
	}

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle item selection
            switch (item.getItemId()) {
            case R.id.mniCanvasClear:                
		clearCanvas();
                sendClear();
                return true;
            default:
                return super.onOptionsItemSelected(item);
            }
        }

        private void clearCanvas() {
            final Paint lBck = new Paint();
            lBck.setARGB(0xff, 0xff, 0xff, 0xff);
            lCanvas.drawRect(0, 0, lWidth, lHeight, lBck);
            lImgView.invalidate();
        }

        private void sendClear() {
            Token lCanvasToken = new Token();
            lCanvasToken.put("ns", "org.jWebSocket.plugins.system");
            lCanvasToken.put("type", "broadcast");
            lCanvasToken.put("id", CANVAS_ID);

            // pass namespace and type
            // for client's canvas "command"
            lCanvasToken.put("reqNS", "org.jWebSocket.plugins.canvas");
            lCanvasToken.put("reqType", "clear");

            try {
                    JWC.sendToken(lCanvasToken);
            } catch (WebSocketException e) {
                    //TODO: log exception
            }
        }


	@Override
	protected void onResume() {
		super.onResume();
		try {
			JWC.addListener(this);
			JWC.open();
		} catch (WebSocketException ex) {
			//TODO: log exception
		}
	}

        

	@Override
	protected void onPause() {
		super.onPause();
		try {
			JWC.removeListener(this);
			JWC.close();
		} catch (WebSocketException ex) {
			//TODO: log exception
		}

	}

	public void processToken(WebSocketClientEvent aEvent, Token aToken) {
		// check if incoming token is targetted to canvas (by name space)
		if ("org.jWebSocket.plugins.canvas".equals(aToken.getString("reqNS"))) {
			// check "beginPath" request
			if ("beginPath".equals(aToken.getString("reqType"))) {
				// nothing to do here
			} else if ("moveTo".equals(aToken.getString("reqType"))) {
				// keep start position "in mind"
				lSX = new Float(aToken.getDouble("x", 0.0));
				lSY = new Float(aToken.getDouble("y", 0.0));
				// check "lineTo" request
			} else if ("lineTo".equals(aToken.getString("reqType"))) {
				// TODO: implement multiple canvas, this is what is used for
				// int id = aToken.getInteger("identifier");
				// if (id != getTaskId()) {
				float lEX = new Float(aToken.getDouble("x", 0.0));
				float lEY = new Float(aToken.getDouble("y", 0.0));
				// draw the line
				lCanvas.drawLine(lSX, lSY, lEX, lEY, lPaint);
				// invalidate image view to re-draw the canvas
                                                               
				lImgView.invalidate();

				lSX = lEX;
				lSY = lEY;
				// }
				// check "closePath" request
			} else if ("closePath".equals(aToken.getString("reqType"))) {
				// nothing to do here
			}else if("clear".equals(aToken.getString("reqType"))) {
                            clearCanvas();
                        }
			
		}
	}

	public void processOpened(WebSocketClientEvent aEvent) {
	}

	public void processPacket(WebSocketClientEvent aEvent, WebSocketPacket aPacket) {
	}

	public void processClosed(WebSocketClientEvent aEvent) {
	}
}
