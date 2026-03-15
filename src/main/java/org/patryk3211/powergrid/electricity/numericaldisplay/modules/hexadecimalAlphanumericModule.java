package org.patryk3211.powergrid.electricity.numericaldisplay.modules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.numericaldisplay.DisplayModuleType;
import org.patryk3211.powergrid.electricity.numericaldisplay.IDisplayModule;

public class hexadecimalAlphanumericModule implements IDisplayModule {
    private final int Index;
    private final boolean halfClick;
    private final DyeColor color;
    //counts 0,1,2,3,4,5,6,7,8,9,A,B,C,D,E,F,blank,0 (first number repeated for smooth transition)
    public hexadecimalAlphanumericModule(int Index, boolean halfClick, DyeColor color) {
        if (Index < 0 || Index > getDisplayTextureCharacterCount() + 2)
            throw new IllegalArgumentException("Index must be 0-"+getDisplayTextureCharacterCount() + 2 + ", got: " + Index);
        this.Index = Index;
        this.halfClick = halfClick;
        this.color = color;
    }

    @Override
    public IDisplayModule withIndex(int newIndex) {
        return new hexadecimalAlphanumericModule(newIndex, this.halfClick, this.color);
    }

    @Override
    public IDisplayModule withHalfClick(boolean halfClick) {
        return new hexadecimalAlphanumericModule(this.Index, halfClick, this.color);
    }

    @Override
    public IDisplayModule withColor(DyeColor color) {
        return new hexadecimalAlphanumericModule(this.Index, this.halfClick, color);
    }

    public float getDisplayTextureSize() {
        return 112f;
    }

    public int getDisplayTextureCharacterCount() {
        return 15;
    }

    @Override
    public ResourceLocation getDisplayTexture() {
        return PowerGrid.texture("block/numerical_display/zerotof");
    }

    public DisplayModuleType getDisplayModuleType() {
        return DisplayModuleType.HEXADECIMAL;
    }

    public boolean getHalfClick() {
        return halfClick;
    }

    @Override
    public ModuleType getType() {
        return ModuleType.ALPHANUMERIC;
    }

    @Override
    public int getIndex() {
        return Index;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override
    public String serialize() {
        return "hexadecimal:" + Index + ":" + halfClick + ":" + color;
    }

}