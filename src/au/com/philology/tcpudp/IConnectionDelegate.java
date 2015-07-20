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
public interface IConnectionDelegate
{

	public void ConnectionDelegateConnected(String address, int port);

	public void ConnectionDelegateLostConnection(String address, int port);
}
