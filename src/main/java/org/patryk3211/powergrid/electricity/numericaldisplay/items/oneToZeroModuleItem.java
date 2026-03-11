package org.patryk3211.powergrid.electricity.numericaldisplay.items;


import net.minecraft.resources.ResourceLocation;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.components.NumericalDisplayComponent;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;

public class oneToZeroModuleItem extends NumericalDisplayComponent {
    public oneToZeroModuleItem(ComponentFootprint footprint) {
        super(footprint, PowerGrid.texture("block/numerical_display/onetozero"), 9f, 80f);
    }
}
