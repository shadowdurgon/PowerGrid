package org.patryk3211.powergrid.electricity.numericaldisplay.modules;

import net.minecraft.resources.ResourceLocation;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.electricity.numericaldisplay.DisplayModuleType;
import org.patryk3211.powergrid.electricity.numericaldisplay.IDisplayModule;

public class alphabetLetterModule implements IDisplayModule {
    private final int Index;
    private final boolean halfClick;
    //counts .,comma,<,>,=,+,-,x,/,blank,. (first symbol repeated for smooth transition)
    public alphabetLetterModule(int Index, boolean halfClick){
        if (Index < 0 || Index > getDisplayTextureCharacterCount() + 2)
            throw new IllegalArgumentException("Index must be 0-"+getDisplayTextureCharacterCount() + 2 + ", got: " + Index);
        this.Index = Index;
        this.halfClick = halfClick;
    }

    @Override
    public IDisplayModule withIndex(int newIndex) {
        return new alphabetLetterModule(newIndex, this.halfClick);
    }

    @Override
    public IDisplayModule withHalfClick(boolean halfClick) {
        return new alphabetLetterModule(this.Index, halfClick);
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

    @Override public String serialize() {
        return "alphabet:" + Index + ":" + halfClick;
    }
}
