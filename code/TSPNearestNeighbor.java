import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TSPNearestNeighbor {

    public static void main(String[] args) {
        String filename = "../TestCases/si535.tsp";  // Substitua pelo nome do seu arquivo .tsp
        try {
            TSPData tspData = readTSPFile(filename);
            List<Integer> tour = nearestNeighbor(tspData.edgeWeights, 0);  // Começando pelo nó 0
            System.out.println("Rota: " + tour);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Classe auxiliar para armazenar os dados do TSP
    static class TSPData {
        int dimension;
        int[][] edgeWeights;
    }

    // Função para ler o arquivo .tsp e extrair os dados
    public static TSPData readTSPFile(String filename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        TSPData tspData = new TSPData();
        
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("DIMENSION")) {
                tspData.dimension = Integer.parseInt(line.split(":")[1].trim());
                tspData.edgeWeights = new int[tspData.dimension][tspData.dimension];
            } else if (line.startsWith("EDGE_WEIGHT_SECTION")) {
                break;
            }
        }
        
        int rowIndex = 0;
        while ((line = reader.readLine()) != null && !line.equals("EOF")) {
            String[] parts = line.trim().split("\\s+");
            for (int i = 0; i < parts.length; i++) {
                int colIndex = rowIndex + i;
                tspData.edgeWeights[rowIndex][colIndex] = Integer.parseInt(parts[i]);
                tspData.edgeWeights[colIndex][rowIndex] = tspData.edgeWeights[rowIndex][colIndex]; // Matriz simétrica
            }
            rowIndex++;
        }
        
        reader.close();
        return tspData;
    }

    // Função para resolver o TSP usando a heurística do vizinho mais próximo
    public static List<Integer> nearestNeighbor(int[][] matrix, int startNode) {
        int n = matrix.length;
        boolean[] visited = new boolean[n];
        List<Integer> tour = new ArrayList<>();
        
        int currentNode = startNode;
        visited[currentNode] = true;
        tour.add(currentNode);
        
        while (tour.size() < n) {
            int nearestNeighbor = -1;
            int nearestDistance = Integer.MAX_VALUE;
            
            for (int nextNode = 0; nextNode < n; nextNode++) {
                if (!visited[nextNode] && matrix[currentNode][nextNode] < nearestDistance) {
                    nearestNeighbor = nextNode;
                    nearestDistance = matrix[currentNode][nextNode];
                }
            }
            
            tour.add(nearestNeighbor);
            visited[nearestNeighbor] = true;
            currentNode = nearestNeighbor;
        }
        
        tour.add(startNode);  // Retorne ao ponto inicial para fechar o ciclo
        return tour;
    }
}
