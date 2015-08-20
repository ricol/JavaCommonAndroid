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
public class ClientCommunicationThread extends CommunicationThread
{

	IClientDelegate theClientDelegate;

	public ClientCommunicationThread(Socket aClient, IClientDelegate clientDelegate, ICommunicationThreadDelegate communicationThreadDelegate)
	{
		super(aClient, communicationThreadDelegate);

		this.theClientDelegate = clientDelegate;
	}

	@Override
	public void run()
	{
		if (this.theClientDelegate != null)
			this.theClientDelegate.ConnectionDelegateConnected(this.remoteAddress, this.remotePort);

		this.WaitsForData();

		if (this.theClientDelegate != null)
			this.theClientDelegate.ConnectionDelegateLostConnection(this.remoteAddress, this.remotePort);
	}

	@Override
	void dataReceived(String msg, String clientAddress, int clientPort)
	{
		super.dataReceived(msg, clientAddress, clientPort); // To change body of
															// generated
															// methods, choose
															// Tools |
															// Templates.

		if (this.theClientDelegate != null)
			this.theClientDelegate.ClientDelegateMessageReceived(msg, clientAddress, clientPort);
	}

}
