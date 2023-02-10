package com.example.lookbackapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class userQrScannerActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 101;
    private FirebaseAuth fAuth;
    private FirebaseDatabase fbDb;
    private DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_qr_scanner);

        fAuth   = FirebaseAuth.getInstance();
        fbDb    = FirebaseDatabase.getInstance("https://lookbackapp-2a576-default-rtdb.asia-southeast1.firebasedatabase.app/");

        if(Build.VERSION.SDK_INT >= 23){
            if(checkPermission(Manifest.permission.CAMERA)){
                openScanner();
            }else{
                requestPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            }
        }else{
            openScanner();
        }
    }

    private void openScanner(){
        new IntentIntegrator(userQrScannerActivity.this).initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        dbRef = fbDb.getReference("Users");
        String userId = fAuth.getCurrentUser().getUid();
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        String managementId = result.getContents();
        SimpleDateFormat formatter  = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        SimpleDateFormat formatter2 = new SimpleDateFormat("dd MM yyyy");
        Date date                   = new Date();
        String time2                = formatter2.format(date);
        String time                 = formatter.format(date);
        if(result != null){
            if(result.getContents() == null){
                Toast.makeText(this, "Blank", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(userQrScannerActivity.this,userHistoryActivity.class));
            }else{
                DatabaseReference dbUserHistoryDates = fbDb.getReference("Users").child(userId).child("HistoryDates").child(time2).child("list");
                dbUserHistoryDates.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            String data = snapshot.getValue().toString();
                            String[] dataArray = data.split("\\s+");
                            int i = 1;
                            int length = dataArray.length;
                            for (String x : dataArray){
                                if (!managementId.equals(x)){
                                    if (i != length){
                                        i++;
                                    }else {
                                        String value = snapshot.getValue().toString();
                                        String value2 = value + " " + managementId;
                                        dbUserHistoryDates.setValue(value2);
                                    }
                                }
                            }
                        }else{
                            dbUserHistoryDates.setValue(managementId);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                DatabaseReference dbManagementHistory = fbDb.getReference("Management").child(managementId).child("History").child(time2);
                dbManagementHistory.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String data = snapshot.getValue().toString();
                            String[] dataArray = data.split("\\s+");
                            int i = 1;
                            int length = dataArray.length;
                            for (String x : dataArray){
                                if (!userId.equals(x)){
                                    if (i != length){
                                        i++;
                                    }else {
                                        String value = snapshot.getValue().toString();
                                        String value2 = value + " " + userId;
                                        dbManagementHistory.setValue(value2);
                                    }
                                }
                            }
                        }else{
                            dbManagementHistory.setValue(userId);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                History history = new History();
                DatabaseReference db2 = fbDb.getReference("Management").child(managementId).child("name");
                db2.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            String fileName             = userId + ".txt";
                            String name                 = snapshot.getValue().toString();

                            history.setName(name);
                            history.setTime(time);

                            String pushVal = dbRef.child(userId).child("HISTORY").push().getKey();
                            dbRef.child(userId).child("HISTORY").child(pushVal).setValue(history);
                            String data = saveLocal(fileName, pushVal);
                            dbRef.child(userId).child("HISTORY").child("List").setValue(data);

                            Toast.makeText(userQrScannerActivity.this, history.toString(), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(userQrScannerActivity.this, "QR doesn't exist", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                DatabaseReference db3 = fbDb.getReference("Management").child(managementId).child("checkIns");
                db3.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String ScheckIns = snapshot.getValue().toString();
                        int checkIns = Integer.parseInt(ScheckIns);
                        checkIns = checkIns + 1;
                        db3.setValue(checkIns);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(userQrScannerActivity.this, userHistoryActivity.class);
                        startActivity(i);
                        finish();
                    }
                }, 1000);
            }
        }else{
            Toast.makeText(this, "Blank", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(userQrScannerActivity.this, userHistoryActivity.class));
        }
    }

    private boolean checkPermission(String permission){
        int result = ContextCompat.checkSelfPermission(userQrScannerActivity.this, permission);
        if(result == PackageManager.PERMISSION_GRANTED){
            return true;
        }else{
            return false;
        }
    }

    private void requestPermission(String permission, int code){
        if(ActivityCompat.shouldShowRequestPermissionRationale(userQrScannerActivity.this, permission)){
        }
        else{
            ActivityCompat.requestPermissions(userQrScannerActivity.this, new String[]{permission}, code);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    openScanner();
                }
        }
    }

    public String saveLocal (String fileName, String pushVal){
        FileOutputStream fos = null;
        FileInputStream fis = null;
        String data = "";

        try {
            fis = openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            while ((text = br.readLine()) != null){
                sb.append(text).append("\n");
            }
            data = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null){
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        data = data + pushVal + "\n";
        try {
            fos = openFileOutput(fileName, MODE_PRIVATE);
            fos.write(data.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return data;
    }

    @Override
    public void onBackPressed() {
    }

}