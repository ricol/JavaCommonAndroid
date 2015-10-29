package au.com.philology.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

public class DialogConfirm
{
	public int tag;
	public Object theData;
	public IDialogConfirmDelegate theDelegate;
	protected AlertDialog dialog;
	protected View theView;

	public DialogConfirm(Activity theActivity, String title, String msg, boolean customerView, int viewResource, IDialogConfirmDelegate delegate, int tag)
	{
		this.theDelegate = delegate;
		this.tag = tag;
		theView = null;

		if (customerView)
		{
			LayoutInflater factory = LayoutInflater.from(theActivity);
			final View textEntryView = factory.inflate(viewResource, null);
			theView = textEntryView;
		}

		final View dialogView = theView;

		dialog = new AlertDialog.Builder(theActivity).setIconAttribute(android.R.attr.alertDialogIcon).setTitle(title).setMessage(msg).setView(dialogView)
				.setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						if (theDelegate != null)
							theDelegate.dialogConfirmOnCancel(DialogConfirm.this, dialogView);
					}
				}).setPositiveButton("OK", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{
						if (theDelegate != null)
							theDelegate.dialogConfirmOnOk(DialogConfirm.this, dialogView);
					}
				}).create();
	}

	public void show()
	{
		dialog.show();
	}
}
