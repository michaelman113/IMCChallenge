import java.io.*;
import java.util.*;

public class TechChallenge {
    public static void main(String[] args) {
        // Read the file as a list of ExchangePair
        List<ExchangePair> edges = readTSV();
        List<ExchangePair> result = new ArrayList<>();

        // Select edges to buy
        for (ExchangePair edge : edges) {
            // edges with latency=0 do not directly connect, and cannot be in the result
            if (edge.getLatency() != 0) {
                result.add(edge);
            }
        }

        // Compute score
        System.out.println("Score: " + scoreSubmission(edges, result));

        // Write the list of ExchangePair to a TSV file
        writeTSV(result);
    }

    public static List<ExchangePair> readTSV() {
        List<ExchangePair> records = new ArrayList<>();
        String inputLocation = System.getProperty("user.dir") + "/data/dataset.tsv";
        try (BufferedReader br = new BufferedReader(new FileReader(inputLocation))) {
            String line;
            while ((line = br.readLine()) != null) {
                StringTokenizer tokenizer = new StringTokenizer(line, "\t");
                records.add(new ExchangePair(
                        tokenizer.nextToken(),
                        tokenizer.nextToken(),
                        Long.parseLong(tokenizer.nextToken()),
                        Long.parseLong(tokenizer.nextToken()),
                        Long.parseLong(tokenizer.nextToken())
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return records;
    }

    public static void writeTSV(List<ExchangePair> exchangePairs) {
        String outputLocation = System.getProperty("user.dir") + "/data/solution.tsv";
        File file = new File(outputLocation);

        try {
            FileWriter fileWriter = new FileWriter(file, false);
            for (ExchangePair exchangePair: exchangePairs) {
                fileWriter.write(exchangePair.getCityA() + "\t" + exchangePair.getCityB() + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long scoreSubmission(List<ExchangePair> allExchangePairs, List<ExchangePair> boughtExchangePairs) {
        // Get all cities
        Set<String> allCities = new HashSet<>();
        for (ExchangePair exchangePair: allExchangePairs) {
            allCities.add(exchangePair.getCityA());
            allCities.add(exchangePair.getCityB());
        }

        // Create mapping City -> Integer
        int index = 0;
        Map<String, Integer> cityToIndex = new HashMap<>();
        for (String city: allCities) {
            cityToIndex.put(city, index++);
        }

        // Initialize distances & messages
        int n = allCities.size();
        long[][] distances = new long[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(distances[i], 10 * 10000);
        }

        for (ExchangePair edge: boughtExchangePairs) {
            int indexCityA = cityToIndex.get(edge.getCityA());
            int indexCityB = cityToIndex.get(edge.getCityB());
            long latency = edge.getLatency();
            if (latency == 0) {
                throw new RuntimeException("Attempting to connect exchange pair with latency 0");
            }
            distances[indexCityA][indexCityB] = edge.getLatency();
            distances[indexCityB][indexCityA] = edge.getLatency();
        }

        // Determine messages
        long[][] messages = new long[n][n];
        for (ExchangePair edge: allExchangePairs) {
            int indexCityA = cityToIndex.get(edge.getCityA());
            int indexCityB = cityToIndex.get(edge.getCityB());
            messages[indexCityA][indexCityB] = edge.getMessages();
            messages[indexCityB][indexCityA] = edge.getMessages();
        }

        // Perform floyd warshall
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < n; i++) {
                for (int k = 0; k < n; k++) {
                    distances[i][k] = Math.min(distances[i][k], distances[i][j] + distances[j][k]);
                }
            }
        }

        // Compute score
        long profit = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                profit += Math.max(0, 10000 - distances[i][j]) * messages[i][j];
            }
        }

        long cost = 0;
        for (ExchangePair edge: boughtExchangePairs) {
            cost += edge.getCost();
        }

        return profit / 2 - cost;
    }

    public static class ExchangePair {
        private final String cityA, cityB;
        private final long latency;
        private final long cost;
        private final long messages;

        public ExchangePair(String cityA, String cityB, long latency, long cost, long messages) {
            this.cityA = cityA;
            this.cityB = cityB;
            this.latency = latency;
            this.cost = cost;
            this.messages = messages;
        }

        @Override
        public String toString()
        {
            return "ExchangePair{" +
                "cityA='" + cityA + '\'' +
                ", cityB='" + cityB + '\'' +
                ", latency=" + latency +
                ", cost=" + cost +
                ", messages=" + messages +
                '}';
        }

        public String getCityA() {
            return cityA;
        }

        public String getCityB() {
            return cityB;
        }

        public long getLatency() {
            return latency;
        }

        public long getCost() {
            return cost;
        }

        public long getMessages() {
            return messages;
        }
    }
}
