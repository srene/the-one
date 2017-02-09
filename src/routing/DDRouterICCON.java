package routing;

import core.Settings;
import core.SimClock;
import applications.TestApplication;
import core.Connection;
import core.DTNHost;
import core.Application;
import java.util.LinkedList;
import core.Message;
import java.util.HashMap;

/**
 * Router that will deliver messages only to the final recipient.
 * Also notifies the applications attached to host that a 
 * connection is up.
 */
public class DDRouterICCON extends DirectDeliveryRouter {

	public static final String APP_ID = "apps";
	public static final String PRIORITY = "prioritize";
	public static final String FLOOD = "flood";
	public static final String RELAY_MODE = "relayMode";
	public static final String RELAY_TIME = "relayTime";

	public static final String TIME = "time";
	public static final String CONTACT = "contact";

	// Scenario mode
	private static final String MODE = "mode";

	private static final String GROUP_ID = "s";

	public static final String MSG_TTL_S = "msgTtl";

	protected boolean flood;
	protected boolean relay;
	protected String relayMode;
	protected double relayTime;
	protected double lastRelay;
	protected int nrofApps;
	protected String[] apps;
	protected boolean prioritize;
	protected LinkedList<TestApplication> sendList;
	protected LinkedList<TestApplication> checkList;

	protected double msgTtl;
	protected double lastTtlCheck;

	protected HashMap<String, TestApplication> applications;
	protected HashMap<String, Message> pendingUpdates;

	//Scenario mode
	private String mode;

	
	public DDRouterICCON(Settings s) {
		super(s);
				
		s.setNameSpace("Scenario");
		this.mode = s.getSetting(MODE);
		s.restoreNameSpace();

		s.setNameSpace("Group");
		this.msgTtl = (double)s.getInt(MSG_TTL_S) * 60.0;
		s.restoreNameSpace();

		nrofApps = s.getInt("nrofApplications");
		apps = new String[nrofApps];

		switch (mode.toLowerCase()){
			case "default":
				for (int i=0; i<nrofApps; i++)
					apps[i] = s.getSetting("application"+(i+1));					
				break;
			case "zipf":
				for (int i=0; i<nrofApps; i++)
					apps[i] = s.getSetting("application")+Integer.toString(i+1);
				break;
		}
		
		s.setNameSpace("DDRouterICCON");
		if (s.contains(FLOOD))	
			flood = s.getBoolean(FLOOD);
		else
			flood = false;

		if (s.contains(RELAY_MODE))	
			relayMode = s.getSetting(RELAY_MODE);
		else
			relayMode = TIME;

		if (s.contains(RELAY_TIME))
			relayTime = s.getDouble(RELAY_TIME);
		else
			relayTime = 300.0;
		
		if (s.contains(PRIORITY))
			prioritize = s.getBoolean(PRIORITY);
		else
			prioritize = false;
		s.restoreNameSpace();

		this.relay = false;	
		this.lastRelay = 0.0;
		this.lastTtlCheck = 0.0;
		this.sendList = new LinkedList<TestApplication>();
		this.checkList = new LinkedList<TestApplication>();
		this.pendingUpdates = new HashMap<String, Message>();
		
	}

	protected DDRouterICCON(DDRouterICCON r) {
		super(r);
		this.flood = r.flood;
		this.relay = r.relay;
		this.relayMode = r.relayMode;
		this.relayTime = r.relayTime;
		this.lastRelay = r.lastRelay;
		this.apps = r.apps;
		this.sendList = r.sendList;
		this.checkList = r.checkList;
		this.prioritize = r.prioritize;
		this.pendingUpdates = r.pendingUpdates;
		this.msgTtl = r.msgTtl;
		this.lastTtlCheck = r.lastTtlCheck;
		this.applications = r.applications;
	}

	
	@Override
	public DDRouterICCON replicate() {
		return new DDRouterICCON(this);
	}

	public void initApplicationMap(){
		this.applications = new HashMap<String,TestApplication>();
		for (int i=0; i<apps.length; i++){			
			for (Application app : getApplications(apps[i])){
				TestApplication myApp = (TestApplication) app;
				applications.put(apps[i], myApp);
			}			
		}

	}

	public TestApplication getTestApplication(String id){
		return applications.get(id);
	}

	public boolean containsApplication(String id){
		return this.applications.containsKey(id);
	}

	@Override
	public void update() {
		super.update();

		if (applications == null)
			initApplicationMap();

		double curTime = SimClock.getTime();
		if (flood && relayMode.equals(TIME) && this.isRelay() && (curTime - lastRelay) >= relayTime){
			relay = false;
			//pendingUpdates.clear();
			pendingUpdates = new HashMap<String, Message>();
		}

		/*if ((curTime - lastTtlCheck) >= msgTtl*2){
			clearDelivered();
			lastTtlCheck = curTime;
		}*/
	}

	@Override
	public Message messageTransferred(String id, DTNHost from) {
		Message m = super.messageTransferred(id, from);
		String appID = m.getAppID();
		int updateID = (int)m.getProperty("update");
		if (flood){
			if (!this.isRelay()){
				relay = true;
				lastRelay = SimClock.getTime();
			}

			if (!pendingUpdates.containsKey(appID))
				pendingUpdates.put(appID, m.replicate());
			else if ((int)pendingUpdates.get(appID).getProperty("update") < updateID)
				pendingUpdates.put(appID, m);
		}
		return m;
	}

	@Override
	public void changedConnection(Connection con) {
		if (flood)		
			floodProcess(con);
		else{
			if (prioritize)
				priorityProcess(con);		
			else
				nonPriorityProcess(con);
		}
	}

	public void floodProcess(Connection con){
		if (con.isUp() && SimClock.getTime() > 10.0){	
			DTNHost otherHost = con.getOtherNode(getHost());
			if (this.isSource()){
				for (int i=0; i<apps.length; i++){	
					/*for (Application app : getApplications(apps[i])) {
						TestApplication testapp = (TestApplication) app;
						testapp.sendUpdate(getHost(), con.getOtherNode(getHost()));
					}*/
					if (applications.containsKey(apps[i])){
						TestApplication testApp = applications.get(apps[i]);
						testApp.sendUpdate(getHost(), con.getOtherNode(getHost()));
					}
				}
			}
			else if (this.isRelay()){
				for (String appID : pendingUpdates.keySet()){
					Message m = pendingUpdates.get(appID);
					DTNHost thisHost = this.getHost();
					String id = appID + "-R-" + SimClock.getIntTime() + "-" +
					thisHost.getAddress();				
					Message newMsg = new Message(thisHost, otherHost, id, m.getSize());
					newMsg.addProperty("update", m.getProperty("update"));
					newMsg.addProperty("relay", true);
					newMsg.setAppID(appID);
					thisHost.createNewMessage(newMsg);
				}
				/*if (relayMode == CONTACT){
					relay = false;
					pendingUpdates.clear();
				}*/
			}
		}
		else if (!con.isUp() && (relayMode.equals(CONTACT)) && this.isRelay()){
			relay = false;
			//pendingUpdates.clear();
			pendingUpdates = new HashMap<String, Message>();
		}

	}

	public void priorityProcess(Connection con){
		if (con.isUp()){
			DTNHost otherHost = con.getOtherNode(getHost());	
			DDRouterICCON otherRouter = (DDRouterICCON)otherHost.getRouter();
			sendList.clear();
			checkList.clear();
			for (int i=0; i<apps.length; i++){	
				/*for (Application myApp : getApplications(apps[i])) {
					TestApplication myTestApp = (TestApplication) myApp;
					for(Application yourApp : otherHost.getRouter().getApplications(apps[i])){
						TestApplication yourTestApp = (TestApplication) yourApp;
						if (yourTestApp.getUpdateID() < myTestApp.getUpdateID())
							//myTestApp.sendUpdate(getHost(), con.getOtherNode(getHost()));
							addToSendList(myTestApp, yourTestApp, otherHost.getAddress());
					}
				}*/
				if (applications.containsKey(apps[i])){
					TestApplication myApp = applications.get(apps[i]);
					if (otherRouter.containsApplication(apps[i])){
						TestApplication yourApp = otherRouter.getTestApplication(apps[i]);
						addToSendList(myApp, yourApp, otherHost.getAddress());
					}
				}
			}
			sendUpdates(otherHost);
		}
	}

	public void nonPriorityProcess(Connection con){
		if (con.isUp()){
			DTNHost otherHost = con.getOtherNode(getHost());	
			DDRouterICCON otherRouter = (DDRouterICCON)otherHost.getRouter();
			for (int i=0; i<apps.length; i++){	
				/*for (Application myApp : getApplications(apps[i])) {
					TestApplication myTestApp = (TestApplication) myApp;
					for(Application yourApp : otherHost.getRouter().getApplications(apps[i])){
						TestApplication yourTestApp = (TestApplication) yourApp;
						if (yourTestApp.getUpdateID() < myTestApp.getUpdateID())
							myTestApp.sendUpdate(getHost(), otherHost);							
					}
				}*/
				if (applications.containsKey(apps[i])){
					TestApplication myApp = applications.get(apps[i]);
					if (otherRouter.containsApplication(apps[i])){
						TestApplication yourApp = otherRouter.getTestApplication(apps[i]);
						if (yourApp.getUpdateID() < myApp.getUpdateID())
							myApp.sendUpdate(getHost(), otherHost);
					}
				}
			}			
		}
	}

	

	public void addToSendList(TestApplication myApp, TestApplication yourApp, int adrs){
		
		if (sendList.isEmpty()){
			sendList.add(myApp);
			checkList.add(yourApp);
		}
		else{
			for (int i=0; i<sendList.size(); i++){
				if (yourApp.getChecks(adrs) > checkList.get(i).getChecks(adrs)){
					sendList.add(i,myApp);
					checkList.add(i,yourApp);
					break;
				}
				if(i == (sendList.size()-1)){
					sendList.addLast(myApp);
					checkList.addLast(yourApp);
					break;
				}
			}			
		}
	}

	public void sendUpdates(DTNHost host){
		
		for (int i=0; i<sendList.size(); i++){
			TestApplication app = sendList.get(i);
			app.sendUpdate(getHost(), host);
		}
	}

	// Returns true if host is active (source or relay) for at least one app instance
	public boolean isActive(){
		if (this.isSource() || this.isRelay())
			return true;
		return false;

		/*for (int i=0; i<apps.length; i++){	
			for (Application myApp : getApplications(apps[i])) {
				TestApplication myTestApp = (TestApplication) myApp;
				if (myTestApp.isSource(getHost()) || myTestApp.isRelay(getHost()))
					return true;
			}
		}
		return false;*/
	}
	// Returns true if host is a source for at least one app instance
	public boolean isSource(){
		/*for (int i=0; i<apps.length; i++){	
			for (Application myApp : getApplications(apps[i])) {
				TestApplication myTestApp = (TestApplication) myApp;
				if (myTestApp.isSource(getHost()))
					return true;
			}
		}
		return false;*/
		return getHost().getGroupId().equals(GROUP_ID);
	}
	// Returns true if host is a relay for at least one app instance
	public boolean isRelay(){
		if (flood)
			return relay;
		else{
			for (int i=0; i<apps.length; i++){	
				/*for (Application myApp : getApplications(apps[i])) {
					TestApplication myTestApp = (TestApplication) myApp;
					if (myTestApp.isRelay(getHost()))
						return true;
				}*/
				if (applications.containsKey(apps[i]))
					return applications.get(apps[i]).isRelay(getHost());
			}
			return false;	
		}
	}
}