package com.example.dailyshopping;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText email;
    private EditText password;
    private TextView sign_in;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // get edit texts by id
        email = findViewById(R.id.email_reg);
        password = findViewById(R.id.password_reg);
        sign_in = findViewById(R.id.sign_in);

        Button btnReg = findViewById(R.id.reg);
        progressDialog = new ProgressDialog(this);



        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get text from EditText
                String mEmail = email.getText().toString();
                String mPassword = password.getText().toString();

                if (TextUtils.isEmpty(mEmail)){
                    email.setError("Required field");
                    return;
                }
                if (TextUtils.isEmpty(mPassword)){
                    password.setError("Required field");
                    return;
                }

                progressDialog.setMessage("Processing...");
                progressDialog.show();

                mAuth.createUserWithEmailAndPassword(mEmail, mPassword)
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                                    Toast.makeText(getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();


                                } else {
                                    Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();

                                }
                            }
                        });
            }
        });

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            }
        });
    }
}
