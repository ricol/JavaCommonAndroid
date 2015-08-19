package au.com.philology.tcpudp;

import java.util.ArrayList;

public class BasicNet implements IScanTCP, ICommunicationThreadDelegate
{

	ArrayList<CommunicationThread> allCommunicationThreads = new ArrayList<>();
	IScanDelegate scanDelegate;
	ScannerIpForPortThread theScannerIpForPortThread;
	ScannerPortForIpThread theScannerPortForIpThread;

	public BasicNet(IScanDelegate scanDelegate)
	{
		this.scanDelegate = scanDelegate;
	}

	@Override
	public void startScaningIpForPort(int port, int ipStart, int ipEnd, int timeout)
	{
		this.stopScaningIpForPort();

		theScannerIpForPortThread = new ScannerIpForPortThread(port, ipStart, ipEnd, timeout, this.scanDelegate);
		theScannerIpForPortThread.start();
	}

	@Override
	public void stopScaningIpForPort()
	{
		if (theScannerIpForPortThread != null)
		{
			theScannerIpForPortThread.interrupt();
			theScannerIpForPortThread = null;
		}
	}

	@Override
	public boolean isScanningIpForPort()
	{
		if (theScannerIpForPortThread != null)
		{
			return theScannerIpForPortThread.isAlive();
		}

		return false;
	}

	@Override
	public void CommunicationThreadStart(CommunicationThread thread)
	{
		synchronized (allCommunicationThreads)
		{
			this.allCommunicationThreads.add(thread);
		}
	}

	@Override
	public void CommunicationThreadEnd(CommunicationThread thread)
	{
		synchronized (allCommunicationThreads)
		{
			this.allCommunicationThreads.remove(thread);
		}
	}

	@Override
	public void startScanningPortForIp(String ip, int timeout, int startPort, int endPort)
	{
		this.stopScanningPortForIp();

		theScannerPortForIpThread = new ScannerPortForIpThread(ip, timeout, startPort, endPort, this.scanDelegate);
		theScannerPortForIpThread.start();
	}

	@Override
	public void stopScanningPortForIp()
	{
		if (theScannerPortForIpThread != null)
		{
			theScannerPortForIpThread.interrupt();
			theScannerPortForIpThread = null;
		}
	}

	@Override
	public boolean isScanningPortForIp()
	{
		if (theScannerPortForIpThread != null)
		{
			return theScannerPortForIpThread.isAlive();
		}

		return false;
	}

	public String getLocalIp()
	{
		return IPAddress.getLocalAddress();
	}
}
