package pieka.websocket;

public class LockFabric {

	private static Object socketTabLock = new Object();
	
	
	public static Object getSocketTabLock()
	{
		return socketTabLock;
	}
	
	
}
