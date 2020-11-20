package net.fabricmc.example;

import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.Text;

public interface HostTransfer {
    void onDisconnect(DisconnectS2CPacket packet);
    void onDisconnected(Text reason);
}
