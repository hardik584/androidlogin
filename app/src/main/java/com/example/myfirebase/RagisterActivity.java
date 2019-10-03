package com.example.myfirebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.R.layout;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class RagisterActivity extends AppCompatActivity {
    private Toolbar toolbars;
    private ImageView back;
    private ImageView logout;
    private TextView tooltitle;
    private EditText passwordreg;
    private EditText phone;
    private EditText emailreg;
    private Button registbtn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private String message;
    private int color;
    private String email = "";
    private String password = "";
    private String number, code, number2 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ragister);
        initobj();
        initclick();
        tooltitle.setText("Ragister");
        mAuth = FirebaseAuth.getInstance();

    }

    private void initobj() {
        tooltitle = findViewById(R.id.tooltitle);
        back = findViewById(R.id.back);
        logout = findViewById(R.id.logout);
        toolbars = findViewById(R.id.toolbars);
        emailreg = findViewById(R.id.emailreg);
        phone = findViewById(R.id.phone);
        passwordreg = findViewById(R.id.passwordreg);
        registbtn = findViewById(R.id.registbtn);
        progressBar = findViewById(R.id.progressBar);
        logout.setVisibility(View.INVISIBLE);
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
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Back();
            }
        });
        registbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userRegister();
                Complete();
            }

        });
    }

    private void Open() {
        Intent i = new Intent(RagisterActivity.this, LoginActivity.class);
//        i.putExtra("PHONE", number);
//        i.putExtra("em", email);
//        i.putExtra("pa", password);
        startActivity(i);
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(this, "Registerd Successfully...!", Toast.LENGTH_SHORT).show();
    }

    private void userRegister() {

        email = emailreg.getText().toString();
        password = passwordreg.getText().toString();
        number2 = phone.getText().toString().trim();
        number = "+91" + number2;


        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), "Please enter email...", Toast.LENGTH_LONG).show();
            emailreg.setError("Enter Email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
            passwordreg.setError("Enter Password");
            return;
        }
        if (number.isEmpty() || number.length() < 10) {
            phone.setError("Enter A Valid Number");
            Toast.makeText(getApplicationContext(), "Mobile Number Required", Toast.LENGTH_SHORT).show();
            phone.requestFocus();
            return;
        }
        if (password.length() < 6) {
            passwordreg.setError("Password Is Too Short");
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
            return;

        }
        Open();
    }
//    protected void Complete(){
//        mAuth.createUserWithEmailAndPassword(email, password)
//                .addOnCompleteListener(RagisterActivity.this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        Toast.makeText(RagisterActivity.this, "User Created" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
//                        progressBar.setVisibility(View.GONE);
//
//                        if (!task.isSuccessful()) {
//                            Toast.makeText(RagisterActivity.this, "Authentication failed." + task.getException(),
//                                    Toast.LENGTH_SHORT).show();
//                        } else
//                            Toast.makeText(RagisterActivity.this, "Successfulley Complete", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);

    }

    private void Back() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
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
            Snackbar snackbar = Snackbar.make(findViewById(R.id.registbtn), message, Snackbar.LENGTH_LONG);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(R.id.snackbar_text);
            textView.setTextColor(color);
            snackbar.show();
        }

    }
    protected void Complete() {

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.VISIBLE);
                        if (task.isSuccessful()) {
                            User user = new User(email, number);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RagisterActivity.this, "Registration Success", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(RagisterActivity.this, "Ragistration Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(RagisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
}
