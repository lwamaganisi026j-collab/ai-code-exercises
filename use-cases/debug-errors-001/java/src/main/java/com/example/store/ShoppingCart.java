// ShoppingCart.java
package com.example.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShoppingCart {
    private final List<Product> items = new ArrayList<>();

    public void addItem(Product product) {
        items.add(Objects.requireNonNull(product, "product must not be null"));
    }

    public double calculateTotal() {
        return items.stream().mapToDouble(Product::getPrice).sum();
    }

    public void checkout() {
        System.out.println("Total price: $" + calculateTotal());
    }
}
