package com.app.yonosbiscraper.utils;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class AccessibilityMethod {
    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> accessibilityService) {
        String serviceName = context.getPackageName() + "/" + accessibilityService.getName();
        Log.d("Initial Service = ", "Service Name " + serviceName);
        String enabledServices = Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        return enabledServices != null && enabledServices.contains(serviceName);
    }
    public static AccessibilityNodeInfo findEditTextNode(AccessibilityNodeInfo rootNode) {
        if (rootNode == null) {
            return null;
        }
        if ("android.widget.EditText".equals(rootNode.getClassName())) {
            return rootNode; // Return the EditText node
        }

        int childCount = rootNode.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNode = rootNode.getChild(i);
            AccessibilityNodeInfo foundNode = findEditTextNode(childNode);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }

    public static AccessibilityNodeInfo findNodeByResourceId(AccessibilityNodeInfo rootNode, String resourceId) {
        if (rootNode == null) {
            return null;
        }
        if (resourceId.equals(rootNode.getViewIdResourceName())) {
            return rootNode;
        }
        int childCount = rootNode.getChildCount();
        for (int i = 0; i < childCount; i++) {
            AccessibilityNodeInfo childNode = rootNode.getChild(i);
            AccessibilityNodeInfo foundNode = findNodeByResourceId(childNode, resourceId);
            if (foundNode != null) {
                return foundNode;
            }
        }
        return null;
    }

    public static void printResourceIdsRecursive(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            // Print the resource ID if available
            CharSequence resourceId = nodeInfo.getViewIdResourceName();
            if (resourceId != null) {
                Log.d("Resource ID", "ID: " + resourceId.toString());
            }

            // Recursively traverse child nodes
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo childNode = nodeInfo.getChild(i);
                printResourceIdsRecursive(childNode);
                childNode.recycle();
            }
        }
    }

    public static void printClassNamesRecursive(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo != null) {
            // Print the class name if available
            CharSequence className = nodeInfo.getClassName();
            if (className != null) {
                // Log.d("Class Name", "Name: " + className.toString());

            }
            if (nodeInfo.getText() != null) {
                Log.d("getText = ", "" + nodeInfo.getText().toString());
                // Log.d("getText = ", "" + nodeInfo.getText().toString());
            }

            if (nodeInfo.getContentDescription() != null) {
                Log.d("getContentDescription  =  ", "" + nodeInfo.getContentDescription().toString());
                // Log.d("getContentDescription = ", "" +
                // nodeInfo.getContentDescription().toString());
            }

            // Recursively traverse child nodes
            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo childNode = nodeInfo.getChild(i);
                printClassNamesRecursive(childNode);
                // childNode.recycle();
            }
        }
    }

    public static void findAndClickNodeByText(AccessibilityNodeInfo rootNode, String targetText) {
        if (rootNode == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByText(targetText);

        for (AccessibilityNodeInfo node : nodes) {
            Log.d("TEXT", node.getText().toString());
            Log.d("Node ", String.valueOf(node.isClickable()));
            if (node.isClickable()) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                return;
            }
        }
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = rootNode.getChild(i);
            findAndClickNodeByText(childNode, targetText);
        }
    }

    public static AccessibilityNodeInfo findNodeWithTextRecursive(AccessibilityNodeInfo nodeInfo, String targetText) {
        if (nodeInfo != null) {
            if (nodeInfo.getText() != null && targetText.equals(nodeInfo.getText().toString())) {
                Log.d("getText", "" + nodeInfo.getText().toString());
                return nodeInfo;
            }

            for (int i = 0; i < nodeInfo.getChildCount(); i++) {
                AccessibilityNodeInfo childNode = nodeInfo.getChild(i);
                AccessibilityNodeInfo resultNode = findNodeWithTextRecursive(childNode, targetText);
                if (resultNode != null) {
                    return resultNode;
                }
            }
        }
        return null;
    }

    public static AccessibilityNodeInfo findNodeByClassName(AccessibilityNodeInfo rootNode, String className) {
        if (rootNode != null) {
            Queue<AccessibilityNodeInfo> queue = new LinkedList<>();
            queue.add(rootNode);

            while (!queue.isEmpty()) {
                AccessibilityNodeInfo currentNode = queue.poll();

                if (currentNode != null && className.equals(currentNode.getClassName())) {
                    return currentNode;
                }

                for (int i = 0; i < currentNode.getChildCount(); i++) {
                    AccessibilityNodeInfo childNode = currentNode.getChild(i);
                    if (childNode != null) {
                        queue.add(childNode);
                    }
                }
            }
        }

        return null;
    }

    public static List<String> getAllTextInNode(AccessibilityNodeInfo node) {
        List<String> allTextList = new ArrayList<>();
        collectTextFromNode(node, allTextList);
        return allTextList;
    }

    private static void collectTextFromNode(AccessibilityNodeInfo node, List<String> allTextList) {
        if (node == null) {
            return;
        }
        if (node.getText() != null) {
            allTextList.add(node.getText().toString());
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            collectTextFromNode(childNode, allTextList);
        }
    }


    public static AccessibilityNodeInfo findAndScrollListView(AccessibilityNodeInfo node) {
        if (node != null) {
            int childCount = node.getChildCount();
            for (int i = 0; i < childCount; i++) {
                AccessibilityNodeInfo childNode = node.getChild(i);

                if (childNode != null && "android.widget.ListView".equals(childNode.getClassName())) {
                    return childNode;
                }
                AccessibilityNodeInfo resultNode = findAndScrollListView(childNode);
                if (resultNode != null) {
                    return resultNode;
                }
            }
        }
        return null;
    }


    public static List<String> getAllPackageNames(Context context) {
        List<String> packageNames = new ArrayList<>();

        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);

        for (PackageInfo packageInfo : packages) {
            String packageName = packageInfo.packageName;
            packageNames.add(packageName);
        }

        return packageNames;
    }


    public static List<String> listAllTextsInActiveWindow(AccessibilityNodeInfo rootNode) {
        if (rootNode != null) {
            List<String> allTexts = new ArrayList<>();
            traverseNodesForText(rootNode, allTexts);
            rootNode.recycle();
            // Now 'allTexts' contains a list of all texts in the active window
            Gson gson = new Gson();
            String json = gson.toJson(allTexts);
            Log.d("OUTPUT", json);
            return allTexts;
        } else {
            Log.d("OUTPUT", "[]");
        }
        return new ArrayList<>();
    }

    public static void traverseNodesForText(AccessibilityNodeInfo node, List<String> allTexts) {
        if (node == null) return;
        String output = node.getText() != null ? node.getText().toString() : node.getContentDescription() != null ? node.getContentDescription().toString() : "";
        allTexts.add(output);

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            traverseNodesForText(childNode, allTexts);
        }
    }

    public static AccessibilityNodeInfo findNodeByPackageName(AccessibilityNodeInfo node, String targetClassName) {
        if (node == null) return null;

        if (node.getPackageName().toString().equals(targetClassName)) {
            return node;
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = node.getChild(i);
            AccessibilityNodeInfo targetNode = findNodeByPackageName(childNode, targetClassName);
            if (targetNode != null) {
                return targetNode;
            }
        }

        return null;
    }

    public static AccessibilityNodeInfo getTopMostParentNode(AccessibilityNodeInfo nodeInfo) {
        if (nodeInfo == null) {
            return null;
        }

        AccessibilityNodeInfo parentNode = nodeInfo;
        AccessibilityNodeInfo topMostParentNode = null;

        while (parentNode != null) {
            topMostParentNode = parentNode;
            parentNode = parentNode.getParent();
        }

        return topMostParentNode;
    }

    public static AccessibilityNodeInfo findNodeByText(AccessibilityNodeInfo rootNode, String text, boolean deepSearch, boolean clickable) {
        if (rootNode == null) return null;

        for (int i = 0; i < rootNode.getChildCount(); i++) {
            AccessibilityNodeInfo childNode = rootNode.getChild(i);
            if (childNode != null) {
                if (childNode.getText() != null && text.equals(childNode.getText().toString()) && (!clickable || childNode.isClickable())) {
                    return childNode;
                } else if (deepSearch) {
                    if (childNode.getText() != null && childNode.getText().toString().contains(text) && (!clickable || childNode.isClickable())) {
                        return childNode;
                    }
                }
                if (childNode.getContentDescription() != null && text.equals(childNode.getContentDescription().toString()) && (!clickable || childNode.isClickable())) {
                    return childNode;
                } else if (deepSearch) {
                    if (childNode.getContentDescription() != null && childNode.getContentDescription().toString().contains(text) && (!clickable || childNode.isClickable())) {
                        return childNode;
                    }
                }
                AccessibilityNodeInfo foundNode = findNodeByText(childNode, text, deepSearch, clickable);
                childNode.recycle();
                if (foundNode != null) {
                    return foundNode;
                }
            }
        }
        return null;
    }



}
