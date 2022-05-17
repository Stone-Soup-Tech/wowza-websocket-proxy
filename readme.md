To add in Server.xml.xml, at vhost listeners:

	<VHostListener>
		<BaseClass>com.stonesoup.WebsocketServerVHostListener</BaseClass>
	</VHostListener>
		
External library used:

	https://github.com/TooTallNate/Java-WebSocket/releases/tag/v1.5.3
	https://github.com/TooTallNate/Java-WebSocket
	
To add in vhost.xml properties, if you want to move the proxy to other port than 443. The wowza streaming host port and the proxy need to have diferent ports.

	<Property>
		<Name>proxyPort</Name>
		<Value>444</Value>
		<Type>Integer</Type>
	</Property>