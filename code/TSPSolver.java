import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TSPSolver {

    // Método para ler a matriz de distâncias do arquivo TSPLIB
    public static double[][] readDistanceMatrix(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        List<double[]> matrixList = new ArrayList<>();
        boolean matrixStarted = false;
        String edgeWeightFormat = "";

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("NAME") || line.startsWith("COMMENT") ||
                line.startsWith("TYPE") || line.startsWith("DIMENSION") ||
                line.startsWith("EDGE_WEIGHT_TYPE") || line.startsWith("NODE_COORD_TYPE") ||
                line.startsWith("DISPLAY_DATA_TYPE") || line.startsWith("EOF")) {
                if (line.startsWith("EDGE_WEIGHT_FORMAT")) {
                    edgeWeightFormat = line.split(":")[1].trim();
                }
                continue;
            }
            if (line.startsWith("EDGE_WEIGHT_SECTION")) {
                matrixStarted = true;
                continue;
            }
            if (matrixStarted) {
                String[] values = line.split("\\s+");
                double[] row = new double[values.length];
                for (int i = 0; i < values.length; i++) {
                    row[i] = Double.parseDouble(values[i]);
                }
                matrixList.add(row);
            }
        }
        reader.close();

        int size = matrixList.size();
        double[][] matrix = new double[size][size];

        if ("LOWER_DIAG_ROW".equals(edgeWeightFormat)) {
            // Construir matriz para LOWER_DIAG_ROW
            for (int i = 0; i < size; i++) {
                for (int j = 0; j <= i; j++) {
                    matrix[i][j] = matrix[j][i] = matrixList.get(i)[j];
                }
            }
        } else if ("UPPER_DIAG_ROW".equals(edgeWeightFormat)) {
            // Construir matriz para UPPER_DIAG_ROW
            for (int i = 0; i < size; i++) {
                for (int j = i; j < size; j++) {
                    matrix[i][j] = matrix[j][i] = matrixList.get(i)[j - i];
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported edge weight format: " + edgeWeightFormat);
        }

        return matrix;
    }

    // Método para encontrar o tour usando a heurística de Inserção Mínima
    public static int[] findTour(double[][] distanceMatrix) {
        int n = distanceMatrix.length;
        boolean[] visited = new boolean[n];
        List<Integer> tour = new ArrayList<>();

        // Começar com a cidade 0
        tour.add(0);
        visited[0] = true;

        while (tour.size() < n) {
            double minIncrease = Double.MAX_VALUE;
            int bestCity = -1;
            int insertPos = -1;

            for (int i = 0; i < n; i++) {
                if (visited[i]) continue;
                for (int j = 0; j < tour.size(); j++) {
                    int prev = tour.get(j);
                    int next = tour.get((j + 1) % tour.size());
                    double increase = distanceMatrix[prev][i] + distanceMatrix[i][next] - distanceMatrix[prev][next];
                    if (increase < minIncrease) {
                        minIncrease = increase;
                        bestCity = i;
                        insertPos = j + 1;
                    }
                }
            }

            if (bestCity == -1 || insertPos == -1) {
                throw new IllegalStateException("Could not find a valid city to insert.");
            }

            tour.add(insertPos, bestCity);
            visited[bestCity] = true;
        }

        return tour.stream().mapToInt(i -> i).toArray();
    }

    // Método para calcular o comprimento do tour
    public static double calculateTourLength(int[] tour, double[][] distanceMatrix) {
        double length = 0;
        for (int i = 0; i < tour.length; i++) {
            length += distanceMatrix[tour[i]][tour[(i + 1) % tour.length]];
        }
        return length;
    }

    public static void main(String[] args) {
        String[] files = {"../testCases/si535.tsp", "../testCases/pa561.tsp", "../testCases/si1032.tsp"};

        for (String file : files) {
            try {
                double[][] distanceMatrix = readDistanceMatrix(file);
                int[] tour = findTour(distanceMatrix);
                double length = calculateTourLength(tour, distanceMatrix);

                System.out.println("Tour length for " + file + ": " + length);
            } catch (IOException e) {
                System.err.println("Error reading file " + file + ": " + e.getMessage());
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.err.println("Error in processing file " + file + ": " + e.getMessage());
            }
        }
    }
}
