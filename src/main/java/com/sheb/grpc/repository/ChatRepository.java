package com.sheb.grpc.repository;

import com.sheb.grpc.ChatMessage;
import com.sheb.grpc.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Stock,Long> {
    Stock findByStockSymbol(String chatId);
}