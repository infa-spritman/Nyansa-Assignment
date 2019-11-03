package nyansa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * L  : number of lines in the file
 * U  : Number of unique urls in the file
 * D  : Number of unique days
 *
 * Time complexity : O ( L + D*Log(D) + D*U*Log(U))
 *
 * as L >> D and U >> D ; we can approx the time complexity to be
 *
 *   O( L + U*Log(U)) ; and if L >> U then it will simply be
 *
 *   O( L )
 */
public class App {

    /**
     * Fills the dataHashMap for the corresponding entries from the file
     * @param filePath String representation of the given input file path
     * @param dateHashMap Map for storing the counts of URL for each given date
     */
    private static void createMap(String filePath,
                                  Map<LocalDate, HashMap<String, Integer>> dateHashMap) {

        try (Stream<String> lines = Files.lines(Paths.get(filePath))) {
            lines.forEach(l -> {
                // Assuming file is not distorted or broken
                String line[] = l.split("\\|");

                // Extracting epochSeconds and URL from each line
                long epochSeconds = Long.parseLong(line[0].trim());
                String URL = line[1].trim();

                // Converting epochSeconds to LocalDate format
                LocalDate localDate = Instant.ofEpochSecond(epochSeconds)
                        .atZone(ZoneId.of("GMT"))
                        .toLocalDate();

                // Storing the counts of each URL for a given date
                dateHashMap.putIfAbsent(localDate, new HashMap<>());

                dateHashMap.computeIfPresent(localDate, (ld, urlMap) -> {
                    urlMap.putIfAbsent(URL, 0);
                    urlMap.computeIfPresent(URL, (url, count) -> count + 1);
                    return urlMap;
                });

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints tdaily summarized report on url hit count, organized daily (mm/dd/yyyy GMT)
     * with the earliest date appearing first
     * @param dateHashMap
     */
    private static void printMap(Map<LocalDate, HashMap<String, Integer>> dateHashMap) {
        final DateTimeFormatter pattern = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        dateHashMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    System.out.println(e.getKey().format(pattern).concat(" GMT"));

                    e.getValue()
                            .entrySet()
                            .stream()
                            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                            .forEach(es -> {
                                System.out.println(new StringBuilder()
                                        .append(es.getKey())
                                        .append(" ")
                                        .append(es.getValue())
                                        .toString());
                            });
                });
    }

    /**
     * Main Funtion
     * @param args command line arguments array
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Invalid number of arguments");
            return;
        }

        Map<LocalDate, HashMap<String, Integer>> dateHashMap = new HashMap<>();
        createMap(args[0], dateHashMap);
        printMap(dateHashMap);
    }
}
