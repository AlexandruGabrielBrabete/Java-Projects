import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * A console application that reads pairs of large integers from standard input,
 * spawns a thread for each pair to compute the greatest common divisor (GCD),
 * and then displays each result along with performance statistics.
 */
public class GCDMultiThreadApp {
    /**
     * Immutable data structure to store the result of a GCD computation.
     */
    static class Result {
        final BigInteger numberA;
        final BigInteger numberB;
        final BigInteger gcdValue;
        final long durationNano;

        Result(BigInteger numberA, BigInteger numberB, BigInteger gcdValue, long durationNano) {
            this.numberA = numberA;
            this.numberB = numberB;
            this.gcdValue = gcdValue;
            this.durationNano = durationNano;
        }
    }

    // Thread-safe list to collect results from all threads
    private static final List<Result> results =
        Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        List<Thread> threads = new ArrayList<>();

        System.out.println("Enter pairs of integers (or type 'exit' to finish):");
        while (true) {
            System.out.print("First number: ");
            String inputA = scanner.next();
            if (inputA.equalsIgnoreCase("exit")) {
                break;
            }

            System.out.print("Second number: ");
            String inputB = scanner.next();
            if (inputB.equalsIgnoreCase("exit")) {
                break;
            }

            try {
                BigInteger a = new BigInteger(inputA);
                BigInteger b = new BigInteger(inputB);
                // Create and start a thread to compute GCD
                Thread gcdThread = new GCDComputeThread(a, b);
                threads.add(gcdThread);
                gcdThread.start();
            } catch (NumberFormatException ex) {
                System.err.println("Invalid format. Please enter valid large integers.");
            }
        }

        // Wait for all computation threads to finish
        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // After all threads complete, summarize performance statistics
        if (!results.isEmpty()) {
            long totalTime = 0;
            for (Result r : results) {
                totalTime += r.durationNano;
            }
            long averageTime = totalTime / results.size();

            // Find the fastest and slowest computations
            Result fastest = Collections.min(results, Comparator.comparingLong(r -> r.durationNano));
            Result slowest = Collections.max(results, Comparator.comparingLong(r -> r.durationNano));

            System.out.println("\n=== Performance Summary ===");
            System.out.println("Total computations: " + results.size());
            System.out.println("Average time: " + (averageTime / 1_000_000) + " ms");
            System.out.println("Fastest: gcd(" + fastest.numberA + ", " + fastest.numberB + ") = "
                + fastest.gcdValue + " in " + (fastest.durationNano / 1_000_000) + " ms");
            System.out.println("Slowest: gcd(" + slowest.numberA + ", " + slowest.numberB + ") = "
                + slowest.gcdValue + " in " + (slowest.durationNano / 1_000_000) + " ms");
        }
    }

    /**
     * Thread class responsible for computing the GCD of two BigInteger values
     * and recording the computation time.
     */
    static class GCDComputeThread extends Thread {
        private final BigInteger x;
        private final BigInteger y;

        GCDComputeThread(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void run() {
            long startTime = System.nanoTime();
            BigInteger gcdResult = x.gcd(y);
            long duration = System.nanoTime() - startTime;

            // Display the result for this thread
            System.out.println("gcd(" + x + ", " + y + ") = " + gcdResult
                + " computed in " + (duration / 1_000_000) + " ms (Thread: " + getName() + ")");

            // Save result object for summary
            results.add(new Result(x, y, gcdResult, duration));
        }
    }
}
