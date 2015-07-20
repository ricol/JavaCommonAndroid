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
public interface IScanDelegate
{

	void ScanDelegateIsScanningIpForPort(String serverAddress, int port);

	void ScanDelegateIpFoundForPort(String serverAddress, int port);

	void ScanDelegateCompleteScanningIpForPort();

	void ScanDelegateStartScanningIpForPort();

	void ScanDelegateStartScanningPortForIp(String ip, int startPort,
			int endPort);

	void ScanDelegateIsScanningPortForIp(String ip, int port);

	void ScanDelegatePortFoundForIp(String ip, int port);

	void ScanDelegateCompleteScanningPortForIp(String ip, int startPort,
			int endPort);
}
