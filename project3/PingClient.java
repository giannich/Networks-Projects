import java.io.*;
import java.net.*;
import java.util.*;
import static java.lang.Math.toIntExact;
import java.net.DatagramSocket;
import java.util.Timer;
import java.util.TimerTask;

public class PingClient {

	// Static variable being used in multiple functions
	private static String serverIP;
	private static int pings[];
	
	public static void main(String[] argv) 
	{
		// Initialize the command-line values
		serverIP = "";
		int portNum = -1;
		int pingCount = -1;
		int pingCooldown = -1;
		int pingTimeout = -1; 

		// Process command-line arguments.
    	for (String arg : argv) 
    	{
      		String[] splitArg = arg.split("=");
      		// Checks for Server IP Address
      		if (splitArg.length == 2 && splitArg[0].equals("--server_ip")) 
      			serverIP = splitArg[1];

      		// Checks for Server Port
      		else if (splitArg.length == 2 && splitArg[0].equals("--server_port")) 
        		portNum = Integer.parseInt(splitArg[1]);

        	// Checks for Ping Count
      		else if (splitArg.length == 2 && splitArg[0].equals("--count")) 
        		pingCount = Integer.parseInt(splitArg[1]);

        	// Checks for Ping Interval Period
      		else if (splitArg.length == 2 && splitArg[0].equals("--period")) 
        		pingCooldown = Integer.parseInt(splitArg[1]);

        	// Checks for Ping Timeout Period
      		else if (splitArg.length == 2 && splitArg[0].equals("--timeout")) 
        		pingTimeout = Integer.parseInt(splitArg[1]);

        	// Otherwise, return an error
      		else 
      		{
        		System.err.println("Usage: java PingClient --server_ip=<server ip addr> --server_port=<server port> --count=<number of pings to send> --period=<wait interval> --timeout=<timeout>");
        		return;
      		}
    	}

    	// Checks that the user put in all the arguments
	    if (portNum == -1 || serverIP.equals("") || pingCount == -1 || pingCooldown == -1 || pingTimeout == -1)
	    {
	  		System.err.println("Error: One or more of IP Address, Port number, Ping Count, Period, and/or Timeout is not specified");
	      	return;
	    }
	    // Prevents the user from putting a port number smaller than 1024
	    if (portNum <= 1024) 
	    {
	    	System.err.println("Warning: Avoid potentially reserved port number: " + portNum + " (should be > 1024)");
	    	return;
	    }

	    // Prints single line stating that we are pinging the specified IP Address
	    System.out.println("\nPING " + serverIP);

	    // Initalizes the pings array
	    pings = new int[pingCount];

		// For each ping, schedules a new timer
		for (int i = 0; i < pingCount; i++)
			new Scheduler(pingCooldown, i, portNum, pingTimeout);

		// At the end of the day, we just print the overall statistics, but we also need to schedule it beforehand
		// The StatPrinter class is schedulued to run after all the pings have been sent plus the timeout period starting from the last ping
		// For example, at 5 pings of 100ms between each ping with a timeout of 500ms, the StatPrinter is scheduled to run after (5 * 100) + 500 = 1000ms
		// This ensures that the printOverallPingStats is not run before all pings are received
		new StatPrinter(pingCooldown, pingCount, pingTimeout);
	}

	// Scheduler class used to schedule ping sending and single ping statistic print out
	private static class Scheduler 
	{
    	Timer timer;
    	int number;
    	int port;
    	int timeout;

    	// Scheduler Constructor
    	public Scheduler(int pingCooldown, int pingNum, int portNum, int pingTimeout) 
    	{
    		number = pingNum;
    		port = portNum;
    		timeout = pingTimeout;

        	timer = new Timer();
        	timer.schedule(new EchoTask(), pingCooldown * number);
		}

		// EchoTask class handles every task related to a single ping
    	class EchoTask extends TimerTask 
    	{
        	public void run() 
        	{
        		// Gets the time difference, stores it in the array and prints out the ping stat
        		try
        		{
        			int diff = echoStuff(port, timeout);
        			pings[number] = diff;
					System.out.println("PONG " + serverIP + ": seq=" + (number + 1) + " time=" + diff + " ms");
            		timer.cancel();
            	}
            	// Catches SocketTimeoutExceptions
				catch (SocketTimeoutException e)
				{
					pings[number] = timeout;
					timer.cancel();
	    		}
	    		// Catches IOExceptions
	    		catch (IOException e) 
				{
			      	System.err.println("Error processing ping request: " + e.getMessage());
			      	pings[number] = timeout;
			      	timer.cancel();
			    }
        	}
    	}
	}

	// Printer class that needs to be scheduled to print out the overall statistics
	private static class StatPrinter
	{
		Timer timer;
		int number;
		int timeout;
		int cooldown;

		// StatPrinter Constructor
		public StatPrinter(int pingCooldown, int packetNums, int pingTimeout)
		{
			number = packetNums;
			timeout = pingTimeout;
			cooldown = pingCooldown;

			timer = new Timer();
        	timer.schedule(new PrintTask(), (number * cooldown + timeout));
		}

		// PrintTask class prints out the overall ping statistics after a certain period of time
		class PrintTask extends TimerTask 
    	{
        	public void run()
        	{
        		printOverallPingStats(number, pings, timeout, cooldown);
        		timer.cancel();
        	}
    	}
	}

	// Main datagram function, roughly copied from PingServer's code
	public static int echoStuff(int portNum, int pingTimeout) throws IOException, SocketTimeoutException
	{
		byte[] sendBuf = new byte[256];
		DatagramSocket socket = new DatagramSocket();

		InetAddress address = InetAddress.getByName(serverIP);
		DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, portNum);

		// Sets the timeout period to pingTimeout, will throw a SocketTimeoutException if not received by timeout
		socket.setSoTimeout(pingTimeout);

		// Begins logging time and sends packet
		Long begin = System.currentTimeMillis();
		socket.send(packet);

		// Receives packet and logs time taken
		packet = new DatagramPacket(sendBuf, sendBuf.length);
		socket.receive(packet);
		Long end = System.currentTimeMillis();

		return (toIntExact(end - begin));
	}

	// Print all ping statistics
	private static void printOverallPingStats(int transmitted, int[] timeList, int pingTimeout, int pingCooldown) 
	{
		// Sets up a couple of variables first
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int avg = 0;
		int count = timeList.length;
		int totalTimeElapsed = 0;
		int pingsReceived = 0;
		int pingsLost = 0;
		int lossPercentage = 0;

		// Calculate the min, max, and avg
		for (int i = 0; i < count; i++)
		{
			// Here we check for the total time elapsed by calculating the longest time taken for a ping to be sent and received
			// This means that it is not always the case that the last ping sent defines the total time taken
			// But rather it is the one that took the longest to be sent and received
			if (totalTimeElapsed < (timeList[i] + i * pingCooldown))
				totalTimeElapsed = (timeList[i] + i * pingCooldown);

			// Goes to the next ping stat if we had a packet loss
			// This is here because we don't want it to affect the min/avg/max stats
			if (timeList[i] == pingTimeout)
			{
				pingsLost += 1;
				continue;
			}

			// Checks timing for max and min
			if (timeList[i] > max)
				max = timeList[i];

			if (timeList[i] < min)
				min = timeList[i];

			// Finally increments the number of pings received and adds it to the average and total time elapsed
			pingsReceived += 1;
			avg += timeList[i];
		}

		// Calculate the average and the loss percentage
		if (pingsReceived > 0)
			avg /= pingsReceived;
		else
		{
			min = 0;
			max = 0;
			avg = 0;
		}

		lossPercentage = pingsLost * 100 / transmitted;

		// Finally print everything out
		System.out.println("\n--- " + serverIP + " ping statistics ---");
		System.out.println(transmitted + " transmitted, " + pingsReceived + " received, " + lossPercentage + "% loss, time " + totalTimeElapsed + "ms");
		System.out.println("rtt min/avg/max = " + min + "/" + avg + "/" + max + " ms\n");
	}
}