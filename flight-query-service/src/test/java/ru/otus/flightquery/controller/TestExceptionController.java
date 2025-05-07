package ru.otus.flightquery.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestExceptionController {

    @GetMapping("/not-found")
    public String throwNotFound() {
        throw new RuntimeException("Resource not found");
    }

    @GetMapping("/internal-error")
    public String throwInternal() {
        throw new RuntimeException("Something went wrong");
    }

    @GetMapping("/null-error")
    public String throwNull() {
        throw new RuntimeException();
    }
}

