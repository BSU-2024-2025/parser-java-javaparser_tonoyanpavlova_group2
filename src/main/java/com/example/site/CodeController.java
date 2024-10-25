package com.example.site;

import org.springframework.web.bind.annotation.*;
import javax.tools.*;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class CodeController {

    private static final Logger logger = LoggerFactory.getLogger(CodeController.class);
    private static final String CODE_STORAGE_DIR = "saved_codes";

    public CodeController() {
        File storageDir = new File(CODE_STORAGE_DIR);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
    }

    @PostMapping("/execute")
    public String executeCode(@RequestBody String code) {
        logger.info("Received code to execute: " + code);
        if (isExpression(code)) {
            return compileAndRunJavaCode("System.out.println(" + code + ");", false);
        }
        return compileAndRunJavaCode(code, code.contains("class"));
    }

    @PostMapping("/save")
    public String saveCode(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        String fileName = payload.get("fileName");
        if (fileName == null || fileName.trim().isEmpty()) {
            return "Error: Filename cannot be empty.";
        }

        File file = new File(CODE_STORAGE_DIR, fileName + ".java");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(code);
            logger.info("Code saved as: " + file.getName());
            return "Code saved as " + fileName;
        } catch (IOException e) {
            logger.error("Error saving code to file", e);
            return "Error saving code: " + e.getMessage();
        }
    }

    @GetMapping("/codes")
    public List<String> getSavedCodes() {
        File folder = new File(CODE_STORAGE_DIR);
        String[] files = folder.list((dir, name) -> name.endsWith(".java"));
        List<String> codeFiles = new ArrayList<>();
        if (files != null) {
            for (String file : files) {
                codeFiles.add(file.replace(".java", ""));
            }
        }
        return codeFiles;
    }

    @GetMapping("/execute/{fileName}")
    public String executeSavedCode(@PathVariable String fileName) {
        File file = new File(CODE_STORAGE_DIR, fileName + ".java");
        if (!file.exists()) {
            return "Error: Code with name " + fileName + " not found.";
        }

        try {
            String code = new String(Files.readAllBytes(file.toPath()));
            logger.info("Executing saved code: " + code);

            // Wrap expression in println if necessary
            if (isExpression(code)) {
                code = "System.out.println(" + code + ");";
            }

            return compileAndRunJavaCode(code, code.contains("class"));
        } catch (IOException e) {
            logger.error("Error reading saved code file", e);
            return "Error reading file: " + e.getMessage();
        }
    }

    private boolean isExpression(String code) {
        // Check if code is likely an expression rather than a full class or method
        return !code.contains("class") && !code.contains(";") && !code.trim().startsWith("public");
    }

    private String compileAndRunJavaCode(String code, boolean isFullClass) {
        String className = "UserCode";
        String fullCode;

        if (!isFullClass) {
            fullCode = "public class " + className + " { public static void main(String[] args) { " + code + " } }";
        } else {
            fullCode = code;
        }

        logger.info("Generated Java code: " + fullCode);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return "Error: Java compiler is not available. Make sure the application is running with a JDK.";
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        File sourceFile = new File(tempDir, className + ".java");

        try (FileWriter writer = new FileWriter(sourceFile)) {
            writer.write(fullCode);
        } catch (IOException e) {
            logger.error("Error writing source file", e);
            return "Error writing file: " + e.getMessage();
        }

        List<String> options = Arrays.asList("-d", tempDir.getAbsolutePath(), "-proc:none");
        JavaCompiler.CompilationTask task = compiler.getTask(null, null, diagnostics, options, null, Arrays.asList(new SimpleJavaFileObject(sourceFile.toURI(), JavaFileObject.Kind.SOURCE) {
            @Override
            public CharSequence getCharContent(boolean ignoreEncodingErrors) {
                return fullCode;
            }
        }));

        if (!task.call()) {
            StringBuilder errorMessage = new StringBuilder("Compilation Error:\n");
            diagnostics.getDiagnostics().forEach(diagnostic -> errorMessage.append(diagnostic).append("\n"));
            logger.error("Compilation errors: " + errorMessage);
            return errorMessage.toString();
        }

        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{tempDir.toURI().toURL()})) {
            Class<?> clazz = classLoader.loadClass(className);
            Method mainMethod = clazz.getMethod("main", String[].class);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            System.setOut(printStream);

            mainMethod.invoke(null, (Object) new String[0]);

            return outputStream.toString();
        } catch (Exception e) {
            logger.error("Execution error", e);
            return "Execution Error: " + e.getMessage();
        } finally {
            sourceFile.delete();
        }
    }
}
