package com.hackensack.umc.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hackensack.umc.R;
import com.hackensack.umc.datastructure.HttpResponse;
import com.hackensack.umc.http.HttpDownloader;
import com.hackensack.umc.http.ServerConstants;
import com.hackensack.umc.listener.HttpDownloadCompleteListener;
import com.hackensack.umc.util.Constant;
import com.hackensack.umc.util.Util;

public class LoginRegistrationActivity extends AppCompatActivity implements HttpDownloadCompleteListener {

    Button mLoginButton, mRegisterButton;

    EditText mUserName, mPassword;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_registration);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        mLoginButton = (Button) findViewById(R.id.login_button);
        mLoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                String userName = mUserName.getText().toString();
                String password = mPassword.getText().toString();

                if(userName == null || userName.length() < 1) {
                    Util.showNetworkOfflineDialog(LoginRegistrationActivity.this, "Error", "Please provide valid Username");
                    return;
                }

                if(password == null || password.length() < 1) {
                    Util.showNetworkOfflineDialog(LoginRegistrationActivity.this, "Error", "Please provide valid Password");
                    return;
                }

                startProgress();

                HttpDownloader http = new HttpDownloader(LoginRegistrationActivity.this, ServerConstants.LOGIN_URL, Constant.LOGIN_DATA, LoginRegistrationActivity.this);
                http.startDownloading();

            }

        });

        mRegisterButton = (Button) findViewById(R.id.register_button);
        mRegisterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                Intent intent = new Intent(LoginRegistrationActivity.this, ProfileActivity.class);
                startActivity(intent);

            }

        });

        mUserName = (EditText) findViewById(R.id.username_edittext);
//        mUserName.setText("bibin_kurian@persistent.com");
        mPassword = (EditText) findViewById(R.id.password_edittext);
//        mPassword.setText("bibin123");

    }

    public String getUserData() {

        String str = "{\"username\": \"USERID\", \"password\": \"PASS\"}" ;

        str = str.replace("USERID", mUserName.getText().toString().trim());
        str = str.replace("PASS", mPassword.getText().toString().trim());

        return str;
    }

    public String getUserName() {
        return mUserName.getText().toString().trim();
    }

    public String getPassword() {
        return mPassword.getText().toString().trim();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_doctor_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private AlertDialog alert;

    @Override
    public void HttpDownloadCompleted(HttpResponse data) {

        Object obj = data.getDataObject();

        if (obj != null && !isFinishing()) {


            if (!isFinishing()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(LoginRegistrationActivity.this);

                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_network_offline, null);
                builder.setView(dialogView);

                ((TextView) dialogView.findViewById(R.id.dialog_title)).setText("Success");

                ((TextView) dialogView.findViewById(R.id.text_message)).setText("Login successful");

                TextView btnDismiss = (TextView) dialogView.findViewById(R.id.button_dialog_ok);
                btnDismiss.setOnClickListener(new Button.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        finish();

                        Intent intent = new Intent(LoginRegistrationActivity.this, DoctorAppointmentSummaryActivity.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra(Constant.KEY_LOGIN_SUCCESSFUL, true);
                        startActivity(intent);

                        alert.dismiss();
                    }
                });

                alert = builder.show();
            }

        }else {
            stopProgress();
            Util.showNetworkOfflineDialog(LoginRegistrationActivity.this, "Fail", "Log in fail, please try again with valid username and password");
        }

    }

    public void startProgress(){
//        mProgressDialog.setTitle("Logging in...");
        mProgressDialog.setMessage("Logging in...");
        mProgressDialog.show();
    }

    public void stopProgress(){
        mProgressDialog.dismiss();
    }


}

