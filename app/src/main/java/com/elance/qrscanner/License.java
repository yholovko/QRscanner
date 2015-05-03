package com.elance.qrscanner;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class License {
    public static void check(final MainActivity mainActivity){
        SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
        boolean isActivated = sharedPref.getBoolean(Constants.LICENSE, false);

        if (isActivated)
            return;

        final EditText input = new EditText(mainActivity);
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        final AlertDialog builder = new AlertDialog.Builder(mainActivity)
            .setTitle("Inserisci il codice di licenza")
            .setCancelable(false)
            .setView(input)
            .setPositiveButton("Verificare", null)
            .create();

        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button b = builder.getButton(AlertDialog.BUTTON_POSITIVE);

                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            String response = new AsyncPostRequest(Constants.SERVER_URL + Constants.CHECK_LICENSE, CodeRequestManager.checkLicense(input.getText().toString()))
                                    .execute()
                                    .get(29, TimeUnit.SECONDS);

                            JSONObject jsonResponse = new JSONObject(response);

                            int result = jsonResponse.getInt("result");

                            if (result == 1) {
                                SharedPreferences sharedPref = mainActivity.getPreferences(Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putBoolean(Constants.LICENSE, true);
                                editor.commit();
                                builder.dismiss();
                            } else {
                                Toast.makeText(mainActivity.getBaseContext(), "Scorretto", Toast.LENGTH_SHORT).show();
                            }
                        }catch (InterruptedException | ExecutionException | JSONException | TimeoutException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        builder.show();
    }
}