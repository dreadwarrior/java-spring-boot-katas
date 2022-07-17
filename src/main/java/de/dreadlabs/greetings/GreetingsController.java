package de.dreadlabs.greetings;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/greetings")
public class GreetingsController {

    @GetMapping(value = "/{name}", produces = MediaType.TEXT_PLAIN_VALUE)
    ResponseEntity<String> greeting(@PathVariable String name) {
        return ResponseEntity.ok("Hello, %s!".formatted(name));
    }
}
