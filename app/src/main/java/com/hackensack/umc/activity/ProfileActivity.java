package com.hackensack.umc.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hackensack.umc.R;
import com.hackensack.umc.datastructure.DataForAutoRegistration;
import com.hackensack.umc.datastructure.DocumentPojo;
import com.hackensack.umc.datastructure.Documents;
import com.hackensack.umc.http.CommonAPICalls;
import com.hackensack.umc.http.HttpUtils;
import com.hackensack.umc.http.ServerConstants;
import com.hackensack.umc.response.CardResponse;
import com.hackensack.umc.response.DatamotionTokenCredential;
import com.hackensack.umc.response.MessageRespponse;
import com.hackensack.umc.util.Base64Converter;
import com.hackensack.umc.util.CameraFunctionality;
import com.hackensack.umc.util.Constant;
import com.hackensack.umc.util.Util;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.hackensack.umc.util.Base64Converter.decodeUri;

public class ProfileActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int RESULT_GALLERY = 0;
    private static final int IMAGE_VIEW_1 = 10;
    private static boolean isDialogAlertShown;
    private ImageView mImageView;
    private String mCurrentPhotoPath;
    private Uri imageUri;
    private String pathDlFront = null;
    private String pathDlBack = null;
    private String pathIcFront = null;
    private String pathIcBack = null;
    private String pathSelfie = null;
    private String TAG = "ProfileActivity";
    private int selectedImageView;
    private String token;
    private SharedPreferences sharedPreferences;
    private Uri uriDlFront = null;
    private Uri uriDlback = null;
    private Uri uriIcFront = null;
    private Uri uriIcBack = null;
    private Uri uriSelfie = null;
    private ImageView imgIcFront;
    private ImageView imgSelfie;
    private ImageView imgIcBack;
    private ImageView imgIdFront;
    private ImageView imgIdBack;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_profile);

        if(savedInstanceState==null){
            showAlert(ProfileActivity.this, getString(R.string.msg_easy_data), "Easy Data Entry");
        }else{
        }

        imgIdFront = (ImageView) findViewById(R.id.dl_front);
        imgIdBack = (ImageView) findViewById(R.id.dl_back);
        imgIcFront = (ImageView) findViewById(R.id.ic_front);
        imgIcBack = (ImageView) findViewById(R.id.ic_back);
        imgSelfie = (ImageView) findViewById(R.id.selfie_img);

        imgIdFront.setOnClickListener(this);
        imgIdBack.setOnClickListener(this);
        imgIcFront.setOnClickListener(this);
        imgIcBack.setOnClickListener(this);
        imgSelfie.setOnClickListener(this);


        ((Button) findViewById(R.id.proceed_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // startActivity(new Intent(ProfileActivity.this, RegistrationDetailsActivity.class));

                // startRegistrationActivity();
                if (checkValidationsBeforeCall()) {
                    if (Util.ifNetworkAvailableShowProgressDialog(ProfileActivity.this, getString(R.string.scanning_images), true)) {
                        new GetAccessTokenCallDataMotion().execute();
                    }
                }
            }
        });
        ((Button) findViewById(R.id.manual_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // startActivity(new Intent(ProfileActivity.this, RegistrationDetailsActivity.class));

                Intent intent = new Intent(ProfileActivity.this, RegistrationDetailsActivity.class);
                Bundle b = new Bundle();
                b.putInt(Constant.REGISTRATION_MODE, Constant.MANUAL);
                intent.putExtra(Constant.BUNDLE, b);

                startActivity(intent);

            }
        });
        initialisevariables();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(TAG,"onRestoreInstanceState");
        if(savedInstanceState==null){
            showAlert(ProfileActivity.this, getString(R.string.msg_easy_data), "Easy Data Entry");
        }else {
            setValuesFromSavedInstanceState(savedInstanceState);
        }
    }

    private void setValuesFromSavedInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (!savedInstanceState.getBoolean(Constant.PROFILE_DIALOG)) {
                showAlert(ProfileActivity.this, getString(R.string.msg_easy_data), "Easy Data Entry");
            }
            String imgUrlFront = savedInstanceState.getString(Constant.KEY_ID_FRONT);
            if (imgUrlFront != null) {
                setPhotoToImageView(imgUrlFront,imgIdFront);
            }
            String imgUrlBack = savedInstanceState.getString(Constant.KEY_ID_BACK);
            if (imgUrlBack != null) {
                setPhotoToImageView(imgUrlBack,imgIdBack);
            }
            String imgUrlIcFront = savedInstanceState.getString(Constant.KEY_INSURANCE_FRONT);
            if (imgUrlIcFront != null) {
                setPhotoToImageView(imgUrlIcFront,imgIcFront);
            }
            String imgUrlIcBack = savedInstanceState.getString(Constant.KEY_INSURANCE_BACK);
            if (imgUrlIcBack != null) {
                setPhotoToImageView(imgUrlIcBack,imgIcBack);
            }
            String imgUrlSelfie = savedInstanceState.getString(Constant.KEY_INSURANCE_BACK);
            if (imgUrlSelfie != null) {
                setPhotoToImageView(imgUrlSelfie,imgSelfie);
            }
        }
    }

    private void startRegistrationActivity(DataForAutoRegistration dataTosend) {
        Intent intent = new Intent(ProfileActivity.this, RegistrationDetailsActivity.class);
        Bundle b = new Bundle();
        b.putSerializable(Constant.REG_REQUIRED_DATA, dataTosend);
        b.putInt(Constant.REGISTRATION_MODE, Constant.AUTO);
        b = putUrlValuesInBundle(b);
        intent.putExtra(Constant.BUNDLE, b);
        startActivity(intent);
    }

    private Bundle putUrlValuesInBundle(Bundle b) {
        if (uriSelfie != null) {
            b.putString(Constant.KEY_SLFIE, uriSelfie.toString());
        }
        if (uriDlFront != null) {
            b.putString(Constant.KEY_ID_FRONT, uriDlFront.toString());
        }
        if (uriDlback != null) {
            b.putString(Constant.KEY_ID_BACK, uriDlback.toString());
        }
        if (uriIcFront != null) {
            b.putString(Constant.KEY_INSURANCE_FRONT, uriIcFront.toString());
        }
        if (uriIcBack != null) {
            b.putString(Constant.KEY_INSURANCE_BACK, uriIcBack.toString());
        }
        return b;
    }

    private void initialisevariables() {
        sharedPreferences = getSharedPreferences(Constant.SHAREPREF_TAG, MODE_PRIVATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                isBackPressed = true;
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }

    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    try {
                        if (data != null) {
                            imageUri = data.getData();
                            setImageToImageView(imageUri);

                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
                break;
            case RESULT_GALLERY:
                if (resultCode == RESULT_OK) {
                    try {
                        if (data != null) {
                            imageUri = data.getData();
                            Log.v(TAG, "imageUri::" + imageUri);
                            setImageToImageView(imageUri);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }

    }

    private void setImageToImageView(Uri imageUri) {
        switch (selectedImageView) {
            case 1:
                pathDlFront = Base64Converter.createBase64StringFroImage(imageUri.toString(), ProfileActivity.this);
                uriDlFront = imageUri;
                CameraFunctionality.storeImagesInFolder(uriDlFront.toString(), ProfileActivity.this);
                //  PhotoList.DlFront=pathDlFront;
                break;
            case 2:
                pathDlBack = Base64Converter.createBase64StringFroImage(imageUri.toString(), ProfileActivity.this);
                uriDlback = imageUri;
                break;
            case 3:
                pathIcFront = Base64Converter.createBase64StringFroImage(imageUri.toString(), ProfileActivity.this);
                uriIcFront = imageUri;
                break;
            case 4:
                pathIcBack = Base64Converter.createBase64StringFroImage(imageUri.toString(), ProfileActivity.this);
                uriIcBack = imageUri;
                break;
            case 5:
                pathSelfie = Base64Converter.createBase64StringFroImage(imageUri.toString(), ProfileActivity.this);
                uriSelfie = imageUri;
                break;
        }

        setPhotoToImageView(imageUri.toString(),mImageView);


    }

    private void setPhotoToImageView(String urlString,ImageView mImageView) {
        try {
            Bitmap bitmap = decodeUri(urlString, ProfileActivity.this);
            bitmap = CameraFunctionality.checkImageOrientation(bitmap, urlString.toString(), ProfileActivity.this);
            mImageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void dispatchTakePictureIntent(int cameraFacing) {

        //CameraFunctionality.dispatchTakePictureIntent(cameraFacing, ProfileActivity.this);


    }

    private String dispatchGalleryPictureIntent() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_GALLERY);
        return mCurrentPhotoPath;
    }


    private File createImageFile() throws IOException {
        File image = null;
        File Folder = new File(Environment.getExternalStorageDirectory() + "/.mydir");
        if (Folder.mkdir()) {


            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "/.mydir" + "JPEG_" + timeStamp + "_";
            File storageDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);
            image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );

            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        }
        return image;
    }

/*
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dl_front:
                mImageView = (ImageView) findViewById(R.id.dl_front);
                //dispatchGalleryPictureIntent();
                selectedImageView = 1;
                dispatchTakePictureIntent(0);
                // pathDlFront=imageUri.toString();
                // Log.v(TAG,pathDlFront);
                break;
            case R.id.dl_back:
                mImageView = (ImageView) findViewById(R.id.dl_back);
              *//* pathDlBack=dispatchGalleryPictureIntent(0);
                Log.v(TAG,pathDlBack);*//*
                // dispatchGalleryPictureIntent();
                dispatchTakePictureIntent(0);
                selectedImageView = 2;
                break;
            case R.id.ic_front:
                mImageView = (ImageView) findViewById(R.id.ic_front);
                dispatchTakePictureIntent(0);

                //  dispatchGalleryPictureIntent();
                selectedImageView = 3;
                break;
            case R.id.ic_back:
                mImageView = (ImageView) findViewById(R.id.ic_back);
                dispatchTakePictureIntent(0);
             *//*  pathIcBack = dispatchTakePictureIntent(0);
                Log.v(TAG, pathIcBack);*//*
                //dispatchGalleryPictureIntent();
                selectedImageView = 4;
                break;
            case R.id.selfie_img:
                mImageView = (ImageView) findViewById(R.id.selfie_img);
                dispatchTakePictureIntent(1);
              *//*  pathSelfie = dispatchTakePictureIntent(1);

                Log.v(TAG, pathSelfie);*//*
                //  dispatchGalleryPictureIntent();
                selectedImageView = 5;
                break;
        }
    }*/
///For Gallery///
@Override
public void onClick(View v) {
    switch (v.getId()) {
        case R.id.dl_front:
            mImageView = (ImageView) findViewById(R.id.dl_front);
            dispatchGalleryPictureIntent();
            selectedImageView = 1;
          //  dispatchTakePictureIntent(0);
            // pathDlFront=imageUri.toString();
            // Log.v(TAG,pathDlFront);
            break;
        case R.id.dl_back:
            mImageView = (ImageView) findViewById(R.id.dl_back);
              //  pathDlBack=dispatchGalleryPictureIntent(0);
             //   Log.v(TAG,pathDlBack);
             dispatchGalleryPictureIntent();
            //dispatchTakePictureIntent(0);
            selectedImageView = 2;
            break;
        case R.id.ic_front:
            mImageView = (ImageView) findViewById(R.id.ic_front);
           // dispatchTakePictureIntent(0);

             dispatchGalleryPictureIntent();
            selectedImageView = 3;
            break;
        case R.id.ic_back:
            mImageView = (ImageView) findViewById(R.id.ic_back);
            //dispatchTakePictureIntent(0);
               // pathIcBack = dispatchTakePictureIntent(0);
              //  Log.v(TAG, pathIcBack);
            dispatchGalleryPictureIntent();
            selectedImageView = 4;
            break;
        case R.id.selfie_img:
            mImageView = (ImageView) findViewById(R.id.selfie_img);
           // dispatchTakePictureIntent(1);
                //pathSelfie = dispatchTakePictureIntent(1);

               // Log.v(TAG, pathSelfie);
              dispatchGalleryPictureIntent();
            selectedImageView = 5;
            break;
    }
}
////
    ///////////////////////////create JsonToSend/////


    private void saveCardResponse(ArrayList<CardResponse> cardResponse1) {

        try {

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(Constant.CARD_RESPONSE_FILE_PATH))); //Select where you wish to save the file...

            oos.writeObject(cardResponse1);

            oos.flush();

            oos.close();

        } catch (Exception ex) {

            Log.v("Address Book", ex.getMessage());

            ex.printStackTrace();

        }

    }


    private String getPath(Uri uri) {
        String[] data = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(ProfileActivity.this, uri, data, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private List<NameValuePair> createTokenCallHeaders() {
        List<NameValuePair> headers = new ArrayList<NameValuePair>();
        headers.add(new BasicNameValuePair("Content-Type", "application/x-www-form-urlencoded"));//header can be append here also
        return headers;
    }


    private class GetAccessTokenCallDataMotion extends AsyncTask<Void, Void, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.v(TAG, "GetAccessTokenCallDataMotion::");
            //  startProgress(getString(R.string.scanning_images));
        }

        @Override
        protected String doInBackground(Void... params) {
            String response = HttpUtils.getHttpGetResponse(ProfileActivity.this, ServerConstants.GET_CREDENTIALS_FOR_ACCESS_TOKEN, new CommonAPICalls(ProfileActivity.this).createTokenForDatamotionCall());
            Log.v("DatamotionCallResponse:", response);

            DatamotionTokenCredential datamotionTokenCredential = null;
            Gson gson = new Gson();
            try {
                datamotionTokenCredential = gson.fromJson(response, DatamotionTokenCredential.class);
            } catch (Exception e) {

            }
            if(datamotionTokenCredential!=null){
                return new CommonAPICalls(ProfileActivity.this).sendTokencallForDataMotion(datamotionTokenCredential);
            }
            return response;
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            //  dismissProgressDialog();
            if (aVoid != null) {
                if (checkValidationsBeforeCall()) {
                    if (Util.ifNetworkAvailableShowProgressDialog(ProfileActivity.this, (getString(R.string.scanning_images)), true)) {
                        new sendCardImages().execute();
                    }

                }
            } else {
                stopProgress();
                Util.showAlert(ProfileActivity.this, "Server error.Please try again.", "Alert");
            }


        }

    }
/*
    private String sendTokencallForDataMotion() {
        *//*HttpDownLoadManager httpDownLoadManager=new HttpDownLoadManager(Regis
        trationDetailsActivity.this,this);
        httpDownLoadManager.pos*//*
        String response=null;
        try {
            response = HttpUtils.httpPost(ProfileActivity.this, ServerConstants.URL_TOKEN, createDataMotionTokenCallParameters(), createTokenCallHeaders());
            Log.v(TAG, "Response Is::" + response);
           response=parseJson(response);
        } catch (Exception e) {
            return response;
        }
        return response;


    }

    private List<NameValuePair> createDataMotionTokenCallParameters() {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair(ServerConstants.SERVER_PARA_GRANT_TYPE, ServerConstants.SERVER_PARA_PASSWORD));
        nameValuePairs.add(new BasicNameValuePair(ServerConstants.SERVER_PARA_USER_NAME, "gaurav_salunkhe@persistent.com"));
        nameValuePairs.add(new BasicNameValuePair(ServerConstants.SERVER_PARA_PASSWORD, "c2b82e$af2dc85c1Caa025A3b74"));
        return nameValuePairs;

    }

    private String parseJson(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            token = jsonObject.getString("access_token");
            sharedPreferences.edit().putString(ServerConstants.ACCESS_TOKEN_DATA_MOTION, "bearer " + token).commit();

            Log.v(TAG, "***?////////////////////////////////TOKEN" + token);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return token;
    }*/

    private JSONObject createCardJson() {
        JSONObject cardJson = null;
        Gson gson = new Gson();

        ArrayList<Documents> documentList = new ArrayList<>();
        if (pathDlFront != null && pathDlBack != null) {
            documentList.add(new Documents(0, pathDlFront, pathDlBack));
        }
        if (pathIcFront != null && pathIcBack != null) {
            documentList.add(new Documents(1, pathIcFront, pathIcBack));
        }

        //documentList.add(new Documents(0, pathSelfie, pathSelfie));

        DocumentPojo documentPojo = new DocumentPojo(documentList);

        String jsonCard = gson.toJson(documentPojo);
        try {
            cardJson = new JSONObject(jsonCard);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("JSON IS::", cardJson.toString());
        return cardJson;

    }

    private List<NameValuePair> createHeaders() {
        ArrayList<NameValuePair> headers = new ArrayList<>();
        headers.add(new BasicNameValuePair("Content-Type", "application/json"));
        Log.v("Token is::", "Bearer " + sharedPreferences.getString(ServerConstants.ACCESS_TOKEN_DATA_MOTION, ""));
        headers.add(new BasicNameValuePair("Authorization", getSharedPreferences(Constant.SHAREPREF_TAG, MODE_PRIVATE).getString(ServerConstants.ACCESS_TOKEN_DATA_MOTION, "")));
        return headers;

    }

    private class sendCardImages extends AsyncTask<Void, Void, DataForAutoRegistration> {
        MessageRespponse messageRespponse;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected DataForAutoRegistration doInBackground(Void... params) {
            String cardResponse = HttpUtils.sendHttpPostForUsingJsonObj(ServerConstants.URL_SEND_CARD_IMAGES, createCardJson(), createHeaders());
            ArrayList<CardResponse> cardResponseList = parseCarddata(cardResponse);
            if (cardResponseList != null) {

                DataForAutoRegistration dataForAutoRegistration = getRequiredFileds(cardResponseList);
                Log.v(TAG, "dataForAutoRegistration" + dataForAutoRegistration);
                return dataForAutoRegistration;
            } else {
                try {
                    messageRespponse = new Gson().fromJson(cardResponse, MessageRespponse.class);
                } catch (Exception e) {

                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(DataForAutoRegistration response) {
            super.onPostExecute(response);
            stopProgress();
            if (response != null) {
                startRegistrationActivity(response);
            } else {
                if (messageRespponse != null) {
                    showDilaog(messageRespponse.getMessage());
                }
            }


        }

    }


    private DataForAutoRegistration getRequiredFileds(ArrayList<CardResponse> cardResponseList) {
        DataForAutoRegistration dataForAutoRegistration = new DataForAutoRegistration();

        /*private String fName;
        private String lastName;
        private String email;//not in response
        private String birthDate;
        private String gender;
        private String licennse;
        private String cellNumber;//no
        private String workPhoneNumber;//no
        private String homePhoneNumber;//no
        private String planProvider;
        private String memberId;
        private String grpName;
        private String grpNumber;
        private String coverage;
        private String planType;
        private String street1;*/
        if (cardResponseList != null) {
            for (int i = 0; i < cardResponseList.size(); i++) {
                String firstName = cardResponseList.get(i).getFirstName();
                String lastName = cardResponseList.get(i).getLastName();
                String birthDate = cardResponseList.get(i).getDateOfBirth();
                String licennse = cardResponseList.get(i).getLicense();
                String planProvider = cardResponseList.get(i).getPlanProvider();
                String gender = cardResponseList.get(i).getSex();
                String memberId = cardResponseList.get(i).getMemberID();
                String grpName = cardResponseList.get(i).getGroupName();
                String grpNumber = cardResponseList.get(i).getGroupNumber();
                String coverage = cardResponseList.get(i).getCoverage();
                String planType = cardResponseList.get(i).getPlanType();
                String city = cardResponseList.get(i).getCity();
                String state = cardResponseList.get(i).getState();


                if (checkValidationForField(firstName)) {
                    dataForAutoRegistration.setfName(firstName);
                }
                if (checkValidationForField(lastName)) {
                    dataForAutoRegistration.setLastName(lastName);

                }
                if (checkValidationForField(birthDate)) {
                    dataForAutoRegistration.setBirthDate(birthDate);
                }
                if (checkValidationForField(licennse)) {
                    dataForAutoRegistration.setLicennse(licennse);

                }
                if (checkValidationForField(planProvider)) {
                    dataForAutoRegistration.setPlanProvider(planProvider);
                }
                if (checkValidationForField(gender)) {
                    dataForAutoRegistration.setGender(gender);

                }
                if (checkValidationForField(memberId)) {
                    dataForAutoRegistration.setMemberId(memberId);
                }
                if (checkValidationForField(grpName)) {
                    dataForAutoRegistration.setGrpName(grpName);

                }
                if (checkValidationForField(grpNumber)) {
                    dataForAutoRegistration.setGrpNumber(grpNumber);
                }
                if (checkValidationForField(coverage)) {
                    dataForAutoRegistration.setCoverage(coverage);

                }
                if (checkValidationForField(planType)) {
                    dataForAutoRegistration.setPlanType(planType);
                }
                if (checkValidationForField(city)) {
                    dataForAutoRegistration.setCity(city);

                }
                if (checkValidationForField(state)) {
                    dataForAutoRegistration.setState(state);

                }

            }
        }
        return dataForAutoRegistration;
    }

    private boolean checkValidationForField(String data) {
        if (data == null || TextUtils.isEmpty(data)) {
            return false;
        }
        return true;
    }

    private ArrayList<CardResponse> parseCarddata(String cardResponse) {
        Gson gson = new Gson();
        try {
            Type listType = new TypeToken<List<CardResponse>>() {
            }.getType();
            List<CardResponse> cardResponses = (List<CardResponse>) gson.fromJson(cardResponse, listType);
            ArrayList<CardResponse> cardResponseList = (ArrayList) cardResponses;
            Log.v(TAG, cardResponseList.toString());
            CardResponse cardRes = cardResponseList.get(0);
            Log.v(TAG, "CardResponse Data:as obj:" + cardRes.toString());
            return cardResponseList;
        } catch (Exception e) {

            return null;

        }

    }

    private void showDilaog(String message) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        if (Util.ifNetworkAvailableShowProgressDialog(ProfileActivity.this, getString(R.string.loading_text), true)) {
                            new sendCardImages().execute();
                        }

                        dialog.dismiss();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
        builder.setTitle("Alert");
        builder.setMessage(message + " .Try again.").

                setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();

    }

    private boolean checkValidationsBeforeCall() {
        if (pathIcFront == null && pathIcBack == null && pathDlFront == null && pathDlBack == null && pathSelfie == null) {
            Util.showAlert(ProfileActivity.this, "Please attach Id proof.", "Alert");
            return false;
        } else if ((pathDlFront != null && pathDlBack == null) || (pathDlBack != null && pathDlFront == null)) {

            Util.showAlert(ProfileActivity.this, "Please attach Id proof for front and back.", "Alert");
            return false;

        } else if ((pathIcFront != null && pathIcBack == null) || (pathIcBack != null && pathIcFront == null)) {
            Util.showAlert(ProfileActivity.this, "Please attach insurance proof for front and back.", "Alert");
            return false;
        } else if (pathSelfie != null) {
            if ((pathDlFront == null || pathDlBack == null)) {

                Util.showAlert(ProfileActivity.this, "Please attach Id proof for front and back.", "Alert");
                return false;
            }
        }
        return true;

    }

    @Override
    protected void onResume() {
        super.onResume();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (Util.isRegistrationFlowFinished || isUserLogout) {
            Util.isRegistrationFlowFinished = false;
            finish();
        }
    }

    public static void showAlert(Context context, String message, String title) {
        new android.support.v7.app.AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message).setCancelable(false)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isDialogAlertShown = true;
                    }
                }).show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(Constant.PROFILE_DIALOG, isDialogAlertShown);
        putUrlValuesInBundle(outState);
    }
}
