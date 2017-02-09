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
 * Reporter for the <code>TestApplication</code>. Records hosts checking their app
 * and reports number of times up to date content was available.
 * @author dnx2000
 */

public class TestAppChecks extends Report implements ApplicationListener {

	// Number of hosts in the simulation
	public static final String HOSTS = "nrofHosts";
	public static final String SIM_TIME = "endTime";
	public static final String UPDATE_INTERVAL = "interval";

	private int hosts;
	private int[] total;
	private int[] successful;

	private int nrofUpdates;
	private double interval;
	private int updateID  = 0;
	private int srcUpdate;
	private Message msg;
	private PackUpdateEvent pack;	
	private double lastUpdate;


	//Constructor
	public TestAppChecks(){
		init();
	}

	@Override
	public void init(){
		super.init();
		Settings s = getSettings();
		interval = s.getDouble(UPDATE_INTERVAL);
		nrofUpdates = (int) (s.getDouble(SIM_TIME) / interval);
		hosts = s.getInt(HOSTS);
		total = new int[hosts];
		successful = new int[hosts];
		write("Host : Successful Checks : Total Checks : Success Rate \n");
	}

	public void gotEvent(String event, Object params, Application app,
			DTNHost host) {

		// Check that the event is sent by correct application type
		if (!(app instanceof TestApplication)) return;

		if (event.toLowerCase() == "check"){

			pack = (PackUpdateEvent)params;
			srcUpdate = pack.getSrcUpdate();
			double curTime = SimClock.getTime();
			if (curTime - this.lastUpdate >= this.interval){	
				updateID++;
				//this.lastUpdate = curTime;
				this.lastUpdate += interval;
			}
			if (srcUpdate == updateID)
				successful[host.getAddress()]++;
			total[host.getAddress()]++;
		}
	}

	public double getRate(int successful, int total){
		return 100*((double)successful/(double)total);
	}


	@Override
	public void done() {
		double sum =0;

		for (int i = 0; i<hosts; i++){
			double rate = getRate(successful[i],total[i]);
			sum += rate;
			write(i+" : "+successful[i]+" : "+total[i]+" : "+rate);
		}

		write("\nAverage Success Rate : "+(sum/hosts)+"%");

		super.done();
	}
}




