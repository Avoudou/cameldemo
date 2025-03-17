package com.example.cameldemo.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
public class CsvUploadController {

    private final String inputDir = System.getProperty("user.dir") + File.separator + "input";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            System.out.println("Using input path: " + inputDir);

            File inputFolder = new File(inputDir);
            if (!inputFolder.exists()) inputFolder.mkdirs();

            File destFile = new File(inputFolder, file.getOriginalFilename());
            file.transferTo(destFile);

            return ResponseEntity.ok("File uploaded successfully: " + destFile.getName());
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
        }
    }
}
