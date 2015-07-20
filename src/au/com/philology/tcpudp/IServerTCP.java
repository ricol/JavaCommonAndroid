package au.com.philology.tcpudp;

import java.util.ArrayList;

public interface IServerTCP extends IScanTCP
{
	public void broadcast(String msg);

	public void send(String message, String address, int Port);

	public void startListening(int port);

	public void stopListenning();

	public boolean isListenning();

	public ArrayList<String> getAllClientsIp();

	public ArrayList<Integer> getAllClientsPort();
}
