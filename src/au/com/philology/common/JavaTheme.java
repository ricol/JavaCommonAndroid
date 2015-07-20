package au.com.philology.common;

public class JavaTheme
{

	public static String tag = "DEBUG";

	public static void print(String message)
	{
		System.out.println(tag + ": " + message);
	}

	public final static String LOOKANDFEEL_METAL = "Metal";
	public final static String LOOKANDFEEL_SYSTEM = "System";
	public final static String LOOKANDFEEL_MOTIF = "Motif";
	public final static String LOOKANDFEEL_GTK = "GTK";
}
