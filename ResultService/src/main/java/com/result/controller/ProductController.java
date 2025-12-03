package com.result.controller;


import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {
    private ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public Product create (@RequestBody Product product){
        return productService.add(product);

    }
    @GetMapping
    public List<Product> get() {
        return productService.get();
    }
    @GetMapping("/{id}")
    public Product getOne(@PathVariable Long id) {
        return productService.get(id);
    }
}
