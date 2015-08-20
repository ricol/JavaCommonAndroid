/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package au.com.philology.tcpudp;

/**
 *
 * @author ricolwang
 */
public interface IServerDelegate extends IConnectionDelegate
{

	public void ServerDelegateClientMessageReceived(String msg, String clientAddress, int clientPort);

	public void ServerDelegateStartListening(int port);

	public void ServerDelegateStopListening(int port);
}
