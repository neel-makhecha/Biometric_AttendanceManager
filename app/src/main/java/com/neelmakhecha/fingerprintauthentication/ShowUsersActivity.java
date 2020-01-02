package com.neelmakhecha.fingerprintauthentication;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mantra.mfs100.MFS100;
import com.mantra.mfs100.MFS100Event;

public class ShowUsersActivity extends AppCompatActivity implements MFS100Event {

    MFS100 mfs100 = new MFS100(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_users);

        mfs100.SetApplicationContext(this);

        int result = mfs100.Init();

    }

    @Override
    public void OnDeviceAttached(int i, int i1, boolean b) {

    }

    @Override
    public void OnDeviceDetached() {

    }

    @Override
    public void OnHostCheckFailed(String s) {

    }
}
