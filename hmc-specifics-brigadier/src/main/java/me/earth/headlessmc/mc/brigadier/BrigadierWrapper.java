package me.earth.headlessmc.mc.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;

/**
 * Wrapper around brigadier because Mc ships different versions, so I am just careful.
 * I have made bad experiences on forge with even loading classes that contain references to classes not available.
 */
public class BrigadierWrapper {
    public static <T> List<Map.Entry<String, String>> getCompletions(CommandDispatcher<T> dispatcher,
                                                                     T suggestionsProvider,
                                                                     Collection<String> customTabSugggestions,
                                                                     BiFunction<Collection<String>, SuggestionsBuilder, CompletableFuture<Suggestions>> suggestFunction,
                                                                     String line) {
        try {
            BrigadierSuggestionsProvider<T> provider = new BrigadierSuggestionsProvider<>(
                dispatcher, suggestionsProvider, customTabSugggestions, suggestFunction, line);
            return provider.getCompletions();
        } catch (Throwable throwable) {
            throwable.printStackTrace(System.err);
            return new ArrayList<>();
        }
    }

}
