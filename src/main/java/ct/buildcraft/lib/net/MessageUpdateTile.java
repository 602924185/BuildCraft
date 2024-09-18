/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.net;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.misc.MessageUtil;
import com.mojang.logging.LogUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

public class MessageUpdateTile {
    private BlockPos pos;
    private FriendlyByteBuf payload;

    @SuppressWarnings("unused")
    public MessageUpdateTile() {}

    public MessageUpdateTile(BlockPos pos, FriendlyByteBuf payload) {
        this.pos = pos;
        this.payload = payload;
        if (getPayloadSize() > 1 << 24) {
            throw new IllegalStateException("Can't write out " + getPayloadSize() + "bytes!");
        }
    }

    public int getPayloadSize() {
        return payload == null ? 0 : payload.readableBytes();
    }

    public MessageUpdateTile(FriendlyByteBuf buf) {
        pos = buf.readBlockPos();
        buf.discardReadBytes();
        payload = buf;
    }

    public static void toBytes(MessageUpdateTile msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeBytes(msg.payload);
    }

    public static final BiConsumer<MessageUpdateTile, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
    	
    	ctx.get().enqueueWork(() -> {
        	try {
                ClientLevel level = Minecraft.getInstance().level;
                if (level == null) {
                    return;
                }
//                BCLog.logger.debug("trying to updata client pipe in "+ message.pos);
                BlockEntity tile = level.getBlockEntity(message.pos);
                if (tile instanceof IPayloadReceiver) {
                	((IPayloadReceiver) tile).receivePayload(ctx.get(), message.payload);
                    return ;
                } else {
                    BCLog.logger.warn("Dropped message for player " + "null" + " for tile at " + message.pos
                        + " (found " + tile + ")");
                }
                return;
            } catch (IOException io) {
                throw new RuntimeException(io);
            } finally {
                message.payload.release();
            }
    	  });
    	  ctx.get().setPacketHandled(true);

    	
    };
}
