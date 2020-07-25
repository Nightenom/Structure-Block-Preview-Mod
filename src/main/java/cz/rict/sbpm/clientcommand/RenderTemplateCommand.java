package cz.rict.sbpm.clientcommand;

import java.util.HashMap;
import java.util.Map;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import cz.rict.sbpm.render.StructureDisplayer;
import net.minecraft.client.Minecraft;
import net.minecraft.command.arguments.LocationPart;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.feature.template.PlacementSettings;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RenderTemplateCommand
{
    private static final Map<String, Mirror> mirrors = new HashMap<>();
    private static final Map<String, Rotation> rotations = new HashMap<>();

    static
    {
        mirrors.put("n", Mirror.NONE);
        mirrors.put("fb", Mirror.FRONT_BACK);
        mirrors.put("lr", Mirror.LEFT_RIGHT);
        rotations.put("0", Rotation.NONE);
        rotations.put("90", Rotation.CLOCKWISE_90);
        rotations.put("180", Rotation.CLOCKWISE_180);
        rotations.put("270", Rotation.COUNTERCLOCKWISE_90);
    }

    @SubscribeEvent
    public static void onClientChat(final ClientChatEvent event)
    {
        if (event.getOriginalMessage().startsWith("/sbpm "))
        {
            Minecraft.getInstance().ingameGUI.getChatGUI().addToSentMessages(event.getOriginalMessage());
            event.setMessage("");
            event.setCanceled(true);
            final String[] parts = event.getOriginalMessage().split(" ");
            final BlockPos playerPos = Minecraft.getInstance().player.getPosition();
            BlockPos pos = playerPos;
            Mirror mirror = Mirror.NONE;
            Rotation rotation = Rotation.NONE;
            boolean ignoreEntities = true;
            if (parts.length >= 5)
            {
                try
                {
                    pos = parseBlockPos(new StringReader(parts[2] + " " + parts[3] + " " + parts[4]), playerPos);
                }
                catch (final CommandSyntaxException | IllegalArgumentException e)
                {
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent("Wrong pos argument"));
                    return;
                }
            }
            if (parts.length >= 6)
            {
                if (rotations.containsKey(parts[5]))
                {
                    rotation = rotations.get(parts[5]);
                }
                else
                {
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent("Wrong rotation argument"));
                    return;
                }
            }
            if (parts.length >= 7)
            {
                if (mirrors.containsKey(parts[6]))
                {
                    mirror = mirrors.get(parts[6]);
                }
                else
                {
                    Minecraft.getInstance().player.sendMessage(new StringTextComponent("Wrong mirror argument"));
                    return;
                }
            }
            if (parts.length >= 8)
            {
                ignoreEntities = !parts[7].equals("f");
            }
            try
            {
                StructureDisplayer.loadTemplate(pos,
                    parts[1],
                    new PlacementSettings().setMirror(mirror).setRotation(rotation).setIgnoreEntities(ignoreEntities).setChunk(null));
            }
            catch (Throwable e)
            {
                StructureDisplayer.LOGGER.info("", e);
            }
        }
    }

    private static BlockPos parseBlockPos(StringReader reader, BlockPos relative) throws CommandSyntaxException
    {
        int i = reader.getCursor();
        LocationPart locationpart = LocationPart.parseInt(reader);
        if (reader.canRead() && reader.peek() == ' ')
        {
            reader.skip();
            LocationPart locationpart1 = LocationPart.parseInt(reader);
            if (reader.canRead() && reader.peek() == ' ')
            {
                reader.skip();
                LocationPart locationpart2 = LocationPart.parseInt(reader);
                return new BlockPos(locationpart.get(relative.getX()),
                    locationpart1.get(relative.getY()),
                    locationpart2.get(relative.getZ()));
            }
            else
            {
                reader.setCursor(i);
                throw new IllegalArgumentException();
            }
        }
        else
        {
            reader.setCursor(i);
            throw new IllegalArgumentException();
        }
    }
}
