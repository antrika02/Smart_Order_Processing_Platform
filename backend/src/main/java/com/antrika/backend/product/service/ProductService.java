package com.antrika.backend.product.service;

import com.antrika.backend.product.dto.CreateProductRequest;
import com.antrika.backend.product.dto.ProductResponse;
import com.antrika.backend.product.entity.Product;
import com.antrika.backend.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductResponse createProduct(CreateProductRequest request) {

        Product product = new Product(
                request.name(),
                request.description(),
                request.price(),
                request.stockQuantity()
        );

        Product savedProduct = productRepository.save(product);

        return toResponse(savedProduct);
    }

    private ProductResponse toResponse(Product product) {

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getActive()
        );
    }
}