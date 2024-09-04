import java.io.*;
import java.util.*;

public class TSPCheapestInsertion {

    // Função para ler os dados do arquivo TSP
    public static double[][] lerTSP(String caminho) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(caminho));
        String linha;
        List<String> linhas = new ArrayList<>();
        
        // Lê todas as linhas do arquivo e armazena na lista 'linhas'
        while ((linha = br.readLine()) != null) {
            linhas.add(linha);
        }
        br.close();
        
        // Encontra o índice da seção de pesos das arestas
        int inicio = linhas.indexOf("EDGE_WEIGHT_SECTION") + 1;
        List<String> dadosRawList = linhas.subList(inicio, linhas.size());
        
        // Cria uma lista para armazenar os dados válidos
        List<Double> dadosList = new ArrayList<>();
        
        // Itera sobre cada linha e processa os dados
        for (String dadosRaw : dadosRawList) {
            String[] valores = dadosRaw.trim().split("\\s+");
            for (String valor : valores) {
                if (!valor.isEmpty()) {
                    try {
                        dadosList.add(Double.parseDouble(valor));
                    } catch (NumberFormatException e) {
                        // Ignora valores que não podem ser convertidos para double
                        System.err.println("Aviso: Valor ignorado por erro de formato: " + valor);
                    }
                }
            }
        }
        
        // Converte a lista para array
        double[] dados = dadosList.stream().mapToDouble(Double::doubleValue).toArray();
        
        // Determina o número de cidades a partir da linha 'DIMENSION'
        int dimensao = linhas.stream()
            .filter(l -> l.startsWith("DIMENSION"))
            .map(l -> l.split(":")[1].trim())
            .mapToInt(Integer::parseInt)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Não foi possível determinar a dimensão do problema."));
        
        // Cria a matriz de adjacência e inicializa com valores infinitos
        double[][] matriz = new double[dimensao][dimensao];
        for (int i = 0; i < dimensao; i++) {
            Arrays.fill(matriz[i], Double.POSITIVE_INFINITY);
        }
        
        // Preenche a matriz de adjacência com os dados lidos
        int pos = 0;
        for (int i = 0; i < dimensao; i++) {
            for (int j = i; j < dimensao; j++) {
                if (pos < dados.length) {
                    matriz[i][j] = dados[pos];
                    matriz[j][i] = dados[pos];
                    pos++;
                } else {
                    matriz[i][j] = Double.POSITIVE_INFINITY;
                    matriz[j][i] = Double.POSITIVE_INFINITY;
                }
            }
        }
        
        return matriz;
    }

    // Função de heurística Cheapest Insertion
    public static Resultado cheapestInsertion(double[][] matriz) {
        int n = matriz.length;
        boolean[] inserido = new boolean[n];
        List<Integer> tour = new ArrayList<>();
        double totalDistancia = 0.0;

        // Passo 1: Seleciona a cidade inicial (0) e a adiciona ao tour
        int inicial = 0;
        inserido[inicial] = true;
        tour.add(inicial);

        // Passo 2: Encontra a cidade mais próxima da inicial e adiciona ao tour
        double minDist = Double.POSITIVE_INFINITY;
        int maisProxima = -1;
        for (int i = 1; i < n; i++) {
            if (matriz[inicial][i] < minDist) {
                minDist = matriz[inicial][i];
                maisProxima = i;
            }
        }
        if (maisProxima == -1) {
            throw new RuntimeException("Não foi possível encontrar uma cidade para iniciar o tour.");
        }
        inserido[maisProxima] = true;
        tour.add(maisProxima);
        totalDistancia += minDist;

        // Passo 3: Encontra a terceira cidade que fecha o ciclo inicial
        minDist = Double.POSITIVE_INFINITY;
        int terceira = -1;
        for (int i = 0; i < n; i++) {
            if (!inserido[i] && (matriz[inicial][i] + matriz[maisProxima][i]) < minDist) {
                minDist = matriz[inicial][i] + matriz[maisProxima][i];
                terceira = i;
            }
        }
        if (terceira == -1) {
            throw new RuntimeException("Não foi possível encontrar a terceira cidade para fechar o ciclo.");
        }
        inserido[terceira] = true;
        tour.add(terceira);
        tour.add(inicial); // Fecha o ciclo
        totalDistancia += matriz[maisProxima][terceira] + matriz[terceira][inicial];

        // Passo 4: Insere as cidades restantes no tour
        for (int i = 0; i < n; i++) {
            if (!inserido[i]) {
                double melhorAumento = Double.POSITIVE_INFINITY;
                int melhorPosicao = -1;

                // Tenta inserir a cidade i entre cada par de cidades no tour
                for (int j = 0; j < tour.size() - 1; j++) {
                    int cidade1 = tour.get(j);
                    int cidade2 = tour.get(j + 1);
                    double aumento = matriz[cidade1][i] + matriz[i][cidade2] - matriz[cidade1][cidade2];
                    if (aumento < melhorAumento) {
                        melhorAumento = aumento;
                        melhorPosicao = j + 1;
                    }
                }

                // Insere a cidade na posição que causa o menor aumento de custo
                if (melhorPosicao != -1) {
                    tour.add(melhorPosicao, i);
                    totalDistancia += melhorAumento;
                    inserido[i] = true;
                } else {
                    throw new RuntimeException("Não foi possível inserir a cidade " + i + " no tour.");
                }
            }
        }

        // Calcula o custo total do tour completo
        totalDistancia = calculateTourCost(tour, matriz);

        return new Resultado(tour, totalDistancia);
    }

    // Método para calcular o custo total de um tour
    private static double calculateTourCost(List<Integer> tour, double[][] matriz) {
        double cost = 0.0;
        for (int i = 0; i < tour.size() - 1; i++) {
            cost += matriz[tour.get(i)][tour.get(i + 1)];
        }
        cost += matriz[tour.get(tour.size() - 1)][tour.get(0)]; // Retorna à cidade inicial
        return cost;
    }

    // Classe para armazenar o resultado
    public static class Resultado {
        public List<Integer> caminho;
        public double distancia;

        public Resultado(List<Integer> caminho, double distancia) {
            this.caminho = caminho;
            this.distancia = distancia;
        }
    }

    // Função principal para aplicar a heurística
    public static Resultado aplicarHeuristica(String arquivo) throws IOException {
        double[][] matriz = lerTSP(arquivo);
        
        // Verifica se a matriz contém valores NaN ou Infinito
        for (double[] linha : matriz) {
            for (double valor : linha) {
                if (Double.isNaN(valor) || Double.isInfinite(valor)) {
                    throw new RuntimeException("A matriz de distâncias contém valores inválidos.");
                }
            }
        }
        
        return cheapestInsertion(matriz);
    }

    // Função principal para testar com arquivos
    public static void main(String[] args) {
        String diretorio = "../../testCases/";
        String[] arquivos = { "si535.tsp", "pa561.tsp", "si1032.tsp" };
        
        try {
            // Redireciona a saída padrão para um arquivo de log
            PrintStream logStream = new PrintStream(new FileOutputStream("tsp_log.txt"));
            System.setOut(logStream);

            for (String arquivo : arquivos) {
                try {
                    String caminhoArquivo = diretorio + arquivo;

                    // Aplica a heurística ao arquivo TSP
                    Resultado resultado = aplicarHeuristica(caminhoArquivo);
                    System.out.println("Resultado para " + arquivo + ":");
                    System.out.print("Caminho: ");
                    for (int cidade : resultado.caminho) {
                        System.out.print(cidade + " -> ");
                    }
                    System.out.println(resultado.caminho.get(0)); // Fecha o ciclo
                    System.out.println("Distância total: " + resultado.distancia);
                    System.out.println();
                } catch (IOException e) {
                    System.err.println("Erro ao ler o arquivo " + arquivo + ": " + e.getMessage());
                } catch (RuntimeException e) {
                    System.err.println("Erro ao aplicar a heurística no arquivo " + arquivo + ": " + e.getMessage());
                }
            }
            
            // Fechar o stream do arquivo
            logStream.close();  

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }     
    }
}