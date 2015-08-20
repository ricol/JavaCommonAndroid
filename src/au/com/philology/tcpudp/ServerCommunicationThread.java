/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.com.philology.tcpudp;

import java.net.Socket;

/**
 *
 * @author ricolwang
 */
public class ServerCommunicationThread extends CommunicationThread
{

	String welcomeMsg;
	IServerDelegate theServerDelegate;

	public final static String DEFAULT_WELCOME_MSG = "Welcome!";

	public ServerCommunicationThread(Socket aClient, String welcomeMsg, IServerDelegate serverdelegate, ICommunicationThreadDelegate communicationThreadDelegate)
	{
		super(aClient, communicationThreadDelegate);
		this.welcomeMsg = welcomeMsg;
		this.theServerDelegate = serverdelegate;
	}

	@Override
	public void run()
	{
		if (this.theServerDelegate != null)
			this.theServerDelegate.ConnectionDelegateConnected(this.remoteAddress, this.remotePort);

		this.sendMessage(this.welcomeMsg);

		this.WaitsForData();

		if (this.theServerDelegate != null)
			this.theServerDelegate.ConnectionDelegateLostConnection(this.remoteAddress, this.remotePort);

	}

	@Override
	void dataReceived(String msg, String clientAddress, int clientPort)
	{
		super.dataReceived(msg, clientAddress, clientPort); // To change body of
															// generated
															// methods, choose
															// Tools |
															// Templates.

		if (this.theServerDelegate != null)
			this.theServerDelegate.ServerDelegateClientMessageReceived(msg, clientAddress, clientPort);
	}
}
