package com.example.lookbackapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class userRegisterActivity extends AppCompatActivity {

    EditText            txtEmail, txtPassword, txtConfirmPass, txtLName, txtFName, txtAddress, txtCompany, txtEmployment;
    RadioButton         radioMale, radioFemale;
    String              semail, spass, sconf, slname, sfname, sgender, saddress, scompany, semployment;
    Button              btnRegister;
    CheckBox            cbTerms, cbPrivacy;
    User                user;
    FirebaseAuth        fAuth;
    FirebaseDatabase    fbDb;
    DatabaseReference   dbRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        fbDb            = FirebaseDatabase.getInstance("https://lookbackapp-2a576-default-rtdb.asia-southeast1.firebasedatabase.app/");
        dbRef           = fbDb.getReference("Users");
        fAuth           = FirebaseAuth.getInstance();
        txtEmail        = (EditText) findViewById(R.id.editTextEmailAddress);
        txtPassword     = (EditText) findViewById(R.id.editTextPassword);
        txtConfirmPass  = (EditText) findViewById(R.id.editTextConfirmPassword);
        txtLName        = (EditText) findViewById(R.id.editTextLastName);
        txtFName        = (EditText) findViewById(R.id.editTextFirstName);
        radioMale       = (RadioButton) findViewById(R.id.genderMale);
        radioFemale     = (RadioButton) findViewById(R.id.genderFemale);
        txtAddress      = (EditText) findViewById(R.id.editTextAddress);
        txtCompany      = (EditText) findViewById(R.id.editTextCompany);
        txtEmployment   = (EditText) findViewById(R.id.editTextEmployment);
        cbTerms         = (CheckBox) findViewById(R.id.checkBoxTerms);
        cbPrivacy       = (CheckBox) findViewById(R.id.checkBoxPrivacy);
        btnRegister     = (Button) findViewById(R.id.buttonRegister);
        user            = new User();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                semail      = txtEmail.getText().toString();
                spass       = txtPassword.getText().toString();
                sconf       = txtConfirmPass.getText().toString();
                slname      = txtLName.getText().toString();
                sfname      = txtFName.getText().toString();
                if(radioMale.isChecked())   sgender = "Male";
                else                        sgender = "Female";
                saddress    = txtAddress.getText().toString();
                scompany    = txtCompany.getText().toString();
                semployment = txtEmployment.getText().toString();
                if(fieldChecker(semail, spass, sconf, slname, sfname, sgender, saddress, scompany, semployment, cbTerms.isChecked(), cbPrivacy.isChecked())){
                    registration(semail, spass, slname, sfname, sgender, saddress, scompany, semployment);
                }
            }
        });
    }

    private Boolean fieldChecker(String cemail, String cpass, String cconf, String clname, String cfname, String cgender, String caddress, String ccompany, String cemployment, boolean terms, boolean privacy){
        if((terms && privacy)){
            if (cemail.equals("") | cpass.equals("") | cconf.equals("") | clname.equals("") | cfname.equals("") | cgender.equals("") | caddress.equals("") | ccompany.equals("") | cemployment.equals("")){
                Toast.makeText(this, "One or more fields are empty", Toast.LENGTH_SHORT).show();
                return false;
            }else{
                if(!(cconf.equals(cpass))){
                    Toast.makeText(this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                    return false;
                }else{
                    return true;
                }
            }
        }else{
            Toast.makeText(this, "Please check Terms and Privacy", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void registration(String remail, String rpass, String rlname, String rfname, String rgender, String raddress, String rcompany, String remployment){
        fAuth.createUserWithEmailAndPassword(remail, rpass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    String uid = fAuth.getCurrentUser().getUid();
                    user.setEmail(remail);
                    user.setPass(rpass);
                    user.setLname(rlname);
                    user.setFname(rfname);
                    user.setGender(rgender);
                    user.setAddress(raddress);
                    user.setCompany(rcompany);
                    user.setEmployment(remployment);
                    user.setCovStat("NEGATIVE");
                    dbRef.child(uid).setValue(user);
                    Toast.makeText(userRegisterActivity.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getApplicationContext(),loginActivity.class));
                }else {
                    Toast.makeText(userRegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}