/* Copyright (C) 2016 Tcl Corporation Limited */
package cn.tcl.filemanager;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import cn.tcl.filemanager.manager.MountManager;

public class MountReceiver extends BroadcastReceiver {

    private final ArrayList<MountListener> mMountListenerList = new ArrayList<MountListener>();

    public interface MountListener {
        /**
         * This method will be called when receive a mounted intent.
         */
        void onMounted();

        /**
         * This method will be implemented by its class who implements this
         * interface, and called when receive a unMounted intent.
         *
         * @param mountPoint the path of mount point
         */
        void onUnmounted(String mountPoint);
        void onScannerFinished();
        void onScannerStarted();
        void onEject();
    }

    /**
     * This method gets MountPointManager's instance
     */
    public MountReceiver() {}

    /**
     * This method adds listener for activities
     *
     * @param listener listener of certain activity to respond mounted and
     *            unMounted intent
     */
    public void registerMountListener(MountListener listener) {
        mMountListenerList.add(listener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String mountPoint = null;
        Uri mountPointUri = intent.getData();
        if (mountPointUri != null) {
            mountPoint = mountPointUri.getPath();
        }

        if (mountPoint == null || mountPointUri == null) {
            return;
        }

        if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
        	MountManager.getInstance().init(context);
            synchronized (this) {
                for (MountListener listener : mMountListenerList) {
                    listener.onMounted();
                }
            }
        } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)
                || Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)) {
        	//add for PR932402 by yane.wang@jrdcom.com 20150215
                synchronized (this) {
                    for (MountListener listener : mMountListenerList) {
                        listener.onUnmounted(mountPoint);
                    }
                }
		} else if (Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
        	 for (MountListener listener : mMountListenerList) {
                 listener.onScannerFinished();
             }
		} else if (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)) {
			for (MountListener listener : mMountListenerList) {
				listener.onScannerStarted();
			}
		} else if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
			for (MountListener listener : mMountListenerList) {
				listener.onEject();
			}
		}
    }

    /**
     * Register a MountReceiver for context. See
     * {@link Intent.ACTION_MEDIA_MOUNTED} {@link Intent.ACTION_MEDIA_UNMOUNTED}
     *
     * @param context Context to use
     * @return A mountReceiver
     */
    public static MountReceiver registerMountReceiver(Context context) {
        MountReceiver receiver = new MountReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addDataScheme("file");
        context.registerReceiver(receiver, intentFilter);
        return receiver;
    }
}
