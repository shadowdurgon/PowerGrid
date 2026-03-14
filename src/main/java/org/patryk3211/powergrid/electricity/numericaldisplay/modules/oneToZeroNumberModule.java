package org.patryk3211.powergrid.electricity.numericaldisplay.modules;

import net.minecraft.resources.ResourceLocation;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.numericaldisplay.DisplayModuleType;
import org.patryk3211.powergrid.electricity.numericaldisplay.IDisplayModule;

public class oneToZeroNumberModule implements IDisplayModule {
    private final int Index;
    private final boolean halfClick;
    //counts 1,2,3,4,5,6,7,8,9,0,blank,1 (first number repeated for smooth transition)
    public oneToZeroNumberModule(int Index, boolean halfClick) {
        if (Index < 0 || Index > getDisplayTextureCharacterCount() + 2)
            throw new IllegalArgumentException("Index must be 0-"+getDisplayTextureCharacterCount() + 2 + ", got: " + Index);
        this.Index = Index;
        this.halfClick = halfClick;
    }

    @Override
    public IDisplayModule withIndex(int newIndex) {
        return new oneToZeroNumberModule(newIndex, this.halfClick);
    }

    @Override
    public IDisplayModule withHalfClick(boolean halfClick) {
        return new oneToZeroNumberModule(this.Index, halfClick);
    }

    public float getDisplayTextureSize() {
        return 80f;
    }

    public int getDisplayTextureCharacterCount() {
        return 9;
    }

    @Override
    public ResourceLocation getDisplayTexture() {
        return PowerGrid.texture("block/numerical_display/onetozero");
    }

    public DisplayModuleType getDisplayModuleType() {
        return DisplayModuleType.ONE_TO_ZERO;
    }

    public boolean getHalfClick() {
        return halfClick;
    }

    @Override public ModuleType getType() { return ModuleType.DIGIT; }

    @Override public int getIndex() { return Index; }

    @Override public String serialize() { return "onetozero:" + Index + ":" + halfClick; }
}
