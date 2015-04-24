package com.elance.qrscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.format.Time;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";

    private AlertDialog.Builder builder;

    private GPSTracker gps;
    private double latitude;
    private double longitude;

    private TextView tvDate;
    private TextView tvTime;
    private TextView tvContentName;
    private TextView tvPhoneNumber;
    private TextView tvLatitude;
    private TextView tvLongitude;

    private Button btnSendSms;
    private Button btnSendToServer;
    private Button btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gps = new GPSTracker(MainActivity.this);
        if (!gps.canGetLocation()){
            gps.showSettingsAlert();
        }

        tvDate = (TextView) findViewById(R.id.textViewDate);
        tvTime = (TextView) findViewById(R.id.textViewTime);
        tvContentName = (TextView) findViewById(R.id.textViewContentName);
        tvPhoneNumber = (TextView) findViewById(R.id.textViewPhoneNumber);
        tvLatitude = (TextView) findViewById(R.id.textViewLatitude);
        tvLongitude = (TextView) findViewById(R.id.textViewLongitude);
        btnScan = (Button) findViewById(R.id.buttonScan);
        btnSendSms = (Button) findViewById(R.id.buttonSendSms);
        btnSendToServer = (Button) findViewById(R.id.buttonSendToServer);

        btnScan.setOnClickListener(this);
        btnSendSms.setOnClickListener(this);
        btnSendToServer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonScan:
                scanQR();
                break;
            case R.id.buttonSendSms:
                String googleMapsLink = String.format("http://maps.google.com/maps?q=%s,%s", tvLatitude.getText().toString(), tvLongitude.getText().toString());
                String messageContent = String.format("Abbiamo controllato il tuo negozio sito in %s " +
                                                      "il giorno %s " +
                                                      "alle ore %s.", googleMapsLink, tvDate.getText().toString(), tvTime.getText().toString());
                sendSMS(extractPhoneFromContent(tvContentName.getText().toString()), messageContent);
                break;
            case R.id.buttonSendToServer:

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                //date, time , mobile number, location
                Time now = new Time();
                now.setToNow();
                btnSendSms.setEnabled(true);
                btnSendToServer.setEnabled(true);

                tvDate.setText(now.monthDay+":"+(now.month+1)+":"+now.year);
                tvTime.setText(now.hour+":"+now.minute+":"+now.second);
                tvContentName.setText(intent.getStringExtra("SCAN_RESULT"));
                tvPhoneNumber.setText(getUserTelephoneNumber());

                if(gps.canGetLocation()) {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
//                    Toast.makeText(getApplicationContext(), "Your Location is -\nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                }
                tvLatitude.setText(String.valueOf(latitude));
                tvLongitude.setText(String.valueOf(longitude));
            }
        }
    }

    private void scanQR() {
        try {
            Intent intent = new Intent(ACTION_SCAN);
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    private String extractPhoneFromContent(String content){
        if (content.startsWith("SMSTO:")){
            return content.replace("SMSTO:)","").replace(":","").trim();
        }else {
            return content;
        }
    }

    private void sendSMS(final String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
        Toast.makeText(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT).show();
    }

    private String getUserTelephoneNumber(){
        String mPhoneNumber = null;
        final String[] mTempNumber = {""};

        if (builder != null)
            return "";

        try {
            TelephonyManager tMgr = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);
            mPhoneNumber = tMgr.getLine1Number();
        }catch (Exception e){
        }

        if (mPhoneNumber == null || mPhoneNumber.equals("")){
            builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter your phone number");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_PHONE);
            builder.setView(input);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mTempNumber[0] = input.getText().toString();
                    //check template
                    //save in preferences
                    tvPhoneNumber.setText(mTempNumber[0]);
                }
            });

            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                    finish();
                }
            });


            builder.show();

            return mTempNumber[0];
        }else {
            return mPhoneNumber;
        }
    }

    //alert dialog for downloadDialog
    private AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setMessage(message);
        downloadDialog.setPositiveButton(buttonYes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Uri uri = Uri.parse("market://search?q=pname:" + "com.google.zxing.client.android");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                try {
                    act.startActivity(intent);
                } catch (ActivityNotFoundException anfe) {

                }
            }
        });
        downloadDialog.setNegativeButton(buttonNo, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });

        return downloadDialog.show();
    }
}
