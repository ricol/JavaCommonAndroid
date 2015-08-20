package au.com.philology.tcpudp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

public class ScannerPortForIpThread extends Thread
{
	String ip;
	int startPort;
	int endPort;
	int timeout;
	IScanDelegate scanDelegate;

	public ScannerPortForIpThread(String ip, int timeout, int startPort, int endPort, IScanDelegate scanDelegate)
	{
		this.ip = ip;
		this.startPort = startPort;
		this.endPort = endPort;
		this.timeout = timeout;
		this.scanDelegate = scanDelegate;
	}

	@Override
	public void run()
	{
		int tmpStart = this.startPort >= 0 && this.startPort <= 255 * 255 ? this.startPort : 0;
		int tmpEnd = this.endPort >= tmpStart && this.endPort <= 255 * 255 ? this.endPort : 255 * 255;
		int tmpTimeout = this.timeout >= 10 && this.timeout <= 1000 ? this.timeout : 50;

		if (scanDelegate != null)
		{
			scanDelegate.ScanDelegateStartScanningPortForIp(ip, tmpStart, tmpEnd);
		}

		for (int i = tmpStart; i <= tmpEnd; i++)
		{
			if (this.isInterrupted())
			{
				break;
			}

			Socket mySocket = new Socket();
			SocketAddress address = new InetSocketAddress(ip, i);

			try
			{
				if (scanDelegate != null)
				{
					scanDelegate.ScanDelegateIsScanningPortForIp(ip, i);
				}

				mySocket.connect(address, tmpTimeout);
				mySocket.close();

				if (scanDelegate != null)
				{
					scanDelegate.ScanDelegatePortFoundForIp(ip, i);
				}

			} catch (IllegalArgumentException e)
			{
				Debug.print("IllegalArgumentException: " + e);
			} catch (SocketException e)
			{
				Debug.print("SocketException: " + e);
			} catch (UnsupportedOperationException e)
			{
				Debug.print("UnsupportedOperationException: " + e);
			} catch (IOException e)
			{
				Debug.print("IOException: " + e);
			}
		}

		if (scanDelegate != null)
		{
			scanDelegate.ScanDelegateCompleteScanningPortForIp(ip, tmpStart, tmpEnd);
		}
	}
}