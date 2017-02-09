package core;

import java.util.LinkedHashMap;
import java.util.Set;
import core.DTNHost;
import core.SimClock;

public class AppStats {

	public static final String GROUP_ID = "s";

	
	private int createdSrc = 0;
	private int createdRelay = 0;
	private int sentSrc = 0;
	private int tempSentSrc = 0;
	private int sentRelay = 0;
	private int tempSentRelay = 0;
	private int received = 0;
	private int rejected = 0;
	private int aborted = 0;
	//private int[] rejArray;
	private LinkedHashMap<DTNHost, Integer> rejArray;

	//private int[] total;
	private LinkedHashMap<DTNHost, Integer> total;
	private LinkedHashMap<DTNHost, Integer> tempTotal;
	//private int[] successful;
	private LinkedHashMap<DTNHost, Integer> successful;
	private LinkedHashMap<DTNHost, Integer> tempSuccessful;

	private LinkedHashMap<DTNHost, Boolean> source;

	private int nrofUpdates = 0;
	private int intervalCount = -1;
	private int[] finalUpdated;
	private int[][] updated;
	private int updateCount = 0;
	private int nrofUpdateChecks =0;
	private double lastUpdate = 0;
	private double interval;
	private int updateID = 0;

	private double lastUserCheck = 0;
	private double lastUpdateCheck = 0;
	private double checkInterval;
	private int nrofUserChecks = 0;
	private int checkCount = 0;
	private double[][] rate;
	private double[] avgRate;
	private double[][] relayPercentage;
	private double[] avgRelayPercentage;

	public AppStats (double interval, double checkInterval, double simTime){

		this.nrofUpdates = (int)(simTime/interval);
		this.finalUpdated = new int[nrofUpdates];
		this.nrofUpdateChecks = 10;
		this.updated = new int[nrofUpdates-1][nrofUpdateChecks];
		this.relayPercentage = new double[nrofUpdates-1][nrofUpdateChecks+1];
		//this.rejArray = new int[hosts];
		//this.total = new int[hosts];
		//this.successful = new int[hosts];
		this.rejArray = new LinkedHashMap<DTNHost, Integer>();
		this.total = new LinkedHashMap<DTNHost, Integer>();
		this.tempTotal = new LinkedHashMap<DTNHost, Integer>();
		this.successful = new LinkedHashMap<DTNHost, Integer>();
		this.tempSuccessful = new LinkedHashMap<DTNHost, Integer>();
		this.source = new LinkedHashMap<DTNHost, Boolean>();
		this.interval = interval;
		this.checkInterval = checkInterval;

		//this.nrofUserChecks = (int)(interval/checkInterval)+1;
		this.nrofUserChecks = 11;
		this.rate = new double[nrofUserChecks][nrofUpdates-1];
		this.avgRate = new double[nrofUserChecks];
	}

	public void addHost(DTNHost host){
		rejArray.put(host, 0);
		total.put(host, 0);
		tempTotal.put(host, 0);
		successful.put(host, 0);
		tempSuccessful.put(host, 0);
		if (host.getGroupId().equals(GROUP_ID))
			source.put(host, true);
		else source.put(host, false);
	}

	public boolean isSource(DTNHost host){
		return source.get(host);
	}

	public void incCreatedSrc(){
		createdSrc++;
	}

	public void incCreatedRelay(){
		createdRelay++;
	}

	public void incSentSrc(){
		sentSrc++;
		tempSentSrc++;
	}

	public void incSentRelay(){
		sentRelay++;
		tempSentRelay++;
	}

	public void incReceived(){
		received++;
	}

	public void incRejected(){
		rejected++;
	}

	public void incAborted(){
		aborted++;
	}

	public void incRejArray(DTNHost host){
		rejArray.put(host, rejArray.get(host)+1);
	}

	public void incIntervalCount(){
		if (intervalCount >= 0){
			calculateRate();
			relayPercentage[intervalCount][nrofUpdateChecks] = 100*((double)tempSentRelay/(tempSentSrc+tempSentRelay));
		}
		intervalCount++;
		resetChecks();
		resetMsgCount();
	}

	public void incUpdateID(){
		updateID++;
	}

	public void incFinalUpdated(){
		finalUpdated[intervalCount]++;
	}

	public void incUpdated(){
		updated[intervalCount][updateCount]++;		
	}

	public void incUpdatedCount(){
		updateCount++;
		if (updateCount == nrofUpdateChecks)
			updateCount = 0;
	}

	public void incTotalChecks(DTNHost host){
		total.put(host, total.get(host)+1);
		tempTotal.put(host, tempTotal.get(host)+1);
	}

	
	public void incSuccessfulChecks(DTNHost host){
		successful.put(host, successful.get(host)+1);
		tempSuccessful.put(host, tempSuccessful.get(host)+1);
	}	

	public int getCreatedSrc(){
		return createdSrc;
	}

	public int getCreatedRelay(){
		return createdRelay;
	}

	public int getCreatedTotal(){
		return (createdSrc+createdRelay);
	}

	public int getSentTotal(){
		return (sentSrc+sentRelay);
	}

	public int getSentSrc(){
		return sentSrc;
	}

	public int getSentRelay(){
		return sentRelay;
	}

	public int getReceived(){
		return received;
	}

	public int getRejected(){
		return rejected;
	}

	public int getAborted(){
		return aborted;
	}

	/*public int getRejArray(int host){
		return rejArray[host];
	}*/

	public int getRejArray(DTNHost host){
		return rejArray.get(host);
	}

	public int getFinalUpdated(int interval){
		return finalUpdated[interval];
	}

	public double avgFinalUpdated(){
		int sum = 0;
		for (int i=1; i<nrofUpdates; i++)
			sum += finalUpdated[i];
		return (double)sum/(nrofUpdates-1);
	}

	public int getUpdated(int interval, int count){
		return updated[interval][count];
	}

	public double[] getAverageUpdated(){
		double[] avg = new double[nrofUpdateChecks+1];
		for (int i=0; i<nrofUpdateChecks; i++){
			int sum = 0;
			for (int j=0; j<(nrofUpdates-1); j++)
				sum += updated[j][i];
			avg[i] = (double)sum/(nrofUpdates-1);
		}
		avg[nrofUpdateChecks] = avgFinalUpdated();
		return avg;
	}
 	
 	public double[] getAverageRelayPercentage(){
 		double[] avg = new double[nrofUpdateChecks+1];
 		for (int i=0; i<=nrofUpdateChecks; i++){
			int sum = 0;
			for (int j=0; j<(nrofUpdates-1); j++){
				System.out.println("Interval "+j+" Check "+i+": relayPercentage - "+relayPercentage[j][i]);
				sum += relayPercentage[j][i];
			}
			avg[i] = (double)sum/(nrofUpdates-1);
		}
		
		return avg;
	}
 	
	public double getInterval(){
		return interval;
	}

	public double getCheckInterval(){
		return checkInterval;
	}

	public void setLastUpdate(double lastUpdate){
		this.lastUpdate = lastUpdate;
	}

	public double getLastUpdate(){
		return lastUpdate;
	}

	public void setLastUserCheck(double lastCheck){
		lastUserCheck = lastCheck;
	}

	public void setLastUpdateCheck(double lastCheck){
		lastUpdateCheck = lastCheck;
	}

	public double getLastUserCheck(){
		return lastUserCheck;
	}

	public double getLastUpdateCheck(){
		return lastUpdateCheck;
	}

	public int getUpdateID(){
		return updateID;
	}

	public int getNrofUpdates(){
		return nrofUpdates;
	}

	public int getIntervalCount(){
		return intervalCount;
	}

	/*public int getTotalChecks(int host){
		return total[host];
	}*/

	public int getTotalChecks(DTNHost host){
		return total.get(host);
	}

	/*public int getSuccessfulChecks(int host){
		return successful[host];
	}*/

	public int getSuccessfulChecks(DTNHost host){
		return successful.get(host);
	}

	public void calculateRate(){
		double sum = 0;
		int count = 0;
		
		for (DTNHost host : source.keySet()){
			if (!isSource(host)){
				if (tempTotal.get(host)==0)
					sum += 0;
				else
					sum += (double)tempSuccessful.get(host) / (double)tempTotal.get(host);
				//System.out.println(tempSuccessful.get(host)+" / "+tempTotal.get(host));
				count ++;
			}
		}
		rate[checkCount][intervalCount] = sum/count;
		//System.out.println(SimClock.getTime()+" "+intervalCount+" "+checkCount+" "+rate[checkCount][intervalCount]);
		checkCount ++;
		if (checkCount == nrofUserChecks)//{
			checkCount = 0;
			//System.out.println();}
	}

	public void calculateRelayPercentage(){
		relayPercentage[intervalCount][updateCount] = 100*((double)tempSentRelay/(tempSentRelay+tempSentSrc));
				System.out.println(SimClock.getTime()+": R - "+tempSentRelay+" S - "+tempSentSrc+" % - "+relayPercentage[intervalCount][updateCount]);

	}

	public double[] getAverageRates(){
		
		for (int i=0; i<avgRate.length; i++){
			double sum = 0;
			for (int j=0; j<(nrofUpdates-1); j++){
				sum += rate[i][j];
			}
			avgRate[i] = sum/(nrofUpdates-1);
		}
		return avgRate;
	}

	public void resetChecks(){
		for (DTNHost host : source.keySet()){
			tempTotal.put(host, 0);
			tempSuccessful.put(host, 0);
		}
	}

	public void resetMsgCount(){
		tempSentSrc = 0;
		tempSentRelay = 0;
	}

	public Set<DTNHost> getHosts(){
		return source.keySet();
	}

	public int getDestinations(){
		int count = 0;
		for (DTNHost host : source.keySet()){
			if (!isSource(host))
				count ++;
		}
		return count;
	}
}


