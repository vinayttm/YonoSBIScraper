package com.app.yonosbiscraper.utils;

import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.yonosbiscraper.api.ApiCaller;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DataFilter {

    public static void convertToJson(AccessibilityNodeInfo miniStatement) {
        String modelNumber = DeviceInfo.getModelNumber();
        String secureId = DeviceInfo.generateSecureId(Config.context);
        JSONArray jsonArray = new JSONArray();
        List<String> unfilterList = AccessibilityMethod.listAllTextsInActiveWindow(miniStatement);
        Log.d("unfilterList", unfilterList.toString());
        unfilterList.removeIf(String::isEmpty);
        List<String> stringsToRemove = Arrays.asList("Account Summary", "Account Information", "Mini Statement");
        unfilterList.removeAll(stringsToRemove);
        List<String> filterList = unfilterList;
        System.out.println("Original List: " + filterList);
        for (int i = 0; i < filterList.size(); i += 3) {
            JSONObject entry = new JSONObject();
            String date = filterList.get(i);
            if (isDate(date)) {
                String description = filterList.get(i + 1);
                String amount = filterList.get(i + 2);

                try {
                    if (amount.contains("(Cr)")) {
                        amount = amount.replace("(Cr)", "");
                        amount = amount.replace(" ", "");
                    }
                    if (amount.contains("(Dr)")) {
                        amount = amount.replace("(Dr)", "");
                        amount = amount.replaceAll(",","");
                        amount = amount.replace(" ", "");
                        amount = "-" + amount;
                    }
                    if (amount.contains("Rs.") || amount.contains("Rs")) {
                        amount = amount.replace("Rs.", "");
                        amount = amount.replaceAll(",","");
                    }
                    entry.put("Description", extractUTRFromDesc(description));
                    entry.put("UPIId", getUPIId(description));
                    entry.put("CreatedDate", convertDateFormat(date));
                    entry.put("Amount", amount.trim());
                    entry.put("RefNumber", extractUTRFromDesc(description));
                    entry.put("BankName", "SBI Bank-" + Config.BankLoginId);
                    entry.put("BankLoginId", Config.BankLoginId);
                    entry.put("DeviceInfo", modelNumber + " " + secureId);
                    entry.put("AccountBalance", Config.totalBalance);

                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                jsonArray.put(entry);
            } else {
                Log.d(Config.TAG, "Date not found object");
            }

            Log.d("Data", jsonArray.toString());
            if(jsonArray.length() > 0)
            {
                JSONObject finalJson = new JSONObject();
                Log.d("Data",jsonArray.toString());
                try {
                    finalJson.put("Result", AES.encrypt(jsonArray.toString()));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
              sendTransactionData(finalJson.toString());
            }
        }
    }


    public static String extractUTRFromDesc(String description) {
        try {
            String[] split = description.split("/");
            String value = null;
            value = Arrays.stream(split).filter(x -> x.length() == 12).findFirst().orElse(null);
            if (value != null) {
                return value + " " + description;
            }
            return description;
        } catch (Exception e) {
            return description;
        }
    }

    public static String getUPIId(String description) {
        try {
            if (!description.contains("@"))
                return "";
            String[] split = description.split("/");
            String value = null;
            value = Arrays.stream(split).filter(x -> x.contains("@")).findFirst().orElse(null);
            return value != null ? value : "";
        } catch (Exception ex) {
            Log.d("Exception", ex.getMessage());
            return "";
        }
    }




    private static void sendTransactionData(String data) {
        ApiCaller apiCaller = new ApiCaller();
        if (apiCaller.getUpiStatus(Config.getUpiStatusUrl + Config.upiId)) {
            Config.isLoading = true;
            apiCaller.postData(Config.SaveMobileBankTransactionUrl, data);
            updateDateBasedOnUpi();
        } else {
            Config.isLoading = false;
            Log.d("Failed to called api because of upi status off", "in Active status");
        }
    }

    private static void updateDateBasedOnUpi() {
        System.out.println("Const.upiId" + Config.upiId);
        ApiCaller apiCaller = new ApiCaller();
        apiCaller.fetchData(Config.updateDateBasedOnUpi + Config.upiId);
        Config.isLoading = false;
    }



    public static boolean isDate(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        dateFormat.setLenient(false);
        try {
            Date parsedDate = dateFormat.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static String convertDateFormat(String inputDateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MMM-yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/M/yyyy");
            Date date = inputFormat.parse(inputDateString);
            return outputFormat.format(date);

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
