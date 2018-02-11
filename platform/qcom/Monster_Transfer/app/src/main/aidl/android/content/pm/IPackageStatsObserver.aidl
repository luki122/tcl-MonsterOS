/*Copyright (C) 2016 Tcl Corporation Limited */
package android.content.pm;

oneway interface IPackageStatsObserver {

    void onGetStatsCompleted(in PackageStats pStats, boolean succeeded);
}
