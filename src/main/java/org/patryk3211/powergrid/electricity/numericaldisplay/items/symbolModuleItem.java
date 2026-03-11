package org.patryk3211.powergrid.electricity.numericaldisplay.items;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.components.NumericalDisplayComponent;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;

public class symbolModuleItem extends NumericalDisplayComponent {
    public symbolModuleItem(ComponentFootprint footprint) {
        super(footprint, PowerGrid.texture("block/numerical_display/symbols"), 8f, 80f);
    }
}
