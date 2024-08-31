# Função para ler os dados do arquivo TSP
ler_tsp <- function(caminho) {
  linhas <- readLines(caminho)
  
  # Encontrar a seção onde a matriz de distâncias começa
  inicio <- which(linhas == "EDGE_WEIGHT_SECTION") + 1
  dados_raw <- unlist(strsplit(gsub("\\s+", " ", linhas[inicio:length(linhas)]), " "))
  
  # Remove valores vazios
  dados_raw <- dados_raw[dados_raw != ""]
  dados <- as.numeric(dados_raw)
  
  # Determine o número de cidades
  dimensao <- as.numeric(gsub(".*DIMENSION\\s*:\\s*(\\d+).*", "\\1", linhas[grep("DIMENSION", linhas)]))
  
  if (is.na(dimensao)) {
    stop("Não foi possível determinar a dimensão do problema.")
  }
  
  # Crie a matriz de adjacência
  matriz <- matrix(Inf, nrow = dimensao, ncol = dimensao)
  pos <- 1
  
  for (i in 1:dimensao) {
    for (j in i:dimensao) {
      if (pos <= length(dados)) {
        matriz[i, j] <- dados[pos]
        matriz[j, i] <- dados[pos]
        pos <- pos + 1
      } else {
        matriz[i, j] <- Inf
        matriz[j, i] <- Inf
      }
    }
  }
  
  # Verifica se há valores NA na matriz
  if (any(is.na(matriz))) {
    stop("A matriz de distâncias contém valores ausentes após a leitura.")
  }
  
  return(matriz)
}

# Função de heurística Vizinho Mais Próximo
vizinho_mais_proximo <- function(matriz) {
  n <- nrow(matriz)
  visitado <- rep(FALSE, n)
  caminho <- numeric(n)
  total_distancia <- 0
  
  # Começa do primeiro ponto
  atual <- 1
  visitado[atual] <- TRUE
  caminho[1] <- atual
  
  for (i in 2:n) {
    menor_dist <- Inf
    proximo <- NULL
    
    for (j in 1:n) {
      if (!visitado[j] && !is.na(matriz[atual, j]) && matriz[atual, j] < menor_dist) {
        menor_dist <- matriz[atual, j]
        proximo <- j
      }
    }
    
    if (is.null(proximo)) {
      stop("Não foi possível encontrar um próximo ponto. Verifique a matriz de distâncias.")
    }
    
    caminho[i] <- proximo
    visitado[proximo] <- TRUE
    total_distancia <- total_distancia + menor_dist
    atual <- proximo
  }
  
  # Adiciona a distância de volta ao ponto inicial
  total_distancia <- total_distancia + matriz[atual, caminho[1]]
  
  return(list(caminho = caminho, distancia = total_distancia))
}

# Aplicar heurística em instâncias do problema
aplicar_heuristica <- function(arquivo) {
  matriz <- ler_tsp(arquivo)
  
  # Verifica se a matriz está vazia ou contém valores NaN
  if (any(is.na(matriz))) {
    stop("A matriz de distâncias contém valores ausentes.")
  }
  
  resultado <- vizinho_mais_proximo(matriz)
  return(resultado)
}

# Testar com os arquivos fornecidos e medir tempo
diretorio <- "../testCases/"
arquivos <- c("si535.tsp", "pa561.tsp", "si1032.tsp")

tempos <- numeric(length(arquivos))
resultados <- vector("list", length(arquivos))

for (i in seq_along(arquivos)) {
  caminho <- paste0(diretorio, arquivos[i])
  start_time <- Sys.time()
  resultados[[i]] <- aplicar_heuristica(caminho)
  end_time <- Sys.time()
  
  tempos[i] <- as.numeric(difftime(end_time, start_time, units = "secs"))
  
  cat(sprintf("Resultado para %s:\n", arquivos[i]))
  cat(sprintf("Caminho: %s\n", paste(resultados[[i]]$caminho, collapse = " -> ")))
  cat(sprintf("Distância total: %f\n\n", resultados[[i]]$distancia))
}

# Plotando os resultados com funções nativas do R
pdf("tempo_execucao.pdf", width = 8, height = 5)
barplot(
  tempos,
  names.arg = arquivos,
  col = "skyblue",
  main = "Tempo de Execução por Instância TSP",
  xlab = "Arquivo TSP",
  ylab = "Tempo (segundos)"
)
text(
  x = seq_along(tempos), 
  y = tempos, 
  labels = round(tempos, 2), 
  pos = 3
)
dev.off()

cat("Gráfico salvo como 'tempo_execucao.pdf'.\n")
