package org.patryk3211.powergrid.electricity.airsiren;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.patryk3211.powergrid.electricity.sim.ElectricWire;

public class AirSirenBlockEntity extends ElectricBlockEntity {
    private ElectricWire wire;

    @Override
    public void electricalTick() {
        applyPower(wire);
    }

    @Override
    public void buildCircuit(IElectricEntity.CircuitBuilder builder) {
        builder.setTerminalCount(2);
        wire = builder.connect(resistance(), builder.terminalNode(0), builder.terminalNode(1));
    }

    public AirSirenBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
}
