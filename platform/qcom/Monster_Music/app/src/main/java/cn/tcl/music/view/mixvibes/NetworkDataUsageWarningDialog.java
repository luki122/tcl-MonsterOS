package cn.tcl.music.view.mixvibes;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import cn.tcl.music.util.Connectivity;
import mst.app.dialog.AlertDialog;


public class NetworkDataUsageWarningDialog extends DialogFragment {
	public final Context context;
	public static final String PREFS_NAME = "NetworkDataUsageWarningDialog";
	
	public NetworkDataUsageWarningDialog(Context context) 
	{
		this.context = context;
	}
	
	static void resetWarning(Context context)
	{
    	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.clear();
    	editor.commit();
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) 
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(com.mixvibes.mvlib.R.layout.networkdatausagewarningdialog, null);
        final CheckBox dontShowAgain = (CheckBox) layout.findViewById(com.mixvibes.mvlib.R.id.skip);
        
        builder.setView(layout);
        builder.setTitle("Data usage warning");
        builder.setMessage(Html.fromHtml("Online music content will use a large amount of data."));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int which) 
            {
                String checkBoxResult = "NOT checked";
                if (dontShowAgain.isChecked())
                    checkBoxResult = "checked";
                SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("skipMessage", checkBoxResult);
                editor.commit();
                return;
            }
        });

        return builder.create();
    }
    
    @Override
    public void show(FragmentManager manager, String tag)
    {
    	//if(Connectivity.isConnectedWifi(context)) Use this line to test functionality while in WIFI MODE.
    	if(Connectivity.isConnectedMobile(context)) {
        	SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
            String skipMessage = settings.getString("skipMessage", "NOT checked");
            if (!skipMessage.equals("checked"))
            	super.show(manager, tag);
    	}
    }
}