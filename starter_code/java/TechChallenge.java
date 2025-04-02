import java.io.*;
import java.util.*;

public class TechChallenge {
    public static void main(String[] args) {
        // Read the file as a list of ExchangePair
        List<ExchangePair> edges = readTSV();
        List<ExchangePair> result = new ArrayList<>();
        
        // implementation
        // result = findConnection(edges);

        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).latency > 0 && Math.random() < 0.40) {
                result.add(edges.get(i));
            }
        }

        // Select edges to buy
        // for (ExchangePair edge : edges) {
        //     // edges with latency=0 do not directly connect, and cannot be in the result
        //     if (edge.getLatency() != 0) {
        //         result.add(edge);
        //     }
        // }

        // Compute score
        while (scoreSubmission(edges, result) < 800000000) {
            result = new ArrayList<>();
            for (int i = 0; i < edges.size(); i++) {
                if (edges.get(i).latency > 0 && Math.random() < 0.40) {
                    result.add(edges.get(i));
                }
            }
        }
        System.out.println("Score: " + scoreSubmission(edges, result));

        // Write the list of ExchangePair to a TSV file
        writeTSV(result);
    }

    public static List<ExchangePair> findConnection(List<ExchangePair> edges) {
        List<ExchangePair> result = new ArrayList<>();

        long[] profit = new long[edges.size()];

        for (int i = 0; i < edges.size(); i++) {
            if (edges.get(i).latency != 0) {
                profit[i] = Math.max(edges.get(i).messages * Math.max(10000 - edges.get(i).latency, 0) - edges.get(i).cost, 0);
                // System.out.println(profit[i]);
            }
        }

        int maxProfitIndex = -1;
        long maxProfit = profit[0];
        for (int i = 1; i < profit.length; i++) {
            if (profit[i] > maxProfit) {
                maxProfit = profit[i];
                maxProfitIndex = i;
            }
        }

        result.add(edges.get(maxProfitIndex));

        // for (int j = 0; j < n; j++) {
        //     for (int i = 0; i < n; i++) {
        //         for (int k = 0; k < n; k++) {
        //             distances[i][k] = Math.min(distances[i][k], distances[i][j] + distances[j][k]);
        //         }
        //     }
        // }
        
        HashMap<String, Integer> cityToIndex = new HashMap<>();
        int index = 0;
        for (int i = 0; i < edges.size(); i++) {
            if (cityToIndex.get(edges.get(i).cityA) == null) {
                cityToIndex.put(edges.get(i).cityA, index++);
            }
            if (cityToIndex.get(edges.get(i).cityB) == null) {
                cityToIndex.put(edges.get(i).cityB, index++);
            }
        }

        long[][] graph = new long[index][index];
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph.length; j++) {
                graph[i][j] = Long.MAX_VALUE;
            }
        }
        // System.out.println(maxProfit);
        graph[cityToIndex.get(result.get(0).cityA)][cityToIndex.get(result.get(0).cityB)] = result.get(0).cost;
        graph[cityToIndex.get(result.get(0).cityB)][cityToIndex.get(result.get(0).cityA)] = result.get(0).cost;

        List<String> cities = new ArrayList<>();
        HashSet<String> noDuplicates = new HashSet<>();

        for (int i = 0; i < edges.size(); i++) {
            noDuplicates.add(edges.get(i).cityA);
            noDuplicates.add(edges.get(i).cityB);
        }

        cities.addAll(noDuplicates);

        for (int i = 0; i < cities.size() - 1; i++) {
            for (int j = i + 1; j < cities.size(); j++) {
                long directPathProfit = Long.MAX_VALUE;
                ExchangePair edge = null;
                for (int k = 0; k < edges.size(); k++) {
                    if (((cities.get(i).equals(edges.get(k).cityA) && cities.get(j).equals(edges.get(k).cityB)) || (cities.get(i).equals(edges.get(k).cityB) && cities.get(j).equals(edges.get(k).cityA))) && edges.get(k).latency > 0) {
                        directPathProfit = edges.get(k).cost;
                        edge = edges.get(k);
                        // System.out.println(edge == null); 
                        break;
                    }
                }
                // System.out.println(directPathProfit);
                long indirectPathProfit = 0;

                for (int k = 0; k < graph.length; k++) {
                    for (int l = 0; l < graph[k].length; l++) {
                        for (int m = 0; m < graph.length; m++) {
                            // if (graph[l][k] < 0 && graph[k][m] < 0) {
                            if (graph[l][k] < Long.MAX_VALUE && graph[k][m] < Long.MAX_VALUE) {
                                graph[l][m] = Math.min(graph[l][m], graph[l][k] + graph[k][m]);
                            }
                                // if (graph[l][m] > 0 && graph[l][m] < Long.MAX_VALUE) {
                                //     System.out.println(graph[l][m] + " " + graph[l][k] + " " + graph[k][m]);
                                // }
                            // }
                        }
                    }
                }
                // System.out.println(graph[cityToIndex.get(cities.get(i))][cityToIndex.get(cities.get(j))]);
                indirectPathProfit = graph[cityToIndex.get(cities.get(i))][cityToIndex.get(cities.get(j))];
                // System.out.println(null);
                if (directPathProfit == Long.MAX_VALUE && indirectPathProfit == Long.MAX_VALUE) {
                    continue;
                }
                else {
                    if (directPathProfit < indirectPathProfit) {
                        // System.out.println(edge == null);
                        // System.out.println(indirectPathProfit);
                        // System.out.println(indirectPathProfit);
                        result.add(edge);
                        graph[cityToIndex.get(cities.get(i))][cityToIndex.get(cities.get(j))] = directPathProfit;
                        graph[cityToIndex.get(cities.get(j))][cityToIndex.get(cities.get(i))] = directPathProfit;
                    }
                }
            }
        }

        return result;
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
