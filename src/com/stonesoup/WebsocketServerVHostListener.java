package com.stonesoup;

import java.net.InetSocketAddress;

import com.wowza.wms.amf.AMFDataList;
import com.wowza.wms.client.IClient;
import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.request.RequestFunction;
import com.wowza.wms.vhost.HostPort;
import com.wowza.wms.vhost.HostPortList;
import com.wowza.wms.vhost.IVHost;
import com.wowza.wms.vhost.IVHostNotify;

public class WebsocketServerVHostListener  implements IVHostNotify  {
	private static final Class<WebsocketServerVHostListener> CLASS = WebsocketServerVHostListener.class;
	private static final String CLASSNAME = CLASS.getName();

	@Override
	public void onVHostCreate(IVHost vhost) {
		// TODO Auto-generated method stub
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onVHostCreate: VHost created");
	}

	@Override
	public void onVHostInit(IVHost vhost) {
		// TODO Auto-generated method stub
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onVHostInit: VHost initialized");

		// get the host port used for webrtc defined in vhost.xml
		HostPortList list = vhost.getHostPortsList();
		HostPort hostPort = null;
		
		for(int i=0; i<list.size(); i++) {
			if(list.get(i).getSSLConfig() != null) {
				hostPort = list.get(i);
				break;
			}
		}
		
		// define, initialise and start custom websocket server
		int proxyPort = vhost.getProperties().getPropertyInt("proxyPort", 443);
		CustomWebSocketServer websocketServer = new CustomWebSocketServer(new InetSocketAddress(proxyPort));
		websocketServer.initialise(vhost, hostPort);
		websocketServer.start();
	}

	@Override
	public void onVHostShutdownComplete(IVHost vhost) {
		// TODO Auto-generated method stub
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onVHostShutdownComplete: VHost shutdown complete");
	}

	@Override
	public void onVHostShutdownStart(IVHost vhost) {
		// TODO Auto-generated method stub
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onVHostShutdownStart: VHost shutdown started");
	}
	
	@Override
	public void onVHostClientConnect(IVHost vhost, IClient inClient, RequestFunction function, AMFDataList params)
	{
		// TODO Auto-generated method stub
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".onVHostClientConnect: VHost client connected");
	}
}
