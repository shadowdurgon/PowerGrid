/*
 * Copyright 2025 patryk3211
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.patryk3211.powergrid.circuits.components;

import com.google.common.collect.ImmutableCollection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

public class SwitchComponent extends OrientableComponent implements IInteractableComponent, IGoggleLabel {
    public static final BooleanProperty STATE = new BooleanProperty(PowerGrid.MOD_ID, "switch_state");
    public static final BooleanProperty SPDT_MODE = new BooleanProperty(PowerGrid.MOD_ID, "spdt_mode").hidden().cast();

    private static final ComponentFootprint SPDT_FOOTPRINT = new ComponentFootprint.Builder(4, 3)
            .addPad(0, 1, 0)
            .addPad(3, 0, 1)
            .addPad(3, 2, 2)
            .withItem().withOutline().build();

    public SwitchComponent(ComponentFootprint footprint) {
        super(footprint);
    }

    @Override
    protected void addProperties(ImmutableCollection.Builder<ComponentProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(STATE, LABEL, SPDT_MODE, current(16));
    }

    @Override
    public ComponentFootprint footprint(@Nullable PlacedComponent placed) {
        if(placed != null && placed.get(SPDT_MODE)) {
            return SPDT_FOOTPRINT.rotated(placed.get(ORIENTATION));
        }
        return super.footprint(placed);
    }

    @Override
    public void bake(@NotNull PlacedComponent placed, @NotNull ComponentCircuitBuilder builder, @NotNull ThermalBuilder.IEmitter thermals) {
        if (placed.get(SPDT_MODE)) {
            var wire1 = builder.connectSwitch(0.1f, builder.terminalNode(0), builder.terminalNode(1), placed.get(STATE));
            var wire2 = builder.connectSwitch(0.1f, builder.terminalNode(0), builder.terminalNode(2), !(placed.get(STATE)));
            placed.add(wire1); placed.add(wire2);
            thermals.builder()
                    .setMaxCurrent(16, 0.1f, 150)
                    .setThermalMass(0.01f)
                    .addHeatSource(wire1)
                    .addHeatSource(wire2);
        } else {
            var wire = builder.connectSwitch(0.1f, builder.terminalNode(0), builder.terminalNode(1), placed.get(STATE));
            placed.add(wire);
            thermals.builder()
                    .setMaxCurrent(16, 0.1f, 150)
                    .setThermalMass(0.01f)
                    .addHeatSource(wire);
        }
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
        if (placed.get(SPDT_MODE)) {
            ((SwitchedWire) placed.wires.get(1)).setState(!placed.get(STATE));
        }
        placed.onClientWorld(() -> world -> modelChanged(placed.getPos()));
    }

    @Override
    public @NotNull ResourceLocation getModelId(@NotNull PlacedComponent component) {
        if (component.get(SPDT_MODE)) {
            return component.get(STATE)
                    ? PowerGrid.asResource("switch_on")
                    : PowerGrid.asResource("switch");
//                    ? PowerGrid.asResource("spdt_switch_on") todo uncomment and remove above if it gets its own model otherwise delete these
//                    : PowerGrid.asResource("spdt_switch");
        } else {
            return component.get(STATE)
                    ? PowerGrid.asResource("switch_on")
                    : PowerGrid.asResource("switch");
        }
    }

    @Override
    public @NotNull Collection<ResourceLocation> requestedModels() {
        return List.of(
                PowerGrid.asResource("switch"),
                PowerGrid.asResource("switch_on")
//                PowerGrid.asResource("spdt_switch"), todo same as above uncomment if new model is added
//                PowerGrid.asResource("spdt_switch_on")
        );
    }

    @Override
    public boolean rotate(@NotNull PlacedComponent placed, boolean counterClockwise) {
        if(!counterClockwise) {
            if (!placed.get(SPDT_MODE)) {
                placed.set(SPDT_MODE, true);
                return true;
            }
            placed.set(SPDT_MODE, false);
        } else {
            if(placed.get(SPDT_MODE)) {
                placed.set(SPDT_MODE, false);
                return true;
            }
            placed.set(SPDT_MODE, true);
        }
        return super.rotate(placed, counterClockwise);
    }
}
