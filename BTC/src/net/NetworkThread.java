package net;

import java.io.Serializable;

import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Thread used to transfer data in parallel with the game.
 * <p>
 * This thread provides no guarantees regarding the time between
 * data being passed to the queue and data being sent.
 * </p>
 * <p>
 * There is also no guarantee that data written to the thread
 * will ever get sent.
 * </p>
 */
public class NetworkThread extends Thread {

	/** The data still to be sent */
	private TreeMap<Long, Serializable> dataBuffer;
	
	/** The messages still to be sent */
	private String messages;
	
	/** The data still to be read */
	private TreeMap<Long, Serializable> responseBuffer;
	
	/** The most recent data received so far */
	private long mostRecent;
	
	/** The thread's status */
	private boolean status;
	
	/** The status mutex */
	private Object statusMutex;
	
	
	/**
	 * Constructs a new thread for sending data.
	 */
	public NetworkThread() {
		this.dataBuffer = new TreeMap<Long, Serializable>();
		this.messages = "";
		this.responseBuffer = new TreeMap<Long, Serializable>();
		this.mostRecent = Long.MAX_VALUE;
		this.status = true;
		this.statusMutex = new Object();
	}
	
	
	/**
	 * Sends data in the data buffer.
	 */
	@Override
	public void run() {
		while (true) {
			sendNextData();
			sendMessages();
			
			synchronized (statusMutex) {
				if (!status) break;
			}
		}
	}
	
	/**
	 * Sends the next object in the data buffer.
	 * <p>
	 * NOTE: this method is <b>destructive</b>, i.e. the sent data
	 * will be removed from the data buffer after being sent.
	 * </p>
	 */
	private void sendNextData() {
		Entry<Long, Serializable> dataEntry = null;
		
		// Obtain a lock on the data buffer
		synchronized(dataBuffer) {
			if ((dataBuffer.size() == 0) || (dataBuffer.lastEntry() == null)) {
				// Send null
			} else {
				// Get the next data element, and remove it from the
				// data buffer
				dataEntry = dataBuffer.lastEntry();
				dataBuffer.clear();
			}
		}
		
		// Send the post request to the server, and read the response
		Entry<Long, byte[]> receivedData =
				NetworkManager.postObject(dataEntry);

		// Write the response to the response buffer
		if (receivedData != null) {
			synchronized(responseBuffer) {
				responseBuffer.put(receivedData.getKey(),
						NetworkManager.deserialiseData(receivedData.getValue()));
			}
		}
	}
	
	/**
	 * Writes data to the data buffer.
	 * @param timeValid - the time at which the data was valid
	 * @param data - the data to write to the data buffer
	 */
	public void writeData(long timeValid, Serializable data) {
		// Obtain a lock on the data buffer
		synchronized(dataBuffer) {
			if (data != null) {
				// Write the data to the data buffer
				dataBuffer.put(timeValid, data);
			}
		}
	}
	
	/**
	 * Reads the next response from the received buffer.
	 * <p>
	 * NOTE: this method is <b>destructive</b>, i.e. the response buffer
	 * will be cleared after being read.
	 * </p>
	 * @return the next object in the response buffer
	 */
	public Serializable readResponse() {
		// Obtain a lock on the response buffer
		synchronized(responseBuffer) {
			// Read data from the buffer
			if (responseBuffer.size() == 0) {
				// No data in the buffer
				return null;
			} else {
				Serializable response = null;
				
				if (responseBuffer.lastEntry() != null) {
					// Check if the data in the buffer is up-to-date
					if (responseBuffer.lastEntry().getKey() > mostRecent) {
						// Update the most recent value
						mostRecent = responseBuffer.lastEntry().getKey();

						// Data is more up-to-date than any seen so far,
						// so return it
						response = responseBuffer.lastEntry().getValue();
					}
				}
				
				responseBuffer.clear();
				return response;
			}
		}
	}
	
	/**
	 * Sends the messages in the message string.
	 * <p>
	 * NOTE: this method is <b>destructive</b>, i.e. the sent messages
	 * will be removed from the message string after being sent.
	 * </p>
	 */
	private void sendMessages() {
		String messageString = "";
		
		// Obtain a lock on the message string
		synchronized(messageString) {
			// Get (and clear) the message string
			messageString = messages;
			messages = "";
		}
		
		// Send the post request to the server
		NetworkManager.postMessage(messageString);
	}
	
	/**
	 * Writes a message to the message string.
	 * @param message - the message to write to the message string
	 */
	public void writeMessage(String message) {
		// Obtain a lock on the message string
		synchronized(messages) {
			if (message != null && !message.equals("")) {
				// Write the message to the message string
				messages += message;
			}
		}
	}
	
	/**
	 * Stops the thread.
	 */
	public void end() {
		synchronized (statusMutex) {
			status = false;
		}
	}
	
}
