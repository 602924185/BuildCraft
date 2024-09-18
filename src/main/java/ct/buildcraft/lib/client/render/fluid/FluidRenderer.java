/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.client.render.fluid;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import ct.buildcraft.lib.client.model.MutableVertex;
import ct.buildcraft.lib.misc.MathUtil;
import ct.buildcraft.lib.misc.SpriteUtil;
import ct.buildcraft.lib.misc.VecUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

/** Can render 3D fluid cuboid's, up to 1x1x1 in size. Note that they *must* be contained within the 1x1x1 block space -
 * you can't use this to render off large multiblocks. Not thread safe -- this uses static variables so you should only
 * call this from the main client thread. */
// TODO: thread safety (per thread context?)
// Perhaps move this into IModelRenderer? And that way we get the buffer, force shaders to cope with fluids (?!), etc
@OnlyIn(Dist.CLIENT)
public class FluidRenderer {

    private static final EnumMap<FluidSpriteType, Map<String, TextureAtlasSprite>> fluidSprites =
        new EnumMap<>(FluidSpriteType.class);
    public static final MutableVertex vertex = new MutableVertex();
    private static final boolean[] DEFAULT_FACES = { true, true, true, true, true, true };
    private static Function<ResourceLocation, TextureAtlasSprite> blockTexMap;
    
    // Cached fields that prevent lots of arguments on most methods
    private static VertexConsumer bb;
    private static Pose pose;
    private static TextureAtlasSprite sprite;
    private static TexMap texmap;
    private static int color = 0xFFFFFFFF;
    private static boolean invertU, invertV;
    private static double xTexDiff, yTexDiff, zTexDiff;

    static {
        // TODO: allow the caller to change the light level
        vertex.lighti(0xF, 0xF);
        for (FluidSpriteType type : FluidSpriteType.values()) {
            fluidSprites.put(type, new HashMap<>());
        }
    }

/*    public static void onTextureStitchPre(TextureMap map) {
        for (FluidSpriteType type : FluidSpriteType.values()) {
            fluidSprites.get(type).clear();
        }
        Map<ResourceLocation, SpriteFluidFrozen> spritesStitched = new HashMap<>();
        for (Fluid fluid : ForgeRegistries.FLUIDS.getValues()) {
        	var flu = IClientFluidTypeExtensions.of(fluid);
            ResourceLocation still = flu.getStillTexture();
            ResourceLocation flowing = flu.getFlowingTexture();
            if (still == null || flowing == null) {
                throw new IllegalStateException("Encountered a fluid with a null still sprite! (" + fluid.getFluidType().getDescriptionId()
                    + " - " + ForgeRegistries.FLUIDS.getKey(fluid) + ")");
            }
            if (spritesStitched.containsKey(still)) {
                fluidSprites.get(FluidSpriteType.FROZEN).put(fluid.getFluidType().getDescriptionId(), spritesStitched.get(still));
            } else {
                SpriteFluidFrozen spriteFrozen = new SpriteFluidFrozen(still);
                spritesStitched.put(still, spriteFrozen);
                if (!map.setTextureEntry(spriteFrozen)) {
                    throw new IllegalStateException("Failed to set the frozen variant of " + still + "!");
                }
                fluidSprites.get(FluidSpriteType.FROZEN).put(fluid.getFluidType().getDescriptionId(), spriteFrozen);
            }
            // Note: this must be called with EventPriority.LOW so that we don't overwrite other custom sprites.
            fluidSprites.get(FluidSpriteType.STILL).put(fluid.getFluidType().getDescriptionId(), map.registerSprite(still));
            fluidSprites.get(FluidSpriteType.FLOWING).put(fluid.getFluidType().getDescriptionId(), map.registerSprite(flowing));
        }
    }*/

    /** Renders a fluid cuboid to the given vertex buffer. The cube shouldn't cross over any {@literal 0->1} boundary
     * (so the cube must be contained within a block).
     * 
     * @param type The type of sprite to use. See {@link FluidSpriteType} for more details.
     * @param tank The fluid tank that should be rendered.
     * @param min The minimum coordinate that the tank should be rendered from
     * @param max The maximum coordinate that the tank will be rendered to.
     * @param bbIn The {@link BufferBuilder} that the fluid will be rendered into.
     * @param sideRender A size 6 boolean array that determines if the face will be rendered. If it is null then all
     *            faces will be rendered. The indexes are determined by what {@link Direction#ordinal()} returns.
     * @see #renderFluid(FluidSpriteType, FluidStack, double, double, Vec3, Vec3, BufferBuilder, boolean[]) */
    public static void renderFluid(FluidSpriteType type, IFluidTank tank, Vec3 min, Vec3 max, VertexConsumer bbIn, Pose matrix,
        boolean[] sideRender) {
        renderFluid(type, tank.getFluid(), tank.getCapacity(), min, max, bbIn, matrix, sideRender);
    }

    /** Render's a fluid cuboid to the given vertex buffer. The cube shouldn't cross over any {@literal 0->1} boundary
     * (so the cube must be contained within a block).
     * 
     * @param type The type of sprite to use. See {@link FluidSpriteType} for more details.
     * @param fluid The stack that represents the fluid to render
     * @param cap The maximum amount of fluid that could be in the stack. Usually the capacity of the tank.
     * @param min The minimum coordinate that the tank should be rendered from
     * @param max The maximum coordinate that the tank will be rendered to.
     * @param bbIn The {@link BufferBuilder} that the fluid will be rendered into.
     * @param sideRender A size 6 boolean array that determines if the face will be rendered. If it is null then all
     *            faces will be rendered. The indexes are determined by what {@link Direction#ordinal()} returns. */
    public static void renderFluid(FluidSpriteType type, FluidStack fluid, int cap, Vec3 min, Vec3 max,
    		VertexConsumer bbIn, Pose matrix, boolean[] sideRender) {
        renderFluid(type, fluid, fluid == null ? 0 : fluid.getAmount(), cap, min, max, bbIn, matrix,sideRender);
    }

    /** Render's a fluid cuboid to the given vertex buffer. The cube shouldn't cross over any {@literal 0->1} boundary
     * (so the cube must be contained within a block).
     * 
     * @param type The type of sprite to use. See {@link FluidSpriteType} for more details.
     * @param fluid The stack that represents the fluid to render. Note that the amount from the stack is NOT used.
     * @param amount The actual amount of fluid in the stack. Is a "double" rather than an "int" as then you can
     *            interpolate between frames.
     * @param cap The maximum amount of fluid that could be in the stack. Usually the capacity of the tank.
     * @param min The minimum coordinate that the tank should be rendered from
     * @param max The maximum coordinate that the tank will be rendered to.
     * @param fluidBuffer The {@link BufferBuilder} that the fluid will be rendered into.
     * @param matrix 
     * @param sideRender A size 6 boolean array that determines if the face will be rendered. If it is null then all
     *            faces will be rendered. The indexes are determined by what {@link Direction#ordinal()} returns. */
    public static void renderFluid(FluidSpriteType type, FluidStack fluid, double amount, double cap, Vec3 min,
        Vec3 max, VertexConsumer fluidBuffer, Pose matrix, boolean[] sideRender) {
        if (fluid == null || fluid.getFluid() == null || amount <= 0) {
            return;
        }
        if (sideRender == null) {
            sideRender = DEFAULT_FACES;
        }

        double height = Mth.clamp(amount / cap, 0, 1);
        final Vec3 realMin, realMax;
        if (fluid.getFluid().getFluidType().getDensity()<0) {
            realMin = VecUtil.replaceValue(min, Axis.Y, MathUtil.interp(1 - height, min.y, max.y));
            realMax = max;
        } else {
            realMin = min;
            realMax = VecUtil.replaceValue(max, Axis.Y, MathUtil.interp(height, min.y, max.y));
        }

        bb = fluidBuffer;
        pose = matrix;

        if (type == null) {
            type = FluidSpriteType.STILL;
        }
        sprite = getFluidSprite(type, fluid.getFluid(), fluid);

        final double xs = realMin.x;
        final double ys = realMin.y;
        final double zs = realMin.z;

        final double xb = realMax.x;
        final double yb = realMax.y;
        final double zb = realMax.z;

        if (type == FluidSpriteType.FROZEN) {
            if (min.x > 1) {
                xTexDiff = Math.floor(min.x);
            } else if (min.x < 0) {
                xTexDiff = Math.floor(min.x);
            } else {
                xTexDiff = 0;
            }
            if (min.y > 1) {
                yTexDiff = Math.floor(min.y);
            } else if (min.y < 0) {
                yTexDiff = Math.floor(min.y);
            } else {
                yTexDiff = 0;
            }
            if (min.z > 1) {
                zTexDiff = Math.floor(min.z);
            } else if (min.z < 0) {
                zTexDiff = Math.floor(min.z);
            } else {
                zTexDiff = 0;
            }
        } else {
            xTexDiff = 0;
            yTexDiff = 0;
            zTexDiff = 0;
        }

//        vertex.colouri(RenderUtil.swapARGBforABGR(fluid.getFluid()));
        vertex.colouri(IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor());
        

        texmap = TexMap.XZ;
        // TODO: Enable/disable inversion for the correct faces
        invertU = false;
        invertV = false;
        if (sideRender[Direction.UP.ordinal()]) {
            vertex(xs, yb, zb);
            vertex(xb, yb, zb);
            vertex(xb, yb, zs);
            vertex(xs, yb, zs);
        }

        if (sideRender[Direction.DOWN.ordinal()]) {
            vertex(xs, ys, zs);
            vertex(xb, ys, zs);
            vertex(xb, ys, zb);
            vertex(xs, ys, zb);
        }

        texmap = TexMap.ZY;
        if (sideRender[Direction.WEST.ordinal()]) {
            vertex(xs, ys, zs);
            vertex(xs, ys, zb);
            vertex(xs, yb, zb);
            vertex(xs, yb, zs);
        }

        if (sideRender[Direction.EAST.ordinal()]) {
            vertex(xb, yb, zs);
            vertex(xb, yb, zb);
            vertex(xb, ys, zb);
            vertex(xb, ys, zs);
        }

        texmap = TexMap.XY;
        if (sideRender[Direction.NORTH.ordinal()]) {
            vertex(xs, yb, zs);
            vertex(xb, yb, zs);
            vertex(xb, ys, zs);
            vertex(xs, ys, zs);
        }

        if (sideRender[Direction.SOUTH.ordinal()]) {
            vertex(xs, ys, zb);
            vertex(xb, ys, zb);
            vertex(xb, yb, zb);
            vertex(xs, yb, zb);
        }
        

        sprite = null;
        texmap = null;
        bb = null;
        pose = null;
    }


    public static TextureAtlasSprite getFluidSprite(FluidSpriteType type, Fluid fluid, FluidStack stack) {
        if (fluid == null) {
            return SpriteUtil.missingSprite();
        }
        TextureAtlasSprite s = getSprite(type, fluid.getFluidType().getDescriptionId(), fluid, stack);
        return s != null ? s : SpriteUtil.missingSprite();
    }
    
    private static TextureAtlasSprite getSprite(FluidSpriteType type, String key, Fluid fluid, FluidStack stack) {
    	TextureAtlasSprite tex = fluidSprites.get(type).get(key);
    	if(blockTexMap == null) blockTexMap = Minecraft.getInstance()
               .getTextureAtlas(InventoryMenu.BLOCK_ATLAS);
    	switch(type){
		case FLOWING:
			if(tex == null) 
				tex = blockTexMap.apply(IClientFluidTypeExtensions.of(fluid).getFlowingTexture(stack));
			fluidSprites.get(type).put(key, tex);
			break;
		case STILL:
			if(tex == null) 
				tex = blockTexMap.apply(IClientFluidTypeExtensions.of(fluid).getStillTexture(stack));
			fluidSprites.get(type).put(key, tex);
			break;
		case FROZEN:
			if(tex == null) 
				tex = blockTexMap.apply(IClientFluidTypeExtensions.of(fluid).getStillTexture(stack));
			fluidSprites.get(type).put(key, tex);
			break;
    	}
    	return tex;
    }

    /** Helper function to add a vertex. */
    private static void vertex(double x, double y, double z) {
    	
        vertex.positiond(x, y, z);
        texmap.apply(x - xTexDiff, y - yTexDiff, z - zTexDiff);
        vertex.renderAsBlock(pose.pose(), pose.normal(), bb);
    }

    /** Fills up the given region with the fluids texture, repeated. Ignores the value of {@link FluidStack#amount}. Use
     * {@link GuiUtil}'s fluid drawing methods in preference to this. */
    public static void drawFluidForGui(FluidStack fluid, double startX, double startY, double endX, double endY, Pose matrix) {

 //       sprite = FluidRenderer.fluidSprites.get(FluidSpriteType.STILL).get(fluid.getFluid().getFluidType().getDescriptionId());
    	sprite = getFluidSprite(FluidSpriteType.STILL, fluid.getFluid(), fluid);
        color = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor();
        if (sprite == null) {
            sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation());
        }
//        Minecraft.getInstance().getBlockRenderer().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
//        RenderUtil.setGLColorFromInt(fluid.getFluid().getFluidType(fluid));

//        Tessellator tess = Tessellator.getInstance();
//        bb = tess.getBuffer();
//        bb.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

        // draw all the full sprites
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        
        bb = bufferbuilder;
        pose = matrix;

        double diffX = endX - startX;
        double diffY = endY - startY;

        int stepX = diffX > 0 ? 16 : -16;
        int stepY = diffY > 0 ? 16 : -16;

        int loopCountX = (int) Math.abs(diffX / 16);
        int loopCountY = (int) Math.abs(diffY / 16);

        double x = startX;
        for (int xc = 0; xc < loopCountX; xc++) {
            double y = startY;
            for (int yc = 0; yc < loopCountY; yc++) {
            	bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
                guiVertex(x, y, 0, 0);
                guiVertex(x + stepX, y, 16, 0);
                guiVertex(x + stepX, y + stepY, 16, 16);
                guiVertex(x, y + stepY, 0, 16);
                BufferUploader.drawWithShader(bufferbuilder.end());
                y += stepY;
            }
            x += stepX;
        }

        if (diffX % 16 != 0) {
            double additionalWidth = diffX % 16;
            x = endX - additionalWidth;
            double xTex = Math.abs(additionalWidth);
            double y = startY;
            for (int yc = 0; yc < loopCountY; yc++) {
            	bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
                guiVertex(x, y, 0, 0);
                guiVertex(endX, y, xTex, 0);
                guiVertex(endX, y + stepY, xTex, 16);
                guiVertex(x, y + stepY, 0, 16);
                BufferUploader.drawWithShader(bufferbuilder.end());
                y += stepY;
            }
        }

        if (diffY % 16 != 0) {
            double additionalHeight = diffY % 16;
            double y = endY - additionalHeight;
            double yTex = Math.abs(additionalHeight);
            x = startX;
            for (int xc = 0; xc < loopCountX; xc++) {
            	bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
                guiVertex(x, y, 0, 0);
                guiVertex(x + stepX, y, 16, 0);
                guiVertex(x + stepX, endY, 16, yTex);
                guiVertex(x, endY, 0, yTex);
                x += stepX;
                BufferUploader.drawWithShader(bufferbuilder.end());
            }
        }

        if (diffX % 16 != 0 && diffY % 16 != 0) {
            double w = diffX % 16;
            double h = diffY % 16;
            x = endX - w;
            double y = endY - h;
            double tx = w < 0 ? -w : w;
            double ty = h < 0 ? -h : h;
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_TEX);
            guiVertex(x, y, 0, 0);
            guiVertex(endX, y, tx, 0);
            guiVertex(endX, endY, tx, ty);
            guiVertex(x, endY, 0, ty);
            BufferUploader.drawWithShader(bufferbuilder.end());
        }

//        tess.draw();
        
        sprite = null;
        bb = null;
        color = 0xFFFFFFFF;
        pose = null;
    }

    private static void guiVertex(double x, double y, double u, double v) {
        float ru = sprite.getU(u);
        float rv = sprite.getV(v);
        bb.vertex(pose.pose(),(float)x, (float)y, 0);
        bb.color(color);
        bb.uv(ru, rv);
        bb.endVertex();
    }


    /** Used to keep track of what position maps to what texture co-ord.
     * <p>
     * For example XY maps X to U and Y to V, and ignores Z */
    private enum TexMap {
        XY(true, true),
        XZ(true, false),
        ZY(false, true);

        /** If true, then X maps to U. Otherwise Z maps to U. */
        private final boolean ux;
        /** If true, then Y maps to V. Otherwise Z maps to V. */
        private final boolean vy;

        TexMap(boolean ux, boolean vy) {
            this.ux = ux;
            this.vy = vy;
        }

        /** Changes the vertex's texture co-ord to be the same as the position, for that face. (Uses {@link #ux} and
         * {@link #vy} to determine how they are mapped). */
        private void apply(double x, double y, double z) {
            double realu = ux ? x : z;
            double realv = vy ? y : z;
            if (invertU) {
                realu = 1 - realu;
            }
            if (invertV) {
                realv = 1 - realv;
            }
//            realu = realu > 1 ? 1 : realu;
//            realv = realv > 1 ? 1 : realv;
            vertex.texf(sprite.getU(realu * 16), sprite.getV(realv * 16));
        }
    }

    public static class TankSize {
        public final Vec3 min;
        public final Vec3 max;

        public TankSize(int sx, int sy, int sz, int ex, int ey, int ez) {
            this(new Vec3(sx, sy, sz).scale(1 / 16.0), new Vec3(ex, ey, ez).scale(1 / 16.0));
        }

        public TankSize(Vec3 min, Vec3 max) {
            this.min = min;
            this.max = max;
        }

        public TankSize shrink(double by) {
            return shrink(by, by, by);
        }

        public TankSize shrink(double x, double y, double z) {
            return new TankSize(min.add(x, y, z), max.subtract(x, y, z));
        }

        public TankSize shink(Vec3 by) {
            return shrink(by.x, by.y, by.z);
        }

        public TankSize rotateY() {
            Vec3 _min = rotateY(min);
            Vec3 _max = rotateY(max);
            return new TankSize(VecUtil.min(_min, _max), VecUtil.max(_min, _max));
        }

        private static Vec3 rotateY(Vec3 vec) {
            return new Vec3(//
                1 - vec.z, //
                vec.y, //
                vec.x//
            );
        }
    }
}
