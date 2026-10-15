package me.earth.headlessmc.mc.jline.isolated;

import io.github.headlesshq.headlessmc.console.ArgSplitter;
import org.jline.reader.Parser;
import org.jline.reader.impl.DefaultParser;

/**
 * An {@link ArgSplitter} backed by the JLine {@link DefaultParser},
 * so that commands are split the same way the {@link IsolatedJlineConsole} parses them for completions.
 * This class is loaded on an isolated classloader together with JLine,
 * it may only reference JLine, the JDK and {@code io.github.headlesshq.headlessmc} classes.
 */
@SuppressWarnings("unused") // instantiated reflectively by JlineLoader
public final class IsolatedJlineArgSplitter implements ArgSplitter {
    @Override
    public String[] split(String line) {
        Parser parser = new DefaultParser();
        return parser.parse(line, line.length(), Parser.ParseContext.ACCEPT_LINE)
            .words()
            .toArray(new String[0]);
    }

}
