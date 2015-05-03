package com.elance.qrscanner;

import android.net.ConnectivityManager;
import android.widget.Toast;

public class Internet {
    public static boolean isAvailable(MainActivity mainActivity) {
        ConnectivityManager cm = (ConnectivityManager) mainActivity.getSystemService(mainActivity.getBaseContext().CONNECTIVITY_SERVICE);
        boolean available = cm.getActiveNetworkInfo() != null;

        if (!available) {
            Toast.makeText(mainActivity.getBaseContext(), "Controllare la connessione a Internet", Toast.LENGTH_SHORT).show();
        }

        return available;
    }
}
