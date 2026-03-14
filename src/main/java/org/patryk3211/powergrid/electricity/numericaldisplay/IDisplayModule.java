package org.patryk3211.powergrid.electricity.numericaldisplay;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public interface IDisplayModule {

    enum ModuleType {
        CUSTOM,
        DIGIT,                      // pure numbers
        LETTER,                     // pure letters
        ALPHANUMERIC,               // number letter mix
        BLANKING                    // blanking plate
    }

    ModuleType getType();

    DisplayModuleType getDisplayModuleType();

    default boolean getHalfClick() {return false;}

    default int getIndex() {return -1;}

    IDisplayModule withIndex(int newIndex);

    IDisplayModule withHalfClick(boolean halfClick);

    default ResourceLocation getDisplayTexture() {return null;}

    default float getDisplayTextureSize() {return 0;}

    default int getDisplayTextureCharacterCount() {return 0;}

    default ResourceLocation getModuleModel() {return null;}

    String serialize();

}
