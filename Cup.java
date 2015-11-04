import java.io.*;

import lcm.lcm.*;
import chat.*;

public class Cup implements LCMSubscriber
{
	LCM lcm;

	public Cup() throws IOException 
	{
		this.lcm = new LCM();
		this.lcm.subscribe("BROADCAST", this);
	}
	
	@Override
	public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins) {
		System.out.println("");
	}

	public static void main(String args[])
	{
		System.out.println("Hello, World!");
	}
}
