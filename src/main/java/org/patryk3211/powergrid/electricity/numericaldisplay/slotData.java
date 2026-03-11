package org.patryk3211.powergrid.electricity.numericaldisplay;

import org.jetbrains.annotations.Nullable;

public class slotData {

    @Nullable
    private final IDisplayModule module;

    public slotData(@Nullable IDisplayModule module) {
        this.module = module;
    }

    public static slotData empty() {
        return new slotData(null);
    }

    public boolean isEmpty()    { return module == null; }
    public boolean isBlanking() { return module != null && module.getType() == IDisplayModule.ModuleType.BLANKING; }
    //public boolean isDigit()    { return module != null && module.getType() == IDisplayModule.ModuleType.DIGIT; }

    public boolean isHalfClick() { return module.getHalfClick();}

    public int getIndex() {
        return module != null ? module.getIndex() : -1;
    }

    @Nullable
    public IDisplayModule getModule() { return module; }
}
