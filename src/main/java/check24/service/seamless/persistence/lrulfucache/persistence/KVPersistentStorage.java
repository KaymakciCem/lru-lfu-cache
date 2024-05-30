package com.celonis.kvstore.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.celonis.kvstore.KVPair;

@Component
public class KVPersistentStorage {

    private static Logger logger = LoggerFactory.getLogger(KVPersistentStorage.class);

    public Optional<KVPair> retrieve(String key) {
        try {
            Path file = getFile();
            List<String> lines = Files.readAllLines(file);

            for (String line : lines) {
                String[] split = line.split(",");
                if (split[0].equals(key)) {
                    return Optional.of(new KVPair(split[0], split[1]));
                }
            }
        } catch (IOException e) {
            logger.info("Error occurred while reading file. Exception: {}", e.getMessage());
        }

        return Optional.empty();
    }

    public void store(KVPair kvPair) {
        try {
            Path file = getFile();
            Files.write(file,
                        (kvPair.getKey() + "," + kvPair.getValue()).getBytes(),
                        StandardOpenOption.APPEND);

            //add new line
            Files.write(file,
                        System.lineSeparator().getBytes(),
                        StandardOpenOption.APPEND);

        } catch (IOException e) {
            logger.info("Failed to write file. Exception: {}", e.getMessage());
        }
    }

    public void delete(String key) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    private Path getFile() throws IOException {
        Path file = Paths.get("key-value-store.txt");
        if (!Files.exists(file)) {
            return Files.createFile(file);
        }
        return file;

    }
}