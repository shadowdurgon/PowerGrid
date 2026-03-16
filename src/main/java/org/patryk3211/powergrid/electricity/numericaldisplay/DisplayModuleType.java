package org.patryk3211.powergrid.electricity.numericaldisplay;
import com.mojang.datafixers.TypeRewriteRule;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.simibubi.create.foundation.gui.AllIcons;
import org.patryk3211.powergrid.utility.Lang;

public enum DisplayModuleType implements INamedIconOptions {
    ZERO_TO_NINE(Lang.translateDirect("gui.modular_display.0 - 9").toString(), AllIcons.I_NONE), // 0-9
    NINE_TO_ZERO(Lang.translateDirect("gui.modular_display.9 - 0").toString(), AllIcons.I_NONE),
    ONE_TO_ZERO(Lang.translateDirect("gui.modular_display.1 - 0").toString(), AllIcons.I_NONE),
    HEXADECIMAL(Lang.translateDirect("gui.modular_display.hexadecimal").toString(), AllIcons.I_NONE),
    SYMBOLS(Lang.translateDirect("gui.modular_display.symbols").toString(), AllIcons.I_NONE),
    ALPHABET(Lang.translateDirect("gui.modular_display.alphabet").toString(), AllIcons.I_NONE);



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