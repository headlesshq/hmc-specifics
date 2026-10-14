package me.earth.headlessmc.mc.adventure;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.TranslationRegistry;
import net.kyori.adventure.translation.Translator;
import net.kyori.ansi.ColorLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * We want color in the console! On older versions of Minecraft this is easy, color codes are simply a § character followed by a number or letter and we could easily use an own converter or the MinecraftFormattingConverter in
 * TerminalConsoleAppender. However, newer versions use much more complicated TextComponents. So here we use adventures {@link ANSIComponentSerializer} to convert such text components to a readable string containing ANSI escape sequences.
 *
 * @see <a href=https://github.com/Minecrell/TerminalConsoleAppender/issues/18>https://github.com/Minecrell/TerminalConsoleAppender/issues/18</a>
 */
public class AdventureHelperImpl implements AdventureHelper {
    private static final Pattern LOCALIZATION_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?s");
    private static final Logger LOGGER = Logger.getLogger(AdventureHelperImpl.class.getName());
    public final ComponentFlattener flattener;
    public final ANSIComponentSerializer ansiSerializer;

    public AdventureHelperImpl(Function<String, Boolean> languageHasKey, BiFunction<String, String, String> languageGetOrDefault) {
        ColorLevel colorLevel = ColorLevel.compute();
        LOGGER.info("ColorLevel: " + colorLevel);
        if (colorLevel.equals(ColorLevel.NONE)) {
            if (!AdventureWrapper.USE_NONE_COLOR_LEVEL) {
                colorLevel = ColorLevel.INDEXED_16;
            }
        }

        // PaperAdventure!!!
        this.flattener = ComponentFlattener
            .basic()
            .toBuilder()
            .complexMapper(TranslatableComponent.class, (translatable, consumer) -> {
                if (!languageHasKey.apply(translatable.key())) {
                    for (final Translator source : GlobalTranslator.translator().sources()) {
                        if (source instanceof TranslationRegistry && ((TranslationRegistry) source).contains(translatable.key())) {
                            consumer.accept(GlobalTranslator.render(translatable, Locale.US));
                            return;
                        }
                    }
                }

                final @Nullable String fallback = translatable.fallback();
                final @NotNull String translated = languageGetOrDefault.apply(translatable.key(), fallback != null ? fallback : translatable.key());

                final Matcher matcher = LOCALIZATION_PATTERN.matcher(translated);
                final List<TranslationArgument> args = translatable.arguments();
                int argPosition = 0;
                int lastIdx = 0;
                while (matcher.find()) {
                    // append prior
                    if (lastIdx < matcher.start()) {
                        consumer.accept(Component.text(translated.substring(lastIdx, matcher.start())));
                    }
                    lastIdx = matcher.end();

                    final @Nullable String argIdx = matcher.group(1);
                    // calculate argument position
                    if (argIdx != null) {
                        try {
                            final int idx = Integer.parseInt(argIdx) - 1;
                            if (idx < args.size()) {
                                consumer.accept(args.get(idx).asComponent());
                            }
                        } catch (final NumberFormatException ex) {
                            // ignore, drop the format placeholder
                        }
                    } else {
                        final int idx = argPosition++;
                        if (idx < args.size()) {
                            consumer.accept(args.get(idx).asComponent());
                        }
                    }
                }

                // append tail
                if (lastIdx < translated.length()) {
                    consumer.accept(Component.text(translated.substring(lastIdx)));
                }
            }).build();

        ansiSerializer = ANSIComponentSerializer.builder().colorLevel(colorLevel).flattener(flattener).build();
    }

    @Override
    public String toAnsiStringJson(String jsonString) {
        Component jsonComponent = GsonComponentSerializer.gson().deserialize(jsonString);
        return "\u001B[m" + ansiSerializer.serialize(jsonComponent);
    }

    @Override
    public String toAnsiStringLegacy(String legacyString) {
        Component component = LegacyComponentSerializer.legacySection().deserialize(legacyString);
        return ansiSerializer.serialize(component);
    }

}
