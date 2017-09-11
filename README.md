# GDH - Generalized Diffie-Hellman Key Exchange Platform (In development)

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

TBD

# Code Quality

Every build the code runs through a couple of static code analyzers (PMD and findbugs) to ensure code quality is maintained.
Contributions of more code analyzers are welcome. Each push to the Github repository triggers a cloud build via TravisCI which in turn pushes the code into another cloud code analyzer (SonarQube). If anything goes wrong during any of these steps the build fails.

# Testing

The code is tested by both unit tests and integration tests. The integration testing involves actual spinning up of verticles, performing negotiations and checking the correctness and security of the transactions.
