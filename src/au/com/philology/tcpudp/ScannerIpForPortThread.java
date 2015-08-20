package au.com.philology.tcpudp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

public class ScannerIpForPortThread extends Thread
{

	int port;
	int ipStart;
	int ipEnd;
	int timeout;
	IScanDelegate scanDelegate;

	public ScannerIpForPortThread(int port, int ipStart, int ipEnd, int timeout, IScanDelegate scanDelegate)
	{
		this.port = port;
		this.ipStart = ipStart;
		this.ipEnd = ipEnd;
		this.timeout = timeout;
		this.scanDelegate = scanDelegate;
	}

	@Override
	public void run()
	{
		String iIPv4 = IPAddress.getLocalAddress();
		iIPv4 = iIPv4.substring(0, iIPv4.lastIndexOf("."));
		iIPv4 += ".";

		if (scanDelegate != null)
		{
			scanDelegate.ScanDelegateStartScanningIpForPort();
		}

		int tmpStart = this.ipStart >= 1 && this.ipStart <= 254 ? this.ipStart : 1;
		int tmpEnd = this.ipEnd >= tmpStart && this.ipEnd <= 254 ? this.ipEnd : 254;
		int tmpTimeout = this.timeout >= 10 && this.timeout <= 1000 ? this.timeout : 50;

		for (int i = tmpStart; i <= tmpEnd; i++)
		{
			if (this.isInterrupted())
			{
				break;
			}

			System.out.println(iIPv4 + i);
			Socket mySocket = new Socket();
			final String ip = iIPv4 + i;
			SocketAddress address = new InetSocketAddress(ip, port);

			if (scanDelegate != null)
			{
				scanDelegate.ScanDelegateIsScanningIpForPort(ip, port);
			}

			try
			{
				mySocket.connect(address, tmpTimeout);
				mySocket.close();
				if (scanDelegate != null)
				{
					scanDelegate.ScanDelegateIpFoundForPort(ip, port);
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
			scanDelegate.ScanDelegateCompleteScanningIpForPort();
		}
	}
}