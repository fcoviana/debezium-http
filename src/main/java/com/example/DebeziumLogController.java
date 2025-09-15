package com.example;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/logs")
public class DebeziumLogController {

    @PostMapping
    public ResponseEntity<Void> receiveLog(@RequestBody String event) {
        System.out.println("Evento recebido do Debezium: " + event);
        return ResponseEntity.ok().build();
    }
}