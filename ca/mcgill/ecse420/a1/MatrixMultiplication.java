package ca.mcgill.ecse420.a1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MatrixMultiplication {

    private static final int THREAD_BENCHMARK_SIZE = 4000;
    private static final int[] MATRIX_SIZES = {100, 200, 500, 1000, 2000, 3000, 4000};
    private static final int THREAD_BENCHMARK_RUNS = 3;
    private static final int SIZE_BENCHMARK_RUNS = 1;

    public static void main(String[] args) {

        if (args.length == 0) {
            printUsage();
            return;
        }

        switch (args[0].toLowerCase()) {

            case "validate":
                validateMultiplication();
                break;

            case "timing":
                runTimingTest();
                break;

            case "threads":
                runThreadBenchmark();
                break;

            case "sizes":
                int bestThreadCount = getBestThreadCountFromCsv();
                benchmarkMatrixSizes(bestThreadCount);
                break;

            case "all":

                validateMultiplication();
                System.out.println("\nQuestion 1.3");
                runTimingTest();
                System.out.println("\nQuestion 1.4");
                runThreadBenchmark();
                System.out.println("\nQuestion 1.5");
                int bestThreads = getBestThreadCountFromCsv();
                benchmarkMatrixSizes(bestThreads);
                break;

            default:
                printUsage();
                break;
        }
    }

    // Question 1.1
    public static double[][] sequentialMultiplyMatrix(double[][] a, double[][] b) {

        if (a[0].length != b.length) {
            throw new IllegalArgumentException("Number of columns of A must equal number of rows of B.");
        }

        int rowsA = a.length;
        int colsA = a[0].length;
        int colsB = b[0].length;
        double[][] result = new double[rowsA][colsB];

        for (int i = 0; i < rowsA; i++) {
            for (int j = 0; j < colsB; j++) {
                for (int k = 0; k < colsA; k++) {
                    result[i][j] += a[i][k] * b[k][j];
                }
            }
        }
        return result;
    }

    //question 1.2
    public static double[][] parallelMultiplyMatrix(double[][] a, double[][] b, int numberThreads) {
        
        if (a[0].length != b.length) {
            throw new IllegalArgumentException("Number of columns of A must equal number of rows of B.");
        }

        if (numberThreads <= 0) {
            throw new IllegalArgumentException("Number of threads must be greater than zero.");
        }

        int rowsA = a.length;
        int colsA = a[0].length;
        int colsB = b[0].length;

        double[][] result = new double[rowsA][colsB];
        ExecutorService executor = Executors.newFixedThreadPool(numberThreads);
        int rowsPerThread = (int) Math.ceil((double) rowsA / numberThreads);

        for (int thread = 0; thread < numberThreads; thread++) {

            int startRow = thread * rowsPerThread;
            int endRow = Math.min(startRow + rowsPerThread, rowsA);

            if (startRow >= rowsA) {
                break;
            }

            executor.execute(() -> {
                for (int i = startRow; i < endRow; i++) {
                    for (int j = 0; j < colsB; j++) {
                        for (int k = 0; k < colsA; k++) {
                            result[i][j] += a[i][k] * b[k][j];
                        }
                    }
                }
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Matrix multiplication was interrupted.", e);
        }

        return result;
    }

    //validation for Questions 1.1 and 1.2
    public static void validateMultiplication() {

        System.out.println("Questions 1.1 and 1.2");

        double[][] a = {{1, 2}, {3, 4}};
        double[][] b = {{5, 6}, {7, 8}};
        double[][] expected = {{19, 22}, {43, 50}};

        double[][] sequentialResult = sequentialMultiplyMatrix(a, b);
        double[][] parallelResult = parallelMultiplyMatrix(a, b, 2);

        boolean sequentialCorrect = matricesEqual(sequentialResult, expected);
        boolean parallelCorrect = matricesEqual(parallelResult, expected);
        boolean resultsMatch = matricesEqual(sequentialResult, parallelResult);

        System.out.println("Sequential result correct: " + sequentialCorrect);
        System.out.println("Parallel result correct: " + parallelCorrect);
        System.out.println("Sequential and parallel results match: " + resultsMatch);
    }

    // Question 1.3
    public static void measureExecutionTime(double[][] a, double[][] b, int numberThreads) {
        long startTime = System.nanoTime();
        sequentialMultiplyMatrix(a, b);
        long endTime = System.nanoTime();
        double sequentialTime = (endTime - startTime) / 1_000_000.0;
        startTime = System.nanoTime();
        parallelMultiplyMatrix(a, b, numberThreads);
        endTime = System.nanoTime();
        double parallelTime = (endTime - startTime) / 1_000_000.0;
        System.out.println("Sequential execution time: " + sequentialTime + " ms");
        System.out.println("Parallel execution time (" + numberThreads + " threads): " + parallelTime + " ms");
    }

    private static void runTimingTest() {
        int matrixSize = 1000;
        int numberThreads = Runtime.getRuntime().availableProcessors();
        System.out.println("Matrix size: " + matrixSize + " x " + matrixSize);
        System.out.println("Threads: " + numberThreads);
        double[][] a = generateRandomMatrix(matrixSize, matrixSize);
        double[][] b = generateRandomMatrix(matrixSize, matrixSize);
        measureExecutionTime(a, b, numberThreads);
    }

    // Question 1.4
    public static void runThreadBenchmark() {
        System.out.println("Generating " + THREAD_BENCHMARK_SIZE + " x " + THREAD_BENCHMARK_SIZE + " matrices...");
        double[][] a = generateRandomMatrix(THREAD_BENCHMARK_SIZE, THREAD_BENCHMARK_SIZE);
        double[][] b = generateRandomMatrix(THREAD_BENCHMARK_SIZE, THREAD_BENCHMARK_SIZE);
        benchmarkThreadCounts(a, b);
    }

    public static void benchmarkThreadCounts(double[][] a, double[][] b) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int maxThreads = availableProcessors * 2;
        System.out.println("Available processors: " + availableProcessors);
        System.out.println("Testing thread counts from 1 to " + maxThreads);
        double oneThreadTime = 0;
        double minimumTime = Double.MAX_VALUE;
        int bestThreadCount = 1;
        try (FileWriter writer = new FileWriter("thread_results.csv")) {
            writer.write("Threads,AverageTime,Speedup\n");
            for (int threads = 1; threads <= maxThreads; threads++) {
                System.out.println("Testing " + threads + " threads...");
                double totalTime = 0;
                for (int run = 0; run < THREAD_BENCHMARK_RUNS; run++) {
                    long startTime = System.nanoTime();
                    parallelMultiplyMatrix(a, b, threads);
                    long endTime = System.nanoTime();
                    double executionTime = (endTime - startTime) / 1_000_000.0;
                    totalTime += executionTime;
                }
                double averageTime = totalTime / THREAD_BENCHMARK_RUNS;
                if (threads == 1) {
                    oneThreadTime = averageTime;
                }
                double speedup = oneThreadTime / averageTime;
                if (averageTime < minimumTime) {
                    minimumTime = averageTime;
                    bestThreadCount = threads;
                }
                writer.write(threads + "," + averageTime + "," + speedup + "\n");
                System.out.println(threads + " threads: " + averageTime + " ms | speedup: " + speedup);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("\nBest thread count: " + bestThreadCount);
        System.out.println("Minimum average time: " + minimumTime + " ms");
        System.out.println("Results saved to thread_results.csv");
    }

    // Reads the best thread count produced by Question 1.4
    private static int getBestThreadCountFromCsv() {
        String fileName = "thread_results.csv";
        double minimumTime = Double.MAX_VALUE;
        int bestThreadCount = -1;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            // Skip CSV header
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                int threads = Integer.parseInt(values[0]);
                double averageTime = Double.parseDouble(values[1]);
                if (averageTime < minimumTime) {
                    minimumTime = averageTime;
                    bestThreadCount = threads;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read " + fileName + ". Run the threads benchmark first.", e);
        }
        if (bestThreadCount == -1) {
            throw new RuntimeException("No benchmark results found in " + fileName);
        }
        System.out.println("Best thread count from " + fileName + ": " + bestThreadCount);
        System.out.println("Minimum average time: " + minimumTime + " ms");
        return bestThreadCount;
    }

    // Question 1.5
    public static void benchmarkMatrixSizes(int bestThreadCount) {

        System.out.println("Using " + bestThreadCount + " threads for parallel tests.");

        try (FileWriter writer = new FileWriter("matrix_size_results.csv")) {
            writer.write("MatrixSize,SequentialTime,ParallelTime\n");

            for (int size : MATRIX_SIZES) {

                System.out.println("\nTesting matrix size " + size + " x " + size + "...");
                double[][] a = generateRandomMatrix(size, size);
                double[][] b = generateRandomMatrix(size, size);
                double sequentialTotal = 0;
                double parallelTotal = 0;

                for (int run = 0; run < SIZE_BENCHMARK_RUNS; run++) {
                    System.out.println("Run " + (run + 1) + " of " + SIZE_BENCHMARK_RUNS);
                    long startTime = System.nanoTime();

                    sequentialMultiplyMatrix(a, b);
                    long endTime = System.nanoTime();

                    sequentialTotal += (endTime - startTime) / 1_000_000.0;
                    startTime = System.nanoTime();

                    parallelMultiplyMatrix(a, b, bestThreadCount);
                    endTime = System.nanoTime();

                    parallelTotal += (endTime - startTime) / 1_000_000.0;
                }

                double sequentialAverage = sequentialTotal / SIZE_BENCHMARK_RUNS;
                double parallelAverage = parallelTotal / SIZE_BENCHMARK_RUNS;

                writer.write(size + "," + sequentialAverage + "," + parallelAverage + "\n");
                System.out.println("Sequential: " + sequentialAverage + " ms");
                System.out.println("Parallel: " + parallelAverage + " ms");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("\nResults saved to matrix_size_results.csv");
    }

    private static boolean matricesEqual(double[][] a, double[][] b) {

        if (a.length != b.length || a[0].length != b[0].length) {
            return false;
        }

        double tolerance = 1e-9;

        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[0].length; j++) {
                if (Math.abs(a[i][j] - b[i][j]) > tolerance) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double[][] generateRandomMatrix(int numRows, int numCols) {

        double[][] matrix = new double[numRows][numCols];
        
        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numCols; col++) {
                matrix[row][col] = (double) ((int) (Math.random() * 10.0));
            }
        }
        return matrix;
    }

    private static void printUsage() {

        System.out.println("Usage:");
        System.out.println("validate -> Questions 1.1 and 1.2");
        System.out.println("timing -> Question 1.3");
        System.out.println("threads -> Question 1.4");
        System.out.println("sizes -> Question 1.5 " + "(reads best thread count from thread_results.csv)");
        System.out.println("all -> Run Questions 1.1 to 1.5");
    }
}