
/* 
A Java program for a Client

Encryption code sections developed from the java tutorials on www.baeldung.com :
ssl: 
https://www.baeldung.com/java-ssl, https://www.baeldung.com/java-ssl-handshake-failures
key store: 
https://www.baeldung.com/java-keystore
encryption/decryption: 
https://www.baeldung.com/java-aes-encryption-decryption

and the oracle Security Developers Guide:
https://docs.oracle.com/javase/10/security/toc.htm
*/
import java.net.*;
import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.security.KeyStore;

public class Client {

    /**
     * Private class to establish a connection object
     */
    private static class ClientConnection {
        // initialize socket and input output streams
        private SSLSocket sslSocket;
        private DataOutputStream out;
        private DataInputStream in;
        private String address;
        private int port;
        private String msg;
        private String msgIn;
        private Boolean valid;

        // constructor to put ip address and port
        public ClientConnection(String address, int port) {
            this.address = address;
            this.port = port;
            this.msg = null;
            this.sslSocket = null;
            this.out = null;
            this.in = null;
            this.msgIn = null;
            this.valid = null;
        }

        /**
         * ssl socket code from oracle.com
         *
         * @param psw
         * @throws IOException
         */
        public void connect(String psw) throws Exception {
            // establish a connection
            SSLSocketFactory factory = null;
            try {
                SSLContext context;
                KeyManagerFactory keyManager;
                KeyStore ks;
                // For demo use default password "passphrase"
                char[] passphrase = "passphrase".toCharArray();

                // create Key Store
                context = SSLContext.getInstance("TLS");
                keyManager = KeyManagerFactory.getInstance("SunX509");
                ks = KeyStore.getInstance("JKS");

                // set up key manager to do server authentication
                ks.load(new FileInputStream("testkeys"), passphrase);
                keyManager.init(ks, passphrase);
                context.init(keyManager.getKeyManagers(), null, null);

                factory = context.getSocketFactory();

                this.sslSocket = (SSLSocket) factory.createSocket(this.address, this.port);

                // establish data outputstream to socket
                this.out = new DataOutputStream(this.sslSocket.getOutputStream());

                // send password for authenticating
                this.send(psw, false);
                System.out.println("Authenticating");
                this.read();
                if (this.msgIn.equals("SUCCESS")) {
                    this.valid = true;
                } else {
                    this.valid = false;
                }
                this.msgIn = null;
            } catch (Exception e) {
                throw e;
            }
        }

        public void send(String msg, Boolean connected) throws IOException {
            try {
                if (this.sslSocket == null || this.out == null) {
                    throw new IOException();
                } else if (connected) {
                    // send 'START' msg
                    this.out.writeUTF("START RNA" + "\n");
                    // send msg to server
                    this.out.writeUTF(msg + "\n");
                } else {
                    // send 'AUTH' msg
                    this.out.writeUTF("AUTH" + "\n");
                    // send psw to server
                    this.out.writeUTF(msg + "\n");
                }
            } catch (IOException e) {
                throw e;
            }
        }

        public void read() throws IOException {
            while (this.msgIn == null) {
                try {
                    this.in = new DataInputStream(new BufferedInputStream(this.sslSocket.getInputStream()));
                    this.msgIn = this.in.readUTF().trim();
                } catch (IOException e) {
                    throw e;
                }
            }
        }

        public void reset() {
            this.msg = null;
            this.msgIn = null;
        }

        public void disconnect() throws IOException {
            // close the connection
            try {
                if (this.in != null) {
                    this.in.close();
                }
                if (this.out != null) {
                    this.out.close();
                }
                if (this.sslSocket != null) {
                    this.sslSocket.close();
                }
            } catch (IOException e) {
                throw e;
            }
        }
    }

    /**
     * Function to determine if input is of type int
     *
     * @param input
     * @return boolean stating if input is of type int
     */
    public static boolean isInt(String input) {
        boolean check = false;
        try {
            Integer.parseInt(input);
            check = true;
        } catch (NumberFormatException err) {
        }
        return check;
    }

    /**
     * Function to determine if input is a valid port number
     *
     * @param port
     * @return boolean stating if input is valid port number
     */
    public static boolean isValidPort(String port) {
        boolean valid = false;
        if (isInt(port) && Integer.parseInt(port) >= 0 && Integer.parseInt(port) <= 65535) {
            valid = true;
        }
        return valid;
    }

    /**
     * Function to determine if input is a valid RNA expression
     *
     * @param rna
     * @return boolean stating if input is valid RNA string
     */
    public static boolean isValidRNA(String rna) {
        boolean valid = false;
        if (!rna.equals("") && rna.length() % 3 == 0 && rna.matches("^[GgCcAaTt]*$")) {
            valid = true;
        }
        return valid;
    }

    /**
     * Function for user to specify branch of application
     *
     * @return boolean determining if program continues to input or exits
     */
    public static boolean menuInput() {
        boolean required = true;
        boolean result = true;
        String input;
        // Create a new InputStreamReader and connecting to STDIN
        InputStreamReader istream = new InputStreamReader(System.in);

        // Create a new BufferedReader and connect it to the InputStreamReader
        BufferedReader bufRead = new BufferedReader(istream);
        while (required) {
            try {
                System.out.println("** MENU **\n- Enter 1 to input RNA string: \n- Enter 2 to exit: ");
                input = bufRead.readLine();
                if (isInt(input) && Integer.parseInt(input) == 1) {
                    required = false;
                } else if (isInt(input) && Integer.parseInt(input) == 2) {
                    required = false;
                    result = false;
                } else {
                    throw new IOException();
                }
            } catch (IOException err) {
                System.out.println("Invalid input: Please enter 1 or 2");
            }
        }
        return result;
    }

    /**
     * Function for user to input condon to send to server for optimisation
     *
     * @return string of the valid condon to send to server
     */
    public static String rnaInput() throws IOException {
        boolean required = true;
        String input = "";
        // Create a new InputStreamReader and connecting to STDIN
        InputStreamReader istream = new InputStreamReader(System.in);

        // Create a new BufferedReader and connect it to the InputStreamReader
        BufferedReader bufRead = new BufferedReader(istream);
        while (required) {
            try {
                System.out.println("Please enter a RNA sequence: ");
                // store input whilst removing whitespace
                input = bufRead.readLine().replaceAll(" ", "");
                if (isValidRNA(input)) {
                    required = false;
                } else {
                    System.out.println(
                            "Invalid input: RNA string must only contain chars G,C,A and/or T and length must be multiple of three");
                }
            } catch (IOException e) {
                throw e;
            }
        }
        return input;
    }

    /**
     * Function for user to input password. User a Console object with
     * readPassword() method to hide user input on screen.
     * https://www.tutorialspoint.com/java/io/console_readpassword.htm
     *
     * @param instruction of the instruction to print out to user
     * @return string of the user password
     */
    public static String passwordInput(String instruction) throws Exception {
        String input = "";
        Console cnsl = null;
        while (input.length() == 0) {
            try {
                // creates a console object
                cnsl = System.console();
                // if console is not null
                if (cnsl != null) {
                    // read password into the char array
                    char[] pwd = cnsl.readPassword(instruction);
                    input = new String(pwd);
                }
            } catch (Exception e) {
                throw e;
            }
        }
        return input;
    }

    /**
     * Function to perform the sending and receiving of messages from client to
     * server
     *
     * @param client A ClientConnection object connected to socket for msg
     *               communication
     */
    public static void communicateWithServer(ClientConnection client) throws IOException {
        try {
            client.send(client.msg, true);
            System.out.println("Sent RNA string to Server...");
            client.read();
            System.out.println("Received response from Server...");
            if (client.msgIn.equals("INVALID")) {
                System.out.println("RNA sent to Server was invalid.");
            } else {
                if (isValidRNA(client.msgIn)) {
                    System.out.println("Optimised RNA: " + client.msgIn);
                } else {
                    System.out.println("Optimised RNA receiced from Server was invalid.");
                }
            }

        } catch (IOException e) {
            throw e;
        }
    }

    public static void main(String args[]) {
        if (args.length >= 2) {

            InetAddress address = null;
            // validate given Hostname exists
            try {
                address = InetAddress.getByName(args[0]);
            } catch (UnknownHostException err) {
                System.out.println("Invalid Arg: Unkown Hostname.");
                System.exit(1);
            }

            int port = 0;
            // validate given port number
            if (isValidPort(args[1])) {
                port = Integer.parseInt(args[1]);
            } else {
                System.out.println("Invalid Arg: Port number must be an integer value 0-65535.");
                System.exit(1);
            }

            ClientConnection client = null;

            try {
                System.out.println("Communicating with Server...");
                client = new ClientConnection(address.getHostAddress(), port);

                // enter codon system password authorise connection
                String psw = passwordInput("Please enter codon system password: ");
                client.connect(psw);

                if (!client.valid) {
                    System.out.println("Invalid Password");
                    System.exit(1);
                }

                // confirm RNA string input method
                System.out.println("Client looking for file...");

                if (args.length == 3) {

                    System.out.println("File arg specified, attemting validation...");
                    BufferedReader reader = null;
                    // has file arg given so validate if file exists
                    try {
                        File codonFile = new File("./" + args[2]);
                        boolean exists = codonFile.exists();
                        if (!exists) {
                            throw new NullPointerException();
                        }
                        // get first line of file
                        reader = new BufferedReader(new FileReader(codonFile));
                        // store in ClientConnection object removing whitespace
                        client.msg = reader.readLine().replaceAll(" ", "");
                        System.out.println("File sucessfully read...");

                        // Validate RNA string
                        if (isValidRNA(client.msg)) {
                            try {
                                communicateWithServer(client);
                                client.send("DISCONNECT", true);
                                client.disconnect();
                            } catch (IOException e) {
                                System.out.println("Error Communicating with Server.");
                                System.exit(1);
                            }
                        } else {
                            System.out.println("Invalid Input: RNA string is incorrect format.");
                            System.exit(1);
                        }
                    } catch (Exception e) {
                        System.out.println("Invalid Arg: File must be in working directory.");
                        System.exit(1);
                    } finally {
                        reader.close();
                    }

                } else {
                    // no arg file given so ask for input
                    System.out.println("No file arg specified...");
                    boolean inputting = true;
                    while (inputting) {
                        if (menuInput()) {
                            // Option 1. ask to enter RNA string
                            try {
                                client.msg = rnaInput();
                            } catch (IOException e) {
                                System.out.println("Error reading input. Please try again.");
                            }
                            communicateWithServer(client);
                            client.reset();
                        } else {
                            // Option 2. exit
                            client.send("DISCONNECT", true);
                            client.disconnect();

                            inputting = false;
                        }
                    }
                }

            } catch (IOException e) {
                System.out.println("Error Connecting to Server." + e.getMessage());
                System.exit(1);
            } catch (Exception e) {
                System.out.println("Error Connecting to Server." + e.getMessage());
                System.exit(1);
            }

            // Finish program without error
            System.out.println("Goodbye!");
            System.exit(0);

        } else {
            System.out.println("Invalid Arg: Insufficient number of args supplied.");
            System.exit(1);
        }
    }
}
