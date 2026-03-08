package org.patryk3211.powergrid.electricity.numericaldisplay.modules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.electricity.numericaldisplay.IDisplayModule;

public class oneToZeroNumberModule implements IDisplayModule {
    private final int digit;
    //counts 1,2,3,4,5,6,7,8,9,0,blank,1 (first number repeated for smooth transition)
    public oneToZeroNumberModule(int digit) {
        if (digit < 0 || digit > 11)
            throw new IllegalArgumentException("Digit must be 0-11, got: " + digit);
        this.digit = digit;
    }


    @Override
    public ResourceLocation getDisplayTexture() {
        return PowerGrid.texture("block/numerical_display/onetozero");
    }

    @Override public ModuleType getType() { return ModuleType.DIGIT; }
    @Override public int getDigit() { return digit; }
    @Override public String serialize() { return "onetozero:" + digit; }

    @Override
    public ItemStack toItemStack() {
        return new ItemStack(ModdedItems.ONETOZERO_NUMBER_MODULE.get());
    }
}
