package com.dhruva.freshers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        final FirebaseAuth mAuth;
        mAuth = FirebaseAuth.getInstance();
        Button signin = findViewById(R.id.signin);
        final EditText us = findViewById(R.id.us);
        final EditText pw = findViewById(R.id.pw);
        ImageView sett = findViewById(R.id.sett);
        sett.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_VIEW).setClass(Login.this, Settings.class);
                startActivity(i);
            }
        });
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = us.getText().toString() + "@iisermohali.ac.in";
                String password = pw.getText().toString();
                if (!email.equals("") && !password.equals("")){
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("tag", "signInWithEmail:success");
                                        Toast.makeText(Login.this, getResources().getString(R.string.welcome),
                                                Toast.LENGTH_SHORT).show();
                                        Intent i = new Intent(Intent.ACTION_VIEW).setClass(Login.this, MainActivity.class);
                                        startActivity(i);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("tag", "signInWithEmail:failure", task.getException());
                                        Toast.makeText(Login.this, getResources().getString(R.string.login_failed),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(getApplicationContext(), "Enpty Fields", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
