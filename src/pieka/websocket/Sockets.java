package pieka.websocket;
import java.net.Socket;


public class Sockets implements Runnable{
	
	

	
	private ServerClient tab[];
	
	private int allocated;
	private int size;
	private WebSocketServer server;
	public Sockets(WebSocketServer server)
	{
		
		size = 0;
		allocated = 16;
		
		this.server = server;
		
		tab = new ServerClient[16];
		
		new Thread(this).start();

		
	}
	
	
	private void realloc()
	{
		allocated *= 2;
		ServerClient newTab[] = new ServerClient[allocated];
		
		
		for(int i = 0 ; i < allocated/2 ; i++)
		{
			newTab[i] = tab[i];
		}
		
		tab = newTab;
		
		
	}
	
	
	
	
	
	public void add(Socket socket)
	{
		
		System.out.println("Adding new socket");
		synchronized(LockFabric.getSocketTabLock())
		{
			if( size == allocated )
				realloc();
			
			tab[size++] = new ServerClient(socket, server);
			}
	}
	
	
	public void remove(Socket socket)
	{
		synchronized(LockFabric.getSocketTabLock())
		{
			int i;
			for(i = 0 ; i < size; i++)
			{
				if( tab[i].getSocket().equals(socket ) )
				{
					size--;
					break;
				}
			}
			
			for(;i<size;i++)
			{
				tab[i] = tab[i+1];
			}
		}
		
		
		
	}
	
	


	@Override
	public void run() {

		
		System.out.println("Sockets thread run");
		

		for(;;)
		{
			
			synchronized(LockFabric.getSocketTabLock())
			{
				for(int i = 0 ; i < size; i ++)
				{
					tab[i].update();
				}
			}
			
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	
	

}
