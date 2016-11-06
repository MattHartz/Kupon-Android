package coop.com.kuponandroid.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import coop.com.kuponandroid.R;
import coop.com.kuponandroid.helpers.BarcodeTrackerFactory;
import coop.com.kuponandroid.helpers.CameraSource;
import coop.com.kuponandroid.helpers.CameraSourcePreview;
import coop.com.kuponandroid.helpers.GraphicOverlay;

public class CameraActivity extends AppCompatActivity {
    private CameraSourcePreview mCameraSourcePreview;
    private GraphicOverlay mGraphicOverlay;
    private CameraSource mCameraSource;
    private ImageButton mCaptureButton;
    private CameraSource.PictureCallback mPictureCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mCameraSourcePreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.barcodeOverlay);
        mCaptureButton = (ImageButton) findViewById(R.id.captureButton);

        BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE).build();
        BarcodeTrackerFactory barcodeFactory = new BarcodeTrackerFactory(mGraphicOverlay);
        detector.setProcessor(new MultiProcessor.Builder<>(barcodeFactory).build());

        mCameraSource = new CameraSource.Builder(getApplicationContext(), detector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .build();

        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraSource.takePicture(new CameraSource.ShutterCallback() {
                    @Override
                    public void onShutter() {

                    }
                }, new CameraSource.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data) {
                        Intent intent = new Intent();
                        intent.putExtra("image", data);
                        setResult(RESULT_OK, intent);
                        mCameraSource.stop();
                        finish();
                    }
                });
            }
        });

        startCameraSource();
    }

    //starting the preview
    private void startCameraSource() {
        try {
            mCameraSourcePreview.start(mCameraSource, mGraphicOverlay);
        } catch (IOException e) {
            mCameraSource.release();
            mCameraSource = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource(); //start
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraSourcePreview.stop(); //stop
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraSource.release(); //release the resources
    }

    @Override
    public void onBackPressed() {
        //Include the code here
        super.onBackPressed();

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }
}
