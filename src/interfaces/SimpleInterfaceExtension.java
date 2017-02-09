package interfaces;

import java.util.Collection;

import core.CBRConnection;
import core.Connection;
import core.NetworkInterface;
import core.Settings;
import core.SimClock;
import routing.DDRouterICCON;


public class SimpleInterfaceExtension extends SimpleBroadcastInterface {

	/**
	 * Reads the interface settings from the Settings file
	 */
	public SimpleInterfaceExtension(Settings s)	{
		super(s);
	}

	/**
	 * Copy constructor
	 * @param ni the copied network interface object
	 */
	public SimpleInterfaceExtension(SimpleInterfaceExtension ni) {
		super(ni);
	}

	public NetworkInterface replicate()	{
		return new SimpleInterfaceExtension(this);
	}

	public void connect(NetworkInterface anotherInterface) {
		DDRouterICCON myRouter = (DDRouterICCON) this.getHost().getRouter();
		DDRouterICCON otherRouter = (DDRouterICCON) anotherInterface.getHost().getRouter();
		if (SimClock.getTime() < 10) return;
		if (!myRouter.isActive() && !otherRouter.isActive()) return;
		if (myRouter.isSource() && otherRouter.isSource()) return;
		


		super.connect(anotherInterface);
	}

	/**
	 * Updates the state of current connections (i.e. tears down connections
	 * that are out of range and creates new ones).
	 */
	public int getNearInterfaces(){

		Collection<NetworkInterface> interfaces =
			optimizer.getNearInterfaces(this);

		return interfaces.size();
	}

	public String toString() {
		return "SimpleInterfaceExtension " + super.toString();
	}
}