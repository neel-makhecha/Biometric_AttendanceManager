package com.neelmakhecha.fingerprintauthentication;

import android.app.AlertDialog;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mantra.mfs100.DeviceInfo;
import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

import org.w3c.dom.Text;

import java.util.Currency;

public class AttendanceActivity extends AppCompatActivity implements MFS100Event {

    TextView usernameTextView;
    Button scanFingerprintButton;
    ImageView profilePhoto;

    byte[] fingerprintBinary;

    MFS100 mfs100 = new MFS100(this);

    DatabaseHelper databaseHelper;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        databaseHelper = new DatabaseHelper(this);

        mfs100.SetApplicationContext(this);
        initialiseSensor();

        usernameTextView = (TextView)findViewById(R.id.txt_usernameRecognised);
        scanFingerprintButton = (Button)findViewById(R.id.btn_scanFingerprintForAttendance);
        profilePhoto = (ImageView)findViewById(R.id.img_profilePhoto);

        scanFingerprintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try{
                            FingerData fingerData = new FingerData();

                            Log.d("mfs100","Starting Auto Capture...");

                            usernameTextView.setText("Ready to Scan. Rest your finger on the scanner");

                            int result = mfs100.AutoCapture(fingerData,0,true);

                            if(result != 0){
                                Log.d("mfs100",mfs100.GetErrorMsg(result));
                                usernameTextView.setText("" + mfs100.GetErrorMsg(result));
                            }else{
                                Log.d("mfs100","Fingerprint Scanned\n Quality: " + fingerData.Quality() + "\nNFIQ: " + fingerData.Nfiq());

                                if(fingerData.Quality() > 75){

                                    fingerprintBinary = fingerData.ISOTemplate();

                                    if(fingerprintBinary != null){

                                        boolean fingerprintMatched = false;
                                        cursor = databaseHelper.getAllFingerprintsWithUsernames();
                                        while(cursor.moveToNext() && !fingerprintMatched){

                                            int score = mfs100.MatchISO(fingerprintBinary,cursor.getBlob(3));
                                            if(score > 75){
                                                usernameTextView.setText("User Detected: " + cursor.getString(1) + "\n" + "User ID: " + cursor.getInt(0));

                                                byte[] bytes = cursor.getBlob(2);
                                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                                                profilePhoto.setImageBitmap(bitmap);

                                                fingerprintMatched = true;
                                                mfs100.StopAutoCapture();
                                            }

                                        }

                                        if(fingerprintMatched == false){
                                            usernameTextView.setText("No Match Found");
                                            mfs100.StopAutoCapture();
                                        }

                                    }{
                                        if(fingerprintBinary == null)
                                            usernameTextView.setText("fingerprintBinary is null!");
                                            mfs100.StopAutoCapture();
                                    }

                                }else{
                                    usernameTextView.setText("Couldn't Get a proper print. Try Again.");
                                    mfs100.StopAutoCapture();
                                }

                            }



                        }catch (Exception exception){
                            Log.d("mfs100","Exception occured while scanning fingerprint. If crashed, exception occured while display Toast.");
                            usernameTextView.setText("Couldn't Start the scanner at this time. Please try again.");
                        }




                    }
                }).start();



            }
        });


    }

    @Override
    public void OnDeviceAttached(int i, int i1, boolean b) {

        Toast.makeText(getApplicationContext(), "Scanner Attached.", Toast.LENGTH_SHORT).show();
        showSensorDetails();

    }

    @Override
    public void OnDeviceDetached() {

        Toast.makeText(getApplicationContext(), "Scanner Detached.", Toast.LENGTH_SHORT).show();


    }

    @Override
    public void OnHostCheckFailed(String s) {

    }

    @Override
    public void onBackPressed() {
        mfs100.StopAutoCapture();
        super.onBackPressed();
    }

    private void initialiseSensor(){

        String sensorInformation = "";

        if(mfs100.IsConnected()){
            Toast.makeText(this,"Fingerprint scanner is connected.",Toast.LENGTH_SHORT).show();

            if(mfs100.LoadFirmware() == 0){
                Toast.makeText(this,"Fingerprint scanner firmware loaded successfully.",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Fingerprint scanner firmware NOT loaded.",Toast.LENGTH_SHORT).show();
            }

            int result = mfs100.Init();

            if(result != 0){
                Toast.makeText(this,"Error occured while initialsing the sensor.",Toast.LENGTH_SHORT).show();
                Log.d("mfs100",mfs100.GetErrorMsg(result));
            }else{
                DeviceInfo deviceInfo = mfs100.GetDeviceInfo();

                if(deviceInfo != null){
                    String information = "SERIAL No.: " + deviceInfo.SerialNo() + "Make: " + deviceInfo.Make() + "Model: " + deviceInfo.Model();
                    Log.d("mfs100","Device Information\n" + information);
                    sensorInformation = sensorInformation + information;
                }else{
                    Log.d("mfs100","Error occured while retriving device information");
                    sensorInformation = "Error occured while retriving device information";
                }
            }

            String certificate = mfs100.GetCertification();

            if(certificate != null) {
                Log.d("mfs100", "Certificate: " + certificate);
                sensorInformation = sensorInformation + "\nCertificate: " + certificate;
            }else{
                Log.d("mfs100","No Certificate Found. MFS100 not initialised.");
                sensorInformation = sensorInformation + "\nNo Certificate Found. MFS100 not initialised";
            }

            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Connected Sensor Information");
            alert.setMessage(sensorInformation);
            alert.setPositiveButton("Okay",null);
            alert.show();

        }else{
            Toast.makeText(this,"Fingerprint scanner is NOT connected.",Toast.LENGTH_LONG).show();
        }

    }

    private void showSensorDetails(){

        String sensorInformation = "";

        mfs100.Init();

        DeviceInfo deviceInfo = mfs100.GetDeviceInfo();

        if(deviceInfo != null){
            String information = "SERIAL No.: " + deviceInfo.SerialNo() + "Make: " + deviceInfo.Make() + "Model: " + deviceInfo.Model();
            Log.d("mfs100","Device Information\n" + information);
            sensorInformation = sensorInformation + information;
        }else{
            Log.d("mfs100","Error occured while retriving device information");
            sensorInformation = "Error occured while retriving device information";
        }

        String certificate = mfs100.GetCertification();

        if(certificate != null) {
            Log.d("mfs100", "Certificate: " + certificate);
            sensorInformation = sensorInformation + "\nCertificate: " + certificate;
        }else{
            Log.d("mfs100","No Certificate Found. MFS100 not initialised.");
            sensorInformation = sensorInformation + "\nNo Certificate Found. MFS100 not initialised";
        }

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Sensor Information | MFS100");
        alert.setMessage(sensorInformation);
        alert.setPositiveButton("Okay",null);
        alert.show();

    }
}
