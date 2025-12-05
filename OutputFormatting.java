import java.util.*;

public class OutputFormatting {

    public OutputFormatting() {
    }

    public void displayWithPagination(List<String> rows, int pageSize) {
        Scanner scanner = new Scanner(System.in);
        int totalRows = rows.size();
        int totalPages = (int) Math.ceil((double) totalRows / pageSize);
        int currentPage = 1;

        while (true) {
            int start = (currentPage - 1) * pageSize;
            int end = Math.min(start + pageSize, totalRows);

            
            System.out.println("--------------------------------------------------");
            for (int i = start; i < end; i++) {
                System.out.println(rows.get(i));
            }
            System.out.println("--------------------------------------------------");

            if (currentPage < totalPages) {
                System.out.println("\nPage " + currentPage + " of " + totalPages);
                System.out.print("Press Enter or type something and then press Enter to view the next page or type 'exit' to quit: ");
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                currentPage++;
            } else {
                System.out.println("End of results.");
                break;
            }
        }
    }
}

