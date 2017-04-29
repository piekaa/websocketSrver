package pieka.websocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;


public class ServerClient {
	
	
	
//	private Vector lastMessage;
	private WebsocketDataBuffer buffer = new WebsocketDataBuffer();
	private String handshakeString = new String();
	
	private int rnCounter = 0;
	
	private WebSocketServer server;
	
	private int byteCounter = 0;
	
	private byte opcode;
	private byte mask[] = new byte[4];
	
	private int maskCount = 0;
	
	
	private int index=0;
	
	private long payloadLength=0;
	private long payloadLengthComplete=0;
	
	private boolean payloadCompleted=false;
	
	
	byte[] message;
	
	
	public Socket getSocket() {
		return socket;
	}

	public ServerClient()
	{}
	
	public void setSocket(Socket socket) {
		this.socket = socket;
		
		InputStream stream = null;
		  
		try {
			stream = socket.getInputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		  in = new DataInputStream (stream);
	}


	private Socket socket;
	private DataInputStream in;
	private OutputStream out;
	
	
	public ServerClient(Socket socket, WebSocketServer server)
	{
		this.server = server;
		this.socket = socket;
		
		InputStream stream = null;
		OutputStream ostream = null;
		
		
		try {
			stream = socket.getInputStream();
			ostream = socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		  in = new DataInputStream (stream);
		  try {
			out = socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	private Map handshake = new HashMap<String, String>();
	
	
	private boolean hanshakeComplete  = false;


	public void getMessage()
	{
		try {
			int bytes = in.available() + 10000;
			
			//lastMessage = new Vector();
			
			
			for(int i = 0 ;  in.available() > 0 ; i++)
			{
				byte m = (byte) in.read();
				
			//	System.out.print((char)m);
				
				//lastMessage.push(m);
				buffer.pushByte(m);
			}
			
			workWithMessage();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	
	private void workWithMessage()
	{
		if( isHanshakeComplete() == false )
		{

			for(;buffer.hasNext();)
			{
				
				char c = (char)buffer.next();
				
				handshakeString += c;
				
				
				if( c== '\n' )
				{
					rnCounter++;
					
					if( rnCounter >= 2 )
					{
						hanshakeComplete = true;
						break;
					}
					
				}
				
				if( c == '\r' && c == '\r' )
					rnCounter=0;

			}
			
			makeHandshake();
			
			
			server.onConnect(this);
			
			
			workWithMessage();
		}
			

		
		else
		{
			
			
			while( buffer.hasNext() )
			{
				byte b = buffer.next();
				
				
				if( byteCounter == 0 )
				{
					boolean fin = (b<0 );
			//		System.out.println("Fin:  " + fin );
					
					opcode = (byte) (b & 0xf);
					
					
			//		System.out.println("Opcode: " + opcode);
					byteCounter++;
					continue;
				}
				
				
				if( byteCounter == 1 )
				{
					payloadLength = b & 127;
			//		System.out.println( "PayloadLength: "  +  payloadLength );
					
					
					if ( payloadLength < 126)
					{
						payloadLengthComplete = payloadLength;
						message = new byte[(int)payloadLengthComplete];
						payloadCompleted = true;
					}
					
					byteCounter++;
					continue;
				}
				
				
				
				if( byteCounter > 1)
				{
					if( payloadCompleted == false ) 
					{
						
						if( payloadLength == 126 )
						{
							payloadLengthComplete <<= 8;
							payloadLengthComplete +=  ( 255 & b  );
							
							
							byteCounter++;
							
							if( byteCounter == 4 )
							{
								payloadCompleted = true;
								message = new byte[(int)payloadLengthComplete];
								continue;
							}
							
						}
					}
					else
					{
						if( maskCount < 4 )
						{
							mask[ maskCount++ ] = b;
							continue;
						}
						else
						{
							message[index] =  (byte) (  b  ^ mask[index%4]);
							index++;
							
							
							if( index == payloadLengthComplete )
							{
						//		System.out.println("Message Reciving completed, opcode: " + opcode);
								
								
								if( opcode == 1)
								{
									server.onMessage(new String(message), this);
									/*
									for(int i = 0 ; i < payloadLengthComplete ; i++)
									{
										System.out.print((char)message[i]);
									}
									*/
								}
								
								
								if( opcode == 2)
								{
									server.onMessage(message, this);
									/*
									for(int i = 0 ; i < payloadLengthComplete ; i++)
									{
										System.out.print( (255&message[i]) +" ");
									}
									*/
								}
								
								if( opcode == 8)
								{
									server.onClose(this);
									/*
									System.out.println("Disconnected");
									for(int i = 0 ; i < payloadLengthComplete ; i++)
									{
										System.out.print( (255&message[i]) +" ");
									}
									*/
								}
						//		System.out.println();
								
								
							//	sendDisconnect();
								
								
								
								
								
								
								opcode = 0;
								maskCount = 0;
								byteCounter = 0;
								payloadCompleted = false;
								payloadLength = 0;
								payloadLengthComplete =0;
								index = 0;
								
								
								
							}
							
							
							
						}
						
						
						
					}
					
					
				}

			}
			
		
			
		}
	}
	
	
	public void sendMessage(String text)
	{
		
		byte message[] = text.getBytes();
		
		//65535
		int start =0 ;
		
		byte[] toSend = null;
		if( message.length < 126)
		{
			toSend = new byte[2 + message.length];
			toSend[0] = (byte)129;
			toSend[1]  = (byte)message.length;
			
			start = 2;
		}
		
		
		if( message.length == 126)
		{
			toSend = new byte[2 + message.length];
			toSend[0] = (byte)129;
			
			toSend[1] = (byte) (message.length >> 8);
			toSend[2] = (byte) (message.length & 255);
			
			start = 3;
		}
		
		
		if( message.length > 126)
		{
			toSend = new byte[2 + message.length];
			toSend[0] = (byte)129;
			
			int l = message.length;
			
			toSend[1] = 0;
			toSend[2] = 0;
			toSend[3] = 0;
			toSend[4] = 0;
			
			toSend[8] = (byte)(l & 255);
			l >>= 8;
			toSend[7] = (byte)(l & 255);
			l >>= 8;
			toSend[6] = (byte)(l & 255);
			l >>= 8;
			toSend[5] = (byte)(l & 255);
			
			start = 9;
			
			
		}
		
		
		for(int i = 0 ; i < message.length ; i++)
			toSend[i + start] = message[i];
		
		
		try {
			out.write(toSend);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		
	}
	
	public void sendMessage(byte[] message)
	{
		
		//65535
				int start =0 ;
				
				byte[] toSend = null;
				if( message.length < 126)
				{
					toSend = new byte[2 + message.length];
					toSend[0] = (byte)130; /// 10000010
					toSend[1]  = (byte)message.length;
					
					start = 2;
				}
				
				else
				if( message.length >= 126 && message.length < 1<<16)
				{
				//	System.out.println("Srednia");
					toSend = new byte[4 + message.length];
					toSend[0] = (byte)130;
					toSend[1] = (byte)126;
					
					
					toSend[2] = (byte) (message.length >> 8);
					toSend[3] = (byte) (message.length & 255);
					
					start = 4;
				}
				
				else
				{
				//	System.out.println("Najwieksza");
					toSend = new byte[10 + message.length];
					toSend[0] = (byte)130;
					toSend[1] = (byte)127;
					int l = message.length;
					
					toSend[2] = 0;
					toSend[3] = 0;
					toSend[4] = 0;
					toSend[5] = 0;
					
					toSend[9] = (byte)(l & 255);
					l >>= 8;
					toSend[8] = (byte)(l & 255);
					l >>= 8;
					toSend[7] = (byte)(l & 255);
					l >>= 8;
					toSend[6] = (byte)(l & 255);
					
					start = 10;
					
					
				}
				
				
				for(int i = 0 ; i < message.length ; i++)
					toSend[i + start] = message[i];
				
				
				try {
					out.write(toSend);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					
				}
				
		
	}
	
	
	private void makeHandshake()
	{
	
		
		
		String keyy = new String();
		String valuee = new String();
		
		boolean k = false;
		
		for(int i = 0 ; i < handshakeString.length() ; i++)
		{
			
			if( handshakeString.charAt(i) == ':' )
			{
				k = true;
				i++;
				continue;
			}
			
			
			if( handshakeString.charAt(i) == '\r' )
			{
				k = false;
				
				i++;
		//		System.out.println("Key: " + key +", Value: " + value);
				
				this.handshake.put(keyy, valuee);
				
				keyy = new String();
				valuee = new String();
				
				continue;
			}
			
			if( k == false )
			{
				keyy += handshakeString.charAt(i);
			}
			
			if( k == true )
			{
				valuee += handshakeString.charAt(i);
			}
		}
		
		
		
		
		
		
		
		
		
		
		String handshakeResponse = new String();
		
		
		String key = (String)handshake.get("Sec-WebSocket-Key");
		
		String resKey = WebSocketServer.secWebsocketAccept(key);
		
//		System.out.println("Key: " + key +" resKey: " + resKey);

		
		
		
	//	System.out.println(handshake.get("Sec-WebSocket-Key"));
		
		String headLine = "HTTP/1.1 101 Web Socket Protocol Handshake\r\n";
		
		
		String serverLine = "Server: Pieka Server \r\n";
		
	//	String xPowerLine = "X-Powered-By: Servlet/3.1 JSP/2.3 (GlassFish Server Open Source Edition  4.1  Java/Oracle Corporation/1.8)\r\n";
		
		String connectionLine = "Connection: Upgrade\r\n";
		
		String secWebsocketAcceptLine = "Sec-WebSocket-Accept: " + WebSocketServer.secWebsocketAccept((String)handshake.get("Sec-WebSocket-Key")) + "\r\n";
		
		String upgradeLine = "Upgrade: websocket\r\n\r\n";
		
		handshakeResponse += headLine + serverLine +  connectionLine + secWebsocketAcceptLine + upgradeLine;
		
	//	System.out.println(handshakeResponse);
		
		
		try {
			out.write(handshakeResponse.getBytes());
		//	out.write("bla bla bla".getBytes());
			out.flush();
			
		//	out.close();
	//		System.out.println("Handshake response sent");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		hanshakeComplete = true;
		
	}
	
	
	
	
	private void sendDisconnect()
{
		
		
//		System.out.println("Sending disconnect");
		//65535
		
				byte[] toSend = null;
			
					toSend = new byte[2 ];
					toSend[0] = (byte)136; /// 10001000
					toSend[1]  = (byte)message.length;
					
				
				
				try {
					out.write(toSend);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					out.write(toSend);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
		
	}
	
	
	
	public boolean isHanshakeComplete() {
		return hanshakeComplete;
	}


	public void setHanshakeComplete(boolean hanshakeComplete) {
		this.hanshakeComplete = hanshakeComplete;
	}

	public void update() {
		try {
			if( in.available() > 0 )
			{
		//		System.out.println("New message");
				getMessage();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	
	
	public boolean equals(Object o)
	{
		return socket.equals(o);
	}
	
	public int hashCode()
	{
		return socket.hashCode();
	}
	
	
	
	
	
	
	
	

}
