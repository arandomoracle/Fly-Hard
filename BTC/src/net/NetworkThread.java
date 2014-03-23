package net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.client.ClientProtocolException;

public class NetworkThread extends Thread {
	
	/** The data still to be sent */
	private ArrayList<String> dataBuffer;
	
	/** The data still to be read */
	private ArrayList<String> responseBuffer;
	
	/** The buffer mutex */
	private Object dataBufferMutex;
	
	/** The buffer mutex */
	private Object responseBufferMutex;
	
	/** The thread's status */
	private boolean status;
	
	/**
	 * Constructs a new thread for sending data.
	 */
	public NetworkThread() {
		this.dataBuffer = new ArrayList<String>();
		this.responseBuffer = new ArrayList<String>();
		this.dataBufferMutex = new Object();
		this.responseBufferMutex = new Object();
		this.status = true;
	}
	
	
	/**
	 * Sends data in the data buffer.
	 */
	@Override
	public void run() {
		while (status) {
			sendNextData();
		}
	}
	
	private void sendNextData() {
		HashMap<String, String> headers = null;
		
		// Obtain a lock on the data buffer
		synchronized(dataBufferMutex) {
			if ((dataBuffer.size() == 0) || (dataBuffer.get(0) == null)) {
				// Nothing to write, so exit
				return;
			} else {
				// Get the standard headers, along with a new header
				// containing the data to send
				headers = setupHeaders(dataBuffer.get(0));
				dataBuffer.remove(0);
			}
		}
		
		ArrayList<String> response = null;
		try {
			// Send the post request to the server, and read the response
			response = NetworkManager.httpPost(NetworkManager.SERVER_URL
					+ NetworkManager.POST_EXT, headers);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Write the response to the response buffer
		synchronized(responseBufferMutex) {
			responseBuffer.addAll(response);
			
			NetworkManager.print("<RECEIVED>");
			for (String string : response) {
				NetworkManager.print("             " + string);
			}
			NetworkManager.print("</RECEIVED>");
		}
	}
	
	public HashMap<String, String> setupHeaders(String data) {
		// Form the request headers
		HashMap<String, String> headers = new HashMap<String, String>();

		// Set user agent so that the server recognises this as a valid
		// request
		headers.put("user-agent", "Fly-Hard");

		// Add client's IP to the headers
		try {
			headers.put("ip", InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		// Add the data to the headers
		headers.put("data", data);
		
		// Return the headers
		return headers;
	}
	
	public void writeData(String data) {
		// Obtain a lock on the data buffer
		synchronized(dataBufferMutex) {
			// Write data to the buffer
			dataBuffer.add(data);
		}
	}
	
	public String readResponse() {
		// Obtain a lock on the response buffer
		synchronized(responseBufferMutex) {
			// Read data from the buffer
			if (responseBuffer.size() == 0) {
				// No data in the buffer
				return null;
			} else {
				String data = responseBuffer.get(0);
				responseBuffer.remove(0);
				return data;
			}
		}
	}
	
	public void end() {
		this.status = false;
	}
	
}
