package com.example;

public class ExamplesTest {
    void verifyHello() {
        if (!"Hello World!".equals(new ExamplesMain().getMessage())) {
            throw new AssertionError();
        } else {
            System.out.println("Succeeded");
        }
    }

    public static void main(String[] args) {
        new ExamplesTest().verifyHello();
    }
}