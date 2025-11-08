package com.sheb.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;

public class FileDownloadClient {
    private static final String INPUT_FILE_STORAGE_LOCATION = "200MB-TESTFILE.ORG.pdf";
    private static final String OUTPUT_FILE_STORAGE_LOCATION = "C:\\Software\\oracle.zip";
    public static void main(String[] args) {
        // gRPC server address and port
        String serverHost = "localhost";
        int serverPort = 9090;

        // Ensure output directory exists
        /*try {
            Files.createDirectories(downloadDir);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }*/

        // Build gRPC channel
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(serverHost, serverPort)
                .usePlaintext()  // Disable TLS for local testing
                // You can increase this if your chunks are larger
                .maxInboundMessageSize(100 * 1024 * 1024)  // 100 MB per message
                .build();

        FileTransferServiceGrpc.FileTransferServiceBlockingStub stub = FileTransferServiceGrpc.newBlockingStub(channel);

        System.out.println("⬇️ Starting download of " + INPUT_FILE_STORAGE_LOCATION + " from gRPC server...");

        long start = System.currentTimeMillis();
        Long totalBytes = 0L;

        try (FileOutputStream fos = new FileOutputStream(new File(OUTPUT_FILE_STORAGE_LOCATION))) {

            stub.downloadFiles(
                    FileRequest.newBuilder().setName(INPUT_FILE_STORAGE_LOCATION).build()
            ).forEachRemaining((FileResponse chunk) -> {
                try {
                    byte[] bytes = chunk.getData().toByteArray();
                    fos.write(bytes);
                    System.out.printf("✅ Download in progress: %,d bytes %n",
                            bytes.length);
                } catch (IOException e) {
                    throw new RuntimeException("Error writing chunk to file", e);
                }
            });

            fos.flush();

            long elapsed = System.currentTimeMillis() - start;
            System.out.printf("✅ Download complete: %,d bytes in %.2f seconds%n",
                    totalBytes, elapsed / 1000.0);

        } catch (Exception e) {
            System.err.println("❌ Download failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            channel.shutdown();
        }
    }
}
