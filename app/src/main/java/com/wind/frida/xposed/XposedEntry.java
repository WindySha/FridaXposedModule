package com.wind.frida.xposed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.wind.frida.xposed.helper.NativeLibraryHelperExt;
import com.wind.frida.xposed.utils.AppUtils;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

@SuppressLint("UnsafeDynamicallyLoadedCode")
public class XposedEntry implements IXposedHookLoadPackage {
    private static final String TAG = "XposedEntry";
    private static final String FRIDA_SO_FILE_NAME = "libfrida-gadget.so";  // current so version is 12.8.20

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        boolean isSystemApp;
        if (lpparam.appInfo != null) {
            isSystemApp = (lpparam.appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        } else {
            isSystemApp = true;
        }

        if (isSystemApp) {
            Log.w(TAG, "handleLoadPackage, but this is systemApp, pkg = " + lpparam.packageName);
            return;
        }

        if (AppUtils.classNameContainedInStackTrace("com.elderdrivers.riru.edxp._hooker.impl.LoadedApkGetCL")) {
            //  use EdXposed may cause java.lang.StackOverflowError  in AppUtils.createAppContext();
            Log.e(TAG, " createAppContext failed, classNameContainedInStackTrace  pkg = " + lpparam.packageName);
            return;
        }

        Context context = AppUtils.createAppContext();
        if (context == null) {
            Log.e(TAG, " createAppContext failed, context is null !!!  pkg = " + lpparam.packageName);
            return;
        }

        String processName = AppUtils.getCurrentProcessName(context);
        boolean isMainProcess = context.getPackageName().equals(processName);

        Log.d(TAG, String.format(" handleLoadPackage, packageName = %s , isMainProcess = %s  processName = %s",
                context.getPackageName(), isMainProcess, processName));

        String dataFilePath = context.getFilesDir().getAbsolutePath();
        AppUtils.ensurePathExist(dataFilePath);

        String libPath = dataFilePath + File.separator + "lib";
        AppUtils.ensurePathExist(libPath);

        String soFilePath = libPath + File.separator + FRIDA_SO_FILE_NAME;
        File soFile = new File(soFilePath);
        String pluginPath = "";

        // only copy the so file on main process
        if (isMainProcess && !soFile.exists()) {
            pluginPath = AppUtils.getPluginApkPath();
            NativeLibraryHelperExt.copyNativeBinaries(new File(pluginPath), new File(libPath));
        }

        // You can put libfrida-gadget.config under sdcard and copy script file to /data/local/tmp/hook.js
        // then frida can be used without command line.
        // Or, you can put libfrida-gadget.config.so in the lib path, rebuild and Install this plugin,
        // then the libfrida-gadget.config.so is copied into taget path
//        if (isMainProcess) {
//            String configPath = "sdcard/libfrida-gadget.config";
//            AppUtils.copyFile(configPath, libPath + File.separator + "libfrida-gadget.config");
//        }

        Log.i(TAG, " handleLoadPackage pluginPath = " + pluginPath + " pluginPath exist = "
                + (new File(pluginPath)).exists() + " soFilePath = " + soFilePath + " soFilePath exist = "
                + soFile.exists());

        if (soFile.exists()) {
            try {
                System.load(soFilePath);
            } catch (UnsatisfiedLinkError ex) {
                Log.e(TAG, String.format(" load so file %s faied", soFilePath), ex);
            }
        } else {
            Log.e(TAG, String.format(" try to load so file %s, but it it not exist.", soFilePath));
        }
    }
}
