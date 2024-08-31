# tsp_solver.R

# Função para ler matriz diagonal superior
read_upper_diag <- function(file_path) {
  lines <- readLines(file_path)
  start <- grep("EDGE_WEIGHT_SECTION", lines) + 1
  end <- grep("EOF", lines) - 1
  
  if (length(start) == 0 || length(end) == 0) {
    stop("Não foi possível encontrar as seções EDGE_WEIGHT_SECTION ou EOF no arquivo.")
  }
  
  # Ler e processar os dados
  data <- as.numeric(unlist(strsplit(trimws(lines[start:end]), "\\s+")))
  
  # Verificar se há dados válidos
  if (any(is.na(data))) {
    stop("Dados no arquivo não puderam ser convertidos para números.")
  }
  
  # Extrair a dimensão
  n <- as.numeric(gsub("DIMENSION\\s*:\\s*(\\d+)", "\\1", grep("DIMENSION", lines, value = TRUE)))
  
  if (is.na(n)) {
    stop("Não foi possível encontrar a dimensão no arquivo.")
  }
  
  # Calcular o tamanho esperado da matriz
  matrix_size <- n * (n - 1) / 2
  
  # Verificar se o número de dados é consistente com o tamanho esperado
  if (length(data) != matrix_size) {
    stop(paste("Número de elementos na matriz de distância (", length(data), 
               ") não corresponde ao tamanho esperado (", matrix_size, ").", sep = ""))
  }
  
  # Preencher a matriz triangular superior
  matrix_upper <- matrix(0, n, n)
  index <- 1
  for (i in 1:(n - 1)) {
    for (j in (i + 1):n) {
      matrix_upper[i, j] <- data[index]
      matrix_upper[j, i] <- data[index]
      index <- index + 1
    }
  }
  
  return(matrix_upper)
}

# Função para ler matriz diagonal inferior
read_lower_diag <- function(file_path) {
  lines <- readLines(file_path)
  start <- grep("EDGE_WEIGHT_SECTION", lines) + 1
  end <- grep("EOF", lines) - 1
  
  if (length(start) == 0 || length(end) == 0) {
    stop("Não foi possível encontrar as seções EDGE_WEIGHT_SECTION ou EOF no arquivo.")
  }
  
  # Ler e processar os dados
  data <- as.numeric(unlist(strsplit(trimws(lines[start:end]), "\\s+")))
  
  # Verificar se há dados válidos
  if (any(is.na(data))) {
    stop("Dados no arquivo não puderam ser convertidos para números.")
  }
  
  # Extrair a dimensão
  n <- as.numeric(gsub("DIMENSION\\s*:\\s*(\\d+)", "\\1", grep("DIMENSION", lines, value = TRUE)))
  
  if (is.na(n)) {
    stop("Não foi possível encontrar a dimensão no arquivo.")
  }
  
  # Calcular o tamanho esperado da matriz
  matrix_size <- n * (n + 1) / 2
  
  # Verificar se o número de dados é consistente com o tamanho esperado
  if (length(data) != matrix_size) {
    stop(paste("Número de elementos na matriz de distância (", length(data), 
               ") não corresponde ao tamanho esperado (", matrix_size, ").", sep = ""))
  }
  
  # Preencher a matriz triangular inferior
  matrix_lower <- matrix(0, n, n)
  index <- 1
  for (i in 2:n) {
    for (j in 1:(i - 1)) {
      matrix_lower[i, j] <- data[index]
      matrix_lower[j, i] <- data[index]
      index <- index + 1
    }
  }
  
  return(matrix_lower)
}

# Função para aplicar uma heurística (exemplo: caminho aleatório)
heuristic_tsp <- function(distance_matrix) {
  n <- nrow(distance_matrix)
  path <- sample(1:n, n)
  path
}

# Função para calcular a distância do caminho
calculate_path_distance <- function(path, distance_matrix) {
  n <- length(path)
  distance <- 0
  for (i in 1:(n - 1)) {
    distance <- distance + distance_matrix[path[i], path[i + 1]]
  }
  distance <- distance + distance_matrix[path[n], path[1]]  # Retorno ao início
  return(distance)
}

# Função para processar uma instância do TSP
process_tsp_instance <- function(file_path, is_upper_diag = TRUE) {
  if (is_upper_diag) {
    distance_matrix <- read_upper_diag(file_path)
  } else {
    distance_matrix <- read_lower_diag(file_path)
  }
  
  path <- heuristic_tsp(distance_matrix)
  distance <- calculate_path_distance(path, distance_matrix)
  
  return(list(path = path, distance = distance))
}

# Caminho para a pasta com os arquivos
file_dir <- "../testCases/"

# Leitura dos arquivos e processamento
result_si535 <- process_tsp_instance(paste0(file_dir, "si535.tsp"), is_upper_diag = TRUE)
result_pa561 <- process_tsp_instance(paste0(file_dir, "pa561.tsp"), is_upper_diag = FALSE)
result_si1032 <- process_tsp_instance(paste0(file_dir, "si1032.tsp"), is_upper_diag = TRUE)

# Exibir resultados
print("Resultado para si535.tsp:")
print(result_si535$distance)

print("Resultado para pa561.tsp:")
print(result_pa561$distance)

print("Resultado para si1032.tsp:")
print(result_si1032$distance)
