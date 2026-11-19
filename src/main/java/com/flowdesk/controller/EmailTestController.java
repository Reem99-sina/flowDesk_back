package com.flowdesk.controller;

import com.flowdesk.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailTestController {

    private final EmailService emailService;

    @PostMapping("/api/test/email")
    public ResponseEntity<String> testEmail() {

        emailService.sendEmail(
                "reemsina2@gmail.com",
                "FlowDesk Email Test",
                "Congratulations! FlowDesk can send emails successfully."
        );

        return ResponseEntity.ok("Test email sent successfully");
    }
}