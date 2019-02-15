# Auto
Automatic update   
The things connecting to the Internet of Things is ubiquitous in the future, so the World of things is bound to be huge and complex. The complex update operation of devices is impossible by ordinary users due to the huge workload and expensive time cost. In this case, automatic update is a better choice.   
The problem of this project is to find and investigate means for secure pushing automatic updates to devices in the Internet of Things system, then propose own feasible implemented solution in a proof-of-concept scenario and measure its performance and security.  

Overall Workflow:
First, the administrator will interact with database directly. Likely, the manager will interact with the database directly. Then the manager will communicate with devices if there is a new version application in database through the Internet. Finally, the device will receive the new version application from devices manager via local network, then decide that if update it after checking.

Code function:  
auto-app: It is the program to generate the single JAR file.  
Administrator: upload JAR file to tomcat server.  
tomcat: It is the server storing thr JAR file.  
Manager: Download the JAR file from server and send this file to device.  
Device: receive the JAR file and install it.  


Thesis:  
Min M. Evaluation and Implementation for Pushing Automatic Updates to IoT Devices[J]. 2017.  
