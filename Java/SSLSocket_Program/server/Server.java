
/* A Java program for a Server

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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import javax.net.ssl.*;
import java.security.KeyStore;
import javax.crypto.SecretKey;
import java.io.Console;
import javax.security.cert.X509Certificate;

public class Server {
    /**
     * Private class to establish a connection object
     */
    private static class ServerConnection {
        private Socket socket;
        private SSLServerSocket sslServer;
        private DataInputStream in;
        private DataOutputStream out;
        private int port;
        private String msg;
        private Boolean valid;

        // constructor with port
        public ServerConnection(int port) {
            this.port = port;
            this.socket = null;
            this.sslServer = null;
            this.in = null;
            this.out = null;
            this.msg = null;
            this.valid = null;
        }

        /**
         * SSL socket code from oracle.com
         *
         * @throws IOException
         */
        public void connect() throws Exception {
            // sets up ssl socket factory
            SSLServerSocketFactory factory = null;
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

                factory = context.getServerSocketFactory();
                ServerSocket listener = factory.createServerSocket(this.port);
                this.sslServer = (SSLServerSocket) listener;
                // require two-way ssl handshake
                this.sslServer.setNeedClientAuth(true);

            } catch (Exception e) {
                throw e;
            }
        }

        public void listen() throws IOException {
            try {
                // waits for client then initializes sslSocket
                this.socket = this.sslServer.accept();
            } catch (IOException e) {
                throw e;
            }
        }

        public void read() throws IOException {
            while (this.msg == null) {
                try {
                    this.in = new DataInputStream(new BufferedInputStream(this.socket.getInputStream()));
                    // read input and validate
                    this.msg = this.in.readUTF();
                    if (this.msg.equals("AUTH\n") || this.msg.equals("START RNA\n")) {
                        this.msg = in.readUTF();
                    } else if (this.msg.equals("DISCONNECT\n")) {
                        continue;
                    } else {
                        throw new IOException();
                    }
                } catch (IOException e) {
                    throw e;
                }
            }

        }

        public void send(String msg) throws IOException {
            try {
                // initialize output stream
                this.out = new DataOutputStream(this.socket.getOutputStream());
                // send msg to client
                this.out.writeUTF(msg + "\n");

            } catch (IOException e) {
                System.out.println("Error sending message to client");
                throw e;
            }
        }

        public boolean authorise() throws Exception {
            boolean check = false;
            try {
                this.read();
                if (validatePassword(this.msg)) {
                    check = true;
                }
            } catch (Exception e) {
                throw e;
            }
            return check;
        }

        public void reset() {
            this.msg = null;
        }

        public void disconnect() throws IOException {
            // close the connection
            try {
                System.out.println("Closing connection");
                if (this.in != null) {
                    this.in.close();
                }
                if (this.out != null) {
                    this.out.close();
                }
                if (this.socket != null) {
                    this.socket.close();
                }
            } catch (IOException e) {
                throw e;
            }
        }
    }

    /**
     * Function to authorise client password entry. reads the bytes from the db.txt
     * file gets salt and hash then uses Passwords singleton object to check aginst
     * the expected outcome.
     *
     * @param psw password from user
     * @return boolean stating if input is valid
     */
    public static boolean validatePassword(String psw) throws Exception {
        boolean check = false;
        try {
            String cleanPsw = psw.trim();
            byte[] array = Files.readAllBytes(Paths.get("db.txt"));
            byte[] salt = Arrays.copyOfRange(array, 0, 16);
            byte[] hash = Arrays.copyOfRange(array, 16, array.length);
            if (Passwords.isExpectedPassword(cleanPsw.toCharArray(), salt, hash)) {
                check = true;
            }
        } catch (Exception e) {
            throw e;
        }
        return check;
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
     * Function to determine if received msg is a valid RNA expression
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
     * Function to read file
     *
     * @param filePath path to file to read
     * @return Hash map contain keys: characters of aminoacids, to values: Array
     *         list of corresponding codon strings
     */
    public static HashMap<Character, ArrayList<String>> fileReader(String filePath) throws Exception {
        // data structure to organise Aminoacid/codon groupings
        HashMap<Character, ArrayList<String>> codons = new HashMap<Character, ArrayList<String>>();
        // buffereader to read lines
        BufferedReader buff = null;

        try {
            // create file object
            File file = new File(filePath);
            // create BufferedReader object from the File
            buff = new BufferedReader(new FileReader(file));

            String line = null;

            // read file line by line
            while ((line = buff.readLine()) != null) {
                // split the line
                String[] parts = line.split(",");
                // first part is codon, second is amino acid
                String codon = parts[0].trim();
                Character aminoacid = parts[1].trim().charAt(0);

                // add codon to corresponding key if it already exists
                if (codons.containsKey(aminoacid)) {
                    codons.get(aminoacid).add(codon);
                } else {
                    // add aminoacid key if does not exist with new ArrayList
                    ArrayList<String> co = new ArrayList<>();
                    co.add(codon);
                    codons.put(aminoacid, co);
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            // close the BufferedReader
            if (buff != null) {
                try {
                    buff.close();
                } catch (Exception e) {
                    throw e;
                }
            }
        }
        return codons;
    }

    /**
     * Function to optimise rna codon from client input. Optimisations deduced from
     * https://berthub.eu/articles/posts/reverse-engineering-source-code-of-the-biontech-pfizer-vaccine/
     *
     * @param rna string to optimise
     * @return string of the optimised rna string to send to the client
     */
    public static String rnaOptimisation(String rna) throws Exception {
        // transform to upper case and remove whitespace
        String rnaOptimise = rna.toUpperCase().trim();
        try {
            // get codon mapped to aminoacid data from file into hash map
            HashMap<Character, ArrayList<String>> codons = fileReader("codon-aminoacid.csv");

            String tempRna = "";

            // slice RNA string into individual codon to find optimsations
            for (int i = 0; i < rnaOptimise.length(); i += 3) {
                String currentCodon = rnaOptimise.substring(i, i + 3);

                if (currentCodon.equals("TAA")) {
                    // The original virus uses the TAA stop codon, the vaccine
                    // uses two TGA stop codons,
                    tempRna += "TGATGA";
                } else if (currentCodon.equals("GTA") || currentCodon.equals("GTT") || currentCodon.equals("AAA")) {
                    // change unoptimised codons from 'K' or 'V' aminoacid to
                    // ‘P’ (Proline) aminoacid
                    tempRna += "CCT";

                } else if (currentCodon.equals("CCA")) {
                    // change that might prevent a "hairpin".
                    tempRna += "CCT";

                } else if (!currentCodon.equals("ATG") && !currentCodon.equals("TGG")) {
                    // loop over hash map to find codon and optimise tail
                    // character
                    for (Map.Entry<Character, ArrayList<String>> entry : codons.entrySet()) {
                        // check if ArrayList contains the current codon
                        if (entry.getValue().contains(currentCodon)) {
                            if (entry.getKey().equals('R')) {
                                if (currentCodon.charAt(0) == 'A') {
                                    tempRna += "CGG";
                                } else {
                                    tempRna += "CGC";
                                }
                            } else if (entry.getKey().equals('S')) {
                                if (currentCodon.charAt(0) == 'A') {
                                    tempRna += "AGC";
                                } else {
                                    tempRna += "TCG";
                                }
                            } else {
                                for (int j = 0; j < entry.getValue().size(); j++) {
                                    if (entry.getValue().get(j).matches(".*C$")
                                            || entry.getValue().get(j).matches(".*G$")) {
                                        tempRna += entry.getValue().get(j);
                                    }
                                }
                            }
                        }
                    }

                } else {
                    // just add codon representing amino M or W: only one choice
                    tempRna += currentCodon;
                }
            }
            // update rna optimise string
            rnaOptimise = tempRna;
        } catch (Exception e) {
            System.out.println("Error while running Optimisation");
            throw e;
        }

        return rnaOptimise;
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
     * Function to store users salt and hashed password to .txt file. Simulates
     * database operations of storing user data Code sourced from:
     * https://examples.javacodegeeks.com
     *
     * @param salt
     * @param hashed
     */
    public static void writeToFile(byte[] salt, byte[] hashed) throws IOException, FileNotFoundException {
        // create file
        File file = new File("db.txt");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            // Writes bytes from the specified byte array to this file output stream

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(salt);
            outputStream.write(hashed);

            byte[] input = outputStream.toByteArray();
            fos.write(input);
        } catch (FileNotFoundException e) {
            System.out.println("File not found" + e);
            throw e;
        } catch (IOException e) {
            System.out.println("Exception while writing file " + e);
            throw e;
        } finally {
            // close the streams using close method
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                System.out.println("Error while closing stream: " + e);
                throw e;
            }

        }
    }

    /**
     * Function that generates a private encyption key from user input password and
     * random salt. Singleton object Passwords is used for salt generation, and hash
     * of password. Salt and salted and hashed password stored to file to simulate
     * user data being stored in database.
     *
     * @return the SecretKey generated from password and salt
     */
    public static SecretKey getPassword() throws Exception {
        SecretKey secret = null;
        try {
            // get input
            String password = passwordInput("Please enter codon system password: ");
            byte[] salt = Passwords.getNextSalt();
            byte[] hashpsw = Passwords.hash(password.toCharArray(), salt);

            // simulate stored login details
            writeToFile(salt, hashpsw);
        } catch (Exception e) {
            throw e;
        }
        return secret;

    }

    public static void main(String args[]) {
        if (args.length == 1) {
            int port = 0;

            // validate given port number
            if (isValidPort(args[0])) {
                port = Integer.parseInt(args[0]);
            } else {
                System.out.println("Invalid Arg: Port number must be an integer value 0-65535.");
                System.exit(1);
            }
            try {
                // establish new server connection object
                ServerConnection server = new ServerConnection(port);
                // connect server listening on specified port
                server.connect();
                System.out.println("Server started...");

                // generate password
                getPassword();

                boolean runServer = true;
                while (runServer) {
                    System.out.println("Waiting for a client ...");
                    server.listen();
                    if (server.authorise()) {
                        server.send("SUCCESS");
                        server.valid = true;
                        System.out.println("Client accepted");
                    } else {
                        server.send("FAIL");
                        System.out.println("Invalid Password");
                        server.valid = false;
                    }
                    // make msg field null
                    server.reset();

                    // read/optimise/send cycle if connected until disconnect signal sent
                    while (server.valid) {
                        try {
                            System.out.println("Listening for message...");
                            server.read();
                            System.out.println("Read msg from client...");

                            if (server.msg.equals("DISCONNECT\n")) {
                                System.out.println("Disconnecting client...");
                                server.disconnect();
                                server.valid = false;
                            } else {
                                // optimise RNA string
                                try {
                                    System.out.println("Optimising RNA...");
                                    // run optimisation then send to client
                                    if (isValidRNA(server.msg.trim())) {
                                        server.send(rnaOptimisation(server.msg));
                                    } else {
                                        server.send("INVALID");
                                    }
                                    System.out.println("Sent response to client.");
                                } catch (Exception e) {
                                    server.disconnect();
                                    server.valid = false;
                                } finally {
                                    server.reset();
                                }

                            }
                        } catch (IOException e) {
                            System.out.println("Error reading from client.");
                            server.disconnect();
                            server.valid = false;
                        } finally {
                            server.reset();
                        }
                    }

                }

            } catch (IOException e) {
                System.out.println("Server Connection Error: " + e.getMessage());
                System.exit(1);
            } catch (Exception e) {
                System.out.println("Server Connection Error: " + e.getMessage());
                System.exit(1);
            }

        } else {
            System.out.println("Invalid Arg: Incorrect number of args supplied.");
            System.exit(1);
        }

    }
}
