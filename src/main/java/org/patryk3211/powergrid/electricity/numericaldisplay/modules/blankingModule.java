package org.patryk3211.powergrid.electricity.numericaldisplay.modules;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.electricity.numericaldisplay.IDisplayModule;

public class blankingModule implements IDisplayModule {

    @Override
    public ModuleType getType() {
        return ModuleType.BLANKING;
    }

    @Override
    public IDisplayModule withIndex(int newDigit) {
        return new blankingModule();
    }

    @Override
    public IDisplayModule withHalfClick(boolean halfClick) {
        return new blankingModule();
    }

    @Override
    public ResourceLocation getModuleModel() {
        return IDisplayModule.super.getModuleModel();
    }

    @Override
    public String serialize() {
        return "blank";
    }
    @Override
    public ItemStack toItemStack() {
        return new ItemStack(ModdedItems.BLANKING_MODULE.get());
    }
}
