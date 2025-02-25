package io.github.xiewuzhiying.vs_addition.context.conditiontester;

import dev.architectury.platform.Platform;
import io.github.xiewuzhiying.vs_addition.VSAdditionConfig;
import me.fallenbreath.conditionalmixin.api.mixin.ConditionTester;

public class InteractiveConditionTester implements ConditionTester {
    @Override
    public boolean isSatisfied(String s) {
        return !VSAdditionConfig.COMMON.getCreate().getInsteadCreateInteractiveDeployer() && Platform.isModLoaded("create_interactive");
    }
}
