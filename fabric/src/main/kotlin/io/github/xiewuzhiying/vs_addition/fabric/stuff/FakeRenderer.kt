package io.github.xiewuzhiying.vs_addition.fabric.stuff

import io.github.xiewuzhiying.vs_addition.VSAdditionConfig
import io.github.xiewuzhiying.vs_addition.stuff.airpocket.FakeAirPocketClient
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents

class FakeRenderer : WorldRenderEvents.DebugRender {
    override fun beforeDebugRender(context: WorldRenderContext) {
        if (VSAdditionConfig.COMMON.experimental.fakeAirPocket) {
            FakeAirPocketClient.render(context.matrixStack(), context.camera(), context.consumers())
        }
    }
}