package com.example.store;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ShoppingCartTest {
    @Test
    void calculatesTotalForAddedProducts() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(new Product("Keyboard", 50.0));
        cart.addItem(new Product("Mouse", 25.0));

        assertEquals(75.0, cart.calculateTotal());
    }

    @Test
    void rejectsNullProductsAtInsertion() {
        ShoppingCart cart = new ShoppingCart();

        assertThrows(NullPointerException.class, () -> cart.addItem(null));
    }
}
