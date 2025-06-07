import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A Java application demonstrating concurrent writing and real-time reading
 * of the same file. Two writer threads append timestamped messages,
 * and a reader thread "tails" the file, printing new lines as they appear.
 * An AtomicBoolean flag signals when writing is complete.
 */
public class SharedFileReadWriteApp {
    private static final String FILE_NAME = "shared.txt";
    // Flag to indicate writers have finished producing messages
    private static final AtomicBoolean finished = new AtomicBoolean(false);

    public static void main(String[] args) throws Exception {
        // Clear or create the shared file at startup
        new PrintWriter(FILE_NAME).close();

        // Create two writer threads with distinct message sets
        Thread writer1 = new WriterThread("Producer-1", new String[]{
            "Hello from Writer 1", "Next message from Writer 1", "Writer 1 done"
        });
        Thread writer2 = new WriterThread("Producer-2", new String[]{
            "Hello from Writer 2", "Next message from Writer 2", "Writer 2 done"
        });
        // Create a reader thread that tails the file
        Thread reader = new ReaderThread();

        // Start reader first to catch all messages in real time
        reader.start();
        // Then start both writers
        writer1.start();
        writer2.start();

        // Wait for writers to finish
        writer1.join();
        writer2.join();
        // Signal the reader that writing is complete
        finished.set(true);
        // Wait for reader to finish processing
        reader.join();

        System.out.println("Application has terminated.");
    }

    /**
     * WriterThread appends timestamped messages to the shared file.
     */
    static class WriterThread extends Thread {
        private final String producerId;
        private final String[] messages;
        // Formatter for timestamps
        private final SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm:ss.SSS");

        WriterThread(String producerId, String[] messages) {
            super(producerId);
            this.producerId = producerId;
            this.messages = messages;
        }

        @Override
        public void run() {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
                for (String msg : messages) {
                    // Build line: [timestamp] Producer-ID: message
                    String line = String.format("[%s] %s: %s",
                        timestampFormat.format(new Date()), producerId, msg);
                    writer.write(line);
                    writer.newLine();
                    writer.flush();
                    // Pause between messages to simulate work
                    Thread.sleep(400);
                }
            } catch (Exception e) {
                System.err.println(producerId + " error: " + e.getMessage());
            }
        }
    }

    /**
     * ReaderThread tails the file, printing new lines as they are written.
     */
    static class ReaderThread extends Thread {
        @Override
        public void run() {
            try (RandomAccessFile raf = new RandomAccessFile(FILE_NAME, "r")) {
                long filePointer = 0;
                // Continue while writers are running or file has new data
                while (!finished.get() || raf.length() > filePointer) {
                    long fileLength = raf.length();
                    if (fileLength > filePointer) {
                        // Seek to last read position
                        raf.seek(filePointer);
                        String line;
                        // Read and print all new lines
                        while ((line = raf.readLine()) != null) {
                            System.out.println("Reader: " + line);
                        }
                        // Update pointer to current file position
                        filePointer = raf.getFilePointer();
                    }
                    // Sleep briefly before checking again
                    Thread.sleep(200);
                }
            } catch (Exception e) {
                System.err.println("Reader error: " + e.getMessage());
            }
        }
    }
}
