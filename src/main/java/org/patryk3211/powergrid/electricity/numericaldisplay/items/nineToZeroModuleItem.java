package org.patryk3211.powergrid.electricity.numericaldisplay.items;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.components.NumericalDisplayComponent;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;

public class nineToZeroModuleItem extends NumericalDisplayComponent {
    public nineToZeroModuleItem(ComponentFootprint footprint) {
        super(footprint, PowerGrid.texture("block/numerical_display/ninetozero"), 9f, 80f);
    }
}
