package org.patryk3211.powergrid.electricity.airsiren;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.patryk3211.powergrid.collections.ModdedBlockEntities;
import org.patryk3211.powergrid.electricity.base.HorizontalElectricBlock;
import org.patryk3211.powergrid.electricity.base.IDecoratedTerminal;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;
import org.patryk3211.powergrid.electricity.info.IHaveElectricProperties;
import org.patryk3211.powergrid.electricity.info.Resistance;
import org.patryk3211.powergrid.electricity.info.Voltage;

import java.util.List;

public class AirSirenBlock extends HorizontalElectricBlock implements IBE<AirSirenBlockEntity>, IHaveElectricProperties {

    private static final VoxelShape NORTHSHAPE = Shapes.or(
            box(5,0,1,11,6,4),
            box(6, 1, 6, 10, 5, 12),
            box(5,0,12,11,6,15)
    );

    private static final TerminalBoundingBox[] NORTHTERMINALS = new TerminalBoundingBox[]{
            new TerminalBoundingBox(IDecoratedTerminal.CONNECTOR, 5, 3, 9, 6, 4, 10),
            new TerminalBoundingBox(IDecoratedTerminal.CONNECTOR, 5, 3, 6, 6, 4, 7)
    };

    public AirSirenBlock(Properties properties) {
        super(properties);
        setTerminalCollection(horizontalNorthTerminals(this, NORTHTERMINALS, NORTHSHAPE));
    }

    @Override
    public Class<AirSirenBlockEntity> getBlockEntityClass() {
        return AirSirenBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends AirSirenBlockEntity> getBlockEntityType() {
        return ModdedBlockEntities.AIR_SIREN.get();
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        return direction == state.getValue(HORIZONTAL_FACING) && !canSurvive(state, world, pos)
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void appendProperties(ItemStack stack, Player player, List<Component> tooltip) {
        Resistance.series(resistance(), player, tooltip);
        Voltage.rated(resistance() * 1, player, tooltip);
    }

}
