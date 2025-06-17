package com.example.product_catalog_api.service;

import com.example.product_catalog_api.model.Product;
import com.example.product_catalog_api.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException; // Import specific exception
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional // Ensures the entire method runs in a single transaction
    public Product createProduct(Product product) {
        // When saving a new product, 'version' will be initialized by JPA
        return productRepository.save(product);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional
    public Product updateProductPrice(Long id, BigDecimal newPrice) {
        // Retrieve the product
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isEmpty()) {
            throw new RuntimeException("Product not found with id: " + id);
        }

        Product product = optionalProduct.get();
        product.setPrice(newPrice);

        try {
            // Save the updated product. If the version doesn't match,
            // ObjectOptimisticLockingFailureException will be thrown.
            return productRepository.save(product);
        } catch (ObjectOptimisticLockingFailureException e) {
            // This is where you handle the conflict.
            // You might log it, retry the operation, or inform the user.
            System.err.println("Optimistic Lock Exception: Another user modified the product with ID " + id);
            throw new RuntimeException("Conflict: Product with ID " + id + " was updated by another user. Please retry your operation.", e);
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        // You can also get optimistic locking failures on delete if the record was modified
        // by another transaction between reading and deleting.
        productRepository.deleteById(id);
    }
}