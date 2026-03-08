package org.patryk3211.powergrid.electricity.numericaldisplay;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedBlockEntities;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.patryk3211.powergrid.electricity.numericaldisplay.modules.blankingModule;
import org.patryk3211.powergrid.electricity.numericaldisplay.modules.oneToZeroNumberModule;
import org.patryk3211.powergrid.electricity.numericaldisplay.modules.zeroToNineNumberModule;
import org.patryk3211.powergrid.electricity.sim.SwitchedWire;

public class numericalDisplayBlockEntity extends ElectricBlockEntity implements IElectricEntity {
    private SwitchedWire[] wires;
    public static final int SLOT_COUNT = 16;

    private final IDisplayModule[] modules = new IDisplayModule[SLOT_COUNT];

    public numericalDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(ModdedBlockEntities.NUMERICAL_DISPLAY.get(), pos, state);
    }

    public slotData getSlot(int index) {
        if (index < 0 || index >= SLOT_COUNT) return slotData.empty();
        return new slotData(modules[index]);
    }

    public boolean interact(int slotIndex, Player player) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return false;

        ItemStack held = player.getMainHandItem();
        IDisplayModule heldModule = resolveModule(held);
        IDisplayModule current = modules[slotIndex];

        if (held.isEmpty() && player.isCrouching()) {
            // Sneaking + empty hand = remove module, return item to player
            if (current == null) return false;
            if (!player.getInventory().add(current.toItemStack())) {
                player.drop(current.toItemStack(), false);
            }
            modules[slotIndex] = null;
            markUpdated();

            return true;
        }

        if (heldModule == null) return false;

        if (current != null) {
            if (!player.getInventory().add(current.toItemStack())) {
                player.drop(current.toItemStack(), false);
            }
        }

        modules[slotIndex] = heldModule;
        if (!player.isCreative()) held.shrink(1);
        wires[slotIndex * 3].setState(true);
        wires[slotIndex * 3+1].setState(true);
        wires[slotIndex * 3+2].setState(false);
        //

        markUpdated();
        return true;
    }

    @Nullable
    private IDisplayModule resolveModule(ItemStack stack) {
        if (stack.isEmpty()) return null;

        if (stack.is(ModdedItems.ONETOZERO_NUMBER_MODULE.get())) {
            return new oneToZeroNumberModule(0);
        }

        if (stack.is(ModdedItems.BLANKING_MODULE.get())) {
            return new blankingModule();
        }

        if (stack.is(ModdedItems.ZEROTONINE_NUMBER_MODULE.get())) {
            return new zeroToNineNumberModule(0, false);
        }

        return null;
    }

    private void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            read(tag, registries,false);
        }
    }

    @Override
    public void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);

        ListTag slotList = new ListTag();
        for (int i = 0; i < SLOT_COUNT; i++) {
            IDisplayModule module = modules[i];
            slotList.add(StringTag.valueOf(module != null ? module.serialize() : ""));
        }
        tag.put("slots", slotList);
    }

    @Override
    public void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);

        if (tag.contains("slots", Tag.TAG_LIST)) {
            ListTag slotList = tag.getList("slots", Tag.TAG_STRING);
            for (int i = 0; i < Math.min(slotList.size(), SLOT_COUNT); i++) {
                modules[i] = displayModuleRegistry.deserialize(slotList.getString(i));
            }
        }
    }

    public void setDigit(int slotIndex, int digit) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return;
        if (modules[slotIndex] instanceof zeroToNineNumberModule) {
            modules[slotIndex] = new zeroToNineNumberModule(digit, modules[slotIndex].getHalfClick());
            markUpdated();
        }
    }

    public void add1ToDigit(int slotIndex){
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return;
        if (modules[slotIndex].getType() == IDisplayModule.ModuleType.DIGIT){
            int val = modules[slotIndex].getDigit() + 1;
            modules[slotIndex] = new zeroToNineNumberModule(val, modules[slotIndex].getHalfClick());
            markUpdated();
        }


    }

    public void setDigits(int[] digits) {
        for (int i = 0; i < Math.min(digits.length, SLOT_COUNT); i++) {
            setDigit(i, digits[i]);
        }
    }

    @Override
    public void electricalTick() {
        for (SwitchedWire wire : wires) applyPower(wire);
        int w1 = 0, w2 = 1, w3 = 2;
        for (int i = 0; i < SLOT_COUNT; i++) {
            var resetToGround = wires[w1];
            var posToNegitive = wires[w2];
            var posToReset = wires[w3];
            var slot = getSlot(i);
            if (!slot.isEmpty()) {
                if (posToNegitive.current() >= .5 && slot.getDigit() != 10 && !slot.getModule().getHalfClick()) {
                    add1ToDigit(i);
                    setHalfClick(i, true);
                    ModdedSoundEvents.RELAY_CLICK.playOnServer(level, worldPosition, .75f, 2f);
                    markUpdated();
                }

                if (posToNegitive.current() < .5 && slot.getDigit() == 10 && posToNegitive.getState()){
                    ModdedSoundEvents.RELAY_CLICK.playOnServer(level, worldPosition, .75f, 1.9f);
                    posToNegitive.setState(false);
                    posToReset.setState(true);
                    resetToGround.setState(false);
                    setHalfClick(i, false);
                    markUpdated();
                }

                if (posToNegitive.current() < .5 && slot.getModule() != null && posToNegitive.getState()) {
                    if (slot.getModule().getHalfClick()) {
                        setHalfClick(i, false);
                        ModdedSoundEvents.RELAY_CLICK.playOnServer(level, worldPosition, .75f, 1.9f);
                        markUpdated();
                    }
                }

                if (posToReset.getState() && posToReset.current() >= .5 && slot.getDigit() == 10) {
                    ModdedSoundEvents.RELAY_CLICK.playOnServer(level, worldPosition, .75f, 1.9f);
                    add1ToDigit(i);
                    setHalfClick(i, true);
                    posToNegitive.setState(true);
                    posToReset.setState(false);
                    resetToGround.setState(true);
                    markUpdated();
                }

                if (slot.getDigit() >= 11 && !slot.isHalfClick()){
                    setDigit(i, 0);
                    markUpdated();
                }
            }
            w1+=3; w2+=3; w3+=3;
        }
    }

    public void setHalfClick(int slotIndex, boolean halfClick) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return;
        if (modules[slotIndex] instanceof zeroToNineNumberModule current) {
            modules[slotIndex] = new zeroToNineNumberModule(current.getDigit(), halfClick);
            markUpdated();
        }
    }

    public numericalDisplayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void buildCircuit(CircuitBuilder builder) {
        builder.setTerminalCount(2*SLOT_COUNT + 1);
        wires = new SwitchedWire[3*SLOT_COUNT];
        var negative = builder.terminalNode(0);

        int p = 1, r = 2, w1 = 0, w2 = 1, w3 = 2;
        for (int s = 0; s < SLOT_COUNT; s++){

            var positive = builder.terminalNode(p);
            var reset = builder.terminalNode(r);
            wires[w1] = builder.connectSwitch(1, reset, negative, true);
            wires[w2] = builder.connectSwitch(25, positive, negative, true);
            wires[w3] = builder.connectSwitch(25, positive, reset, false);
            p +=2; r +=2; w1+=3; w2+=3; w3+=3;
        }
    }
}
