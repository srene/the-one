package routing;

import core.Settings;
import applications.TestApplication;
import core.Connection;
import core.DTNHost;
import core.Application;

/**
 * Router that will deliver messages only to the final recipient.
 * Also notifies the applications attached to host that a 
 * connection is up.
 */
public class DDRouterExtension extends DirectDeliveryRouter {

	public static final String APP_ID = "apps";

	private String[] apps;

	public DDRouterExtension(Settings s) {
		super(s);
		if (s.contains(APP_ID))
			apps = s.getCsvSetting(APP_ID);
		else 
			apps = new String[]{"TestApplication"};
	}

	protected DDRouterExtension(DDRouterExtension r) {
		super(r);
		this.apps = r.apps;
	}

	
	@Override
	public DDRouterExtension replicate() {
		return new DDRouterExtension(this);
	}

	@Override
	public void changedConnection(Connection con) {
		if (con.isUp()){	
			for (int i=0; i<apps.length; i++){	
				for (Application app : getApplications(apps[i])) {
					TestApplication testapp = (TestApplication) app;
					testapp.sendUpdate(getHost(), con.getOtherNode(getHost()));
				}
			}
		}
	}
}
