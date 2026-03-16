package org.patryk3211.powergrid.electricity.numericaldisplay.modules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.numericaldisplay.DisplayModuleType;
import org.patryk3211.powergrid.electricity.numericaldisplay.IDisplayModule;

public class symbolLetterModule implements IDisplayModule {
    private final int Index;
    private final boolean halfClick;
    private final DyeColor color;
    //counts .,comma,<,>,=,+,-,x,/,blank,. (first symbol repeated for smooth transition)
    public symbolLetterModule(int Index, boolean halfClick, DyeColor color){
        if (Index < 0 || Index > getDisplayTextureCharacterCount() + 3)
            throw new IllegalArgumentException("Index must be 0-"+ (getDisplayTextureCharacterCount() + 2) + ", got: " + Index);
        this.Index = Index;
        this.halfClick = halfClick;
        this.color = color;
    }

    @Override
    public IDisplayModule withIndex(int newIndex) {
        return new symbolLetterModule(newIndex, this.halfClick, this.color);
    }

    @Override
    public IDisplayModule withHalfClick(boolean halfClick) {
        return new symbolLetterModule(this.Index, halfClick, this.color);
    }

    @Override
    public IDisplayModule withColor(DyeColor color) {
        return new symbolLetterModule(this.Index, this.halfClick, color);
    }

    public float getDisplayTextureSize() {
        return 80f;
    }

    public int getDisplayTextureCharacterCount() {
        return 8;
    }

    @Override
    public ResourceLocation getDisplayTexture() {
        return PowerGrid.texture("block/numerical_display/symbols");
    }

    public DisplayModuleType getDisplayModuleType() {
        return DisplayModuleType.SYMBOLS;
    }

    public boolean getHalfClick() {
        return halfClick;
    }

    @Override
    public ModuleType getType() {
        return ModuleType.LETTER;
    }

    @Override public int getIndex() {
        return Index;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override public String serialize() {
        return "symbol:" + Index + ":" + halfClick + ":" + color;
    }
}
