package net.cwjn.idf.event;

import net.cwjn.idf.api.event.LivingLifestealEvent;
import net.cwjn.idf.api.event.OnItemStackCreatedEvent;
import net.cwjn.idf.api.event.PostMitigationDamageEvent;
import net.cwjn.idf.attribute.IDFAttributes;
import net.cwjn.idf.command.ChangeDebugStatusCommand;
import net.cwjn.idf.config.CommonConfig;
import net.cwjn.idf.network.PacketHandler;
import net.cwjn.idf.network.packets.DisplayDamageIndicatorPacket;
import net.cwjn.idf.network.packets.SyncSkyDarkenPacket;
import net.cwjn.idf.util.Color;
import net.cwjn.idf.util.ItemInterface;
import net.cwjn.idf.util.Util;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.level.ChunkWatchEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

import java.util.*;

@Mod.EventBusSubscriber
public class LogicalEvents {

    public static boolean debugMode = false;
    private static final Random random = new Random();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void instantiateDefaultTags(OnItemStackCreatedEvent event) {
        ItemStack item = event.getItemStack();
        Item baseItem = item.getItem();
        CompoundTag defaultTag = ((ItemInterface) baseItem).getDefaultTags();
        if (defaultTag != null) {
            item.getOrCreateTag().merge(defaultTag);
            if (CommonConfig.LEGENDARY_TOOLTIPS_COMPAT_MODE.get() && !item.getTag().contains("idf.tooltip_border")) {
                item.getTag().putInt("idf.tooltip_border", Util.getItemBorderType(item.getTag().getString("idf.damage_class"), item.getAttributeModifiers(EquipmentSlot.MAINHAND)));
            }
        }
    }

    @SubscribeEvent
    public static void assertMaxHP(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void cancelWeakAttacks(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!player.level.isClientSide()) {
            if (player.getAttackStrengthScale(0.5f) < 0.4) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void checkEvasion(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (target.getLevel() instanceof ServerLevel level) {
            if (target.getAttributeValue(IDFAttributes.EVASION.get())/100 >= Math.random()) {
                for (int i = 0; i < 360; i++) {
                    if (i % 20 == 0) {
                        level.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.5, target.getZ(), 3, Math.cos(i) * 0.25, 0, Math.sin(i) * 0.25, 0.25);
                        level.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1.25, target.getZ(), 3, Math.cos(i) * 0.25, 0, Math.sin(i) * 0.25, 0.25);
                        level.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 1, target.getZ(), 3, Math.cos(i) * 0.25, 0, Math.sin(i) * 0.25, 0.25);
                        level.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.75, target.getZ(), 3, Math.cos(i) * 0.25, 0, Math.sin(i) * 0.25, 0.25);
                        level.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY() + 0.5, target.getZ(), 3, Math.cos(i) * 0.25, 0, Math.sin(i) * 0.25, 0.25);
                    }
                }
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addDamageIndicators(PostMitigationDamageEvent event) {
        LivingEntity target = event.getTarget();
        Level world = target.getLevel();
        UUID id = net.minecraft.Util.NIL_UUID;
        if (target instanceof Player) id = target.getUUID();
        if (!world.isClientSide()) {
            double x = target.getX();
            double y = target.getEyeY();
            double z = target.getZ();
            List<Float> locs = new ArrayList<>(List.of(
                    random.nextFloat(-1.0f, -0.6666667f),
                    random.nextFloat(-0.6666667f, -0.333333f),
                    random.nextFloat(-0.333333f, 0),
                    random.nextFloat(0, 0.333333f),
                    random.nextFloat(0.333333f, 0.6666667f),
                    random.nextFloat(0.6666667f, 1.0f),
                    random.nextFloat(1.0f, 1.333333f)));
            if (event.getFire() > 0) {
                int loc = random.nextInt(locs.size());
                PacketHandler.serverToNearPoint(new DisplayDamageIndicatorPacket(x, y, z, event.getFire(), locs.get(loc), Color.FIRE_COLOUR.getColor(), id), x, y, z, 15, target.getCommandSenderWorld().dimension());
                locs.remove(loc);
            }
            if (event.getWater() > 0) {
                int loc = random.nextInt(locs.size());
                PacketHandler.serverToNearPoint(new DisplayDamageIndicatorPacket(x, y, z, event.getWater(), locs.get(loc), Color.WATER_COLOUR.getColor(), id), x, y, z, 15, target.getCommandSenderWorld().dimension());
                locs.remove(loc);
            }
            if (event.getLightning() > 0) {
                int loc = random.nextInt(locs.size());
                PacketHandler.serverToNearPoint(new DisplayDamageIndicatorPacket(x, y, z, event.getLightning(), locs.get(loc),  Color.LIGHTNING_COLOUR.getColor(), id), x, y, z, 15, target.getCommandSenderWorld().dimension());
                locs.remove(loc);
            }
            if (event.getMagic() > 0) {
                int loc = random.nextInt(locs.size());
                PacketHandler.serverToNearPoint(new DisplayDamageIndicatorPacket(x, y, z, event.getMagic(), locs.get(loc),  Color.MAGIC_COLOUR.getColor(), id), x, y, z, 15, target.getCommandSenderWorld().dimension());
                locs.remove(loc);
            }
            if (event.getDark() > 0) {
                int loc = random.nextInt(locs.size());
                PacketHandler.serverToNearPoint(new DisplayDamageIndicatorPacket(x, y, z, event.getDark(), locs.get(loc),  Color.DARK_COLOUR.getColor(), id), x, y, z, 15, target.getCommandSenderWorld().dimension());
                locs.remove(loc);
            }
            if (event.getPhysical() > 0) {
                int loc = random.nextInt(locs.size());
                PacketHandler.serverToNearPoint(new DisplayDamageIndicatorPacket(x, y, z, event.getPhysical(), locs.get(loc), Color.PHYSICAL_COLOUR.getColor(), id), x, y, z, 15, target.getCommandSenderWorld().dimension());
            }
            if (event.getHoly() > 0) {
                int loc = random.nextInt(locs.size());
                PacketHandler.serverToNearPoint(new DisplayDamageIndicatorPacket(x, y, z, event.getHoly(), locs.get(loc), Color.HOLY_COLOUR.getColor(), id), x, y, z, 15, target.getCommandSenderWorld().dimension());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void addLifestealIndicator(LivingLifestealEvent event) {
        if (event.getEntity() instanceof Player player) {
            LivingEntity target = event.getTarget();
            if (target.getLevel() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.CRIMSON_SPORE, target.getX(), target.getEyeY(), target.getZ(), 3, player.getX() - target.getX(), player.getY() - target.getY(), player.getZ() - target.getZ(), 1);
            }
        }
    }

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new ChangeDebugStatusCommand(event.getDispatcher());
        ConfigCommand.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent event) {
        if (event.getLevel() == event.getPlayer().getLevel()) {
            PacketHandler.serverToPlayer(new SyncSkyDarkenPacket(event.getLevel().getSkyDarken()), event.getPlayer());
        }
    }

}
