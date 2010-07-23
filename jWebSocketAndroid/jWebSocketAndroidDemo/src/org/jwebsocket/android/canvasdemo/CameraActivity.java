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
import android.hardware.Camera;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import java.io.IOException;

/**
 *
 * @author aschulze
 */
public class CameraActivity extends Activity implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private boolean mPreviewRunning = false;
    Camera.PictureCallback mPictureCallback;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.camera_hvga_p);

        mSurfaceView = (SurfaceView) findViewById(R.id.sfvCamera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mPictureCallback = new Camera.PictureCallback() {

            public void onPictureTaken(byte[] imageData, Camera c) {
                Toast.makeText(getApplicationContext(), "Photo has been taken!",
                        Toast.LENGTH_SHORT).show();
            }
        };

        mSurfaceView.setOnClickListener(new OnClickListener() {

            public void onClick(View aView) {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {

                    public void onAutoFocus(boolean arg0, Camera arg1) {
                        mCamera.takePicture(null, null, mPictureCallback);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                        }
                        mCamera.startPreview();
                        mPreviewRunning = true;
                    }
                });

            }
        });
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        if (mPreviewRunning) {
            mCamera.stopPreview();
        }
        Camera.Parameters p = mCamera.getParameters();
        p.setPreviewSize(w, h);
        mCamera.setParameters(p);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        mPreviewRunning = true;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mPreviewRunning = false;
        mCamera.release();

    }
}
