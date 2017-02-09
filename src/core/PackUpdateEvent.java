package core;

import core.Message;

//Simple class to pack info for Test App Reporter
public class PackUpdateEvent {

	private int srcUpdate;
	private int destUpdate;
	private Message msg;

	// Constructor 
	public PackUpdateEvent(int srcUpdate, int destUpdate, Message msg){
		this.srcUpdate = srcUpdate;
		this.destUpdate = destUpdate;
		this.msg = msg;
	}

	// Another constructor just to send the update value
	public PackUpdateEvent(int srcUpdate){
		this.srcUpdate = srcUpdate;
		this.destUpdate = 0;
		this.msg = null;
	}

	public void setSrcUpdate(int srcUpdate){
		this.srcUpdate = srcUpdate;
	}

	public int getSrcUpdate(){
		return srcUpdate;
	}

	public void setsDestUpdate(int destUpdate){
		this.destUpdate = destUpdate;
	}

	public int getDestUpdate(){
		return destUpdate;
	}

	public void setMsg(Message msg){
		this.msg = msg;
	}

	public Message getMsg(){
		return msg;
	}
}

