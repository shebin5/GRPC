package com.sheb.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientStreamingExample {
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        FileTransferServiceGrpc.FileTransferServiceStub asyncStub = FileTransferServiceGrpc.newStub(channel);

        CountDownLatch latch = new CountDownLatch(1);

        StreamObserver<FileResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(FileResponse value) {
                System.out.println("Server Response: " + value.getData() +
                        " (Received " + value.getSize() + " chunks)");
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + io.grpc.Status.fromThrowable(t));
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Upload finished successfully.");
                latch.countDown();
            }
        };

        StreamObserver<FileRequest> requestObserver =
                asyncStub.withDeadlineAfter(500, TimeUnit.SECONDS) // deadline example
                        .uploadFiles(responseObserver);

        try {
            for (int i = 1; i <= 5; i++) {
                FileRequest chunk = FileRequest.newBuilder()
                        .setId("chunk-" + i)
                        .setPayload("This is data part " + i)
                        .build();

                System.out.println("Sending: " + chunk.getPayload());
                requestObserver.onNext(chunk);
                Thread.sleep(500); // simulate delay between sends
            }

            // Signal end of stream
            requestObserver.onCompleted();

            latch.await(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            requestObserver.onError(e);
        } finally {
            channel.shutdown();
        }
    }
}
