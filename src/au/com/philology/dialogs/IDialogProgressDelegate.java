package au.com.philology.dialogs;

public interface IDialogProgressDelegate
{
	public void dialogProgressStarted(int min, int max);
	
	public void dialogProgressStepped(int value);
	
	public void dialogProgressDismissed();
}
