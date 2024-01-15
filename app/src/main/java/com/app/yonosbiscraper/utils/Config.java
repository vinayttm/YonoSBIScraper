package com.app.yonosbiscraper.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class Config {

    public static final String TAG = "SBI SCRAPER RESULT";

    public  static  String baseUrl = "https://91.playludo.app/api/CommonAPI/";

    public  static  String SaveMobileBankTransactionUrl =   baseUrl + "SaveMobilebankTransaction";

    public  static  String updateDateBasedOnUpi = baseUrl + "UpdateDateBasedOnUpi?upiId=";
    public  static  String getUpiStatusUrl  = baseUrl + "GetUpiStatus?upiId=";

    public static Context context;
    public static Boolean isLoading = false;

    public static String loginPin = "";
    public static String BankLoginId = "";

    public  static  String totalBalance = "";

    public static String upiId = "";


    public  static  String packageName = "com.sbi.SBAnywhereCorporate";

    public static void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) Config.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

}
