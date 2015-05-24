package com.elance.qrscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.Time;
import android.util.Pair;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MainActivity extends Activity implements View.OnClickListener {
    private GPSTracker gps;
    private double latitude;
    private double longitude;

    private Pair customerIdCompanyName;

    private TextView tvDate;
    private TextView tvTime;
    private TextView tvContentName;
    private TextView tvLatitude;
    private TextView tvLongitude;
    private TextView tvAddress;

    private Button btnSendSms;
    private Button btnSendToServer;
    private Button btnScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        License.check(this);

        gps = new GPSTracker(MainActivity.this, this);
        if (!gps.canGetLocation()) {
            gps.showSettingsAlert();
        }

        tvDate = (TextView) findViewById(R.id.textViewDate);
        tvTime = (TextView) findViewById(R.id.textViewTime);
        tvContentName = (TextView) findViewById(R.id.textViewContentName);
        tvLatitude = (TextView) findViewById(R.id.textViewLatitude);
        tvLongitude = (TextView) findViewById(R.id.textViewLongitude);
        tvAddress = (TextView) findViewById(R.id.textViewAddress);
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
                String messageContent = String.format("Ho controllato il tuo negozio sito in %s il giorno %s alle ore %s.Cordiali saluti %s",
                        googleMapsLink, tvDate.getText().toString(), tvTime.getText().toString(), customerIdCompanyName.second.toString());
                sendSMS(tvContentName.getText().toString(), messageContent);
                btnSendSms.setEnabled(false);
                break;
            case R.id.buttonSendToServer:
                try {
                    if (Internet.isAvailable(this)) {
                        String response = new AsyncPostRequest(Constants.SERVER_URL + Constants.ADD_DATA, CodeRequestManager.addData(tvDate.getText().toString(), tvTime.getText().toString(),
                                tvLatitude.getText().toString(), tvLongitude.getText().toString(), tvAddress.getText().toString(), customerIdCompanyName.first.toString()))
                                .execute()
                                .get(29, TimeUnit.SECONDS);

                        JSONObject jsonResponse = new JSONObject(response);
                        int result = jsonResponse.getInt("result");
                        if (result == 1) {
                            Toast.makeText(getBaseContext(), "Informazioni inviate", Toast.LENGTH_SHORT).show();
                            btnSendToServer.setEnabled(false);
                        }
                    }
                } catch (InterruptedException | ExecutionException | JSONException | TimeoutException e) {
                    e.printStackTrace();
                }

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
                //date, time , latitude, longitude, id_customer
                Time now = new Time();
                now.setToNow();
                btnSendSms.setEnabled(true);
                btnSendToServer.setEnabled(true);

                tvDate.setText(now.year + ":" + (now.month + 1) + ":" + now.monthDay);
                tvTime.setText(now.hour + ":" + now.minute + ":" + now.second);
                tvContentName.setText(extractPhoneFromContent(intent.getStringExtra("SCAN_RESULT")));

                if (gps.canGetLocation()) {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                }

                tvLatitude.setText(String.valueOf(latitude));
                tvLongitude.setText(String.valueOf(longitude));
                tvAddress.setText(gps.getStreetName());

                try {
                    if (Internet.isAvailable(this)) {
                        String response = new AsyncPostRequest(Constants.SERVER_URL + Constants.GET_CUSTOMER, CodeRequestManager.getCustomer(tvContentName.getText().toString()))
                                .execute()
                                .get(29, TimeUnit.SECONDS);

                        JSONObject jsonResponse = new JSONObject(response);
                        customerIdCompanyName = new Pair(jsonResponse.getString("id"), jsonResponse.getString("name"));
                    }
                } catch (InterruptedException | ExecutionException | TimeoutException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void scanQR() {
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
            startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException anfe) {
            showDialog(MainActivity.this, "No Scanner Found", "Download a scanner code activity?", "Yes", "No").show();
        }
    }

    private String extractPhoneFromContent(String content) {
        if (content.startsWith("SMSTO:")) {
            return content.replace("SMSTO:", "").replace(":", "").trim();
        } else {
            return content;
        }
    }

    private void sendSMS(final String phoneNumber, String message) {
        String SENT = "SMS_SENT";
        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);

        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(phoneNumber, null, message, sentPI, null);
        Toast.makeText(getBaseContext(), "SMS inviato", Toast.LENGTH_SHORT).show();
    }

    private AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(act);
        downloadDialog.setTitle(title);
        downloadDialog.setCancelable(false);
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