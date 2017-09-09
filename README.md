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

# Usage

TBD

# Code Quality

Every build the code runs through a couple of static code analyzers (PMD and findbugs) to ensure code quality is maintained.
Contributions of more code analyzers are welcome.

# Testing

The code is tested by both unit tests and integration tests. The integration testing involves actual spinning up of verticles, performing negotiations and making sure the keys produced match, and that a key is never sent over the wire (among other things).
