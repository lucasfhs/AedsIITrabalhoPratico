import sys
import os

def read_tsp_file(filename):
    with open(filename, 'r') as file:
        dimension = 0
        edge_weights = []
        edge_format = None

        for line in file:
            if line.startswith("DIMENSION"):
                dimension = int(line.split(":")[1].strip())
            elif line.startswith("EDGE_WEIGHT_TYPE"):
                edge_format = line.split(":")[1].strip()
            elif line.startswith("EDGE_WEIGHT_SECTION"):
                break
        
        for line in file:
            if line.strip() == "EOF":
                break
            edge_weights.extend(map(int, line.split()))

    return dimension, edge_weights, edge_format

def get_distance(edge_weights, dimension, i, j, edge_format):
    if i == j:
        return 0
    if edge_format == "UPPER_DIAG_ROW":
        if i > j:
            i, j = j, i
        index = (i * dimension) - (i * (i + 1)) // 2 + (j - i - 1)
    elif edge_format == "LOWER_DIAG_ROW":
        if i < j:
            i, j = j, i
        index = (i * (i - 1)) // 2 + j
    else:
        raise ValueError(f"Formato de peso de borda desconhecido: {edge_format}")
    return edge_weights[index]

def nearest_neighbor_tsp(dimension, edge_weights, edge_format):
    visited = [False] * dimension
    path = []
    current_node = 0
    visited[current_node] = True
    path.append(current_node)

    for _ in range(dimension - 1):
        next_node = None
        min_distance = sys.maxsize
        for j in range(dimension):
            if not visited[j]:
                distance = get_distance(edge_weights, dimension, current_node, j, edge_format)
                if distance < min_distance:
                    min_distance = distance
                    next_node = j
        path.append(next_node)
        visited[next_node] = True
        current_node = next_node

    path.append(path[0])  # Volta ao ponto inicial
    return path

def calculate_tour_length(path, dimension, edge_weights, edge_format):
    length = 0
    for i in range(len(path) - 1):
        length += get_distance(edge_weights, dimension, path[i], path[i + 1], edge_format)
    return length

def main():
    test_cases_dir = "../testCases"
    files = ["si535.tsp", "pa561.tsp", "si1032.tsp"]
    
    for filename in files:
        filepath = os.path.join(test_cases_dir, filename)
        print(f"Processando {filepath}...")
        dimension, edge_weights, edge_format = read_tsp_file(filepath)
        print(f"Dimensão: {dimension}")
        print(f"Formato de peso de borda: {edge_format}")
        tour = nearest_neighbor_tsp(dimension, edge_weights, edge_format)
        tour_length = calculate_tour_length(tour, dimension, edge_weights, edge_format)
        print(f"Rota encontrada para {filename}: {tour}")
        print(f"Distância total: {tour_length}")

if __name__ == "__main__":
    main()
