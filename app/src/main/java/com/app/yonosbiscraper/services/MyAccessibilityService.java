package com.app.yonosbiscraper.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Bundle;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;


import com.app.yonosbiscraper.localstorage.SharedPreferencesManager;
import com.app.yonosbiscraper.utils.AccessibilityMethod;
import com.app.yonosbiscraper.utils.Const;
import com.app.yonosbiscraper.utils.DataFilter;

import java.util.List;

public class MyAccessibilityService extends AccessibilityService {


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        CharSequence packageNameCharSeq = event.getPackageName();
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (packageNameCharSeq != null) {
            String packageName = packageNameCharSeq.toString();
            Log.d("MyAccessibilityService", "Package Name: " + packageName);
        } else {
            Log.e("MyAccessibilityService", "Package Name is null");
        }
    }

    private void warningDialog(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<String> check = AccessibilityMethod.getAllTextInNode(rootNode);
            if (check.contains("I Accept the Risk and provide my consent to proceed")) {
                AccessibilityNodeInfo inputTextField = AccessibilityMethod.findNodeWithTextRecursive(rootNode, "I Accept the Risk and provide my consent to proceed");
                inputTextField.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void serverDialog(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<String> check = AccessibilityMethod.getAllTextInNode(rootNode);
            if (check.contains("Sorry! We are unable to service your request at this time. Please try again.")) {
                AccessibilityNodeInfo inputTextField = AccessibilityMethod.findNodeWithTextRecursive(rootNode, "OK");
                boolean isClicked = inputTextField.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                if (isClicked) {
                    Const.processToClickMiniStatement = false;
                    Const.isCompleteStatement = false;
                    Const.isLoginProcess = false;
                }
            } else {
                Log.d(Const.TAG, "Warning Popup not found !");
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void loginUser(AccessibilityNodeInfo rootNode) {
        if (Const.isLoginProcess) {
            return;
        }
        List<String> loginScreenText = AccessibilityMethod.getAllTextInNode(rootNode);
        Log.d("loginScreenText", loginScreenText.toString());
        for (String text : loginScreenText) {
            if (text.contains("Open Saving Account")) {
                AccessibilityNodeInfo inputEditText = AccessibilityMethod.findEditTextNode(rootNode);
                if (inputEditText != null) {
                    String pinValue = Const.pinText;
                    if (pinValue.contains("") || pinValue.isEmpty()) {
                        SharedPreferencesManager sharedPreferencesManager = SharedPreferencesManager.getInstance(Const.context);
                        pinValue = sharedPreferencesManager.getStringValue("pinText");
                        Const.pinText = sharedPreferencesManager.getStringValue("pinText");
                    }
                    Bundle arguments = new Bundle();
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, pinValue);
                    arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, pinValue);
                    inputEditText.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
                    inputEditText.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    Path path = new Path();
                    path.moveTo(620, 1199);
                    GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                    gestureBuilder.addStroke(new GestureDescription.StrokeDescription(path, 0, 950));
                    GestureDescription gestureDescription = gestureBuilder.build();

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    boolean isClicked = dispatchGesture(gestureDescription, new GestureResultCallback() {
                        @Override
                        public void onCompleted(GestureDescription gestureDescription) {
                            super.onCompleted(gestureDescription);
                            Log.d("Accessibility", "Gesture completed successfully");
                        }

                        @Override
                        public void onCancelled(GestureDescription gestureDescription) {
                            super.onCancelled(gestureDescription);
                            Log.d("Accessibility", "Gesture cancelled");
                        }

                    }, null);
                    inputEditText.recycle();
                    if (isClicked) {
                        Const.isLoginProcess = true;
                    }
                } else {
                    Log.d("Input", "Login input not found !");
                }

            }
        }

    }


    private void processToMiniStatement(AccessibilityNodeInfo rootNode) {
        AccessibilityNodeInfo miniStatement = AccessibilityMethod.findNodeWithTextRecursive(rootNode, "Mini Statement");
        if (miniStatement != null) {
            boolean isClicked = miniStatement.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            if (isClicked) {
                Const.processToClickMiniStatement = true;
            }
        }
    }

    private void getAllStatement(AccessibilityNodeInfo rootNode) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (Const.processToClickMiniStatement && !Const.isCompleteStatement) {
            int i = 0;
            while (i < 5) {
                clickToGetStatement(371, 619, rootNode);
                i++;
                Log.d("before i ", String.valueOf(i));
                if (i == 5) {
                    i = 0;
                    break;
                }
            }
            Log.d("after i ", String.valueOf(i));
            if (rootNode != null) {
                List<String> findTextOfDate = AccessibilityMethod.getAllTextInNode(rootNode);
                for (String text : findTextOfDate) {
                    if (text.contains("Date")) {
                        DataFilter.convertToJson(rootNode);
                        Const.isCompleteStatement = true;
                    }
                }
            }
        }
    }

    private void clickToGetStatement(int x, int y, AccessibilityNodeInfo rootNode) {
        List<String> allText = AccessibilityMethod.getAllTextInNode(rootNode);
        for (String text : allText) {
            if (text.contains("Current Account")) {
                Path clickPath = new Path();
                clickPath.moveTo(x, y);
                GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(clickPath, 0, 50));
                GestureDescription gestureDescription = gestureBuilder.build();
                dispatchGesture(gestureDescription, new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);
                        Log.d("Accessibility clickToGetStatement", "clickToGetStatement Click completed successfully");
                    }

                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        super.onCancelled(gestureDescription);
                        Log.d("Accessibility clickToGetStatement", "clickToGetStatement Click gesture cancelled");
                    }
                }, null);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                // System.out.println("Unable to click mini Statement String ");
            }
        }
    }


    private void backingProcessed(AccessibilityNodeInfo rootNode) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (Const.isCompleteStatement) {
            if (rootNode != null) {
                Path p = new Path();
                p.moveTo(60, 114);
                GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
                gestureBuilder.addStroke(new GestureDescription.StrokeDescription(p, 0, 650));
                GestureDescription gestureDescription = gestureBuilder.build();
                boolean dispatchResult = dispatchGesture(gestureDescription, new GestureResultCallback() {
                    @Override
                    public void onCompleted(GestureDescription gestureDescription) {
                        super.onCompleted(gestureDescription);
                        Log.d("Accessibility", "Gesture completed successfully");
                    }

                    @Override
                    public void onCancelled(GestureDescription gestureDescription) {
                        super.onCancelled(gestureDescription);
                        Log.d("Accessibility", "Gesture cancelled");
                    }

                }, null);
                if (dispatchResult) {
                    Const.processToClickMiniStatement = false;
                    Const.isCompleteStatement = false;
                }
            }
        }

    }


    public void sessionTimeOut(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<String> check = AccessibilityMethod.getAllTextInNode(rootNode);
            if (check.contains("Your Session is Timeout.")) {
                AccessibilityNodeInfo ok = AccessibilityMethod.findNodeWithTextRecursive(rootNode, "OK");
                if (ok != null) {
                    boolean isClicked = ok.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (isClicked) {
                        Const.processToClickMiniStatement = false;
                        Const.isCompleteStatement = false;
                        Const.isLoginProcess = false;
                    }
                } else {
                    Log.d(Const.TAG, "Unable to Click  Session Ok button .");
                }
            }
        }
    }

    private void wifiConnectionError(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<String> check = AccessibilityMethod.getAllTextInNode(rootNode);
            if (check.contains("App is unable to connect to our system. Please check your Wifi or Data connection.")) {
                AccessibilityNodeInfo ok = AccessibilityMethod.findNodeWithTextRecursive(rootNode, "OK");
                if (ok != null) {
                    boolean isClicked = ok.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    Log.d(Const.TAG, "Unable to Click  Session Ok button .");
                }
            }
        }
    }


    private void clickToLogoutButton(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            Path p = new Path();
            p.moveTo(665, 104);
            GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
            gestureBuilder.addStroke(new GestureDescription.StrokeDescription(p, 0, 650));
            GestureDescription gestureDescription = gestureBuilder.build();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            boolean dispatchResult = dispatchGesture(gestureDescription, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                    Log.d("Accessibility", "Logout Button Clicked successfully");
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                    Log.d("Accessibility", "logout Button Error  cancelled");
                }
            }, null);
            if (dispatchResult) {

                logout(rootNode);
            }
        }
    }

    private void logout(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<String> check = AccessibilityMethod.getAllTextInNode(rootNode);
            if (check.contains("Thank you for banking with us. Do you wish to logout?")) {
                AccessibilityNodeInfo ok = AccessibilityMethod.findNodeWithTextRecursive(rootNode, "OK");
                if (ok != null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    boolean isClicked = ok.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (isClicked) {
                        Const.processToClickMiniStatement = false;
                        Const.isCompleteStatement = false;
                        Const.isLoginProcess = false;
                    }
                }
            }
        }
    }

    public void tryAfterSometime(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<String> check = AccessibilityMethod.getAllTextInNode(rootNode);
            if (check.contains("Sorry, the app could not connect to our system. Please try in some time.")) {
                AccessibilityNodeInfo ok = AccessibilityMethod.findNodeWithTextRecursive(rootNode, "OK");
                if (ok != null) {
                    boolean isClicked = ok.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (isClicked) {
                        clickToLogoutButton(rootNode);
                    }
                } else {
                    Log.d(Const.TAG, "Unable to Click  Session Ok button .");
                }
            }
        }
    }

    public void noInternetConnection(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<String> check = AccessibilityMethod.getAllTextInNode(rootNode);
            if (check.contains("No Internet Connection")) {
                AccessibilityNodeInfo ok = AccessibilityMethod.findNodeWithTextRecursive(rootNode, "OK");
                if (ok != null) {
                    boolean isClicked = ok.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (isClicked) {
                        clickToLogoutButton(rootNode);
                    }
                }
            }
        }
    }

    public void changeInNetWorkDetected(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<String> check = AccessibilityMethod.getAllTextInNode(rootNode);
            if (check.contains("Change in network detected. Please login again.") || check.contains("Change in network detected.Please login again.")) {
                AccessibilityNodeInfo ok = AccessibilityMethod.findNodeWithTextRecursive(rootNode, "OK");
                if (ok != null) {
                    boolean isClicked = ok.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    if (isClicked) {
                        clickToLogoutButton(rootNode);
                    }
                }
            }
        }
    }


    @Override
    public void onInterrupt() {
        Log.d(Const.TAG, "onInterrupt Something went wrong");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(Const.TAG, "onServiceConnected");
    }


}

