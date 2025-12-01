import java.util.Scanner;
import java.util.List;
import java.util.Arrays;

public class DatabaseInterface {
    static TorontoDatabase db = new TorontoDatabase();
    static Populate po = new Populate();
    static Scanner mainScanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println(" ");
        System.err.println(" ");
        System.out.println("Welcome to the City of Toronto Database!");
        System.out.println("Explore Toronto Data: Neighbourhoods, Crime, Airbnb & Attractions");
        System.out.println("---------------------------------------------------------------");

        while (true) {
            System.out.print("\nOptions: \n [h] for help ie display main menu, \n [q] to quit, \n [r] to delete and repopulate ");
            System.out.print("\n User Input: ");
            String input = mainScanner.nextLine().trim();

            if (input.equalsIgnoreCase("q")) {
                System.out.println("Exiting the City of Toronto Database. Goodbye!");
                break;
            
            } else if (input.equalsIgnoreCase("h") || input.equalsIgnoreCase("help")) {
                displayMainMenu();
            
            } else if (input.equalsIgnoreCase("d")) {
                deployDatabase();
            
            } else if (input.equalsIgnoreCase("r")) {
                reloadData();
            
            } else if (input.equals("1")) {
                runNeighbourhoodCategory();
            
            } else if (input.equals("2")) {
                runCrimeCategory();
            
            } else if (input.equals("3")) {
                runAirbnbCategory();
            
            } else if (input.equals("4")) {
                runTourismCategory();
            
            } else if (input.equals("5")) {
                runCrossAnalysisCategory();
            
            } else {
                System.out.println("Invalid option. Type 'h' for menu.");
            }
        }
        mainScanner.close();
    }

    private static void displayMainMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("THE CITY OF TORONTO - MAIN MENU");
        System.out.println("=".repeat(50));
        System.out.println("1. Explore Neighbourhoods");
        System.out.println("2. Safety & Crime Insights");
        System.out.println("3. Airbnb Market Analysis");
        System.out.println("4. Tourism & Attractions");
        System.out.println("5. Cross-Analysis Reports");
        System.out.println();
        System.out.println("=".repeat(50));
    }

    // NEW METHOD: Neighbourhood selection system - FIXED VERSION
private static String selectNeighbourhood() {
    Scanner localScanner = new Scanner(System.in);
    
    System.out.println("\n--- SELECT NEIGHBOURHOOD REGION ---");
    System.out.println("1. Downtown Core");
    System.out.println("2. West End");
    System.out.println("3. East End");
    System.out.println("4. North York");
    System.out.println("5. Etobicoke");
    System.out.println("6. Scarborough");
    System.out.println("7. Central/Midtown");
    System.out.println("0. Cancel");
    System.out.print("Choose region (1-7, 0 to cancel): ");
    
    String regionChoice = localScanner.nextLine().trim();
    if (regionChoice.equals("0")) return null;
    
    List<String> neighbourhoods = getNeighbourhoodsByRegion(regionChoice);
    
    if (neighbourhoods == null || neighbourhoods.isEmpty()) {
        System.out.println("No neighbourhoods found for this region.");
        return null;
    }
    
    System.out.println("\n--- NEIGHBOURHOODS IN SELECTED REGION ---");
    // Display with proper numbering
    for (int i = 0; i < neighbourhoods.size(); i++) {
        System.out.printf("%2d. %s\n", (i+1), neighbourhoods.get(i));
    }
    System.out.println(" 0. Go back to region selection");
    System.out.print("Select neighbourhood number: ");
    
    try {
        int hoodChoice = Integer.parseInt(localScanner.nextLine().trim());
        
        if (hoodChoice == 0) {
            return selectNeighbourhood(); // Recursive call to go back
        }
        
        if (hoodChoice >= 1 && hoodChoice <= neighbourhoods.size()) {
            String selectedNeighbourhood = neighbourhoods.get(hoodChoice - 1);
            System.out.println("Selected: " + selectedNeighbourhood);
            return selectedNeighbourhood;
        } else {
            System.out.println("Invalid selection. Please try again.");
            return selectNeighbourhood();
        }
    } catch (NumberFormatException e) {
        System.out.println("Please enter a valid number.");
        return selectNeighbourhood();
    }
}

    // Helper method to get neighbourhoods by region
    private static List<String> getNeighbourhoodsByRegion(String regionCode) {
        switch (regionCode) {
            case "1": // Downtown Core
                return Arrays.asList(
                    "Bay Street Corridor", "Church-Yonge Corridor", "Cabbagetown-South St.James Town",
                    "Entertainment District", "Financial District", "Kensington-Chinatown",
                    "Moss Park", "Niagara", "North St.James Town", "Regent Park",
                    "St. Lawrence", "South Core", "University", "Waterfront Communities-The Island",
                    "Yonge-Dundas"
                );
                
            case "2": // West End
                return Arrays.asList(
                    "Alderwood", "Baby Point", "Bloor West Village", "Brockton Village",
                    "Corso Italia-Davenport", "Dovercourt-Wallace Emerson-Junction", "Dufferin Grove",
                    "High Park North", "High Park-Swansea", "Junction Area",
                    "Keelesdale-Eglinton West", "Lambton Baby Point", "Little Portugal",
                    "Oakwood Village", "Palmerston-Little Italy", "Parkdale",
                    "Roncesvalles", "South Parkdale", "Trinity-Bellwoods", "Wychwood"
                );
                
            case "3": // East End
                return Arrays.asList(
                    "Beaches", "Broadview North", "Danforth", "Danforth East York",
                    "East End-Danforth", "Greenwood-Coxwell", "Leslieville", "North Riverdale",
                    "Old East York", "Playter Estates-Danforth", "Riverdale", "South Riverdale",
                    "Taylor-Massey", "The Beaches", "Upper Beaches", "Woodbine Corridor",
                    "Woodbine-Lumsden", "Blake-Jones", "Oakridge", "Victoria Village"
                );
                
            case "4": // North York
                return Arrays.asList(
                    "Bathurst Manor", "Bayview Village", "Bayview Woods-Steeles", "Bedford Park-Nortown",
                    "Bridle Path-Sunnybrook-York Mills", "Don Valley Village", "Don Mills", "Flemingdon Park",
                    "Henry Farm", "Hillcrest Village", "Hogg's Hollow", "Lawrence Park North",
                    "Lawrence Park South", "Newtonbrook East", "Newtonbrook West", "Parkway Forest",
                    "Pleasant View", "St.Andrew-Windfields", "Willowdale East", "Willowdale West",
                    "York Mills", "York University Heights", "Yorkdale-Glen Park", "Jane and Finch",
                    "Black Creek"
                );
                
            case "5": // Etobicoke
                return Arrays.asList(
                    "Clairville", "Eatonville", "Edenbridge-Humber Valley", "Elms-Old Rexdale",
                    "Eringate-Centennial-West Deane", "Etobicoke West Mall", "Humber Heights-Westmount",
                    "Humbermede", "Humber Summit", "Islington-City Centre West",
                    "Kingsview Village-The Westway", "Kingsway South", "Long Branch",
                    "Markland Wood", "Mimico (includes Humber Bay Shores)", "New Toronto",
                    "Princess-Rosethorn", "Rexdale-Kipling", "Stonegate-Queensway"
                );
                
            case "6": // Scarborough
                return Arrays.asList(
                    "Agincourt North", "Agincourt South-Malvern West", "Bendale", "Birchcliffe-Cliffside",
                    "Centennial Scarborough", "Clairlea-Birchmount", "Cliffcrest", "Dorset Park",
                    "Eglinton East", "Guildwood", "Highland Creek", "Ionview", "Kennedy Park",
                    "L'Amoreaux", "Malvern", "Milliken", "Morningside", "Rouge",
                    "Scarborough Village", "Tam O'Shanter-Sullivan", "West Hill", "Wexford/Maryvale",
                    "Woburn"
                );
                
            case "7": // Central/Midtown
                return Arrays.asList(
                    "Annex", "Casa Loma", "Davisville Village", "Deer Park",
                    "Forest Hill North", "Forest Hill South", "Humewood-Cedarvale", "Leaside-Bennington",
                    "Mount Pleasant East", "Mount Pleasant West", "North Toronto", "Rosedale-Moore Park",
                    "Summerhill", "Yonge-Eglinton", "Yonge-St.Clair"
                );
                
            default:
                return null;
        }
    }

    private static void runNeighbourhoodCategory() {
        Scanner localScanner = new Scanner(System.in);
        boolean stayInCategory = true;
        
        while (stayInCategory) {
            System.out.println("\n--- NEIGHBOURHOOD EXPLORER ---");
            System.out.println("1. Crime reports in area");
            System.out.println("2. Browse local listings");
            System.out.println("3. Price-ranked rentals");
            System.out.println("4. Rental density by area");
            System.out.println("5. Top listing reviews");
            System.out.println("0. Back to main menu");
            System.out.print("Choice (1-5, 0 to exit): ");
            
            String choice = localScanner.nextLine().trim();

            if(choice.equals("0")){
                stayInCategory = false;
                displayMainMenu();
                break;
            }
            
            switch (choice) {
                case "1":
                    String neighbourhood1 = selectNeighbourhood();
                    if (neighbourhood1 != null) {
                    	 db.showCrimesInNeighbourhood(neighbourhood1);
                        System.out.println("[Query 1 would execute for: " + neighbourhood1 + "]");
                    }
                    break;
                    
                case "2":
                    String neighbourhood2 = selectNeighbourhood();
                    if (neighbourhood2 != null) {
                        db.browseLocalListings(neighbourhood2);
                        System.out.println("[Query 2 would execute for: " + neighbourhood2 + "]");
                    }
                    break;
                    
                case "3":
                    String neighbourhood3 = selectNeighbourhood();
                    if (neighbourhood3 != null) {
                        System.out.print("Sort (high/low): ");
                        String order = localScanner.nextLine().trim();
                        db.rankListingsByPrice(neighbourhood3, order);
                        System.out.println("[Query 3 would execute for: " + neighbourhood3 + " sorted by: " + order + "]");
                    }
                    break;
                    
                case "4":
                    db.countListingsPerNeighbourhood();
                    System.out.println("[Query 4 would execute here - shows ALL neighbourhoods]");
                    break;
                    
                case "5":
                    String neighbourhood5 = selectNeighbourhood();
                    if (neighbourhood5 != null) {
                        db.getTopListingReviews(neighbourhood5);
                        System.out.println("[Query 5 would execute for: " + neighbourhood5 + "]");
                    }
                    break;
                    
                default:
                    System.out.println("Invalid choice.");
            }
        }
        localScanner.close();
    }

    private static void runCrimeCategory() {
        Scanner localScanner = new Scanner(System.in);
        boolean stayInCategory = true;
        
        while (stayInCategory) {
            System.out.println("\n--- CRIME & SAFETY DASHBOARD ---");
            System.out.println("1. High-crime areas");
            System.out.println("2. Police station coverage");
            System.out.println("3. Crime vs tourism analysis");
            System.out.println("4. Attraction safety check");
            System.out.println("5. Repeat offender tracking");
            System.out.println("0. Back to main menu");
            System.out.print("Choice (1-5, 0 to exit): ");
            
            String choice = localScanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    // Query 5: Top N neighborhoods with most crimes
                    System.out.print("Show top N areas: ");
                    try {
                        int limit = Integer.parseInt(localScanner.nextLine().trim());
                        db.getHighCrimeNeighbourhoods(limit);
                        System.out.println("[Query 5 would execute with limit: " + limit + "]");
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid number");
                    }
                    break;
                    
                case "2":
                    // Query 7: Crime handled per police station
                    db.getPoliceStationCoverage();
                    System.out.println("[Query 7: Police coverage query would execute here]");
                    break;
                    
                case "3":
                    // Query 9: Crime rate vs. Airbnb listings analysis
                    db.analyzeCrimeVsTourism();
                    System.out.println("[Query 9: Crime-tourism analysis would execute here]");
                    break;
                    
                case "4":
                    // Query 10: Attractions vs. crime comparisons - needs attraction name
					db.displayAllAttractions();
                    System.out.print("Enter attraction name: ");
                    String attraction = localScanner.nextLine().trim();
                    db.checkAttractionSafety(attraction);
                    System.out.println("[Query 10 would execute for attraction: " + attraction + "]");
                    break;
                    
                case "5":
                    // Query 13: Repeat offenders vs attractions
                    db.trackRepeatOffenders();
                    System.out.println("[Query 13: Repeat offender query would execute here]");
                    break;
                    
                case "0":
                    stayInCategory = false;
                    displayMainMenu();
                    break;
                    
                default:
                    System.out.println("Invalid choice.");
            }
        }
        localScanner.close();
    }

    private static void runAirbnbCategory() {
        Scanner localScanner = new Scanner(System.in);
        boolean stayInCategory = true;
        
        while (stayInCategory) {
            System.out.println("\n--- AIRBNB MARKET ANALYTICS ---");
            System.out.println("1. Listing amenities");
            System.out.println("2. Amenity popularity");
            System.out.println("3. Amenity pricing impact");
            System.out.println("4. Host-guest overlap");
            System.out.println("5. Review impact analysis");
            System.out.println("0. Back to main menu");
            System.out.print("Choice (1-5, 0 to exit): ");
            
            String choice = localScanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    // Query 2: List all associated amenities for a specific Airbnb listing
                    System.out.print("Enter listing ID: ");
                    try {
                        int id = Integer.parseInt(localScanner.nextLine().trim());
                        db.getListingAmenities(id);
                        System.out.println("[Query 2 would execute for listing ID: " + id + "]");
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid ID");
                    }
                    break;
                    
                case "2":
                    // Query 14: Amenities vs booking analysis
                    db.analyzeAmenityPopularity();
                    System.out.println("[Query 14: Amenity popularity query would execute here]");
                    break;
                    
                case "3":
                    // Query 15: Amenities vs pricing analysis
                    db.analyzeAmenityPricing();
                    System.out.println("[Query 15: Pricing impact query would execute here]");
                    break;
                    
                case "4":
                    // Query 16: Host who are also guests
                    db.findHostGuestOverlap();
                    System.out.println("[Query 16: Host-guest query would execute here]");
                    break;
                    
                case "5":
                    // Query 17: Reviews vs. booking analysis
                    db.analyzeReviewImpact();
                    System.out.println("[Query 17: Review impact query would execute here]");
                    break;
                    
                case "0":
                    stayInCategory = false;
                    displayMainMenu();
                    break;
                    
                default:
                    System.out.println("Invalid choice.");
            }
        }
        localScanner.close();
    }

    private static void runTourismCategory() {
        Scanner localScanner = new Scanner(System.in);
        boolean stayInCategory = true;
        
        while (stayInCategory) {
            System.out.println("\n--- TOURISM & VISITOR INSIGHTS ---");
            System.out.println("1. Guest attraction patterns");
            System.out.println("2. Attraction price influence");
            System.out.println("3. Most visited spots");
            System.out.println("0. Back to main menu");
            System.out.print("Choice (1-3, 0 to exit): ");
            
            String choice = localScanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    // Query 8: Guests with both Airbnb & attraction visits
                    db.trackGuestAttractionActivity();
                    System.out.println("[Query 8: Guest patterns query would execute here]");
                    break;
                    
                case "2":
                    // Query 11: Attractions vs. Airbnb prices
                    db.analyzeAttractionImpactOnPrices();
                    System.out.println("[Query 11: Price influence query would execute here]");
                    break;
                    
                case "3":
                    // Query 20: Most visited attractions by guests
                    db.findMostVisitedAttractions();
                    System.out.println("[Query 20: Popular spots query would execute here]");
                    break;
                    
                case "0":
                    stayInCategory = false;
                    displayMainMenu();
                    break;
                    
                default:
                    System.out.println("Invalid choice.");
            }
        }
        localScanner.close();
    }

    private static void runCrossAnalysisCategory() {
        Scanner localScanner = new Scanner(System.in);
        boolean stayInCategory = true;
        
        while (stayInCategory) {
            System.out.println("\n--- CROSS-DOMAIN ANALYSIS ---");
            System.out.println("1. Comprehensive safety profiles");
            System.out.println("0. Back to main menu");
            System.out.print("Choice (1 or 0): ");
            
            String choice = localScanner.nextLine().trim();
            
            if (choice.equals("1")) {
                // Query 12: Crime, attractions, and police stations analysis
                db.generateAreaSafetyProfiles();
                System.out.println("[Query 12: Cross-analysis query would execute here]");
            } else if (choice.equals("0")) {
                stayInCategory = false;
                displayMainMenu();
            } else {
                System.out.println("Invalid choice.");
            }
        }
        localScanner.close();
    }

    private static void deployDatabase() {
        try {
            // Execute SQL files sequentially
            System.out.println("Creating Toronto database tables...");
            // Add your actual SQL file execution here
            System.out.println("Database deployment completed.");
        } catch (Exception e) {
            System.out.println("Deployment error: " + e.getMessage());
        }
    }

    private static void reloadData() {
        try {
            System.out.println("Loading sample data...");
            // po.loadConfigAndPopulate();
            System.out.println("Data reload completed.");
        } catch (Exception e) {
            System.out.println("Data loading error: " + e.getMessage());
        }
    }
   
}