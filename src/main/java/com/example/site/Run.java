package com.example.site;

import java.util.List;
import java.util.Stack;

public class Run {
    private final Stack<Double> values = new Stack<>();

    public void execute(List<ICommand> commands) {
        for (ICommand command : commands) {
            command.execute(values);
        }
    }

    public double getResult() {
        return values.pop();
    }
}
