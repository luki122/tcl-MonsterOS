package com.mst.thememanager;

/**
 * Every presenter in the app must either implement this interface or extend BasePresenter
 * indicating the MvpView type that wants to be attached with.
 */
public interface Presenter<V extends MvpView> {

	/**
	 * Attach MVPView to Presenter
	 * @param mvpView
	 */
    void attachView(V mvpView);
    
    /**
     * Release MVPView from Presenter when current Presenter
     * is going to die
     */
    void detachView();
    
    void onDestory();
    
}
