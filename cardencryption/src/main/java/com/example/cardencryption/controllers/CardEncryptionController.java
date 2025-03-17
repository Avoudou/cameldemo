package com.example.cardencryption.controllers;

import com.example.cardencryption.dto.EncryptionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CardEncryptionController {


    @PostMapping("/encrypt")
    public ResponseEntity<EncryptionResponse> encryptCard(@RequestBody CardRequest request) {
        String masked = "mockEncrypted_" + request.getCardNumber();
        return ResponseEntity.ok(new EncryptionResponse(masked));
    }

//    @PostMapping("/decrypt")
//    public ResponseEntity<String> decryptCard(@RequestBody DecryptRequest request) {
//        String encrypted = request.getEncrypted();
//        if (encrypted != null && encrypted.startsWith("mockEncrypted_")) {
//            String original = encrypted.replace("mockEncrypted_", "");
//            return ResponseEntity.ok(original);
//        } else {
//            return ResponseEntity.badRequest().body("Invalid encrypted format");
//        }
//    }

    public static class CardRequest {
        private String cardNumber;
        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    }

    public static class DecryptRequest {
        private String encrypted;
        public String getEncrypted() { return encrypted; }
        public void setEncrypted(String encrypted) { this.encrypted = encrypted; }
    }
}
