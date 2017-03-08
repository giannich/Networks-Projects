/*
 * Copyright (c) 1995, 2013, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 

import java.io.*;
import java.net.*;

public class EchoClient {
    public static void main(String[] args) throws IOException {
        
        // EchoClient accepts 2 arguments, which are the hostname and its portnumber
        if (args.length != 2) {
            System.err.println(
			       "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);

        // Try catch segment
        try 
        (
            // Creates an echo socket to the hostname and portnumber
            Socket echoSocket = new Socket(hostName, portNumber);

            // Sets out as the socket's outputstream (or whatever the server receives)
            PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);

            // Sets in as the socket's inputstream (or whatever the server sends back)
            BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));

            // Finally, sets stdIn as whatever the user types in the stdin of the client
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
	    ) 
        {
            // Read userinput from stin, and while it is not null, sends the message to the server and prints in stdout whatever the server sends back
            String userInput;
            while ((userInput = stdIn.readLine()) != null) 
            {
                out.println(userInput);
                System.out.println(in.readLine());
            }

	    } 

        // Wrong host exception
        catch (UnknownHostException e) 
        {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } 
        // Connection exception
        catch (IOException e) 
        {
            System.err.println("Couldn't get I/O for the connection to " +
			       hostName);
            System.exit(1);
        } 
    }
}