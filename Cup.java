import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Random;

import lcm.lcm.*;
import org.json.simple.*;
import chat.*;

public class Cup implements LCMSubscriber
{
	LCM lcm;
	private String floorNumber;
	private String host;
	private float temperature;
	private String sprinklerState;

	public Cup(String host, String floorNumber) throws IOException
	{
		this.lcm = new LCM();
		this.lcm.subscribe("BROADCAST", this);

		this.host = host;
		this.floorNumber = floorNumber;
		this.temperature = 0;
		this.sprinklerState = "TURN_OFF";
	}

	@Override
	public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins) {
		System.out.printf("%s\n", channel);
		try {
			if (channel.equals("BROADCAST")) {
				tea_t msg = new tea_t(ins);
				if (msg.message.equals("TURN_ON")) {
					this.sprinklerState = "TURN_ON";
				}
				System.out.printf("received %s command%n", msg.message);
			}
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}

	public void addTemperature(float increment) {
		this.temperature += increment;
	}

	public boolean reportTemperature() throws IOException {
		Socket socket = new Socket(this.host, 1234);

		DataOutputStream out = new DataOutputStream(
			socket.getOutputStream()
		);

		JSONObject jsonParams = new JSONObject();
		jsonParams.put("floor_number", this.floorNumber);
		jsonParams.put("temperature", Float.toString(this.temperature));

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put("method", "evaluate_temperature");
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

		System.out.printf(
			"SPRINKLER STATE: %s, TEMPERATURE: %s Celcius\n"
			, this.sprinklerState
			, this.temperature
		);

		socket.close();
		out.close();
		p.close();

		return true;
	}

	public boolean isTurnOn() {
		return this.sprinklerState.equals("TURN_ON");
	}

	public static void main(String args[])
	{
		Random random = new Random();
		Scanner scanner = new Scanner(System.in);

		try {
			System.out.println("Enter Host IP Address: ");
			String host = scanner.nextLine();
			System.out.println("Which floor are you? ");
			String floorNumber = scanner.nextLine();

			Cup cup = new Cup(host, floorNumber);
			while (true) {
				cup.addTemperature((random.nextFloat() - (float) 0.1) * (float) 10.0);
				cup.reportTemperature();
				if (cup.isTurnOn()) {
					System.out.println("Sprinkler turned on, exiting report.");
					break;
				}
				Thread.sleep(1000);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (InterruptedException e) {}
	}
}
