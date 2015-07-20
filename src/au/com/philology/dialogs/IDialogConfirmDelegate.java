package au.com.philology.dialogs;

import android.view.View;

public interface IDialogConfirmDelegate
{
	public void dialogConfirmOnOk(DialogConfirm theDialog, View theContentView);
	
	public void dialogConfirmOnCancel(DialogConfirm theDialog, View theContentView);
}
