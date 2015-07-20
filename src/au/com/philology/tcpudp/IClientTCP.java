package au.com.philology.tcpudp;

public interface IClientTCP extends IScanTCP
{
	public void send(String message);
	
	public void send(char[] data);

	public void connect(String serverAddress, int port);

	public void disconnect();

	public boolean isConnected();

	public String getServerIp();

	public int getServerPort();
}
