package org.patryk3211.powergrid.electricity.numericaldisplay.modules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.electricity.numericaldisplay.IDisplayModule;

public class zeroToNineNumberModule implements IDisplayModule {
    private final int Index;
    private final boolean halfClick;
    //counts 0,1,2,3,4,5,6,7,8,9,blank,0 (first number repeated for smooth transition)
    public zeroToNineNumberModule(int Index, boolean halfClick) {
        if (Index < 0 || Index > getDisplayTextureCharacterCount() + 2)
            throw new IllegalArgumentException("Index must be 0-"+getDisplayTextureCharacterCount() + 2 + ", got: " + Index);
        this.Index = Index;
        this.halfClick = halfClick;
    }

    @Override
    public IDisplayModule withIndex(int newIndex) {
        return new zeroToNineNumberModule(newIndex, this.halfClick);
    }

    @Override
    public IDisplayModule withHalfClick(boolean halfClick) {
        return new zeroToNineNumberModule(this.Index, halfClick);
    }

    @Override
    public ResourceLocation getDisplayTexture() {
        return PowerGrid.texture("block/numerical_display/zerotonine");
    }

    public float getDisplayTextureSize() {
        return 80f;
    }
    public int getDisplayTextureCharacterCount() {
        return 9;
    }

    @Override
    public boolean getHalfClick() {
        return halfClick;
    }

    @Override public ModuleType getType() { return ModuleType.DIGIT; }
    @Override public int getIndex() { return Index; }
    @Override public String serialize() { return "zerotonine:" + Index + ":" + halfClick; }

    @Override
    public ItemStack toItemStack() {
        return new ItemStack(ModdedItems.ZEROTONINE_NUMBER_MODULE.get());
    }
}
