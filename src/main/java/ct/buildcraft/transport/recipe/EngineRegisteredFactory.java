package ct.buildcraft.transport.recipe;

import java.util.function.BooleanSupplier;

import com.google.gson.JsonObject;

import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;

import ct.buildcraft.api.enums.EnumEngineType;

import ct.buildcraft.core.BCCoreBlocks;

public class EngineRegisteredFactory implements IConditionFactory {

    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json) {
        return () -> BCCoreBlocks.engine.isRegistered(EnumEngineType.valueOf(json.get("engineType").getAsString()));
    }
}
