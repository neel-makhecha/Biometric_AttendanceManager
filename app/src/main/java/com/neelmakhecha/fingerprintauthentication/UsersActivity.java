package com.neelmakhecha.fingerprintauthentication;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class UsersActivity extends AppCompatActivity {

    TextView textView;
    Cursor cursor;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        String data = "";

        textView = (TextView)findViewById(R.id.txt_dataDisplay);

        databaseHelper = new DatabaseHelper(this);

        cursor = databaseHelper.getAllFingerprintsWithUsernames();

        int counter = 0;
        while(cursor.moveToNext()){

            data = data + "ID: " + cursor.getInt(0) + " | " + "Name: " + cursor.getString(1) + "\n";
            //data = data + "Finger: " + cursor.getBlob(2) + "\n";
            counter++;

        }

        data = data + "\nCOUNTER: " + counter;
        textView.setText(data);



    }
}
