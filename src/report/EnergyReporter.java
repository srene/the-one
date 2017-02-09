package report;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import core.ConnectionListener;
import core.MessageListener;
import core.ApplicationListener;
import core.DTNHost;
import core.Message;
import core.Settings;
import core.SimClock;
import core.EnergyStats; 
import core.Application;
import applications.TestApplication;
import routing.MessageRouter;
import routing.DDRouterICCON;

public class EnergyReporter extends Report implements ConnectionListener, MessageListener, ApplicationListener{

	public static final String REPORT_NAME = "EnergyReport";
	public static final String REPORT_DIR = "reportDir";
	public static final String APP_ID = "apps";

	// Energy required to establish a connection by source in mJ
	public static final String CON_ENERGY_SRC = "srcConnectionEnergy";
	// Energy required to establish a connection by destination in mJ
	public static final String CON_ENERGY_DST = "dstConnectionEnergy";
	// Energy spent delivering messages/maintaining conenction in mW
	public static final String DEL_ENERGY_SRC = "srcDeliveryEnergy";
	// Energy spent receiving messages/maintaining connection in mW
	public static final String DEL_ENERGY_DST = "dstDeliveryEnergy";
	// Energy spent by nodes in Inquiry (source) mode in mW
	public static final String INQ_ENERGY = "inquiryEnergy";
	// Energy spent by nodes in Scanning (destination) mode in mW
	public static final String SCAN_ENERGY = "scanEnergy";

	public static final int SOURCE = 0;
	public static final int RELAY = 1;
	public static final int DESTINATION = 2;

	private int hosts;
	private String reportDir;
	private EnergyStats[] energyStats;

	private String[] apps;

	private double srcCon;
	private double dstCon;
	private double srcMsg;
	private double dstMsg;
	private double inquiry;
	private double scan;
	private double relay;

	private Random rng;
	
	//protected HashMap<ConnectionInfo, ConnectionInfo> connections;
	//protected HashMap<Message, double> messages;

	//Constructor
	public EnergyReporter(){
		init();
	}

	@Override
	public void init(){

		Settings s = getSettings();
		reportDir = s.getSetting(REPORT_DIR);
		apps = s.getCsvSetting(APP_ID);
		s.setNameSpace("Group");
		hosts = s.getInt("nrofHosts");
		s.restoreNameSpace();

		energyStats = new EnergyStats[hosts];
		for (int i=0; i<hosts; i++)
			energyStats[i] = new EnergyStats();

		
		for (int i=0; i<apps.length; i++){
			s.setNameSpace(apps[i]);
			int[] src = s.getCsvInts("source");
			if (src.length == 2){
				for (int j=src[0]; j<src[1]; j++)
					energyStats[j].setType(SOURCE);
			}
			else {
				for(int j=0; j<src.length; j++)
				energyStats[src[i]].setType(SOURCE);
			}
			s.restoreNameSpace();
		}

		srcCon = s.getDouble(CON_ENERGY_SRC);
		dstCon = s.getDouble(CON_ENERGY_DST);
		srcMsg = s.getDouble(DEL_ENERGY_SRC);
		dstMsg = s.getDouble(DEL_ENERGY_DST);
		inquiry = s.getDouble(INQ_ENERGY);
		scan = s.getDouble(SCAN_ENERGY);
		relay = (inquiry+scan)/2;

		rng = new Random();

		//connections = new HashMap<ConnectionInfo, ConnectionInfo>();
		//messages = new HashMap<Message, double>();

	}

	// Connections between two Sources or two non-relay Destination nodes
	// not allowed
	public boolean allowConnection(DTNHost host1, DTNHost host2){
		
		/*DDRouterICCON router1 = (DDRouterICCON) host1.getRouter();
		DDRouterICCON router2 = (DDRouterICCON) host2.getRouter();
		if (router1.isSource() && router2.isSource()) return false;
		if (!router1.isActive() && !router2.isActive()) return false;
		*/
		int adrs1 = host1.getAddress();
		int adrs2 = host2.getAddress();		
		if((energyStats[adrs1].getType()==SOURCE)&&(energyStats[adrs2].getType()==SOURCE))
			return false;
		if((energyStats[adrs1].getType()==DESTINATION)&&(energyStats[adrs2].getType()==DESTINATION))
			return false;

		return true;		
	}

	public boolean allowMessageTransfer(DTNHost from, DTNHost to){

		/*DDRouterICCON router1 = (DDRouterICCON) from.getRouter();
		DDRouterICCON router2 = (DDRouterICCON) to.getRouter();

		if (!router1.isActive() || router2.isActive())
			return false;*/

		int src = from.getAddress();
		int dst = to.getAddress();
		if((energyStats[src].getType()==DESTINATION)||(energyStats[dst].getType()==SOURCE))	
			return false;

		return true;
	}

	/*public void addConnections(DTNHost host){
		DDRouterICCON router = (DDRouterICCON) host.getRouter();
		if (router.isActive())
			energyStats[host.getAddress()].incSrcConnections();
		else
			energyStats[host.getAddress()].incDstConnections();
	}*/

	public void addConnections(DTNHost host1, DTNHost host2){
		int adrs1 = host1.getAddress();
		int adrs2 = host2.getAddress();

		switch(energyStats[adrs1].getType()){

			case SOURCE:
				energyStats[adrs1].incSrcConnections();
				energyStats[adrs2].incDstConnections();
				break;
			case RELAY:
				switch(energyStats[adrs2].getType()){
					case SOURCE:
						energyStats[adrs2].incSrcConnections();
						energyStats[adrs1].incDstConnections();
						break;
					case RELAY:
						if(rng.nextBoolean()){
							energyStats[adrs1].incSrcConnections();
							energyStats[adrs2].incDstConnections();
						} else{
							energyStats[adrs2].incSrcConnections();
							energyStats[adrs1].incDstConnections();
						}
						break;
					case DESTINATION:
						energyStats[adrs1].incSrcConnections();
						energyStats[adrs2].incDstConnections();
						break;
				} break;
			case DESTINATION:
				energyStats[adrs2].incSrcConnections();
				energyStats[adrs1].incDstConnections();
				break;
		}
	}

	public void addConTime(DTNHost host){		
		int adrs = host.getAddress();
		energyStats[adrs].incConnected();
		if (energyStats[adrs].isConnected()>1) return;
		
		double curTime = SimClock.getTime();
		double lastTime = energyStats[adrs].getLastConTime();

		/*if (host.getAddress() == 5)
			System.out.println(host+" ADD "+energyStats[adrs].getType()+" - "+curTime+" - "+lastTime+" = "+(curTime-lastTime));*/

		if (energyStats[adrs].getType() == SOURCE)
			energyStats[adrs].addInquiryTime(curTime - lastTime);
		else if (energyStats[adrs].getType() == RELAY)
			energyStats[adrs].addRelayTime(curTime - lastTime);
		else if (energyStats[adrs].getType() == DESTINATION)
			energyStats[adrs].addScanTime(curTime - lastTime);
	}

	public void addMsgTime(DTNHost from, DTNHost to){
		double curTime = SimClock.getTime();
		int src = from.getAddress();
		int dst = to.getAddress();

		energyStats[src].addSrcMsgTime(curTime - energyStats[src].getLastMsgTime());
		energyStats[dst].addDstMsgTime(curTime - energyStats[dst].getLastMsgTime());
	}

	/**
	 * Method is called when two hosts are connected.
	 * @param host1 Host that initiated the connection
	 * @param host2 Host that was connected to
	 */
	public void hostsConnected(DTNHost host1, DTNHost host2){
		//Ignore Connection change between two non-relay destination nodes
		//if(!allowConnection(host1, host2))	return;	

		/*if ((host1.getAddress() == 5)||host2.getAddress() == 5)
			System.out.println(SimClock.getTime()+" UP"+host1+" <-> "+host2);*/

		addConTime(host1);
		addConTime(host2);	
		addConnections(host1,host2);
		
	}

	/**
	 * Method is called when connection between hosts is disconnected.
	 * @param host1 Host that initiated the disconnection
	 * @param host2 Host at the other end of the connection
	 */
	public void hostsDisconnected(DTNHost host1, DTNHost host2){
		//Ignore Connection change between two non-relay destination nodes
		//if(!allowConnection(host1, host2))	return;	
		double curTime = SimClock.getTime();
		energyStats[host1.getAddress()].decConnected();
		energyStats[host2.getAddress()].decConnected();
		/*if ((host1.getAddress() == 5)||host2.getAddress() == 5){
			System.out.println(SimClock.getTime()+" DOWN"+host1+" <-> "+host2);
			if (energyStats[host1.getAddress()].isConnected() == 0)
				System.out.println(host1+" SET - "+curTime);
			if (energyStats[host2.getAddress()].isConnected() == 0)
				System.out.println(host2+" SET - "+curTime);
		}*/
		if (energyStats[host1.getAddress()].isConnected() == 0)			
			energyStats[host1.getAddress()].setLastConTime(curTime);
		
		if (energyStats[host2.getAddress()].isConnected() == 0)
			energyStats[host2.getAddress()].setLastConTime(curTime);
			
	}


	/**
	 * Method is called when a message's transfer is started
	 * @param m The message that is going to be transferred
	 * @param from Node where the message is transferred from
	 * @param to Node where the message is transferred to
	 */
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to){

		if(!allowMessageTransfer(from, to)) return;

		double curTime = SimClock.getTime();
		energyStats[from.getAddress()].setLastMsgTime(curTime);
		energyStats[to.getAddress()].setLastMsgTime(curTime);

	}

	/**
	 * Method is called when a message's transfer was aborted before
	 * it finished
	 * @param m The message that was being transferred
	 * @param from Node where the message was being transferred from
	 * @param to Node where the message was being transferred to
	 */
	public void messageTransferAborted(Message m, DTNHost from, DTNHost to){

		if(!allowMessageTransfer(from, to)) return;

		addMsgTime(from, to);
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
		if(!allowMessageTransfer(from, to)) return;

		addMsgTime(from, to);
	}

	public void newMessage(Message m){}
	public void messageDeleted(Message m, DTNHost where, boolean dropped){}

	public void gotEvent(String event, Object params, Application app,
			DTNHost host) {

		if (!(app instanceof TestApplication)) return;

		switch(event.toLowerCase()){

			case "start":
				/*if(host.getAddress()==5)
					System.out.println(SimClock.getTime()+" - "+host+" START");*/
				energyStats[host.getAddress()].setType(RELAY);
				break;
			case "stop":
				DDRouterICCON router = (DDRouterICCON) host.getRouter();
				/*if(host.getAddress()==5)
					System.out.println(SimClock.getTime()+" - "+host+" STOP");*/
				if(!router.isRelay()){
					int adrs = host.getAddress();
					double curTime = SimClock.getTime();
					energyStats[adrs].setType(DESTINATION);
					if (energyStats[adrs].isConnected() == 0){
						/*if (adrs == 5)
							System.out.println(host+" ADD R - "+curTime+" - "+energyStats[adrs].getLastConTime()+" = "+(curTime-energyStats[adrs].getLastConTime()));*/
						energyStats[adrs].addRelayTime(curTime - energyStats[adrs].getLastConTime());
						energyStats[adrs].setLastConTime(curTime);
					}
				}
				break;
		}
	}

	public void done() {
		double curTime = SimClock.getTime();

		createOutput(reportDir+getScenarioName()+"_"+REPORT_NAME+".txt");
		write("Host : Inquiry : Relay : Scanning : Src_Connection : Dst_Connection : Delivering : Receiving : Total (J)\n");

		double sum = 0;
		double srcSum = 0;
		double dstSum = 0;
		int srcCount = 0;
		for (int i=0; i<hosts; i++){
			if (energyStats[i].getType() == SOURCE)
				energyStats[i].addInquiryTime(curTime - energyStats[i].getLastConTime());
			else if (energyStats[i].getType() == RELAY)
				energyStats[i].addRelayTime(curTime - energyStats[i].getLastConTime());
			else if (energyStats[i].getType() == DESTINATION)
				energyStats[i].addScanTime(curTime - energyStats[i].getLastConTime());

			double inquiryEnergy = inquiry * energyStats[i].getInquiryTime();
			double relayEnergy = relay * energyStats[i].getRelayTime();
			double scanEnergy = scan * energyStats[i].getScanTime();
			double srcConEnergy = srcCon * energyStats[i].getSrcConnections();
			double dstConEnergy = dstCon * energyStats[i].getDstConnections();
			double srcMsgEnergy = srcMsg * energyStats[i].getSrcMsgTime();
			double dstMsgEnergy = dstMsg * energyStats[i].getDstMsgTime();
			double total = inquiryEnergy + relayEnergy + scanEnergy + srcConEnergy + dstConEnergy + srcMsgEnergy + 
				dstMsgEnergy;
			sum += total;
			if (energyStats[i].getType() == SOURCE){
				srcSum += total; 
				srcCount ++;
			}
			
			write(i+" "+format(inquiryEnergy)+" "+format(relayEnergy)+" "+format(scanEnergy)+" "+format(srcConEnergy)+
				" "+format(dstConEnergy)+" "+format(srcMsgEnergy)+" "+format(dstMsgEnergy)+" "+format(total));			
		}
		dstSum = sum - srcSum;

		write("\nAverage_Source_Energy: "+format(srcSum/srcCount)+" J");
		write("Average_Destination_Energy: "+format(dstSum/(hosts-srcCount))+" J");
		write("Total_Average_Energy: "+format(sum/hosts)+" J");
		super.done();
	}
}
