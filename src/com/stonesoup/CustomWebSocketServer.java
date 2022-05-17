package com.stonesoup;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

import com.wowza.wms.logging.WMSLoggerFactory;
import com.wowza.wms.vhost.HostPort;
import com.wowza.wms.vhost.IVHost;

public class CustomWebSocketServer extends WebSocketServer {
	private static final Class<CustomWebSocketServer> CLASS = CustomWebSocketServer.class;
	private static final String CLASSNAME = CLASS.getName();
	
	private static Map<WebSocket, CustomWebSocketClient>  websocketSessionsMap = new HashMap<>();
	String domainName = null;
	int port = 444;
	
	public CustomWebSocketServer(InetSocketAddress address) {
		super(address);
	}
	
	public void initialise(IVHost vhost, HostPort hostPort) {
		String certificateName = hostPort.getSSLConfig().getKeyStorePath();
		String[] certificateNameBits = certificateName.split("/");
		certificateName = certificateNameBits[certificateNameBits.length - 1].replace(".jks", "");
		
		String keyPath = vhost.getHomePath() + "/conf/" + certificateName + ".jks";
		String keyPassword = hostPort.getSSLConfig().getKeyStorePass();
		port = hostPort.getPort();
		
		domainName = vhost.getProperties().getPropertyStr("serverName");
		
		int maxPendingConnections = hostPort.getConfiguation().getAcceptorBackLog();
		setMaxPendingConnections(maxPendingConnections);
		setReuseAddr(true);
		
		// load ssl
		SSLContext sslContext = getSSLConextFromKeystore(keyPath, keyPassword);
		setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
	}
	
	private SSLContext getSSLConextFromKeystore(String keyPath, String keyPassword) {
	    KeyStore ks;
	    SSLContext sslContext;
	    try {
	        ks = KeyStore.getInstance("JKS");
	        ks.load(new FileInputStream(keyPath), keyPassword.toCharArray());
	        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
	        kmf.init(ks, keyPassword.toCharArray());
	        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
	        tmf.init(ks);
        
	        sslContext = SSLContext.getInstance("TLS");
	        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
	    } catch (UnrecoverableEntryException | KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | KeyManagementException e) {
	    	WMSLoggerFactory.getLogger(CLASS).error(CLASSNAME+".getSSLConextFromKeystore: " + e);
	    	return null;
	    }
	    

		return sslContext;
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".new connection to: " + conn.getRemoteSocketAddress());
		try {
			// create a websocket client for this connection, that redirects messages to wowza's default websocket server
			CustomWebSocketClient client = new CustomWebSocketClient(new URI("wss://" + domainName + ":" + port + "/webrtc-session.json"));
		    websocketSessionsMap.put(conn, client);
		    client.setWebsocketSession(conn);
			client.connectBlocking();
		} catch (URISyntaxException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".closed " + conn.getRemoteSocketAddress() + " with exit code " + code + " additional info: " + reason);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".received message from: " + conn.getRemoteSocketAddress() + ": " + message);
		websocketSessionsMap.get(conn).send(message);
	}

	@Override
	public void onMessage( WebSocket conn, ByteBuffer message ) {
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".received ByteBuffer from: " 	+ conn.getRemoteSocketAddress());
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".an error occurred on connection: " + conn.getRemoteSocketAddress()  + ":" + ex);
	}
	
	@Override
	public void onStart() {
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".server started");
	}

}