# Design and Implementation of a Chat Server and Client

## Description 
This project contains the source code for a chat server and client. The server is a multithreaded application that can 
handle multiple clients. The server is responsible for authenticating the client and maintaining the list of online 
users. The client is a multithreaded application that can send and receive messages from other clients. The client also 
creates a UDP socket for receiving messages from other client.

## Motivation and Design

The motivation behind this project is to demonstrate the knowledge of the following concepts:
- Socket Programming
- Multithreading
- State Machine 

As mentioned in the report, the main motivation of server implementation was from the state machine. The server 
maintains various states for each client. 
The state machine is implemented by using the following design patterns:

- State Pattern (each state is represented different application state)
- Singleton Pattern (some of the classes and objects are shared between threads)

The initial communication between client and server has been re-written before finalising a simple state machine. 

Both Client and Server have module implementation. I.e. each package or client has its own responsibility. It 
can be modified or change without affecting other modules.


## Communication Patten 

Server and Client communicates using a simple string with specific format. This section highlights the message structure 
between client and server. 

Message Format: 
```<message_meta_data> SEPARATOR <message_text>```

For example 
```<MSGTO><></msgto jiggy hello, jiggy>```

Messages are encoded using base64 on the sender side and decoding on the receiving end. 

# ChatServer 

## Description
This project contains the application code for server and client. The development of was done by using the following 
technologies:
- Java 11
- Maven
- IntelliJ 

The compilation of the project is done by using the following command:
```javac -d . *.java ```

Both applications (client and server) have package structure which means that a command for compiling the java files 
must create folders based on these packages. Otherwise, while running the program, you will get an error message 
related to class path. It is beyond the scope of this. 

(For the development and testing purpose, I have used IntelliJ which does the compilation and running of the program 
automatically.). Executing JAVAC is an old school method. 

Please ensure that credential.txt file are with the same directory as the compiled files. 

To run the server, please use the following command:
```java org.weinnovateit.server.Server 60000 5```

The first argument represents the TCP port that server will listen to and the second command is an integrate value which represents the number allowed failed
login attempts before the server will block the client.



# Chat Client 

## Description 
For compiling the source code, please follow the same pattern 
```javac -d . *.java ```

As mentioned before, -d option will create the package structure.

To run the client, please use the following command:
```java ava org.weinnovateit.client.Client localhost 60000 35001```

Arguments:  
- localhost: represents the IP address of the server
- 60000: represents the TCP port of the server
- 35001: represents the UDP port of the client.

# Testing 
For testing the application, I have used the following tools:
- JUnit 5 (I could have used more detailed unit testing but didn't get much time create socket.)
- Shell Script 

The shell script is located in the util folder which at the root directory of the project. It is called client_test.sh. 

To run the test script, please use the following command:
```./client_test.sh```

Please reach the comments in the script for more details.

# Improvements

1. More unit test using proper mocking framework such mocking socket 
2. More detailed logging
3. Better transfer implementation for UDP (I could have defined packet header and use sequence numbers to figure out EOF)
4. Too much text between server and client. (Cache some of the common text at the client side to avoid the data transfer)


# Final Thoughts

I want to express my gratitude to the staff of COMP9331 for providing me with the opportunity. This project and the 
assignment was good refresher for my networking skills. Also, I should highlight the promptness by some of the staff 
members on the forum. It is worth to mention Isura who is very prompt and helpful. He has been constantly looking at 
the threads and provide his view. 