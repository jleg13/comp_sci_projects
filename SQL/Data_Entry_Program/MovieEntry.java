package Data_Entry_Program;

import java.io.*;
import java.sql.*;

public class MovieEntry {

    /******************************************************************************/
    /**
     * private class to create A row Attribute object that holds all the information
     * relevant to a particular column of the row to be inserted
     */
    private static class RowAttribute {
        String title;
        String instruction;
        boolean required;
        String input;
        int typeCode;
        boolean valid;
        String error;

        RowAttribute(String title, String instruction, boolean required, String input, int typeCode, boolean valid,
                String error) {
            this.title = title;
            this.instruction = instruction;
            this.required = required;
            this.input = input;
            this.typeCode = typeCode;
            this.valid = valid;
            this.error = error;
        }
    }

    /******************************************************************************/

    /**
     * stringReader takes a RowAttribute argument and use the relvant data to diplay
     * instructions to the user, reads in the input and performs client side
     * validation including null inputs, and incorrect data types.
     * 
     * @param attribute A Row attribute object hold info relevant to the column
     */
    public static void stringReader(RowAttribute attribute) {
        // Create a new InputStreamReader and connecting to STDIN
        InputStreamReader istream = new InputStreamReader(System.in);

        // Create a new BufferedReader and connect it to the InputStreamReader
        BufferedReader bufRead = new BufferedReader(istream);
        String input = null;
        boolean required = true;
        boolean correct;
        while (required) {
            try {
                correct = true;
                // print out error message if re-inputing data
                if (!attribute.valid) {
                    System.out.println(attribute.error);
                }
                // print instruction and read in the line
                System.out.print(attribute.instruction);
                input = bufRead.readLine();

                // check input according to expected datatype
                switch (attribute.typeCode) {
                    case 1: {
                        if (!isInt(input)) {
                            input = "";
                        }
                        break;
                    }
                    case 5: {
                        if (input.length() > 50) {
                            if (attribute.required) {
                                input = "";
                            } else {
                                correct = false;
                            }
                        }
                        break;
                    }
                    case 6: {
                        if (input.length() > 20) {
                            if (attribute.required) {
                                input = "";
                            } else {
                                correct = false;
                            }
                        }
                        break;
                    }
                    case 3: {
                        if (!isFloat(input)) {
                            input = "";
                        }
                        break;
                    }
                    case 4: {
                        if (!isDate(input)) {
                            correct = false;
                        }
                        break;
                    }
                    case 0: {
                        if (!input.equals("y") && !input.equals("n")) {
                            input = "";
                            attribute.valid = false;
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
                if (!attribute.required && input.isEmpty() || attribute.required && !input.isEmpty()
                        || !attribute.required && !input.isEmpty() && correct) {
                    required = false;
                    attribute.valid = true;
                }
            } catch (IOException err) {
                System.out.println(attribute.error);
            }
        }
        // after input loop finalised assigned input to RowAttribute object
        attribute.input = input;
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
            System.out.println("Invalid Input: Requires numerical input.");
        }
        return check;
    }

    /**
     * Function to determine if input is of type float
     * 
     * @param input String
     * @return boolean stating if input is of type float
     */
    public static boolean isFloat(String input) {
        boolean check = false;
        try {
            Float.parseFloat(input);
            check = true;
        } catch (NumberFormatException err) {
            System.out.println("Invalid Input: Requires numerical input.");
        }
        return check;
    }

    /**
     * Function to determine if input is of type date
     * 
     * @param input String
     * @return boolean stating if input is of type date
     */
    public static boolean isDate(String input) {
        boolean check = false;
        try {
            if (!input.isEmpty()) {
                Date.valueOf(input);
                check = true;
            }
        } catch (IllegalArgumentException err) {
            System.out.println("Invalid Input: Requires date format 'YYYY-MM-DD'.");
        }
        return check;
    }

    /******************************************************************************/
    public static void main(String args[]) {

        System.out.println(
                "\n********************************************************************************\n\n \t\t\t  Welcome To MovieDirect\n\n********************************************************************************\n\n");

        /*
         * An array of RowAtrribute objects that store all information related to login
         * details
         */
        RowAttribute[] login = {
                new RowAttribute("database", "Database: ", true, "", 2, true, "Invalid Input: Invalid Database name."),
                new RowAttribute("user", "User: ", true, "", 2, true, "Invalid Input: Invalid User."),
                new RowAttribute("password", "Password: ", true, "", 2, true, "Invalid Input: Invalid Password.") 
            };

        /*
         * An array of RowAtrribute objects that store all information related to each
         * data input required
         */
        RowAttribute[] attributes = {
                new RowAttribute("movie_id", "Please enter the id for the new movie: ", true, "", 1, true,"Input must be a unique identifier"),
                new RowAttribute("movie_title", "Please enter the title for the new movie: ", true, "", 2, true,"Input must have less then 100 chacacters."),
                new RowAttribute("director_first_name", "Please enter the director's first name: ", true, "", 5, true,"Input must have less then 50 chacacters."),
                new RowAttribute("director_last_name", "Please enter the director's last name: ", true, "", 5, true,"Input must have less then 50 chacacters."),
                new RowAttribute("genre", "Please enter the genre of the movie: ", true, "", 6, true, "Input must be one of: Action, Adventure, Comedy, Romance, Science Fiction, Documentary, Drama or Horror."),
                new RowAttribute("media_type", "Please enter the media type: ", false, "", 6, true, "Input can be either DVD or Blu-Ray."),
                new RowAttribute("release_date", "Please enter the movies's release date: ", false, "", 4, true, "Requires date format 'YYYY-MM-DD'."),
                new RowAttribute("studio_name", "Please enter the movies's studio: ", false, "", 5, true, "Input must have less then 50 chacacters."),
                new RowAttribute("retail_price", "Please enter the retail price of the Movie: ", true, "", 3, true, "Input must be greater than 0"),
                new RowAttribute("current_stock", "Please enter the number of copies in stock: ", true, "", 1, true, "Input must be greater than or eqaul 0."), 
            };

        // Database connection using users input
        Connection conn = null;
        while (conn == null) {
            try {
                // loop over login details to allow user input
                for (RowAttribute detail : login) {
                    stringReader(detail);
                }
                Class.forName("org.postgresql.Driver");
                String url = "jdbc:postgresql://localhost/" + login[0].input;
                conn = DriverManager.getConnection(url, login[1].input, login[2].input);
            } catch (ClassNotFoundException e) {
                System.out.println("Failed to load PostgreSQL JDBC driver");
                System.exit(1);
            } catch (SQLException e) {
                System.out.println("Invalid Login credentials. Please try again.");
            }
        }

        /*
         * Outer loop allows user input with client-side validation and allows user to
         * enter multiple rows to the database
         */

        boolean inputting = true;
        while (inputting) {
            // get user input
            for (RowAttribute detail : attributes) {
                stringReader(detail);
            }

            /**
             * Inner loop tries to insert user input into table catching constraint
             * violation on SQL exceptions, then ask user to re insert offending data
             */
            boolean connected = true;
            while (connected) {
                try {
                    Statement stmt = null;
                    // Create a new statement object
                    stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    // Get all records in the movie table
                    ResultSet uprs = stmt.executeQuery("SELECT * FROM movies");
                    // Create a new row in the ResultSet object
                    uprs.moveToInsertRow();
                    // Add new movies information to the new row of data
                    for (RowAttribute detail : attributes) {
                        switch (detail.typeCode) {
                            case 1: {
                                uprs.updateInt(detail.title, Integer.parseInt(detail.input));
                                break;
                            }
                            case 2: {
                                uprs.updateString(detail.title, detail.input);
                                break;
                            }
                            case 5: {
                                uprs.updateString(detail.title, detail.input);
                                break;
                            }
                            case 6: {
                                uprs.updateString(detail.title, detail.input);
                                break;
                            }
                            case 3: {
                                uprs.updateFloat(detail.title, Float.parseFloat(detail.input));
                                break;
                            }
                            case 4: {
                                Date val = null;
                                if (!detail.input.isEmpty()) {
                                    val = Date.valueOf(detail.input);
                                }
                                uprs.updateDate(detail.title, val);
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                    }

                    // Insert the new row of data to the database
                    uprs.insertRow();
                    // Move the cursor back to the start of the ResultSet object
                    uprs.beforeFirst();

                    System.out.printf("\nSuccess! A new entry for %s has been entered into the database.\n",attributes[1].input);

                    RowAttribute cont = new RowAttribute("continue", "Would you like to enter another movie? y/n: ",true, "", 2, true, "Invalid Input: Input must be either 'y' or 'n'.");

                    //loop to ask user to input more rows
                    boolean continuing = true;
                    while (continuing) {
                        stringReader(cont);
                        if (cont.input.equals("n")) {
                            connected = false;
                            inputting = false;
                            continuing = false;
                            try {
                                // Close the database connection
                                conn.close();
                                System.out.println("Goodbye!");
                            } catch (SQLException e) {
                                System.out.println("Database connection error. Please try again.");
                            }
                        } else if (cont.input.equals("y")) {
                            // break inner loops
                            connected = false;
                            continuing = false;
                        } else {
                            cont.valid = false;
                        }
                    }
                } catch (SQLException e) {
                    //loop over chained exceptions if more then one
                    while (e != null) {
                        // check the reason for the exception
                        String sqlState = e.getSQLState();
                        String message = e.getMessage();
                        //determine course of action
                        switch (sqlState) {
                            case "23505": {
                                attributes[0].valid = false;
                                stringReader(attributes[0]);
                                break;
                            }
                            case "22001": {
                                if (message.contains("character(100)")) {
                                    attributes[1].valid = false;
                                    stringReader(attributes[1]);
                                }
                                break;
                            }
                            case "23514": {
                                if (message.contains("media")) {
                                    attributes[5].valid = false;
                                    stringReader(attributes[5]);
                                } else if (message.contains("genre")) {
                                    attributes[4].valid = false;
                                    stringReader(attributes[4]);
                                } else if (message.contains("price")) {
                                    attributes[8].valid = false;
                                    stringReader(attributes[8]);
                                } else if (message.contains("stock")) {
                                    attributes[9].valid = false;
                                    stringReader(attributes[9]);
                                }
                                break;
                            }
                            default: {
                                break;
                            }
                        }
                        // If exception has been chained; process the next exception in the chain
                        e = e.getNextException();
                    }
                }
            }
        }
    }
}