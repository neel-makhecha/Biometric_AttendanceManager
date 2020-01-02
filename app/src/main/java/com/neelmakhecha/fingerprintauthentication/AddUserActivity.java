package com.neelmakhecha.fingerprintauthentication;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mantra.mfs100.DeviceInfo;
import com.mantra.mfs100.FingerData;
import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class AddUserActivity extends AppCompatActivity implements MFS100Event {


    MFS100 mfs100 = new MFS100(this);
    DatabaseHelper databaseHelper;

    EditText usernameEditText;
    ImageView profilePhotoImageView;
    TextView scanResultTextView;

    Button scanFingerprintButton;
    Button takePhotoButton;
    Button saveUserButton;

    String scanResult = "";
    int scanQuality = 0;
    byte[] fingerprintBinary;
    byte[] profilePhotoBinary;
    boolean photoTaken = false;

    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);

        mfs100.SetApplicationContext(this);
        databaseHelper = new DatabaseHelper(getApplicationContext());

        profilePhotoImageView = (ImageView)findViewById(R.id.img_fingerprintOfNewUser);
        scanFingerprintButton = (Button)findViewById(R.id.btn_scanFingerprintForNewUser);
        usernameEditText = (EditText)findViewById(R.id.edtxt_username);
        saveUserButton = (Button)findViewById(R.id.btn_saveUser);
        scanResultTextView = (TextView)findViewById(R.id.txt_scanResult);
        takePhotoButton = (Button)findViewById(R.id.btn_takePhoto);

        initialiseSensor();

        scanFingerprintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scanResult = "";

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try{
                            FingerData fingerData = new FingerData();

                            Log.d("mfs100","Starting Auto Capture...");

                            int result = mfs100.AutoCapture(fingerData,0,true);

                            if(result != 0){
                                //Log.d("mfs100",mfs100.GetErrorMsg(result));
                                scanResult = scanResult + "Result Non ZERO ERROR: " + mfs100.GetErrorMsg(result);
                            }else{
                                Log.d("mfs100","Fingerprint Scanned\n Quality: " + fingerData.Quality() + "\nNFIQ: " + fingerData.Nfiq());
                                scanResult = scanResult + "Fingerprint Scanned\n Quality: " + fingerData.Quality() + "\nNFIQ: " + fingerData.Nfiq();
                                scanQuality = fingerData.Quality();
                                fingerprintBinary = fingerData.ISOTemplate();

                                Bitmap bitmap = BitmapFactory.decodeByteArray(fingerData.FingerImage(), 0, fingerData.FingerImage().length);
                                profilePhotoImageView.setImageBitmap(bitmap);
                            }



                        }catch (Exception exception){
                            Log.d("mfs100","Exception occured while scanning fingerprint.\nLocalized Description: " + exception.getLocalizedMessage());
                            scanResult = "EXCEPTION OCCURED: " + exception.getLocalizedMessage();
                        }

                        Log.d("mfs100_RESULT",scanResult);

                        if(scanQuality < 80){
                            scanResultTextView.setText("Scan Quality is: " + scanQuality + ". It is advisable to re-scan.") ;
                        }else{
                            scanResultTextView.setText("Fingerprint scanned with quality: " + scanQuality);
                        }

                    }
                }).start();



            }


        });

        saveUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String username = usernameEditText.getText().toString();

                if(username.matches("")){

                    Toast.makeText(getApplicationContext(),"Username is empty",Toast.LENGTH_SHORT).show();

                }else if(!photoTaken){

                    Toast.makeText(getApplicationContext(),"Profile photo not taken.",Toast.LENGTH_SHORT).show();

                }else{

                    boolean result = databaseHelper.addNewFingerprint(username,profilePhotoBinary,fingerprintBinary);

                    if(result){
                        Toast.makeText(getApplicationContext(),"New user saved with it's fingerprint successfully.",Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(getApplicationContext(),"Could not save new user into the database",Toast.LENGTH_SHORT).show();
                    }

                }

            }
        });

        takePhotoButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {


                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else
                {
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }


            }
        });


    }

    private void displayFinger(final Bitmap bitmap){
        profilePhotoImageView.post(new Runnable() {
            @Override
            public void run() {
                profilePhotoImageView.setImageBitmap(bitmap);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE)
        {
            //if (grantResults[0] == PackageManager.PERMISSION_GRANTED) //Check for camera permissions
            if(true)
            {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
            else
            {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK)
        {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            profilePhotoImageView.setImageBitmap(photo);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            profilePhotoBinary = stream.toByteArray();
            photoTaken = true;
            //photo.recycle();
        }

    }

}
