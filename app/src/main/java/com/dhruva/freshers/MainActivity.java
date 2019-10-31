package com.dhruva.freshers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public final static int QRcodeSize = 500;
    Bitmap bitmap;
    Context context;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db;
    String payeename, upiid;
    Button gen, calc;
    EditText payername, QchocET, payerroll;
    TextView amt;
    EditText QmmET;
    int[] Amount, Qmm, QChoc;
    SharedPreferences sp;
    Button pay;
    ImageView qr;

    public MainActivity() {}

    @Override
    protected void onResume() {
        super.onResume();
        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(getApplicationContext(), "Sign back in", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initLogic();
    }

    private void init() {
        db = FirebaseFirestore.getInstance();
        context = this;
        calc = findViewById(R.id.calc);
        QmmET = findViewById(R.id.Qmm);
        gen = findViewById(R.id.gen);
        qr = findViewById(R.id.qr);
        pay = findViewById(R.id.pay);
        payername = findViewById(R.id.payername);
        amt = findViewById(R.id.amount);
        payerroll = findViewById(R.id.payerroll);
        QchocET = findViewById(R.id.Qchoc);
        sp = getSharedPreferences("costs", MODE_PRIVATE);
        QChoc = new int[1];
        Qmm = new int[1];
        Amount = new int[1];
    }

    private void initLogic() {
        calc.setVisibility(View.INVISIBLE);
        gen.setVisibility(View.INVISIBLE);
        pay.setVisibility(View.INVISIBLE);
        getInfo(Objects.requireNonNull(mAuth.getCurrentUser()).getUid());
        calc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gen.setVisibility(View.VISIBLE);
                String QChocS = QchocET.getText().toString();
                String QmmS = QmmET.getText().toString();
                if (QChocS.equals("")) {
                    QChoc[0] = 0;
                    QchocET.setText("0");
                } else {
                    QChoc[0] = Integer.parseInt(QChocS);
                }
                if (QmmS.equals("")) {
                    Qmm[0] = 0;
                    QmmET.setText("0");
                } else {
                    Qmm[0] = Integer.parseInt(QmmS);
                }
                Amount[0] = QChoc[0] * sp.getInt("choc", 20) + Qmm[0] * sp.getInt("mm", 10);
                amt.setText(String.valueOf(Amount[0]));
            }
        });
        gen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pay.setVisibility(View.VISIBLE);
                enableSetter(false);
                String EditTextValue = "upi://pay?" +
                        "pa=" + upiid +
                        "&pn=" + payeename +
                        "&am=" + amt.getText().toString() +
                        "&cu=INR" +
                        "&tn=" + Uri.encode("LDS Event");
                try {
                    bitmap = TextToImageEncode(EditTextValue);
                    qr.setImageBitmap(bitmap);

                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });
        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long time = Calendar.getInstance().getTimeInMillis();
                Map<String, String> data = new HashMap<>();
                data.put("Payee Name", payeename);
                data.put("Payer Name", payername.getText().toString());
                data.put("Payer Roll Number", payerroll.getText().toString());
                data.put("Amount", amt.getText().toString());
                data.put("Upi Id", upiid);
                data.put("Time", new SimpleDateFormat("hh:mm", Locale.getDefault()).format(time));
                data.put("Qchoc", String.valueOf(QChoc[0]));
                data.put("Qmm", String.valueOf(Qmm[0]));
                db.collection("data").document(String.valueOf(time))
                        .set(data)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("tag", "DocumentSnapshot successfully written!");
                                Toast.makeText(getApplicationContext(), "Success!",
                                        Toast.LENGTH_SHORT).show();
                                cleanup();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("tag", "Error writing document", e);
                                Toast.makeText(getApplicationContext(),
                                        "Failed with: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void enableSetter(boolean b) {
        payername.setEnabled(b);
        payerroll.setEnabled(b);
        QchocET.setEnabled(b);
        QmmET.setEnabled(b);
        calc.setEnabled(b);
    }

    private void cleanup() {
        qr.setImageDrawable(null);
        gen.setVisibility(View.INVISIBLE);
        pay.setVisibility(View.INVISIBLE);
        enableSetter(true);
        payername.setText("");
        payerroll.setText("");
        QchocET.setText("");
        QmmET.setText("");
        amt.setText("");

    }

    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.QR_CODE,
                    QRcodeSize, QRcodeSize, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.QRCodeBlackColor) :
                        getResources().getColor(R.color.QRCodeWhiteColor);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight,
                Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    void getInfo(String UID) {
        db.collection("users").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    assert document != null;
                    if (document.exists()) {
                        Log.d("tag", "DocumentSnapshot data: " + document.getData());
                        payeename = document.getString("name");
                        upiid = document.getString("upiid");
                        calc.setVisibility(View.VISIBLE);
                    } else {
                        Log.d("tag", "No such document");
                    }
                } else {
                    Log.d("tag", "get failed with ", task.getException());
                }
            }
        });
    }
}
