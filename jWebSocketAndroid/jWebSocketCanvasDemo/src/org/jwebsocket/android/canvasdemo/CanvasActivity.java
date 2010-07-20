/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jwebsocket.android.canvasdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 *
 * @author aschulze
 */
public class CanvasActivity extends Activity {

    LinearLayout mLinearLayout;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Create a LinearLayout in which to add the ImageView
        mLinearLayout = new LinearLayout(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        final int width = metrics.widthPixels;
        final int height = metrics.heightPixels;

        final Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas(b);
        final ImageView i = new ImageView(this);
        // i.setAdjustViewBounds(true); // set the ImageView bounds to match the Drawable's dimensions
        i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        i.setImageBitmap(b);
        i.setScaleType(ImageView.ScaleType.CENTER);
        i.setPadding(0, 0, 0, 0);

        final Paint bck = new Paint();
        bck.setARGB(0xff, 0x80, 0x80, 0x80);
        c.drawRect(0, 0, width, height, bck);

        final Paint p = new Paint();
        p.setARGB(0xff, 0xff, 0xff, 0xff);
        c.drawText("Hello World!", 10, 10, p);

        mLinearLayout.addView(i);
        setContentView(mLinearLayout);



        i.setOnTouchListener(new OnTouchListener() {

            float sx, sy, ex, ey;

            public boolean onTouch(View aView, MotionEvent aME) {

                Rect rect = new Rect();
                Window window = getWindow();
                window.getDecorView().getWindowVisibleDisplayFrame(rect);
                int statusBarHeight = rect.top;
                int contentViewTop =
                        window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
                final int titleBarHeight = contentViewTop - statusBarHeight;

                int lAction = aME.getAction();
                float lX = aME.getX();
                float lY = aME.getY();
                float lOfsX = 0;
                float lOfsY = 20;
                c.drawRect(0, 0, width, 100, bck);
                c.drawText("getX/Y:" + aME.getX() + "/" + aME.getY(), 0, 10, p);
                c.drawText("getRawX/Y:" + aME.getRawX() + "/" + aME.getRawY(), 0, 25, p);
                c.drawText("getPrecisionX/Y:" + aME.getXPrecision() + "/" + aME.getXPrecision(), 0, 40, p);
                c.drawText("getTop/Left:" + i.getTop() + "/" + i.getLeft(), 0, 55, p);
                switch (lAction) {
                    case MotionEvent.ACTION_DOWN:
                        ex = lX + lOfsX;
                        ey = lY + titleBarHeight;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        sx = ex;
                        sy = ey;
                        ex = lX + lOfsX;
                        ey = lY + titleBarHeight;
                        c.drawLine(sx, sy, ex, ey, p);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        break;
                }
                i.invalidate();
                //Matrix m = c.getMatrix();
                //m.setScale(1.0f, 1.0f);
                return true;
            }
        });

    }
}
