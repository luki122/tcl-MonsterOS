package cn.tcl.music.view.mixvibes;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class RdioWarningDialog
{
	protected static boolean warningAnonymousAccessDialogHasBeenShown = false;
	protected static boolean warningNotUnlimitedAccessDialogHasBeenShown = false;
	
	
	public static void showWarningDialogIfNeeded(Activity mainActivity)
	{
    	final String isUnlimited = "isUnlimited";
    	

    	if(isUnlimited == null)
    	{
    		showWarningAnonymousAccessDialog(mainActivity);
    	}
    	else if(isUnlimited.equals("0"))
    	{
    		showWarningNotUnlimitedAccessDialog(mainActivity);
    	}
    	else if(isUnlimited.equals("1"))
    	{
    	}

	}

	private static void showWarningNotUnlimitedAccessDialog(final Activity mainActivity) 
	{
		if(warningAnonymousAccessDialogHasBeenShown)
			return;
		warningAnonymousAccessDialogHasBeenShown = true;
		
		final Dialog dialog = new Dialog(mainActivity);
		dialog.setContentView(com.mixvibes.mvlib.R.layout.dialog_rdio_not_unlimited);
		dialog.setTitle(com.mixvibes.mvlib.R.string.dialogtitle_rdio_not_unlimited);
		
		Button getPremium = (Button) dialog.findViewById(com.mixvibes.mvlib.R.id.get_premium);
		getPremium.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				final String url = "http://www.rdio.com/settings/subscription/";
				mainActivity.startActivity (new Intent (Intent.ACTION_VIEW, Uri.parse (url)));	
			}

		});
		
		dialog.show();
	}

	private static void showWarningAnonymousAccessDialog(final Activity mainActivity) 
	{
		if(warningNotUnlimitedAccessDialogHasBeenShown == true)
			return;
		warningNotUnlimitedAccessDialogHasBeenShown = true;
		
		final Dialog dialog = new Dialog(mainActivity);
		dialog.setContentView(com.mixvibes.mvlib.R.layout.dialog_rdio_anonymous);
		dialog.setTitle(com.mixvibes.mvlib.R.string.dialogtitle_rdio_anonymous);

		Button signup = (Button) dialog.findViewById(com.mixvibes.mvlib.R.id.sign_up);
		signup.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
			}

		});

		Button login = (Button) dialog.findViewById(com.mixvibes.mvlib.R.id.log_in);
		login.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				Class<?> widgetClass = null;
				try 
				{
					final String remoteMediaUtilsClass = "RemoteMediaUtils";
					widgetClass = Class.forName(remoteMediaUtilsClass);
				}
				catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				if (widgetClass != null) {
			    	Method method;
					try {
						method = widgetClass.getMethod("manageRdioSession", FragmentManager.class);
						if (method != null)
						{
							FragmentActivity fragmentActivity = (FragmentActivity) mainActivity;
							if(fragmentActivity != null)
								method.invoke(null, fragmentActivity.getFragmentManager());
							dialog.dismiss();
						}
					} catch (NoSuchMethodException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						e.printStackTrace();
					}
		    	}
			}

		});
		
		Button getPremium = (Button) dialog.findViewById(com.mixvibes.mvlib.R.id.get_premium);
		getPremium.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
			}

		});
		
		dialog.show();
	}
	
	
}
