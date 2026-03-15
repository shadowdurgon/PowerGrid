package org.patryk3211.powergrid.electricity.numericaldisplay;
import com.mojang.datafixers.TypeRewriteRule;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;

public enum DisplayModuleType implements INamedIconOptions {
    ZERO_TO_NINE("0 - 9", AllIcons.I_NONE),
    NINE_TO_ZERO("9 - 0", AllIcons.I_NONE),
    ONE_TO_ZERO("1 - 0", AllIcons.I_NONE),
    HEXADECIMAL("Hexadecimal", AllIcons.I_NONE),
    SYMBOLS("Symbols", AllIcons.I_NONE),
    ALPHABET("Alphabet", AllIcons.I_NONE);



    private final String name;
    private final AllIcons icon;

    DisplayModuleType(String name, AllIcons icon) {
        this.name = name;
        this.icon = icon;
    }

    @Override
    public String getTranslationKey() {
        return name;
    }

    @Override
    public AllIcons getIcon() {
        return icon;
    }
}