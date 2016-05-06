package com.hackensack.umc.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hackensack.umc.R;
import com.hackensack.umc.datastructure.HttpResponse;
import com.hackensack.umc.datastructure.PatientData;
import com.hackensack.umc.datastructure.UserToken;
import com.hackensack.umc.http.CommonAPICalls;
import com.hackensack.umc.http.HttpDownloader;
import com.hackensack.umc.http.HttpUtils;
import com.hackensack.umc.http.ServerConstants;
import com.hackensack.umc.listener.HttpDownloadCompleteListener;
import com.hackensack.umc.listener.ILoginInteface;
import com.hackensack.umc.patient_data.Extension;
import com.hackensack.umc.patient_data.PatientJsonCreater;
import com.hackensack.umc.patient_data.Telecom;
import com.hackensack.umc.response.AccessTokenResponse;
import com.hackensack.umc.util.Constant;
import com.hackensack.umc.util.MychartLogin;
import com.hackensack.umc.util.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import epic.mychart.android.library.open.WPOpen;
public class CreatePasswordActivity extends BaseActivity implements HttpDownloadCompleteListener,ILoginInteface {
    private EditText edtPassword;
    private EditText edtVerifyPassword;
    private EditText txtUserName;
    private EditText txtMRN;
    private String mUserName;
    private String mMRN;
    private String TAG = "CreatePasswordActivity";
    private SharedPreferences sharedPreferences;
    private String mPassword;
    private String mVerifyPassword;
    private String lastName;
    private String firstName;
    private String birthDate;
    private String gender;
    private String mUserEmailId;
    // private Button btnGetMRN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_create_password);

        iflateXml();
        getDataFromIntent();
        initialiseVariables();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_password, menu);
        return true;
    }



    @Override
    public void didLogIn(boolean isLoggedIn) {
         stopProgress();
        if (isLoggedIn) {
            isMyChartLogin = true;
            Log.e("mychart","CreatePassword didlogin "+isLoggedIn);
            Util.startMychartTokenSync(CreatePasswordActivity.this, WPOpen.millisToTokenExpiration());
            Util.startDeviceSync(CreatePasswordActivity.this, WPOpen.millisToDeviceTimeOut());
            if (Util.ifNetworkAvailableShowProgressDialog(CreatePasswordActivity.this, getString(R.string.login_text), true)) {
                HttpDownloader http = new HttpDownloader(CreatePasswordActivity.this, ServerConstants.LOGIN_URL, Constant.LOGIN_DATA, CreatePasswordActivity.this);
                http.startDownloading();
            }
        } else {
            Log.e("bhagya","CreatePassword didlogin "+isLoggedIn);
            AlertDialog.Builder builder = new AlertDialog.Builder(CreatePasswordActivity.this);

            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_network_offline, null);
            builder.setView(dialogView);
                ((TextView) dialogView.findViewById(R.id.dialog_title)).setText(getString(R.string.error_str));
            ((TextView) dialogView.findViewById(R.id.text_message)).setText(getString(R.string.mychart_decline_str));

            Button btnDismiss = (Button) dialogView.findViewById(R.id.button_dialog_ok);
            btnDismiss.setOnClickListener(new Button.OnClickListener() {

                @Override
                public void onClick(View v) {
                    alert.dismiss();
                    isLoginDone = true;
                    isMyChartLogin = false;
                    Util.isRegistrationFlowFinished = true;
                    Util.setPatientMRNID(CreatePasswordActivity.this, mMRN);
                    CreatePasswordActivity.this.finish();
                }
            });

            alert = builder.show();
            //Util.showNetworkOfflineDialog(CreatePasswordActivity.this, getString(R.string.error_str), getString(R.string.mychart_decline_str));

            //stopProgress();
            //Show popup call helpdesk
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case Constant.REQUEST_TERMSCONDITIONS:
                if(resultCode == RESULT_OK){
                    Log.e("mychart","CreatePassword REQUEST_TERMSCONDITIONS "+resultCode);
                    didLogIn(true);}
                else{
                    Log.e("mychart","CreatePassword REQUEST_TERMSCONDITIONS "+resultCode);
                didLogIn(false);}
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                Constant.backFromCreatePassword = true;
                isBackPressed = true;
                finish();
                return true;
            case R.id.password_action_done:
                if (validatePasswords()) {
                    Log.v(TAG, "create_password_action_done");
                    if (Util.ifNetworkAvailableShowProgressDialog(CreatePasswordActivity.this, (getString(R.string.creating_mychart)), true)) {
                        new getEpicAccessToken().execute();
                    }

                }
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }

    }

    private void initialiseVariables() {

        sharedPreferences = getSharedPreferences(Constant.SHAREPREF_TAG, MODE_PRIVATE);
    }

    private void getDataFromIntent() {
        Intent intent = getIntent();
        int from = intent.getIntExtra(Constant.FROM_ACTIVTY, Constant.FROM_REGISTRATION_ACTIVITY);
        if (from == Constant.FROM_REGISTRATION_ACTIVITY) {
            txtUserName.setEnabled(false);
            txtMRN.setEnabled(false);
            mUserName = intent.getStringExtra(Constant.USER_NAME);
            mMRN = intent.getStringExtra(Constant.MRN);
            mUserEmailId = intent.getStringExtra(Constant.USER_NAME);
            lastName = intent.getStringExtra(Constant.PARENTS_LAST_NAME);
            firstName = intent.getStringExtra(Constant.PARENTS_FIRST_NAME);
            birthDate = intent.getStringExtra(Constant.PARENTS_BIRTHDATE);
            gender= intent.getStringExtra(Constant.PARENTS_GENDER);

            txtUserName.setText(mUserName);
            txtMRN.setText(mMRN);

           setTitle(getString(R.string.title_activity_create_password));

        } else if (from == Constant.FROM_LOGIN_ACTIVITY) {
            //btnGetMRN.setVisibility(View.VISIBLE);
            txtUserName.setEnabled(true);
            txtMRN.setEnabled(true);
            setTitle(getString(R.string.title_activity_create_creadentials));
        }

       /* mMRN = "900702417";
        mUserName = "gautam_zodape@persistent.com";*/

    }

    private void iflateXml() {
        edtPassword = (EditText) findViewById(R.id.edtPassword);
        edtVerifyPassword = (EditText) findViewById(R.id.edtVerifyPassword);
        txtUserName = (EditText) findViewById(R.id.txt_userName);
        txtMRN = (EditText) findViewById(R.id.txt_MRN);
       /* btnGetMRN= (Button) findViewById(R.id.btnGetMRN);
        btnGetMRN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CreatePasswordActivity.this, ProfileSelfieActivity.class);
                startActivity(intent);
                finish();
            }
        });*/


    }

    private AlertDialog alert;

    public void HttpDownloadCompleted(HttpResponse data) {
        Log.v(TAG, String.valueOf(data.getResponseCode()));
        Log.v(TAG, "in HttpDownloadCompleted");

        Object obj = data.getDataObject();

        if (obj != null)
            Log.v(TAG, obj.toString());
        if (data.getRequestType() == Constant.LOGIN_DATA) {
            if ((data.getResponseCode() >= Constant.HTTP_OK && data.getResponseCode() < Constant.HTTP_REDIRECT)) {
                if (obj != null && !isFinishing()) {

                    if (obj instanceof UserToken) {

                        String token = ((UserToken) obj).getAccessToken();

                        if (token != null && token.length() > 0) {

                            HttpDownloader http = new HttpDownloader(CreatePasswordActivity.this, ServerConstants.READ_PATIENT_URL + Util.getPatientMRNID(this) + ServerConstants.READ_PATIENT_URL_PART, Constant.READ_PATIENT_DATA, CreatePasswordActivity.this);
                            http.startDownloading();

                            Util.startUserSession(this);

                            return;
                        }
                    }

                }
            } else {
                Util.showNetworkOfflineDialog(CreatePasswordActivity.this, "Fail", "Log in fail, please try again with valid username and password");
            }

        } else if (data.getRequestType() == Constant.READ_PATIENT_DATA && !isFinishing()) {

            isLoginDone = true;
            Util.isRegistrationFlowFinished = true;
            finish();

        }
        stopProgress();
    }
    private class CreatePassword extends AsyncTask<String, Void, String> {
        String createPasswordSuccess;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            String message = null;
            String response = HttpUtils.sendHttpPostForUsingJsonObj(ServerConstants.URL_CREATE_ACCOUNT, createJsonForCreatePassword(params[0]), createHeadersForCreatePassword());
            try {
                JSONObject jsonObject = new JSONObject(response);
                //11-04 15:23:26.683: V/CreatePasswordActivity(14731): response to createPassword::{"success":true}

                createPasswordSuccess = jsonObject.getString("success");
            } catch (JSONException e) {
                e.printStackTrace();
                //11-05 19:41:12.174: V/CreatePasswordActivity(27479): response to createPassword::{"message":"The login ID passed in already belongs to somebody else's MyChart account.","code":"422"}
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    message = jsonObject.getString("message");
                    return message;
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }

            Log.v(TAG, "response to createPassword::" + response);
            return createPasswordSuccess;
        }

        @Override
        protected void onPostExecute(String response) {

            super.onPostExecute(response);

            stopProgress();

            if (response != null) {
                if (response.equalsIgnoreCase("true")) {
//                    Util.showAlert(CreatePasswordActivity.this, getString(R.string.msg_password_created_successfully), getString(R.string.success));

//                    new AlertDialog.Builder(CreatePasswordActivity.this)
//                            .setTitle(getString(R.string.success))
//                            .setMessage(getString(R.string.msg_password_created_successfully)).setCancelable(false)
//                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//
//                                    dialog.dismiss();
//
////                                    Util.isRegistrationFlowFinished = true;
////                                    finish();
//                                    if (Util.ifNetworkAvailableShowProgressDialog(CreatePasswordActivity.this, getString(R.string.login_text), true)) {
//                                        HttpDownloader http = new HttpDownloader(CreatePasswordActivity.this, Constant.LOGIN_URL, Constant.LOGIN_DATA, CreatePasswordActivity.this);
//                                        http.startDownloading();
//                                    }
//
//                                }
//                            }).show();

//                    if (!isFinishing()) {
//
//                        AlertDialog.Builder builder = new AlertDialog.Builder(CreatePasswordActivity.this);
//                        builder.setCancelable(false);
//                        LayoutInflater inflater = getLayoutInflater();
//                        View dialogView = inflater.inflate(R.layout.dialog_network_offline, null);
//                        builder.setView(dialogView);
//
//                        ((TextView) dialogView.findViewById(R.id.dialog_title)).setText(getString(R.string.success));
//                        ((TextView) dialogView.findViewById(R.id.text_message)).setText(getString(R.string.msg_password_created_successfully));
//
//                        Button btnOk = (Button) dialogView.findViewById(R.id.button_dialog_ok);
//                        btnOk.setOnClickListener(new Button.OnClickListener() {
//
//                            @Override
//                            public void onClick(View v) {
//
//                                alert.dismiss();
//                                if (Util.ifNetworkAvailableShowProgressDialog(CreatePasswordActivity.this, getString(R.string.login_text), true)) {
//                                    HttpDownloader http = new HttpDownloader(CreatePasswordActivity.this, Constant.LOGIN_URL, Constant.LOGIN_DATA, CreatePasswordActivity.this);
//                                    http.startDownloading();
//                                }
//                            }
//                        });
//
//                        alert = builder.show();
//                    }

                    if (Util.ifNetworkAvailableShowProgressDialog(CreatePasswordActivity.this, getString(R.string.login_text), true)) {
                        /*HttpDownloader http = new HttpDownloader(CreatePasswordActivity.this, ServerConstants.LOGIN_URL, Constant.LOGIN_DATA, CreatePasswordActivity.this);
                        http.startDownloading();*/
                        MychartLogin mychartLoginAsync = new MychartLogin(CreatePasswordActivity.this,CreatePasswordActivity.this);
                        mychartLoginAsync.execute(mUserName, mPassword);
                    }


                    //Util.showAlert(CreatePasswordActivity.this, getString(R.string.msg_password_created_successfully), getString(R.string.success));
                } else {
                    Util.showAlert(CreatePasswordActivity.this, response, "Alert");
                }
            }
           // stopProgress();
        }
    }

    public String getUserData() {

        String str = "{\"username\": \"USERID\", \"password\": \"PASS\"}";

        str = str.replace("USERID", mUserName.trim());
        str = str.replace("PASS", edtVerifyPassword.getText().toString().trim());

        return str;
    }

    private List<NameValuePair> createHeadersForCreatePassword() {
        ArrayList<NameValuePair> headers = new ArrayList<>();
        headers.add(new BasicNameValuePair("Content-Type", "application/json"));
        headers.add(new BasicNameValuePair("Authorization", "Bearer " + sharedPreferences.getString(ServerConstants.ACCESS_TOKEN_EPIC, "")));
        return headers;
    }

    private JSONObject createJsonForCreatePassword(String patientId) {
        Gson gson = new Gson();
        JSONObject jsonForCreatePassword = null;
        mMRN=txtMRN.getText().toString().trim();
        mUserName=txtUserName.getText().toString().trim();
        patientId=mMRN;
        if(TextUtils.isEmpty(mUserEmailId)){
            mUserEmailId=mUserName;
        }
        PatientData patientData=new PatientData(patientId,"HUMC",mUserName,"Favorite game?",mPassword,"cricket",mUserEmailId,firstName,lastName,birthDate,"true");
        String dataForPasswordCreation = gson.toJson(patientData);
       // String dataForPasswordCreation = gson.toJson(new PatientData(patientId, "HUMC", mUserName, "Favorite game?", "cricket", mUserName, "true", mPassword));
      //  String dataForPasswordCreation = gson.toJson(new PatientData(txtMRN.getText().toString().trim(), "HUMC", txtUserName.getText().toString().trim(), "", "", txtUserName.getText().toString().trim(), "true", mPassword));

        //String patientId, String patientIdType, String loginId, String passwordResetQuestion, String passwordResetAnswer,
        // String emailAddress, String receiveEmailNotifications, String password) {
        try {
            jsonForCreatePassword = new JSONObject(dataForPasswordCreation);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v(TAG, "jsonForCreatePassword::" + jsonForCreatePassword.toString());
        return jsonForCreatePassword;
    }

    private boolean validatePasswords() {
        mPassword = edtPassword.getText().toString();
        mVerifyPassword = edtVerifyPassword.getText().toString();
        if (TextUtils.isEmpty(mPassword) || TextUtils.isEmpty(mVerifyPassword)) {
            Util.showAlert(CreatePasswordActivity.this, getString(R.string.msg_enter_valid_password), getString(R.string.app_name));
            return false;
        } else if (!mPassword.matches(Constant.PASSWOSRD_REGULAR_EXPRESSION)) {
            Util.showAlert(CreatePasswordActivity.this, getString(R.string.msg_enter_valid_password), getString(R.string.app_name));
            return false;
        } else if (!mPassword.equalsIgnoreCase(mVerifyPassword)) {
            Util.showAlert(CreatePasswordActivity.this, getString(R.string.msg_enter_password_not_matching), getString(R.string.app_name));
            return false;
        }
        return true;
    }

    private class getEpicAccessToken extends AsyncTask<Void, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // startProgress(getString(R.string.creating_mychart));
        }

        @Override
        protected String doInBackground(Void... params) {
            String accessToken = new CommonAPICalls(CreatePasswordActivity.this).getEpicAccessToken();
            return accessToken;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (Util.ifNetworkAvailableShowProgressDialog(CreatePasswordActivity.this, (getString(R.string.creating_mychart)), true)) {
                new CreatePassword().execute(mMRN);
            }


            // new readPatient().execute(accessToken);//Login call after userCreatyion

        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Constant.backFromCreatePassword = true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Constant.backFromCreatePassword = true;
        }
        return super.onKeyDown(keyCode, event);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isUserLogout) {
            finish();
        }
    }
}
