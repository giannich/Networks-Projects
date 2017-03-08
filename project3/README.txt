/************************************
*									*
* 	Name: Gianni Chen				*	
* 	MPCS 54001: Networks			*
* 	Project 3 						*
* 	Due Date: Sun Mar 5th @ 11:59PM	*
*									*
*************************************

Contents:

project3
|
|--PingClient.java
|--makefile
`--README.txt

Compilation and Running Notes:
- You can quickly compile the files through the "make" command, and clean them through "make clean", please note that the clean command removes all .class files

Client commandline format:
	"java PingClient --server_ip=<server ip addr> --server_port=<server port> --count=<number of pings to send> --period=<wait interval> --timeout=<timeout>"

Other Notes:
- The program uses a scheduler named StatPrinter to print out the overall ping statistics
- The StatPrinter class is schedulued to run after all the pings have been sent plus the timeout period starting from the last ping
- For example, at 5 pings of 100ms between each ping with a timeout of 500ms, the StatPrinter is scheduled to run after (5 * 100) + 500 = 1000ms
- This ensures that the printOverallPingStats is not run before all pings are received 