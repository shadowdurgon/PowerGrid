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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.patryk3211.powergrid.collections.ModdedItems;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;
import org.patryk3211.powergrid.electricity.base.ElectricBlockEntity;
import org.patryk3211.powergrid.electricity.base.ThermalBehaviour;
import org.patryk3211.powergrid.electricity.numericaldisplay.modules.*;
import org.patryk3211.powergrid.electricity.sim.AbstractElectricWire;
import org.patryk3211.powergrid.electricity.sim.SwitchedWire;

import java.util.List;

public class ModularDisplayBlockEntity extends ElectricBlockEntity {
    private AbstractElectricWire[] wires;
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
                new CustomValueBoxTransformer(this)
        ) {
            @Override
            public boolean bypassesInput(ItemStack mainhandItem) {
                return mainhandItem.getItem() instanceof DyeItem;
            }
        };

        moduleTypeBehaviour.setValue(0);
        moduleTypeBehaviour.withCallback(value -> onSlotTypeChanged(lastHitSlot, value));
        behaviours.add(moduleTypeBehaviour);
    }

    private void onSlotTypeChanged(int slot, int value) {
        DisplayModuleType type = DisplayModuleType.values()[value];
        var lastColor = modules[slot].getColor();
        switch (type) {
            case ZERO_TO_NINE -> modules[slot] = new zeroToNineNumberModule(0, false, lastColor);
            case NINE_TO_ZERO -> modules[slot] = new nineToZeroNumberModule(0, false, lastColor);
            case ONE_TO_ZERO -> modules[slot] = new oneToZeroNumberModule(0, false, lastColor);
            case HEXADECIMAL -> modules[slot] = new hexadecimalAlphanumericModule(0, false, lastColor);
            case SYMBOLS -> modules[slot] = new symbolLetterModule(0, false, lastColor);
            case ALPHABET -> modules[slot] = new alphabetLetterModule(0, false, lastColor);
        }
        markUpdated();
    }

    public void syncBehaviourToSlot(int slot) {
        if (modules[slot] == null) return;
        moduleTypeBehaviour.value = modules[slot].getDisplayModuleType().ordinal();
    }

    public SlotData getSlot(int index) {
        if (index < 0 || index >= SLOT_COUNT) return SlotData.empty();
        return new SlotData(modules[index]);
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

        if (heldModule == null)
            return false;

        if (current != null) {
            if (!player.getInventory().add(new ItemStack(ModdedItems.DISPLAY_MODULE.get()))) {
                player.drop(new ItemStack(ModdedItems.DISPLAY_MODULE.get()), false);
            }
        }

        modules[slotIndex] = heldModule;
        if (!player.isCreative()) held.shrink(1);
        var negative = (SwitchedWire) wires[slotIndex * 3+1];
        var reset = (SwitchedWire) wires[slotIndex * 3+2];
        reset.setState(false);
        negative.setState(true);

        markUpdated();
        return true;
    }

    public void setColor(int slotIndex, DyeColor color) {
        modules[slotIndex] = modules[slotIndex].withColor(color);
        markUpdated();
    }

    @Nullable
    private IDisplayModule resolveModule(ItemStack stack) {
        if (stack.isEmpty()) return null;
        if (stack.is(ModdedItems.DISPLAY_MODULE.get())) {
            return new zeroToNineNumberModule(0, false, DyeColor.WHITE);
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
                modules[i] = DisplayModuleRegistry.deserialize(slotList.getString(i));
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
        for (AbstractElectricWire wire : wires) applyPower(wire);
        int w1 = 0, w2 = 1, w3 = 2;
        boolean updated = false;
        for (int i = 0; i < SLOT_COUNT; i++) {
            var coil = wires[w1];
            var coilNodeToNegative = (SwitchedWire) wires[w2];
            var coilNodeToReset = (SwitchedWire) wires[w3];
            var coilNodeToNegativeCurrent = Math.abs(coilNodeToNegative.current());
            var coilNodeToResetCurrent = Math.abs(coilNodeToReset.current());
            var slot = getSlot(i);
            if (!slot.isEmpty()) {
                var charCount = slot.getModule().getDisplayTextureCharacterCount();
                //every module display texture has the characters in the sprite plus a blank space and the first character again for smooth transition
                //but im only counting characters before the blank space and adding one for the blank space and two for the transition
                var temp = coilNodeToNegative.current();
                if (coilNodeToNegativeCurrent >= .5 && slot.getIndex() != charCount+1 && !slot.getHalfClick()) {
                    add1ToIndex(i);
                    setHalfClick(i, true);
                    ModdedSoundEvents.RELAY_CLICK.playOnServer(level, worldPosition, .75f, 2f);
                    updated = true;
                }

                if (coilNodeToNegativeCurrent < .5 && slot.getIndex() == charCount+1 && coilNodeToNegative.getState()){
                    ModdedSoundEvents.RELAY_CLICK.playOnServer(level, worldPosition, .75f, 1.9f);
                    coilNodeToNegative.setState(false);
                    coilNodeToReset.setState(true);
                    setHalfClick(i, false);
                    updated = true;
                }

                if (coilNodeToNegativeCurrent < .5 && coilNodeToNegative.getState() && slot.getHalfClick()) {
                    setHalfClick(i, false);
                    ModdedSoundEvents.RELAY_CLICK.playOnServer(level, worldPosition, .75f, 1.9f);
                    updated = true;
                }

                if (coilNodeToReset.getState() && coilNodeToResetCurrent >= .5 && slot.getIndex() == charCount+1) {
                    ModdedSoundEvents.RELAY_CLICK.playOnServer(level, worldPosition, .75f, 2f);
                    add1ToIndex(i);
                    setHalfClick(i, true);
                    coilNodeToNegative.setState(true);
                    coilNodeToReset.setState(false);
                    updated = true;
                }

                if (slot.getIndex() >= charCount+2 && !slot.getHalfClick()){
                    setIndex(i, 0);
                    updated = true;
                }
            }
            w1+=3; w2+=3; w3+=3;
        }
        if (updated) {
            markUpdated();
        }

    }

    @Override
    public @Nullable ThermalBehaviour specifyThermalBehaviour() {
        return super.specifyThermalBehaviour();
    }

    public ModularDisplayBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void buildCircuit(CircuitBuilder builder) {
        builder.setTerminalCount(2*SLOT_COUNT + 1);
        wires = new AbstractElectricWire[3*SLOT_COUNT];
        var negative = builder.terminalNode(0);

        int p = 1, r = 2, w1 = 0, w2 = 1, w3 = 2;
        for (int s = 0; s < SLOT_COUNT; s++){

            var positive = builder.terminalNode(p);
            var reset = builder.terminalNode(r);
            var coilNode = builder.addInternalNode();

            wires[w1] = builder.connect(25, builder.terminalNode(p), coilNode);
            wires[w2] = builder.connectSwitch(0.1f, negative, coilNode, true);
            wires[w3] = builder.connectSwitch(0.1f, builder.terminalNode(r), coilNode, false);
            p +=2; r +=2; w1+=3; w2+=3; w3+=3;
        }
    }
}
