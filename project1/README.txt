/************************************
*									*
* 	Name: Gianni Chen				*	
* 	MPCS 54001: Networks			*
* 	Project 1 						*
* 	Due Date: Wed Jan 11th @ 5:30PM	*
*									*
*************************************

Contents:

project1
|
|--EchoClient.java
|--EchoServer.java
|--makefile
`--README.txt

Notes:
- You can quickly compile the files through the "make" command, and clean them through "make clean"
- You need to pass the hostname and the port number in EchoClient through the following format: "java EchoClient <host name> <port number>""
- The same applies to the port number in EchoServer through the similar format: "java EchoServer <port number>""
- This is mainly due to the fact that the linux computers do not have the Apache Commons CLI libraries installed
- You simply run the client and the server as usual as seen in the above examples