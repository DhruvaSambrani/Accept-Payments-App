package com.dhruva.freshers;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Settings extends AppCompatActivity {

    EditText choc, pass, mm;
    Button up;
    SharedPreferences sp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init();
    }

    private void init() {
        choc = findViewById(R.id.choc);
        mm = findViewById(R.id.mm);
        pass = findViewById(R.id.pass);
        up = findViewById(R.id.up);
        sp = getSharedPreferences("costs", MODE_PRIVATE);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pass.getText().toString().equals("master")){
                    sp.edit().putInt("choc", Integer.parseInt(choc.getText().toString())).apply();
                    sp.edit().putInt("mm", Integer.parseInt(mm.getText().toString())).apply();
                    Toast.makeText(getApplicationContext(), "Updated!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Unauthorised!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mm.setText(String.valueOf(sp.getInt("mm",10)));
        choc.setText(String.valueOf(sp.getInt("choc",20)));
    }
}
