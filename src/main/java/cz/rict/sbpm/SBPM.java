package cz.rict.sbpm;

import cz.rict.sbpm.clientcommand.RenderTemplateCommand;
import cz.rict.sbpm.render.StructureDisplayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(SBPM.MOD_ID)
public class SBPM
{
    public static final String MOD_ID = "sbpm";

    public SBPM()
    {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            MinecraftForge.EVENT_BUS.register(StructureDisplayer.class);
            MinecraftForge.EVENT_BUS.register(RenderTemplateCommand.class);
        });
    }

    public static ResourceLocation createLocationFor(final String path)
    {
        return new ResourceLocation(MOD_ID, path);
    }
}
