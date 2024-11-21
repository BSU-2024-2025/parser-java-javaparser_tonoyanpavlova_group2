package com.example.site;

import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class CodeController {

    private static final Logger logger = LoggerFactory.getLogger(CodeController.class);
    private static final String CODE_STORAGE_DIR = "saved_codes";
    private static final Map<String, Integer> variables = new HashMap<>();

    public CodeController() {
        File storageDir = new File(CODE_STORAGE_DIR);
        if (!storageDir.exists()) {
            boolean created = storageDir.mkdir();
            if (!created) {
                logger.error("Failed to create directory: {}", CODE_STORAGE_DIR);
                throw new RuntimeException("Could not create storage directory: " + CODE_STORAGE_DIR);
            }
        }
    }

    @PostMapping("/execute")
    public String executeCode(@RequestBody String code) {
        logger.info("Received code to execute: {}", code);
        String[] statements = code.split(";");
        StringBuilder output = new StringBuilder();

        for (String statement : statements) {
            statement = statement.trim();
            if (statement.contains("=")) {
                // Handle variable assignment
                String[] parts = statement.split("=");
                String varName = parts[0].trim();
                String expression = parts[1].trim();
                int result = evaluateExpression(expression);
                variables.put(varName, result);
                output.append(varName).append(" = ").append(result).append("\n");
            } else {
                // Evaluate simple expressions
                int result = evaluateExpression(statement);
                output.append(result).append("\n");
            }
        }

        return output.toString();
    }

    private int evaluateExpression(String expression) {
        for (Map.Entry<String, Integer> entry : variables.entrySet()) {
            expression = expression.replace(entry.getKey(), entry.getValue().toString());
        }

        Parser parser = new Parser(expression);
        return (int) parser.parseAndEvaluate();
    }

    @PostMapping("/save")
    public String saveCode(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        String fileName = payload.get("fileName");

        if (fileName == null || fileName.trim().isEmpty()) {
            return "Error: Filename cannot be empty.";
        }

        File file = new File(CODE_STORAGE_DIR, fileName + ".txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(code);
            logger.info("Code saved as: {}", file.getName());
            return "Code saved as " + fileName;
        } catch (IOException e) {
            logger.error("Error saving code to file", e);
            return "Error saving code: " + e.getMessage();
        }
    }

    @GetMapping("/codes")
    public List<String> getSavedCodes() {
        File folder = new File(CODE_STORAGE_DIR);
        String[] files = folder.list((dir, name) -> name.endsWith(".txt"));
        List<String> codeFiles = new ArrayList<>();
        if (files != null) {
            for (String file : files) {
                codeFiles.add(file.replace(".txt", ""));
            }
        }
        return codeFiles;
    }

    @GetMapping("/code/{fileName}")
    public String getSavedCode(@PathVariable String fileName) {
        File file = new File(CODE_STORAGE_DIR, fileName + ".txt");
        if (!file.exists()) {
            return "Error: Code with name " + fileName + " not found.";
        }

        try {
            return new String(Files.readAllBytes(file.toPath()));
        } catch (IOException e) {
            logger.error("Error reading saved code file", e);
            return "Error reading file: " + e.getMessage();
        }
    }

    @DeleteMapping("/code/{fileName}")
    public String deleteSavedCode(@PathVariable String fileName) {
        File file = new File(CODE_STORAGE_DIR, fileName + ".txt");
        if (!file.exists()) {
            return "Error: Code with name " + fileName + " not found.";
        }

        if (file.delete()) {
            logger.info("Code deleted: {}", fileName);
            return "Code " + fileName + " deleted successfully.";
        } else {
            return "Error deleting code: Unable to delete file.";
        }
    }
}