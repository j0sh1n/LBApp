package com.example.lookbackapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

public class userHistoryActivity extends AppCompatActivity {

    ImageView qrScanBtn, settingsBtn, notifBtn;
    ListView history;
    FirebaseAuth fAuth;
    FirebaseDatabase fbDb;
    DatabaseReference dbRef;
    String userId, fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_history);

        fbDb        = FirebaseDatabase.getInstance("https://lookbackapp-2a576-default-rtdb.asia-southeast1.firebasedatabase.app/");
        dbRef       = fbDb.getReference("Users");
        fAuth       = FirebaseAuth.getInstance();
        userId      = fAuth.getCurrentUser().getUid();
        qrScanBtn   = (ImageView) findViewById(R.id.btnTarget);
        settingsBtn = (ImageView) findViewById(R.id.btnSetting);
        fileName    = userId + ".txt";
        history     = (ListView) findViewById(R.id.listViewHistory);
        notifBtn    = (ImageView) findViewById(R.id.btnStat);

        retrieveFromDB(userId);

        qrScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(userHistoryActivity.this, userQrScannerActivity.class));
            }
        });

        settingsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(userHistoryActivity.this, userSettingsActivity.class));
            }
        });

        notifBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(userHistoryActivity.this, userNotificationActivity.class));
            }
        });

    }

    public void retrieveFromDB(String userID){
        dbRef.child(userID).child("HISTORY").child("List").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String data = snapshot.getValue().toString();
                    saveLocal(data, fileName);
                    fileToList(data);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void saveLocal(String data, String fileName){
        FileOutputStream fos = null;
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
    }

    int x;
    ArrayList<String> historyList = new ArrayList<>();
    public void fileToList(String data){
        String histories[] = data.split("\n");
        DatabaseReference dbRefPush = dbRef.child(userId).child("HISTORY");
        int len = histories.length;
        for (int i = len-1; i >= 0 ; i--){
            dbRefPush.child(histories[i]).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    x++;
                    if(snapshot.exists()){
                        String data = snapshot.getValue().toString();
                        data = dataStringFix(data);
                        addArrayList(data);
                        if (x == len){
                            putArrayList(historyList);
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    public String dataStringFix(String data){
        String temp = data;
        StringBuilder temp1 = new StringBuilder();
        temp1.append(temp);
        temp1.reverse();
        temp = temp1.toString();
        String[] temp2 = temp.split(",",2);
        StringBuilder temp3 = new StringBuilder();
        if (temp2.length != 1){
            temp3.append(temp2[0]);
            temp3.reverse();
            temp2[0] = temp3.toString();
            temp3 = new StringBuilder();
            temp3.append(temp2[1]);
            temp3.reverse();
            temp2[1] = temp3.toString();
            data = "NAME : " + temp2[1].substring(6, temp2[1].length()) + "\nDATE : " + temp2[0].substring(6, temp2[0].length()-1);
        }
        return data;
    }

    public void addArrayList(String data){
        historyList.add(data);
    }

    public void putArrayList(ArrayList<String> phistoryList){
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, phistoryList);
        history.setAdapter(arrayAdapter);
    }

    @Override
    public void onBackPressed() {
    }

}