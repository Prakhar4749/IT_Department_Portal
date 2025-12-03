package com.result.services;



import com.result.entities.Product;

import java.util.List;

public interface ProductService {

    Product add(Product quiz);

    List<Product> get();

    Product get(Long id);


}