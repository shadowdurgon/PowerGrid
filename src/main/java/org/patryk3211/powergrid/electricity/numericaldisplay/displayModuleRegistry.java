package org.patryk3211.powergrid.electricity.numericaldisplay;

import net.minecraft.world.item.DyeColor;
import org.patryk3211.powergrid.electricity.numericaldisplay.modules.*;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class displayModuleRegistry {

    private static final Map<String, Function<String, IDisplayModule>> DESERIALIZERS = new HashMap<>();

    static {
        register("onetozero", value -> {
            String[] parts = value.split(":");
            int digit = Integer.parseInt(parts[0]);
            boolean halfClick = parts.length > 1 && Boolean.parseBoolean(parts[1]);
            DyeColor color = DyeColor.byName(parts[2], DyeColor.WHITE);
            return new oneToZeroNumberModule(digit, halfClick, color);
        });

        register("zerotonine", value -> {
            String[] parts = value.split(":");
            int digit = Integer.parseInt(parts[0]);
            boolean halfClick = parts.length > 1 && Boolean.parseBoolean(parts[1]);
            DyeColor color = DyeColor.byName(parts[2], DyeColor.WHITE);
            return new zeroToNineNumberModule(digit, halfClick, color);
        });
        register("symbol", value -> {
            String[] parts = value.split(":");
            int digit = Integer.parseInt(parts[0]);
            boolean halfClick = parts.length > 1 && Boolean.parseBoolean(parts[1]);
            DyeColor color = DyeColor.byName(parts[2], DyeColor.WHITE);
            return new symbolLetterModule(digit, halfClick, color);
        });
        register("hexadecimal", value -> {
            String[] parts = value.split(":");
            int digit = Integer.parseInt(parts[0]);
            boolean halfClick = parts.length > 1 && Boolean.parseBoolean(parts[1]);
            DyeColor color = DyeColor.byName(parts[2], DyeColor.WHITE);
            return new hexadecimalAlphanumericModule(digit, halfClick, color);
        });
        register("ninetozero", value -> {
            String[] parts = value.split(":");
            int digit = Integer.parseInt(parts[0]);
            boolean halfClick = parts.length > 1 && Boolean.parseBoolean(parts[1]);
            DyeColor color = DyeColor.byName(parts[2], DyeColor.WHITE);
            return new nineToZeroNumberModule(digit, halfClick, color);
        });
        register("alphabet", value -> {
            String[] parts = value.split(":");
            int digit = Integer.parseInt(parts[0]);
            boolean halfClick = parts.length > 1 && Boolean.parseBoolean(parts[1]);
            DyeColor color = DyeColor.byName(parts[2], DyeColor.WHITE);
            return new alphabetLetterModule(digit, halfClick, color);
        });

    }

    public static void register(String key, Function<String, IDisplayModule> factory) {
        DESERIALIZERS.put(key, factory);
    }

    public static IDisplayModule deserialize(String serialized) {
        if (serialized == null || serialized.isBlank()) return null;

        String[] parts = serialized.split(":", 2);
        String key   = parts[0];
        String value = parts.length > 1 ? parts[1] : "";

        Function<String, IDisplayModule> factory = DESERIALIZERS.get(key);
        if (factory == null) return null;

        try {
            return factory.apply(value);
        } catch (Exception e) {
            return null;
        }
    }
}
