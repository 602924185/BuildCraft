/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.misc;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.render.ISprite;
import ct.buildcraft.lib.client.sprite.SpriteRaw;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.entity.SkullBlockEntity;

public class SpriteUtil {

    private static final ResourceLocation LOCATION_SKIN_LOADING = new ResourceLocation("skin:loading");
    protected static TextureAtlasSprite MISSING_TEX ;
    private static final Map<GameProfile, GameProfile> CACHED = new HashMap<>();

    public static void bindBlockTextureMap() {
        bindTexture(TextureAtlas.LOCATION_BLOCKS);
    }

    public static void bindTexture(String identifier) {
        bindTexture(new ResourceLocation(identifier));
    }

    public static void bindTexture(ResourceLocation identifier) {
        Minecraft.getInstance().textureManager.bindForSetup(identifier);
    }

    /** Transforms the given {@link ResourceLocation}, adding ".png" to the end and prepending that
     * {@link ResourceLocation#getResourcePath()} with "textures/", just like what {@link TextureMap} does. */
    public static ResourceLocation transformLocation(ResourceLocation location) {
        return new ResourceLocation(location.getNamespace(), "textures/" + location.getPath() + ".png");
    }

    @Nullable
    public static ResourceLocation getSkinSpriteLocation(GameProfile profile) {
        ResourceLocation loc = getSkinSpriteLocation0(profile);
        return loc == LOCATION_SKIN_LOADING ? null : loc;
    }

    @Nullable
    private static ResourceLocation getSkinSpriteLocation0(GameProfile profile) {
        if (profile == null) {
            return null;
        }
        Minecraft mc = Minecraft.getInstance();

        if (CACHED.containsKey(profile) && CACHED.get(profile) == null && Math.random() >= 0.99) {
            CACHED.remove(profile);
        }

        try {
            if (!CACHED.containsKey(profile)) {
            	var p = profile;
            	SkullBlockEntity.updateGameprofile(profile, (a) ->
            	CACHED.put(p, a));
            }
            GameProfile p2 = CACHED.get(profile);
            if (p2 == null) {
                return null;
            }
            profile = p2;
            Map<Type, MinecraftProfileTexture> map = mc.getSkinManager().getInsecureSkinInformation(profile);
            MinecraftProfileTexture tex = map.get(Type.SKIN);
            if (tex != null) {
                return mc.getSkinManager().registerTexture(tex, Type.SKIN);
            }
            return LOCATION_SKIN_LOADING;
        } catch (NullPointerException | ClassCastException e) {
            // Fix for https://github.com/BuildCraft/BuildCraft/issues/4419
            // I'm not quite sure why this throws an NPE but this should at
            // least stop it from crashing
            e.printStackTrace();
            CACHED.put(profile, profile);
            return null;
        }
    }
/*//temp cancel
    public static ISprite getFaceSprite(GameProfile profile) {
        if (profile == null) {
            return BCLibSprites.HELP;
        }
        ResourceLocation loc = getSkinSpriteLocation0(profile);
        if (loc == null) {
            return BCLibSprites.LOCK;
        }
        return new SpriteRaw(loc, 8, 8, 8, 8, 64);
    }
*/
    @Nullable
    public static ISprite getFaceOverlaySprite(GameProfile profile) {
        if (profile == null) {
            return null;
        }
        ResourceLocation loc = getSkinSpriteLocation0(profile);
        if (loc == null) {
            return null;
        }
        return new SpriteRaw(loc, 40, 8, 8, 8, 64);
    }

    public static TextureAtlasSprite missingSprite() {
        if(MISSING_TEX == null)
        	MISSING_TEX = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(MissingTextureAtlasSprite.getLocation());
        return MISSING_TEX;
    }
}
