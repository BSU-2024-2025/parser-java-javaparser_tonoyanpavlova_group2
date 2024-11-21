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
    private static final Map<String, Integer> variables = new HashMap<>();

    public CodeController() {
        File storageDir = new File(CODE_STORAGE_DIR);
        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
    }


    @PostMapping("/execute")
    public String executeCode(@RequestBody String code) {
        logger.info("Received code to execute: " + code);

        // Сначала обработаем выражения с переменными
        String[] statements = code.split(";");
        StringBuilder output = new StringBuilder();

        for (String statement : statements) {
            statement = statement.trim();
            if (statement.contains("=")) {
                // Парсинг выражений вида "x=1+8"
                String[] parts = statement.split("=");
                String varName = parts[0].trim();
                String expression = parts[1].trim();
                int result = evaluateExpression(expression);
                variables.put(varName, result);
                output.append(varName).append(" = ").append(result).append("\n");
            } else {
                // Для простого выражения, вычисляем и выводим результат
                int result = evaluateExpression(statement);
                output.append(result).append("\n");
            }
        }

        return output.toString();
    }
    //стек
    private int evaluateExpression(String expression) {
        // Подстановка значений переменных в выражение
        for (Map.Entry<String, Integer> entry : variables.entrySet()) {
            expression = expression.replace(entry.getKey(), entry.getValue().toString());
        }

        // Поддержка операций с приоритетом с использованием стека
        Stack<Integer> values = new Stack<>();
        Stack<Character> operators = new Stack<>();

        int i = 0;
        while (i < expression.length()) {
            char c = expression.charAt(i);

            if (Character.isDigit(c)) {
                // Считывание числа
                StringBuilder sb = new StringBuilder();
                while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
                    sb.append(expression.charAt(i++));
                }
                values.push(Integer.parseInt(sb.toString()));
                continue;
            } else if (c == '+' || c == '-' || c == '*' || c == '/') {
                // Обработка операций
                while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
                    values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
                }
                operators.push(c);
            }
            i++;
        }

        // Выполнение оставшихся операций
        while (!operators.isEmpty()) {
            values.push(applyOperator(operators.pop(), values.pop(), values.pop()));
        }

        return values.pop();
    }

    private boolean hasPrecedence(char op1, char op2) {
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-')) {
            return false;
        }
        return true;
    }

    private int applyOperator(char operator, int b, int a) {
        switch (operator) {
            case '+':
                return a + b;
            case '-':
                return a - b;
            case '*':
                return a * b;
            case '/':
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
        }
        throw new UnsupportedOperationException("Unsupported operator: " + operator);
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

    @GetMapping("/code/{fileName}")
    public String getSavedCode(@PathVariable String fileName) {
        File file = new File(CODE_STORAGE_DIR, fileName + ".java");
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
        File file = new File(CODE_STORAGE_DIR, fileName + ".java");
        if (!file.exists()) {
            return "Error: Code with name " + fileName + " not found.";
        }

        if (file.delete()) {
            logger.info("Code deleted: " + fileName);
            return "Code " + fileName + " deleted successfully.";
        } else {
            return "Error deleting code: Unable to delete file.";
        }
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
