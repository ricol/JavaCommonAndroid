/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package au.com.philology.bluetoothservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import au.com.philology.common.utils;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for incoming
 * connections, a thread for connecting with a device, and a thread for
 * performing data transmissions when connected.
 */
public class BluetoothChatService
{
	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int DISCOVERY_TIME = 300;
	private String nameSecure;
	private UUID uuidSecure;
	public Context context;
	public ArrayAdapter<String> mPairedDevicesArrayAdapter;
	public ArrayAdapter<String> mNewDevicesArrayAdapter;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Member fields
	private BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private AcceptThread mSecureAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private int mState;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming
												// connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote
													// device

	public boolean bIsListenning;

	/**
	 * Constructor. Prepares a new BluetoothChat session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param handler
	 *            A Handler to send messages back to the UI Activity
	 */
	public BluetoothChatService(Context context, Handler handler, String nameSecure, UUID uuid)
	{
		this.mAdapter = BluetoothAdapter.getDefaultAdapter();

		this.mState = STATE_NONE;
		this.context = context;
		this.mHandler = handler;
		this.nameSecure = nameSecure;
		this.uuidSecure = uuid;

		// Initialize array adapters. One for already paired devices and
		// one for newly discovered devices
		mPairedDevicesArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
		mNewDevicesArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1);
	}

	public void updateName(String name)
	{
		this.mAdapter.setName(name);
	}

	public void scan()
	{
		// If we're already discovering, stop it
		this.stopScan();

		utils.print("scan...");

		this.mPairedDevicesArrayAdapter.clear();
		this.mNewDevicesArrayAdapter.clear();

		// Register for broadcasts when a device is discovered
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.context.registerReceiver(mReceiver, filter);

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.context.registerReceiver(mReceiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		this.context.registerReceiver(mReceiver, filter);

		// Request discover from BluetoothAdapter
		// Get a set of currently paired devices
		Set<BluetoothDevice> pairedDevices = mAdapter.getBondedDevices();

		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0)
		{
			for (BluetoothDevice device : pairedDevices)
			{
				mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		} else
		{
			mPairedDevicesArrayAdapter.add("No paired devices.");
		}
		mAdapter.startDiscovery();
	}

	public void stopScan()
	{
		utils.print("stopScan.");

		if (mAdapter.isDiscovering())
		{
			mAdapter.cancelDiscovery();
		}
	}

	public boolean isScanning()
	{
		return this.mAdapter.isDiscovering();
	}

	/**
	 * Set the current state of the chat connection
	 * 
	 * @param state
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(int state)
	{
		utils.print("setState() " + mState + " -> " + state);
		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(BluetoothChatService.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	/**
	 * Return the current connection state.
	 */
	public synchronized int getState()
	{
		return mState;
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void listen()
	{
		utils.print("start");

		this.stopListen();

		setState(STATE_LISTEN);

		if (mAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
		{
			Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVERY_TIME);
			this.context.startActivity(discoverableIntent);
		}

		// Start the thread to listen on a BluetoothServerSocket
		if (mSecureAcceptThread == null)
		{
			mSecureAcceptThread = new AcceptThread(true);
		}

		mSecureAcceptThread.start();

		this.bIsListenning = true;
	}

	public void stopListen()
	{
		// Cancel any thread attempting to make a connection
		if (mConnectThread != null)
		{
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null)
		{
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mSecureAcceptThread != null)
		{
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}

		setState(STATE_NONE);

		this.bIsListenning = false;
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 * @param secure
	 *            Socket Security type - Secure (true) , Insecure (false)
	 */
	public synchronized void connect(String address, boolean secure)
	{
		utils.print("connect to: " + address);

		BluetoothDevice device = this.mAdapter.getRemoteDevice(address);

		// Cancel any thread attempting to make a connection
		if (mState == STATE_CONNECTING)
		{
			if (mConnectThread != null)
			{
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null)
		{
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device, secure);
		mConnectThread.start();
		setState(STATE_CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	private synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType)
	{
		utils.print("connected, Socket Type:" + socketType);

		// Cancel the thread that completed the connection
		if (mConnectThread != null)
		{
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null)
		{
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Cancel the accept thread because we only want to connect to one
		// device
		if (mSecureAcceptThread != null)
		{
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket, socketType);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = mHandler.obtainMessage(BluetoothChatService.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothChatService.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		setState(STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop()
	{
		utils.print("stop");

		if (mConnectThread != null)
		{
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null)
		{
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mSecureAcceptThread != null)
		{
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}

		setState(STATE_NONE);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out)
	{
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this)
		{
			if (mState != STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	private void connectionFailed()
	{
		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(BluetoothChatService.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothChatService.TOAST, "Unable to connect device");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost()
	{
		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(BluetoothChatService.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(BluetoothChatService.TOAST, "Device connection was lost");
		msg.setData(bundle);
		mHandler.sendMessage(msg);
	}

	/**
	 * This thread runs while listening for incoming connections. It behaves
	 * like a server-side client. It runs until a connection is accepted (or
	 * until cancelled).
	 */
	private class AcceptThread extends Thread
	{
		// The local server socket
		private final BluetoothServerSocket mmServerSocket;
		private String mSocketType;

		public AcceptThread(boolean secure)
		{
			BluetoothServerSocket tmp = null;
			mSocketType = "Secure";

			// Create a new listening server socket
			try
			{
				tmp = mAdapter.listenUsingRfcommWithServiceRecord(nameSecure, uuidSecure);
			} catch (IOException e)
			{
				utils.print("Socket Type: " + mSocketType + "listen() failed! exception: " + e);
			}
			mmServerSocket = tmp;
		}

		public void run()
		{
			utils.print("Socket Type: " + mSocketType);
			utils.print("BEGIN mAcceptThread" + this);
			setName("AcceptThread" + mSocketType);

			BluetoothSocket socket = null;

			// Listen to the server socket if we're not connected
			while (mState != STATE_CONNECTED)
			{
				try
				{
					// This is a blocking call and will only return on a
					// successful connection or an exception
					socket = mmServerSocket.accept();
				} catch (IOException e)
				{
					utils.print("Socket Type: " + mSocketType + "accept() failed! exception: " + e);
					break;
				}

				// If a connection was accepted
				if (socket != null)
				{
					synchronized (BluetoothChatService.this)
					{
						switch (mState)
						{
							case STATE_LISTEN:
								break;
							case STATE_CONNECTING:
								// Situation normal. Start the connected thread.
								connected(socket, socket.getRemoteDevice(), mSocketType);
								break;
							case STATE_NONE:
								break;
							case STATE_CONNECTED:
								// Either not ready or already connected.
								// Terminate
								// new socket.
								try
								{
									socket.close();
								} catch (IOException e)
								{
									utils.print("Could not close unwanted socket! exception: " + e);
								}
								break;
						}
					}
				}
			}

			utils.print("END mAcceptThread, socket Type: " + mSocketType);

		}

		public void cancel()
		{
			utils.print("Socket Type" + mSocketType + "cancel " + this);
			try
			{
				mmServerSocket.close();
			} catch (IOException e)
			{
				utils.print("Socket Type" + mSocketType + "close() of server failed! exception: " + e);
			}
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread
	{
		private final BluetoothSocket mmSocket;
		private final BluetoothDevice mmDevice;
		private String mSocketType;

		public ConnectThread(BluetoothDevice device, boolean secure)
		{
			mmDevice = device;
			BluetoothSocket tmp = null;
			mSocketType = secure ? "Secure" : "Insecure";

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			try
			{
				tmp = device.createRfcommSocketToServiceRecord(uuidSecure);
			} catch (IOException e)
			{
				utils.print("Socket Type: " + mSocketType + "create() failed! exception: " + e);
			}
			mmSocket = tmp;
		}

		public void run()
		{
			utils.print("BEGIN mConnectThread SocketType:" + mSocketType);
			setName("ConnectThread" + mSocketType);

			// Always cancel discovery because it will slow down a connection
			mAdapter.cancelDiscovery();

			// Make a connection to the BluetoothSocket
			try
			{
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mmSocket.connect();
			} catch (IOException e)
			{
				// Close the socket
				try
				{
					mmSocket.close();
				} catch (IOException e2)
				{
					utils.print("unable to close() " + mSocketType + " socket during connection failure! Exception: " + e2);
				}
				connectionFailed();
				return;
			}

			// Reset the ConnectThread because we're done
			synchronized (BluetoothChatService.this)
			{
				mConnectThread = null;
			}

			// Start the connected thread
			connected(mmSocket, mmDevice, mSocketType);
		}

		public void cancel()
		{
			try
			{
				mmSocket.close();
			} catch (IOException e)
			{
				utils.print("close() of connect " + mSocketType + " socket failed! Exception: " + e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread
	{
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(BluetoothSocket socket, String socketType)
		{
			utils.print("create ConnectedThread: " + socketType);
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the BluetoothSocket input and output streams
			try
			{
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e)
			{
				utils.print("temp sockets not created! Exception: " + e);
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
		}

		public void run()
		{
			utils.print("BEGIN mConnectedThread");
			byte[] buffer = new byte[1024];
			int bytes;

			// Keep listening to the InputStream while connected
			while (true)
			{
				try
				{
					// Read from the InputStream
					bytes = mmInStream.read(buffer);

					// Send the obtained bytes to the UI Activity
					mHandler.obtainMessage(BluetoothChatService.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
				} catch (IOException e)
				{
					utils.print("disconnected! Exception: " + e);
					connectionLost();
					// Start the service over to restart listening mode
					// BluetoothChatService.this.start();
					break;
				}
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		public void write(byte[] buffer)
		{
			try
			{
				mmOutStream.write(buffer);

				// Share the sent message back to the UI Activity
				mHandler.obtainMessage(BluetoothChatService.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
			} catch (IOException e)
			{
				utils.print("Exception during write! Exception: " + e);
			}
		}

		public void cancel()
		{
			try
			{
				mmSocket.close();
			} catch (IOException e)
			{
				utils.print("close() of connect socket failed! Exception: " + e);
			}
		}
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver()
	{
		@SuppressLint("NewApi")
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// If it's already paired, skip it, because it's been listed
				// already
				// if (device.getBondState() != BluetoothDevice.BOND_BONDED)
				// {
				mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				// }
				// When discovery is finished, change the Activity title
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
				if (mNewDevicesArrayAdapter.getCount() == 0)
				{
					mNewDevicesArrayAdapter.add("No found");
				} else
					mNewDevicesArrayAdapter.add("complete.");
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
			{
				utils.print("end discovery.");
			}
		}
	};

	public boolean isListenning()
	{
		return this.bIsListenning;
	}

	public void test()
	{

	}

	public boolean check()
	{
		// If the adapter is null, then Bluetooth is not supported
		mAdapter = BluetoothAdapter.getDefaultAdapter();

		if (this.mAdapter == null)
		{
			Toast.makeText(this.context, "Bluetooth is not available!", Toast.LENGTH_LONG).show();
			return false;
		}

		if (!this.mAdapter.isEnabled())
		{
			Toast.makeText(this.context, "Bluetooth is not enabled!", Toast.LENGTH_LONG).show();
			return false;
		}

		return true;
	}
}
