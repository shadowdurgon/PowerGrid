package org.patryk3211.powergrid.electricity.numericaldisplay.items;

import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.components.NumericalDisplayComponent;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;

public class zeroToNineModuleItem extends NumericalDisplayComponent {
    public zeroToNineModuleItem(ComponentFootprint footprint) {
        super(footprint, PowerGrid.texture("block/numerical_display/zerotonine"), 9f, 80f);
    }
}
