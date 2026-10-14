package me.earth.headlessmc.mc.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.SuggestionContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is essentially {@code net.minecraft.client.gui.components.CommandSuggestions} hacked for our purposes.
 *
 * @param <T> the type of SharedSuggestionProvider
 */
public class BrigadierSuggestionsProvider<T> {
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("(\\s+)");
    private final List<Map.Entry<String, String>> commandUsage = new ArrayList<>();

    private final CommandDispatcher<T> dispatcher;
    private final T suggestionsProvider;
    private final Collection<String> customTabSugggestions;
    private final BiFunction<Collection<String>, SuggestionsBuilder, CompletableFuture<Suggestions>> suggestFunction;
    private final String line;

    private CompletableFuture<Suggestions> pendingSuggestions;
    private ParseResults<T> currentParse;

    private boolean onlyShowIfCursorPastError = false;
    private boolean commandsOnly = false;

    public BrigadierSuggestionsProvider(CommandDispatcher<T> dispatcher,
                                        T suggestionsProvider,
                                        Collection<String> customTabSugggestions,
                                        BiFunction<Collection<String>, SuggestionsBuilder, CompletableFuture<Suggestions>> suggestFunction,
                                        String line) {
        this.dispatcher = dispatcher;
        this.suggestionsProvider = suggestionsProvider;
        this.customTabSugggestions = customTabSugggestions;
        this.suggestFunction = suggestFunction;
        this.line = line;
    }

    public List<Map.Entry<String, String>> getCompletions() {
        updateCommandInfo();
        List<Map.Entry<String, String>> result = new ArrayList<>();
        if (this.pendingSuggestions != null) {
            Suggestions suggestions;
            try {
                suggestions = this.pendingSuggestions.get(1, TimeUnit.SECONDS);
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                return result;
            }

            if (!suggestions.isEmpty()) {
                for (Suggestion suggestion : this.sortSuggestions(suggestions)) {
                    result.add(new AbstractMap.SimpleEntry<>(suggestion.getText(), null));
                }
            } else {
                result.addAll(commandUsage);
            }
        }

        return result;
    }

    public boolean checkIfCommandAndSkipPrefix(StringReader stringReader) {
        boolean isCommand = stringReader.canRead() && stringReader.peek() == '/';
        if (isCommand) {
            stringReader.skip();
        }

        return isCommand;
    }

    public void updateCommandInfo() {
        if (this.currentParse != null && !this.currentParse.getReader().getString().equals(line)) {
            this.currentParse = null;
        }

        StringReader stringReader = new StringReader(line);
        boolean isCommand = checkIfCommandAndSkipPrefix(stringReader);

        boolean commandOrCommandsOnly = commandsOnly || isCommand;
        int length = this.line.length();
        if (commandOrCommandsOnly) {
            CommandDispatcher<T> commandDispatcher = dispatcher;
            if (this.currentParse == null) {
                this.currentParse = commandDispatcher.parse(stringReader, suggestionsProvider);
            }

            int cursor = onlyShowIfCursorPastError ? stringReader.getCursor() : 1;
            if (length >= cursor) {
                this.pendingSuggestions = commandDispatcher.getCompletionSuggestions(this.currentParse, length);
            }
        } else {
            int lastWordIndex = getLastWordIndex(line);
            this.pendingSuggestions = suggestFunction.apply(customTabSugggestions, new SuggestionsBuilder(line, lastWordIndex));
        }
    }

    private List<Suggestion> sortSuggestions(Suggestions suggestions) {
        int lastWordIndex = getLastWordIndex(line);
        String lineWithoutLastWord = line.substring(lastWordIndex).toLowerCase(Locale.ROOT);
        List<Suggestion> result = new ArrayList<>();
        List<Suggestion> mcSuggestions = new ArrayList<>();

        for (Suggestion suggestion : suggestions.getList()) {
            if (!suggestion.getText().startsWith(lineWithoutLastWord) && !suggestion.getText().startsWith("minecraft:" + lineWithoutLastWord)) {
                mcSuggestions.add(suggestion);
            } else {
                result.add(suggestion);
            }
        }

        result.addAll(mcSuggestions);
        return result;
    }

    private boolean fillNodeUsage() {
        CommandContextBuilder<T> commandContextBuilder = this.currentParse.getContext();
        SuggestionContext<T> suggestionContext = commandContextBuilder.findSuggestionContext(this.line.length());
        Map<CommandNode<T>, String> map = dispatcher.getSmartUsage(suggestionContext.parent, suggestionsProvider);
        List<Map.Entry<String, String>> list = new ArrayList<>();
        for (Map.Entry<CommandNode<T>, String> entry : map.entrySet()) {
            if (!(entry.getKey() instanceof LiteralCommandNode)) {
                list.add(new AbstractMap.SimpleEntry<>(entry.getValue(), null));
            }
        }

        if (!list.isEmpty()) {
            this.commandUsage.addAll(list);
            return true;
        } else {
            return false;
        }
    }

    private static int getLastWordIndex(String string) {
        if (string == null || string.isEmpty()) {
            return 0;
        } else {
            int i = 0;
            Matcher matcher = WHITESPACE_PATTERN.matcher(string);
            while (matcher.find()) {
                i = matcher.end();
            }

            return i;
        }
    }

    private static <S> CommandSyntaxException getParseException(ParseResults<S> parseResults) {
        if (!parseResults.getReader().canRead()) {
            return null;
        } else if (parseResults.getExceptions().size() == 1) {
            return parseResults.getExceptions().values().iterator().next();
        } else {
            return parseResults.getContext().getRange().isEmpty()
                ? CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(parseResults.getReader())
                : CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(parseResults.getReader());
        }
    }

    public boolean isCommandsOnly() {
        return commandsOnly;
    }

    public void setCommandsOnly(boolean commandsOnly) {
        this.commandsOnly = commandsOnly;
    }

    public boolean isOnlyShowIfCursorPastError() {
        return onlyShowIfCursorPastError;
    }

    public void setOnlyShowIfCursorPastError(boolean onlyShowIfCursorPastError) {
        this.onlyShowIfCursorPastError = onlyShowIfCursorPastError;
    }

}
