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
package org.patryk3211.powergrid.electricity.electricswitch;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.base.CustomProperties;
import org.patryk3211.powergrid.circuits.components.properties.FloatProperty;
import org.patryk3211.powergrid.circuits.components.properties.IntProperty;

public class SurfaceSwitchBlock extends SwitchBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ALONG_FIRST_AXIS = CustomProperties.ALONG_FIRST_AXIS;
    public static final IntegerProperty ROTATION = CustomProperties.ROTATION_4;
//    public static final IntegerProperty ROTATION = IntegerProperty.create("rotation", 0, 2);
//    public static final DirectionProperty HORIZONTAL_FACING = BlockStateProperties.HORIZONTAL_FACING;

    public SurfaceSwitchBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, ALONG_FIRST_AXIS, ROTATION);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var facing = ctx.getClickedFace().getOpposite();
        boolean along = true;
        int rotation;
        if(facing.getAxis() == Direction.Axis.Y) {
            var player = ctx.getHorizontalDirection();
            if(player.getAxis() == Direction.Axis.X)
                along = false;
        } else {
            along = false;
            if(ctx.getNearestLookingDirection().getAxis() == facing.getClockWise().getAxis())
                along = true;
        }
        if(along) {
            rotation = 0;
        }else {
            rotation = 3;
        }

        return defaultBlockState()
                .setValue(FACING, facing)
                .setValue(ALONG_FIRST_AXIS, along)
                .setValue(ROTATION, rotation);
    }

//    @Override
//    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
//        var face = ctx.getClickedFace();
//        if(face.getAxis() == Direction.Axis.Y) {
//            var player = ctx.getPlayer() == null || !ctx.getPlayer().isShiftKeyDown() ?
//                    ctx.getHorizontalDirection() : ctx.getHorizontalDirection().getOpposite();
//            return defaultBlockState()
//                    .setValue(FACING, player)
//                    .setValue(ALONG_FIRST_AXIS, face == Direction.UP ? 0 : 2);
//        } else {
//            return defaultBlockState()
//                    .setValue(FACING, 1)
//                    .setValue(ALONG_FIRST_AXIS, face.getOpposite());
//        }
//    }

    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }
}
