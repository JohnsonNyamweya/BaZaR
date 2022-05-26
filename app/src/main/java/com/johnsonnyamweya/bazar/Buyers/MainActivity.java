package com.johnsonnyamweya.bazar.Buyers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.johnsonnyamweya.bazar.Model.Users;
import com.johnsonnyamweya.bazar.Prevalent.Prevalent;
import com.johnsonnyamweya.bazar.R;
import com.johnsonnyamweya.bazar.Sellers.SellerLoginActivity;
import com.johnsonnyamweya.bazar.Sellers.SellerRegistrationActivity;

import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity {

    private Button joinNowButton, loginButton;
    private ProgressDialog loadingBar;
    private TextView sellerBegin;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        joinNowButton = (Button) findViewById(R.id.main_join_now_btn);
        loginButton = (Button) findViewById(R.id.main_login_btn);
        sellerBegin = (TextView) findViewById(R.id.seller_begin);
        loadingBar = new ProgressDialog(this);

        Paper.init(this);

        sellerBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SellerRegistrationActivity.class);
                startActivity(intent);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        joinNowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        String userPhoneKey = Paper.book().read(Prevalent.userPhoneKey);
        String userPasswordKey = Paper.book().read(Prevalent.userPasswordKey);

        if (userPhoneKey != "" && userPasswordKey != ""){
           if (!TextUtils.isEmpty(userPhoneKey) && !TextUtils.isEmpty(userPasswordKey)){

               allowAccess(userPhoneKey, userPasswordKey);

               loadingBar.setTitle("Already Logged in");
               loadingBar.setMessage("Please wait...");
               loadingBar.setCanceledOnTouchOutside(false);
               loadingBar.show();

           }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null){
            Intent intent = new Intent(MainActivity.this, SellerLoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
//        else{
//
//        }
    }

    private void allowAccess(final String phone, final String password) {

        final DatabaseReference rootRef;
        rootRef = FirebaseDatabase.getInstance().getReference();

        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.child("Users").child(phone).exists()){

//                    retrieve phoneNumber and password
                    Users userData = snapshot.child("Users").child(phone).getValue(Users.class);

                    if (userData.getPhone().equals(phone)){

                        if (userData.getPassword().equals(password)){
                            Toast.makeText(MainActivity.this,
                                    "Please wait, you are already logged in", Toast.LENGTH_SHORT).show();

                            loadingBar.dismiss();

                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);

                            Prevalent.currentOnlineUser = userData;
                            startActivity(intent);
                        }
                        else{
                            loadingBar.dismiss();
                            Toast.makeText(MainActivity.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                        }
                    }

                }
                else{
                    Toast.makeText(MainActivity.this,
                            "Account with this " + phone +"does not exist", Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                    Toast.makeText(MainActivity.this,
                            "You need to create a new account", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}