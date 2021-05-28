##Project Protocol:

The following protocol is for a secured network application to get optimisations of RNA sequences. Note all messages sent over network consists of a string of ASCII characters followed by the line feed character.
To run the application use the provided shell scripts startClient.sh and start- Server.sh.
Open two terminals and once in the working directory start the server in the first terminal. The command is:
```bash 
$ ./startServer.sh ∗arg1∗ 
```
- arg1 is the port (port range 0-65535)

On starting the server the user is greated with the following message:


```bash 
$ Server started...
$ Please enter codon system password :
```

The user can input a system password to allow for authorised access. The password cannot be an empty string. The server then waits till a client asks to connect.

Next in the working directory start the client in the second terminal using the command:
```bash
$ ./startClient.sh ∗arg1∗ ∗arg2∗ ∗arg3∗
```
- arg1 is the host (for the demo localhost). 

- arg2 is the port (port range 0-65535). 

- arg3 is the optional filename (a .txt file).

On starting the client the user is greated with the following message:
```bash 
$ Communicating with Server...
$ Please enter codon system password :
```
Now the client user can only access the server with the system password setup when starting the server. The user inputs the password. This cannot be an empty String. It is sent encypted over the secure TSL socket connection to the server and verified against the stored salt and hash.

If the password is correct the server sends back a SUCCESS message. If the password is incorrect the server sends back a FAIL message and the connection gets dropped waiting for the next client.

Following successful user verification the application will look for a specified file in the argument inputs. If a file is specified first line of file is read and validity of RNA sequence is verified.

To be valid a sequence must be made up of G, C, A, and/or T characters to represent codons. These can be read into client as lower or uppercase, but are read back from the server as uppercase.

If the sequence is invalid the client sends a DISCONNECT message to terminate the session. If no file is specified the user menu is shown:
```bash 
$ Authenticating
$ Client looking for file ...
$ No file arg specified...

$ ∗∗ MENU ∗∗
$ − Enter 1 to input RNA string :
$ − Enter 2 to exit:
```

Entering 2 sends a DISCONNECT message to the server ending the session.
Entering 1 asks for the user to input an RNA string. Once entered the validity of the RNA sequence is verified. Again, to be valid a sequence must be made up of G, C, A, and/or T characters, lower or upper case to represent codons. If invalid the client ask the user to input again.


In either scenario (file input or user input) once verified the client sends a START RNA message followed by the RNA Sequence over the encypted TSL secure connection. The client then waits for a response.

On the server side an incoming START RNA message causes the next input to be read and verified that it is a valid RNA sequence. If it is not valid the server sends an INVALID message to the client and the connection is terminated.

If it is a valid sequence it gets optimised by the optimisation rules outlined in the following section. Once optimised the RNA string is sent back over the secure connection to the client.

On recieving an RNA string the client verifies if it is valid. If not a meassge is displayed telling the user an incorrect RNA sequence was return and the session is terminated. If it is valid the sequence is displayed for the user.

Finally if the RNA string was read from file a DISCONNECT message is sent to the server and the session is terminated. If it was from user input the user returns to the main menu to continue entering RNA sequences or exit.