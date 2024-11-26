package com.example.site;

import java.util.Stack;

import java.util.Stack;

public class Command {

    public static class PushConstCommand implements ICommand {
        private final double value;

        public PushConstCommand(double value) {
            this.value = value;
        }

        @Override
        public void execute(Stack<Double> values) {
            values.push(value);
        }
    }

    public static class AddCommand implements ICommand {
        @Override
        public void execute(Stack<Double> values) {
            double b = values.pop();
            double a = values.pop();
            values.push(a + b);
        }
    }

    public static class SubtractCommand implements ICommand {
        @Override
        public void execute(Stack<Double> values) {
            double b = values.pop();
            double a = values.pop();
            values.push(a - b);
        }
    }

    public static class MultiplyCommand implements ICommand {
        @Override
        public void execute(Stack<Double> values) {
            double b = values.pop();
            double a = values.pop();
            values.push(a * b);
        }
    }

    public static class DivideCommand implements ICommand {
        @Override
        public void execute(Stack<Double> values) {
            double b = values.pop();
            double a = values.pop();
            if (b == 0) throw new UnsupportedOperationException("Cannot divide by zero");
            values.push(a / b);
        }
    }

    public static class EndExprCommand implements ICommand {
        @Override
        public void execute(Stack<Double> values) {
            // Не требует действия; просто завершает выражение
        }
    }
}