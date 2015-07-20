package au.com.philology.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;

public class DialogConfirm
{
	IDialogConfirmDelegate theDelegate;
	public int tag;
	AlertDialog dialog;

	public DialogConfirm(Activity theActivity, String title, String msg, View view, 
			IDialogConfirmDelegate delegate, int tag)
	{
		this.theDelegate = delegate;
		this.tag = tag;
		final View theView = view;
		
		dialog = new AlertDialog.Builder(theActivity)
				.setIconAttribute(android.R.attr.alertDialogIcon)
				.setTitle(title)
				.setMessage(msg)
				.setView(view)
				.setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						theDelegate.dialogConfirmOnOk(DialogConfirm.this, theView);
					}
				})
				.setNegativeButton("CANCEL",
						new DialogInterface.OnClickListener()
						{
							public void onClick(DialogInterface dialog,
									int whichButton)
							{
								theDelegate
										.dialogConfirmOnCancel(DialogConfirm.this, theView);
							}
						}).create();
	}
	
	public void show()
	{
		dialog.show();
	}
}
