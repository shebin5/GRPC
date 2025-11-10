package com.sheb.grpc.service;

import com.google.protobuf.ByteString;
import com.sheb.grpc.*;
import com.sheb.grpc.entity.Stock;
import com.sheb.grpc.repository.StockRepository;
import io.grpc.Context;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

@GrpcService
public class FileTransferServiceImpl extends FileTransferServiceGrpc.FileTransferServiceImplBase {


    private static final String FILE_STORAGE_LOCATION = "C:\\Software\\";
    private final StockRepository stockRepository;

    public FileTransferServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void downloadFiles(FileRequest request, StreamObserver<FileResponse> responseObserver) {
        String filename = request.getId();
        if (request.getId().isEmpty()) {
            throw Status.INVALID_ARGUMENT
                    .withDescription("Name cannot be empty.")
                    .asRuntimeException();
        }
        File file = new File(FILE_STORAGE_LOCATION + filename);

        if (!file.exists() || !file.canRead()) {
            responseObserver.onError(
                    new RuntimeException("File not found or unreadable: " + filename)
            );
            return;
        }
        Context context = Context.current();
        int count = 0;
        byte[] buffer = new byte[1024 * 64];
        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                count++;
                if (count > 50) {
                    System.out.println("Client throw exception the stream for file: " + file.getName());
                     throw new Exception("!!!!!!!!!!!!!!!!");
                }
                if (context.isCancelled()) {
                    System.out.println("Client cancelled the stream for file: " + file.getName());
                    break;
                }
                FileResponse chunk = FileResponse.newBuilder()
                        .setData(ByteString.copyFrom(buffer, 0, bytesRead))
                        .setSize((int) file.length())
                        .build();
                responseObserver.onNext(chunk);
            }
            responseObserver.onCompleted();
        } catch (IOException e) {
            responseObserver.onError(e);
        } catch (Exception e) {
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Internal server error!!!!!!!!!!: " + e.getMessage())
                            .augmentDescription("Please contact support.")
                            .asRuntimeException()
            );
        }
    }

    @Override
    public StreamObserver<FileRequest> uploadFiles(StreamObserver<FileResponse> responseObserver) {
        // The server will process the requests sent by the client
        return new StreamObserver<FileRequest>() {
            private int count = 0;
            Context context = Context.current();

            @Override
            public void onNext(FileRequest chunk) {
                if (context.isCancelled()) {
                    System.out.println("Call was cancelled (deadline or manual).");
                    return;
                }
                System.out.println("Received chunk: " + chunk.getPayload());
                count++;
            }

            @Override
            public void onError(Throwable t) {
                // Handle any errors that might occur while processing the stream
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                System.out.println("All chunks received: " + count);
                FileResponse status = FileResponse.newBuilder()
                        .setData(ByteString.copyFromUtf8("Upload complete."))
                        .setSize(count)
                        .build();
                responseObserver.onNext(status);
                responseObserver.onCompleted();    // Complete the response stream
            }
        };
    }

    @Override
    public StreamObserver<ChatMessage> transferFiles(StreamObserver<ChatMessage> responseObserver) {
        // This is the server-side stream observer, which listens for incoming messages from the client.
        return new StreamObserver<ChatMessage>() {

            @Override
            public void onNext(ChatMessage chatMessage) {
                // Process each incoming chat message from the client
                System.out.println("Received message from " + chatMessage.getSender() + ": " + chatMessage.getMessage());

                // Create a response message (for example, echoing back the same message)
                ChatMessage response = ChatMessage.newBuilder()
                        .setSender("Shebin")
                        .setMessage("Echo: Hi HDFC")
                        .setTimestamp(System.currentTimeMillis())
                        .build();

                // Send the response back to the client
                responseObserver.onNext(response);
            }

            @Override
            public void onError(Throwable t) {
                // Handle any errors that occur during the stream processing
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                // Once the client finishes sending messages, the server ends the response stream
                System.out.println("Client has finished the chat.");
                responseObserver.onCompleted(); // Indicate that the server has completed sending responses
            }
        };
    }

}
