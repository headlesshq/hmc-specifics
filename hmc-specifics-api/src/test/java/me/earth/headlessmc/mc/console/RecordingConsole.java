package me.earth.headlessmc.mc.console;

import io.github.headlesshq.headlessmc.console.Console;
import io.github.headlesshq.headlessmc.console.Password;

import java.util.ArrayList;
import java.util.List;

public class RecordingConsole implements Console {
    public final List<String> output = new ArrayList<>();

    @Override
    public void write(String message) {
        output.add(message);
    }

    @Override
    public void write(String message, String nextLine) {
        output.add(message + nextLine);
    }

    @Override
    public String read() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Password readPassword() {
        throw new UnsupportedOperationException();
    }

    public String joined() {
        return String.join("\n", output);
    }

}
