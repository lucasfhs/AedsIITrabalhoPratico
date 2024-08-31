package BruteForce;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TSPBruteForce {

    // Função que calcula a distância total de uma rota específica
    public static int calculateDistance(int[][] distances, List<Integer> path) {
        int totalDistance = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            totalDistance += distances[path.get(i)][path.get(i + 1)];
        }
        // Adiciona a distância de retorno à cidade inicial
        totalDistance += distances[path.get(path.size() - 1)][path.get(0)];
        return totalDistance;
    }

    // Função que gera todas as permutações possíveis e encontra a rota de menor distância
    public static List<Integer> findShortestRoute(int[][] distances) {
        List<Integer> cities = new ArrayList<>();
        for (int i = 0; i < distances.length; i++) {
            cities.add(i);
        }

        List<Integer> bestPath = new ArrayList<>(cities);
        int minDistance = calculateDistance(distances, bestPath);

        do {
            int currentDistance = calculateDistance(distances, cities);
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                bestPath = new ArrayList<>(cities);
            }
        } while (nextPermutation(cities));

        System.out.println("Menor distância: " + minDistance);
        return bestPath;
    }

    // Função para gerar uma matriz de distâncias aleatória
    public static int[][] generateRandomInstance(int n) {
        Random rand = new Random();
        int[][] distances = new int[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int distance = rand.nextInt(100) + 1; // Distâncias entre 1 e 100
                distances[i][j] = distance;
                distances[j][i] = distance;
            }
        }

        return distances;
    }

    // Implementação de nextPermutation para gerar a próxima permutação lexicográfica
    public static boolean nextPermutation(List<Integer> data) {
        int i = data.size() - 1;
        while (i > 0 && data.get(i - 1) >= data.get(i))
            i--;

        if (i <= 0)
            return false;

        int j = data.size() - 1;
        while (data.get(j) <= data.get(i - 1))
            j--;

        swap(data, i - 1, j);

        j = data.size() - 1;
        while (i < j) {
            swap(data, i, j);
            i++;
            j--;
        }
        return true;
    }

    // Função auxiliar para troca de elementos na lista
    private static void swap(List<Integer> data, int i, int j) {
        int temp = data.get(i);
        data.set(i, data.get(j));
        data.set(j, temp);
    }

    public static void main(String[] args) {
        try {
            // Redireciona a saída padrão para um arquivo de log
            PrintStream logStream = new PrintStream(new FileOutputStream("tsp_log.txt"));
            System.setOut(logStream);

            int maxCities = 12;
            List<Long> executionTimes = new ArrayList<>();

            for (int numCities = 2; numCities <= maxCities; numCities++) {
                int[][] distances = generateRandomInstance(numCities);

                long startTime = System.nanoTime();
                findShortestRoute(distances);
                long endTime = System.nanoTime();

                long duration = (endTime - startTime) / 1_000_000; // Em milissegundos
                executionTimes.add(duration);

                System.out.println("Cidades: " + numCities + " | Tempo de execução: " + duration + " ms");
            }

            System.out.println("\nTempos de execução para diferentes instâncias:");
            for (int i = 0; i < executionTimes.size(); i++) {
                System.out.println("Cidades: " + (i + 2) + " | Tempo: " + executionTimes.get(i) + " ms");
            }

            // Fechar o stream do arquivo
            logStream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
