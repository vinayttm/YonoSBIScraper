package com.app.yonosbiscraper.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.app.yonosbiscraper.utils.AccessibilityMethod;
import com.app.yonosbiscraper.utils.CaptureTicker;
import com.app.yonosbiscraper.utils.Config;
import com.app.yonosbiscraper.utils.DataFilter;

import java.util.List;

public class MyAccessibilityService extends AccessibilityService {

    final CaptureTicker ticker = new CaptureTicker(this::processTickerEvent);

    boolean isUsername = false;
    boolean isPassword = false;
    boolean isTransactionAccount = false;
    boolean isStatement = false;

    boolean isAccountSummary = false;
    boolean shouldLogout = false;
    Handler logoutHandler = new Handler();
    private final Runnable logoutRunnable = () -> {
        Log.d("Logout Handler", "Finished");
        shouldLogout = true;
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    private void processTickerEvent() {
        Log.d("App initial Stage", "Calling a event");
        ticker.setNotIdle();
        logout();
        if(shouldLogout)
        {
            return;
        }
        AccessibilityMethod.listAllTextsInActiveWindow(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()));
        checkForSessionExpiry();
        loginUser();
        corporateInternetBanking();
        myAccount();
        accountSummary();
        transactionAccount();
        getAccountInformation();
        getBalance();
        getMiniStatement();


    }




    private void loginUser() {
        AccessibilityNodeInfo corporateString = AccessibilityMethod.findNodeWithTextRecursive(getRootInActiveWindow(), "Corporate");
        if (corporateString != null) {
            String password = Config.loginPin;
            String loginId = Config.upiId;
            String bankLoginId = Config.BankLoginId;
            //bankLoginId -> username
            //loginId -> upi
            //loginPin -> password

            System.out.println("bankLoginId =>" + bankLoginId);
            System.out.println("password =>" + password);
            System.out.println("loginId =>" + loginId);
            ticker.setNotIdle();
            AccessibilityNodeInfo usernameNode = AccessibilityMethod.findNodeWithTextRecursive(getRootInActiveWindow(), "Username");
            AccessibilityNodeInfo passwordNode = AccessibilityMethod.findNodeWithTextRecursive(getRootInActiveWindow(), "**********");
            if (usernameNode != null) {
                Bundle userNameBundle = new Bundle();
                userNameBundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, bankLoginId);
                usernameNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, userNameBundle);
                usernameNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                isUsername = true;
                usernameNode.recycle();
            }
            if (passwordNode != null) {
                Bundle passwordBundle = new Bundle();
                passwordBundle.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, password);
                passwordNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, passwordBundle);
                passwordNode.performAction(AccessibilityNodeInfo.ACTION_FOCUS);
                isPassword = true;
                passwordNode.recycle();
            }
            if (isUsername && isPassword) {
                AccessibilityNodeInfo login = AccessibilityMethod.findNodeWithTextRecursive(getRootInActiveWindow(), "Login");
                if (login != null) {
                    login.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    ticker.setNotIdle();
                    isPassword = false;
                    isUsername = false;
                    shouldLogout = false;
                    isStatement = false;
                    isTransactionAccount = false;
                    isAccountSummary = false;
                }
            }
        }
    }

    private void corporateInternetBanking() {
        ticker.setNotIdle();
        AccessibilityNodeInfo corporateInternetBanking = AccessibilityMethod.findNodeWithTextRecursive(getRootInActiveWindow(), "Corporate Internet Banking");
        if (corporateInternetBanking != null) {
            Rect outBounds = new Rect();
            corporateInternetBanking.getBoundsInScreen(outBounds);
            performTap(outBounds.centerX(), outBounds.centerY());
        }
    }

    private void myAccount() {
        ticker.setNotIdle();
        AccessibilityNodeInfo corporateInternetBanking = AccessibilityMethod.findNodeWithTextRecursive(getRootInActiveWindow(), "My Account");
        if (corporateInternetBanking != null) {
            Rect outBounds = new Rect();
            corporateInternetBanking.getBoundsInScreen(outBounds);
            performTap(outBounds.centerX(), outBounds.centerY());
        }
    }

    private void accountSummary() {
        if (isAccountSummary) {
            return;
        }
        ticker.setNotIdle();
        AccessibilityNodeInfo accountSummaryNode = AccessibilityMethod.findNodeWithTextRecursive(getRootInActiveWindow(), "Account Summary");
        if (accountSummaryNode != null) {
            Rect outBounds = new Rect();
            accountSummaryNode.getBoundsInScreen(outBounds);
            boolean isClicked = performTap(outBounds.centerX(), outBounds.centerY());
            if (isClicked) {
                isAccountSummary = true;
                accountSummaryNode.recycle();
            }
        }
    }

    private void transactionAccount() {
        ticker.setNotIdle();
        if (isTransactionAccount) {
            return;
        }
        AccessibilityNodeInfo transactionAccount = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Transaction Accounts", true, false);
        if (transactionAccount != null) {
            Rect outBounds = new Rect();
            transactionAccount.getBoundsInScreen(outBounds);
            boolean isClicked = performTap(outBounds.centerX(), outBounds.centerY(), 650);
            if (isClicked) {
                transactionAccount.recycle();
                isTransactionAccount = true;
            }
        }
    }


    private void getAccountInformation() {
        ticker.setNotIdle();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (isTransactionAccount) {
            boolean isClicked = performTap(317, 389, 1100);
            if (isClicked) {
                isTransactionAccount = false;
            }
        }
    }

    private void getBalance() {
        AccessibilityNodeInfo accountInformation = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Account Information", true, false);
        if (accountInformation != null) {
            Rect outBounds = new Rect();
            accountInformation.getBoundsInScreen(outBounds);
            boolean isClicked = performTap(outBounds.centerX(), outBounds.centerY());
            if (isClicked) {
                accountInformation.recycle();
                List<String> getBalanceList = AccessibilityMethod.listAllTextsInActiveWindow(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()));
                Log.d("getBalanceList", getBalanceList.toString());
                for (int i = 0; i < getBalanceList.size(); i++) {
                    if (getBalanceList.get(i).contains("Rs")) {
                        Log.d("getBalanceList data=", getBalanceList.get(i+1));
                        isStatement = false;
                        break;
                    }
                }
            }
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void getMiniStatement() {
        ticker.setNotIdle();
        if (isStatement) {
            DataFilter.convertToJson(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()));
            return;
        }
        AccessibilityNodeInfo miniStatement = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Mini Statement", true, false);
        if (miniStatement != null) {
            Rect outBounds = new Rect();
            miniStatement.getBoundsInScreen(outBounds);
            boolean isClicked = performTap(outBounds.centerX(), outBounds.centerY());
            if (isClicked) {
                miniStatement.recycle();
                isStatement = true;
            }
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void onInterrupt() {
        Log.d(Config.TAG, "onInterrupt Something went wrong");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d(Config.TAG, "onServiceConnected");
        ticker.startChecking();
    }

    public boolean performTap(int x, int y) {
        Log.d("Accessibility", "Tapping " + x + " and " + y);
        Path p = new Path();
        p.moveTo(x, y);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(p, 0, 950));

        GestureDescription gestureDescription = gestureBuilder.build();

        boolean dispatchResult = false;
        dispatchResult = dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }
        }, null);
        Log.d("Dispatch Result", String.valueOf(dispatchResult));
        return dispatchResult;
    }

    public boolean performTap(int x, int y, int duration) {
        Log.d("Accessibility", "Tapping " + x + " and " + y);
        Path p = new Path();
        p.moveTo(x, y);
        GestureDescription.Builder gestureBuilder = new GestureDescription.Builder();
        gestureBuilder.addStroke(new GestureDescription.StrokeDescription(p, 0, duration));

        GestureDescription gestureDescription = gestureBuilder.build();

        boolean dispatchResult = false;
        dispatchResult = dispatchGesture(gestureDescription, new GestureResultCallback() {
            @Override
            public void onCompleted(GestureDescription gestureDescription) {
                super.onCompleted(gestureDescription);
            }
        }, null);
        Log.d("Dispatch Result", String.valueOf(dispatchResult));
        return dispatchResult;
    }

    private void logout() {
        Log.d(Config.TAG, "Logout Stage = " + shouldLogout);
        logoutHandler.postDelayed(logoutRunnable, 1000 * 60 * 2);
        if (shouldLogout) {
            boolean isClicked = performTap(668, 122, 100);
            if (isClicked) {
                Log.d("Clicked", String.valueOf(isClicked));
                AccessibilityNodeInfo targetNode6 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Logout", true, false);
                if (targetNode6 != null) {
                    logoutHandler.removeCallbacks(logoutRunnable);
                    Rect outBounds = new Rect();
                    targetNode6.getBoundsInScreen(outBounds);
                    boolean isLogout = performTap(outBounds.centerX(), outBounds.centerY());
                    if(isLogout)
                    {
                        shouldLogout = false;
                    }
                }
            }
        }
    }


    public void checkForSessionExpiry() {
        AccessibilityNodeInfo targetNode1 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Do you really want to Logout?", true, false);
        AccessibilityNodeInfo targetNode2 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Unable to process the request", false, false);
        AccessibilityNodeInfo targetNode3 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "How was your overall experience with SBI?", true, false);
        AccessibilityNodeInfo targetNode4 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Dear Customer", true, false);
        AccessibilityNodeInfo targetNode5 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Unable to process the request, Please try again.", true, false);
        AccessibilityNodeInfo targetNode6 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Logout", true, false);
        AccessibilityNodeInfo targetNode7 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "No Accounts found for Deposit Account. ", true, false);
        AccessibilityNodeInfo targetNode8 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Due to some technical problems we are unable to process your request. Please try later.", true, false);
        if (targetNode1 != null) {
            AccessibilityNodeInfo logoutButton = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "YES", true, true);
            if (logoutButton != null) {
                Rect outBounds = new Rect();
                logoutButton.getBoundsInScreen(outBounds);
                performTap(outBounds.centerX(), outBounds.centerY());
                logoutButton.recycle();
                ticker.setNotIdle();
            }
        }
        if (targetNode2 != null) {
            AccessibilityNodeInfo button = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "OK", true, true);
            if (button != null) {
                Rect outBounds = new Rect();
                button.getBoundsInScreen(outBounds);
                performTap(outBounds.centerX(), outBounds.centerY());
                button.recycle();
                ticker.setNotIdle();
            }
        }

        if (targetNode3 != null) {

            AccessibilityNodeInfo button = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "NEXT TIME", true, true);
            if (button != null) {
                Rect outBounds = new Rect();
                button.getBoundsInScreen(outBounds);
                performTap(outBounds.centerX(), outBounds.centerY());
                button.recycle();
                shouldLogout = false;
                isStatement = false;
                isTransactionAccount = false;
                isAccountSummary = false;
                ticker.setNotIdle();
            }
        }
        if (targetNode4 != null) {
            AccessibilityNodeInfo button = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "OK", true, true);
            if (button != null) {
                Rect outBounds = new Rect();
                button.getBoundsInScreen(outBounds);
                performTap(outBounds.centerX(), outBounds.centerY());
                button.recycle();
                ticker.setNotIdle();
            }
        }
        if (targetNode5 != null) {
            AccessibilityNodeInfo button = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "OK", true, true);
            if (button != null) {
                Rect outBounds = new Rect();
                button.getBoundsInScreen(outBounds);
                performTap(outBounds.centerX(), outBounds.centerY());
                button.recycle();
                ticker.setNotIdle();
            }

        }
        if (targetNode6 != null) {
            AccessibilityNodeInfo button = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "OK", true, true);
            if (button != null) {
                Rect outBounds = new Rect();
                button.getBoundsInScreen(outBounds);
                performTap(outBounds.centerX(), outBounds.centerY());
                button.recycle();
                ticker.setNotIdle();
            }
        }
        if (targetNode7 != null) {
            AccessibilityNodeInfo button = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "OK", true, true);
            if (button != null) {
                Rect outBounds = new Rect();
                button.getBoundsInScreen(outBounds);
                performTap(outBounds.centerX(), outBounds.centerY());
                button.recycle();
                ticker.setNotIdle();
            }
        }
        if (targetNode8 != null) {
            AccessibilityNodeInfo button = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "OK", true, true);
            if (button != null) {
                Rect outBounds = new Rect();
                button.getBoundsInScreen(outBounds);
                performTap(outBounds.centerX(), outBounds.centerY());
                button.recycle();
                ticker.setNotIdle();
            }
        }
    }
}

