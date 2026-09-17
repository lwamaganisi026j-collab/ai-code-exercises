// FactorialCalculator.java
package com.example.recursion;

/**
 * A utility class for calculating factorials.
 *
 * The factorial of a non-negative integer n is the product of all
 * positive integers less than or equal to n.
 */
public class FactorialCalculator {

    public static void main(String[] args) {
        int result = calculateFactorial(5);
        System.out.println("Factorial of 5 is: " + result);
    }

    /**
     * Calculates the factorial of a non-negative number recursively.
     *
     * @param num the number to calculate factorial for
     * @return the factorial value
     * @throws IllegalArgumentException when {@code num} is negative
     */
    public static int calculateFactorial(int num) {
        if (num < 0) {
            throw new IllegalArgumentException("Factorial is undefined for negative numbers: " + num);
        }
        if (num == 0 || num == 1) {
            return 1;
        }
        return num * calculateFactorial(num - 1);
    }
}
