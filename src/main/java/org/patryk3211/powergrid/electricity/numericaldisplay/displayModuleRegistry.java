package org.patryk3211.powergrid.electricity.numericaldisplay;

import org.patryk3211.powergrid.electricity.numericaldisplay.modules.blankingModule;
import org.patryk3211.powergrid.electricity.numericaldisplay.modules.oneToZeroNumberModule;
import org.patryk3211.powergrid.electricity.numericaldisplay.modules.zeroToNineNumberModule;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class displayModuleRegistry {

    private static final Map<String, Function<String, IDisplayModule>> DESERIALIZERS = new HashMap<>();

    static {
        register("onetozero", value -> new oneToZeroNumberModule(Integer.parseInt(value)));
        register("blanking", value -> new blankingModule());
        //register("zerotonine", value -> new zeroToNineNumberModule(Integer.parseInt(value), Boolean.parseBoolean()));
        register("zerotonine", value -> {
            String[] parts = value.split(":");
            int digit = Integer.parseInt(parts[0]);
            boolean halfClick = parts.length > 1 && Boolean.parseBoolean(parts[1]);
            return new zeroToNineNumberModule(digit, halfClick);
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
