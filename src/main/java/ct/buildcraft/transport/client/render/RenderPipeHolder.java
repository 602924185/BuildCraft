/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.transport.client.render;

import ct.buildcraft.api.transport.pipe.IPipeFlowRenderer;
import ct.buildcraft.api.transport.pipe.PipeFlow;
import ct.buildcraft.api.transport.pluggable.IPlugDynamicRenderer;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.transport.client.PipeRegistryClient;
import ct.buildcraft.transport.pipe.Pipe;
import ct.buildcraft.transport.tile.TilePipeHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;



public class RenderPipeHolder implements BlockEntityRenderer<TilePipeHolder> {
	

	public static final Direction[] renderFacing = {Direction.UP,Direction.NORTH,Direction.WEST,Direction.SOUTH,Direction.EAST,Direction.DOWN};
	public static final int[] CENTER_UV = {4,12,4,12};
	public static final int[] EAST_UV = {0,8,4,16};
	public static final int[] WEST_UV = {0,8,4,16};
	public static final int[] SOUTH_UV = {0,8,4,16};
	public static final int[] NORTH_UV = {0,8,4,16};
	public static final int[] UP_UV = {4,12,0,4};
	public static final int[] DOWN_UV = {4,12,12,16};
	
	
	public RenderPipeHolder(BlockEntityRendererProvider.Context ctx) {
    }
	
	@Override
	public void render(TilePipeHolder pipe, float partialTicks, PoseStack matrix, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
		Pipe p = pipe.getPipe();
		if(p == Pipe.EMPTY) return;
		matrix.pushPose();
        float conSize = 0;
        renderContents(pipe, partialTicks, matrix, buffer, combinedLight, combinedOverlay);
        renderPluggables(pipe, conSize, matrix, buffer, combinedOverlay, combinedOverlay);
		matrix.popPose();
		
	}
    private static void renderPluggables(TilePipeHolder pipe,  float partialTicks, PoseStack matrix, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
        for (Direction face : Direction.values()) {
            PipePluggable plug = pipe.getPluggable(face);
            if (plug == PipePluggable.EMPTY) {
                continue;
            }
            renderPlug(plug, partialTicks, matrix, buffer, combinedOverlay, combinedOverlay);
        }
    }

    private static <P extends PipePluggable> void renderPlug(P plug, float partialTicks, PoseStack matrix, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
        IPlugDynamicRenderer<P> renderer = PipeRegistryClient.getPlugRenderer(plug);
        if (renderer != null) {
            renderer.render(plug, partialTicks, matrix, buffer, combinedOverlay, combinedOverlay);
        }
    }

    private static void renderContents(TilePipeHolder pipe, float partialTicks, PoseStack matrix, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
        Pipe p = pipe.getPipe();
        if (p == null) {
            return;
        }
        if (p.flow != null) {
            renderFlow(p.flow,  partialTicks, matrix, buffer, combinedLight, combinedOverlay);
        }
/*        if (p.behaviour != null) {
            renderBehaviour(p.behaviour, x, y, z, partialTicks, bb);
        }*/
    }

    private static <F extends PipeFlow> void renderFlow(F flow, float partialTicks, PoseStack matrix, MultiBufferSource buffer,
			int combinedLight, int combinedOverlay) {
        IPipeFlowRenderer<F> renderer = PipeRegistryClient.getFlowRenderer(flow);
        if (renderer != null) {
            renderer.render(flow, partialTicks, matrix, buffer, combinedLight, combinedOverlay);
        }
    }
/*
    private static <B extends PipeBehaviour> void renderBehaviour(B behaviour, double x, double y, double z,
        float partialTicks, BufferBuilder bb) {
        IPipeBehaviourRenderer<B> renderer = PipeRegistryClient.getBehaviourRenderer(behaviour);
        if (renderer != null) {
            Minecraft.getMinecraft().mcProfiler.startSection(behaviour.getClass());
            renderer.render(behaviour, x, y, z, partialTicks, bb);
            Minecraft.getMinecraft().mcProfiler.endSection();
        }
    }

*/
}
