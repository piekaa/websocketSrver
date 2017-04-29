package pieka.websocket;

public class Vector {

	
	private byte bytes[];
	

	private int size;
	private int allocated;
	
	
	public Vector()
	{
		size = 0;
		allocated = 32;
		
		
		bytes = new byte[allocated];
		
	}
	
	
	private void  realloc()
	{
		
		allocated *= 2;
		
		byte[] newTab = new byte[allocated];
		
		for(int i = 0 ; i < size ; i++)
		{
			 newTab[i] = bytes [i];
		}
		
		
		bytes = newTab;
		
	}
	
	
	public void push(byte b)
	{
		
		if( size == allocated)
			realloc();
		bytes[size++] = b;
	}
	
	
	public byte[] getBytes()
	{
		return bytes;
	}
	
	
	public int size()
	{
		return size;
	}
	
	public byte get(int i)
	{
		return bytes[i];
	}
	
	
	public byte[] getUsedBytes()
	{
		byte b[] = new byte[size];
		
		for(int i = 0 ; i < size ; i ++)
			b[i] = bytes[i];
		return b;
	}
	
	
}
