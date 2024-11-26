package com.example.site;

import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final String expression;

    public Parser(String expression) {
        this.expression = expression;
    }

    public double parseAndEvaluate() {
        List<ICommand> commands = new ArrayList<>();
        Stack<Character> operators = new Stack<>();

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (Character.isWhitespace(c)) {
                continue;
            }

            if (Character.isDigit(c)) {
                StringBuilder number = new StringBuilder();
                while (i < expression.length() && Character.isDigit(expression.charAt(i))) {
                    number.append(expression.charAt(i++));
                }
                i--;
                commands.add(new Command.PushConstCommand(Double.parseDouble(number.toString())));
            } else if (c == '(') {
                operators.push(c);
            } else if (c == ')') {
                while (!operators.isEmpty() && operators.peek() != '(') {
                    commands.add(createCommand(operators.pop()));
                }
                operators.pop(); // Удаляем '('
            } else if (isOperator(c)) {
                while (!operators.isEmpty() && hasPrecedence(c, operators.peek())) {
                    commands.add(createCommand(operators.pop()));
                }
                operators.push(c);
            }
        }

        while (!operators.isEmpty()) {
            commands.add(createCommand(operators.pop()));
        }

        Run runner = new Run();
        runner.execute(commands);

        return runner.getResult();
    }

    private ICommand createCommand(char operator) {
        return switch (operator) {
            case '+' -> new Command.AddCommand();
            case '-' -> new Command.SubtractCommand();
            case '*' -> new Command.MultiplyCommand();
            case '/' -> new Command.DivideCommand();
            default -> throw new IllegalArgumentException("Unknown operator: " + operator);
        };
    }

    private boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private boolean hasPrecedence(char op1, char op2) {
        if (op2 == '(' || op2 == ')') return false;
        return (op1 != '*' && op1 != '/') || (op2 != '+' && op2 != '-');
    }
}