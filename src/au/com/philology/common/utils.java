package au.com.philology.common;

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
}
