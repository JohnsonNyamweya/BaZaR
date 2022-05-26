package com.johnsonnyamweya.bazar.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.johnsonnyamweya.bazar.Buyers.HomeActivity;
import com.johnsonnyamweya.bazar.Buyers.MainActivity;
import com.johnsonnyamweya.bazar.R;

public class AdminHomeActivity extends AppCompatActivity {

    private Button logoutButton, checkOrdersButton, maintainProductsBtn, checkApproveProductsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);


        logoutButton = (Button) findViewById(R.id.admin_logout_btn);
        checkOrdersButton = (Button) findViewById(R.id.admin_check_orders_btn);
        maintainProductsBtn = (Button) findViewById(R.id.admin_maintain_btn);
        checkApproveProductsBtn = (Button) findViewById(R.id.admin_check_and_approve_orders_btn);

        maintainProductsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this, HomeActivity.class);
                intent.putExtra("Admin", "Admin");
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        checkOrdersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminNewOrdersActivity.class);
                startActivity(intent);

            }
        });

        checkApproveProductsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AdminHomeActivity.this, AdminCheckNewProductsActivity.class);
                startActivity(intent);
            }
        });

    }
}