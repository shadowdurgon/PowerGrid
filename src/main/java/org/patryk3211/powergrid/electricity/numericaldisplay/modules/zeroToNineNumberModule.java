package org.patryk3211.powergrid.electricity.numericaldisplay.modules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.electricity.numericaldisplay.IDisplayModule;

public class zeroToNineNumberModule implements IDisplayModule {
    private final int digit;
    private final boolean halfClick;
    //counts 0,1,2,3,4,5,6,7,8,9,blank,0 (first number repeated for smooth transition)
    public zeroToNineNumberModule(int digit, Boolean halfClick) {
        if (digit < 0 || digit > 50)
            throw new IllegalArgumentException("Digit must be 0-12, got: " + digit);
        this.digit = digit;
        this.halfClick = halfClick;
    }

    @Override
    public ResourceLocation getDisplayTexture() {
        return PowerGrid.texture("block/numerical_display/zerotonine");
    }

    @Override
    public boolean getHalfClick() {
        return halfClick;
    }

    @Override public ModuleType getType() { return ModuleType.DIGIT; }
    @Override public int getDigit() { return digit; }
    @Override public String serialize() { return "zerotonine:" + digit + ":" + halfClick; }

    @Override
    public ItemStack toItemStack() {
        return new ItemStack(ModdedItems.ZEROTONINE_NUMBER_MODULE.get());
    }
}
