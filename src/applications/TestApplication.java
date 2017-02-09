/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */

package applications;

//TODO import reporter class
import core.Application;
import core.DTNHost;
import core.DTNSim;
import core.Message;
import core.Settings;
import core.SimClock;
import core.SimScenario;
import core.World;
import core.PackUpdateEvent;
import java.util.Random;
import java.lang.Math;
import java.util.HashMap;
import java.util.List;

import core.Zipf;


/**
 * <p>
 * Base class for applications. Nodes that have an application running will
 * forward all incoming messages to the application <code>handle()</code>
 * method before they are processed further. The application can change the
 * properties of the message before returning it or return null to signal
 * to the router that it wants the message to be dropped.
 * </p>
 *
 * <p>
 * In addition, the application's <code>update()</code> method is called every
 * simulation cycle.
 * </p>
 *
 * <p>
 * Configuration of application is done by picking a unique application instance
 * name (e.g., mySimpleApp) and setting its <code>type</code> property to the
 * concrete application class: <code>mySimpleApp.type = SimpleApplication
 * </code>. These application instances can be assigned to node groups using the
 * <code>Group.application</code> setting: <code>Group1.application =
 * mySimpleApp</code>.
 * </p>
 *
 * @author mjpitka
 * @author teemuk
 */
public class TestApplication extends Application {

	//private List<ApplicationListener> aListeners = null;

	//public String	appID	= null;
	
	/** Message size range -setting id ({@value}). Can be either a single
	 * value or a range (min, max) of uniformly distributed random values.
	 * Defines the message size (bytes). */
	public static final String UPDATE_SIZE = "size";
	/** Sender/receiver address range -setting id ({@value}).
	 * The lower bound is inclusive and upper bound exclusive. */
	public static final String SOURCE_RANGE = "source";
	/** (Optional) receiver address range -setting id ({@value}).
	 * If a value for this setting is defined, the destination hosts are
	 * selected from this range and the source hosts from the
	 * {@link #HOST_RANGE_S} setting's range.
	 * The lower bound is inclusive and upper bound exclusive. */
	public static final String DESTINATION_RANGE = "destination";
	/** Interval in seconds between updates. If the application is run
	 * on an active host the local update value will be incremented at 
	 * this interval  */
	public static final String UPDATE_INTERVAL = "interval";

	/** Application ID */
	//public static final String APP_ID = "TestApplication";
	public static final String APP_ID = "appID";

	// Number of Hosts 
	public static final String HOSTS = "nrofHosts";

	// Average time between successive app checks
	public static final String CHECK_TIME = "checkTime";

	// Time (in sec) a destination node is can be a relay node
	public static final String RELAY_TIME = "relayTime";

	// Probability of relay node sending an update message
	public static final String RELAY_PROB = "relayProbability";

	// Relay Modes: 'time' Relay limitted by time
	// 'single' Relays for only one message
	public static final String RELAY_MODE = "relayMode";

	public static final String SEED = "seed";

	// Scenario mode
	private static final String MODE = "mode";

	private static final String GROUP_ID = "s";

	public static final int DESTINATION = 0;
	public static final int SOURCE = 1;
	public static final int RELAY = 2;
	

	// Private vars
	private double	interval = 1800;
	private boolean active = false;
	private int[] type;
	//private HashMap<DTNHost, Integer> type;
	private int     srcMin = 0;
	private int     srcMax = 1;
	private int		destMin = 1;
	private int		destMax = 2;
	private int     updateID = 0;
	private int  size = 1;
	private double lastUpdate = 0;
	private double relayTime = 0;
	private double[] lastRelay;
	private double checkTime = 600;
	private int hosts = 1;
	private int[] totalChecks;
	private double[] nextCheck;
	private double[] lastCheck;
	private Random[] rngArray;
	private Random relayRng;
	private double relayProbability = 1;
	private String relayMode = "time";
	private int seed = 0;
	
	private String mode;
	private static int uniqueID;

	static {
		reset();
		DTNSim.registerForReset(TestApplication.class.getCanonicalName());
	}

	/**
	 * Resets all static fields to default values
	 */
	public static void reset() {
		uniqueID = 0;
	}
	/**
	 * Creates a new ping application with the given settings.
	 *
	 * @param s	Settings to use for initializing the application.
	 */
	public TestApplication(Settings s){

		s.setNameSpace("Scenario");
		this.mode = s.getSetting(MODE);
		s.restoreNameSpace();

		/*List<DTNHost> hostList = SimScenario.getInstance().getHosts();
		this.hosts = hostList.size();
		type = new HashMap<DTNHost, Integer>();
		for (DTNHost host : hostList){
			if (host.getGroupId().equals(GROUP_ID))
				type.put(host,SOURCE);
			else type.put(host,DESTINATION);
		}*/

		s.setNameSpace("Group");
		this.hosts = s.getInt("nrofHosts");
		s.restoreNameSpace();

		switch (mode.toLowerCase()){
			case "default":
				if (s.contains(APP_ID))
					setAppID(s.getSetting(APP_ID));
				break;
			case "zipf":
				uniqueID++;
				if (s.contains(APP_ID))
					setAppID(s.getSetting(APP_ID)+Integer.toString(uniqueID));
				break;	
		}

		if (s.contains(UPDATE_SIZE)){
			this.size = s.getInt(UPDATE_SIZE);
		}
		if (s.contains(UPDATE_INTERVAL)){
			this.interval = s.getDouble(UPDATE_INTERVAL);
		}

		
		type = new int[hosts];/*
		if (s.contains(SOURCE_RANGE)){
			int[] src = s.getCsvInts(SOURCE_RANGE);
			if (src.length == 2){
				for (int i=src[0]; i<src[1]; i++)
					type[i] = SOURCE;
			}
			else {
				for(int i=0; i<src.length; i++)
				type[src[i]] = SOURCE;
			}
		}
		else 
			type[0] = SOURCE;
		*/
		if (s.contains(DESTINATION_RANGE)){
			int[] destination = s.getCsvInts(DESTINATION_RANGE,2);
			this.destMin = destination[0];
			this.destMax = destination[1];
		}


		if (s.contains(CHECK_TIME)){
			this.checkTime = s.getDouble(CHECK_TIME);
		}

		if (s.contains(RELAY_TIME)){
			this.relayTime = s.getDouble(RELAY_TIME);
		}

		if (s.contains(RELAY_PROB)){
			this.relayProbability = s.getDouble(RELAY_PROB);
		}

		if (s.contains(RELAY_MODE)){
			this.relayMode = s.getSetting(RELAY_MODE);			
		}
		
		if (s.contains(SEED)){
			this.seed = s.getInt(SEED);
		}

		totalChecks = new int[hosts];
		nextCheck = new double[hosts];
		lastCheck = new double[hosts];
		rngArray = new Random[hosts];
		lastRelay = new double[hosts];
		//relay = new boolean[hosts];
		relayRng = new Random(seed);
		
		for (int i=0; i<hosts; i++){
			rngArray[i] = new Random((seed*hosts)+i);
			nextCheck[i] = getNextTime(i);
			lastCheck[i] = 0;
			lastRelay[i] = 0;
			//relay[i] = false;
		}
		

		//super.setAppID(APP_ID);
		//super.sendEventToListeners("initialise", s, null);
	}


	/**
	 * Copy-constructor
	 *
	 * @param a
	 */
	public TestApplication(TestApplication a) {
		super(a);
		this.interval = a.getInterval();
		this.active = a.isActive();
		this.type = a.getType();
		this.srcMin = a.getSrcMin();
		this.srcMax = a.getSrcMax();
		this.destMin = a.getDestMin();
		this.destMax = a.getDestMax();
		this.updateID = a.getUpdateID();
		this.size = a.getSize();
		this.lastUpdate = a.getLastUpdate();
		this.checkTime = a.getCheckTime();
		this.hosts = a.getHosts();
		this.totalChecks = a.getTotalChecks();
		this.nextCheck = a.getNextCheck();
		this.lastCheck = a.getLastCheck();
		this.rngArray = a.getRngArray();
		this.relayTime = a.getRelayTime();
		this.relayMode = a.getRelayMode();
		this.lastRelay = a.getLastRelays();
		this.relayProbability = a.getRelayProbability();
		this.seed = a.getSeed();
		this.relayRng = new Random(this.seed);

	}


	/**
	 * Handles an incoming message. Extracts update ID value from 
	 * message property and compares to local value. If the value is 
	 * larger the local ID is updated, otherwise it is ignored. The 
	 * result of the action is reported to the application listener
	 *
	 * @param msg	message received by the router
	 * @param host	host to which the application instance is attached
	 */
	@Override
	public Message handle(Message msg, DTNHost host) {
		
		try{	
			int newUpdate = (int)msg.getProperty("update");
	
			if (msg.getTo()!=host)
				return msg;

			PackUpdateEvent pack = new PackUpdateEvent(updateID, newUpdate, msg);
			if (newUpdate > updateID){			
				super.sendEventToListeners("accept", pack, host);
				updateID = newUpdate;
				if ((this.relayProbability > 0) && (!this.isRelay(host))){
					//this.type.put(host,RELAY);
					this.type[host.getAddress()] = RELAY;
					this.lastRelay[host.getAddress()] = SimClock.getTime();
					super.sendEventToListeners("start", null, host);   //Started being a relay
				}
			}
			else
				super.sendEventToListeners("reject", pack, host);
		
			return null;
		} 
		catch(NullPointerException e){
			return msg; //Not a valid message
		}
	}


	/**
	 * Updates the local update value if this is an active source host.
	 * Also models App users checking their apps at various intervals.
	 * @param host to which the application instance is attached
	 */
	@Override
	public void update(DTNHost host) {

		double curTime = SimClock.getTime();
		int adrs = host.getAddress();
		
		if (curTime - this.lastUpdate >= this.interval){	
			PackUpdateEvent pack = new PackUpdateEvent(updateID);
			//Update local ID if past interval
			if (this.isSource(host)){				
				updateID++;
				super.sendEventToListeners("update", pack, host);			
			}
			else 
				super.sendEventToListeners("version", pack, host);
			this.lastUpdate = curTime;	
		}

		if (curTime - this.lastCheck[adrs] >= this.nextCheck[adrs]){
			PackUpdateEvent pack = new PackUpdateEvent(updateID);
			super.sendEventToListeners("check", pack, host);
			this.totalChecks[adrs]++;
			this.lastCheck[adrs] = curTime;
			this.nextCheck[adrs] = getNextTime(adrs);
		}

		if (this.relayMode.equals("time") && (this.isRelay(host)) && ((curTime - this.lastRelay[adrs]) >= this.relayTime)){
			//this.type.put(host,DESTINATION);
			this.type[adrs] = DESTINATION;
			super.sendEventToListeners("stop", null, host);  //Stopped being a relay
		}
	}

    /** 
    * Method called by extended router whenever a new connection is  
 	* established. An active host will create and attempt to send a message 
 	* to the host on the other end of the connection.
    */
	public void sendUpdate(DTNHost from, DTNHost to) {
		if (this.isSource(from)){
			String id = getAppID() + SimClock.getIntTime() + "-" +
				from.getAddress();				
			Message m = new Message(from, to, id, getSize());
			m.addProperty("update", updateID);
			m.addProperty("relay", false);
			m.setAppID(getAppID());
			from.createNewMessage(m);
			PackUpdateEvent pack = new PackUpdateEvent(updateID, 0, m);
			super.sendEventToListeners("sent", pack, from);	
		} 
		else if (this.isRelay(from) && this.getNextRandom()){
			String id = getAppID() + "-R-" + SimClock.getIntTime() + "-" +
				from.getAddress();	
			Message m = new Message(from, to, id, getSize());
			m.addProperty("update", updateID);
			m.addProperty("relay", true);
			m.setAppID(getAppID());
			from.createNewMessage(m);
			PackUpdateEvent pack = new PackUpdateEvent(updateID, 0, m);
			super.sendEventToListeners("relay", pack, from);

			if (this.relayMode.equals("single") && (this.type[from.getAddress()] == RELAY)){
				//this.type.put(from,DESTINATION);
				this.type[from.getAddress()] = DESTINATION;
				super.sendEventToListeners("stop", null, from);  //Stopped being a relay
			}
		}
	}

	public double getNextTime(int host){
		return (-Math.log(1 - rngArray[host].nextDouble()) / (1/checkTime));
	}

	public boolean getNextRandom(){
		return relayRng.nextDouble() < relayProbability;
	}


	@Override
	public Application replicate() {
		return new TestApplication(this);
	}

	/**
	 * @return the interval
	 */
	public double getInterval() {
		return interval;
	}

	/**
	 * @param interval the interval to set
	 */
	public void setInterval(double interval) {
		this.interval = interval;
	}

	/**
	 * @return the active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * @param active the active flag to set
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * @return the srcMin
	 */
	public int getSrcMin() {
		return srcMin;
	}

	/**
	 * @param srcMin the srcMin to set
	 */
	public void setsrcMin(int srcMin) {
		this.srcMin = srcMin;
	}

	/**
	 * @return the srcMax
	 */
	public int getSrcMax() {
		return srcMax;
	}

	/**
	 * @param srcMax the srcMax to set
	 */
	public void setSrcMax(int srcMax) {
		this.srcMax = srcMax;
	}

	/**
	 * @return the destMin
	 */
	public int getDestMin() {
		return destMin;
	}

	/**
	 * @param destMin the destMin to set
	 */
	public void setDestMin(int destMin) {
		this.destMin = destMin;
	}

	/**
	 * @return the destMax
	 */
	public int getDestMax() {
		return destMax;
	}

	/**
	 * @param destMax the destMax to set
	 */
	public void setDestMax(int destMax) {
		this.destMax = destMax;
	}

	/**
	 * @return the Update ID
	 */
	public int getUpdateID() {
		return updateID;
	}

	/**
	 * @param updateID the update ID to set
	 */
	public void setUpdateID(int updateID) {
		this.updateID = updateID;
	}

	/**
	 * @return the Update Size
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param size the Update size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}
	/**
	 * @return the last update time
	 */
	public double getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * @param lastUpdate the last Update time to set
	 */
	public void setLastUpdate(double lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public double getCheckTime() {
		return checkTime;
	}

	public int getHosts(){
		return hosts;
	}

	public int[] getTotalChecks(){
		return totalChecks;
	}

	public int getChecks(int adrs){
		return totalChecks[adrs];
	}

	public double[] getNextCheck() {
		return nextCheck;
	}

	public double[] getLastCheck() {
		return lastCheck;
	}

	
	public Random[] getRngArray() {
		return rngArray;
	}

	public boolean isRelay(DTNHost host) {
		//return type.get(host) == RELAY;
		return type[host.getAddress()] == RELAY;
	}

	public double getRelayTime() {
		return relayTime;
	}

	public String getRelayMode() {
		return relayMode;
	}

	public double[] getLastRelays() {
		return lastRelay;
	}

	public double getRelayProbability() {
		return relayProbability;
	}

	public int getSeed() {
		return seed;
	}

	public int[] getType(){
		return type;
	}

	public boolean isSource(DTNHost host){
		return host.getGroupId().equals(GROUP_ID);
		//return type.get(host) == SOURCE;
	}
}

