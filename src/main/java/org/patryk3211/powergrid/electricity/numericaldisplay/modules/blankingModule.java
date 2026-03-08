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
    public ResourceLocation getMoudleModel() {
        return IDisplayModule.super.getMoudleModel();
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
