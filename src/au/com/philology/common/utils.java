package au.com.philology.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

public class utils
{
	public static String tag = "DEBUG";

	public static void print(String message)
	{
		Log.i(tag, message);
	}

	public static void print(String message, String tag)
	{
		Log.i(tag, message);
	}

	public static void showDialog(Activity theActivity, String title, String msg)
	{
		AlertDialog dialog = new AlertDialog.Builder(theActivity)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle(title).setMessage(msg)
				.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{

					}
				}).create();
		dialog.show();
	}
}
