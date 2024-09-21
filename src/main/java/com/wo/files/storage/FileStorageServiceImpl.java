package com.wo.files.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {
    private final Path storageLocation;

    @Autowired
    public FileStorageServiceImpl(StorageProperties storageProperties) {
        if (storageProperties.getLocation().trim().isEmpty()) {
            throw new StorageException("File upload location can not be Empty.");
        }
        this.storageLocation = Paths.get(storageProperties.getLocation());
    }

    @Override
    public void initializeStorage() {
        try {
            Files.createDirectories(storageLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

    @Override
    public void save(MultipartFile file) {
        //try {
        validateFile(file);
        Path targetLocation = getTargetLocation(file);
        validateTargetLocation(targetLocation);
        copyFiletoLocation(file, targetLocation);


        /*} catch (IOException e) {
            throw new StorageException("Could not store file", e);
        }*/
    }

    private void copyFiletoLocation(MultipartFile file, Path targetLocation) {
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Could not copy file", e);
        }
    }

    private Path getTargetLocation(MultipartFile file) {
        return storageLocation.resolve(file.getOriginalFilename()).normalize().toAbsolutePath();
    }

    private void validateTargetLocation(Path targetLocation) {
        if (!targetLocation.getParent().equals(this.storageLocation.toAbsolutePath())) {
            throw new StorageException("File upload location does not match storage location.");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new StorageException("File upload can not be Empty.");
        }
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.storageLocation, 1)
                    .filter(path -> !path.equals(this.storageLocation))
                    .map(this.storageLocation::relativize);
        } catch (IOException e) {
            throw new StorageException("Could not load storage files", e);
        }
    }

    @Override
    public Path load(String filename) {
        return storageLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(storageLocation.toFile());
    }

    @Override
    public void delete(String filename) {
        FileSystemUtils.deleteRecursively(storageLocation.resolve(filename).toFile());
    }
}
