package report;

import core.DTNHost;
import core.Message;
import core.MessageListener;
import routing.DDRouterICCON;

public class MessageRelayReport extends Report implements MessageListener{

	private int createdSrc;
	private int createdRelay;
	private int sentSrc;
	private int sentRelay;
	private int received;
	private int aborted;

	/**
	 * Constructor.
	 */
	public MessageRelayReport() {
		init();
	}

	@Override
	protected void init() {
		super.init();
		
		createdSrc = 0;
		createdRelay = 0;
		sentSrc = 0;
		sentRelay = 0;
		received = 0;
		aborted = 0;
	}


	/**
	 * Method is called when a new message is created
	 * @param m Message that was created
	 */
	public void newMessage(Message m){
		DTNHost from = m.getFrom();
		DDRouterICCON r = (DDRouterICCON)from.getRouter();

		if (r.isRelay())
			createdRelay++;
		else
			createdSrc++;

	}

	/**
	 * Method is called when a message's transfer is started
	 * @param m The message that is going to be transferred
	 * @param from Node where the message is transferred from
	 * @param to Node where the message is transferred to
	 */
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to){
		DDRouterICCON r = (DDRouterICCON)from.getRouter();

		if (r.isRelay())
			sentRelay++;
		else 
			sentSrc++; 
	}

	/**
	 * Method is called when a message is deleted
	 * @param m The message that was deleted
	 * @param where The host where the message was deleted
	 * @param dropped True if the message was dropped, false if removed
	 */
	public void messageDeleted(Message m, DTNHost where, boolean dropped){}

	/**
	 * Method is called when a message's transfer was aborted before
	 * it finished
	 * @param m The message that was being transferred
	 * @param from Node where the message was being transferred from
	 * @param to Node where the message was being transferred to
	 */
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to){
		aborted++;
	}

	/**
	 * Method is called when a message is successfully transferred from
	 * a node to another.
	 * @param m The message that was transferred
	 * @param from Node where the message was transferred from
	 * @param to Node where the message was transferred to
	 * @param firstDelivery Was the target node final destination of the message
	 * and received this message for the first time.
	 */
	public void messageTransferred(Message m, DTNHost from, DTNHost to,
			boolean firstDelivery){
		received++;
	}

	public void done(){
		int createdTotal = createdSrc+createdRelay;
		int sentTotal = sentSrc+sentRelay;

		write("Message stats for scenario " + getScenarioName());
		write("Created by Source: "+createdSrc+
			"\nCreated by Relay: "+createdRelay+
			"\nTotal Created: "+createdTotal+
			"\n\nSent by Source: "+sentSrc+
			"\nSent by Relay: "+sentRelay+
			"\nTotal Sent: "+sentTotal+
			"\n\nAborted: "+aborted+
			"\nReceived: "+received);

		double percent = 100*((double)sentRelay/(double)sentTotal);
		write("\nRelay_Percentage "+format(percent));

		super.done();
	}
}