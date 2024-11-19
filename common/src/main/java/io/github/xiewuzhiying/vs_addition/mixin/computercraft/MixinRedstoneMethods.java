package io.github.xiewuzhiying.vs_addition.mixin.computercraft;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.core.apis.RedstoneMethods;
import dan200.computercraft.core.computer.ComputerSide;
import dan200.computercraft.core.redstone.RedstoneAccess;
import org.spongepowered.asm.mixin.*;

@Pseudo
@Mixin(RedstoneMethods.class)
public abstract class MixinRedstoneMethods {

    @Shadow @Final private RedstoneAccess redstone;

    @LuaFunction({"setAnalogOutput", "setAnalogueOutput"})
    public final void setAnalogOutput(ComputerSide side, int value) throws LuaException {
        if( value < 0) throw new LuaException( "Expected number in range >= 0" );
        this.redstone.setOutput(side, value);
    }
}
