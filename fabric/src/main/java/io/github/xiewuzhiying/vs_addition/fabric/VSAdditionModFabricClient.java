package io.github.xiewuzhiying.vs_addition.fabric;

import net.fabricmc.api.ClientModInitializer;
import org.valkyrienskies.mod.common.hooks.VSGameEvents;

import static io.github.xiewuzhiying.vs_addition.VSAdditionMod.initClient;

public class VSAdditionModFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        initClient();
    }
}
