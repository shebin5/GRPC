package com.sheb.grpc.service;

import com.sheb.grpc.*;
import com.sheb.grpc.entity.Stock;
import com.sheb.grpc.repository.StockRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.LocalDateTime;

@GrpcService
public class ChatServiceImpl extends ChatServiceGrpc.ChatServiceImplBase {


    private final StockRepository stockRepository;

    public ChatServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void notifyCustomer(NotifyRequest request,
                              StreamObserver<HelloResponse> responseObserver) {
        //stockName -> DB -> map response -> return

        String stockSymbol = request.getName();
        Stock stockEntity = stockRepository.findByStockSymbol(stockSymbol);

        HelloResponse stockResponse = HelloResponse.newBuilder()
                .setMessage(stockEntity.getStockSymbol())
                .build();

        responseObserver.onNext(stockResponse);
        responseObserver.onCompleted();
    }
    @Override
    public StreamObserver<DataRequest> uploadDataToServer(StreamObserver<HelloResponse> responseObserver) {
        // The server will process the requests sent by the client
        return new StreamObserver<DataRequest>() {
            private int count = 0;

            @Override
            public void onNext(DataRequest request) {
                // Handle each incoming request
                count++;
                System.out.println("Received request " + count + ": " + request.getName());
            }

            @Override
            public void onError(Throwable t) {
                // Handle any errors that might occur while processing the stream
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                // Once the client finishes sending data, respond with a single response
                HelloResponse response = HelloResponse.newBuilder()
                        .setMessage("Received " + count + " requests.")
                        .build();
                responseObserver.onNext(response);  // Send the response to the client
                responseObserver.onCompleted();     // Complete the response stream
            }
        };
    }
    @Override
    public StreamObserver<ChatMessage> chat(StreamObserver<ChatMessage> responseObserver) {
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