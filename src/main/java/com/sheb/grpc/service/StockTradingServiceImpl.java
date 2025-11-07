package com.sheb.grpc.service;

import com.sheb.grpc.*;
import com.sheb.grpc.entity.Stock;
import com.sheb.grpc.repository.StockRepository;
import io.grpc.stub.StreamObserver;
import org.springframework.grpc.server.service.GrpcService;

import java.time.LocalDateTime;

@GrpcService
public class StockTradingServiceImpl extends StockTradingServiceGrpc.StockTradingServiceImplBase {


    private final StockRepository stockRepository;

    public StockTradingServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void getStockPrice(StockRequest request,
                              StreamObserver<StockResponse> responseObserver) {
        //stockName -> DB -> map response -> return

        String stockSymbol = request.getStockSymbol();
        Stock stockEntity = stockRepository.findByStockSymbol(stockSymbol);

        StockResponse stockResponse = StockResponse.newBuilder()
                .setStockSymbol(stockEntity.getStockSymbol())
                .setPrice(stockEntity.getPrice())
                .setTimestamp(stockEntity.getLastUpdated().toString())
                .build();

        responseObserver.onNext(stockResponse);
        responseObserver.onCompleted();
    }
    @Override
    public void buyStock(StockRequest request,
                              StreamObserver<StockResponse> responseObserver) {
        //stockName -> DB -> map response -> return

        String stockSymbol = request.getStockSymbol();
        Stock stockEntity = stockRepository.save(new Stock("INFY", 100, LocalDateTime.now()));

        StockResponse stockResponse = StockResponse.newBuilder()
                .setStockSymbol(stockEntity.getStockSymbol())
                .setPrice(stockEntity.getPrice())
                .setTimestamp(stockEntity.getLastUpdated().toString())
                .build();

        responseObserver.onNext(stockResponse);
        responseObserver.onCompleted();
    }
    @Override
    public void sellStock(StockRequest request,
                              StreamObserver<StockResponse> responseObserver) {
        //stockName -> DB -> map response -> return
        String stockSymbol = request.getStockSymbol();
        double stockPrice = request.getPrice();
        Stock stockEntity = stockRepository.save(new Stock(stockSymbol, stockPrice, LocalDateTime.now()));

        StockResponse stockResponse = StockResponse.newBuilder()
                .setStockSymbol(stockEntity.getStockSymbol())
                .setPrice(stockEntity.getPrice())
                .setTimestamp(stockEntity.getLastUpdated().toString())
                .build();

        responseObserver.onNext(stockResponse);
        responseObserver.onCompleted();
    }
    @Override
    public void getPositionStream(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        for (int i = 0; i < 5; i++) {
            StockResponse response = StockResponse.newBuilder()
                    .setMessage("Hello, " + request.getStockSymbol() + " " + i)
                    .build();
            responseObserver.onNext(response);
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<StockRequest> addToFunds(StreamObserver<StockResponse> responseObserver) {
        // The server will process the requests sent by the client
        return new StreamObserver<StockRequest>() {
            private int count = 0;

            @Override
            public void onNext(StockRequest request) {
                // Handle each incoming request
                count++;
                System.out.println("Received request " + count + ": " + request.getStockSymbol());
            }

            @Override
            public void onError(Throwable t) {
                // Handle any errors that might occur while processing the stream
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                // Once the client finishes sending data, respond with a single response
                StockResponse response = StockResponse.newBuilder()
                        .setMessage("Received " + count + " requests.")
                        .build();
                responseObserver.onNext(response);  // Send the response to the client
                responseObserver.onCompleted();     // Complete the response stream
            }
        };
    }

}