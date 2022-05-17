package com.stonesoup;


import java.net.URI;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import com.wowza.wms.logging.WMSLoggerFactory;

public class CustomWebSocketClient extends WebSocketClient {
	private static final Class<CustomWebSocketClient> CLASS = CustomWebSocketClient.class;
	private static final String CLASSNAME = CLASS.getName();
	private WebSocket websocketSession = null;

	public CustomWebSocketClient(URI serverUri, Draft draft) {
		super(serverUri, draft);
	}

	public CustomWebSocketClient(URI serverURI) {
		super(serverURI);
	}

	@Override
	public void onOpen(ServerHandshake handshakedata) {
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".new connection opened");
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".closed with exit code" + code + " additional info: " + reason);
	}

	@Override
	public void onMessage(String message) {
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".received message: " + message);
		// send message from custom websocket server to client
		websocketSession.send(message);
	}

	@Override
	public void onMessage(ByteBuffer message) {
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".received ByteBuffer");
	}

	@Override
	public void onError(Exception ex) {
		WMSLoggerFactory.getLogger(CLASS).info(CLASSNAME+".an error occurred: " + ex);
	}
	
	public void setWebsocketSession(WebSocket websocketSession) {
		this.websocketSession = websocketSession;
	}
	
	public WebSocket getWebsocketSession() {
		return this.websocketSession;
	}
}