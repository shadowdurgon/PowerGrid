package org.patryk3211.powergrid.electricity.numericaldisplay;

import com.simibubi.create.foundation.blockEntity.behaviour.*;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.base.IElectricEntity;
import org.patryk3211.powergrid.electricity.base.ThermalBehaviour;
import org.patryk3211.powergrid.electricity.numericaldisplay.modules.*;
import org.patryk3211.powergrid.electricity.sim.SwitchedWire;

import java.util.List;

public class numericalDisplayBlockEntity extends ElectricBlockEntity implements IElectricEntity {
    private SwitchedWire[] wires;
    public static final int SLOT_COUNT = 16;
    private ScrollOptionBehaviour<DisplayModuleType> moduleTypeBehaviour;
    public int lastHitSlot = 0;

    public final IDisplayModule[] modules = new IDisplayModule[SLOT_COUNT];

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);

        moduleTypeBehaviour = new ScrollOptionBehaviour<>(
                DisplayModuleType.class,
                Component.translatable("Module Type"),
                this,
                new SingleSlotTransform(this)
        );
        moduleTypeBehaviour.setValue(0);
        moduleTypeBehaviour.withCallback(value -> onSlotTypeChanged(lastHitSlot, value));
        behaviours.add(moduleTypeBehaviour);
    }

    private void onSlotTypeChanged(int slot, int value) {
        DisplayModuleType type = DisplayModuleType.values()[value];
        switch (type) {
            case ZERO_TO_NINE -> modules[slot] = new zeroToNineNumberModule(0, false);
            case NINE_TO_ZERO -> modules[slot] = new nineToZeroNumberModule(0, false);
            case ONE_TO_ZERO -> modules[slot] = new oneToZeroNumberModule(0, false);
            case HEXADECIMAL -> modules[slot] = new hexadecimalAlphanumericModule(0, false);
            case SYMBOLS -> modules[slot] = new symbolLetterModule(0, false);
            case ALPHABET -> modules[slot] = new alphabetLetterModule(0, false);
        }
        markUpdated();
    }

    public void syncBehaviourToSlot(int slot) {
        if (modules[slot] == null) return;
        moduleTypeBehaviour.value = modules[slot].getDisplayModuleType().ordinal();
    }



    public slotData getSlot(int index) {
        if (index < 0 || index >= SLOT_COUNT) return slotData.empty();
        return new slotData(modules[index]);
    }


    public boolean interact(int slotIndex, Player player){
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return false;

        ItemStack held = player.getMainHandItem();
        IDisplayModule heldModule = resolveModule(held);
        IDisplayModule current = modules[slotIndex];
        if (held.isEmpty() && player.isCrouching()) {
            if (current == null) return false;
            if (!player.getInventory().add(new ItemStack(ModdedItems.DISPLAY_MODULE.get()))) {
                player.drop(new ItemStack(ModdedItems.DISPLAY_MODULE.get()), false);
            }
            modules[slotIndex] = null;
            markUpdated();
            return true;
        }

        if (heldModule == null) return false;

        if (current != null) {
            if (!player.getInventory().add(new ItemStack(ModdedItems.DISPLAY_MODULE.get()))) {
                player.drop(new ItemStack(ModdedItems.DISPLAY_MODULE.get()), false);
            }
        }

        modules[slotIndex] = heldModule;
        if (!player.isCreative()) held.shrink(1);
        wires[slotIndex * 3].setState(true);
        wires[slotIndex * 3+1].setState(true);
        wires[slotIndex * 3+2].setState(false);

        markUpdated();
        return true;
    }



    @Nullable
    private IDisplayModule resolveModule(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.is(ModdedItems.DISPLAY_MODULE.get())) {
            return new zeroToNineNumberModule(0, false); // type set via ScrollOptionBehaviour
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

//    @Nullable
//    @Override
//    public ClientboundBlockEntityDataPacket getUpdatePacket() {
//        return ClientboundBlockEntityDataPacket.create(this);
//    }
//
//    @Override
//    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
//        CompoundTag tag = pkt.getTag();
//        if (tag != null) {
//            read(tag, registries,false);
//        }
//    }

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

    public void setIndex(int slotIndex, int digit) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return;
        if (modules[slotIndex] != null) {
            modules[slotIndex] = modules[slotIndex].withIndex(digit);
            markUpdated();
        }
    }

    public void add1ToIndex(int slotIndex){
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return;
        if (modules[slotIndex] != null) {
            int newVal = modules[slotIndex].getIndex() + 1;
            modules[slotIndex] = modules[slotIndex].withIndex(newVal);
            markUpdated();
        }
    }

    public void setHalfClick(int slotIndex, boolean halfClick) {
        if (slotIndex < 0 || slotIndex >= SLOT_COUNT) return;
        if (modules[slotIndex] != null) {
            modules[slotIndex] = modules[slotIndex].withHalfClick(halfClick);
            markUpdated();
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
                var charCount = slot.getModule().getDisplayTextureCharacterCount();
                //every module display texture has the characters in the sprite plus a blank space and the first character again for smooth transition
                //but im only counting characters before the blank space and adding one for the blank space and two for the transition
                if (posToNegitive.current() >= .5 && slot.getIndex() != charCount+1 && !slot.getModule().getHalfClick()) {
                    add1ToIndex(i);
                    setHalfClick(i, true);
                    ModdedSoundEvents.RELAY_CLICK.playOnServer(level, worldPosition, .75f, 2f);
                    markUpdated();
                }

                if (posToNegitive.current() < .5 && slot.getIndex() == charCount+1 && posToNegitive.getState()){
                    ModdedSoundEvents.RELAY_CLICK.playOnServer(level, worldPosition, .75f, 1.9f);
                    posToNegitive.setState(false);
                    posToReset.setState(true);
                    resetToGround.setState(false);
                    setHalfClick(i, false);
                    markUpdated();
                }

                if (posToNegitive.current() < .5 && posToNegitive.getState() && slot.getModule().getHalfClick()) {//CHANGED
                    setHalfClick(i, false);
                    ModdedSoundEvents.RELAY_CLICK.playOnServer(level, worldPosition, .75f, 1.9f);
                    markUpdated();
                }

                if (posToReset.getState() && posToReset.current() >= .5 && slot.getIndex() == charCount+1) {
                    ModdedSoundEvents.RELAY_CLICK.playOnServer(level, worldPosition, .75f, 2f);
                    add1ToIndex(i);
                    setHalfClick(i, true);
                    posToNegitive.setState(true);
                    posToReset.setState(false);
                    resetToGround.setState(true);
                    markUpdated();
                }

                if (slot.getIndex() >= charCount+2 && !slot.isHalfClick()){
                    setIndex(i, 0);
                    markUpdated();
                }
            }
            w1+=3; w2+=3; w3+=3;
        }
    }

    @Override
    public @Nullable ThermalBehaviour specifyThermalBehaviour() {
        return super.specifyThermalBehaviour();
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
