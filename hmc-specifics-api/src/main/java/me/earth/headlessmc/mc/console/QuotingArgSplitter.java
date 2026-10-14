package me.earth.headlessmc.mc.console;

import io.github.headlesshq.headlessmc.console.ArgSplitter;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a line at spaces, unless they are inside double quotes or escaped with a backslash.
 */
public class QuotingArgSplitter implements ArgSplitter {
    @Override
    public String[] split(String line) {
        List<String> result = new ArrayList<>();
        boolean quoted = false;
        boolean escaped = false;
        StringBuilder currentArg = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (escaped) {
                currentArg.append(ch);
                escaped = false;
            } else if (ch == '\\') {
                escaped = true;
            } else if (ch == '\"') {
                if (quoted) {
                    result.add(currentArg.toString());
                    currentArg = new StringBuilder();
                }

                quoted = !quoted;
            } else if (ch == ' ') {
                if (quoted) {
                    currentArg.append(ch);
                } else if (currentArg.length() != 0) {
                    result.add(currentArg.toString());
                    currentArg = new StringBuilder();
                }
            } else {
                currentArg.append(ch);
            }
        }

        if (currentArg.length() > 0) {
            result.add(currentArg.toString());
        }

        return result.toArray(new String[0]);
    }

}
