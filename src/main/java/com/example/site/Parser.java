package com.example.site;

public class Parser {

    private String expression;
    private int index;

    public Parser(String expression) {
        this.expression = expression;
        this.index = 0;
    }

    public boolean parseExpr() {
        boolean valid = parseExpression();
        return valid && index == expression.length();
    }

    private boolean parseExpression() {
        if (index >= expression.length()) return false;

        // Пропуск пробелов и комментариев
        skipWhitespaceAndComments();

        // Обработка унарного минуса
        if (expression.charAt(index) == '-') {
            index++;
            if (!parseExpression()) return false;
            return true;
        }

        // Пропуск пробелов и комментариев
        skipWhitespaceAndComments();

        // Обработка чисел и скобок
        if (Character.isDigit(expression.charAt(index))) {
            parseNumber();
        } else if (expression.charAt(index) == '(') {
            index++;
            if (!parseExpression()) return false;
            if (index >= expression.length() || expression.charAt(index) != ')') return false;
            index++;
        } else {
            return false;
        }

        // Пропуск пробелов и комментариев
        skipWhitespaceAndComments();

        // Обработка операторов
        if (index < expression.length() && isOperator(expression.charAt(index))) {
            index++;
            return parseExpression();
        }

        return true;
    }

    // Пропускаем пробелы, переносы строк и комментарии
    private void skipWhitespaceAndComments() {
        while (index < expression.length()) {
            char c = expression.charAt(index);
            if (Character.isWhitespace(c) || c == '\n' || c == '\r') {
                index++;
            } else if (c == '/' && index + 1 < expression.length()) {
                if (expression.charAt(index + 1) == '/') {
                    // Пропускаем однострочный комментарий
                    index += 2;
                    while (index < expression.length() && expression.charAt(index) != '\n' && expression.charAt(index) != '\r') {
                        index++;
                    }
                } else if (expression.charAt(index + 1) == '*') {
                    // Пропускаем многострочный комментарий
                    index += 2;
                    while (index + 1 < expression.length() && !(expression.charAt(index) == '*' && expression.charAt(index + 1) == '/')) {
                        index++;
                    }
                    index += 2; // Пропускаем символы '*/'
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    private void parseNumber() {
        while (index < expression.length() && Character.isDigit(expression.charAt(index))) {
            index++;
        }
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    public static void main(String[] args) {
        Parser parser1 = new Parser("(2+1)*2");
        System.out.println(parser1.parseExpr()); // true

        Parser parser2 = new Parser("((2+1)-(3-4))");
        System.out.println(parser2.parseExpr()); // true

        Parser parser3 = new Parser("(2)");
        System.out.println(parser3.parseExpr()); // true

        Parser parser4 = new Parser("((");
        System.out.println(parser4.parseExpr()); // false

        Parser parser5 = new Parser("(2)+");
        System.out.println(parser5.parseExpr()); // false

        // Проверка унарного минуса
        Parser parser6 = new Parser("-2");
        System.out.println(parser6.parseExpr()); // true

        Parser parser7 = new Parser("-(2)");
        System.out.println(parser7.parseExpr()); // true

        Parser parser8 = new Parser("-(2+3)");
        System.out.println(parser8.parseExpr()); // true

        Parser parser9 = new Parser("-(2+3)*4");
        System.out.println(parser9.parseExpr()); // true

        // Проверка комментариев
        Parser parser10 = new Parser("2 + /* comment */ 3");
        System.out.println(parser10.parseExpr()); // true

        Parser parser11 = new Parser("2 + 3 // end of line comment");
        System.out.println(parser11.parseExpr()); // true

        Parser parser12 = new Parser("/* multi-line\ncomment */ 2 + 3");
        System.out.println(parser12.parseExpr()); // true
    }
}

