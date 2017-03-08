/************************************
*									*
* 	Name: Gianni Chen				*	
* 	MPCS 54001: Networks			*
* 	Project 2 						*
* 	Due Date: Wed Feb 5th @ 5:30PM	*
*									*
*************************************

Contents:

project2
|
|--HTTPServer.java
|--makefile
`--README.txt

Compilation and Running Notes:
- You can quickly compile the files through the "make" command, and clean them through "make clean"
- You need to pass the port number in HTTPServer through the following format: "java HTTPServer --serverPort=<host name>"

Other Notes:
- Unless specified by the user, the HTTPServer runs on a loop and will always be on standy to listen for a connection. The only way to exit the program is to simply sigkill it
- As soon as you start the program, the server will look through the redirect list in order to keep track of what needs to be redirected, it will then:
	- start a new session
	- listen from the port
	- read the request
	- interpret it
	- spit out the response
	- close the session
	- start over with a new session
- I was not able to implement an easy way to deliver images and pdfs since I don't know what kind of byte format the other side should receive on the other end. Other than that, plaintext and html work fine