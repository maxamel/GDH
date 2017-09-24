[![Travis CI](https://travis-ci.org/maxamel/GDH.svg)](https://travis-ci.org/maxamel/GDH)<br/>
[![Quality Gate](https://sonarcloud.io/api/badges/gate?key=GDH)](https://sonarcloud.io/api/badges/gate?key=GDH)<br/>
[![Code Coverage](https://sonarcloud.io/api/badges/measure?key=GDH&metric=coverage)](https://sonarcloud.io/api/badges/measure?key=GDH&metric=coverage)<br/>

# GDH - Generalized Diffie-Hellman Key Exchange Platform

A Diffie-Hellman key exchange library for multiple parties built on top of the asynchronous event-driven Vert.x framework.

# Overview

Diffie-Hellman has been the de-facto standard for key exchange for many years. Two parties who want to communicate on an insecure channel, can use it to generate symmetric keys, and encrypt the messages between them. 
Diffie-Hellman (or derivatives of it, e.g. Elliptic Curve Diffie-Hellman) is commonly used in many authentication protocols and confidential tunneling schemes such as SSL/TLS, SSHv2, SNMPv3, and many more. 
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

The verticle object participating in the key exchange is the GDHVertex. 
Let's define our first GDHVertex and call it activeVertex as it will be the one who initiates key negotiations:
```java 
GDHVertex activeVertex = new GDHVertex();
```
Define a Configuration for the verticle:
```java
Configuration config = new Configuration();
// add parameters to the Configuration
config.setIP("localhost").setPort("5000").setRetries(5).setLogLevel(Level.OFF);
// assign the configuration to the verticle
activeVertex.setConfiguration(config);
```

Now let's define another verticle to participate in the key exchange. 
```java
GDHVertex passiveVertex = new GDHVertex();
Configuration config2 = new Configuration();
config2.setIP("localhost").setPort("5001").setRetries(5).setLogLevel(Level.OFF);
passiveVertex.setConfiguration(config2);
```

Once we have all participants defined, we can go ahead and form a group with the Configuration of one of the verticles:
```java
Group g = new Group(config,activeVertex.getNode(),passiveVertex.getNode());
```

Run the verticles and initiate a key negotiation:
```java
pv.run(passiveVertex,deployment1 -> {
    if (deployment1.succeeded()) {
        pv.run(activeVertex,deployment2 -> {
        	if (deployment2.succeeded()) {
        		activeVertex.negotiate(g.getGroupId(), exchange -> {
        			if (exchange.succeeded()) {
        			    // the key is available in this context and also later as a Future object
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
    }
    else {
        System.out.println("Error deploying!");
    }
}        	
```

At any point we can access the exchanged key as a CompletableFuture object from any verticle.
This object is a representation of the key. The actual key might not be available at this moment in time,
but will be made available as soon as the negotiation finishes. Here are just a handful of options you have 
with the completableFuture:
```java
CompletableFuture<BigInteger> key = passiveVertex.getKey(g.getGroupId()).getKey();

// Wait for the key exchange to complete and get the final key
key.get();

// Wait for the key for a bounded time and throw Exception if this time is exceeded
key.get(1000, TimeUnit.MILLISECONDS);

// Get the key immediately. If it's not available return the default value given as a parameter (null)
key.getNow(null);
```

Don't forget to kill the verticles when you're finished with them:
```java
pv.kill(activeVertex,undeployment1 -> {
	if (undeployment1.succeeded()) {
		System.out.println("First undeployment successful!");
		pv.kill(passiveVertex,undeployment2 -> {
            if (undeployment2.succeeded()) {
                System.out.println("second undeployment successful!");
            }
            else {
                System.out.println("Error undeploying!");
            }
        }      
	}
	else {
		System.out.println("Error undeploying!");
	}
}      
```

# Code Quality

Every build the code runs through a couple of static code analyzers (PMD and findbugs) to ensure code quality is maintained.
Each push to the Github repository triggers a cloud build via TravisCI, which in turn pushes the code into another cloud code analyzer (Sonarcloud). If anything goes wrong during any of these steps the build fails.

# Testing

The code is tested by both unit tests and integration tests. The integration testing involves actual spinning up of verticles, performing negotiations and checking the correctness and security of the transactions. Testing must cover 80% of the code, otherwise the quality gate of Sonarcloud fails.