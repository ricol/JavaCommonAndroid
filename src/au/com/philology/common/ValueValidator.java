package au.com.philology.common;

public class ValueValidator
{
	public static boolean isValidIP(String value)
	{
		if (value == null) return false;
		String regIP = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
				"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
		return value.matches(regIP);
	}
	
	public static boolean isValidInteger(String value)
	{
		if (value == null) return false;
		String regValue = "[0-9]*";
		return value.matches(regValue);
	}
}
