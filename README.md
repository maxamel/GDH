[![Travis CI](https://travis-ci.org/maxamel/GDH.svg)](https://travis-ci.org/maxamel/GDH)<br/>
[![Quality Gate](https://sonarcloud.io/api/badges/gate?key=GDH)](https://sonarcloud.io/api/badges/gate?key=GDH)<br/>

# GDH - Generalized Diffie-Hellman Key Exchange Platform

A Diffie-Hellman key exchange library for multiple parties built on top of the Vert.x framework.

# Overview

Diffie-Hellman has been the de-facto standard for key exchange for many years. Two parties who want to communicate on an insecure channel, 
can use it to generate symmetric keys, and encrypt the messages between them. Diffie-Hellman (or derivatives of it, e.g. 
Elliptic Curve Diffie-Hellman) is commonly used in many authentication protocols and confidential tunneling schemes such as 
SSL/TLS, SSHv2, SNMPv3, and many more. 
The most common and general scenario for the use of Diffie-Hellman is two parties that want to exchange messages over an insecure network. 
Common use-cases are client to web-server or peer-to-peer file-sharing communication. 
However, the case where multiple parties need to share a secret key is rarely addressed. Such cases may arise in complex distributed 
systems where participants are located on different machines, and need to communicate with each other directly, rather than through one 
central entity. Instead of generating a secret key for each pair of participants, it is possible to generate a single secret key shared  by all participants, in a manner which is resistable to eavesdropping and Man-In-The-Middle attacks. This is where Generalized Diffie-Hellman comes in.

The following sequence diagram illustrates how the key exchange is performed. At first, the participants come up with their secret numbers (a,b,c) which they do not reveal to anyone. They then begin a series transactions at the end of which, they can each calculate the same secret key, without it ever being transmitted on the wire. In old-style Diffie-Hellman we would have 3 different keys produced, one per each couple of participants. 
This scheme can be performed for any number of participants. The number of messages needed for N participants to complete a key exchange is N(N-1).  

<p align="center">
  <img src="https://github.com/maxamel/GDH/blob/master/GDH.png" />
</p>

# Usage

The basic usage of the library is spinning up verticles and initiating a key negotiation between them.
Once you have the key you can start encrypting/decrypting messages safely between the verticles. 

The basic object used for deploying and undeploying verticles is the PrimaryVertex. 

```java
PrimaryVertex pv = new PrimaryVertex();
```

The verticle object participating in the key exchange is the GDHVertex:
```java 
GDHVertex vertex = new GDHVertex();
```
Define a Configuration for the vertex:
```java
Configuration config = new Configuration();
// add parameters to the Configuration
config.setIP("localhost").setPort("5000").setRetries(5).setLogLevel(Level.OFF);
// assign the configuration to the verticle
vertex.setConfiguration(config);
```

Define the group of nodes which will participate in the negotiation of keys.
Suppose we have another verticle running on IP 111.200.255.200:
```java
Node a = new Node("localhost","5000");
Node b = new Node("111.200.255.200","3356");
Group g = new Group(conf,a,b);
```

Run the verticle and initiate a key negotiation:
```java
pv.run(vertex,deployment -> {
	if (deployment.succeeded()) {
		v.negotiate(g.getGroupId(), exchange -> {
			if (exchange.succeeded()) {
				System.out.println("Got new key: " + exchange.result());
			}
			else {
				System.out.println("Error negotiating!");
			}
		}
	}
	else {
		System.out.println("Error deploying!");
	}
}          	
```
Don't forget to kill the verticles when you're finished with them:
```java
pv.kill(vertex,undeployment -> {
	if (undeployment.succeeded()) {
		System.out.println("Undeployment successful!");
	}
	else {
		System.out.println("Error undeploying!");
	}
}      
```

# Code Quality

Every build the code runs through a couple of static code analyzers (PMD and findbugs) to ensure code quality is maintained.
Contributions of more code analyzers are welcome. Each push to the Github repository triggers a cloud build via TravisCI, which in turn pushes the code into another cloud code analyzer (Sonarcloud). If anything goes wrong during any of these steps the build fails.

# Testing

The code is tested by both unit tests and integration tests. The integration testing involves actual spinning up of verticles, performing negotiations and checking the correctness and security of the transactions.
