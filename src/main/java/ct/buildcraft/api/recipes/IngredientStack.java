/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.api.recipes;

import com.google.gson.JsonElement;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;

public final class IngredientStack {
    public final Ingredient ingredient;
    public final int count;

    public IngredientStack(Ingredient ingredient, int count) {
        this.ingredient = ingredient;
        this.count = count;
    }

    public IngredientStack(Ingredient ingredient) {
        this(ingredient, 1);
    }

    public static IngredientStack of(JsonElement o) {
        return new IngredientStack(CraftingHelper.getIngredient(o));
    }
}
