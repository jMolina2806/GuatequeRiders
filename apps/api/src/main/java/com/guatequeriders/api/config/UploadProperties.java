package com.guatequeriders.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

@ConfigurationProperties(prefix = "app")
public class UploadProperties {

    private String uploadDir = "uploads";

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public Path getUploadRoot() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public Path getCollaboratorsRoot() {
        return getUploadRoot().resolve("collaborators").normalize();
    }

    public Path getRoutesRoot() {
        return getUploadRoot().resolve("routes").normalize();
    }
}
