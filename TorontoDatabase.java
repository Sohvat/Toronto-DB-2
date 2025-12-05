import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class TorontoDatabase {
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

    // helper method to get neighbourhood ID by neighbourhood name
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

    // 1) Crimes in a specific neighbourhood (by ID) 
    public void showCrimesInNeighbourhood(int neighbourhoodId) {
        String sql = """
                SELECT TOP 100 c.*
                FROM crimes c
                JOIN neighbourhoods n ON c.neighbourhood_id = n.neighbourhood_id
                WHERE n.neighbourhood_id = ?;
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, neighbourhoodId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                System.out.println("Crimes in Neighbourhood ID " + neighbourhoodId + ":");
                System.out.printf("%-10s %-50s %-30s %-30s%n", 
                    "Crime ID", "Crime Type", "Criminal ID", "Neighbourhood ID");
                System.out.println("--------------------------------------------------------------------------------");
                
                while (resultSet.next()) {
                    System.out.printf("%-10d %-50s %-30s %-30s%n",
                        resultSet.getInt("crime_id"),
                        resultSet.getString("crime_type"),                        
                        resultSet.getInt("criminal_id"),
                        resultSet.getInt("neighbourhood_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Crimes in a specific neighbourhood (by name), then get the id and then run the actual query
    public void showCrimesInNeighbourhood(String neighbourhoodName) {
        try {
            int neighbourhoodId = getNeighbourhoodIdByName(neighbourhoodName);
            if (neighbourhoodId == -1) {
                System.out.println("Neighbourhood not found: " + neighbourhoodName);
                return;
            }
            showCrimesInNeighbourhood(neighbourhoodId); // Call the original method
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // browse local listings in a neighbourhood
    public void browseLocalListings(String neighbourhoodName) {
        String sql = """
                SELECT l.listing_id, l.name, l.price, l.property_type, 
                       h.host_name, l.review_scores_value
                FROM listings l
                JOIN hosts h ON l.host_id = h.host_id
                JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
                WHERE n.neighbourhood_name = ?
                ORDER BY l.price DESC;
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, neighbourhoodName);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                System.out.println("\nListings in " + neighbourhoodName + ":");
                System.out.printf("%-15s %-60s %-30s %-25s %-30s %-15s%n", 
                    "Listing ID", "Name", "Price", "Property Type", "Host", "Rating");
                System.out.println("-".repeat(120));
                
                int count = 0;
                while (resultSet.next()) {
                    System.out.printf("%-15d %-60s %-30s %-25s %-30s %-15s",
                        resultSet.getInt("listing_id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price"),
                        resultSet.getString("property_type"),
                        resultSet.getString("host_name"),
                        resultSet.getDouble("review_scores_value"));
                    count++;
                }
                System.out.println("\nTotal listings: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 2) Amenities for a specific listing (by ID)
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
                System.out.println("Amenities for Listing ID " + listingId + ":");
                System.out.printf("%-30s%n", "Amenity Name");
                System.out.println("------------------------------");
                
                while (resultSet.next()) {
                    System.out.printf("%-30s%n", resultSet.getString("amenity_name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // 3) Rank listings by price inside each neighbourhood - Renamed to match DatabaseInterface
    public void rankListingsByPrice() {
        String sql = """
                SELECT l.listing_id, l.name, l.price, n.neighbourhood_name
                FROM listings l
                JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
                ORDER BY n.neighbourhood_name, l.price DESC;
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Listings Ranked by Price in Each Neighbourhood:");
            System.out.printf("%-15s %-120s %-70s %-40s%n", 
                "Listing ID", "Name", "Price", "Neighbourhood");
            System.out.println("----------------------------------------------------------------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-15d %-120s %-70s %-50s%n",
                    resultSet.getInt("listing_id"),
                    resultSet.getString("name"),
                    resultSet.getDouble("price"),
                    resultSet.getString("neighbourhood_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // NEW METHOD: For DatabaseInterface neighbourhood category option 3
    public void rankListingsByPrice(String neighbourhoodName, String order) {
        
        String sql;
        if (order.equalsIgnoreCase("high")) {
        sql = """
            SELECT l.listing_id, l.name, l.price, l.property_type, h.host_name
            FROM listings l
            JOIN hosts h ON l.host_id = h.host_id
            JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
            WHERE n.neighbourhood_name = ?
            ORDER BY l.price DESC""";
    } else if (order.equalsIgnoreCase("low")) {
        sql = """
            SELECT l.listing_id, l.name, l.price, l.property_type, h.host_name
            FROM listings l
            JOIN hosts h ON l.host_id = h.host_id
            JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
            WHERE n.neighbourhood_name = ?
            ORDER BY l.price ASC""";
    }
    else {
        System.out.println("Invalid order specified. Use 'high' or 'low'.");
        return;
    }

    try (PreparedStatement statement = connection.prepareStatement(sql)) {
        statement.setString(1, neighbourhoodName);
            statement.setString(1, neighbourhoodName);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                System.out.println("\nListings in " + neighbourhoodName + " (Sorted by price " + order + "):");
                System.out.printf("%-10s  %-50s $%-20s %-25s %-30s%n", 
                    "Listing ID", "Name", "Price", "Property Type", "Host");
                System.out.println("-".repeat(110));
                
                int count = 0;
                while (resultSet.next()) {
                    System.out.printf("%-10d %-50s $%-20s %-25s %-30s%n",
                        resultSet.getInt("listing_id"),
                        resultSet.getString("name"),
                        resultSet.getDouble("price"),
                        resultSet.getString("property_type"),
                        resultSet.getString("host_name"));
                    count++;
                }
                System.out.println("\nTotal listings: " + count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 4) Listings with their host names + neighbourhoods
    public void listingsWithHostsAndNeighbourhoods() {
        String sql = """
                SELECT l.listing_id, l.name AS listing_name, h.host_name, n.neighbourhood_name
                FROM listings l
                JOIN hosts h ON l.host_id = h.host_id
                JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
                ORDER BY n.neighbourhood_name, l.listing_id;
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Listings with Hosts and Neighbourhoods:");
            System.out.printf("%-12s %-50s %-50s %-35s%n", 
                "Listing ID", "Listing Name", "Host Name", "Neighbourhood");
            System.out.println("-----------------------------------------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-12d %-50s %-50s %-35s%n",
                    resultSet.getInt("listing_id"),
                    resultSet.getString("listing_name"),
                    resultSet.getString("host_name"),
                    resultSet.getString("neighbourhood_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 5) Top N neighbourhoods with the most crimes - Renamed to match DatabaseInterface
    public void getHighCrimeNeighbourhoods(int limit) {
        String sql = """
                SELECT TOP (?) n.neighbourhood_name, COUNT(c.crime_id) AS total_crimes
                FROM neighbourhoods n
                LEFT JOIN crimes c ON n.neighbourhood_id = c.neighbourhood_id
                GROUP BY n.neighbourhood_id, n.neighbourhood_name
                ORDER BY total_crimes DESC;
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                System.out.println("Top " + limit + " High-Crime Neighbourhoods:");
                System.out.printf("%-30s %-30s%n", "Neighbourhood", "Total Crimes");
                System.out.println("--------------------------------------------");
                
                while (resultSet.next()) {
                    System.out.printf("%-30s %-30d%n",
                        resultSet.getString("neighbourhood_name"),
                        resultSet.getInt("total_crimes"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Keep original method name for backward compatibility
    public void topNeighbourhoodsByCrimes(int limit) {
        getHighCrimeNeighbourhoods(limit);
    }

    // 6) Number of listings per neighbourhood - Renamed to match DatabaseInterface
    public void countListingsPerNeighbourhood() {
        String sql = """
                SELECT n.neighbourhood_name, COUNT(l.listing_id) AS listing_count
                FROM neighbourhoods n
                LEFT JOIN listings l ON n.neighbourhood_id = l.neighbourhood_id
                GROUP BY n.neighbourhood_id, n.neighbourhood_name
                ORDER BY listing_count DESC;
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Number of Listings per Neighbourhood (Rental Density):");
            System.out.printf("%-30s %-15s%n", "Neighbourhood", "Listing Count");
            System.out.println("--------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-30s %-15d%n",
                    resultSet.getString("neighbourhood_name"),
                    resultSet.getInt("listing_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Keep original method name for backward compatibility
    public void listingsPerNeighbourhood() {
        countListingsPerNeighbourhood();
    }

    // 7) Crimes handled per police station - Renamed to match DatabaseInterface
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Police Station Coverage (Crimes Handled per Station):");
            System.out.printf("%-40s %-15s%n", "Police Station", "Crime Count");
            System.out.println("---------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-40s %-15d%n",
                    resultSet.getString("police_station_name"),
                    resultSet.getInt("crime_count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // 8) Guests who stayed AND visited attractions 
    public void trackGuestAttractionActivity() {
        String sql = """
                SELECT DISTINCT TOP 200 g.guest_id, g.guest_name
                FROM guests g
                JOIN guest_book_listings gb ON g.guest_id = gb.guest_id
                WHERE EXISTS (
                    SELECT 1 
                    FROM guest_visit_attractions ga
                    WHERE ga.guest_id = g.guest_id
                );
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Guests with Both Airbnb Stays and Attraction Visits:");
            System.out.printf("%-10s %-30s%n", "Guest ID", "Guest Name");
            System.out.println("----------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-10d %-30s%n",
                    resultSet.getInt("guest_id"),
                    resultSet.getString("guest_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // 9) Crime rate vs number of listings + avg price per neighbourhood - Renamed to match DatabaseInterface
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Crime Rate vs Listings Analysis:");
            System.out.printf("%-60s %-40s %20s %-15s%n", 
                "Neighbourhood", "Crimes", "Listings", "Avg Price");
            System.out.println("-------------------------------------------------------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-60s %-40s %20s %-15s%n",
                    resultSet.getString("neighbourhood_name"),
                    resultSet.getInt("crime_count"),
                    resultSet.getInt("listing_count"),
                    resultSet.getDouble("avg_listing_price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

// Helper function to display all attractions
public void displayAllAttractions() {
    String sql = """
            SELECT DISTINCT a.attraction_id, a.attraction_name, n.neighbourhood_name
            FROM attractions a
            JOIN neighbourhoods n ON a.neighbourhood_id = n.neighbourhood_id
            ORDER BY a.attraction_name;
            """;
    
    System.out.println("\n=== ALL ATTRACTIONS IN TORONTO ===");
    System.out.printf("%-5s %-50s %-30s%n", "ID", "Attraction Name", "Neighbourhood");
    System.out.println("-".repeat(90));
    
    try (PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {
        
        int attractionCount = 0;
        while (resultSet.next()) {
            System.out.printf("%-5d %-50s %-30s%n",
                resultSet.getInt("attraction_id"),
                resultSet.getString("attraction_name"),
                resultSet.getString("neighbourhood_name"));
            attractionCount++;
        }
        
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
            System.out.println("Warning: Potential SQL injection detected! Pattern: " + pattern);
            return false;
        }
    }
    
    // Check for stacked queries
    if (input.split(";").length > 2) { // More than one semicolon
        System.out.println("Warning: Potential stacked query detected!");
        return false;
    }
    
    // Check for comment injection
    if (lowerInput.contains("--") || lowerInput.contains("/*")) {
        System.out.println("Warning: Potential comment injection detected!");
        return false;
    }
    
    return true;
}

// Updated checkAttractionSafety that shows all attractions first
public void checkAttractionSafety(String attractionName) {
    // First display all attractions
    if (!isSqlInjectionSafe(attractionName)) {
        System.out.println("Input rejected due to security concerns. Please enter a valid attraction name.");
        return;
    }
    
    System.out.println("\n" + "=".repeat(60));
    System.out.println("Now searching for: " + attractionName);
    System.out.println("=".repeat(60));
    
    // Then perform the original search
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
            System.out.println("\n=== ATTRACTION SAFETY CHECK RESULTS ===");
            System.out.printf("%-40s %-25s %-15s%n", 
                "Attraction", "Neighbourhood", "Nearby Crimes");
            System.out.println("-------------------------------------------------------------------------------------------------------");
            
            boolean found = false;
            while (resultSet.next()) {
                found = true;
                System.out.printf("%-40s %-25s %-15d%n",
                    resultSet.getString("attraction_name"),
                    resultSet.getString("neighbourhood_name"),
                    resultSet.getInt("nearby_crimes"));
            }
            
            if (!found) {
                System.out.println("No attractions found matching: " + attractionName);
                System.out.println("Try using a partial name or check the ID from the list above.");
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

// Overloaded version that can take ID or name
public void checkAttractionSafety(String input, boolean isIdSearch) {
    if (isIdSearch) {
        // Search by ID
        displayAllAttractions();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Searching for Attraction ID: " + input);
        System.out.println("=".repeat(60));
        
        String sql = """
                SELECT a.attraction_name, n.neighbourhood_name,
                       COUNT(c.crime_id) AS nearby_crimes
                FROM attractions a
                JOIN neighbourhoods n ON a.neighbourhood_id = n.neighbourhood_id
                LEFT JOIN crimes c ON n.neighbourhood_id = c.neighbourhood_id
                WHERE a.attraction_id = ?
                GROUP BY a.attraction_id, a.attraction_name, n.neighbourhood_name;
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, Integer.parseInt(input));
            
            try (ResultSet resultSet = statement.executeQuery()) {
                System.out.println("\n=== ATTRACTION SAFETY CHECK RESULTS ===");
                System.out.printf("%-40s %-25s %-15s%n", 
                    "Attraction", "Neighbourhood", "Nearby Crimes");
                System.out.println("--------------------------------------------------------------");
                
                if (resultSet.next()) {
                    System.out.printf("%-40s %-25s %-15d%n",
                        resultSet.getString("attraction_name"),
                        resultSet.getString("neighbourhood_name"),
                        resultSet.getInt("nearby_crimes"));
                } else {
                    System.out.println("No attraction found with ID: " + input);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    } else {
        // Call the original method
        checkAttractionSafety(input);
    }
}

// Additional helper: Show attractions with crime counts
public void showAttractionsWithSafetySummary() {
    String sql = """
            SELECT a.attraction_id, a.attraction_name, n.neighbourhood_name,
                   COUNT(c.crime_id) AS nearby_crimes
            FROM attractions a
            JOIN neighbourhoods n ON a.neighbourhood_id = n.neighbourhood_id
            LEFT JOIN crimes c ON n.neighbourhood_id = c.neighbourhood_id
            GROUP BY a.attraction_id, a.attraction_name, n.neighbourhood_name
            ORDER BY a.attraction_name;
            """;
    
    System.out.println("\n=== ATTRACTIONS WITH SAFETY SUMMARY ===");
    System.out.printf("%-5s %-40s %-25s %-15s%n", 
        "ID", "Attraction Name", "Neighbourhood", "Nearby Crimes");
    System.out.println("-".repeat(95));
    
    try (PreparedStatement statement = connection.prepareStatement(sql);
         ResultSet resultSet = statement.executeQuery()) {
        
        int attractionCount = 0;
        while (resultSet.next()) {
            System.out.printf("%-5d %-40s %-25s %-15d%n",
                resultSet.getInt("attraction_id"),
                resultSet.getString("attraction_name"),
                resultSet.getString("neighbourhood_name"),
                resultSet.getInt("nearby_crimes"));
            attractionCount++;
        }
        
        System.out.println("\nTotal attractions: " + attractionCount);
        System.out.println("\nUse checkAttractionSafety() for detailed safety information.");
        
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
    // Keep original method name for backward compatibility
    public void attractionsVsCrimes() {
        String sql = """
                SELECT n.neighbourhood_id,
                       n.neighbourhood_name,
                       COUNT(DISTINCT a.attraction_id) AS num_attractions,
                       COUNT(c.crime_id) AS num_crimes
                FROM neighbourhoods n
                LEFT JOIN attractions a ON n.neighbourhood_id = a.neighbourhood_id
                LEFT JOIN crimes c ON n.neighbourhood_id = c.neighbourhood_id
                GROUP BY n.neighbourhood_id, n.neighbourhood_name;
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Attractions vs Crimes per Neighbourhood:");
            System.out.printf("%-8s %-25s %-15s %-12s%n", 
                "NID", "Neighbourhood", "Attractions", "Crimes");
            System.out.println("---------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-8d %-25s %-15d %-12d%n",
                    resultSet.getInt("neighbourhood_id"),
                    resultSet.getString("neighbourhood_name"),
                    resultSet.getInt("num_attractions"),
                    resultSet.getInt("num_crimes"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 11) Attraction count vs average listing price 
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Attraction Impact on Airbnb Prices:");
            System.out.printf("%-8s %-25s %-15s %-15s%n", 
                "NID", "Neighbourhood", "Attractions", "Avg Price");
            System.out.println("-----------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-8d %-25s %-15d $%-14.2f%n",
                    resultSet.getInt("neighbourhood_id"),
                    resultSet.getString("neighbourhood_name"),
                    resultSet.getInt("num_attractions"),
                    resultSet.getDouble("average_price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    

    // 12) Attractions, crimes, police stations per neighbourhood - Renamed to match DatabaseInterface
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Comprehensive Neighbourhood Safety Profiles:");
            System.out.printf("%-8s %-25s %-15s %-12s %-12s%n", 
                "NID", "Neighbourhood", "Attractions", "Crimes", "Police Stations");
            System.out.println("----------------------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-8d %-25s %-15d %-12d %-12d%n",
                    resultSet.getInt("neighbourhood_id"),
                    resultSet.getString("neighbourhood_name"),
                    resultSet.getInt("num_attractions"),
                    resultSet.getInt("num_crimes"),
                    resultSet.getInt("num_stations"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 14) Amenities vs bookings per listing
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Amenities impact on Booking Frequency:");
            System.out.printf("%-12s %-30s %-15s %-12s%n", 
                "Listing ID", "Name", "Amenities", "Bookings");
            System.out.println("-----------------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-12d %-30s %-15d %-12d%n",
                    resultSet.getInt("listing_id"),
                    resultSet.getString("name"),
                    resultSet.getInt("num_amenities"),
                    resultSet.getInt("num_bookings"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 15) Amenities vs price 
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Amenity Impact on Pricing:");
            System.out.printf("%-12s %-30s %-15s %-12s%n", 
                "Listing ID", "Name", "Price", "Amenities");
            System.out.println("----------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-12d %-30s $%-14.2f %-12d%n",
                    resultSet.getInt("listing_id"),
                    resultSet.getString("name"),
                    resultSet.getDouble("price"),
                    resultSet.getInt("num_amenities"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 16) Hosts who also appear as guests
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Hosts Who Also Appear as Guests:");
            System.out.printf("%-15s %-30s %-20s%n", 
                "Host ID", "Host Name", "Guest Bookings");
            System.out.println("------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-10d %-30s %-20d%n",
                    resultSet.getInt("host_id"),
                    resultSet.getString("host_name"),
                    resultSet.getInt("num_guest_bookings"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 17) Reviews vs bookings
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Review Impact Analysis:");
            System.out.printf("%-12s %-30s %-20s %-12s%n", 
                "Listing ID", "Name", "Review Score", "Num Reviews", "Num Bookings");
            System.out.println("-----------------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-12d %-30s %-20.2f %-12d%n",
                    resultSet.getInt("listing_id"),
                    resultSet.getString("name"),
                    resultSet.getDouble("review_scores_value"),
                    resultSet.getInt("num_reviews"),
                    resultSet.getInt("num_bookings"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Keep original method name for backward compatibility
    public void reviewsAnalysis() {
        analyzeReviewImpact();
    }

    // 18) Listings never booked
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Listings Never Booked:");
            System.out.printf("%-12s %-30s %-25s %-20s %-15s%n", 
                "Listing ID", "Name", "Host Name", "Verified", "Host Since");
            System.out.println("--------------------------------------------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-12d %-30s %-25s %-20s %-15s%n",
                    resultSet.getInt("listing_id"),
                    resultSet.getString("name"),
                    resultSet.getString("host_name"),
                    resultSet.getString("host_identity_verified"),
                    resultSet.getDate("host_since"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 19) Criminals active in multiple neighbourhoods - Renamed to match DatabaseInterface
    public void trackRepeatOffenders() {
        String sql = """
                SELECT cr.criminal_id,
                       cr.criminal_name,
                       COUNT(DISTINCT c.neighbourhood_id) AS neighbourhood_count,
                       COUNT(DISTINCT c.crime_id) AS total_crimes
                FROM criminals cr
                JOIN crimes c ON cr.criminal_id = c.criminal_id
                GROUP BY cr.criminal_id, cr.criminal_name
                HAVING COUNT(DISTINCT c.neighbourhood_id) > 1
                ORDER BY neighbourhood_count DESC, total_crimes DESC;
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Repeat Offenders Active in Multiple Neighbourhoods:");
            System.out.printf("%-12s %-25s %-20s %-15s%n", 
                "Criminal ID", "Name", "Neighbourhoods", "Total Crimes");
            System.out.println("----------------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-12d %-25s %-20d %-15d%n",
                    resultSet.getInt("criminal_id"),
                    resultSet.getString("criminal_name"),
                    resultSet.getInt("neighbourhood_count"),
                    resultSet.getInt("total_crimes"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 20) Most visited attractions by guests 
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Top 10 Most Visited Attractions:");
            System.out.printf("%-40s %-25s %-15s%n", 
                "Attraction", "Neighbourhood", "Total Visits");
            System.out.println("----------------------------------------------------------------------");
                
            while (resultSet.next()) {
                System.out.printf("%-40s %-25s %-15d%n",
                    resultSet.getString("attraction_name"),
                    resultSet.getString("neighbourhood_name"),
                    resultSet.getInt("total_visits"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // 21) Criminal category analysis
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Criminal Category Analysis:");
            System.out.printf("%-10s %-15s %-25s %n", 
                "Gender", "Age Group", "Total Crimes");
            System.out.println("----------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-10s %-15s %-25d %n",
                    resultSet.getString("gender"),
                    resultSet.getString("age_group"),
                    resultSet.getInt("total_crimes"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 22) Most active criminals near attractions
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Most Active Criminals Near Attractions:");
            System.out.printf("%-25s %-15s %-25s %-20s%n", 
                "Criminal Name", "Age Group", "Neighbourhood", "Crimes Near Attractions");
            System.out.println("---------------------------------------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-25s %-15s %-25s %-20d%n",
                    resultSet.getString("criminal_name"),
                    resultSet.getString("age_group"),
                    resultSet.getString("neighbourhood_name"),
                    resultSet.getInt("crimes_near_attractions"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 23) Busiest booking month
    public void busiestBookingMonth() {
        String sql = """
                SELECT DISTINCT TOP 3
                    MONTH(booking_date) AS month,
                    COUNT(*) AS total_bookings
                FROM guest_book_listings
                GROUP BY MONTH(booking_date)
                ORDER BY total_bookings DESC;
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Busiest Booking Month:");
            System.out.printf("%-10s %-15s%n", "Month", "Total Bookings");
            System.out.println("----------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-15s %-15d%n",
                    monthMapper(resultSet.getInt("month")),
                    resultSet.getInt("total_bookings"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //query 23 helper method
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
    
    // 24. Most expensive property type in each neighbourhood
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Most Expensive Property Types:");
            System.out.printf("%-30s %-15s%n", 
                 "Property Type", "Avg Price");
            System.out.println("-----------------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-30s $%-14.2f%n",
                    resultSet.getString("property_type"),
                    resultSet.getDouble("avg_price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 25) Most booked property type
    public void mostBookedPropertyTypes(int limit) {
        String sql = """
                SELECT TOP (?)
                    l.property_type,
                    COUNT(*) AS total_bookings
                FROM guest_book_listings gbl
                JOIN listings l 
                    ON gbl.listing_id = l.listing_id
                GROUP BY l.property_type
                ORDER BY total_bookings DESC;
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                System.out.println("Top " + limit + " Most Booked Property Types:");
                System.out.printf("%-30s %-15s%n", "Property Type", "Total Bookings");
                System.out.println("------------------------------------------------");
                
                while (resultSet.next()) {
                    System.out.printf("%-30s %-15d%n",
                        resultSet.getString("property_type"),
                        resultSet.getInt("total_bookings"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 26) Cheapest vs most expensive listing type
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

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            System.out.println("Price Range by Property Type:");
            System.out.printf("%-30s %-15s %-15s%n", "Property Type", "Min Price", "Max Price");
            System.out.println("-------------------------------------------------------------");
            
            while (resultSet.next()) {
                System.out.printf("%-30s $%-14.2f $%-14.2f%n",
                    resultSet.getString("property_type"),
                    resultSet.getDouble("min_price"),
                    resultSet.getDouble("max_price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 27) Search comments by neighbourhood ID
    public void commentsByNeighbourhood(int neighbourhoodId) {
        String sql = """
                SELECT 
                    n.neighbourhood_name,
                    l.name AS listing_name,
                    r.comments
                FROM reviews r
                JOIN listings l ON r.listing_id = l.listing_id
                JOIN neighbourhoods n ON l.neighbourhood_id = n.neighbourhood_id
                WHERE n.neighbourhood_id = ?;
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, neighbourhoodId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                System.out.println("Comments for Neighbourhood ID " + neighbourhoodId + ":");
                System.out.printf("%-25s %-30s %-50s%n", 
                    "Neighbourhood", "Listing", "Comments");
                System.out.println("--------------------------------------------------------------------------------------");
                
                while (resultSet.next()) {
                    System.out.printf("%-25s %-30s %-50s%n",
                        resultSet.getString("neighbourhood_name"),
                        resultSet.getString("listing_name"),
                        resultSet.getString("comments"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    //For DatabaseInterface neighbourhood category option 5
public void getTopListingReviews(String neighbourhoodName) {
    // Using COUNT to get the number of reviews instead of assuming a column exists
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
            System.out.println("\nTop Rated Listings in " + neighbourhoodName + ":");
            System.out.printf("%-10s %-150s %-60s %-35s %-45s%n", 
                "Listing ID", "Name", "Rating", "Reviews", "Host");
            System.out.println("-".repeat(105));
            
            int count = 0;
            while (resultSet.next()) {
                System.out.printf("%-10d %-150s %-60s %-35s %-45s%n",
                    resultSet.getInt("listing_id"),
                    resultSet.getString("name"),
                    resultSet.getDouble("review_scores_value"),
                    resultSet.getInt("review_count"),
                    resultSet.getString("host_name"));
                count++;
            }
            
            if (count == 0) {
                System.out.println("No listings with reviews found in " + neighbourhoodName);
            }
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}
   // OVERLOADED VERSION: Search comments by neighbourhood name
    public void commentsByNeighbourhood(String neighbourhoodName) {
        try {
            int neighbourhoodId = getNeighbourhoodIdByName(neighbourhoodName);
            if (neighbourhoodId == -1) {
                System.out.println("Neighbourhood not found: " + neighbourhoodName);
                return;
            }
            commentsByNeighbourhood(neighbourhoodId); // Call the original method
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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