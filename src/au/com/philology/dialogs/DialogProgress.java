package au.com.philology.dialogs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.View;

public class DialogProgress
{
	ProgressDialog mProgressDialog;
	IDialogProgressDelegate theDelegate;
	boolean bAutoCountdown;
	MyThread theThread;
	Activity theActivity;
	String msg;
	View contentView;

	public DialogProgress(Activity theActivity, View contentView, String title,
			String msg, int max, boolean autoCountdown)
	{
		bAutoCountdown = autoCountdown;
		this.theActivity = theActivity;
		this.contentView = contentView;
		this.msg = msg;

		mProgressDialog = new ProgressDialog(theActivity);
		mProgressDialog.setIconAttribute(android.R.attr.alertDialogIcon);
		mProgressDialog.setTitle(title);
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setMax(max);
		mProgressDialog.setView(contentView);
		mProgressDialog.setMessage(msg);
		mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel",
				new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{

					}
				});
	}

	public void start()
	{
		if (mProgressDialog != null)
		{
			mProgressDialog.setProgress(0);
			mProgressDialog.show();
			if (theDelegate != null)
			{
				theDelegate.dialogProgressStarted(0,
						this.mProgressDialog.getMax());
			}

			if (bAutoCountdown)
			{
				theThread = new MyThread(this);
				theThread.start();
			}
		}
	}

	private class MyThread extends Thread
	{
		DialogProgress aDialog;

		public MyThread(final DialogProgress aDialog)
		{
			super();
			this.aDialog = aDialog;
		}

		@Override
		public void run()
		{
			super.run();

			if (aDialog.theActivity == null)
				return;

			for (int i = 0; i < aDialog.mProgressDialog.getMax(); i++)
			{
				if (this.isInterrupted())
					return;

				aDialog.theActivity.runOnUiThread(new Runnable()
				{

					@Override
					public void run()
					{
						aDialog.step(msg, 1, contentView);
					}
				});

				try
				{
					Thread.sleep(1000);
				} catch (InterruptedException e)
				{
					e.printStackTrace();
				}
			}

			aDialog.close();
		}
	}

	public void step(String msg, int value, View contentView)
	{
		if (mProgressDialog != null)
		{
			mProgressDialog.incrementProgressBy(value);

			if (theDelegate != null)
			{
				theDelegate.dialogProgressStepped(
						mProgressDialog.getProgress(), contentView);
			}
		}
	}

	public void close()
	{
		if (mProgressDialog != null)
		{
			if (theThread != null)
			{
				theThread.interrupt();
			}

			mProgressDialog.dismiss();

			if (theDelegate != null)
			{
				theDelegate.dialogProgressDismissed();
			}
		}
	}
}
