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

import com.simibubi.create.AllItems;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.patryk3211.powergrid.collections.ModdedBlocks;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;
import org.patryk3211.powergrid.electricity.base.IDecoratedTerminal;
import org.patryk3211.powergrid.electricity.base.TerminalBoundingBox;
import org.patryk3211.powergrid.electricity.base.terminals.BlockStateTerminalCollection;
import org.patryk3211.powergrid.utility.Lang;

public class LvSwitchBlock extends SurfaceSwitchBlock {
    private static final TerminalBoundingBox[] DOWN_TERMINALS = new TerminalBoundingBox[] {
            new TerminalBoundingBox(IDecoratedTerminal.CONNECTOR, 7, 0, 1, 9, 2, 3),
            new TerminalBoundingBox(IDecoratedTerminal.CONNECTOR, 7, 0, 13, 9, 2, 15)
    };
    private int holdTime = 0;

    private static final VoxelShape SHAPE_DOWN = box(4, 0, 3, 12, 3, 13);
    private static final VoxelShape SHAPE_DOWN_2 = box(3, 0, 4, 13, 3, 12);

    public LvSwitchBlock(Properties settings) {
        super(settings);
        this.maxVoltage = 320;

        var shaper = VoxelShaper.forDirectional(SHAPE_DOWN, Direction.DOWN);
        var shaper2 = VoxelShaper.forDirectional(SHAPE_DOWN_2, Direction.DOWN);
        setTerminalCollection(BlockStateTerminalCollection
                .builder(this)
                .forAllStatesExcept(state -> BlockStateTerminalCollection.each(DOWN_TERMINALS, terminal -> {
                    var facing = state.getValue(FACING);
                    terminal = switch(facing) {
                        case DOWN -> terminal;
                        case UP -> terminal.rotateAroundX(180);
                        case EAST -> terminal.rotateAroundZ(-90);
                        case WEST -> terminal.rotateAroundZ(90);
                        case NORTH -> terminal.rotateAroundZ(90).rotateAroundY(90);
                        case SOUTH -> terminal.rotateAroundZ(90).rotateAroundY(-90);
                    };
                    if(!state.getValue(ALONG_FIRST_AXIS)) {
                        terminal = terminal.rotate(facing.getAxis(), 90);
                    }
                    return terminal;
                }), OPEN)
                .withShapeMapper(state -> {
                    var facing = state.getValue(FACING);
                    var axis_along = state.getValue(ALONG_FIRST_AXIS);
                    var prov = (axis_along ^ facing.getAxis() == Direction.Axis.Y) ? shaper2 : shaper;
                    return prov.get(facing);
                })
                .build());
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {

//        var hzFace = state.getValue(HORIZONTAL_FACING);
//        var rotation = state.getValue(ROTATION);
//
//        System.

        context.getClickedFace();

        return super.onWrenched(state, context);
    }

    @Override
    public void useSound(Level world, BlockPos pos, boolean open) {
        world.playSound(null, pos, ModdedSoundEvents.LV_SWITCH_CLICK.getMainEvent(), SoundSource.BLOCKS, 0.3F, open ? 0.65f : 0.75f);
    }

    public static Component wrenchText(Player player) {
        if (player.getMainHandItem().getItem() != AllItems.WRENCH.asItem()) {
            return null;
        }

        var world = player.level();

        if (Minecraft.getInstance().hitResult instanceof BlockHitResult blockHit){
            if (world.getBlockState(blockHit.getBlockPos()).getBlock() != ModdedBlocks.LV_SWITCH.get()) {
                var temp = world.getBlockEntity(blockHit.getBlockPos());
                return null;
            }

            var reach = player.isCreative() ? 5f : 4.5f;

            Vec3 location = blockHit.getLocation();
            var blockPos = blockHit.getBlockPos();
            var blockEntity = world.getBlockEntity(blockPos);

            if (blockEntity instanceof SwitchBlockEntity switchBlockEntity) {
                var facing = switchBlockEntity.getBlockState().getValue(FACING);

                if (blockHit.getDirection() != facing.getOpposite()){
                    return null;
                }

                var along = switchBlockEntity.getBlockState().getValue(ALONG_FIRST_AXIS);
                VoxelShape shape = world.getBlockState(blockPos).getShape(world, blockPos);
                AABB aabb = null;
                switch(facing) {
                    case DOWN -> {
                        if (along) {
                            aabb = shape.bounds().move(blockPos).deflate((double) 4/16, (double) 3/16, (double) 2/16);
                        } else {
                            aabb = shape.bounds().move(blockPos).deflate((double) 3/16,(double) 4/16, (double) 2/16);
                        }
                    }
                    case UP -> {
                        if (along) {
                            aabb = shape.bounds().move(blockPos).deflate((double) 4/16, (double) 3/16, (double) 2/16);
                        } else {
                            aabb = shape.bounds().move(blockPos).deflate((double) 3/16,(double) 4/16, (double) 2/16);
                        }
                    }
                    case EAST, WEST, NORTH ,SOUTH -> {
                        if (along) {
                            aabb = shape.bounds().move(blockPos).deflate((double) 4/16, (double) 3/16, (double) 2/16);
                        } else {
                            aabb = shape.bounds().move(blockPos).deflate((double) 3/16,(double) 4/16, (double) 2/16);
                        }
                    }
                }


                if (along) {
                    aabb = shape.bounds().move(blockPos).deflate((double) 4/16, (double) 3/16, (double) 2/16);
                } else {
                    aabb = shape.bounds().move(blockPos).deflate((double) 3/16,(double) 4/16, (double) 2/16);
                }
                //AABB aabb = new AABB(.45, .43, .85, .75, .69, .75); // .85 to .75 z   .45 to .75 x   .43 to .69 y
                //aabb.move(blockPos);
                var eyePos = player.getEyePosition(0f);
                var lookVec = player.getViewVector(0f);
                var endPos = eyePos.add(lookVec.scale(reach));

                var hit = aabb.clip(eyePos, endPos);

                if (hit.isPresent()){
                    var voltage = Lang.text( "Hit");
                    return Lang.translate("tooltip.multimeter.voltage")
                            .add(voltage.style(ChatFormatting.BLUE))
                            .style(ChatFormatting.GRAY)
                            .component();
                }
            }
        }

//        var reach = player.isCreative() ? 5f : 4.5f;
//        Vec3 location = player.pick(reach, 0, false).getLocation();
//        var blockPos = BlockPos.containing(location);
//        var blockEntity = world.getBlockEntity(blockPos);
//        if (blockEntity instanceof SwitchBlockEntity switchBlockEntity) {
//            var facing = switchBlockEntity.getBlockState().getValue(FACING);
//            var along = switchBlockEntity.getBlockState().getValue(ALONG_FIRST_AXIS);
//            VoxelShape shape = world.getBlockState(blockPos).getShape(world, blockPos);
//            AABB aabb = shape.bounds().move(blockPos).deflate((double) 3/16,(double) 4/16, 0);
//            //AABB aabb = new AABB(.45, .43, .85, .75, .69, .75); // .85 to .75 z   .45 to .75 x   .43 to .69 y
//            //aabb.move(blockPos);
//            var eyePos = player.getEyePosition(0f);
//            var lookVec = player.getViewVector(0f);
//            var endPos = eyePos.add(lookVec.scale(reach));
//
//            var hit = aabb.clip(eyePos, endPos);
//
//            if (hit.isPresent()){
//                var voltage = Lang.text( "Hit");
//                return Lang.translate("tooltip.multimeter.voltage")
//                        .add(voltage.style(ChatFormatting.BLUE))
//                        .style(ChatFormatting.GRAY)
//                        .component();
//            }
//        }

        return null;
    }
}
