import java.io.*;
import java.net.*;
import java.util.Scanner;

import lcm.lcm.*;
import org.json.simple.*;
import chat.*;

public class Cup implements LCMSubscriber
{
	LCM lcm;
	private String cupOwner;
	private String ip;

	public Cup(String ip, String cupOwner) throws IOException
	{
		this.lcm = new LCM();
		this.lcm.subscribe("BROADCAST", this);

		this.cupOwner = cupOwner;
		this.ip = ip;
	}

	@Override
	public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins) {
		try {
			if (channel.equals("BROADCAST")) {
				tea_t msg = new tea_t(ins);
				System.out.printf("\r> %s: %s%n%s> ", msg.sender, msg.message, this.cupOwner);
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public boolean sendMessage(String message) throws IOException {
		Socket socket = new Socket(this.ip, 1234);

		DataOutputStream out = new DataOutputStream(
			socket.getOutputStream()
		);

		JSONObject jsonParams = new JSONObject();
		jsonParams.put("sender", this.cupOwner);
		jsonParams.put("message", message);

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put("method", "echoback");
		jsonRequest.put("params", jsonParams);

		BufferedReader in = new BufferedReader(
			new InputStreamReader(
				socket.getInputStream()
			)
		);

		PrintWriter p = new PrintWriter(out);
		p.println(jsonRequest);
		p.flush();

		String word = "";
		word = in.readLine();
		while (word.equals("null") || word.equals("")) {
			word = in.readLine();
		}

		String result = word;
		char c = (char) in.read();
		JSONObject resultJson = null;
		while (true) {
			result += c;
			c = (char) in.read();

			if (JSONValue.parse(result) != null) {
				resultJson = (JSONObject) JSONValue.parse(result);
				break;
			}
		}

		System.out.println("\r> message delivered!");

		socket.close();
		out.close();
		p.close();

		return true;
	}

	public static void main(String args[])
	{
		Scanner scanner = new Scanner(System.in);

		try {
			System.out.println("Enter Kettle's IP Address: ");
			String ip = scanner.nextLine();
			System.out.println("Who are you? ");
			String cupOwner = scanner.nextLine();

			Cup cup = new Cup(ip, cupOwner);
			while (true) {
				System.out.printf("%s> ", cupOwner);
				String message = scanner.nextLine();
				cup.sendMessage(message);
				Thread.sleep(1000);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) { }
	}
}
