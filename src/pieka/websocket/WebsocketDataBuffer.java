package pieka.websocket;

public class WebsocketDataBuffer {

	
	private byte[] bytes;
	
	private int size = 10000;
	
	
	private int currentByte = 0;
	
	private int top = 0;
	
	
	public WebsocketDataBuffer()
	{
		bytes = new byte[size];
	}
	
	public WebsocketDataBuffer(int size)
	{
			this.size = size;
			
			
			bytes = new byte[size];
	}
	
	
	
	public void pushByte(byte b)
	{
		
		bytes[top] = b;
		
		top++;
		top %= size;
		
		
	}
	
	public boolean hasNext()
	{
		return top != currentByte;
	}
	
	
	public byte next()
	{
		byte v = bytes[currentByte];
		currentByte++;
		currentByte%=size;
		
		return v;
	}
	
	
	
	
	
}
