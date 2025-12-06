import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class TorontoDatabase {
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";
    public static final String RESET = "\u001B[0m";

    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    private Connection connection;
    private static final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    
    public TorontoDatabase() {
        Properties prop = new Properties();
        String fileName = "auth.cfg";

        try {
            FileInputStream configFile = new FileInputStream(fileName);
            prop.load(configFile);
            configFile.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Could not find config file.");
            System.exit(1);
        } catch (IOException ex) {
            System.out.println("Error reading config file.");
            System.exit(1);
        }

        String username = prop.getProperty("username");
        String password = prop.getProperty("password");

        if (username == null || password == null) {
            System.out.println("Username or password not provided.");
            System.exit(1);
        }

        String connectionUrl = "jdbc:sqlserver://uranium.cs.umanitoba.ca:1433;"
                + "database=cs3380;"
                + "user=" + username + ";"
                + "password=" + password + ";"
                + "encrypt=false;"
                + "trustServerCertificate=false;"
                + "loginTimeout=30;";

           try {
            // Load the driver
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(connectionUrl);
            System.out.println("Connected to database successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("SQL Server JDBC Driver not found.");
            System.out.println("Make sure mssql-jdbc-12.4.2.jre8.jar is in your classpath.");
            e.printStackTrace();
            System.exit(1);
        } catch (SQLException e) {
            System.out.println("Error connecting to the database.");
            System.out.println("Connection URL: " + connectionUrl.replace(password, "******"));
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void executeSqlFile(String filePath) {
        StringBuilder sql = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                sql.append(line).append("\n");
            }
            executeSqlStatements(sql.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeSqlStatements(String sql) {
        try (Statement statement = connection.createStatement()) {
            String[] queries = sql.split(";");
            for (String query : queries) {
                if (!query.trim().isEmpty()) {
                    statement.executeUpdate(query.trim());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // used- helper method to get neighbourhood ID by neighbourhood name
    private int getNeighbourhoodIdByName(String neighbourhoodName) throws SQLException {
        String sql = "SELECT neighbourhood_id FROM neighbourhoods WHERE neighbourhood_name = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, neighbourhoodName);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("neighbourhood_id");
                }
            }
        }
        return -1; // Return -1 if not found
    }

    // 1. used- Crimes in a specific neighbourhood (by name), then get the id and then run the actual query
    public void showCrimesInNeighbourhood(int neighbourhoodId, int limit) {
    String sql = """
            SELECT TOP (?) c.*
            FROM crimes c
            JOIN neighbourhoods n ON c.neighbourhood_id = n.neighbourhood_id
            WHERE n.neighbourhood_id = ?;
            """;

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setInt(1, limit);
        statement.setInt(2, neighbourhoodId);

        try (ResultSet resultSet = statement.executeQuery()) {

            List<List<String>> table = new ArrayList<>();

            List<String> headers = Arrays.asList(
                    "Crime ID", "Crime Type", "Criminal ID", "Neighbourhood ID"
            );

            while (resultSet.next()) {
                table.add(Arrays.asList(
                        String.valueOf(resultSet.getInt("crime_id")),
                        resultSet.getString("crime_type"),
                        String.valueOf(resultSet.getInt("criminal_id")),
                        String.valueOf(resultSet.getInt("neighbourhood_id"))
                ));
            }

            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
        }

    } catch (SQLException e) {
        e.printStackTrace();
    }
}

    


    //1. called- Crimes in a specific neighbourhood (by name), then get the id and then run the actual query
    public void showCrimesInNeighbourhood(String neighbourhoodName, int limit) {
        try {
            int neighbourhoodId = getNeighbourhoodIdByName(neighbourhoodName);
            if (neighbourhoodId == -1) {
                System.out.println("Neighbourhood not found: " + neighbourhoodName);
                return;
            }
            showCrimesInNeighbourhood(neighbourhoodId,limit); 
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2A. called- browse local listings in a neighbourhood
    public void browseLocalListings(String neighbourhoodName, int limit) {

        String sql = """
                SELECT TOP (?) 
                       l.listing_id, l.name, l.price, l.property_type,
                       h.host_name, l.review_scores_value
                FROM listings l
                JOIN hosts h ON l.host_id = h.host_id
                JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
                WHERE n.neighbourhood_name = ?
                ORDER BY l.price DESC;
                """;
    
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            statement.setString(2, neighbourhoodName);
    
            try (ResultSet resultSet = statement.executeQuery()) {
    
                List<List<String>> table = new ArrayList<>();
    
                List<String> headers = Arrays.asList(
                        "Listing ID", "Name", "Price", "Property Type", "Host", "Rating"
                );
    
                while (resultSet.next()) {
                    table.add(Arrays.asList(
                            String.valueOf(resultSet.getInt("listing_id")),
                            resultSet.getString("name"),
                            String.valueOf(resultSet.getDouble("price")),
                            resultSet.getString("property_type"),
                            resultSet.getString("host_name"),
                            String.valueOf(resultSet.getDouble("review_scores_value"))
                    ));
                }
    
                System.out.println("\nListings in " + neighbourhoodName + ":\n");
    
                OutputFormatting of = new OutputFormatting();
                of.printTable(table, headers);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    

    // 2) called-Amenities for a specific listing (by ID)
    public void getListingAmenities(int listingId) {
        String sql = """
                SELECT a.amenity_name
                FROM amenities a
                JOIN listings_have_amenities la ON a.amenity_id = la.amenity_id
                WHERE la.listing_id = ?;
                """;
    
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, listingId);
    
            try (ResultSet resultSet = statement.executeQuery()) {
    
                List<String> rows = new ArrayList<>();
    
                // Header
                rows.add(String.format("%-30s", "Amenity Name"));
               
    
                // Data rows
                while (resultSet.next()) {
                    rows.add(String.format("%-30s",
                            resultSet.getString("amenity_name")));
                }
    
                System.out.println("\nAmenities for Listing ID " + listingId + ":\n");
    
                // Pagination (100 per page)
                OutputFormatting of = new OutputFormatting();
                //of.displayWithPagination(rows, 100);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 3) called- Rank listings by price inside each neighbourhood
    public void rankListingsByPrice(String neighbourhoodName, String order) {

        String sql;
        if (order.equalsIgnoreCase("high")) {
            sql = """
                SELECT l.listing_id, l.name, l.price, l.property_type, h.host_name
                FROM listings l
                JOIN hosts h ON l.host_id = h.host_id
                JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
                WHERE n.neighbourhood_name = ?
                ORDER BY l.price DESC
                """;
        } else if (order.equalsIgnoreCase("low")) {
            sql = """
                SELECT l.listing_id, l.name, l.price, l.property_type, h.host_name
                FROM listings l
                JOIN hosts h ON l.host_id = h.host_id
                JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
                WHERE n.neighbourhood_name = ?
                ORDER BY l.price ASC
                """;
        } else {
            System.out.println("Invalid order specified. Use 'high' or 'low'.");
            return;
        }
    
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, neighbourhoodName);
    
            try (ResultSet resultSet = statement.executeQuery()) {
    
                List<List<String>> table = new ArrayList<>();
    
                List<String> headers = Arrays.asList(
                        "ID", "Name", "Price", "Property Type", "Host"
                );
    
                while (resultSet.next()) {
                    table.add(Arrays.asList(
                            String.valueOf(resultSet.getInt("listing_id")),
                            resultSet.getString("name"),
                            String.valueOf(resultSet.getDouble("price")),
                            resultSet.getString("property_type"),
                            resultSet.getString("host_name")
                    ));
                }
    
                System.out.println("\nListings in " + neighbourhoodName +
                        " (Sorted by price " + order + "):\n");
    
                OutputFormatting of = new OutputFormatting();
                of.printTable(table, headers);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    
    // // 4) Listings with their host names + neighbourhoods
    // public void listingsWithHostsAndNeighbourhoods(int limit) {
    //     String sql = """
    //             SELECT TOP (?) 
    //                    l.listing_id, 
    //                    l.name AS listing_name, 
    //                    h.host_name, 
    //                    n.neighbourhood_name
    //             FROM listings l
    //             JOIN hosts h ON l.host_id = h.host_id
    //             JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
    //             ORDER BY n.neighbourhood_name, l.listing_id;
    //             """;
    
    //     try (PreparedStatement statement = connection.prepareStatement(sql)) {
    
    //         // Set the LIMIT correctly
    //         statement.setInt(1, limit);
    
    //         try (ResultSet resultSet = statement.executeQuery()) {
    
    //             List<String> rows = new ArrayList<>();
    
    //             // Header
    //             rows.add(String.format("%-12s %-50s %-50s %-35s",
    //                     "Listing ID", "Listing Name", "Host Name", "Neighbourhood"));
    //             rows.add("------------------------------------------------------------------------------------------------------------");
    
    //             // Data rows
    //             while (resultSet.next()) {
    //                 rows.add(String.format("%-12d %-50s %-50s %-35s",
    //                         resultSet.getInt("listing_id"),
    //                         resultSet.getString("listing_name"),
    //                         resultSet.getString("host_name"),
    //                         resultSet.getString("neighbourhood_name")));
    //             }
    
    //             System.out.println("\nListings with Hosts and Neighbourhoods:\n");
    
    //             // Pagination (100 rows per page)
    //             OutputFormatting of = new OutputFormatting();
    //             //of.displayWithPagination(rows, 100);
    //         }
    
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }
    

    // 5) called- Top N neighbourhoods with the most crimes 
    public void getHighCrimeNeighbourhoods(int limit) {
        String sql = """
                SELECT TOP (?) 
                       n.neighbourhood_name, 
                       COUNT(c.crime_id) AS total_crimes
                FROM neighbourhoods n
                LEFT JOIN crimes c ON n.neighbourhood_id = c.neighbourhood_id
                GROUP BY n.neighbourhood_id, n.neighbourhood_name
                ORDER BY total_crimes DESC;
                """;
    
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
    
            try (ResultSet resultSet = statement.executeQuery()) {
    
                List<List<String>> table = new ArrayList<>();
                List<String> headers = Arrays.asList("Neighbourhood", "Total Crimes");
    
                while (resultSet.next()) {
                    table.add(Arrays.asList(
                            resultSet.getString("neighbourhood_name"),
                            String.valueOf(resultSet.getInt("total_crimes"))
                    ));
                }
    
                System.out.println("\nTop " + limit + " High-Crime Neighbourhoods:\n");
    
                OutputFormatting of = new OutputFormatting();
                of.printTable(table, headers);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    

    // 6) called-  Number of listings per neighbourhood 
    public void countListingsPerNeighbourhood() {
        String sql = """
                SELECT n.neighbourhood_name, COUNT(l.listing_id) AS listing_count
                FROM neighbourhoods n
                LEFT JOIN listings l ON n.neighbourhood_id = l.neighbourhood_id
                GROUP BY n.neighbourhood_id, n.neighbourhood_name
                ORDER BY listing_count DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList("Neighbourhood", "Listing Count");
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        resultSet.getString("neighbourhood_name"),
                        String.valueOf(resultSet.getInt("listing_count"))
                ));
            }
    
            System.out.println("Number of Listings per Neighbourhood (Rental Density):");
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    

    // 7)called-  Crimes handled per police station 
    public void getPoliceStationCoverage() {
        String sql = """
                SELECT ps.police_station_name,
                       COUNT(c.crime_id) AS crime_count
                FROM police_stations ps
                LEFT JOIN neighbourhoods n ON ps.neighbourhood_id = n.neighbourhood_id
                LEFT JOIN crimes c ON n.neighbourhood_id = c.neighbourhood_id
                GROUP BY ps.police_station_id, ps.police_station_name
                ORDER BY crime_count DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList("Police Station", "Crime Count");
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        resultSet.getString("police_station_name"),
                        String.valueOf(resultSet.getInt("crime_count"))
                ));
            }
    
            System.out.println("Police Station Coverage (Crimes Handled per Station):");
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    


    // 8)called- Guests who stayed AND visited attractions 
    public void trackGuestAttractionActivity(int limit) {
        String sql = """
                SELECT DISTINCT TOP (?) 
                       g.guest_id, 
                       g.guest_name
                FROM guests g
                JOIN guest_book_listings gb ON g.guest_id = gb.guest_id
                WHERE EXISTS (
                    SELECT 1 
                    FROM guest_visit_attractions ga
                    WHERE ga.guest_id = g.guest_id
                );
                """;
    
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
    
            try (ResultSet resultSet = statement.executeQuery()) {
    
                List<List<String>> table = new ArrayList<>();
                List<String> headers = Arrays.asList("Guest ID", "Guest Name");
    
                while (resultSet.next()) {
                    table.add(Arrays.asList(
                            String.valueOf(resultSet.getInt("guest_id")),
                            resultSet.getString("guest_name")
                    ));
                }
    
                OutputFormatting of = new OutputFormatting();
                of.printTable(table, headers);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    


    // 9) called- Crime rate vs number of listings + avg price per neighbourhood 
    public void analyzeCrimeVsTourism() {
        String sql = """
                SELECT n.neighbourhood_name,
                       COUNT(DISTINCT c.crime_id) AS crime_count,
                       COUNT(DISTINCT l.listing_id) AS listing_count,
                       ROUND(AVG(l.price), 2) AS avg_listing_price
                FROM neighbourhoods n
                LEFT JOIN crimes c ON n.neighbourhood_id = c.neighbourhood_id
                LEFT JOIN listings l ON n.neighbourhood_id = l.neighbourhood_id
                GROUP BY n.neighbourhood_id, n.neighbourhood_name
                ORDER BY crime_count DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList(
                    "Neighbourhood", "Crimes", "Listings", "Avg Price"
            );
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        resultSet.getString("neighbourhood_name"),
                        String.valueOf(resultSet.getInt("crime_count")),
                        String.valueOf(resultSet.getInt("listing_count")),
                        String.valueOf(resultSet.getDouble("avg_listing_price"))
                ));
            }
    
            System.out.println("Crime Rate vs Listings Analysis:");
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

// used- Helper function to display all attractions
public void displayAllAttractions() {
    String sql = """
            SELECT DISTINCT a.attraction_id, a.attraction_name, n.neighbourhood_name
            FROM attractions a
            JOIN neighbourhoods n ON a.neighbourhood_id = n.neighbourhood_id
            ORDER BY a.attraction_name;
            """;
    
    System.out.println("\n=== ALL ATTRACTIONS IN TORONTO ===");

    try (PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {

        // Prepare data for OutputFormatting
        List<List<String>> table = new ArrayList<>();
        List<String> headers = Arrays.asList("ID", "Attraction Name", "Neighbourhood");

        int attractionCount = 0;

        while (resultSet.next()) {
            table.add(Arrays.asList(
                    String.valueOf(resultSet.getInt("attraction_id")),
                    resultSet.getString("attraction_name"),
                    resultSet.getString("neighbourhood_name")
            ));
            attractionCount++;
        }

        // Use OutputFormatting to print the table
        OutputFormatting of = new OutputFormatting();
        of.printTable(table, headers);

        System.out.println("\nTotal attractions: " + attractionCount);

    } catch (SQLException e) {
        e.printStackTrace();
    }
}


// SQL injection detection function
private boolean isSqlInjectionSafe(String input) {
    if (input == null || input.trim().isEmpty()) {
        return true; // Empty input is technically safe
    }
    
    // Common SQL injection patterns to check for
    String[] dangerousPatterns = {
        ";",        // Query termination
        "--",       // SQL comment
        "/*",       // Start of block comment
        "*/",       // End of block comment
        "\"",       // String delimiter
        "\\",       // Escape character
        "union",    // UNION attack
        "select",   // SELECT statement
        "insert",   // INSERT statement
        "update",   // UPDATE statement  
        "delete",   // DELETE statement
        "drop",     // DROP statement
        "create",   // CREATE statement
        "alter",    // ALTER statement
        "exec",     // EXEC command
        "execute",  // EXECUTE command
        "xp_",      // Extended stored procedures
        "sp_",      // System stored procedures
        "1=1",      // Always true condition
        "1=0",      // Always false condition
        " or ",     // OR injection
        " and ",    // AND injection
        " OR ",     // OR injection (uppercase)
        " AND ",    // AND injection (uppercase)
        "||",       // Concatenation operator
        "&&",       // AND operator
        "@@",       // System variable
        "@",        // Variable prefix
        "char(",    // CHAR function
        "waitfor",  // WAITFOR command
        "delay",    // DELAY command
        "shutdown", // SHUTDOWN command
        "sleep(",   // SLEEP function
        "benchmark" // BENCHMARK function
    };
    
    String lowerInput = input.toLowerCase();
    
    for (String pattern : dangerousPatterns) {
        if (lowerInput.contains(pattern.toLowerCase())) {
            System.out.println(RED+ BOLD + "Warning: Potential SQL injection detected! Pattern: " + pattern + RESET);
            return false;
        }
    }
    
    // Check for stacked queries
    if (input.split(";").length > 2) { // More than one semicolon
        System.out.println(RED+ BOLD +"Warning: Potential stacked query detected!"+ RESET);
        return false;
    }
    
    // Check for comment injection
    if (lowerInput.contains("--") || lowerInput.contains("/*")) {
        System.out.println(RED+ BOLD +"Warning: Potential comment injection detected!"+ RESET);
        return false;
    }
    
    return true;
}

// Updated checkAttractionSafety that shows all attractions first
public void checkAttractionSafety(String attractionName) {
    if (!isSqlInjectionSafe(attractionName)) {
        System.out.println(BOLD + RED + "Input rejected due to security concerns. Please enter a valid attraction name." + RESET);
        return;
    }

    System.out.println("\n" + "=".repeat(60));
    System.out.println("Now searching for: " + attractionName);
    System.out.println("=".repeat(60));

    String sql = """
            SELECT a.attraction_name, n.neighbourhood_name,
                   COUNT(c.crime_id) AS nearby_crimes
            FROM attractions a
            JOIN neighbourhoods n ON a.neighbourhood_id = n.neighbourhood_id
            LEFT JOIN crimes c ON n.neighbourhood_id = c.neighbourhood_id
            WHERE a.attraction_name LIKE ?
            GROUP BY a.attraction_id, a.attraction_name, n.neighbourhood_name;
            """;

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setString(1, "%" + attractionName + "%");

        try (ResultSet resultSet = statement.executeQuery()) {

            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList("Attraction", "Neighbourhood", "Nearby Crimes");

            boolean found = false;

            while (resultSet.next()) {
                found = true;
                table.add(Arrays.asList(
                        resultSet.getString("attraction_name"),
                        resultSet.getString("neighbourhood_name"),
                        String.valueOf(resultSet.getInt("nearby_crimes"))
                ));
            }

            System.out.println("\n=== ATTRACTION SAFETY CHECK RESULTS ===");

            if (!found) {
                System.out.println("No attractions found matching: " + attractionName);
                System.out.println("Try using a partial name or check the ID from the list above.");
                return;
            }

            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);

        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}





// // Additional helper: Show attractions with crime counts
// public void showAttractionsWithSafetySummary() {
//     String sql = """
//             SELECT a.attraction_id, a.attraction_name, n.neighbourhood_name,
//                    COUNT(c.crime_id) AS nearby_crimes
//             FROM attractions a
//             JOIN neighbourhoods n ON a.neighbourhood_id = n.neighbourhood_id
//             LEFT JOIN crimes c ON n.neighbourhood_id = c.neighbourhood_id
//             GROUP BY a.attraction_id, a.attraction_name, n.neighbourhood_name
//             ORDER BY a.attraction_name;
//             """;
    
//     System.out.println("\n=== ATTRACTIONS WITH SAFETY SUMMARY ===");
//     System.out.printf("%-5s %-40s %-25s %-15s%n", 
//         "ID", "Attraction Name", "Neighbourhood", "Nearby Crimes");
//     System.out.println("-".repeat(95));
    
//     try (PreparedStatement statement = connection.prepareStatement(sql);
//          ResultSet resultSet = statement.executeQuery()) {
        
//         int attractionCount = 0;
//         while (resultSet.next()) {
//             System.out.printf("%-5d %-40s %-25s %-15d%n",
//                 resultSet.getInt("attraction_id"),
//                 resultSet.getString("attraction_name"),
//                 resultSet.getString("neighbourhood_name"),
//                 resultSet.getInt("nearby_crimes"));
//             attractionCount++;
//         }
        
//         System.out.println("\nTotal attractions: " + attractionCount);
//         System.out.println("\nUse checkAttractionSafety() for detailed safety information.");
        
//     } catch (SQLException e) {
//         e.printStackTrace();
//     }
// }

    // 11)called- Attraction count vs average listing price 
    public void analyzeAttractionImpactOnPrices() {
        String sql = """
                SELECT n.neighbourhood_id,
                       n.neighbourhood_name,
                       COUNT(DISTINCT a.attraction_id) AS num_attractions,
                       AVG(l.price) AS average_price
                FROM neighbourhoods n
                LEFT JOIN attractions a ON n.neighbourhood_id = a.neighbourhood_id
                LEFT JOIN listings l ON n.neighbourhood_id = l.neighbourhood_id
                GROUP BY n.neighbourhood_id, n.neighbourhood_name
                ORDER BY num_attractions DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList("NID", "Neighbourhood", "Attractions", "Avg Price");
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        String.valueOf(resultSet.getInt("neighbourhood_id")),
                        resultSet.getString("neighbourhood_name"),
                        String.valueOf(resultSet.getInt("num_attractions")),
                        String.format("%.2f", resultSet.getDouble("average_price"))
                ));
            }
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    

    // 12)called - Attractions, crimes, police stations per neighbourhood 
    public void generateAreaSafetyProfiles() {
        String sql = """
                SELECT n.neighbourhood_id,
                       n.neighbourhood_name,
                       COUNT(DISTINCT a.attraction_id) AS num_attractions,
                       COUNT(DISTINCT c.crime_id) AS num_crimes,
                       COUNT(DISTINCT ps.police_station_id) AS num_stations
                FROM neighbourhoods n
                LEFT JOIN attractions a ON n.neighbourhood_id = a.neighbourhood_id
                LEFT JOIN crimes c ON n.neighbourhood_id = c.neighbourhood_id
                LEFT JOIN police_stations ps ON n.neighbourhood_id = ps.neighbourhood_id
                GROUP BY n.neighbourhood_id, n.neighbourhood_name;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList(
                    "NID", "Neighbourhood", "Attractions", "Crimes", "Police Stations"
            );
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        String.valueOf(resultSet.getInt("neighbourhood_id")),
                        resultSet.getString("neighbourhood_name"),
                        String.valueOf(resultSet.getInt("num_attractions")),
                        String.valueOf(resultSet.getInt("num_crimes")),
                        String.valueOf(resultSet.getInt("num_stations"))
                ));
            }
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // 14) called- Amenities vs bookings per listing
    public void analyzeAmenityPopularity() {
        String sql = """
                SELECT TOP 150 l.listing_id,
                       l.name,
                       COUNT(DISTINCT la.amenity_id) AS num_amenities,
                       COUNT(DISTINCT gb.booking_date) AS num_bookings
                FROM listings l
                LEFT JOIN listings_have_amenities la ON l.listing_id = la.listing_id
                LEFT JOIN guest_book_listings gb ON l.listing_id = gb.listing_id
                GROUP BY l.listing_id, l.name
                ORDER BY num_bookings DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList("Listing ID", "Name", "Amenities", "Bookings");
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        String.valueOf(resultSet.getInt("listing_id")),
                        resultSet.getString("name"),
                        String.valueOf(resultSet.getInt("num_amenities")),
                        String.valueOf(resultSet.getInt("num_bookings"))
                ));
            }
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // 15) called- Amenities vs price 
    public void analyzeAmenityPricing() {
        String sql = """
                SELECT TOP 150 l.listing_id,
                       l.name,
                       l.price,
                       COUNT(la.amenity_id) AS num_amenities
                FROM listings l
                LEFT JOIN listings_have_amenities la ON l.listing_id = la.listing_id
                GROUP BY l.listing_id, l.name, l.price
                ORDER BY l.price DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList("Listing ID", "Name", "Price", "Amenities");
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        String.valueOf(resultSet.getInt("listing_id")),
                        resultSet.getString("name"),
                        String.valueOf(resultSet.getDouble("price")),
                        String.valueOf(resultSet.getInt("num_amenities"))
                ));
            }
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // 16) called- Hosts who also appear as guests
    public void findHostGuestOverlap() {
        String sql = """
                SELECT TOP 150 h.host_id,
                       h.host_name,
                       COUNT(gb.listing_id) AS num_guest_bookings
                FROM hosts h
                LEFT JOIN guest_book_listings gb ON h.host_id = gb.guest_id
                GROUP BY h.host_id, h.host_name
                ORDER BY num_guest_bookings DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList("Host ID", "Host Name", "Guest Bookings");
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        String.valueOf(resultSet.getInt("host_id")),
                        resultSet.getString("host_name"),
                        String.valueOf(resultSet.getInt("num_guest_bookings"))
                ));
            }
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // 17) called- Reviews vs bookings
    public void analyzeReviewImpact() {
        String sql = """
                SELECT TOP 150 l.listing_id,
                       l.name,
                       l.review_scores_value,
                       COUNT(r.review_id) AS num_reviews,
                       COUNT(DISTINCT gb.booking_date) AS num_bookings
                FROM listings l
                LEFT JOIN reviews r ON l.listing_id = r.listing_id
                LEFT JOIN guest_book_listings gb ON l.listing_id = gb.listing_id
                GROUP BY l.listing_id, l.name, l.review_scores_value
                ORDER BY l.review_scores_value DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList(
                    "Listing ID", "Name", "Review Score", "Num Reviews", "Num Bookings"
            );
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        String.valueOf(resultSet.getInt("listing_id")),
                        resultSet.getString("name"),
                        String.valueOf(resultSet.getDouble("review_scores_value")),
                        String.valueOf(resultSet.getInt("num_reviews")),
                        String.valueOf(resultSet.getInt("num_bookings"))
                ));
            }
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    


    // 18) called- Listings never booked
    public void unbookedListings() {
        String sql = """
                SELECT TOP 150 l.listing_id, l.name,
                       h.host_name,
                       h.host_identity_verified,
                       h.host_since
                FROM listings l
                JOIN hosts h ON l.host_id = h.host_id
                WHERE NOT EXISTS (
                    SELECT 1 FROM guest_book_listings gb 
                    WHERE gb.listing_id = l.listing_id
                );
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList(
                    "Listing ID", "Name", "Host Name", "Verified", "Host Since"
            );
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        String.valueOf(resultSet.getInt("listing_id")),
                        resultSet.getString("name"),
                        resultSet.getString("host_name"),
                        resultSet.getString("host_identity_verified"),
                        String.valueOf(resultSet.getDate("host_since"))
                ));
            }
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // 19) called- Criminals active in multiple neighbourhoods 
    public void trackRepeatOffenders() {
        String sql = """
                SELECT TOP 50 cr.criminal_id,
                       cr.criminal_name,
                       COUNT(DISTINCT c.neighbourhood_id) AS neighbourhood_count,
                       COUNT(DISTINCT c.crime_id) AS total_crimes
                FROM criminals cr
                JOIN crimes c ON cr.criminal_id = c.criminal_id
                GROUP BY cr.criminal_id, cr.criminal_name
                HAVING COUNT(DISTINCT c.neighbourhood_id) > 1
                ORDER BY neighbourhood_count DESC, total_crimes DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList(
                    "Criminal ID", "Name", "Neighbourhoods", "Total Crimes"
            );
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        String.valueOf(resultSet.getInt("criminal_id")),
                        resultSet.getString("criminal_name"),
                        String.valueOf(resultSet.getInt("neighbourhood_count")),
                        String.valueOf(resultSet.getInt("total_crimes"))
                ));
            }
    
            System.out.println("Repeat Offenders Active in Multiple Neighbourhoods:");
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // 20)called- Most visited attractions by guests 
    public void findMostVisitedAttractions() {
        String sql = """
                SELECT TOP 10
                    a.attraction_name,
                    n.neighbourhood_name,
                    COUNT(gva.guest_id) AS total_visits
                FROM attractions a
                JOIN neighbourhoods n 
                    ON a.neighbourhood_id = n.neighbourhood_id
                LEFT JOIN guest_visit_attractions gva 
                    ON a.attraction_id = gva.attraction_id
                GROUP BY a.attraction_id, a.attraction_name, n.neighbourhood_name
                ORDER BY total_visits DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList(
                    "Attraction", "Neighbourhood", "Total Visits"
            );
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        resultSet.getString("attraction_name"),
                        resultSet.getString("neighbourhood_name"),
                        String.valueOf(resultSet.getInt("total_visits"))
                ));
            }
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    


    // 21) called- Criminal category analysis
    public void criminalCategoryAnalysis() {
        String sql = """
                SELECT 
                    cr.gender,
                    cr.age_group,
                    COUNT(c.crime_id) AS total_crimes
                FROM criminals cr
                JOIN crimes c 
                    ON cr.criminal_id = c.criminal_id
                GROUP BY cr.gender, cr.age_group
                ORDER BY total_crimes DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList("Gender", "Age Group", "Total Crimes");
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        resultSet.getString("gender"),
                        resultSet.getString("age_group"),
                        String.valueOf(resultSet.getInt("total_crimes"))
                ));
            }
    
            System.out.println("Criminal Category Analysis:");
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // 22) called- Most active criminals near attractions
    public void criminalsNearAttractions() {
        String sql = """
                SELECT TOP 20
                    cr.criminal_name,
                    cr.age_group,
                    n.neighbourhood_name,
                    COUNT(c.crime_id) AS crimes_near_attractions
                FROM crimes c
                JOIN criminals cr ON c.criminal_id = cr.criminal_id
                JOIN neighbourhoods n ON c.neighbourhood_id = n.neighbourhood_id
                WHERE c.neighbourhood_id IN (
                    SELECT DISTINCT neighbourhood_id FROM attractions
                )
                GROUP BY cr.criminal_id, cr.criminal_name, cr.age_group, n.neighbourhood_name
                ORDER BY crimes_near_attractions DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList(
                    "Criminal Name", "Age Group", "Neighbourhood", "Crimes Near Attractions"
            );
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        resultSet.getString("criminal_name"),
                        resultSet.getString("age_group"),
                        resultSet.getString("neighbourhood_name"),
                        String.valueOf(resultSet.getInt("crimes_near_attractions"))
                ));
            }
    
            System.out.println("Most Active Criminals Near Attractions:");
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // 23) called- Busiest booking month
    public void busiestBookingMonth() {
        String sql = """
                SELECT DISTINCT TOP 3
                    MONTH(booking_date) AS month,
                    COUNT(*) AS total_bookings
                FROM guest_book_listings
                GROUP BY MONTH(booking_date)
                ORDER BY total_bookings DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList("Month", "Total Bookings");
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        monthMapper(resultSet.getInt("month")),
                        String.valueOf(resultSet.getInt("total_bookings"))
                ));
            }
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    //used- query 23 helper method
    public String monthMapper(int month) {

        switch (month) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            default:
                return "Invalid month";
        }
    }
    
    // called- 24. Most expensive property type in each neighbourhood
    public void propertyTypePricesByNeighbourhood() {
        String sql = """
                SELECT DISTINCT TOP 10
                    l.property_type,
                    AVG(l.price) AS avg_price
                FROM listings l
                JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
                GROUP BY l.property_type
                ORDER BY avg_price DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList("Property Type", "Avg Price");
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        resultSet.getString("property_type"),
                        String.valueOf(resultSet.getDouble("avg_price"))
                ));
            }
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    // 25) called- Most booked property type
    public void mostBookedPropertyTypes() {
        String sql = """
                SELECT 
                    l.property_type,
                    COUNT(*) AS total_bookings
                FROM guest_book_listings gbl
                JOIN listings l 
                    ON gbl.listing_id = l.listing_id
                GROUP BY l.property_type
                ORDER BY total_bookings DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
    
                List<List<String>> table = new ArrayList<>();
                List<String> headers = Arrays.asList("Property Type", "Total Bookings");
    
                while (resultSet.next()) {
                    table.add(Arrays.asList(
                            resultSet.getString("property_type"),
                            String.valueOf(resultSet.getInt("total_bookings"))
                    ));
                }
    
                OutputFormatting of = new OutputFormatting();
                of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // 26) called- Cheapest vs most expensive listing type
    public void priceRangeByPropertyType() {
        String sql = """
                SELECT 
                    property_type,
                    MIN(price) AS min_price,
                    MAX(price) AS max_price
                FROM listings
                GROUP BY property_type
                ORDER BY max_price DESC;
                """;
    
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
    
            List<List<String>> table = new ArrayList<>();
            List<String> headers = Arrays.asList("Property Type", "Min Price", "Max Price");
    
            while (resultSet.next()) {
                table.add(Arrays.asList(
                        resultSet.getString("property_type"),
                        String.valueOf(resultSet.getDouble("min_price")),
                        String.valueOf(resultSet.getDouble("max_price"))
                ));
            }
    
            OutputFormatting of = new OutputFormatting();
            of.printTable(table, headers);
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    

    // // 27) Search comments by neighbourhood ID
    // public void commentsByNeighbourhood(int neighbourhoodId) {
    //     String sql = """
    //             SELECT 
    //                 n.neighbourhood_name,
    //                 l.name AS listing_name,
    //                 r.comments
    //             FROM reviews r
    //             JOIN listings l ON r.listing_id = l.listing_id
    //             JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
    //             WHERE n.neighbourhood_id = ?;
    //             """;

    //     try (PreparedStatement statement = connection.prepareStatement(sql)) {
    //         statement.setInt(1, neighbourhoodId);
            
    //         try (ResultSet resultSet = statement.executeQuery()) {
    //             System.out.println("Comments for Neighbourhood ID " + neighbourhoodId + ":");
    //             System.out.printf("%-25s %-30s %-50s%n", 
    //                 "Neighbourhood", "Listing", "Comments");
    //             System.out.println("--------------------------------------------------------------------------------------");
                
    //             while (resultSet.next()) {
    //                 System.out.printf("%-25s %-30s %-50s%n",
    //                     resultSet.getString("neighbourhood_name"),
    //                     resultSet.getString("listing_name"),
    //                     resultSet.getString("comments"));
    //             }
    //         }
    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //     }
    // }

    
    //27- called 
    public void topRatedListings(String neighbourhoodName) {
        String sql = """
                SELECT TOP 10
                    l.listing_id,
                    l.name,
                    l.review_scores_value,
                    COUNT(r.review_id) AS review_count,
                    h.host_name
                FROM listings l
                JOIN hosts h ON l.host_id = h.host_id
                JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
                LEFT JOIN reviews r ON l.listing_id = r.listing_id
                WHERE n.neighbourhood_name = ?
                AND l.review_scores_value IS NOT NULL
                GROUP BY l.listing_id, l.name, l.review_scores_value, h.host_name
                ORDER BY l.review_scores_value DESC, COUNT(r.review_id) DESC;
                """;
    
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, neighbourhoodName);
    
            try (ResultSet resultSet = statement.executeQuery()) {
    
                List<List<String>> table = new ArrayList<>();
                List<String> headers = Arrays.asList("Listing ID", "Name", "Rating", "Reviews", "Host");
    
                while (resultSet.next()) {
                    table.add(Arrays.asList(
                            String.valueOf(resultSet.getInt("listing_id")),
                            resultSet.getString("name"),
                            String.valueOf(resultSet.getDouble("review_scores_value")),
                            String.valueOf(resultSet.getInt("review_count")),
                            resultSet.getString("host_name")
                    ));
                }
    
                if (table.isEmpty()) {
                    System.out.println("No listings with reviews found in " + neighbourhoodName);
                    return;
                }
    
                System.out.println("\nTop Rated Listings in " + neighbourhoodName + ":\n");
    
                OutputFormatting of = new OutputFormatting();
                of.printTable(table, headers);
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
//    // Search comments by neighbourhood name
//     public void commentsByNeighbourhood(String neighbourhoodName) {
//         try {
//             int neighbourhoodId = getNeighbourhoodIdByName(neighbourhoodName);
//             if (neighbourhoodId == -1) {
//                 System.out.println("Neighbourhood not found: " + neighbourhoodName);
//                 return;
//             }
//             commentsByNeighbourhood(neighbourhoodId); // Call the original method
//         } catch (SQLException e) {
//             e.printStackTrace();
//         }
//     }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}