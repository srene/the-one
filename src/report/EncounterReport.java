package report;

import java.util.List;

import core.DTNHost;
import core.Settings;
import core.UpdateListener;
import interfaces.SimpleInterfaceExtension;

public class EncounterReport extends Report implements UpdateListener{

	public static final String REPORT_NAME = "EncounterReport";
	public static final String REPORT_DIR = "reportDir";
	public static final String INTERFACE_TYPE = "SimpleInterfaceExtension";
	public static final String TOP = "topEncounters";

	private String reportDir;
	private int[] encounters;
	private int[] top;

	//Constructor
	public EncounterReport(){
		init();
	}

	@Override
	public void init(){
		Settings s = getSettings();
		reportDir = s.getSetting(REPORT_DIR);
		top = new int[s.getInt(TOP)];
	}

	public void updated(List<DTNHost> hosts) {
		if (encounters == null) {
			encounters = new int[hosts.size()];
		}
		for (int i=0; i<hosts.size(); i++){
			SimpleInterfaceExtension in = (SimpleInterfaceExtension) hosts.get(i).getInterface(1);
			encounters[i] += in.getNearInterfaces();
		}

	} 

	public void addEncounters(DTNHost host, int count){
		encounters[host.getAddress()] += count;
	}

	public void done(){

		createOutput(reportDir+getScenarioName()+"_"+REPORT_NAME+".txt");
		write("Host : Encounters\n");

		int[] tmp = encounters.clone();
		int[] pos = new int[top.length];

		for(int i=0; i<top.length; i++){
			for(int j=0; j<encounters.length; j++){
				if (tmp[j] > top[i]){
					top[i] = tmp[j];
					pos[i] = j;
				}
			}
			tmp[pos[i]] = 0;			
		}

		for (int i=0; i<encounters.length; i++)
			write(i+" "+encounters[i]);

		write("\nTop "+top.length+" hosts with most encounters"+
			"\nHost : Encounters");
		for (int i=0; i<top.length; i++)
			write(pos[i]+" "+top[i]);

		super.done();
	}
}