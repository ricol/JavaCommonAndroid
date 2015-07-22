package au.com.philology.dialogs;

import android.view.View;

public interface IDialogProgressDelegate
{
	public void dialogProgressStarted(int min, int max);
	
	public void dialogProgressStepped(int value, View contentView);
	
	public void dialogProgressDismissed();
}
