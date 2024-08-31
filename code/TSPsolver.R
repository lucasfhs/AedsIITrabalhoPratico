# Carregar o pacote TSP
library(TSP)

# Função para ler dados TSP e criar a matriz de distâncias
ler_tsp_matriz <- function(caminho) {
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
  
  return(matriz)
}

# Função para resolver o TSP usando o pacote TSP
resolver_tsp <- function(arquivo) {
  matriz <- ler_tsp_matriz(arquivo)
  tsp <- TSP(as.dist(matriz))
  solução <- solve_TSP(tsp)
  
  caminho <- as.integer(labels(solução))
  distancia <- tour_length(solução)
  
  return(list(caminho = caminho, distancia = distancia))
}

# Testar com os arquivos fornecidos
diretorio <- "../testCases/"
arquivos <- c("si535.tsp", "pa561.tsp", "si1032.tsp")

resultados <- lapply(paste0(diretorio, arquivos), resolver_tsp)

for (i in seq_along(resultados)) {
  cat(sprintf("Resultado para %s:\n", arquivos[i]))
  cat(sprintf("Caminho: %s\n", paste(resultados[[i]]$caminho, collapse = " -> ")))
  cat(sprintf("Distância total: %f\n\n", resultados[[i]]$distancia))
}
