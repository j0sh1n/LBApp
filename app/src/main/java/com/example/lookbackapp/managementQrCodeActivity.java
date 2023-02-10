package com.example.lookbackapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class managementQrCodeActivity extends AppCompatActivity {

    ImageView qrCode;
    FirebaseAuth fAuth;
    String qrCodeId;
    TextView name, address, checkIns, days;
    Button refresh, logout;
    FirebaseDatabase fb = FirebaseDatabase.getInstance("https://lookbackapp-2a576-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference db = fb.getReference("Management");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management_qr_code);

        checkIns    = (TextView) findViewById(R.id.managementCheckIn);
        name        = (TextView) findViewById(R.id.managementName);
        address     = (TextView) findViewById(R.id.managementAddress);
        days        = (TextView) findViewById(R.id.managementDays);
        fAuth       = FirebaseAuth.getInstance();
        qrCodeId    = fAuth.getCurrentUser().getUid();
        qrCode      = findViewById(R.id.qrCode);
        refresh     = (Button) findViewById(R.id.buttonRefresh);
        logout      = (Button) findViewById(R.id.buttonLogout);

        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(qrCodeId, BarcodeFormat.QR_CODE, 750 , 750);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            qrCode.setImageBitmap(bitmap);
        }
        catch (Exception e){
            e.printStackTrace();
        }

        DatabaseReference dbName = db.child(qrCodeId);
        dbName.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    name.setText(snapshot.child("name").getValue().toString());
                    address.setText(snapshot.child("address").getValue().toString());
                    checkIns.setText("Check Ins : " + snapshot.child("checkIns").getValue().toString());
                    days.setText(snapshot.child("daysWithoutCovid").getValue().toString() + " Days");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fAuth.signOut();
                startActivity(new Intent(managementQrCodeActivity.this, loginActivity.class));
                Toast.makeText(managementQrCodeActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

}