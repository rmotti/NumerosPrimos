import matplotlib.pyplot as plt
import os

def read_execution_times(filename):
    execution_data = {}
    with open(filename, 'r') as file:
        for line in file:
            if "thread(s):" in line:
                parts = line.strip().split()
                threads = int(parts[0])  # Número de threads
                time_ms = int(parts[2])  # Tempo em milissegundos
                
                if threads not in execution_data:
                    execution_data[threads] = []
                execution_data[threads].append(time_ms)
    return execution_data

def plot_execution_times(execution_data):
    plt.figure(figsize=(10, 6))
    
    for threads, times in execution_data.items():
        plt.plot(times, label=f"{threads} thread(s)", marker='o')
    
    plt.title("Tempos de Execução ao Longo das Execuções")
    plt.xlabel("Execução")
    plt.ylabel("Tempo (ms)")
    plt.legend(title="Threads")
    plt.grid(True)
    output_path = os.path.join(os.getcwd(), "grafico_execucao.png")
    plt.savefig(output_path)
    print(f"Gráfico salvo em: {output_path}")
    plt.show()

if __name__ == "__main__":
    filename = "execution_times.txt"
    execution_data = read_execution_times(filename)
    plot_execution_times(execution_data)