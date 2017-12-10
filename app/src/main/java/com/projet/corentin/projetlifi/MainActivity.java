package com.projet.corentin.projetlifi;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.SupportMapFragment;
import com.lize.oledcomm.camera_lifisdk_android.LiFiSdkManager;
import com.lize.oledcomm.camera_lifisdk_android.ILiFiPosition;
import com.lize.oledcomm.camera_lifisdk_android.V1.LiFiCamera;

@SuppressWarnings("deprecation")
public class MainActivity extends AppCompatActivity {

    private SmsBroadcastReceiver smsBroadcastReceiver = new SmsBroadcastReceiver();
    private static MainActivity inst;

    // Private managers
    private LiFiSdkManager liFiSdkManager;
    private LocationManager locationManager;

    GoogleMap mapView;


    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private static final int READ_SMS_PERMISSIONS_REQUEST = 1;
    private static final int READ_LOCATION = 1;
    TextView textView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("toto", "onCreate123");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getLocation();
        textView = (TextView) findViewById(R.id.textView);
        requestPermissions();
        liFiSdkManager = new LiFiSdkManager(this, LiFiSdkManager.CAMERA_LIB_VERSION_0_1,
                "token", "user", new ILiFiPosition() {
            @Override
            public void onLiFiPositionUpdate(String lamp) {

                textView.setText(lamp);

            }
        });

        liFiSdkManager.setLocationRequestMode(LiFiSdkManager.LOCATION_REQUEST_OFFLINE_MODE);
        liFiSdkManager.init(R.id.content_main, LiFiCamera.FRONT_CAMERA);
        liFiSdkManager.start();

        /////// SMS

        this.smsBroadcastReceiver = new SmsBroadcastReceiver();


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (liFiSdkManager != null && liFiSdkManager.isStarted()) {
            liFiSdkManager.stop();
            liFiSdkManager.release();
            liFiSdkManager = null;
        }
    }

    protected void requestPermissions() {
        getPermissionToReadSMS();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
            return;
        }
    }

    public void getPermissionToReadSMS() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.READ_SMS)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.READ_SMS},
                    READ_SMS_PERMISSIONS_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_SMS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read SMS permission granted", Toast.LENGTH_SHORT).show();
//                refreshSmsInbox();
            } else {
                Toast.makeText(this, "Read SMS permission denied", Toast.LENGTH_SHORT).show();
            }

        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public static MainActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    public void createToast(String content) {
        Log.d("toto", "toast");
        Toast.makeText(this, content, Toast.LENGTH_LONG).show();
    }

    public void handleMessage(String content) {
        if (content.equals(getString(R.string.sms_locate_msg))) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            vibrator.vibrate(2000);
            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            audioManager.playSoundEffect(AudioManager.FX_KEY_CLICK, 10);
            getLocation();
        }
    }

    public String getLocation() {
        Log.i("Location", "called");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Please allow permission!", Toast.LENGTH_SHORT).show();
            }
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    READ_LOCATION);
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    READ_LOCATION);
           }


            this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            String locationProvider = LocationManager.GPS_PROVIDER;

            Location location = locationManager.getLastKnownLocation(locationProvider);
            Log.i("Location","Position "+location.toString());
            Toast.makeText(this, location.getLatitude() + " / " + location.getLongitude() , Toast.LENGTH_LONG).show();
            GoogleMap map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            this.mapView.setLatLngBoundsForCameraTarget(location.getLatitude(),location.getLongitude());
             return location.toString();

    }

    public Location getLocation(Location location){
        return location;
    }
//    public void refreshSmsInbox() {
//        ContentResolver contentResolver = getContentResolver();
//        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
//        int indexBody = smsInboxCursor.getColumnIndex("body");
//        int indexAddress = smsInboxCursor.getColumnIndex("address");
//        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
//        arrayAdapter.clear();
//        do {
//            String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
//                    "\n" + smsInboxCursor.getString(indexBody) + "\n";
//            arrayAdapter.add(str);
//        } while (smsInboxCursor.moveToNext());
//    }
}
