package core;

public class EnergyStats{

	private double inquiry;
	private double scan;
	private double relay;
	private int srcConnections;
	private int dstConnections;
	private double srcMsgTime;
	private double dstMsgTime;

	private double lastConTime;
	private double lastMsgTime;
	private boolean active;
	private int type;
	private int connected;

	public EnergyStats (){
		inquiry = 0.0;
		scan = 0.0;
		srcConnections = 0;
		dstConnections = 0;
		srcMsgTime = 0.0;
		dstMsgTime = 0.0;
		lastConTime = 0.0;
		lastMsgTime = 0.0;
		active = false;
		type = 2; //DESTINATION by default
		connected = 0;
	}

	public void addInquiryTime(double time){
		inquiry += time;
	}

	public void addScanTime(double time){
		scan += time;
	}

	public void addRelayTime(double time){
		relay += time;
	}

	public void incSrcConnections(){
		srcConnections++;
	}

	public void incDstConnections(){
		dstConnections++;
	}

	public void addSrcMsgTime(double time){
		srcMsgTime += time;
	}

	public void addDstMsgTime(double time){
		dstMsgTime += time;
	}

	public double getInquiryTime(){
		return inquiry;
	}

	public double getScanTime(){
		return scan;
	}

	public double getRelayTime(){
		return relay;
	}

	public int getSrcConnections(){
		return srcConnections;
	}

	public int getDstConnections(){
		return dstConnections;
	}

	public double getSrcMsgTime(){
		return srcMsgTime;
	}

	public double getDstMsgTime(){
		return dstMsgTime;
	}

	public void setLastConTime(double time){
		lastConTime = time;
	}

	public void setLastMsgTime(double time){
		lastMsgTime = time;
	}

	public double getLastConTime(){
		return lastConTime;
	}

	public double getLastMsgTime(){
		return lastMsgTime;
	}

	public void setActive(boolean bool){
		active = bool;
	}
	public boolean getActive(){
		return active;
	}

	public void setType(int type){
		this.type = type;
	}

	public int getType(){
		return type;
	}

	public void incConnected(){
		connected++;
	}

	public void decConnected(){
		connected--;
	}

	public int isConnected(){
		return connected;
	}
}
