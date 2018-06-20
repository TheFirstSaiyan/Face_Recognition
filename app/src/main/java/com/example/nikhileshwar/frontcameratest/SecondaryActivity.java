//package com.example.nikhileshwar.frontcameratest;
//
//import android.app.Activity;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Matrix;
//import android.graphics.drawable.Drawable;
//import android.net.Uri;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.os.Environment;
//import android.util.Base64;
//import android.util.Log;
//import android.widget.ImageView;
//
//import com.kairos.Kairos;
//import com.kairos.KairosListener;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.UnsupportedEncodingException;
//
//
//public class SecondaryActivity extends Activity {
//    Kairos kairos=new Kairos();
//    String app_id="YOUR APP_ID FROM KAIROS";
//    String api_key="YOUR APP KEY FROM KAIROS";
//    int []images= {R.drawable.h1,R.drawable.h2};
//    ImageView imag;
//    private class Enroll extends AsyncTask<Object, Object, Void>
//    {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//            KairosListener listener = new KairosListener() {
//
//                @Override
//                public void onSuccess(String response) {
//                    JSONObject json = null;
//                    try {
//                        json = new JSONObject(response);
//                        JSONArray jArray = json.getJSONArray("images");
//                        JSONObject jsonObj1=jArray.getJSONObject(0);
//                        if(jsonObj1.has("candidates")) {
//                            JSONArray mJsonArrayProperty = jsonObj1.getJSONArray("candidates");
//                            //if (mJsonArrayProperty.length() > 0)
//                            for (int i = 0; i < mJsonArrayProperty.length(); i++) {
//                                JSONObject mJsonObjectProperty = mJsonArrayProperty.getJSONObject(i);
//                                  if(mJsonObjectProperty.getDouble("confidence")>0.7) {
//
//                                      //link to your activities here EAUGENE!
//
//
//                                      Log.i("KAIROS", String.valueOf(mJsonObjectProperty.getDouble("confidence")));
//                                      break;
//                                  }
//                            }
//                        }
////                            for(int i=0; i<jArray.length(); i++) {
////
////                            JSONObject json_data = jArray.getJSONObject(i).getJSONObject("transaction");
////
////                            if(json_data.getDouble("confidence")>0.9)
////                                Log.i("KAIROS", json_data.getString("confidence"));
////
////                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//
//
//                    Log.i("KAIROS DEMO", response);
//                }
//
//                @Override
//                public void onFail(String response) {
//
//                    Log.i("KAIROS DEMO", response);
//                }
//            };
//            int w=0,h=0;
//            Matrix matrix=new Matrix();
//                matrix.postRotate(270);
//                ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.h1);
//                h=bitmap.getHeight();
//                w=bitmap.getWidth();
////            for (int i = 0; i < 2; i++) {
////                Matrix matrix=new Matrix();
////                matrix.postRotate(270);
////                ByteArrayOutputStream baos = new ByteArrayOutputStream();
////                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),images[i]);
////                h=bitmap.getHeight();
////                w=bitmap.getWidth();
////                Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap, w,h, false);
////                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapResized , 0, 0, bitmapResized .getWidth(), bitmapResized .getHeight(), matrix,true);
////                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
////                byte[] imageBytes = baos.toByteArray();
////                String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
////                String subjectId = "hari";
////                String galleryId = "users";
////                String selector = "FULL";
////                String multipleFaces = "false";
////                String minHeadScale = "0.25";
////                try {
////
////                    kairos.enroll(imageString,
////                            subjectId,
////                            galleryId,
////                            selector,
////                            multipleFaces,
////                            minHeadScale,listener);
////                } catch (JSONException e) {
////                    e.printStackTrace();
////                } catch (UnsupportedEncodingException e) {
////                    e.printStackTrace();
////                }
////
////
////            }
//            String path=getIntent().getExtras().getString("path",null);
//            Log.i("KAIROS",path);
//            //File sd = Environment.getExternalStorageDirectory();
//           // img.setImageDrawable(Drawable.createFromPath(path));
//            //File image = new File(sd, "pic.jpg");
//            //BitmapFactory.Options bmOptions = new BitmapFactory.Options();
//            Bitmap bitmap1 = BitmapFactory.decodeFile(path);
//          //
////                Matrix matrix1 = new Matrix();
////
////                matrix1.postRotate(270);
//                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
//                // Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.test);
//                Bitmap bitmapResized = Bitmap.createScaledBitmap(bitmap1, w, h, false);
//                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmapResized, 0, 0, bitmapResized.getWidth(), bitmapResized.getHeight(), matrix, true);
//                imag.setImageBitmap(rotatedBitmap);
//                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos1);
//                byte[] imageBytes = baos1.toByteArray();
//                String imageString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
//                String galleryId = "users";
//                String selector = "FULL";
//                String threshold = "0.75";
//                String minHeadScale = "0.25";
//                String maxNumResults = "25";
//                try {
//                    kairos.recognize(imageString,
//                            galleryId,
//                            selector,
//                            threshold,
//                            minHeadScale,
//                            maxNumResults,
//                            listener);
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                } catch (UnsupportedEncodingException e) {
//                    e.printStackTrace();
//                }
//
//            return;
//        }
//
//        @Override
//        protected Void doInBackground(Object... params) {
//            return null;
//        }
//    }
//    @Override
//    protected void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        kairos.setAuthentication(this, app_id, api_key);
//        imag=(ImageView)findViewById(R.id.imageView);
//        new Enroll().execute();
//       // Log.i("hello", kairos.toString());
//    }
//    }
