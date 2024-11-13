package io.github.xiewuzhiying.vs_addition.mixin.create.client.displayLink;

import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.NumericSingleLineDisplaySource;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import io.github.xiewuzhiying.vs_addition.VSAdditionMod;
import io.github.xiewuzhiying.vs_addition.compats.create.content.redstone.displayLink.ShipDataDisplaySource;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Arrays;

@Mixin(ShipDataDisplaySource.class)
public abstract class MixinShipDataDisplaySource extends NumericSingleLineDisplaySource {
    @Override
    public void initConfigurationWidgets(DisplayLinkContext context, ModularGuiLineBuilder builder, boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
        if (isFirstLine)
            return;
        builder.addSelectionScrollInput(0, 120,
                (si, l) -> si.forOptions(Arrays.asList(
                        Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.id"),
                        Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.slug"),
                        Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.velocity"),
                        Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.omega"),
                        Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.position_in_world"),
                        Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.position_in_ship")))
                        .titled(Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data")), "Mode");
    }
}
