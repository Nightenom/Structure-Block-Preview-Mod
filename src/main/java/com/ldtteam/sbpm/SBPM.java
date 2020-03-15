package com.ldtteam.sbpm;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("sbpm")
public class SBPM
{
    public SBPM()
    {
        MinecraftForge.EVENT_BUS.register(StructureDisplayer.class);
    }
}
