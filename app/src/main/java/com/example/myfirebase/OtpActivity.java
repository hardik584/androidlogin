package com.example.myfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class OtpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText otpcode;
    private Button submit, resend;
    private String number = "";
    private String verificationId;
    private ProgressBar progress;
    private String email, pass = "";
    private String message;
    private int color;
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);
        initobj();
        initclick();
        number = getIntent().getStringExtra("PHONE");
        email = getIntent().getStringExtra("em");
        pass = getIntent().getStringExtra("pa");
//        sendCode(number);
    }

    private void initobj() {
        otpcode = findViewById(R.id.otpcode);
        submit = findViewById(R.id.submit);
        progress = findViewById(R.id.progress);
        resend = findViewById(R.id.resend);
        mAuth = FirebaseAuth.getInstance();
        Run();
    }

    private void Run() {
        final Handler ha = new Handler();
        ha.postDelayed(new Runnable() {

            @Override
            public void run() {
                checkconnection();
                ha.postDelayed(this, 15000);
            }
        }, 3000);
    }

    private void initclick() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = otpcode.getText().toString().trim();

                if (code.isEmpty() || code.length() < 6) {

                    otpcode.setError("Enter Valid code...");
                    otpcode.requestFocus();
                    return;
                }
                verifyCode(code);
                Complete();
                Intent intent = new Intent(OtpActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
        resend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCode(number);
                Toast.makeText(OtpActivity.this, "Resend Otp ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallBack = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
            progress.setVisibility(View.INVISIBLE);
            Toast.makeText(OtpActivity.this, "Otp Sent", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            String code = phoneAuthCredential.getSmsCode();
            if (code != null) {
                otpcode.setText(code);
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(OtpActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    };

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
//        signInWithCredential(credential);
        progress.setVisibility(View.VISIBLE);
//        linkUserAuth();

    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Intent intent = new Intent(OtpActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);

                        } else {
                            Toast.makeText(OtpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void sendCode(String number) {

        progress.setVisibility(View.VISIBLE);
        PhoneAuthProvider.getInstance().verifyPhoneNumber(number, 60, TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallBack
        );
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//
//            Intent intent = new Intent(this, MainActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//        }
//    }

    protected void Complete() {

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progress.setVisibility(View.VISIBLE);
                        if (task.isSuccessful()) {
                            User user = new User(email, number);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progress.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(OtpActivity.this, "Registration Success", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(OtpActivity.this, "Ragistration Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(OtpActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });

//        mAuth.createUserWithEmailAndPassword(email, pass)
//                .addOnCompleteListener(OtpActivity.this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//
//                        Toast.makeText(getApplicationContext(), "User Created" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
//                        progress.setVisibility(View.GONE);
//
//                        if (!task.isSuccessful()) {
//                            Toast.makeText(getApplicationContext(), "Authentication failed." + task.getException(),
//                                    Toast.LENGTH_SHORT).show();
//                        } else
//                            Toast.makeText(getApplicationContext(), "Successfulley Complete", Toast.LENGTH_SHORT).show();
//                    }
//                });
    }

    private void linkUserAuth() {
        AuthCredential credential2 = EmailAuthProvider.getCredential(email, pass);
        mAuth.getCurrentUser().linkWithCredential(credential2)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(OtpActivity.this, "Linked User", Toast.LENGTH_SHORT).show();
//                            FirebaseUser user = task.getResult().getUser();
//                            Complete();
                        } else {
                            Toast.makeText(OtpActivity.this, "Linking Faield", Toast.LENGTH_SHORT).show();

                        }
                    }
                });

    }

    protected boolean isOnline() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            return false;
        }
    }

    public void checkconnection() {
        if (isOnline()) {
        } else {
            message = "No Internet Connection";
            color = Color.RED;
            Snackbar snackbar = Snackbar.make(findViewById(R.id.submit), message, Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }

    }
}