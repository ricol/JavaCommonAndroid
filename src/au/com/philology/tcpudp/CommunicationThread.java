package au.com.philology.tcpudp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class CommunicationThread extends Thread
{

	Socket aClientSocket;
	String remoteAddress;
	int remotePort;
	BufferedReader read;
	PrintWriter out;
	ICommunicationThreadDelegate communicationThreadDelegate;

	public CommunicationThread(Socket aClient,
			ICommunicationThreadDelegate communicationThreadDelegate)
	{
		this.communicationThreadDelegate = communicationThreadDelegate;
		this.aClientSocket = aClient;
		String[] remoteIp = this.aClientSocket.getRemoteSocketAddress()
				.toString().split(":");
		this.remoteAddress = remoteIp[0].split("/")[1];
		this.remotePort = Integer.parseInt(remoteIp[1]);
		try
		{
			this.read = new BufferedReader(new InputStreamReader(
					this.aClientSocket.getInputStream()));
			this.out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(aClient.getOutputStream())), true);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void sendMessage(String msg)
	{
		this.out.write(msg + "\n");
		this.out.flush();
	}
	
	public void sendMessage(char[] data)
	{
		this.out.write(data);
		this.out.flush();
	}

	@Override
	abstract public void run();

	void WaitsForData()
	{
		if (this.communicationThreadDelegate != null)
			this.communicationThreadDelegate.CommunicationThreadStart(this);
		while (!Thread.currentThread().isInterrupted())
		{
			String content;
			try
			{
				content = this.read.readLine();
				if (content == null)
				{
					this.aClientSocket.close();
					break;
				}
				this.dataReceived(content, this.remoteAddress, this.remotePort);
			} catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
		}

		if (this.communicationThreadDelegate != null)
			this.communicationThreadDelegate.CommunicationThreadEnd(this);

	}

	void dataReceived(String msg, String clientAddress, int clientPort)
	{

	}
}
