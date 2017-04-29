package pieka.websocket;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Base64.Encoder;


public abstract class WebSocketServer implements Runnable{
	
	Socket socket = null;
	Sockets sockets  = new Sockets(this);
	
	ServerSocket server_socket = null;
	
	
	
	public WebSocketServer(int port)
	{
		
		
		try {
		  server_socket = new ServerSocket(port);
		  System.out.println("Server is listening on port "+ server_socket.getLocalPort());
		  
		  new Thread(this).start();
		  
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	
	public static String secWebsocketAccept(String s)
	{
		 MessageDigest md = null;
		  try {
			  	md = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  md.reset();
		  try {
			md.update( (s+"258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		  
		  //dGhlIHNhbXBsZSBub25jZQ=="+"258EAFA5-E914-47DA-95CA-C5AB0DC85B11
		  
		  Encoder encoder = Base64.getEncoder();

		  byte src[], dst[];
		  
		  src = md.digest();
		  dst = new byte[src.length*2];
		  
		  for(int i = 0 ; i < dst.length ; i++)
			  dst[i] = 0;
		  
		  encoder.encode(src, dst);
		  
		  int length = dst.length;
		  for(int i = 0 ; i < dst.length ; i++)
		  {
			  if( dst[i] == 0 )
			  {
				  length = i;
				  break;
			  }
			  	
		  }
		  
		  String result = new String(dst, 0, length);
		  
		  
		  
		  
		  return result;
	}
	
	public abstract void onConnect(ServerClient client);
	
	public abstract void onMessage(String message, ServerClient client);
	
	public abstract void onMessage(byte message[], ServerClient client);
	
	public abstract void onClose(ServerClient client);


	@Override
	public void run() {
		
		for(;;)
		{
		 	try {
				socket = server_socket.accept();
				System.out.println("New client " + socket.getInetAddress() );
				sockets.add(socket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	
	

}
