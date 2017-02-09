package report;

import java.util.HashMap;
import java.util.Set;
import java.util.List;
import applications.TestApplication;
import core.Application;
import core.ApplicationListener;
import core.UpdateListener;
import core.MessageListener;
import core.DTNHost;
import core.Message;
import core.PackUpdateEvent;
import core.Settings;
import core.SimClock;
import core.AppStats;

import routing.DDRouterICCON;

/**
 * Reporter for the <code>TestApplication</code>. Records app statistics for 
 * multiple app instances. Genearates two reports per app instance. The first
 * report includes general statistics such as number of update messages
 * created/received and the precentage of destination nodes with up to date
 * content at the end of each update interval. The second report generated
 * records the success rate of destination nodes finding up to date content 
 * when they 'check' their application at some point in time.
 * @author dnx2000
 */
public class MultiAppReporter extends Report implements ApplicationListener, UpdateListener, MessageListener {

	public static final String SIM_TIME = "endTime";
	public static final String APP_ID = "apps";

	public static final String MAIN_REPORT_NAME = "MultiAppReport";
	public static final String CHECK_REPORT_NAME = "MultiAppChecks";
	public static final String CDF_CHECK_NAME = "CDFCheckReport";
	public static final String CDF_UPDATE_NAME = "CDFUpdateReport";
	public static final String CDF_RELAY_NAME = "CDFRelayReport";
	//public static final String AVERAGE_REPORT_NAME = "AverageAppReport";
	public static final String REPORT_DIR = "reportDir";
	public static final String SEED = "rngSeed";

	// Scenario mode
	private static final String MODE = "mode";
	

	private String reportDir;
	private int seed;
	private String appID;
	private String[] apps;
	private int nrofApps;
	private double simTime;
	private double[] intervals;
	
	
	private int srcUpdate = 0;
	private int destUpdate = 0;
	private Message msg;
	private PackUpdateEvent pack;
	private HashMap<String, AppStats> appStatsMap;

	private double[] checkIntervals;	

	// Average Statistics
	private double avgPercentage;
	private double avgSuccess;
	//private double[] cdfPercentage;
	//private double[] cdfRelayPercentage;

	//Scenario mode
	private String mode;

	
	//Constructor
	public MultiAppReporter(){
		init();
	}

	@Override
	public void init(){
		//super.init();
		Settings s = getSettings();
		reportDir = s.getSetting(REPORT_DIR);

		s.setNameSpace("Scenario");
		this.mode = s.getSetting(MODE);
		s.restoreNameSpace();

		s.setNameSpace("Group");
		nrofApps = s.getInt("nrofApplications");
		s.restoreNameSpace();

		apps = new String[nrofApps];
		intervals = new double[nrofApps];
		checkIntervals = new double[nrofApps];

		switch (mode.toLowerCase()){
			case "default":
				apps = s.getCsvSetting(APP_ID);
				for (int i=0; i<apps.length; i++){
					s.setNameSpace(apps[i]);
					intervals[i] = s.getDouble("interval");
					checkIntervals[i] = s.getDouble("checkTime");
					s.restoreNameSpace();
				}
				break;
			case "zipf":
				s.setNameSpace("Group");
				String appName = s.getSetting("application");
				for (int i=0; i<nrofApps; i++)
					apps[i] = appName+Integer.toString(i+1);
				s.restoreNameSpace();
				s.setNameSpace(appName);
				for (int i=0; i<apps.length; i++){					
					intervals[i] = s.getDouble("interval");
					checkIntervals[i] = s.getDouble("checkTime");
				}
				s.restoreNameSpace();
				break;
		}

		simTime = s.getDouble(SIM_TIME);

		s.setNameSpace("MovementModel");
		seed = s.getInt(SEED);
		s.restoreNameSpace();

		//cdfPercentage = new double[11];
		//cdfRelayPercentage = new double[11];
	}

	public void updated(List<DTNHost> hosts) {
		if (appStatsMap == null){
			appStatsMap = new HashMap<String, AppStats>();
			for (int i=0; i<apps.length; i++){
				appStatsMap.put(apps[i], new AppStats(intervals[i], checkIntervals[i], simTime));
				for (DTNHost host : hosts){
					if (!host.getRouter().getApplications(apps[i]).isEmpty())
						appStatsMap.get(apps[i]).addHost(host);
				}
			}

		}

		for (int i=0; i<apps.length; i++){
			AppStats appStats = appStatsMap.get(apps[i]);
			double curTime = SimClock.getTime();
			//if (((curTime - appStats.getLastUserCheck()) >= appStats.getCheckInterval())
			if (((curTime - appStats.getLastUserCheck()) >= (intervals[i]/10))
				&& (appStats.getIntervalCount() > -1)
				&& (appStats.getIntervalCount() < (appStats.getNrofUpdates()-1))){
				syncUpdateID(appStats);
				appStats.calculateRate();
				appStats.setLastUserCheck(curTime);
			}

			if (((curTime - appStats.getLastUpdateCheck()) >= (intervals[i]/10))
				&& (appStats.getIntervalCount() > -1)
				&& (appStats.getIntervalCount() < (appStats.getNrofUpdates()-1))){
				syncUpdateID(appStats);
				for (DTNHost host : hosts){
					for(Application app : host.getRouter().getApplications(apps[i])){
						TestApplication testApp = (TestApplication) app;
						if ((!testApp.isSource(host)) && (testApp.getUpdateID() == appStats.getUpdateID())){
							appStats.incUpdated();
						}
					}					
				}
				appStats.calculateRelayPercentage();
				appStats.incUpdatedCount();
				appStats.setLastUpdateCheck(curTime);
			}
		}

	}

	public void gotEvent(String event, Object params, Application app,
			DTNHost host) {
		appID = app.getAppID();

		// Check that the event is sent by correct application type
		if (!(app instanceof TestApplication) || !(appStatsMap.containsKey(appID)) || (params == null)) return;

		AppStats appStats = appStatsMap.get(appID);		
		pack = (PackUpdateEvent)params;
		srcUpdate = pack.getSrcUpdate();

		switch (event.toLowerCase()){
			
			case "accept":				
				destUpdate = pack.getDestUpdate();
				msg = pack.getMsg();
				//if (!appStats.isSource(host))
				//	appStats.incReceived();
				break;

			case "reject":
				destUpdate = pack.getDestUpdate();
				msg = pack.getMsg();
				if (!appStats.isSource(host)){
					//appStats.incReceived();
					appStats.incRejected();
					appStats.incRejArray(host);
				}
				break;

			case "sent":
				//appStats.incSent();
				break;

			case "update":
				// NOTE: currently unreported event, can be used to report source node updates
				// if a detailed report of activity during simulation is required.
				//write(format(getSimTime()) + " : " + "Active source host "
				// + host + " generated a new update - " + (srcUpdate+1)+"\n");
				break;		
			case "version":
				syncUpdateID(appStats);
				if (srcUpdate == (appStats.getUpdateID()-1))
					appStats.incFinalUpdated();
				break;

			case "check":
				syncUpdateID(appStats);
				if (appStats.getIntervalCount() > -1){
					if (srcUpdate == appStats.getUpdateID())
						appStats.incSuccessfulChecks(host);
					appStats.incTotalChecks(host);
				}
				break;

			case "relay":
				//appStats.incSent();
				//ppStats.incRelay();
				break;
		}
	}

	//Updates the current updateID for an application instance
	public void syncUpdateID(AppStats appStats){
		double curTime = SimClock.getTime();
		double lastUpdate = appStats.getLastUpdate();
		double interval = appStats.getInterval();
		if (curTime - lastUpdate >= interval){	
			appStats.incIntervalCount();
			appStats.incUpdateID();
			//appStats.setLastUpdate(curTime);
			appStats.setLastUpdate(lastUpdate+interval);
		}
	}


/**
	 * Method is called when a new message is created
	 * @param m Message that was created
	 */
	public void newMessage(Message m){
		DTNHost from = m.getFrom();
		DDRouterICCON r = (DDRouterICCON)from.getRouter();
		AppStats appStats = appStatsMap.get(m.getAppID());
		boolean relay = (Boolean)m.getProperty("relay");

		if (relay)
			appStats.incCreatedRelay();
		else
			appStats.incCreatedSrc();

	}

	/**
	 * Method is called when a message's transfer is started
	 * @param m The message that is going to be transferred
	 * @param from Node where the message is transferred from
	 * @param to Node where the message is transferred to
	 */
	public void messageTransferStarted(Message m, DTNHost from, DTNHost to){
		DDRouterICCON r = (DDRouterICCON)from.getRouter();
		AppStats appStats = appStatsMap.get(m.getAppID());
		boolean relay = (Boolean)m.getProperty("relay");


		if (relay)
			appStats.incSentRelay();
		else 
			appStats.incSentSrc(); 
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
		AppStats appStats = appStatsMap.get(m.getAppID());
		appStats.incAborted();
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
		AppStats appStats = appStatsMap.get(m.getAppID());
		appStats.incReceived();
	}





	// Rewritten done() method called at the end of the simulation. Creates multiple reports
	// per app instance by using parent createOutput() method. (NOTE: parent method was changed
	// from private to protected for this purpose)
	@Override
	public void done() {

		for (int i=0; i<apps.length; i++){
			AppStats appStats = appStatsMap.get(apps[i]);
			createOutput(reportDir+getScenarioName()+"_"+MAIN_REPORT_NAME+"-"+apps[i]+".txt");
			write("Reporting for Application "+apps[i]);

			write("\n\nNumber of duplicates received per Node" +
				"\nHost : Number of duplicates");
			
			for (DTNHost host : appStats.getHosts()){
				if (!appStats.isSource(host))
					write(host + " : " + appStats.getRejArray(host));
			}

			write("\n\nMessage stats for scenario " + getScenarioName() + 
				"\nCreated by Source: "+appStats.getCreatedSrc() +
				"\nCreated by Relay: "+appStats.getCreatedRelay() +
				"\nTotal Messages Created: "+appStats.getCreatedTotal() +
				"\n\nSent by Source: "+appStats.getSentSrc() +
				"\nSent by Relay: "+appStats.getSentRelay() +
				"\nTotal Messages Sent: "+appStats.getSentTotal() +
				"\nTotal Messages Aborted: "+appStats.getAborted() +
				"\nTotal Messages received: " + appStats.getReceived() +
				"\nTotal Messages rejected: " + appStats.getRejected());
			double reject = 100*((double)appStats.getRejected()/(double)appStats.getReceived());
			write("\nReject_Percentage "+format(reject));

			double percent = 100*((double)appStats.getSentRelay() / (double)appStats.getSentTotal());
			write("Relay_Percentage: " + format(percent));

			double sum = 0;
			write("\nUsers with up to date content at each interval\n"+
				"Interval : Number of Users : %");
			for (int j = 0; j<appStats.getNrofUpdates(); j++){
				percent = 100*((double)appStats.getFinalUpdated(j)/(double)(appStats.getDestinations()));
				if (j!=0)
					sum+=percent;
				write(j+" : "+appStats.getFinalUpdated(j)+" : "+format(percent));
			}
			double p = sum/(appStats.getNrofUpdates()-1);
			avgPercentage += p;
			write("\nAverage_Percentage: "+	format(p));

			sum = 0;
			super.done();
			createOutput(reportDir+getScenarioName()+"_"+CHECK_REPORT_NAME+"-"+apps[i]+".txt");
			write("Reporting for Application "+apps[i]);
			write("Host : Successful Checks : Total Checks : Success Rate \n");

					
			for (DTNHost host : appStats.getHosts()){
				if (!appStats.isSource(host)){
					double rate = 100*((double)appStats.getSuccessfulChecks(host)/(double)appStats.getTotalChecks(host));
					sum += rate;
					write(host+" : "+appStats.getSuccessfulChecks(host)+" : "+appStats.getTotalChecks(host)+" : "+format(rate));
				}
			}
			double s =	sum/appStats.getDestinations();
			avgSuccess += s;
			write("\nAverage_Success_Rate: "+format(s));

			super.done();
			createOutput(reportDir+getScenarioName()+"_"+CDF_CHECK_NAME+"-"+apps[i]+".txt");
			write("Reporting for Application "+apps[i]);
			write("Check Interval : Average Cumulative Success Rate\n");
			double[] rates = appStats.getAverageRates();
			for (int j=0; j<rates.length; j++){
				write(j+" "+format(rates[j]));
			}


			super.done();
			createOutput(reportDir+getScenarioName()+"_"+CDF_UPDATE_NAME+"-"+apps[i]+".txt");
			write("Reporting for Application "+apps[i]);
			write("Update Check : % of Users\n");
			double[] avg = appStats.getAverageUpdated();
			for (int j=0; j<avg.length; j++){
				percent = 100*(avg[j]/appStats.getDestinations());
				//cdfPercentage[j] += percent;
				write((j)+" "+format(percent));
			}

			super.done();
			createOutput(reportDir+getScenarioName()+"_"+CDF_RELAY_NAME+"-"+apps[i]+".txt");
			write("Reporting for Application "+apps[i]);
			write("Sample Interval : % of Relay Msgs\n");
			avg = appStats.getAverageRelayPercentage();
			for (int j=0; j<avg.length; j++){
				//cdfRelayPercentage[j] += avg[j];
				write(j+" "+format(avg[j]));
			}

			super.done();
		}

		/*createOutput(reportDir+getScenarioName()+"_"+AVERAGE_REPORT_NAME+".txt");
		write("Average Statistics across all Apps");
		write("\nAverage_Percentage: "+	format(avgPercentage/nrofApps));	
		write("\nAverage_Success_Rate: "+format(avgSuccess/nrofApps));
		write("\nUpdate Check : % of Users");
		for (int i=0; i<cdfPercentage.length; i++)
			write(i+" "+format(cdfPercentage[i]/nrofApps));
		write("\nSample Interval : % of Relay Msgs");
		for (int i=0; i<cdfRelayPercentage.length; i++)
			write(i+" "+format(cdfRelayPercentage[i]/nrofApps));

		super.done();*/
	}
}