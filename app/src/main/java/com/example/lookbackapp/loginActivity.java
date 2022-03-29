package com.example.lookbackapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;

public class loginActivity extends AppCompatActivity {

    private TextView            txtRegister;
    private Button              btnUser, btnLogin, btnManagement;
    private Boolean             management;
    private EditText            txtemail, txtpass;
    private FirebaseDatabase    fbDb;
    private DatabaseReference   dbRef;
    private FirebaseAuth        fAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fbDb            = FirebaseDatabase.getInstance("https://lookbackapp-2a576-default-rtdb.asia-southeast1.firebasedatabase.app/");
        fAuth           = FirebaseAuth.getInstance();
        management      = false;

        btnUser         = (Button) findViewById(R.id.buttonUser);
        btnManagement   = (Button) findViewById(R.id.buttonManagement);
        btnLogin        = (Button) findViewById(R.id.buttonLogin);
        txtRegister     = (TextView) findViewById(R.id.clickRegister);
        txtemail        = (EditText) findViewById(R.id.editTextEmail);
        txtpass         = (EditText) findViewById(R.id.editTextPassword);

        fAuth.signOut();
        btnUser.setAlpha(.5f);
        btnUser.setClickable(false);

        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtemail.setText("");
                txtpass.setText("");
                btnUser.setAlpha(.5f);
                btnUser.setClickable(false);
                btnManagement.setAlpha(1);
                btnManagement.setClickable(true);
                management = false;
            }
        });

        btnManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtemail.setText("");
                txtpass.setText("");
                btnManagement.setAlpha(.5f);
                btnManagement.setClickable(false);
                btnUser.setAlpha(1);
                btnUser.setClickable(true);
                management = true;
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email    = txtemail.getText().toString();
                String pass     = txtpass.getText().toString();
                if(email.equals("") || pass.equals("")){
                    Toast.makeText(loginActivity.this, "One or more fields are empty.", Toast.LENGTH_SHORT).show();
                }else{
                    if (management == false){
                        dbRef = fbDb.getReference("Users");
                    }else{
                        dbRef = fbDb.getReference("Management");
                    }
                    signIn(email, pass, dbRef);
                }
            }
        });

        txtRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(management == false){
                    startActivity(new Intent(loginActivity.this, userRegisterActivity.class));
                }else{
                    startActivity(new Intent(loginActivity.this, managementRegisterActivity.class));
                }
            }
        });
    }

    public void signIn(String email, String pass, DatabaseReference dbRefLogin){
        fAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String userID = fAuth.getCurrentUser().getUid();
                    dbRefLogin.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.child(userID).exists()){
                                Toast.makeText(loginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                if (management == false){
                                    startActivity(new Intent(getApplicationContext(),userHistoryActivity.class));
                                }else {
                                    SimpleDateFormat formatter  = new SimpleDateFormat("dd/MM/yyyy");
                                    Date date                   = new Date();
                                    String time                 = formatter.format(date);
                                    fbDb                        = FirebaseDatabase.getInstance("https://lookbackapp-2a576-default-rtdb.asia-southeast1.firebasedatabase.app/");
                                    fAuth                       = FirebaseAuth.getInstance();
                                    String userID               = fAuth.getCurrentUser().getUid();
                                    dbRef                       = fbDb.getReference("Management").child(userID);
                                    dbRef.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String time2 = snapshot.child("date").getValue().toString();
                                            if (!(time2.equals(time))){
                                                dbRef.child("date").setValue(time);
                                                dbRef.child("checkIns").setValue(0);
                                                dbRef.child("daysWithoutCovid").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        int value = Integer.parseInt(snapshot.getValue().toString());
                                                        value++;
                                                        dbRef.child("daysWithoutCovid").setValue(value);
                                                    }
                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                    }
                                                });
                                            }
                                        }
                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                        }
                                    });
                                    startActivity(new Intent(getApplicationContext(),managementQrCodeActivity.class));
                                }
                            }else{
                                Toast.makeText(loginActivity.this, "Invalid Account", Toast.LENGTH_SHORT).show();
                                fAuth.getInstance().signOut();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                        }
                    });
                }else{
                    Toast.makeText(loginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

}