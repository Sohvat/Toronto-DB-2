import java.util.Scanner;
import java.util.List;
import java.util.Arrays;

public class DatabaseInterface {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String UNDERLINE = "\u001B[4m";

    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String MAGENTA = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    static TorontoDatabase db = new TorontoDatabase();
    static Populate po = new Populate();
    static Scanner mainScanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println(" ");
        System.err.println(" ");
        System.out.println(BOLD + MAGENTA +"WELCOME TO THE CITY OF TORONTO DATABASE!"+ RESET);
        System.out.println(BOLD + MAGENTA + "Explore Toronto Data: Neighbourhoods, Crime, Airbnb & Attractions"+ RESET);
        System.out.println(BOLD + MAGENTA +"---------------------------------------------------------------"+ RESET);

        while (true) {
            System.out.print(BLUE + "\nOptions: \n [h] for help ie display main menu, \n [q] to quit, \n [r] to delete and repopulate " + RESET);
            System.out.print(BLUE + "\n User Input: " + RESET);
            String input = mainScanner.nextLine().trim();

            if (input.equalsIgnoreCase("q")) {
                System.out.println(GREEN + "Exiting the City of Toronto Database. Goodbye!" + RESET);
                break;
            
            } else if (input.equalsIgnoreCase("h") || input.equalsIgnoreCase("help")) {
                displayMainMenu();
            
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
                System.out.println(RED + "Invalid option. Type 'h' for menu." + RESET);
            }
        }
       
    }

    private static void displayMainMenu() {
        System.out.println(CYAN + "\n" + "=".repeat(50) + RESET);
        System.out.println(CYAN + "THE CITY OF TORONTO - MAIN MENU" + RESET);
        System.out.println(CYAN + "=".repeat(50) + RESET);
        System.out.println(CYAN + "1. Explore Neighbourhoods" + RESET);
        System.out.println(CYAN + "2. Safety & Crime Insights" + RESET);
        System.out.println(CYAN + "3. Airbnb Market Analysis" + RESET);
        System.out.println(CYAN + "4. Tourism & Attractions" + RESET);
        System.out.println(CYAN + "5. Cross-Analysis Reports" + RESET);
        System.out.println();
        System.out.println(CYAN + "\n" + "=".repeat(50) + RESET);
    }

private static String selectNeighbourhood() {
    Scanner localScanner = new Scanner(System.in);
    
    System.out.println(CYAN + "\n--- SELECT NEIGHBOURHOOD REGION ---" + RESET);
    System.out.println(CYAN + "1. Downtown Core" + RESET);
    System.out.println(CYAN + "2. West End" + RESET);
    System.out.println(CYAN + "3. East End" + RESET);
    System.out.println(CYAN + "4. North York" + RESET);
    System.out.println(CYAN + "5. Etobicoke" + RESET);
    System.out.println(CYAN + "6. Scarborough" + RESET);
    System.out.println(CYAN + "7. Central/Midtown" + RESET);
    System.out.println(CYAN + "0. Cancel" + RESET);
    System.out.print(MAGENTA + "Choose region (1-7, 0 to cancel): " + RESET);
    
    String regionChoice = localScanner.nextLine().trim();
    if (regionChoice.equals("0")) return null;
    
    List<String> neighbourhoods = getNeighbourhoodsByRegion(regionChoice);
    
    if (neighbourhoods == null || neighbourhoods.isEmpty()) {
        System.out.println(RED + "No neighbourhoods found for this region." + RESET);
        return null;
    }
    
    System.out.println(MAGENTA + "\n--- NEIGHBOURHOODS IN SELECTED REGION ---" + RESET);
    
    for (int i = 0; i < neighbourhoods.size(); i++) {
        System.out.printf(CYAN + "%2d. %s\n" + RESET, (i+1), neighbourhoods.get(i));
    }
    System.out.println(CYAN + " 0. Go back to region selection" + RESET);
    System.out.print(BLUE + "\nSelect neighbourhood number: " + RESET);
    
    try {
        int hoodChoice = Integer.parseInt(localScanner.nextLine().trim());
        
        if (hoodChoice == 0) {
            return selectNeighbourhood();
        }
        
        if (hoodChoice >= 1 && hoodChoice <= neighbourhoods.size()) {
            String selectedNeighbourhood = neighbourhoods.get(hoodChoice - 1);
            System.out.println(GREEN + "Selected: " + selectedNeighbourhood + RESET);
            return selectedNeighbourhood;
        } else {
            System.out.println(RED + "Invalid selection. Please try again." + RESET);
            return selectNeighbourhood();
        }
    } catch (NumberFormatException e) {
        System.out.println(RED + "Please enter a valid number." + RESET);
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
                System.out.println(CYAN + "\n--- NEIGHBOURHOOD EXPLORER ---" + RESET);
                System.out.println(CYAN + "1. Crime reports in area" + RESET);
                System.out.println(CYAN + "2. Browse local listings" + RESET);
                System.out.println(CYAN + "3. Price-ranked rentals" + RESET);
                System.out.println(CYAN + "4. Rental density by area" + RESET);
                System.out.println(CYAN + "5. Top listing reviews" + RESET);
                System.out.println(CYAN + "0. Back to main menu" + RESET);
                System.out.print(MAGENTA + "Choice (1-5, 0 to exit): " + RESET);
                
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
                            System.out.print(BLUE + "Please enter the number of rows to display: " + RESET);
                            try {
                                int limit = Integer.parseInt(localScanner.nextLine().trim());
                                System.out.println(GREEN + BOLD+  "\n \n Fetching requested info....\n \n" + RESET);
                                db.showCrimesInNeighbourhood(neighbourhood1, limit);
        
                            } catch (NumberFormatException e) {
                                System.out.println(RED + "Please enter a valid number" + RESET);
                            }        
                        }
                        break;
                        
                    case "2":
                        String neighbourhood2 = selectNeighbourhood();
                        if (neighbourhood2 != null) {
                            System.out.print(BLUE + "Please enter the number of rows to display: " + RESET);
                            try {
                                int limit = Integer.parseInt(localScanner.nextLine().trim());
                                System.out.println(GREEN + BOLD+  "\n \n Fetching requested info....\n \n" + RESET);
                                db.browseLocalListings(neighbourhood2, limit);
        
                            } catch (NumberFormatException e) {
                                System.out.println(RED + "Please enter a valid number" + RESET);
                            } 
                        }
                        break;
                        
                    case "3":
                        String neighbourhood3 = selectNeighbourhood();
                        if (neighbourhood3 != null) {
                            System.out.print(BLUE + "Sort (high/low): " + RESET);
                            String order = localScanner.nextLine().trim();
                            System.out.println(GREEN + "\n \nFetching requested info....\n \n" + RESET);
                            db.rankListingsByPrice(neighbourhood3, order);;
                        }
                        break;
                        
                    case "4":
                        System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                        db.countListingsPerNeighbourhood();
                        break;
                        
                    case "5":
                        String neighbourhood5 = selectNeighbourhood();
                        if (neighbourhood5 != null) {
                            System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                            db.topRatedListings(neighbourhood5);
                        }
                        break;
                        
                    default:
                        //always default here
                        System.out.println(RED + "Invalid choice." + RESET);
                }
            }
        
    }

    private static void runCrimeCategory() {
        Scanner localScanner = new Scanner(System.in);
        boolean stayInCategory = true;
        
        while (stayInCategory) {
            System.out.println(MAGENTA + "\n--- CRIME & SAFETY DASHBOARD ---" + RESET);
            System.out.println(CYAN + "1. High-crime areas" + RESET);
            System.out.println(CYAN + "2. Police station coverage" + RESET);
            System.out.println(CYAN + "3. Crime vs tourism analysis" + RESET);
            System.out.println(CYAN + "4. Attraction safety check" + RESET);
            System.out.println(CYAN + "5. Repeat offender tracking" + RESET);
            System.out.println(CYAN + "6. Criminal Category committing most crimes" + RESET);
            System.out.println(CYAN + "7. Active criminals in neighbourhoods with attractions" + RESET);
            System.out.println(CYAN + "0. Back to main menu" + RESET);
            System.out.print(BLUE + "Choice (1-7, 0 to exit): " + RESET);
            String choice = localScanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    System.out.print(BLUE + "Please enter the number of rows to display: " + RESET);
                    try {
                        int limit = Integer.parseInt(localScanner.nextLine().trim());
                        System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                        db.getHighCrimeNeighbourhoods(limit);

                    } catch (NumberFormatException e) {
                        System.out.println(RED + "Please enter a valid number" + RESET);
                    }
                    break;
                    
                case "2":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.getPoliceStationCoverage();
                    break;
                    
                case "3":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.analyzeCrimeVsTourism();
                    break;
                    
                case "4":
					db.displayAllAttractions();
                    System.out.print(BLUE + "Enter attraction name: " + RESET);
                    String attraction = localScanner.nextLine().trim();
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.checkAttractionSafety(attraction);
                    break;
                    
                case "5":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.trackRepeatOffenders();
                    break;
                
                case "6":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.criminalCategoryAnalysis();
                    break;

                case "7":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.criminalsNearAttractions();
                    break;
                    
                case "0":
                    stayInCategory = false;
                    displayMainMenu();
                    break;
                    
                default:
                    System.out.println(RED + "Invalid choice." + RESET);
            }
        }
      
    }

    private static void runAirbnbCategory() {
        Scanner localScanner = new Scanner(System.in);
        boolean stayInCategory = true;
        
        while (stayInCategory) {
            System.out.println(MAGENTA + "\n--- AIRBNB MARKET ANALYTICS ---" + RESET);
            System.out.println(CYAN + "1. Amenity-Booking Analysis" + RESET);
            System.out.println(CYAN + "2. Amenity-pricing impact" + RESET);
            System.out.println(CYAN + "3. Host-guest overlap" + RESET);
            System.out.println(CYAN + "4. Review-Booking analysis" + RESET);
            System.out.println(CYAN + "5. Unbooked Listings" + RESET);
            System.out.println(CYAN + "6. Busiest Booking months" + RESET);
            System.out.println(CYAN + "7. Most booked property type" + RESET);
            System.out.println(CYAN + "8. Most expensive property type" + RESET);
            System.out.println(CYAN + "9. Cheapest V/s Most expensive listing type" + RESET);
            System.out.println(CYAN + "0. Back to main menu" + RESET);
            System.out.print(BLUE + "Choice (1-9, 0 to exit): " + RESET);
            
            String choice = localScanner.nextLine().trim();
            
            switch (choice) {
                    
                case "1":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.analyzeAmenityPopularity();
                    break;
                    
                case "2":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.analyzeAmenityPricing();
                    break;
                    
                case "3":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.findHostGuestOverlap();
                    break;
                    
                case "4":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.analyzeReviewImpact();
                    break;

                case "5":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.unbookedListings();
                    break;
                    
                case "6":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.busiestBookingMonth();
                    break;
                    
                case "7":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.propertyTypePricesByNeighbourhood();
                    break;
                    
                case "8":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.mostBookedPropertyTypes();
                    break;
                    
                case "9":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.priceRangeByPropertyType();
                    break;
                    
                case "0":
                    stayInCategory = false;
                    displayMainMenu();
                    break;
                    
                default:
                    System.out.println(RED + "Invalid choice." + RESET);
            }
        }
        
    }
    private static void runTourismCategory() {
        Scanner localScanner = new Scanner(System.in);
        boolean stayInCategory = true;
        
        while (stayInCategory) {
            System.out.println(MAGENTA + "\n--- TOURISM & VISITOR INSIGHTS ---" + RESET);
            System.out.println(CYAN + "1. Guest attraction patterns" + RESET);
            System.out.println(CYAN + "2. Attraction price influence" + RESET);
            System.out.println(CYAN + "3. Most visited spots" + RESET);
            System.out.println(CYAN + "0. Back to main menu" + RESET);
            System.out.print(BLUE + "Choice (1-3, 0 to exit): " + RESET);
            
            String choice = localScanner.nextLine().trim();
            
            switch (choice) {
                case "1":
                    System.out.print(BLUE + "\nEnter how many rows would you like to see: " + RESET);
                    try {
                        int limit = Integer.parseInt(localScanner.nextLine().trim());
                        System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                        db.trackGuestAttractionActivity(limit);
                    } catch (NumberFormatException e) {
                        System.out.println(RED + "Please enter a valid number" + RESET);
                    }
                    break;
                    
                case "2":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.analyzeAttractionImpactOnPrices();
                    break;
                    
                case "3":
                    System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                    db.findMostVisitedAttractions();
                    break;
                    
                case "0":
                    stayInCategory = false;
                    displayMainMenu();
                    break;
                    
                default:
                    System.out.println(RED + "Invalid choice." + RESET);
            }
        }
        
    }

    private static void runCrossAnalysisCategory() {
        Scanner localScanner = new Scanner(System.in);
        boolean stayInCategory = true;
        
        while (stayInCategory) {
            System.out.println(MAGENTA + "\n--- CROSS-DOMAIN ANALYSIS ---" + RESET);
            System.out.println(CYAN + "1. Comprehensive safety profiles" + RESET);
            System.out.println(CYAN + "0. Back to main menu" + RESET);
            System.out.print(BLUE + "Choice (1 or 0): " + RESET);
            
            String choice = localScanner.nextLine().trim();
            
            if (choice.equals("1")) {
                System.out.println(GREEN + "\n \n Fetching requested info....\n \n" + RESET);
                db.generateAreaSafetyProfiles();
            } else if (choice.equals("0")) {
                stayInCategory = false;
                displayMainMenu();
            } else {
                System.out.println(RED + "Invalid choice." + RESET);
            }
        }
        
    }

    private static void deployDatabase() {
        try {
            System.out.println(YELLOW + "Deleting Toronto database tables..." + RESET);
            System.out.println(GREEN + "Creating Toronto database tables..." + RESET);
            db.executeSqlFile("toronto_db.sql");
            System.out.println(GREEN + "Database deployment completed." + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Deployment error: " + e.getMessage() + RESET);
        }
    }

    private static void reloadData() {
        deployDatabase();
        try {
            System.out.println(GREEN + "Loading data..." + RESET);
            System.out.println(GREEN + "Data reload completed." + RESET);
            po.loadConfigAndPopulate();
        } catch (Exception e) {
            System.out.println(RED + "Data loading error: " + e.getMessage() + RESET);
        }
    }
   
}
