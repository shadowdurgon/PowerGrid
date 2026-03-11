package org.patryk3211.powergrid.electricity.numericaldisplay.items;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.components.NumericalDisplayComponent;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;

public class alphabetModuleItem extends NumericalDisplayComponent {
    public alphabetModuleItem(ComponentFootprint footprint) {
        super(footprint, PowerGrid.texture("block/numerical_display/alphabet"), 25f, 176f);
    }
}
