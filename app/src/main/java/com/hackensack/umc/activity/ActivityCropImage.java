package com.hackensack.umc.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.hackensack.umc.R;
import com.hackensack.umc.cropper.CropImageView;
import com.hackensack.umc.util.Base64Converter;
import com.hackensack.umc.util.CameraFunctionality;
import com.hackensack.umc.util.Constant;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.hackensack.umc.util.Base64Converter.decodeUri;

public class ActivityCropImage extends BaseActivity implements CropImageView.OnSetImageUriCompleteListener, CropImageView.OnGetCroppedImageCompleteListener {

    //region: Fields and Consts

    private static final int DEFAULT_ASPECT_RATIO_VALUES = 20;

    private static final String ASPECT_RATIO_X = "ASPECT_RATIO_X";

    private static final String ASPECT_RATIO_Y = "ASPECT_RATIO_Y";

    private static final int ON_TOUCH = 1;

    private CropImageView mCropImageView;

    private int mAspectRatioX = DEFAULT_ASPECT_RATIO_VALUES;

    private int mAspectRatioY = DEFAULT_ASPECT_RATIO_VALUES;
    private CameraFunctionality cameraFunctionality;

    public Bitmap getCroppedImage() {
        return croppedImage;
    }

    public void setCroppedImage(Bitmap croppedImage) {

        this.croppedImage = croppedImage;
    }

    private Bitmap croppedImage;
    private ImageView croppedImageView;
    private Uri uriWithoutCrop;
    private int selsetedImageView;
    OnSelfiCropped onSelfiCropped;
    //endregion

    // Saves the state upon rotating the screen/restarting the activity
    @Override
    public void onSaveInstanceState(@SuppressWarnings("NullableProblems") Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt(ASPECT_RATIO_X, mAspectRatioX);
        bundle.putInt(ASPECT_RATIO_Y, mAspectRatioY);
    }

    // Restores the state upon rotating the screen/restarting the activity
    @Override
    protected void onRestoreInstanceState(@SuppressWarnings("NullableProblems") Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        mAspectRatioX = bundle.getInt(ASPECT_RATIO_X);
        mAspectRatioY = bundle.getInt(ASPECT_RATIO_Y);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_crop_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.crop_action_done:
                new backButtonClicked().execute();

                break;
            default:
                return super.onOptionsItemSelected(item);

        }
        return super.onOptionsItemSelected(item);
    }

    private class backButtonClicked extends AsyncTask<Void, Void, Uri> {
        Uri uri = null;
        String base64FileUrl;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            startProgress(ActivityCropImage.this, getString(R.string.loading_text));
        }

        @Override
        protected Uri doInBackground(Void... params) {
            if (uriWithoutCrop != null) {

                if (croppedImage != null) {

                   //croppedImage=getResizedBitmap(croppedImage,375);
                    croppedImage=CameraFunctionality.getResizedBitmap(croppedImage,200,300);

                   String base64= Base64Converter.createBase64StringFromBitmap(croppedImage, ActivityCropImage.this);
                    base64FileUrl=CameraFunctionality.writeBase64(selsetedImageView, base64);
                    uri = cameraFunctionality.saveCropedImage_1(croppedImage, ActivityCropImage.this, selsetedImageView);

                } else {
                    uri = uriWithoutCrop;
                }
            }
            return uri;
        }
        public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
            int width = image.getWidth();
            int height = image.getHeight();

            float bitmapRatio = (float) width / (float) height;
            if (bitmapRatio > 1) {
                width = maxSize;
                height = (int) (width / bitmapRatio);
            } else {
                height = maxSize;
                width = (int) (height * bitmapRatio);
            }

            return Bitmap.createScaledBitmap(image, width, height, true);
        }
        @Override
        protected void onPostExecute(Uri uri) {
            super.onPostExecute(uri);
            if (uri != null) {
                Intent intent = getIntent();
                //intent.putExtra(Constant.IMAGE_FILE_PATH, intent.getStringExtra(Constant.IMAGE_FILE_PATH));
              intent.putExtra(Constant.BASE64_FILE_PATH,base64FileUrl);
               /* ByteArrayOutputStream blob = new ByteArrayOutputStream();
                croppedImage.compress(Bitmap.CompressFormat.PNG, 0 *//*ignored for PNG*//*, blob);
                byte[] bitmapdata = blob.toByteArray();*/
                intent.putExtra(Constant.CROPPED_IMAGE, croppedImage);
               // intent.putExtra("byteArray",bitmapdata);
                intent.setData(uri);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
            stopProgress();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activity_crop_image);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        inflateXml();
        findViewById(R.id.Button_crop).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCropImageView.getCroppedImageAsync(mCropImageView.getCropShape(), 0, 0);
                // mCropImageView.setVisibility(View.GONE);
            }
        });
        findViewById(R.id.btnUndo).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mCropImageView.setImageUriAsync(uriWithoutCrop);
                mCropImageView.setVisibility(View.VISIBLE);
                croppedImageView.setVisibility(View.GONE);
            }
        });

    }

    private void inflateXml() {
        uriWithoutCrop = getIntent().getData();
      //  cameraFunctionality= (CameraFunctionality) getIntent().getSerializableExtra(Constant.Cropp_InterFace);
        selsetedImageView = getIntent().getIntExtra(Constant.SELECTED_IMAGE_VIEW, 1);
        mCropImageView = (CropImageView) findViewById(R.id.CropImageView);
        mCropImageView.setImageUriAsync(uriWithoutCrop);
        croppedImageView = (ImageView) findViewById(R.id.croppedImageView);

    }

    @Override
    protected void onStart() {
        super.onStart();
        mCropImageView.setOnSetImageUriCompleteListener(this);
        mCropImageView.setOnGetCroppedImageCompleteListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCropImageView.setOnSetImageUriCompleteListener(null);
        mCropImageView.setOnGetCroppedImageCompleteListener(null);
    }

    @Override
    public void onSetImageUriComplete(CropImageView view, Uri uri, Exception error) {
        if (error == null) {
            Toast.makeText(mCropImageView.getContext(), "Image load successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mCropImageView.getContext(), "Image load failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGetCroppedImageComplete(CropImageView view, Bitmap bitmap, Exception error) {
        if (error == null) {
            croppedImage = bitmap;
            croppedImageView.setVisibility(View.VISIBLE);
            mCropImageView.setVisibility(View.GONE);
            setCroppedImage(bitmap);

            croppedImageView.setImageBitmap(croppedImage);
        } else {
            Toast.makeText(mCropImageView.getContext(), "Image crop failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    /**
     * Create a chooser intent to select the source to get image from.<br/>
     * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the intent chooser.
     */
    public Intent getPickImageChooserIntent() {

        // Determine Uri of camera image to save.
        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        // collect all camera intents
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        // collect all gallery intents
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        // the main intent is the last in the list (fucking android) so pickup the useless one
        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        // Create a chooser from the main intent
        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        // Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    /**
     * Get URI to image received from capture by camera.
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), Math.random() + "pickImageResult.png"));
        }
        return outputFileUri;
    }

    /**
     * Get the URI of the selected image from {@link #getPickImageChooserIntent()}.<br/>
     * Will return the correct URI for camera and gallery image.
     *
     * @param data the returned data of the activity result
     */
    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }
    public interface OnSelfiCropped{
        void setSelfiBitmap(Bitmap bitmap);
    }
}

