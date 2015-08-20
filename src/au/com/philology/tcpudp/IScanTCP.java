package au.com.philology.tcpudp;

public interface IScanTCP
{

	public void startScaningIpForPort(int port, int ipStart, int ipEnd, int timeout);

	public void stopScaningIpForPort();

	public boolean isScanningIpForPort();

	public void startScanningPortForIp(String ip, int timeout, int startPort, int endPort);

	public void stopScanningPortForIp();

	public boolean isScanningPortForIp();
}
