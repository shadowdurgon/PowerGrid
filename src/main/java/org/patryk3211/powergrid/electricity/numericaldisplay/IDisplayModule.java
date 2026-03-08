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

    default boolean getHalfClick() {return false;}

    default int getDigit() {return -1;}
    /**
     Gets texture for screen
    */
    default ResourceLocation getDisplayTexture() {return null;}

    /**
     Gets texture for module body
     */
    default ResourceLocation getMoudleModel() {return null;}
    String serialize();
    ItemStack toItemStack();

}
