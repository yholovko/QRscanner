package com.elance.qrscanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.format.Time;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final String ACTION_SCAN = "com.google.zxing.client.android.SCAN";
    private AlertDialog.Builder builder;
    private TextView tvDate;
    private TextView tvTime;
    private TextView tvContentName;
    private TextView tvPhoneNumber;
    private TextView tvLatitude;
    private TextView tvLongitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDate = (TextView) findViewById(R.id.textViewDate);
        tvTime = (TextView) findViewById(R.id.textViewTime);
        tvContentName = (TextView) findViewById(R.id.textViewContentName);
        tvPhoneNumber = (TextView) findViewById(R.id.textViewPhoneNumber);
        tvLatitude = (TextView) findViewById(R.id.textViewLatitude);
        tvLongitude = (TextView) findViewById(R.id.textViewLongitude);

        scanQR();
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

                tvDate.setText(now.monthDay+":"+now.month+":"+now.year);
                tvTime.setText(now.hour+":"+now.minute+":"+now.second);
                tvContentName.setText(intent.getStringExtra("SCAN_RESULT"));
                tvPhoneNumber.setText(getUserTelephoneNumber());
                tvLatitude.setText("");
                tvLongitude.setText("");
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
                }
            });


            builder.show();

            return mTempNumber[0];
        }else {
            return mPhoneNumber;
        }
    }

    //alert dialog for downloadDialog
    private static AlertDialog showDialog(final Activity act, CharSequence title, CharSequence message, CharSequence buttonYes, CharSequence buttonNo) {
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

            }
        });

        return downloadDialog.show();
    }
}
