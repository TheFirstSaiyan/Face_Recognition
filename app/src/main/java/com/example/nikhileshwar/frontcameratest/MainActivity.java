package com.example.nikhileshwar.frontcameratest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.kairos.Kairos;
import com.kairos.KairosListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;



import java.util.List;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private Button takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    int i;
    SharedPreferences sharedPreferences;
    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    ImageView imgview;
    int []images= {R.drawable.h1,R.drawable.h2};
    String app_id="b456c74a";
    String api_key="96aa677d5ad75efaf599bec35ec1ec6a";
    Kairos kairos=new Kairos();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        kairos.setAuthentication(getApplicationContext(), app_id, api_key);
//i=0;
sharedPreferences=this.getSharedPreferences("com.example.nikhileshwar.frontcameratest",Context.MODE_PRIVATE);
        i=sharedPreferences.getInt("tries",0);
i=0;
Log.i("KAIROS",String.valueOf(i));
        textureView = (TextureView) findViewById(R.id.texture);
        images=new int[12];

        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        takePictureButton = (Button) findViewById(R.id.btn_takepicture);
        if(i<2) {
            takePictureButton.setText("scan");
            Toast.makeText(this,"scan ("+String.valueOf(2-i)+") times remaining",Toast.LENGTH_LONG).show();

        }
        else
            takePictureButton.setText("identify face");

        assert takePictureButton != null;
        //takePicture();
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
                Log.i("hello","hello");


            }
        });


    }


    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.i(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }
        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
//    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
//        @Override
//        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//            super.onCaptureCompleted(session, request, result);
//
//            Toast.makeText(MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
//            createCameraPreview();
//        }
//    };
    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }
    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    protected void takePicture() {
        if(null == cameraDevice) {
            Log.i(TAG, "cameraDevice is null");
            return;
        }
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                Log.i("wid",Integer.toString(width));
                height = jpegSizes[0].getHeight();
            }
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            final File file = new File(Environment.getExternalStorageDirectory()+"/pic.jpg");


            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
Log.i("wid",Integer.toString(image.getWidth()));
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];

                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }
                private void enroll(String path)
                {
                    KairosListener listener = new KairosListener() {

                        @Override
                        public void onSuccess(String response) {
                            JSONObject json = null;
                            try {
                                json = new JSONObject(response);
                                JSONArray jArray = json.getJSONArray("images");
                                JSONObject jsonObj1=jArray.getJSONObject(0);
                                if(jsonObj1.has("candidates")) {
                                    JSONArray mJsonArrayProperty = jsonObj1.getJSONArray("candidates");
                                    //if (mJsonArrayProperty.length() > 0)
                                    for (int i = 0; i < mJsonArrayProperty.length(); i++) {
                                        JSONObject mJsonObjectProperty = mJsonArrayProperty.getJSONObject(i);
                                        if(mJsonObjectProperty.getDouble("confidence")>0.7) {

                                            //link to your activities here EAUGENE!!!!!
                                            //permission granted!!!

                                            Log.i("confidence :", String.valueOf(mJsonObjectProperty.getDouble("confidence")));
                                            break;
                                        }
                                        else
                                        {
                                            //Do What u want  to do when permission denied!!!!
                                        }
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            Log.i("KAIROS DEMO", response);
                        }

                        @Override
                        public void onFail(String response) {

                            Log.i("KAIROS DEMO", response);
                        }
                    };
                    int w=0,h=0;
                    Matrix matrix=new Matrix();
                    matrix.postRotate(270);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.h1);
                    h=bitmap.getHeight();
                    w=bitmap.getWidth();
                    Log.i("KAIROS",path);
                    Bitmap bitmap1 = BitmapFactory.decodeFile(path);
                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                    Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap1, w, h, false);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapResized, 0, 0, bitmapResized.getWidth(), bitmapResized.getHeight(), matrix, true);
                    //imag.setImageBitmap(rotatedBitmap);
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos1);
                    byte[] imageBytes = baos1.toByteArray();
                    String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    String subjectId = "hari";
                String galleryId = "users";
                String selector = "FULL";
                String multipleFaces = "false";
                String minHeadScale = "0.25";
                                    try {

                    kairos.enroll(imageString,
                            subjectId,
                            galleryId,
                            selector,
                            multipleFaces,
                            minHeadScale,listener);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                }
                private void recognize(String path)
                {
//                    String app_id="b456c74a";
//                    String api_key="96aa677d5ad75efaf599bec35ec1ec6a";
                    KairosListener listener = new KairosListener() {

                        @Override
                        public void onSuccess(String response) {
                            JSONObject json = null;
                            try {
                                json = new JSONObject(response);
                                JSONArray jArray = json.getJSONArray("images");
                                JSONObject jsonObj1=jArray.getJSONObject(0);
                                if(jsonObj1.has("candidates")) {
                                    JSONArray mJsonArrayProperty = jsonObj1.getJSONArray("candidates");
                                    //if (mJsonArrayProperty.length() > 0)
                                    for (int i = 0; i < mJsonArrayProperty.length(); i++) {
                                        JSONObject mJsonObjectProperty = mJsonArrayProperty.getJSONObject(i);
                                        if(mJsonObjectProperty.getDouble("confidence")>0.7) {

                                          
                                            //permission granted!!!

                                            Log.i("confidence :", String.valueOf(mJsonObjectProperty.getDouble("confidence")));
                                            break;
                                        }
                                        else
                                        {
                                            // permission denied!!!!
                                          
                                        }
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                            Log.i("KAIROS DEMO", response);
                        }

                        @Override
                        public void onFail(String response) {

                            Log.i("KAIROS DEMO", response);
                        }
                    };
                    int w=0,h=0;
                    Matrix matrix=new Matrix();
                    matrix.postRotate(270);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.h1);
                    h=bitmap.getHeight();
                    w=bitmap.getWidth();
                    Log.i("KAIROS",path);

//                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),images[i]);

                    Bitmap bitmap1 = BitmapFactory.decodeFile(path);
                    ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                    Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap1, w, h, false);
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapResized, 0, 0, bitmapResized.getWidth(), bitmapResized.getHeight(), matrix, true);
                    //imag.setImageBitmap(rotatedBitmap);
                    rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos1);
                    byte[] imageBytes = baos1.toByteArray();
                    String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    String galleryId = "users";
                    String selector = "FULL";
                    String threshold = "0.75";
                    String minHeadScale = "0.25";
                    String maxNumResults = "25";
                    try {
                        kairos.recognize(imageString,
                                galleryId,
                                selector,
                                threshold,
                                minHeadScale,
                                maxNumResults,
                                listener);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {

                            output.close();

                            if(i>=2)
                            recognize(Environment.getExternalStorageDirectory()+"/pic.jpg");
                            else {
                                i++;

                                sharedPreferences.edit().putInt("tries",i).apply();

                                Log.i("SP",String.valueOf(sharedPreferences.getInt("tries",0)));
                                enroll(Environment.getExternalStorageDirectory() + "/pic.jpg");


                            }

                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    //Toast.makeText(MainActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();

                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            Log.i("widd", String.valueOf(imageDimension.getWidth()));
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback(){
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this,"Configuration change",Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.i(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[1];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "openCamera X");
    }
    protected void updatePreview() {
        if(null == cameraDevice) {
            Log.i(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(MainActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }
    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        //closeCamera();
        stopBackgroundThread();
        super.onPause();
    }


}
