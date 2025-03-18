import java.io.*;
import java.util.*;
import java.util.concurrent.*;

class NumberWithIndex {
    long number;
    int index;

    public NumberWithIndex(long number, int index) {
        this.number = number;
        this.index = index;
    }
}

class PrimeResult implements Comparable<PrimeResult> {
    long prime;
    int index;

    public PrimeResult(long prime, int index) {
        this.prime = prime;
        this.index = index;
    }

    @Override
    public int compareTo(PrimeResult other) {
        return Integer.compare(this.index, other.index);
    }
}

class PrimeChecker implements Callable<List<PrimeResult>> {
    private final List<NumberWithIndex> chunk;

    public PrimeChecker(List<NumberWithIndex> chunk) {
        this.chunk = chunk;
    }

    @Override
    public List<PrimeResult> call() {
        List<PrimeResult> results = new ArrayList<>();
        for (NumberWithIndex nwi : chunk) {
            if (isPrime(nwi.number)) {
                results.add(new PrimeResult(nwi.number, nwi.index));
            }
        }
        return results;
    }

    private boolean isPrime(long n) {
        if (n <= 1) return false;
        if (n == 2) return true;
        if (n % 2 == 0) return false;
        
        long sqrt = (long) Math.sqrt(n);
        for (long i = 3; i <= sqrt; i += 2) {
            if (n % i == 0) return false;
        }
        return true;
    }
}

public class PrimeFinder {
    public static void main(String[] args) {
        String inputFile = "Entrada01.txt";
        List<NumberWithIndex> numbers = readNumbers(inputFile);
        
        int[] threadCounts = {1, 5, 10};
        Map<Integer, Long> executionTimes = new HashMap<>();
        
        for (int threads : threadCounts) {
            long startTime = System.nanoTime();
            
            List<PrimeResult> primes = processWithThreads(numbers, threads);
            
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1_000_000; // milissegundos
            
            executionTimes.put(threads, duration);
            writePrimesToFile(primes, "primes_" + threads + ".txt");
        }
        
        // Escreve os tempos de execução em um arquivo txt
        writeExecutionTimesToFile(executionTimes, "execution_times.txt");
        
        System.out.println("Tempos de execução:");
        for (int threads : threadCounts) {
            System.out.println(threads + " thread(s): " + executionTimes.get(threads) + " ms");
        }
    }

    private static List<PrimeResult> processWithThreads(List<NumberWithIndex> numbers, int threadCount) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<List<PrimeResult>>> futures = new ArrayList<>();
        List<List<NumberWithIndex>> chunks = splitIntoChunks(numbers, threadCount);

        for (List<NumberWithIndex> chunk : chunks) {
            futures.add(executor.submit(new PrimeChecker(chunk)));
        }

        executor.shutdown();

        List<PrimeResult> allPrimes = new ArrayList<>();
        for (Future<List<PrimeResult>> future : futures) {
            try {
                allPrimes.addAll(future.get());
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        Collections.sort(allPrimes);
        return allPrimes;
    }

    private static List<List<NumberWithIndex>> splitIntoChunks(List<NumberWithIndex> list, int threadCount) {
        List<List<NumberWithIndex>> chunks = new ArrayList<>();
        int size = list.size();
        int chunkSize = (size + threadCount - 1) / threadCount;

        for (int i = 0; i < size; i += chunkSize) {
            int end = Math.min(size, i + chunkSize);
            chunks.add(list.subList(i, end));
        }
        return chunks;
    }

    private static List<NumberWithIndex> readNumbers(String filename) {
        List<NumberWithIndex> numbers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            int index = 0;
            while ((line = br.readLine()) != null) {
                try {
                    long number = Long.parseLong(line.trim());
                    numbers.add(new NumberWithIndex(number, index++));
                } catch (NumberFormatException e) {
                    System.err.println("Número inválido: " + line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return numbers;
    }

    private static void writePrimesToFile(List<PrimeResult> primes, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (PrimeResult result : primes) {
                writer.println(result.prime);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeExecutionTimesToFile(Map<Integer, Long> executionTimes, String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Map.Entry<Integer, Long> entry : executionTimes.entrySet()) {
                writer.println(entry.getKey() + " thread(s): " + entry.getValue() + " ms");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}