package com.example.lookbackapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class userNotificationActivity extends AppCompatActivity {

    ImageView btnMap, btnTarget, btnSettings;
    ListView notifListView;
    ArrayList<String> notifList = new ArrayList<>();
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseDatabase fbDb = FirebaseDatabase.getInstance("https://lookbackapp-2a576-default-rtdb.asia-southeast1.firebasedatabase.app/");
    DatabaseReference dbRefUser = fbDb.getReference("Users").child(fAuth.getCurrentUser().getUid());
    DatabaseReference dbRefManagement = fbDb.getReference("Management");
    int z = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_notification);

        btnMap          = (ImageView) findViewById(R.id.btnMap);
        btnTarget       = (ImageView) findViewById(R.id.btnTarget);
        btnSettings     = (ImageView) findViewById(R.id.btnSetting);
        notifListView   = (ListView) findViewById(R.id.listViewHistory);

        dbRefUser.child("Notifications").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(!String.valueOf(task.getResult().getValue()).equals("null")){
                    StringBuilder sbParent = new StringBuilder();
                    StringBuilder sbChild = new StringBuilder();
                    String taskVal = String.valueOf(task.getResult().getValue());
                    String[] parentArray = taskVal.split("\\}\\,\\ ");
                    for(String x : parentArray){
                        if (x.charAt(0) == '{'){sbParent.append(x.substring(1, 11) + "\n");}
                        else {sbParent.append(x.substring(0, 10) + "\n");}
                    }
                    int i = 1;
                    for(DataSnapshot snapshot : task.getResult().getChildren()){
                        sbChild.append(snapshot.getValue().toString().substring(7,snapshot.getValue().toString().length()-2) + "\n");
                        i++;
                    }
                    StringBuilder sbCollection = new StringBuilder();
                    if(!taskVal.equals("null")){
                        parentArray = sbParent.toString().split("\n");
                        String[] childArray = sbChild.toString().split("\n");
                        int x = parentArray.length-1;
                        while(x != -1){
                            for(String y : childArray){
                                String[] childSubArray = y.split(", ");
                                for(String z : childSubArray){
                                    sbCollection.append(parentArray[x] + " " + z.substring(21,z.length()) + "!");
                                    uiFixer(parentArray[x], z.substring(21,z.length()), x, -1);
                                }
                                x--;
                            }
                        }
                        System.out.println(sbCollection.toString());
                        String[] dataCollection = sbCollection.toString().split("!");
                    /*for(String str : dataCollection){
                        addArrayList(str);
                    }*/
                    }
                }
            }
        });

        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(userNotificationActivity.this, userHistoryActivity.class));
            }
        });

        btnTarget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(userNotificationActivity.this, userQrScannerActivity.class));
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(userNotificationActivity.this, userSettingsActivity.class));
            }
        });
    }

    public void uiFixer(String date, String managementId, int ctr, int limit){
        dbRefManagement.child(managementId).child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                String taskVal = String.valueOf(task.getResult().getValue());
                String data = "DATE : " + date.replace(" ", "/") + "\nPLACE : " + taskVal;
                addArrayList(data);
                if(ctr == limit+1){
                    putArrayList(notifList);
                }
            }
        });
    }

    public void addArrayList(String data){
        notifList.add(data);
    }

    public void putArrayList(ArrayList<String> pnotifList){
        ArrayAdapter arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, pnotifList);
        notifListView.setAdapter(arrayAdapter);
    }

    @Override
    public void onBackPressed() {
    }
}