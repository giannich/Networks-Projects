/*
 * Copyright (c) 2013, Oracle and/or its affiliates. All rights reserved.
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

import java.net.*;
import java.io.*;

public class EchoServer {
    public static void main(String[] args) throws IOException {
            
        // Gets the port number from the command line

        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
        
        int portNumber = Integer.parseInt(args[0]);
        
        // Try catch segment

        try 
        (
            // Creates a new server socket at the port number 
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
            Socket clientSocket = serverSocket.accept();     

            // Sets out as the socket's outputstream (or whatever the client receives)
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); 

            // Sets in as the socket's inputstream (or whatever the client sends)              
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) 
        {
            // Read inputline from whatever the client sends in, and while it is not null, prints the message on stdout and echoes it back to the client
            // Keep in mind that when the client sends a null line, it also terminates the server since inputLine will also be set to null
            String inputLine;
            while ((inputLine = in.readLine()) != null) 
            {
                // Note how I changed it, so that the server determines what is being sent back
                out.println("Server echos back: " + inputLine);
                // Keep in mind that this also captures a null read-line
                System.out.println("Client says: " + inputLine);
            }
	    }

        catch (IOException e) 
        {
            System.out.println("Exception caught when trying to listen on port "
			       + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}