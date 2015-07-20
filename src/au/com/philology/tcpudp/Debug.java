package au.com.philology.tcpudp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ricolwang
 */
public class Debug
{

	public static String tag = "DEBUG";

	public static void print(String message)
	{
		System.out.println(tag + ": " + message);
	}
}
