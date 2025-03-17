package com.example.cameldemo.configurations;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "payment")
public class PaymentProperties {

    private String inputFolder;
    private String processedFolder;
    private String errorFolder;

    public String getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(String inputFolder) {
        this.inputFolder = inputFolder;
    }

    public String getProcessedFolder() {
        return processedFolder;
    }

    public void setProcessedFolder(String processedFolder) {
        this.processedFolder = processedFolder;
    }

    public String getErrorFolder() {
        return errorFolder;
    }

    public void setErrorFolder(String errorFolder) {
        this.errorFolder = errorFolder;
    }
}
