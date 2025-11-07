package com.sheb.grpc.repository;

import com.sheb.grpc.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.File;

public interface FileRepository extends JpaRepository<Stock,Long> {
    Stock findByStockSymbol(String name);
}