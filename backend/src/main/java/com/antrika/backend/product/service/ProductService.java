package com.antrika.backend.product.service;

import com.antrika.backend.product.dto.CreateProductRequest;
import com.antrika.backend.product.dto.ProductResponse;
import com.antrika.backend.product.dto.UpdateProductRequest;
import com.antrika.backend.product.entity.Product;
import com.antrika.backend.product.exception.ProductNotFoundException;
import com.antrika.backend.product.repository.ProductRepository;
import org.springframework.stereotype.Service;


import java.util.List;

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

    public List<ProductResponse> getAllProducts() {

        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
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

    public ProductResponse getProductById(Long id) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        )
                );

        return toResponse(product);
     
     }

     public ProductResponse updateProduct(
        Long id,
        UpdateProductRequest request
      ) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new ProductNotFoundException(
                                "Product not found with id: " + id
                        )
                );

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());

        Product updatedProduct = productRepository.save(product);

        return toResponse(updatedProduct);
       }

       public void deleteProduct(Long id) {

                Product product = productRepository.findById(id)
                     .orElseThrow(() ->
                                 new ProductNotFoundException(
                                        "Product not found with id: " + id
                                )
                        );

                productRepository.delete(product);
        }
}