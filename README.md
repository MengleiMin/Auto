# Auto
Automatic update 
The things connecting to the Internet of Things is ubiquitous in the future, so the World of things is bound to be huge and complex. The complex update operation of devices is impossible by ordinary users due to the huge workload and expensive time cost. In this case, automatic update is a better choice. 
The problem of this project is to find and investigate means for secure pushing automatic updates to devices in an Internet of Things system, then propose own feasible implemented solution in a proof-of-concept scenario and measure its performance and security.

auto-app: It is the program to generate the single JAR file.
Administrator: upload JAR file to tomcat server.
tomcat: It is the server storing thr JAR file.
Manager: Download the JAR file from server and send this file to device.
Device: receive the JAR file and install it.
