package com.renatocea.pvpzones.event;

import com.renatocea.pvpzones.zone.Zone;
import com.renatocea.pvpzones.zone.ZoneManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

public class PlayerZoneTracker {

    private final Map<UUID, Vec3> lastPositions = new WeakHashMap<>();
    private final Map<UUID, Boolean> playerZoneState = new WeakHashMap<>();

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Pre event) {
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (player == null || player.getCommandSenderWorld().isClientSide) continue;

            UUID uuid = player.getUUID();
            Vec3 currentPos = player.position();
            Vec3 lastPos = lastPositions.get(uuid);

            if (lastPos != null && lastPos.equals(currentPos)) continue;

            lastPositions.put(uuid, currentPos);

            double px = currentPos.x;
            double pz = currentPos.z;

            Zone zone = ZoneManager.getInstance().getZoneAtPosition(px, pz);
            boolean wasInZone = playerZoneState.getOrDefault(uuid, false);
            boolean isInZone = zone != null;

            if (isInZone && !wasInZone) {
                playerZoneState.put(uuid, true);
                player.displayClientMessage(
                        Component.literal("§c§l⚠ NO MAN'S LAND ⚠ §7| §cYou've entered §4No Man's Land§c. Rules not enforced past this point!"),
                        true
                );
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_HEARTBEAT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0f, 0.8f);
            } else if (!isInZone && wasInZone) {
                playerZoneState.put(uuid, false);
                player.displayClientMessage(Component.literal("§a§l⚑ SAFE ZONE ⚑ §7| §aYou've left §4No Man's Land§a. Rules are now strictly enforced."), true);
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.BEACON_AMBIENT, net.minecraft.sounds.SoundSource.PLAYERS, 0.8f, 1.2f);
            }
        }
    }
}
