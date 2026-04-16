package org.patryk3211.powergrid.circuits.components;

import com.google.common.collect.ImmutableCollection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.circuitboard.ComponentCircuitBuilder;
import org.patryk3211.powergrid.circuits.components.properties.BooleanProperty;
import org.patryk3211.powergrid.circuits.components.properties.ComponentProperty;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.thermal.ThermalBuilder;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;
import org.patryk3211.powergrid.electricity.sim.SwitchedWire;

import java.util.Collection;
import java.util.List;

public class DPDTSwitchComponent extends OrientableComponent implements IInteractableComponent, IGoggleLabel {
    public static final BooleanProperty STATE = new BooleanProperty(PowerGrid.MOD_ID, "switch_state");

    public DPDTSwitchComponent(ComponentFootprint footprint) {
        super(footprint);
    }

    @Override
    protected void addProperties(ImmutableCollection.Builder<ComponentProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(STATE, LABEL, current(16));
    }

    @Override
    public void bake(@NotNull PlacedComponent placed, @NotNull ComponentCircuitBuilder builder, ThermalBuilder.@NotNull IEmitter thermals) {

        var wire1 = builder.connectSwitch(0.1f, builder.terminalNode(0), builder.terminalNode(1), placed.get(STATE));
        var wire2 = builder.connectSwitch(0.1f, builder.terminalNode(0), builder.terminalNode(2), !(placed.get(STATE)));
        var wire3 = builder.connectSwitch(0.1f, builder.terminalNode(3), builder.terminalNode(4), placed.get(STATE));
        var wire4 = builder.connectSwitch(0.1f, builder.terminalNode(3), builder.terminalNode(5), !(placed.get(STATE)));
        placed.add(wire1); placed.add(wire2); placed.add(wire3); placed.add(wire4);
        thermals.builder()
                .setMaxCurrent(16, 0.1f, 150)
                .setThermalMass(0.01f)
                .addHeatSource(wire1)
                .addHeatSource(wire2);
    }

    @Override
    public VoxelShape getShape(@NotNull PlacedComponent placed) {
        return IInteractableComponent.extrudedFootprint(placed, 2 / 16f);
    }

    @Override
    public InteractionResult use(CircuitBoardBlockEntity be, PlacedComponent placed, Player player) {
        var newState = !placed.get(STATE);
        placed.set(STATE, newState);

        if(be.getLevel().isClientSide) {
            Component.modelChanged(be.getBlockPos());
        } else {
            if(newState) {
                ModdedSoundEvents.MICROSWITCH_ON.playOnServer(be.getLevel(), be.getBlockPos());
            } else {
                ModdedSoundEvents.MICROSWITCH_OFF.playOnServer(be.getLevel(), be.getBlockPos());
            }
            placed.notifyClients(STATE);
            stateUpdated(placed);
        }
        be.setChanged();
        return InteractionResult.SUCCESS;
    }

    @Override
    public void stateUpdated(@NotNull PlacedComponent placed) {
        super.stateUpdated(placed);
        if(placed.wires.isEmpty())
            return;
        ((SwitchedWire) placed.wires.get(0)).setState(placed.get(STATE));
        ((SwitchedWire) placed.wires.get(1)).setState(!(placed.get(STATE)));
        ((SwitchedWire) placed.wires.get(2)).setState(placed.get(STATE));
        ((SwitchedWire) placed.wires.get(3)).setState(!(placed.get(STATE)));
        placed.onClientWorld(() -> world -> modelChanged(placed.getPos()));
    }

    @Override
    public @NotNull ResourceLocation getModelId(@NotNull PlacedComponent component) {
        return component.get(STATE)
                ? PowerGrid.asResource("switch_on")
                : PowerGrid.asResource("switch");
    }

    @Override
    public @NotNull Collection<ResourceLocation> requestedModels() {
        return List.of(
                PowerGrid.asResource("switch"),
                PowerGrid.asResource("switch_on")
        );
    }
}
