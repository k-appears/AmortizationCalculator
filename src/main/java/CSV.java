import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Disclaimer: Line can not be longer than 512 chars
 */
class CSV {

    private static final int MBYTE_PER_BYTE = (1024 * 1024);

    /**
     * It is required to process all lenders.
     *
     * @param filePath to CSV file
     * @return List of lenders in CSV file
     * @throws IOException if reading the file
     */
    List<Lender> extractLenders(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("filePath must not be empty or null");
        }
        List<Lender> result = new LinkedList<>();
        // BufferedReader is faster than Scanner
        try (BufferedReader in = new BufferedReader(new FileReader(filePath), 2 * MBYTE_PER_BYTE)) {
            // Loading in memory one line at a time
            String line = in.readLine();
            if (line == null) {
                throw new IllegalArgumentException("File is empty: " + filePath);
            }
            if (!line.equals("Lender,Rate,Available")) {
                throw new IllegalArgumentException("First line of " + filePath + "is not: 'Lender,Rate,Available'");
            }
            line = in.readLine();
            while (line != null && isMemoryAvailable()) {
                if (line.length() >= 512) {
                    throw new IllegalArgumentException("Line can not be longer than 512 chars");
                }
                List<String> fields = Arrays.asList(line.split(","));
                if (fields.size() > 3) {
                    throw new IllegalArgumentException(
                            "Content of " + filePath + " is incorrect, expected 3 words separated by ,: " + fields);
                }
                BigDecimal rate = new BigDecimal(fields.get(1));
                if (rate.doubleValue() < 0) {
                    throw new IllegalArgumentException("rate of " + fields + " must be positive number");
                }
                BigDecimal amount = new BigDecimal(fields.get(2));
                if (amount.doubleValue() < 0) {
                    throw new IllegalArgumentException("amount of " + fields + " must be positive number");
                }
                result.add(new Lender(fields.get(0), rate, amount));

                line = in.readLine();
            }
        }
        return result;
    }

    /**
     * When cache mechanism is implemented, the system could be memory overflown, this method will avoid it.
     *
     * @return if there is memory available in the system
     */
    private static boolean isMemoryAvailable() {
        long totalMemoryInUse = Runtime.getRuntime().totalMemory();
        long freeMemory = Runtime.getRuntime().freeMemory();
        long memoryUsed = totalMemoryInUse - freeMemory;
        double memoryUsedPercentage = ((memoryUsed * 1.0) / totalMemoryInUse) * 100;

        //Threshold for maximum utilization of the JVM Ram available, {@link #isMemoryAvailable()}.
        double MEMORY_PERCENTAGE_ALLOWED = 90;
        if (memoryUsedPercentage > MEMORY_PERCENTAGE_ALLOWED) {
            System.out.println("Memory threshold exceeded:");
            long maxMemory = Runtime.getRuntime().maxMemory();
            System.out.println("Maximum memory JVM will attempt to use (MB): " + (maxMemory == Long.MAX_VALUE ?
                    "no limit" :
                    maxMemory / MBYTE_PER_BYTE));
            System.out.println("Amount of free memory available to the JVM (MB): " + freeMemory / MBYTE_PER_BYTE);
            System.out.println("Total memory currently in use by the JVM (MB): " + totalMemoryInUse / MBYTE_PER_BYTE);
            System.out.println("Memory use " + Math.floor(memoryUsedPercentage * 100) / 100 + " %");
            System.out.println("Memory threshold " + MEMORY_PERCENTAGE_ALLOWED + " %");
            return false;
        }
        return true;
    }
}
