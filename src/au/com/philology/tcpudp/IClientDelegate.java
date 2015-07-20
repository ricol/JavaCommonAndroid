package au.com.philology.tcpudp;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ricolwang
 */
public interface IClientDelegate extends IConnectionDelegate
{

	public void ClientDelegateMessageReceived(String msg, String address,
			int port);
}
