import java.io.*;
import java.util.*;

public class TSPNearestNeighbor {

    // Função para ler os dados do arquivo TSP
    public static double[][] lerTSP(String caminho) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(caminho));
        String linha;
        List<String> linhas = new ArrayList<>();
        String edgeWeightFormat = null;
        
        // Lê todas as linhas do arquivo e armazena na lista 'linhas'
        while ((linha = br.readLine()) != null) {
            linhas.add(linha);
            // Identifica o formato da matriz de pesos das arestas
            if (linha.startsWith("EDGE_WEIGHT_FORMAT")) {
                edgeWeightFormat = linha.split(":")[1].trim();
            }
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
        
        int pos = 0;
        // Preenche a matriz de adjacência com os dados lidos
        if ("UPPER_DIAG_ROW".equals(edgeWeightFormat)) {
            // Lendo a matriz na forma de diagonal superior
            for (int i = 0; i < dimensao; i++) {
                for (int j = i; j < dimensao; j++) {
                    if (pos < dados.length) {
                        matriz[i][j] = dados[pos];
                        matriz[j][i] = dados[pos];
                        pos++;
                    }
                }
            }
        } else if ("LOWER_DIAG_ROW".equals(edgeWeightFormat)) {
            // Lendo a matriz na forma de diagonal inferior
            for (int i = 0; i < dimensao; i++) {
                for (int j = 0; j <= i; j++) {
                    if (pos < dados.length) {
                        matriz[i][j] = dados[pos];
                        matriz[j][i] = dados[pos];
                        pos++;
                    }
                }
            }
        } else {
            throw new RuntimeException("Formato de matriz não suportado: " + edgeWeightFormat);
        }
        
        return matriz;
    }

    // Função de heurística Vizinho Mais Próximo
    public static Resultado vizinhoMaisProximo(double[][] matriz) {
        int n = matriz.length;
        boolean[] visitado = new boolean[n];
        int[] caminho = new int[n];
        double totalDistancia = 0;
        
        // Começa na primeira cidade (índice 0)
        int atual = 0;
        visitado[atual] = true;
        caminho[0] = atual;
        
        // Itera sobre todas as cidades para construir o caminho
        for (int i = 1; i < n; i++) {
            double menorDist = Double.POSITIVE_INFINITY;
            Integer proximo = null;
            
            // Encontra a cidade não visitada mais próxima
            for (int j = 0; j < n; j++) {
                if (!visitado[j] && matriz[atual][j] < menorDist) {
                    menorDist = matriz[atual][j];
                    proximo = j;
                }
            }
            
            if (proximo == null) {
                throw new RuntimeException("Não foi possível encontrar um próximo ponto. Verifique a matriz de distâncias.");
            }
            
            // Atualiza o caminho e marca a cidade como visitada
            caminho[i] = proximo;
            visitado[proximo] = true;
            totalDistancia += menorDist;
            atual = proximo;
        }
        
        // Adiciona a distância de volta à cidade inicial para fechar o ciclo
        totalDistancia += matriz[atual][caminho[0]];
        
        return new Resultado(caminho, totalDistancia);
    }

    // Classe para armazenar o resultado
    public static class Resultado {
        public int[] caminho;
        public double distancia;

        public Resultado(int[] caminho, double distancia) {
            this.caminho = caminho;
            this.distancia = distancia;
        }
    }

    // Função principal para aplicar a heurística
    public static Resultado aplicarHeuristica(String arquivo) throws IOException {
        double[][] matriz = lerTSP(arquivo);
        
        // Verifica se a matriz contém valores NaN
        for (double[] linha : matriz) {
            for (double valor : linha) {
                if (Double.isNaN(valor)) {
                    throw new RuntimeException("A matriz de distâncias contém valores ausentes.");
                }
            }
        }
        
        return vizinhoMaisProximo(matriz);
    }

    // Função principal para testar com arquivos
    public static void main(String[] args) throws IOException {
        String diretorio = "../../testCases/";
        String[] arquivos = { "si535.tsp", "pa561.tsp", "si1032.tsp" };
        
        try {
            // Redireciona a saída padrão para um arquivo de log
            PrintStream logStream = new PrintStream(new FileOutputStream("tsp_log_nearest.txt"));
            System.setOut(logStream);

            for (String arquivo : arquivos) {
                try {
                    String caminhoArquivo = diretorio + arquivo;

                    // Aplica a heurística ao arquivo TSP
                    long startTime = System.nanoTime();
                    Resultado resultado = aplicarHeuristica(caminhoArquivo);
                    long endTime = System.nanoTime();

                    long duration = (endTime - startTime) / 1_000_000; // Em milissegundos

                    System.out.println("Resultado para " + arquivo + ":");
                    System.out.print("Caminho: ");
                    for (int cidade : resultado.caminho) {
                        System.out.print(cidade + " -> ");
                    }
                    // Fecha o ciclo imprimindo a cidade inicial no final
                    System.out.println(resultado.caminho[0]);
                    System.out.println("Distância total: " + resultado.distancia);
                    System.out.println("Tempo de execução: " + duration + " ms");
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