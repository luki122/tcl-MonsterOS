
package com.tcl.monster.fota.ui;

/** Provides callback methods on major lifecycle events of a {@link Crouton}. */
public interface LifecycleCallback {
    /** Will be called when your Crouton has been displayed. */
    public void onDisplayed();

    /** Will be called when your {@link Crouton} has been removed. */
    public void onRemoved();

    // public void onCeasarDressing();
}
