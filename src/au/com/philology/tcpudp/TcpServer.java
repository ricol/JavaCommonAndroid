package au.com.philology.tcpudp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpServer extends BasicNet implements IServerTCP
{

	ServerSocket theServerSocketForListening;
	public int port;
	IServerDelegate theServerDelegate;

	public TcpServer(IServerDelegate serverDelegate, IScanDelegate scanDelegate)
	{
		super(scanDelegate);
		this.theServerDelegate = serverDelegate;
	}

	public TcpServer(IServerDelegate serverDelegate)
	{
		super(null);
		this.theServerDelegate = serverDelegate;
	}

	@Override
	public void send(String message, String address, int Port)
	{
		synchronized (allCommunicationThreads)
		{
			for (CommunicationThread thread : this.allCommunicationThreads)
			{
				if (thread.remoteAddress.equals(address) && thread.remotePort == Port)
				{
					thread.sendMessage(message);
					break;
				}
			}
		}
	}

	@Override
	public void startListening(int port)
	{
		this.stopListenning();

		this.port = port;
		new Thread(new ServerThread(this.theServerSocketForListening, this.port, this)).start();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void stopListenning()
	{
		ArrayList<CommunicationThread> tmpThreads = new ArrayList<CommunicationThread>();
		tmpThreads.addAll(this.allCommunicationThreads);

		for (CommunicationThread thread : tmpThreads)
		{
			try
			{
				if (thread.aClientSocket != null)
				{
					if (thread.aClientSocket.isConnected())
					{
						thread.aClientSocket.close();
						thread.aClientSocket = null;
					}
				}
			} catch (IOException ex)
			{
				Logger.getLogger(TcpServer.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		tmpThreads.clear();

		try
		{
			// TODO Auto-generated method stub
			if (this.theServerSocketForListening != null)
			{
				if (!this.theServerSocketForListening.isClosed())
				{
					this.theServerSocketForListening.close();
					this.theServerSocketForListening = null;
				}
			}
		} catch (IOException ex)
		{
			Logger.getLogger(TcpServer.class.getName()).log(Level.SEVERE, null, ex);
		}

		this.theServerDelegate.ServerDelegateStopListening(this.port);
	}

	@Override
	public boolean isListenning()
	{
		if (this.theServerSocketForListening != null)
		{
			return !this.theServerSocketForListening.isClosed();
		}
		return false;
	}

	class ServerThread implements Runnable
	{

		ICommunicationThreadDelegate communicationThreadDelegate;
		int port;

		public ServerThread(ServerSocket ServerSocket, int port, ICommunicationThreadDelegate communicationThreadDelegate)
		{
			this.port = port;
			this.communicationThreadDelegate = communicationThreadDelegate;
		}

		@Override
		public void run()
		{
			try
			{
				stopListenning();

				theServerSocketForListening = new ServerSocket(this.port);

				// TODO Auto-generated method stub
				while (!Thread.currentThread().isInterrupted())
				{
					try
					{
						theServerDelegate.ServerDelegateStartListening(this.port);
						Socket aClient = theServerSocketForListening.accept();
						CommunicationThread aThread = new ServerCommunicationThread(aClient, ServerCommunicationThread.DEFAULT_WELCOME_MSG, theServerDelegate,
								this.communicationThreadDelegate);
						aThread.start();
					} catch (IOException e)
					{
						e.printStackTrace();
						break;
					}

				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			theServerDelegate.ServerDelegateStopListening(this.port);
		}
	}

	@Override
	public ArrayList<String> getAllClientsIp()
	{
		ArrayList<String> allClients = new ArrayList<String>();

		synchronized (allCommunicationThreads)
		{
			for (int i = 0; i < this.allCommunicationThreads.size(); i++)
			{
				CommunicationThread thread = this.allCommunicationThreads.get(i);

				if (thread.isAlive())
				{
					allClients.add(thread.remoteAddress);
				}
			}
		}

		return allClients;
	}

	@Override
	public ArrayList<Integer> getAllClientsPort()
	{
		ArrayList<Integer> allClients = new ArrayList<Integer>();

		synchronized (allCommunicationThreads)
		{
			for (int i = 0; i < this.allCommunicationThreads.size(); i++)
			{
				CommunicationThread thread = this.allCommunicationThreads.get(i);

				if (thread.isAlive())
				{
					allClients.add(thread.remotePort);
				}
			}
		}

		return allClients;
	}

	@Override
	public void broadcast(String msg)
	{
		synchronized (allCommunicationThreads)
		{
			for (CommunicationThread thread : this.allCommunicationThreads)
			{
				thread.sendMessage(msg);
			}
		}
	}
}
