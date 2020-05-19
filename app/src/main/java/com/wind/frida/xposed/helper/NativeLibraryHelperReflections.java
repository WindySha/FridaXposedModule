package com.wind.frida.xposed.helper;

import android.os.Build;

import com.wind.frida.xposed.utils.ReflectUtils;

import java.io.File;

/**
 * @author Windysha
 */
public class NativeLibraryHelperReflections {

    private static final String NATIVE_LIBRARY_HELPER_HANDLE_CLASS_NAME =
            "com.android.internal.content.NativeLibraryHelper$Handle";
    private static final String NATIVE_LIBRARY_HELPER_CLASS_NAME =
            "com.android.internal.content.NativeLibraryHelper";

    private static final String VMRUNTIME_CLASS_NAME = "dalvik.system.VMRuntime";

    public static Object createHandle(File apkFile) {
        return ReflectUtils.callMethod(NATIVE_LIBRARY_HELPER_HANDLE_CLASS_NAME,
                "create",
                apkFile);
    }

    public static int findSupportedAbi(Object handle, String[] supportedAbis) {
        return (int) ReflectUtils.callMethod(NATIVE_LIBRARY_HELPER_CLASS_NAME,
                "findSupportedAbi", handle, supportedAbis);
    }

    public static int copyNativeBinaries(Object handle, File sharedLibraryDir, String abi) {
        return (int) ReflectUtils.callMethod(NATIVE_LIBRARY_HELPER_CLASS_NAME,
                "copyNativeBinaries", handle, sharedLibraryDir, abi);
    }

    public static int copyNativeBinariesIfNeededLI(File apkFile, File sharedLibraryDir) {
        return (int) ReflectUtils.callMethod(NATIVE_LIBRARY_HELPER_CLASS_NAME,
                "copyNativeBinariesIfNeededLI", apkFile, sharedLibraryDir);
    }

    public static boolean vMRuntimeIs64Bit() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        Object vmObj = ReflectUtils.callMethod(VMRUNTIME_CLASS_NAME, "getRuntime");
        return (Boolean) ReflectUtils.callMethod(vmObj, "is64Bit");
    }
}
