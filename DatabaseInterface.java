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
    System.out.print("\nSelect neighbourhood number: ");
    
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
                            System.out.println("\n \n Fetching requested info....\n \n");
                            db.showCrimesInNeighbourhood(neighbourhood1);
                        }
                        break;
                        
                    case "2":
                        String neighbourhood2 = selectNeighbourhood();
                        if (neighbourhood2 != null) {
                            System.out.println("\n \n Fetching requested info....\n \n");
                            db.browseLocalListings(neighbourhood2);
                        }
                        break;
                        
                    case "3":
                        String neighbourhood3 = selectNeighbourhood();
                        if (neighbourhood3 != null) {
                            System.out.print("Sort (high/low): ");
                            String order = localScanner.nextLine().trim();
                            System.out.println("\n \nFetching requested info....\n \n");
                            db.rankListingsByPrice(neighbourhood3, order);;
                        }
                        break;
                        
                    case "4":
                        System.out.println("\n \n Fetching requested info....\n \n");
                        db.countListingsPerNeighbourhood();
                        break;
                        
                    case "5":
                        String neighbourhood5 = selectNeighbourhood();
                        if (neighbourhood5 != null) {
                            System.out.println("\n \n Fetching requested info....\n \n");
                            db.getTopListingReviews(neighbourhood5);;
                        }
                        break;
                        
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        
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
            System.out.println("6. Criminal Category committing most crimes");
            System.out.println("7. Active criminals in neighbourhoods with attractions");
            System.out.println("0. Back to main menu");
            System.out.print("Choice (1-5, 0 to exit): ");
            
            String choice = localScanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    // Query 5: Top N neighborhoods with most crimes
                    System.out.print("Please enter the number of rows to display: ");
                    try {
                        int limit = Integer.parseInt(localScanner.nextLine().trim());
                        System.out.println("\n \n Fetching requested info....\n \n");
                        db.getHighCrimeNeighbourhoods(limit);

                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid number");
                    }
                    break;
                    
                case "2":
                    // Query 7: Crime handled per police station
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.getPoliceStationCoverage();
                    break;
                    
                case "3":
                    // Query 9: Crime rate vs. Airbnb listings analysis
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.analyzeCrimeVsTourism();
                    break;
                    
                case "4":
                    // Query 10: Attractions vs. crime comparisons - needs attraction name
                    System.out.println("\n \n Fetching requested info....\n \n");
					db.displayAllAttractions();
                    System.out.print("Enter attraction name: ");
                    String attraction = localScanner.nextLine().trim();
                    db.checkAttractionSafety(attraction);
                    break;
                    
                case "5":
                    // Query 13: Repeat offenders vs attractions
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.trackRepeatOffenders();
                    break;
                
                case "6":
                    // Query 21: Criminal category committing most crimes
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.criminalCategoryAnalysis();
                    break;

                case "7":
                    // Query 22: Active criminals in neighbourhoods with attractions 
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.criminalsNearAttractions();
                    break;
                    
                case "0":
                    stayInCategory = false;
                    displayMainMenu();
                    break;
                    
                default:
                    System.out.println("Invalid choice.");
            }
        }
      
    }

    private static void runAirbnbCategory() {
        Scanner localScanner = new Scanner(System.in);
        boolean stayInCategory = true;
        
        while (stayInCategory) {
            System.out.println("\n--- AIRBNB MARKET ANALYTICS ---");
            System.out.println("1. Placeholder for some other query");
            System.out.println("2. Amenity-Booking Analysis");
            System.out.println("3. Amenity-pricing impact");
            System.out.println("4. Host-guest overlap");
            System.out.println("5. Review-Booking analysis");
            System.out.println("6. Unbooked Listings");
            System.out.println("7. Busiest Booking months");
            System.out.println("8. Most booked property type");
            System.out.println("9. Most expensive property type");
            System.out.println("10. Cheapest V/s Most expensive listing type");
            System.out.println("0. Back to main menu");
            System.out.print("Choice (1-5, 0 to exit): ");
            
            String choice = localScanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    // Query 2: List all associated amenities for a specific Airbnb listing
                    // System.out.print("Enter listing ID: ");
                    // System.out.println("\n \n Fetching requested info....\n \n");
                    // try {
                    //     int id = Integer.parseInt(localScanner.nextLine().trim());
                    //     db.getListingAmenities(id);
                    // } catch (NumberFormatException e) {
                    //     System.out.println("Please enter a valid ID");
                    // }
                    break;
                    
                case "2":
                    // Query 14: Amenities vs booking analysis
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.analyzeAmenityPopularity();
                    break;
                    
                case "3":
                    //// Query 15: Amenities vs pricing analysis
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.analyzeAmenityPricing();
                    break;
                    
                case "4":
                    // Query 16: Host who are also guests
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.findHostGuestOverlap();
                    break;
                    
                case "5":
                    // Query 17: Reviews vs. booking analysis
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.analyzeReviewImpact();
                    break;

                case "6":
                    // Query 18:Unbooked listings
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.unbookedListings();
                    break;
                    
                case "7":
                    // Query 23: Busiest Booking month
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.busiestBookingMonth();
                    break;
                    
                case "8":
                    // Query 24: most expensive property type
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.propertyTypePricesByNeighbourhood();
                    break;
                    
                case "9":
                    /// Query 25: most booked property type
                    System.out.print("\nEnter how many would you like to see: ");
                    try {
                        int limit = Integer.parseInt(localScanner.nextLine().trim());
                        System.out.println("\n \n Fetching requested info....\n \n");
                        db.mostBookedPropertyTypes(limit);
                    } catch (NumberFormatException e) {
                        System.out.println("Please enter a valid ID");
                    }
                    System.out.println("\n \n Fetching requested info....\n \n");
                   
                    break;
                    
                case "10":
                    // Query 26: Price range by property type
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.priceRangeByPropertyType();
                    break;
                    
                case "0":
                    stayInCategory = false;
                    displayMainMenu();
                    break;
                    
                default:
                    System.out.println("Invalid choice.");
            }
        }
        
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
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.trackGuestAttractionActivity();
                    break;
                    
                case "2":
                    // Query 11: Attractions vs. Airbnb prices
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.analyzeAttractionImpactOnPrices();
                    break;
                    
                case "3":
                    // Query 20: Most visited attractions by guests
                    System.out.println("\n \n Fetching requested info....\n \n");
                    db.findMostVisitedAttractions();
                    break;
                    
                case "0":
                    stayInCategory = false;
                    displayMainMenu();
                    break;
                    
                default:
                    System.out.println("Invalid choice.");
            }
        }
        
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
                /// Query 12: Crime, attractions, and police stations analysis
                System.out.println("\n \n Fetching requested info....\n \n");
                db.generateAreaSafetyProfiles();
            } else if (choice.equals("0")) {
                stayInCategory = false;
                displayMainMenu();
            } else {
                System.out.println("Invalid choice.");
            }
        }
        
    }

    private static void deployDatabase() {
        try {
            // Execute SQL files sequentially
            System.out.println("Creating Toronto database tables...");
            //// Add your actual SQL file execution here
            System.out.println("Database deployment completed.");
        } catch (Exception e) {
            System.out.println("Deployment error: " + e.getMessage());
        }
    }

    private static void reloadData() {
        try {
            System.out.println("Loading data...");
            ///// po.loadConfigAndPopulate();
            System.out.println("Data reload completed.");
        } catch (Exception e) {
            System.out.println("Data loading error: " + e.getMessage());
        }
    }
   
}