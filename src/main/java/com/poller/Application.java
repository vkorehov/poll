package com.poller;

import io.micronaut.runtime.Micronaut;

import java.util.List;

public class Application {
    public final static int MAX_SCALAR = 5;
    public final static List<String> STOPWORDS = List.of("a", "the", "an");
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}