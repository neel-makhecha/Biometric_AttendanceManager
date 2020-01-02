package com.neelmakhecha.fingerprintauthentication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class HomeActivity extends AppCompatActivity {

    Button addUserButton;
    Button showAllUsersButton;
    Button takeAttendanceButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        addUserButton = (Button)findViewById(R.id.btn_addNewUser);
        showAllUsersButton = (Button)findViewById(R.id.btn_showAllUsers);
        takeAttendanceButton = (Button)findViewById(R.id.btn_takeAttendance);

        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),AddUserActivity.class);
                startActivity(intent);
            }
        });

        showAllUsersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),UsersActivity.class);
                startActivity(intent);
            }
        });

        takeAttendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),AttendanceActivity.class);
                startActivity(intent);
            }
        });

    }
}
