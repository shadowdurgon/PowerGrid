package org.patryk3211.powergrid.electricity.numericaldisplay;

import com.simibubi.create.foundation.block.IBE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.patryk3211.powergrid.collections.ModdedBlockEntities;
import org.patryk3211.powergrid.electricity.base.HorizontalElectricBlock;
import org.patryk3211.powergrid.electricity.base.IDecoratedTerminal;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;
import org.patryk3211.powergrid.electricity.info.IHaveElectricProperties;

import java.util.List;

public class numericalDisplayBlock extends HorizontalElectricBlock implements IBE<numericalDisplayBlockEntity>, IHaveElectricProperties {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape NORTHSHAPE = Shapes.or(
            box(0,0,0 ,16,16,13)
    );

    public numericalDisplayBlock(Properties settings) {
        super(settings);
        setTerminalCollection(horizontalNorthTerminals(this, NORTHTERMINALS, NORTHSHAPE));
    }

    @Override
    public Class<numericalDisplayBlockEntity> getBlockEntityClass() {
        return numericalDisplayBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends numericalDisplayBlockEntity> getBlockEntityType() {
        return ModdedBlockEntities.NUMERICAL_DISPLAY.get();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void appendProperties(ItemStack stack, Player player, List<Component> tooltip) {
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {

        Direction facing = state.getValue(numericalDisplayBlock.HORIZONTAL_FACING);

        if (hit.getDirection() != facing) return InteractionResult.PASS;

        Vec3 hitVec = hit.getLocation();

        double localX = hitVec.x - pos.getX();
        double localY = hitVec.y - pos.getY();
        double localZ = hitVec.z - pos.getZ();

        double faceU, faceV;

        switch (facing) {
            case NORTH -> { faceU = localX;       faceV = 1.0 - localY; }
            case SOUTH -> { faceU = 1.0 - localX; faceV = 1.0 - localY; }
            case WEST  -> { faceU = 1.0 - localZ; faceV = 1.0 - localY; }
            case EAST  -> { faceU = localZ;       faceV = 1.0 - localY; }
            default    -> { return InteractionResult.PASS; }
        }

        int col = Math.clamp((int)(faceU * 4), 0, 3);
        int row = Math.clamp((int)(faceV * 4), 0, 3);
        int slotIndex = row * 4 + col;

        if (level.isClientSide) return InteractionResult.SUCCESS;


        BlockEntity be = level.getBlockEntity(pos);

        if (player.getMainHandItem().getItem() instanceof DyeItem dye) {
            if (be instanceof numericalDisplayBlockEntity display) {
                display.setColor(slotIndex, dye.getDyeColor());
                if (!player.isCreative()) player.getMainHandItem().shrink(1);
                return InteractionResult.CONSUME;
            }
        }

        if (be instanceof numericalDisplayBlockEntity display) {
            display.interact(slotIndex, player);
        }

        return InteractionResult.CONSUME;
    }

    private static final TerminalBoundingBox[] NORTHTERMINALS = new TerminalBoundingBox[]{

            new TerminalBoundingBox(IDecoratedTerminal.CASE_GROUND, 8, 0, 13, 9, 2, 14).withColor(IDecoratedTerminal.BLUE),
            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 0, 15, 13, 1, 16, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 2, 13, 13, 3, 14, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 4, 15, 13, 5, 16, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 6, 13, 13, 7, 14, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 8, 15, 13, 9, 16, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 10, 13, 13, 11, 14, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 12, 15, 13, 13, 16, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 14, 13, 13, 15, 14, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 0, 11, 13, 1, 12, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 2, 9, 13, 3, 10, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 4, 11, 13, 5, 12, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 6, 9, 13, 7, 10, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 8, 11, 13, 9, 12, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 10, 9, 13, 11, 10, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 12, 11, 13, 13, 12, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 14, 9, 13, 15, 10, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 0, 7, 13, 1, 8, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 2, 5, 13, 3, 6, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 4, 7, 13, 5, 8, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 6, 5, 13, 7, 6, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 8, 7, 13, 9, 8, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 10, 5, 13, 11, 6, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 12, 7, 13, 13, 8, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 14, 5, 13, 15, 6, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 0, 3, 13, 1, 4, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 2, 1, 13, 3, 2, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 4, 3, 13, 5, 4, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 6, 1, 13, 7, 2, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 8, 3, 13, 9, 4, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 10, 1, 13, 11, 2, 14),

            new TerminalBoundingBox(IDecoratedTerminal.POSITIVE, 12, 3, 13, 13, 4, 14).withColor(IDecoratedTerminal.RED),
            new TerminalBoundingBox(IDecoratedTerminal.RESET, 14, 1, 13, 15, 2, 14),

    };
}
