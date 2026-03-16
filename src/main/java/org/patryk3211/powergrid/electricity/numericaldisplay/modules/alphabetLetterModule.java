package org.patryk3211.powergrid.electricity.numericaldisplay.modules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.numericaldisplay.DisplayModuleType;
import org.patryk3211.powergrid.electricity.numericaldisplay.IDisplayModule;

public class alphabetLetterModule implements IDisplayModule {
    private final int Index;
    private final boolean halfClick;
    private final DyeColor color;
    //counts A,B,C,D,E,F...X,Y,Z,Blank,A (first symbol repeated for smooth transition)
    public alphabetLetterModule(int Index, boolean halfClick, DyeColor color) {
        if (Index < 0 || Index > getDisplayTextureCharacterCount() + 3)
            throw new IllegalArgumentException("Index must be 0-"+ (getDisplayTextureCharacterCount() + 2) + ", got: " + Index);
        this.Index = Index;
        this.halfClick = halfClick;
        this.color = color;
    }

    @Override
    public IDisplayModule withIndex(int newIndex) {
        return new alphabetLetterModule(newIndex, this.halfClick, this.color);
    }

    @Override
    public IDisplayModule withHalfClick(boolean halfClick) {
        return new alphabetLetterModule(this.Index, halfClick,  this.color);
    }

    @Override
    public IDisplayModule withColor(DyeColor color) {
        return new alphabetLetterModule(this.Index, this.halfClick,  color);
    }

    public float getDisplayTextureSize() {
        return 176f;
    }

    public int getDisplayTextureCharacterCount() {
        return 25;
    }

    @Override
    public ResourceLocation getDisplayTexture() {
        return PowerGrid.texture("block/numerical_display/alphabet");
    }

    public boolean getHalfClick() {
        return halfClick;
    }

    @Override
    public ModuleType getType() {
        return ModuleType.LETTER;
    }

    @Override
    public DisplayModuleType getDisplayModuleType() {
        return DisplayModuleType.ALPHABET;
    }

    @Override public int getIndex() {
        return Index;
    }

    @Override
    public DyeColor getColor() {
        return color;
    }

    @Override public String serialize() {
        return "alphabet:" + Index + ":" + halfClick + ":" + color.getName();
    }
}
