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
		try {
			if (channel.equals("BROADCAST")) {
				tea_t msg = new tea_t(ins);
				System.out.println(String.format("%s: %s", msg.sender, msg.message));
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[])
	{
		try {
			Cup cup = new Cup();
			while (true) {
				Thread.sleep(1000);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) { }
	}
}
