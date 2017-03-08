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

// Usual imports
import java.net.*;
import java.io.*;
// This is for string tokenizing
import java.util.StringTokenizer;
// These are for date formatting
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
// These are for file reading
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
// These are for image reading
//import java.util.Base64;
//import java.lang.Object;
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;

public class HTTPServer {

    public static String[][] redirectList = new String[10][2];
    public static int redirectCount = 0;
    public static int portNumber;
    public static String dataBuffer;

    public static void main(String[] args)
    {            
        // Checks if the arguments are following the correct format
        if (args.length != 1 || !args[0].startsWith("--serverPort=")) {
            System.err.println("Usage: java EchoServer --serverPort=<port number>");
            System.exit(1);
        }

        // Prints a bunch of garbage...
        System.out.println("\n\tEchoServer/1.0 Starting...");
        
        // Gets the port number from arg[0]
        StringTokenizer argTokens = new StringTokenizer(args[0], "=");
        argTokens.nextToken();
        portNumber = Integer.parseInt(argTokens.nextToken());

        // Prints more garbage...
        System.out.println("\tCurrently Listening on port number [" + portNumber + "]");

        // Populates the redirectList from www/redirect.def and prints them out
        populateRedirects();
        System.out.println("\tCurrent Redirect List:");
        for (int i = 0; i < redirectCount; i++)
            System.out.println("\t" + redirectList[i][0] + " -> " + redirectList[i][1]);

        int continueListening = 0;

        // Listening loop, will only break out when the user inputs nothing from the server's side
        do
        {
            continueListening = listenConnections();
        } while (true);
    }

    public static int listenConnections() {

        System.out.println("\nStarting New Session\nWaiting For New Connections...");
        
        // Try catch segment
        try 
        (
            // Creates a new server socket at the port number 
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket clientSocket = serverSocket.accept();

            // Sets out as the socket's outputstream (or whatever the client receives)
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true); 
            //DataOutputStream dataOut = new DataOutputStream(clientSocket.getOutputStream());

            // Sets in as the socket's inputstream (or whatever the client sends)              
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        ) 
        {
            System.out.println("Connection Detected");

            // Read inputline from whatever the client sends in, and while it is not null, prints the message on stdout and echoes it back to the client
            // Keep in mind that when the client sends a null line, it also terminates the server since inputLine will also be set to null
            String inputLine;
            String responseMessage = "";
            String compoundMessage = "";

            // Session Loop Starts
            while ((inputLine = in.readLine()) != "\n\r")
            {
                // If there is an empty line, break out of the while loop
                if (inputLine.equals(""))
                    break;

                // Keep a log of all the client's request and add them into compoundMessage
                compoundMessage += inputLine + "\n";
            }

            // Interprets the compoundMessage through respondRequest
            responseMessage = respondRequest(compoundMessage);
            // Prints out the response message and exits the current Session
            System.out.println("Client Request:\n" + compoundMessage + "\n");
            System.out.println("Response message:\n" + responseMessage + "\n");
            System.out.println("Ending Session");
            out.println(responseMessage);
            return 1;

            /********************************
            * WARNING: CODE GRAVEYARD BELOW *
            ********************************/

            //byte[] imageInByte;
            //BufferedImage originalImage = ImageIO.read(new File("www/images/uchicago/logo.png"));
            //ImageIO.write(originalImage, "png", dataOut);
            
            //File imageFile = new File("www/images/uchicago/logo.png");
            //FileInputStream fileInputStreamReader = new FileInputStream(imageFile);
            //byte[] bytes = new byte[(int)imageFile.length()];
            //Path path = Paths.get("www/images/uchicago/logo.png");
            //byte[] bytes = Files.readAllBytes(path);

            //dataOut.write(bytes, 0, (int) imageFile.length());
	    }
        catch (IOException e) 
        {
            System.out.println("Exception caught when trying to listen on port "
			       + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }

        return 1;
    }

    public static String respondRequest(String requestMessage)
    {
        // Tokenizes the request lines that are delimited by "\n", and then tokenizes the first line as requestLineTokens 
        StringTokenizer requestLine = new StringTokenizer(requestMessage, "\n");
        StringTokenizer requestLineTokens = new StringTokenizer(requestLine.nextToken());

        // Breaks out of the function in case the requestLineTokens does not have the expected 3 tokens, we also hardcode the HTTP version in case
        if (requestLineTokens.countTokens() != 3)
            return "HTTP/1.1 400 Bad Request";

        String requestType = requestLineTokens.nextToken();
        String requestDir = requestLineTokens.nextToken();
        String requestVer = requestLineTokens.nextToken();

        /************
        * GET BLOCK *
        ************/
        if (requestType.equals("GET"))
        {
            // Gets the file object ready
            File retFile = new File("www" + requestDir);

            // First, we check if the file is in the redirect list; if it is, we return the 301 message and the new location
            for (int i = 0; i < redirectCount; i++)
                if (requestDir.startsWith(redirectList[i][0]))
                    return requestVer + " 301 Moved Permanently\nLocation: " + redirectList[i][1];

            // Here we break out of the function in case the file does not exist
            if (!retFile.exists())
                return requestVer + " 404 Not Found";

            // Here we break out of the function in case the requested file is actually a directory
            if (retFile.isDirectory())
                return requestVer + " 400 Bad Request";

            // Tokenizs the directory and discards the fist token (anything before the period)
            StringTokenizer dirTokens = new StringTokenizer(requestDir, ".");
            dirTokens.nextToken();
            String fileExtension = dirTokens.nextToken();
            String mimeType = handleMIME(fileExtension);

            // Assumes such HTTP version exists
            String returnMessage = requestVer + " 200 OK\n";
            // Appends the current date
            returnMessage += "Date: " + getDate(new Date()) + "\n";
            // Appends the server name
            returnMessage += "Server: HTTPServer/1.0\n";
            // Appends the last modified date
            returnMessage += "Last-Modified: " + getDate(new Date(retFile.lastModified())) + "\n";
            // Appends the file length
            returnMessage += "Content-Length: " + retFile.length() + "\n";
            // Appends the file MIME type
            returnMessage += "Content-Type: " + mimeType + "\n\n";

            return returnMessage + readFile("www" + requestDir);
        }

        /*************
        * HEAD BLOCK *
        *************/
        else if (requestType.equals("HEAD"))
        {
                        // Gets the file object ready
            File retFile = new File("www" + requestDir);

            // First, we check if the file is in the redirect list; if it is, we return the 301 message and the new location
            for (int i = 0; i < redirectCount; i++)
                if (requestDir.startsWith(redirectList[i][0]))
                    return requestVer + " 301 Moved Permanently\nLocation: " + redirectList[i][1];

            // Here we break out of the function in case the file does not exist
            if (!retFile.exists())
                return requestVer + " 404 Not Found";

            // Here we break out of the function in case the requested file is actually a directory
            if (retFile.isDirectory())
                return requestVer + " 400 Bad Request";

            // Tokenizs the directory and discards the fist token (anything before the period)
            StringTokenizer dirTokens = new StringTokenizer(requestDir, ".");
            dirTokens.nextToken();
            String fileExtension = dirTokens.nextToken();
            String mimeType = handleMIME(fileExtension);

            // Assumes such HTTP version exists
            String returnMessage = requestVer + " 200 OK\n";
            // Appends the current date
            returnMessage += "Date: " + getDate(new Date()) + "\n";
            // Appends the server name
            returnMessage += "Server: HTTPServer/1.0\n";
            // Appends the last modified date
            returnMessage += "Last-Modified: " + getDate(new Date(retFile.lastModified())) + "\n";
            // Appends the file length
            returnMessage += "Content-Length: " + retFile.length() + "\n";
            // Appends the file MIME type
            returnMessage += "Content-Type: " + mimeType + "\n\n";

            return returnMessage;
        }

        // Otherwise just returns a 403 Forbidden Error
        else
            return "HTTP/1.1 403 Forbidden";
    }

    // Returns a string with the current date, accepts a Date object, which is instanced when it is called
    // For example, getDate(new Date()) returns the current date
    // While getDate(new Date(File.lastModified())) creates an instance of Date object from File.lastModified() and passes it to getDate()
    // Source: http://beginnersbook.com/2013/05/current-date-time-in-java/
    public static String getDate(Date dateobj) 
    {
        // The format below is as follows: "Mon, 30 Jan 2017 14:49:08 CST"
        DateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        return(df.format(dateobj));
    }

    public static void populateRedirects()
    {
        File redirectFile = new File("www/redirect.defs");

        // If the redirect file does not exist, just break out of the function
        if (!redirectFile.exists())
            return;

        try
        (
            BufferedReader redirectReader = new BufferedReader(new FileReader("www/redirect.defs"));
        )
        {
            String redirectLine = redirectReader.readLine();
            StringTokenizer tempTokens;

            while (redirectLine != null)
            {
                tempTokens = new StringTokenizer(redirectLine, " ");
                redirectList[redirectCount][0] = tempTokens.nextToken();
                redirectList[redirectCount][1] = tempTokens.nextToken();
                redirectCount++;
                redirectLine = redirectReader.readLine();
            }
        }
        catch (IOException e) 
        {
            System.out.println("Exception caught when trying to read redirect.defs");
            System.out.println(e.getMessage());
        }

        return;
    }

    // Returns a plaintext of whatever is in the file
    // Source: http://www.java2s.com/Tutorials/Java/java.nio.file/Files/Java_Files_readAllBytes_Path_path_.htm
    public static String readFile(String filePath)
    {
        Path path = Paths.get(filePath);
        try 
        {
          byte[] byteStream = Files.readAllBytes(path);

          String outputStream = new String(byteStream);
          return(outputStream);
        } 
        catch (IOException e) 
        {
          System.err.println(e);
          return "";
        }
    }

    // Just handles the MIME types, I put it here since it's gonna be less cluttered
    public static String handleMIME(String fileExtension)
    {
        switch (fileExtension) {
            case "html":
                return "text/html";
            case "txt":
                return "text/plain";
            case "png": 
                return"image/png";
            case "jpg":
                return "image/jpeg";
            case "pdf":
                return "application/pdf";
            default:
                return fileExtension;
        }
    }
}