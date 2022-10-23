/*
 * The purpose of Iperfer is to test the connection speed between two machines 
 * (a client and a server).
 * Once a server connection is established, a client will connect to it, and send 
 * it a constant stream of packets (thousand byte packets). 
 * It will send these packets for a specified amount of time that is
 * less than 10 minutes. Once the client is done sending and the server is
 * done receiving, both will print out the total number of packets sent/received
 * and the speed at which they sent/received those packets.
 */

import java.net.*; // Provides Socket Class

public class Iperfer {	
	
	/*
	 * When operating in client mode, 
	 * Iperfer will send TCP packets to a specific host for a specified time window
	 * and track how much data was sent during that time frame; it will calculate 
	 * and display the bandwidth based on how much data was sent in the elapsed time. 
	 */
    public static void Client(String address,int port, int time) throws Exception {
    	// Establishes connection to address at specified port
    	Socket serverSocket = new Socket (address, port);
    	
    	// Byte array is filled with zero's by default
    	byte[] packet = new byte[1000];
    	
    	// time * 1000 == translating seconds to milliseconds
    	long endtime = System.currentTimeMillis() + (time * 1000);
    	int dataSent = 0;
    	
    	//sends packets until the time specified is up
    	while (System.currentTimeMillis() < endtime) {
    		serverSocket.getOutputStream().write(packet);
    		dataSent += 1000; // Sending 1000 bytes of data each second 
    	}
    
    	// Disconnecting from server
        serverSocket.close();
        
        // dataSent * 8 translates to bits from bytes
        // bits/1,000,000 = Megabits
        // time = seconds
        double rate = ((dataSent * 8)/Math.pow(10,6)) / time;
        
        //prints the number of bytes sent and the rate it was sent at
        System.out.println("sent=" + Math.round(dataSent/1000) + " KB rate=" + (Math.round(rate * 1000) / 1000.0) + " Mbps");
    }

    public static void Server(int listenPort) throws Exception{
    	// Opens specified port for communication to a client
        ServerSocket server = new ServerSocket(listenPort);     
        
        // After 20 seconds, throw an exception if no data is received
        server.setSoTimeout(20 * 1000);
        
        // Wait (20 secs) for client to connect via specified port
        Socket clientSocket = server.accept();
      
        byte [] receivedPacket = new byte[1000];
        double dataReceived = 0, num = 0, rate = 0;
        long startTime = 0, endTime = 0;

        startTime = System.currentTimeMillis();
        
        while(num > -1) {
        	num = clientSocket.getInputStream().read(receivedPacket, 0, 1000);
        	dataReceived += num; // Num == bytes received
        }
        
        endTime = System.currentTimeMillis();
		

		clientSocket.close();
		server.close();
		
		// translating milliseconds to seconds
		int time = (int) ((endTime - startTime)/1000);

        // dataReceived * 8 translated to bits from bytes
        // bits/1,000,000 = Megabits
        // time = seconds
		rate = ((dataReceived * 8)/Math.pow(10,6)) / time;
		
		//displays the Kilobytes received and the rate they were received
		System.out.println("received=" + Math.round(dataReceived / 1000) + " KB rate=" + (Math.round(rate * 1000) / 1000.0) + " Mbps");
    }

    public static void main(String[] args){
       String address = null;
       int port = 0,time = 0;
       
       // Client 
        if(args.length == 7){
        	
            // Checking order of arguments
            if(!args[0].equals("-c") || !args[1].equals("-h")|| !args[3].equals("-p") || !args[5].equals("-t")) {
            	System.out.println("Error: invalid arguments");
            	System.exit(0);
            }
        	
            address = args[2];
            port = Integer.parseInt(args[4]);
            time = Integer.parseInt(args[6]);
            
            // Port number must be between 1024 and 65535
            if(port < 1024 || port > 65535) {
            	System.out.println("Error: port number must be in the range 1024 to 65535");
            	System.exit(0);
            }
            // Time must be at least 1 second and less than 5 minutes (300). 
            if(time < 1 || time > 300) {
            	System.out.println("Error: time must be in the range 1 to 300");
            	System.exit(0);
            }
            // hostname input value is always assumed to be valid.
            // If hostname is invalid, client() throws an UnknownHostException and is handled properly.
            try {
            	Client(address,port,time);
            }
            catch (UnknownHostException h) {
            	System.out.println("Error: Host is not reachable. Be sure you are using the correct host address (i.e 192.168.0.1");
            	System.exit(0);
            }
            catch (ConnectException c) {
				System.out.println("Error: Unable to connect to host. Be sure the server is actively listening to port " + port);
				System.exit(0);
            }
            catch (Exception e) {
				System.out.println("Error: Exception Thrown in Client. Investigate Code.");
				e.printStackTrace();
				System.exit(1);
            }
        }
        
        // Server
        else if(args.length == 3){
            // Checking args order 
            if(!args[0].equals("-s") || !args[1].equals("-p")) {
            	System.out.println("Error: invalid arguments");
            	System.exit(0);
            }
      
            port = Integer.parseInt(args[2]);
            
            if(port < 1024 || port > 65535) {
            	System.out.println("Error: port number must be in the range 1024 to 65535");
            	System.exit(0);
            }
            
            try {
                System.out.println("Your current IP address : " + InetAddress.getLocalHost());
                System.out.println("Your current Hostname : " + InetAddress.getLocalHost().getHostName());
            	Server(port);
            }
            catch(BindException b){
            	System.out.println("Port already in use");
            	System.exit(0);
            }
            catch(SocketTimeoutException s) {
            	System.out.println("Server Timeout.");
            	System.exit(0);
            }
            catch(UnknownHostException u) {
            	System.out.println("Unable to retrieve hosts IP Address");
            	System.exit(0);
            }
            catch(Exception e) {
				System.out.println("Exception Thrown in Server. Investigate Code");
				e.printStackTrace();
				System.exit(1);
            }
        }
        else {
        	// If any arguments are missing or additional arguments are given
        	System.out.println("Error: invalid arguments");
        	System.exit(0);
        }
    }
}
