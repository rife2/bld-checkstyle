package com.example;

public class ExamplesMain {
    public String getMessage() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        System.out.println(new ExamplesMain().getMessage());
    }
}