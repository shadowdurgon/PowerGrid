package org.patryk3211.powergrid.electricity.numericaldisplay;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;
import org.patryk3211.powergrid.PowerGrid;

public class numericalDisplayBlockEntityRenderer extends SafeBlockEntityRenderer<numericalDisplayBlockEntity> {

    //private static final ModelResourceLocation Model = new ModelResourceLocation(PowerGrid.asResource("block/contactor"), "");

    public numericalDisplayBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }


    private static final ResourceLocation BLANK_PLATE_TEXTURE = //textures
            PowerGrid.asResource("block/numerical_display/onetozero");


    private static final float SHEET_WIDTH = 64f; // 11*4 + 10*1
    private static final float SHEET_HEIGHT = 8f;

    private static final float FRAME_WIDTH = 4f;
    private static final float FRAME_HEIGHT = 7f;
    private static final float FRAME_PADDING = 1f; // padding BETWEEN frames only

    private static final int GRID_COLS = 4;
    private static final int GRID_ROWS = 4;

    private static final float PIXEL = 1f / 16f;
    private static final float CELL_SIZE = 4f * PIXEL;
    private static final float INNER_OFFSET = 1f * PIXEL;
    private static final float INNER_SIZE = 2f * PIXEL;

    private static final float Z_NUDGE = 0.001f;

    @Override
    protected void renderSafe(numericalDisplayBlockEntity be, float partialTicks, PoseStack pStack, MultiBufferSource buffer, int light, int overlay) {

        Direction facing = be.getBlockState().getValue(numericalDisplayBlock.HORIZONTAL_FACING);
        pStack.pushPose();
        pStack.translate(0.5, 0.5, 0.5);

        float yRot = switch (facing) {
            case NORTH -> 0f;
            case SOUTH -> 180f;
            case WEST  -> 90f;
            case EAST  -> 270f;
            default    -> 0f;
        };

        pStack.mulPose(Axis.YP.rotationDegrees(yRot));
        pStack.translate(-0.5, -0.5, -0.5);
        pStack.translate(0f, 0f, -Z_NUDGE);

        Matrix4f matrix = pStack.last().pose();

        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {

                int slotIndex = row * GRID_COLS + col;
                slotData slot = be.getSlot(slotIndex);

                if (slot.isEmpty()) continue;

                var halfClick = slot.getModule().getHalfClick();

                float cellX = col * CELL_SIZE;
                float cellY = (GRID_ROWS - 1 - row) * CELL_SIZE;

                switch (slot.getModule() != null ? slot.getModule().getType().ordinal() : 0){
                    case 1:
                        float innerX = cellX + INNER_OFFSET;
                        float innerY = cellY + INNER_OFFSET;

                        float frameIndex = slot.getDigit();
                        if (halfClick){
                            frameIndex -= .5f;
                        }

                        float uMin = (frameIndex * (FRAME_WIDTH + FRAME_PADDING)) / SHEET_WIDTH;
                        float uMax = (frameIndex * (FRAME_WIDTH + FRAME_PADDING) + FRAME_WIDTH) / SHEET_WIDTH;
                        float vMin = 0f;
                        float vMax = FRAME_HEIGHT / SHEET_HEIGHT;

                        renderQuad(matrix, buffer,
                                slot.getModule().getDisplayTexture(),
                                innerX, innerY,
                                INNER_SIZE, INNER_SIZE,
                                uMin, vMin, uMax, vMax,
                                light, overlay);
                        break;

                    case 3: renderQuad(matrix, buffer,
                            BLANK_PLATE_TEXTURE,
                            cellX, cellY,
                            CELL_SIZE, CELL_SIZE,
                            0f, 0f, 1f, 1f,
                            light, overlay);
                        break;

                    default:throw new IllegalStateException("Cannot find module switch statement");
                }
            }
        }

        pStack.popPose();
    }


    private void renderQuad(Matrix4f matrix, MultiBufferSource bufferSource, ResourceLocation texture,
                            float x, float y, float width, float height, float uMin, float vMin, float uMax, float vMax,
                            int packedLight, int packedOverlay) {

        VertexConsumer vc = bufferSource.getBuffer(RenderType.text(texture));

        vc.addVertex(matrix, x + width, y,          0f).setColor(255, 255, 255, 255)
                .setUv(uMin, vMax).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, 1f);

        vc.addVertex(matrix, x,         y,          0f).setColor(255, 255, 255, 255)
                .setUv(uMax, vMax).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, 1f);

        vc.addVertex(matrix, x,         y + height, 0f).setColor(255, 255, 255, 255)
                .setUv(uMax, vMin).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, 1f);

        vc.addVertex(matrix, x + width, y + height, 0f).setColor(255, 255, 255, 255)
                .setUv(uMin, vMin).setOverlay(packedOverlay).setLight(packedLight)
                .setNormal(0f, 0f, 1f);
    }
}
