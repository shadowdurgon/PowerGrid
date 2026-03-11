package org.patryk3211.powergrid.circuits.components;

import com.google.common.collect.ImmutableCollection;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.patryk3211.powergrid.PowerGrid;
import org.patryk3211.powergrid.circuits.circuitboard.CircuitBoardBlockEntity;
import org.patryk3211.powergrid.circuits.circuitboard.ComponentCircuitBuilder;
import org.patryk3211.powergrid.circuits.components.properties.BooleanProperty;
import org.patryk3211.powergrid.circuits.components.properties.ComponentProperty;
import org.patryk3211.powergrid.circuits.components.properties.FloatProperty;
import org.patryk3211.powergrid.circuits.components.properties.IntProperty;
import org.patryk3211.powergrid.circuits.schematic.ComponentFootprint;
import org.patryk3211.powergrid.circuits.schematic.PlacedComponent;
import org.patryk3211.powergrid.circuits.thermal.ThermalBuilder;
import org.patryk3211.powergrid.collections.ModdedSoundEvents;

import org.patryk3211.powergrid.electricity.sim.SwitchedWire;

public class NumericalDisplayComponent extends OrientableComponent implements IRenderedComponent{
    private SwitchedWire[] wires;
    public static final FloatProperty THRESHOLD_VOLTAGE = new FloatProperty(PowerGrid.MOD_ID, "numerical_display_threshold", 13, 1, 30);
    public static final IntProperty INDEX = new IntProperty(PowerGrid.MOD_ID, "numerical_display_index", 0, 0, 30).hidden().cast();
    public static final BooleanProperty HALF_CLICK = new BooleanProperty(PowerGrid.MOD_ID, "numerical_display_half_click").hidden().cast();

    private final ResourceLocation texture;
    private final float characterCount;
    private final float spriteWidth;

    public NumericalDisplayComponent(ComponentFootprint footprint, ResourceLocation texture, Float characterCount, Float spriteWidth) {
        super(footprint);
        this.texture = texture;
        this.characterCount = characterCount;
        this.spriteWidth= spriteWidth;
    }

    //private static final float SHEET_WIDTH = 80f; // 11*4 + 10*1
    private static final float SHEET_HEIGHT = 16f;

    private static final float FRAME_WIDTH = 5f;
    private static final float FRAME_HEIGHT = 7f;
    private static final float FRAME_PADDING = 1f; // padding BETWEEN frames only

    private static final int GRID_COLS = 4;
    private static final int GRID_ROWS = 4;

    private static final float PIXEL = 1f / 16f;
    private static final float CELL_SIZE = 4f * PIXEL;
    private static final float INNER_OFFSET = 1f * PIXEL;
    private static final float INNER_UD_OFFSET = .75f * PIXEL;
    private static final float INNER_UD_SIZE = 2.5f * PIXEL;
    private static final float INNER_RL_SIZE = 2f * PIXEL;
    private static final float INNER_SIZE = 2f * PIXEL;

    private static final float Y_NUDGE = 0.0001f;

    @Override
    protected void addProperties(ImmutableCollection.Builder<ComponentProperty<?>> properties) {
        super.addProperties(properties);
        properties.add(THRESHOLD_VOLTAGE, INDEX, HALF_CLICK, power(25));
    }

    @Override
    public boolean tick(@NotNull PlacedComponent placed) {
        if (placed.isClient()) return true;
        if(placed.wires.isEmpty())
            return true;
        var resetToGround = (SwitchedWire) placed.wires.get(0);
        var posToNegitive = (SwitchedWire) placed.wires.get(1);
        var posToReset = (SwitchedWire) placed.wires.get(2);
        var posToNegitiveCurrent = Math.abs(posToNegitive.current());
        var posToResetCurrent = Math.abs(posToReset.current());
        var charCount = this.characterCount;
        //every module display texture has the characters in the sprite plus a blank space and the first character again for smooth transition
        //but im only counting characters before the blank space and adding one for the blank space and two for the transition
        if(posToNegitive.isConverged()) {

            if (posToNegitiveCurrent >= .5 && placed.get(INDEX) != charCount+1 && !placed.get(HALF_CLICK)) {
                placed.set(INDEX, placed.get(INDEX) +1);
                placed.set(HALF_CLICK, true);
                placed.onServerWorld(() -> world -> ModdedSoundEvents.RELAY_CLICK.playOnServer(world, placed.getPos(), 0.75f, 2f));
                placed.notifyClients(INDEX);
                placed.notifyClients(HALF_CLICK);
            }

            if (posToNegitiveCurrent < .5 && placed.get(INDEX) == charCount+1 && posToNegitive.getState()){
                placed.onServerWorld(() -> world -> ModdedSoundEvents.RELAY_CLICK.playOnServer(world, placed.getPos(), 0.75f, 1.9f));
                posToNegitive.setState(false);
                posToReset.setState(true);
                resetToGround.setState(false);
                placed.set(HALF_CLICK, false);
                placed.notifyClients(INDEX);
                placed.notifyClients(HALF_CLICK);
            }

            if (posToNegitiveCurrent < .5 && posToNegitive.getState() && placed.get(HALF_CLICK)) {//CHANGED
                placed.set(HALF_CLICK, false);
                placed.onServerWorld(() -> world -> ModdedSoundEvents.RELAY_CLICK.playOnServer(world, placed.getPos(), 0.75f, 1.9f));
                placed.notifyClients(INDEX);
                placed.notifyClients(HALF_CLICK);
            }

            if (posToReset.getState() && posToResetCurrent >= .5 && placed.get(INDEX) == charCount+1) {
                placed.onServerWorld(() -> world -> ModdedSoundEvents.RELAY_CLICK.playOnServer(world, placed.getPos(), 0.75f, 2f));
                placed.set(INDEX, placed.get(INDEX) +1);
                placed.set(HALF_CLICK, true);
                posToNegitive.setState(true);
                posToReset.setState(false);
                resetToGround.setState(true);
                placed.notifyClients(INDEX);
                placed.notifyClients(HALF_CLICK);
            }

            if (placed.get(INDEX) >= charCount+2 && !placed.get(HALF_CLICK)){
                //setDigit(i, 0);
                placed.set(INDEX, 0);
                placed.notifyClients(INDEX);
            }
        }

        return true;
    }

    @Override
    public void bake(@NotNull PlacedComponent placed, @NotNull ComponentCircuitBuilder builder, ThermalBuilder.@NotNull IEmitter thermals) {
        var neutralToReset = builder.connectSwitch(1, builder.terminalNode(2), builder.terminalNode(1), true);
        var coil = builder.connectSwitch(25, builder.terminalNode(0), builder.terminalNode(1), true);
        var positiveToReset = builder.connectSwitch(25, builder.terminalNode(0), builder.terminalNode(2), false);
        placed.add(neutralToReset); placed.add(coil); placed.add(positiveToReset);

        thermals.builder()
                .setThermalMass(0.15f)
                .setMaxPower(25, 125f)
                .addHeatSource(positiveToReset);
        thermals.builder()
                .setThermalMass(0.15f)
                .setMaxPower(25, 125f)
                .addHeatSource(coil);
        thermals.builder()
                .setThermalMass(0.15f)
                .setMaxPower(100, 125f)
                .addHeatSource(neutralToReset);

    }


    @Override
    public void render(CircuitBoardBlockEntity be, PlacedComponent placed, float partialTicks, PoseStack pStack, MultiBufferSource buffer, int light, int overlay) {

        pStack.pushPose();
        pStack.translate(0.5, 8f/16f, 0.5);
        Boolean halfClick = false;

        pStack.translate(-0.5, 0, -0.5);
        pStack.translate(0f, Y_NUDGE, 0f);

        Matrix4f matrix = pStack.last().pose();
        if (placed.has(HALF_CLICK)) {
            halfClick = placed.get(HALF_CLICK);
        }

        float frameIndex = placed.get(INDEX);
        if (halfClick){
            frameIndex -= .5f;
        }

        float innerX = 0 + INNER_OFFSET;
        float innerY = 0 + INNER_UD_OFFSET;

        float uMin = (frameIndex * (FRAME_WIDTH + FRAME_PADDING)) / this.spriteWidth;
        float uMax = (frameIndex * (FRAME_WIDTH + FRAME_PADDING) + FRAME_WIDTH) / this.spriteWidth;
        float vMin = 0f;
        float vMax = FRAME_HEIGHT / SHEET_HEIGHT;

        renderQuad(matrix, buffer,
                this.texture,
                innerX, innerY,
                INNER_RL_SIZE, INNER_UD_SIZE,
                uMin, vMin, uMax, vMax,
                light, overlay);

        pStack.popPose();
    }

    private void renderQuad(Matrix4f matrix, MultiBufferSource bufferSource, ResourceLocation texture,
                            float x, float z, float width, float height, float uMin, float vMin, float uMax, float vMax,
                            int packedLight, int packedOverlay) {

        VertexConsumer vc = bufferSource.getBuffer(RenderType.text(texture));

        vc.addVertex(matrix, x + width, 0f, z).setColor(255, 255, 255, 255)
                .setUv(uMax, vMin).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, 1f);

        vc.addVertex(matrix, x, 0f, z).setColor(255, 255, 255, 255)
                .setUv(uMin, vMin).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, 1f);

        vc.addVertex(matrix, x, 0f, z + height).setColor(255, 255, 255, 255)
                .setUv(uMin, vMax).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, 1f);

        vc.addVertex(matrix, x + width, 0f, z + height).setColor(255, 255, 255, 255)
                .setUv(uMax, vMax).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, 1f);
    }
}
