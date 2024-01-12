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

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

public class MyAccessibilityService extends AccessibilityService {

    boolean isUsername = false;
    final CaptureTicker ticker = new CaptureTicker(this::processTickerEvent);
    boolean isPassword = false;
    boolean isTransactionAccount = false;
    boolean isTransactionAccountDetails = false;
    boolean isStatement = false;
    boolean isMyAccount = false;
    boolean isGetBalance = false;
    boolean isAccountSummary = false;
    boolean shouldLogout = false;

    boolean isStatementStuck = false;
    int counter = 0;
    private final Runnable logoutRunnable = () -> {
        Log.d("Logout Handler", "Finished");
        shouldLogout = true;
    };
    Handler logoutHandler = new Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }

    private void processTickerEvent() {
        Log.d(Config.TAG, "App Start");
        Log.d("All Flags", printAllFlags());
        ticker.setNotIdle();
        AccessibilityNodeInfo rootNode = AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow());
        if (rootNode != null) {
            if (AccessibilityMethod.findNodeByPackageName(rootNode, Config.packageName) == null) {
            } else {
                checkForSessionExpiry();
                logout();
                if (shouldLogout) {
                    isGetBalance = true;
                    isStatement = true;
                    if (counter == 5 && !isStatementStuck) {
                        checkForSessionExpiry();
                        counter = 0;
                        isStatementStuck = true;
                        logoutHandler.removeCallbacks(logoutRunnable);
                    }
                    return;
                }

                //  AccessibilityMethod.listAllTextsInActiveWindow(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()));
                loginUser();
                corporateInternetBanking();
                myAccount();
                accountSummary();
                transactionAccount();
                transactionAccountDetails();
                getBalance();
                getMiniStatement();
            }
            rootNode.recycle();
        }
    }

    private void loginUser() {
        ticker.setNotIdle();
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
                    isUsername = false;
                    isPassword = false;
                    isGetBalance = false;
                    isStatement = false;
                    logoutHandler.removeCallbacks(logoutRunnable);
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
        if (isMyAccount) {
            return;
        }
        AccessibilityNodeInfo corporateInternetBanking = AccessibilityMethod.findNodeWithTextRecursive(getRootInActiveWindow(), "My Account");
        if (corporateInternetBanking != null) {
            Rect outBounds = new Rect();
            corporateInternetBanking.getBoundsInScreen(outBounds);
            boolean isClicked = performTap(outBounds.centerX(), outBounds.centerY());
            if (isClicked) {
                isMyAccount = true;
            }
        }
    }

    private void accountSummary() {
        ticker.setNotIdle();
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
                accountSummaryNode.recycle();
            }
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
                isTransactionAccountDetails  = true;
                isAccountSummary = true;
            }
        }
    }

    private void transactionAccountDetails() {
        ticker.setNotIdle();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (isTransactionAccountDetails) {
            boolean isClicked = performTap(317, 389, 1100);
            if (isClicked) {
                System.out.println("transactionAccountDetails" + isTransactionAccountDetails);

            }
        }
    }

    private void getBalance() {
        ticker.setNotIdle();
        List<String> currentBalanceList = AccessibilityMethod.listAllTextsInActiveWindow(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()));
        int availableBalanceIndex = currentBalanceList.indexOf("Available Balance");
        if (availableBalanceIndex != -1 && availableBalanceIndex < currentBalanceList.size() - 1) {
            String availableBalanceValue = currentBalanceList.get(availableBalanceIndex + 1);
            availableBalanceValue = availableBalanceValue.replaceAll(",", "").replaceAll("Rs\\.\\s", "");
            System.out.println("Available Balance: " + availableBalanceValue);
            Config.totalBalance = availableBalanceValue;
        } else {
            System.out.println("Available Balance not found or value not available.");
        }
        if (isGetBalance) {
            return;
        }
        AccessibilityNodeInfo accountInformation = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Account Information", true, false);
        if (accountInformation != null) {
            Rect outBounds = new Rect();
            accountInformation.getBoundsInScreen(outBounds);
            boolean isClicked = performTap(outBounds.centerX(), outBounds.centerY());
            if (isClicked) {
                accountInformation.recycle();
                isGetBalance = true;
                isStatement = false;
                scrollCounter = 0;
            }
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    int scrollCounter = 0;

    private void getMiniStatement() {
        ticker.setNotIdle();
        if (Config.totalBalance.isEmpty()) {
            isGetBalance = false;
        } else {
            Log.d("Total Balance", "Total Balance=>" + Config.totalBalance);
            AccessibilityNodeInfo miniStatement = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Mini Statement", true, false);
            if (!isStatement) {
                if (miniStatement != null) {
                    Rect outBounds = new Rect();
                    miniStatement.getBoundsInScreen(outBounds);
                    boolean isClicked = performTap(outBounds.centerX(), outBounds.centerY());
                    if (isClicked) {
                        isStatement = true;
                    }
                }
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            AccessibilityNodeInfo scrollRecyclerView = AccessibilityMethod.findAndScrollListView(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "androidx.recyclerview.widget.RecyclerView");
            if (scrollRecyclerView != null) {
                while (scrollCounter < 3) {
                    scrollRecyclerView.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
                    DataFilter.convertToJson(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()));
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    scrollCounter++;
                }
            }
            if (scrollCounter == 3) {
                boolean menuClick = false;
                for(int i = 0;i<3;i++)
                {
                    menuClick = performTap(668, 122, 150);
                }
                if (menuClick) {
                    AccessibilityNodeInfo myAccounts = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "My Accounts", true, false);
                    if (myAccounts != null) {
                        Rect outBounds = new Rect();
                        myAccounts.getBoundsInScreen(outBounds);
                        boolean isClickMyAccounts = performTap(outBounds.centerX(), outBounds.centerY());
                        if (isClickMyAccounts) {
                            AccessibilityNodeInfo transactionAccount = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Transaction Accounts", true, false);
                            if (transactionAccount != null) {
                                Rect outBounds2 = new Rect();
                                transactionAccount.getBoundsInScreen(outBounds2);
                                boolean isTransactionAccount2 = performTap(outBounds2.centerX(), outBounds2.centerY());
                                if (isTransactionAccount2) {
                                    isMyAccount = false;
                                    isTransactionAccount = false;
                                    isTransactionAccountDetails = false;
                                    Config.totalBalance = "";
                                    scrollCounter = 0;
                                }
                            }
                        }
                    }
                }
            }
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
        ticker.setNotIdle();
        Log.d(Config.TAG, "Logout Stage = " + shouldLogout);
        logoutHandler.postDelayed(logoutRunnable, 1000 * 60 * 5);
        if (shouldLogout) {
            while (counter < 5) {
                performTap(668, 122, 150);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                counter++;
                Log.d("counter", String.valueOf(counter));
            }
            AccessibilityNodeInfo targetNode6 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Logout", true, false);
            if (targetNode6 != null) {
                Rect outBounds = new Rect();
                targetNode6.getBoundsInScreen(outBounds);
                boolean isClicked = performTap(outBounds.centerX(), outBounds.centerY());
                if (isClicked) {
                    AccessibilityNodeInfo button = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "YES", true, true);
                    if (button != null) {
                        Log.d("targetNode6 here", String.valueOf(isClicked));
                        Rect outBounds2 = new Rect();
                        button.getBoundsInScreen(outBounds2);
                        performTap(outBounds2.centerX(), outBounds2.centerY());
                        button.recycle();
                        isStatement = false;
                        isTransactionAccount = false;
                        isTransactionAccountDetails = false;
                        isMyAccount = false;
                        isAccountSummary = false;
                        isGetBalance = false;
                        isPassword = false;
                        isUsername = false;
                        isStatementStuck = false;
                        counter = 0;
                        scrollCounter = 0;
                        logoutHandler.removeCallbacks(logoutRunnable);
                        ticker.setNotIdle();
                    }
                    targetNode6.recycle();
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void checkForSessionExpiry() {   //Unable to process the request, Please try again.
        ticker.setNotIdle();
        AccessibilityNodeInfo targetNode1 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Do you really want to Logout?", true, false);
        AccessibilityNodeInfo targetNode2 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Unable to process the request", false, false);
        AccessibilityNodeInfo targetNode3 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Feedback", true, false);
        AccessibilityNodeInfo targetNode4 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Dear Customer", true, false);
        AccessibilityNodeInfo targetNode5 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Unable to process the request, Please try again.", true, false);
        AccessibilityNodeInfo targetNode6 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Logout", true, false);
        AccessibilityNodeInfo targetNode7 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "No Accounts found for Deposit Account. ", true, false);
        AccessibilityNodeInfo targetNode8 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Due to some technical problems we are unable to process your request. Please try later.", true, false);
        //Unable to retrieve last 5 transactions. Please try later.
        AccessibilityNodeInfo targetNode9 = AccessibilityMethod.findNodeByText(AccessibilityMethod.getTopMostParentNode(getRootInActiveWindow()), "Unable to retrieve last 5 transactions. Please try later.", true, false);
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
                isStatement = false;
                isTransactionAccount = false;
                isMyAccount = false;
                isAccountSummary = false;
                isGetBalance = false;
                isPassword = false;
                isUsername = false;
                shouldLogout = false;
                counter = 0;
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
                boolean isClicked = performTap(outBounds.centerX(), outBounds.centerY());
                if (isClicked) {
                    isStatement = false;
                    isTransactionAccount = false;
                    isMyAccount = false;
                    isAccountSummary = false;
                    isGetBalance = false;
                    isPassword = false;
                    isUsername = false;
                    shouldLogout = false;
                    counter = 0;
                    button.recycle();
                    ticker.setNotIdle();
                }
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
        if (targetNode9 != null) {
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

    private String printAllFlags() {
        StringBuilder result = new StringBuilder();

        // Get the fields of the class
        Field[] fields = getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            String fieldName = field.getName();
            try {
                Object value = field.get(this);
                result.append(fieldName).append(": ").append(value).append("\n");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result.toString();
    }


}


