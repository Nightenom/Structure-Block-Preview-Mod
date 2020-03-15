package com.ldtteam.sbpm;

import net.minecraft.state.properties.StructureMode;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

public class CoremodListeners
{
    private CoremodListeners()
    {
    }

    public static void sbteNextMode(final StructureMode mode)
    {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> StructureDisplayer.onStructureBlockUpdate(mode));
    }

    public static void essDonePressed()
    {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> StructureDisplayer.onStructureBlockUpdate(null));
    }
}
