package io.github.xiewuzhiying.vs_addition.mixin.create.client.redstone.displayLink;

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
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.velocity_modulus"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.velocity_modulus_km_h"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.velocity_components"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.omega_modulus"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.omega_components"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.mass_center_position_in_world"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.source_block_position_in_world"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.mass_center_position_in_ship"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.source_block_position_in_ship"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.roll_radians"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.roll_degrees"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.pitch_radians"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.pitch_degrees"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.yaw_radians"),
                            Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data.yaw_degrees")))
                        .titled(Component.translatable(VSAdditionMod.MOD_ID + ".display_source.ship_data")), "Mode");
    }
}
