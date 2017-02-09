package report;

import applications.TestApplication;
import core.Application;
import core.ApplicationListener;
import core.DTNHost;
import core.Message;
import core.PackUpdateEvent;
import core.Settings;
import core.SimClock;

/**
 * Reporter for the <code>TestApplication</code>. Records app update exchanges
 * Reports total number of messages sent, received and rejected
 *
 * @author dnx2000
 */
public class TestAppReporter extends Report implements ApplicationListener {

	public static final String SIM_TIME = "endTime";
	public static final String UPDATE_INTERVAL = "interval";
	public static final String HOSTS = "nrofHosts";
	public static final String APP_ID = "app";

	private String appID;
	private int nrofUpdates;
	private double interval;
	private int hosts;
	private double lastUpdate;

	private int sent = 0;
	private int received = 0;
	private int rejected = 0;
	private int[] rejArray;

	private int updateID  = -1;
	private int srcUpdate = 0;
	private int destUpdate = 0;
	private Message msg;
	private PackUpdateEvent pack;	

	//private int hostCount = 0;
	private int intervalCount = -1;
	private int[] updateStats;


	//Constructor
	public TestAppReporter(){
		init();
	}

	@Override
	public void init(){
		super.init();
		Settings s = getSettings();
		appID = s.getSetting(APP_ID);
		interval = s.getDouble(UPDATE_INTERVAL);
		nrofUpdates = (int) (s.getDouble(SIM_TIME) / interval);
		hosts = s.getInt(HOSTS);
		rejArray = new int[hosts];
		updateStats = new int[nrofUpdates];
		write("Reporting for Application "+appID);
		write("SimTime : Event \n");
	}

	public void gotEvent(String event, Object params, Application app,
			DTNHost host) {
		// Check that the event is sent by correct application type
		if (!(app instanceof TestApplication)) return;
		if (app.getAppID() != appID) return;


		pack = (PackUpdateEvent)params;
		srcUpdate = pack.getSrcUpdate();

		switch (event.toLowerCase()){
			
			case "accept":				
				destUpdate = pack.getDestUpdate();
				msg = pack.getMsg();
				write(format(getSimTime()) + " : " + "Host " + host +
				 " ACCEPTED update " + srcUpdate + " -> " + destUpdate +
				 " from host " + msg.getFrom());
				received++;
				break;

			case "reject":
				destUpdate = pack.getDestUpdate();
				msg = pack.getMsg();
				write(format(getSimTime()) + " : " + "Host " + host +
				 	" REJECTED update " + srcUpdate + " -> " + destUpdate +
				 	" from host " + msg.getFrom());
				received++;
				rejected++;
				rejArray[host.getAddress()]++;
				break;

			case "sent":
				msg = pack.getMsg();
				write(format(getSimTime()) + " : " + "Host " + host +
					" sent update " + srcUpdate + " to host " +
					msg.getTo());
				sent++;
				break;

			case "update":
				write(format(getSimTime()) + " : " + "Active source host "
				 + host + " generated a new update - " + (srcUpdate+1)+"\n");
				break;		
			case "version":
				double curTime = SimClock.getTime();
				if (curTime - this.lastUpdate >= this.interval){	
					intervalCount++;
					updateID++;
					this.lastUpdate = curTime;
				}
				if (srcUpdate == updateID)
					updateStats[intervalCount]++;
		}
	}


	@Override
	public void done() {

		write("\n\nNumber of duplicates received per Node" +
			"\nHost : Number of duplicates");
		for (int i=0; i<hosts; i++)
			write(i + " : " + rejArray[i]);

		write("\n\nOverall stats for scenario " + getScenarioName() + 
			"\nTotal updates sent: " + sent +
			"\nTotal updates received: " + received +
			"\nTotal updates rejected: " + rejected);

		write("\nUsers with up to date content at each interval\n"+
			"Interval : Number of Users");
		for (int i = 0; i<nrofUpdates; i++)
			write(i+" : "+updateStats[i]);

		super.done();
	}
}