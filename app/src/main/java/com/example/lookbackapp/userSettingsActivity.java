package com.example.lookbackapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;

public class userSettingsActivity extends AppCompatActivity {

    private Button logout;
    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase fbDb = FirebaseDatabase.getInstance("https://lookbackapp-2a576-default-rtdb.asia-southeast1.firebasedatabase.app/");
    private DatabaseReference dbRef = fbDb.getReference("Users");
    private ImageView userQrScanBtn, userHistoryBtn, userNotifBtn;
    private Switch covStat;
    private EditText email, lname, fname, gender, address, company, employment;
    private AlertDialog.Builder builder;
    private String userId, covPosCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        email           = (EditText) findViewById(R.id.editTextEmailAddress);
        lname           = (EditText) findViewById(R.id.editTextLastName);
        fname           = (EditText) findViewById(R.id.editTextFirstName);
        gender          = (EditText) findViewById(R.id.editTextGender);
        address         = (EditText) findViewById(R.id.editTextAddress);
        company         = (EditText) findViewById(R.id.editTextCompany);
        employment      = (EditText) findViewById(R.id.editTextEmployment);
        logout          = (Button) findViewById(R.id.btnLogout);
        fAuth           = FirebaseAuth.getInstance();
        userQrScanBtn   = (ImageView) findViewById(R.id.btnTarget);
        userHistoryBtn  = (ImageView) findViewById(R.id.btnMap);
        covStat         = (Switch) findViewById(R.id.covPosSwitch);
        userId          = fAuth.getUid();
        builder         = new AlertDialog.Builder(this);
        userNotifBtn    = (ImageView) findViewById(R.id.btnStat);

        dbRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                email.setText(snapshot.child("email").getValue().toString());
                lname.setText(snapshot.child("lname").getValue().toString());
                fname.setText(snapshot.child("fname").getValue().toString());
                gender.setText(snapshot.child("gender").getValue().toString());
                address.setText(snapshot.child("address").getValue().toString());
                company.setText(snapshot.child("company").getValue().toString());
                employment.setText(snapshot.child("employment").getValue().toString());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        dbRef.child(userId).child("covStat").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                covPosCheck = snapshot.getValue().toString();
                if (covPosCheck.equals("POSITIVE")){
                    covStat.setChecked(true);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        covStat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(covStat.isChecked()) {
                    builder.setTitle("ALERT!").setMessage("By clicking this switch, you are setting your account as covid positive, the system will notify all" +
                            " users who logged in on places you logged in the past two week, Are you sure you want to turn this switch on?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dbRef.child(userId).child("covStat").setValue("POSITIVE");
                                    notifyUsersFirebase(userId);
                                    finish();
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    covStat.setChecked(false);
                                    dbRef.child(userId).child("covStat").setValue("NEGATIVE");
                                    dialog.cancel();
                                }
                            }).show();
                }else{
                    dbRef.child(userId).child("covStat").setValue("NEGATIVE");
                }
            }
        });

        userNotifBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(userSettingsActivity.this, userNotificationActivity.class));
            }
        });

        userQrScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(userSettingsActivity.this, userQrScannerActivity.class));
            }
        });

        userHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(userSettingsActivity.this, userHistoryActivity.class));
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fAuth.signOut();
                startActivity(new Intent(userSettingsActivity.this, loginActivity.class));
                Toast.makeText(userSettingsActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void notifyUsersFirebase(String userId){
        DatabaseReference dbRefHistoryDates = fbDb.getReference("Users").child(userId).child("HistoryDates");
        SimpleDateFormat formatter = new SimpleDateFormat("dd MM yyyy");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        for(int i = 0; i != 14; i ++){
            calendar.add(Calendar.DATE, -i);
            Date upDate = calendar.getTime();
            String Sdate = formatter.format(upDate);
            dbRefHistoryDates.child(Sdate).child("list").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()){
                        String data = snapshot.getValue().toString();
                        dataAnalyzer(data, Sdate);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    int manCount;
    int manCtr = 0;
    public void dataAnalyzer(String data, String Sdate){
        String[] managementIds = data.split("\\s+");
        manCount = managementIds.length;
        for(String x : managementIds){
            DatabaseReference dbRefHistoryManagement = fbDb.getReference("Management").child(x).child("History").child(Sdate);
            dbRefHistoryManagement.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        String data2 = snapshot.getValue().toString();
                        fbDb.getReference("Management").child(x).child("daysWithoutCovid").setValue(0);
                        dataAnalyzer2(data2, x, Sdate, manCtr);
                        manCtr++;
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    StringBuilder sb = new StringBuilder();
    public void dataAnalyzer2(String data, String managementId, String Sdate, int manCtr){
        String[] userIds = data.split("\\s+");
        for(String x : userIds){
            sb.append(Sdate + " ");
            sb.append(x + " ");
            sb.append(managementId + "!");
        }
        if (manCtr == manCount-1){
            putInDbNotif(sb.toString());
        }
    }

    //TODO
    public void putInDbNotif(String data2){
        String[] data = data2.split("!");
        System.out.println(data2);
        for(String x : data){
            String date = x.substring(0,10);
            String userId = x.substring(11,39);
            String managementId = x.substring(40, 68);
            DatabaseReference dbRefNotification = fbDb.getReference("Users").child(userId).child("Notifications").child(date).child("list");
            dbRefNotification.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    System.out.println(x);
                    String taskVal = String.valueOf(snapshot.getValue());
                    if (taskVal.equals("null")){
                        String pushVal = dbRefNotification.push().getKey();
                        dbRefNotification.child(pushVal).setValue(managementId);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
    }

}