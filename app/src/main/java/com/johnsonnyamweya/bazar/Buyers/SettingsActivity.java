package com.johnsonnyamweya.bazar.Buyers;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.johnsonnyamweya.bazar.R;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private String phone, name, address;
    private Button securityQuestionsBtn;
    private CircleImageView profileImageView;
    private TextView  closeTextBtn, updateTextButton, changeProfileTxt;
    private EditText fullNameEditText, userPhoneEditText, addressEditText;
    private static final int galleryPick = 1;
    private Uri imageUri;

    private String downloadImageUrl;
    private StorageReference storageProfilePictureRef;
    private DatabaseReference userAccountRef;

    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageProfilePictureRef = FirebaseStorage.getInstance().getReference().child("Profile pictures");
        userAccountRef = FirebaseDatabase.getInstance().getReference().child("Users");

        loadingBar = new ProgressDialog(this);

        profileImageView = (CircleImageView) findViewById(R.id.settings_profile_image);
        fullNameEditText = (EditText) findViewById(R.id.settings_full_name);
        userPhoneEditText = (EditText) findViewById(R.id.settings_phone_number);
        addressEditText = (EditText) findViewById(R.id.settings_address);
        closeTextBtn = (TextView) findViewById(R.id.close_settings_btn);
        changeProfileTxt = (TextView) findViewById(R.id.profile_image_change);
        updateTextButton = (TextView) findViewById(R.id.update_account_settings_btn);
        securityQuestionsBtn = findViewById(R.id.security_questions_btn);

        securityQuestionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SettingsActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check", "settings");
                startActivity(intent);
            }
        });


        changeProfileTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePhoto();
            }
        });

        updateTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateUserInformation();
            }
        });

        closeTextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

    }

    private void choosePhoto() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, galleryPick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPick && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
        }
    }

    private void  validateUserInformation(){
        phone = userPhoneEditText.getText().toString();
        name = fullNameEditText.getText().toString();
        address = addressEditText.getText().toString();

        Glide.with(profileImageView.getContext()).load(downloadImageUrl)
                .placeholder(com.firebase.ui.database.R.drawable.common_google_signin_btn_icon_dark)
                .into(profileImageView);

        if (imageUri == null){
            Toast.makeText(this, "User profile image is mandatory", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Please write your phone number", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please write your name", Toast.LENGTH_SHORT).show();
        }

        else if (TextUtils.isEmpty(address)){
            Toast.makeText(this, "Please write your address", Toast.LENGTH_SHORT).show();
        }

        else{
            storeUserInformation();
        }

    }

    private void storeUserInformation() {

        loadingBar.setTitle("Update Profile");
        loadingBar.setMessage("Please wait, while we are updating your profile information");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        StorageReference filePath = storageProfilePictureRef.child(imageUri + ".jpg");
        final UploadTask uploadTask = filePath.putFile(imageUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(SettingsActivity.this, " User profile image uploaded successfully...", Toast.LENGTH_SHORT).show();
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }

                        downloadImageUrl =  filePath.getDownloadUrl().toString();
                        return  filePath.getDownloadUrl();

                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful()){

                            downloadImageUrl = task.getResult().toString();

                            Toast.makeText(SettingsActivity.this, "Got the profile image Url successfully..",
                                    Toast.LENGTH_SHORT).show();
                            saveUserProfileInfoToDatabase();
                        }
                    }
                });
            }
        });

    }

    private void saveUserProfileInfoToDatabase() {
        HashMap<String, Object> userDataMap = new HashMap<>();
        userDataMap.put("name", name);
        userDataMap.put("phone", phone);
        userDataMap.put("address", address);
        userDataMap.put("image", downloadImageUrl);

        userAccountRef.child(phone).updateChildren(userDataMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    loadingBar.dismiss();
                    startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                    Toast.makeText(SettingsActivity.this, "User Account updated successfully...",
                            Toast.LENGTH_SHORT).show();

                }
                else{
                    loadingBar.dismiss();
                    String message = task.getException().toString();
                    Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}