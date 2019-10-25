Build Status Coverage Status Mentioned in Awesome Vert.x Known Vulnerabilities

Quality Gate



GDH : Generalized Diffie-Hellman Key Exchange Platform
A Diffie-Hellman key exchange library for multiple parties built on top of the asynchronous, event-driven Vert.x framework.

Overview
Diffie-Hellman has been the de-facto standard for key exchange for many years. Two parties who want to communicate on an insecure channel, can use it to generate symmetric keys, and encrypt the messages between them. Diffie-Hellman (or derivatives of it, e.g. Elliptic Curve Diffie-Hellman) is commonly used in many authentication protocols and confidential tunneling schemes such as SSL/TLS, SSHv2, SNMPv3, and many more. The most common and general scenario for the use of Diffie-Hellman is two parties that want to exchange messages over an insecure network. Common use-cases are client to web-server or peer-to-peer file-sharing communication. However, the case where multiple parties need to share a secret key is rarely addressed. Such cases may arise in complex distributed systems where participants are located on different machines, and need to communicate with each other directly, rather than through one central entity. Instead of generating a secret key for each pair of participants, it is possible to generate a single secret key shared by all participants, in a manner which is resistable to eavesdropping attacks. This is where Generalized Diffie-Hellman comes in.

The following sequence diagram illustrates how the key exchange is performed. At first, two large numbers are distributed among the participants in plaintext. These numbers are the cyclic group generator (g) and a large prime(N). Then the participants come up with their secret numbers (a,b,c) which they do not reveal to anyone. They then begin a series of transactions at the end of which, they can each calculate the same secret key, without it ever being transmitted on the wire. In old-style Diffie-Hellman we would have 3 different keys produced, one per each couple of participants. This scheme can be performed for any number of participants. The number of messages needed for N participants to complete a key exchange is N(N-1).



Due to the discrete logarithm problem, it is impossible for any third party listening in on the communication to compute the final key.

Security
It has been known for a few years now that the NSA has the ability to break Diffie-Hellman encrypted traffic which uses 1024 bit primes. Because these primes need to be carefully picked out, only a handful of such primes are used in practice. That's why it's beneficial for the NSA to invest in breaking those primes - it allows them to decrypt a big percentage of the world's traffic. This is why GDH uses 2048 bit primes and generates 256 bit symmetric keys, which are considered safe for now.

Prerequisites
Written in Java 8.

Built with Gradle.

Installation
Get the code and build:

git clone https://github.com/maxamel/GDH.git
cd GDH
gradle clean build    
Get the generated Jar from the build directory of the project. Add it to the build path of your project and that's it.

Usage
The basic usage of the library is spinning up verticles and initiating a key exchange between them. Once you have the key you can start encrypting/decrypting messages safely between the verticles. Note this library only provides a key exchange platform and utility methods for encryption/decryption. The network layer (e.g messaging protocol) must be implemented by the user.

The basic object used for deploying and undeploying verticles is the PrimaryVertex.

PrimaryVertex pv = new PrimaryVertex();
The verticle object participating in the key exchange is the GDHVertex. Let's define our first GDHVertex and call it activeVertex as it will be the one who initiates key exchanges. All other verticles will be passive. The following example will be run between two verticles on localhost, but can be run with multiple participants in a distributed environment.

GDHVertex activeVertex = new GDHVertex();
Define a Configuration for the verticle:

Configuration config = new Configuration();
// add parameters to the Configuration
config.setIP("localhost").setPort("5000").setRetries(5).setLogLevel(Level.OFF);
// assign the configuration to the verticle
activeVertex.setConfiguration(config);
Now let's define another verticle to participate in the key exchange.

GDHVertex passiveVertex = new GDHVertex();
Configuration config2 = new Configuration();
config2.setIP("localhost").setPort("5001").setExchangeTimeout(5000).setLogLevel(Level.OFF);
passiveVertex.setConfiguration(config2);
Once we have all participants defined, we can go ahead and form a group with the Configuration of one of the verticles. The id of the group is determined by its nodes, so if you construct 2 groups with the same nodes it will essentially be the same group.

Group g = new Group(config,
                    new Node("localhost","5000"),
                    new Node("localhost","5001"));
Now it's all set up and you can run the verticles and initiate a key exchange. The most important rule when developing with Vert.x (or any asynchronous platform) is DO NOT BLOCK THE EVENT LOOP! So remember not to perform blocking operations inside the asynchronous calls.

pv.run(passiveVertex,deployment1 -> {
    if (deployment1.succeeded()) {
        pv.run(activeVertex,deployment2 -> {
        	if (deployment2.succeeded()) {
        		activeVertex.exchange(g.getGroupId(), exchange -> {
        			if (exchange.succeeded()) {
        			    // the key is available in this context and also later as a Future object
        				System.out.println("Got new key: " + exchange.result());
        			}
        			else {
        				System.out.println("Error exchanging!");
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
You can also use blocking code for key exchange:

pv.run(passiveVertex,deployment1 -> {
    if (deployment1.succeeded()) {
        pv.run(activeVertex,deployment2 -> {
            if (deployment2.succeeded()) {
                // get the key as a Future. Do not block inside the asynchronous call
                CompletableFuture<BigInteger> futureKey = activeVertex.exchange(g.getGroupId());
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
You can even use blocking code for the deployments. The verticle which initiates key exchanges should still be deployed using an asynchronous call (Otherwise you have to busy wait on it with a while loop!). All other nodes will participate in the exchange once they are up and running.

pv.run(activeVertex,deployment1 -> {
    if (deployment1.succeeded()) {
        pv.run(passiveVertex);
        // get the key as a Future. Do not block inside the asynchronous call
        CompletableFuture<BigInteger> futureKey = activeVertex.exchange(g.getGroupId());
    }
    else {
        System.out.println("Error deploying!");
    }
} 
At any point you can access the exchanged key as a CompletableFuture object from any verticle. This object is a representation of the key. The actual key might not be available at this moment in time, but will be made available as soon as the exchange finishes. Here are just a handful of options you have with the CompletableFuture:

CompletableFuture<BigInteger> key = passiveVertex.getKey(g.getGroupId());

// Wait for the key exchange to complete and get the final key
BigInteger fin = key.get();

// Wait for the key for a bounded time and throw Exception if this time is exceeded
BigInteger fin = key.get(1000, TimeUnit.MILLISECONDS);

// Get the key immediately. If it's not available return the default value given as a parameter (null)
BigInteger fin = key.getNow(null);
Don't forget to kill the verticles when you're finished with them. As in the deployment, you can use either asynchronous calls or blocking code:

pv.kill(activeVertex,undeployment1 -> {
	if (undeployment1.succeeded()) {
		System.out.println("First undeployment successful!");
		pv.kill(passiveVertex,undeployment2 -> {
                if (undeployment2.succeeded()) {
                    System.out.println("Second undeployment successful!");
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
Let's say you have a distributed system, where each machine is running GDH, and you have no way of choosing or enforcing the GDHVertex which initiates the key exchange, i.e. every machine runs the same code. Hese's a sample code which can run in such an environment and enforce only one key exchange initiator.

GDHVertex vertex = new GDHVertex();
        
Configuration config = new Configuration();
config.setIP("localhost").setPort("5000").setRetries(8).setLogLevel(Level.DEBUG);
vertex.setConfiguration(config);
        
Group g = new Group(config,
          new Node("172.52.44.120","5000"),
          new Node("172.52.44.121","5000"),
          new Node("172.52.44.122","5000"));
        
vertex.addGroup(g);
        
PrimaryVertex pv = new PrimaryVertex();
pv.run(vertex);
        
if (vertex.getNode().equals(g.getActiveNode())) vertex.exchange(g.getGroupId());
CompletableFuture<BigInteger> key = vertex.getKey(g.getGroupId());      // <---- key here
Code Quality
This project is analyzed on Sonarcloud and on Snyk. Every build the code runs through a couple of static code analyzers (PMD and findbugs) to ensure code quality is maintained. Each push to the repository triggers a cloud build via TravisCI, which in turn pushes the code into Sonarcloud to find coding bugs and into Snyk to find security vulnerabilities in the dependencies. If anything goes wrong during any of these steps the build fails.

Testing
The code is tested by both unit tests and integration tests. The integration testing involves actual spinning up of verticles, performing exchanges and checking the correctness and security of the transactions. Testing must cover at least 80% of the code, otherwise the quality gate of Sonarcloud fails.

Logging
GDH logs messages at different points during the exchange. This allows easy debugging and also lets users follow the exchange and helps understand the protocol. Logs are also used in tests. For example, verifying the final key after the exchange is NOT transmitted over the wire, or counting the number of messages required to complete a key exchange. So if you change the logging messages, make sure this hasn't affected any tests.

License
Published under the MIT License. This basically means the software is free and anyone can use it however they wish. No liability or warranty.
