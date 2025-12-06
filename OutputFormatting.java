import java.util.*;

public class OutputFormatting {

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

    // Default config – change these if you want different behavior
    private final int maxWidthPerColumn;
    private final int truncateLength;
    private final int paginationThreshold;
    private final int pageSize;

    public OutputFormatting() {
        // maxWidth = 40 chars, truncate = 40 chars, paginate if >50 rows, 25 rows per page
        this(40, 40, 50, 50);
    }

    public OutputFormatting(int maxWidthPerColumn, int truncateLength,
                            int paginationThreshold, int pageSize) {
        this.maxWidthPerColumn = maxWidthPerColumn;
        this.truncateLength = truncateLength;
        this.paginationThreshold = paginationThreshold;
        this.pageSize = pageSize;
    }

    public void displayWithPagination(List<String> headerLines,
                                      List<String> rows,
                                      int pageSize) {
        Scanner scanner = new Scanner(System.in);
        int totalRows = rows.size();
        if (totalRows == 0) {
            System.out.println(YELLOW + "No results."+ RESET);
            return;
        }

        int totalPages = (int) Math.ceil((double) totalRows / pageSize);
        int currentPage = 1;

        while (true) {
            int start = (currentPage - 1) * pageSize;
            int end = Math.min(start + pageSize, totalRows);

            
            if (headerLines != null) {
                for (String h : headerLines) {
                    System.out.println(h);
                }
            }

            for (int i = start; i < end; i++) {
                System.out.println(rows.get(i));
            }
         

            if (currentPage < totalPages) {
                System.out.println("\nPage " + currentPage + " of " + totalPages);
                System.out.print(MAGENTA + "Press Enter (or type anything) for next page, 'exit' to quit: " + RESET);
                String input = scanner.nextLine();
                if (input.equalsIgnoreCase("exit")) {
                    break;
                }
                currentPage++;
            } else {
                System.out.println(GREEN + BOLD + "End of results." + RESET);
                break;
            }
        }
    }


   
    public int[] computeColumnWidths(List<List<String>> table, List<String> headers) {
        int cols = 0;
        if (!table.isEmpty()) {
            cols = table.get(0).size();
        } else if (headers != null) {
            cols = headers.size();
        }

        int[] widths = new int[cols];

        if (headers != null) {
            for (int i = 0; i < headers.size(); i++) {
                String h = headers.get(i) == null ? "" : headers.get(i);
                int len = Math.min(h.length(), maxWidthPerColumn);
                widths[i] = Math.max(widths[i], len);
            }
        }

        for (List<String> row : table) {
            for (int i = 0; i < cols; i++) {
                String val = (row.get(i) == null) ? "NULL" : row.get(i);
                int len = Math.min(val.length(), maxWidthPerColumn);
                if (len > widths[i]) {
                    widths[i] = len;
                }
            }
        }

        return widths;
    }

    public String formatRow(List<String> values, int[] widths) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < values.size(); i++) {
            String val = values.get(i);
            if (val == null) val = "NULL";

            // truncate if too long
            if (val.length() > truncateLength) {
                if (truncateLength > 3) {
                    val = val.substring(0, truncateLength - 3) + "...";
                } else {
                    val = val.substring(0, truncateLength);
                }
            }

            int width = (i < widths.length) ? widths[i] : truncateLength;
            sb.append(String.format("%-" + width + "s", val));

            if (i < values.size() - 1) {
                sb.append(" | ");
            }
        }
        return sb.toString();
    }


    public String buildSeparatorLine(int[] widths) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < widths.length; i++) {
            int width = widths[i];
            for (int j = 0; j < width; j++) {
                sb.append('-');
            }
            if (i < widths.length - 1) {
                sb.append("-+-");
            }
        }
        return sb.toString();
    }

    public void printTable(List<List<String>> table, List<String> headers) {
        if (headers == null || headers.isEmpty()) {
            System.out.println("No headers provided.");
            return;
        }

        if (table == null || table.isEmpty()) {
            // still print header & separator even if empty table
            int[] widths = computeColumnWidths(Collections.emptyList(), headers);
            String headerLine = formatRow(headers, widths);
            String sep = buildSeparatorLine(widths);
            System.out.println(headerLine);
            System.out.println(sep);
            System.out.println("(No rows)");
            return;
        }

        int[] widths = computeColumnWidths(table, headers);
        String headerLine = formatRow(headers, widths);
        String sepLine = buildSeparatorLine(widths);

        // build data lines
        List<String> dataLines = new ArrayList<>();
        for (List<String> row : table) {
            dataLines.add(formatRow(row, widths));
        }

        // If row count is large, paginate
        if (table.size() > paginationThreshold) {
            List<String> headerLines = new ArrayList<>();
            headerLines.add(headerLine);
            headerLines.add(sepLine);
            displayWithPagination(headerLines, dataLines, pageSize);
        } else {
            System.out.println(headerLine);
            System.out.println(sepLine);
            for (String line : dataLines) {
                System.out.println(line);
            }
        }
    }
}
