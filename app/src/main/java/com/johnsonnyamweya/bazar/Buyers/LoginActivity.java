package com.johnsonnyamweya.bazar.Buyers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.johnsonnyamweya.bazar.Admin.AdminHomeActivity;
import com.johnsonnyamweya.bazar.Model.Users;
import com.johnsonnyamweya.bazar.Prevalent.Prevalent;
import com.johnsonnyamweya.bazar.R;
import com.rey.material.widget.CheckBox;

import io.paperdb.Paper;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText inputPhoneNumber, inputPassword;
    private ProgressDialog loadingBar;

    public String parentDbName = "Users";

    private CheckBox checkBoxRememberMe;

    private TextView adminLink, notAdminLink, forgetPasswordLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = (Button) findViewById(R.id.login_btn);
        inputPhoneNumber = (EditText) findViewById(R.id.login_phone_number_input);
        inputPassword = (EditText) findViewById(R.id.login_password_input);
        forgetPasswordLink = findViewById(R.id.forget_password_link);

        loadingBar = new ProgressDialog(this);

        adminLink = (TextView) findViewById(R.id.admin_panel_link);
        notAdminLink = (TextView) findViewById(R.id.not_admin_panel_link);

        checkBoxRememberMe = (CheckBox) findViewById(R.id.remember_me_checkbox);

        Paper.init(this);

        forgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check", "login");
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUser();
            }
        });

        adminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.setText("Login Admin");
                adminLink.setVisibility(View.INVISIBLE);
                notAdminLink.setVisibility(View.VISIBLE);
                parentDbName = "Admins";
            }
        });

        notAdminLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginButton.setText("Login");
                adminLink.setVisibility(View.VISIBLE);
                notAdminLink.setVisibility(View.INVISIBLE);
                parentDbName = "Users";
            }
        });

    }

    private void loginUser() {
        String phone = inputPhoneNumber.getText().toString();
        String password = inputPassword.getText().toString();

         if (TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Please write your phone number...", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please write your password...", Toast.LENGTH_SHORT).show();
        }
        else{
             loadingBar.setTitle("Login Account");
             loadingBar.setMessage("Please wait, while we are checking credentials");
             loadingBar.setCanceledOnTouchOutside(false);
             loadingBar.show();

             allowAccessToAccount(phone, password);

         }
    }

    private void allowAccessToAccount(String phone, String password) {

//        before we allowAccess, store values to the variables(keys) in Prevalent class

        if (checkBoxRememberMe.isChecked()){
            Paper.book().write(Prevalent.userPhoneKey, phone);
            Paper.book().write(Prevalent.userPasswordKey, password);
        }

        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child(parentDbName).child(phone).exists()){

//                    retrieve phoneNumber and password, and pass it Users class

                    Users userData = snapshot.child(parentDbName).child(phone).getValue(Users.class);

                    if (userData.getPhone().equals(phone)){

                        if (userData.getPassword().equals(password)){

                            if (parentDbName.equals("Admins")){
                                Toast.makeText(LoginActivity.this,
                                        "Welcome Admin, you are logged in successfully", Toast.LENGTH_SHORT).show();

                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                                startActivity(intent);

                            }else if (parentDbName.equals("Users")){
                                Toast.makeText(LoginActivity.this,
                                        "logged in successfully", Toast.LENGTH_SHORT).show();

                                loadingBar.dismiss();

                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);

                                Prevalent.currentOnlineUser = userData;
                                startActivity(intent);
                            }

                        }
                        else{
                            loadingBar.dismiss();
                            Toast.makeText(LoginActivity.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
                else{
                    Toast.makeText(LoginActivity.this,
                            "Account with this " + phone +"does not exist", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(LoginActivity.this,
                            "You need to create a new account", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}