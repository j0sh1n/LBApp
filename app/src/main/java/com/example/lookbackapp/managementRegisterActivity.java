package com.example.lookbackapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class managementRegisterActivity extends AppCompatActivity {

    EditText org,add,email,pass,conf;
    String sorg,sadd,semail,spass,sconf;
    Boolean bterms, bpriv;
    CheckBox terms,priv;
    Button reg;
    Management management;
    FirebaseAuth fAuth;
    FirebaseDatabase fbDb;
    DatabaseReference dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management_register);

        org         = (EditText) findViewById(R.id.editTextManagementOrganization);
        add         = (EditText) findViewById(R.id.editTextManagementAddress);
        email       = (EditText) findViewById(R.id.editTextManagementEmail);
        pass        = (EditText) findViewById(R.id.editTextManagementPassword);
        conf        = (EditText) findViewById(R.id.editTextManagementConfirmPassword);
        terms       = (CheckBox) findViewById(R.id.checkBoxManagementTerms);
        priv        = (CheckBox) findViewById(R.id.checkBoxManagementPrivacy);
        reg         = (Button) findViewById(R.id.buttonManagementRegister);
        fbDb        = FirebaseDatabase.getInstance("https://lookbackapp-2a576-default-rtdb.asia-southeast1.firebasedatabase.app/");
        dbRef       = fbDb.getReference("Management");
        fAuth       = FirebaseAuth.getInstance();
        management  = new Management();

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sorg    = org.getText().toString();
                sadd    = add.getText().toString();
                semail  = email.getText().toString();
                spass   = pass.getText().toString();
                sconf   = conf.getText().toString();
                bterms  = terms.isChecked();
                bpriv   = priv.isChecked();
                if(checkfields(sorg, sadd, semail, spass, sconf, bterms, bpriv)){
                    register(semail, spass, sorg, sadd);
                }
            }
        });
    }

    private Boolean checkfields(String corg, String cadd, String cemail, String cpass, String cconf, Boolean cterms, Boolean cpriv){
        if(corg.equals("") | cadd.equals("") | cemail.equals("") | cpass.equals("") | cconf.equals("")){
            Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
            return false;
        }else{
            if(!(cpass.equals(cconf))){
                Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                return false;
            }else{
                if(cterms == false | cpriv == false){
                    Toast.makeText(this, "Please check terms and privacy", Toast.LENGTH_SHORT).show();
                    return false;
                }else{
                    return true;
                }
            }
        }
    }

    private void register(String remail, String rpass, String rorg, String radd){
        fAuth.createUserWithEmailAndPassword(remail, rpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String user;
                    user = fAuth.getCurrentUser().getUid();
                    SimpleDateFormat formatter  = new SimpleDateFormat("dd/MM/yyyy");
                    Date date                   = new Date();
                    String time                 = formatter.format(date);
                    management.setName(rorg);
                    management.setEmail(remail);
                    management.setPass(rpass);
                    management.setAddress(radd);
                    management.setCheckIns(0);
                    management.setDate(time);
                    management.setDaysWithoutCovid(0);
                    dbRef.child(user).setValue(management);
                    Toast.makeText(managementRegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(managementRegisterActivity.this, loginActivity.class));
                }else {
                    Toast.makeText(managementRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}