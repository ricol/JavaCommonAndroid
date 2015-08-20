package au.com.philology.tcpudp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpClient extends BasicNet implements IClientTCP
{
	Socket theConnectionSocket;
	ClientCommunicationThread theCommunicationThread;
	IClientDelegate theClientDelegate;

	public TcpClient(IClientDelegate clientDelegate, IScanDelegate scanDelegate)
	{
		super(scanDelegate);
		this.theClientDelegate = clientDelegate;
	}

	@Override
	public void send(String message)
	{
		if (this.theCommunicationThread != null)
		{
			this.theCommunicationThread.sendMessage(message);
		}
	}

	@Override
	public void connect(String serverAddress, int port)
	{
		this.disconnect();

		new Thread(new ClientThread(serverAddress, port, this)).start();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void disconnect()
	{
		if (this.theConnectionSocket != null)
		{
			try
			{
				if (this.theConnectionSocket.isConnected())
				{
					if (this.theCommunicationThread != null)
					{
						this.theCommunicationThread.interrupt();
						this.theCommunicationThread = null;
					}
					this.theConnectionSocket.close();
					this.theConnectionSocket = null;
				}
			} catch (IOException ex)
			{
				Logger.getLogger(TcpClient.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	@Override
	public boolean isConnected()
	{
		if (theConnectionSocket != null)
		{
			return theConnectionSocket.isConnected();
		} else
		{
			return false;
		}
	}

	@Override
	public void CommunicationThreadStart(CommunicationThread thread)
	{
	}

	@Override
	public void CommunicationThreadEnd(CommunicationThread thread)
	{
	}

	class ClientThread implements Runnable
	{

		ICommunicationThreadDelegate communicationThreadDelegate;
		String serverAddress;
		int port;

		public ClientThread(String serverAddress, int port, ICommunicationThreadDelegate communicationThreadDelegate)
		{
			this.serverAddress = serverAddress;
			this.port = port;
			this.communicationThreadDelegate = communicationThreadDelegate;
		}

		@Override
		public void run()
		{
			try
			{
				disconnect();

				InetAddress serverAddress = InetAddress.getByName(this.serverAddress);

				theConnectionSocket = new Socket(serverAddress, this.port);
				theCommunicationThread = new ClientCommunicationThread(theConnectionSocket, theClientDelegate, this.communicationThreadDelegate);
				theCommunicationThread.start();
			} catch (UnknownHostException e)
			{
				e.printStackTrace();
			} catch (IOException e)
			{
				e.printStackTrace();
			}

		}

	}

	@Override
	public String getServerIp()
	{
		if (this.isConnected())
		{
			return this.theCommunicationThread.remoteAddress;
		}
		return null;
	}

	@Override
	public int getServerPort()
	{
		if (this.isConnected())
		{
			return this.theCommunicationThread.remotePort;
		}
		return -1;
	}

	@Override
	public void send(char[] data)
	{
		if (this.theCommunicationThread != null)
		{
			this.theCommunicationThread.sendMessage(data);
		}
	}

}
