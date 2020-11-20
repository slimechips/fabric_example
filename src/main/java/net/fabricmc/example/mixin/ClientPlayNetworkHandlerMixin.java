package net.fabricmc.example.mixin;

import net.fabricmc.example.HostTransfer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.realms.gui.screen.DisconnectedRealmsScreen;
import net.minecraft.client.realms.gui.screen.RealmsScreen;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.Text;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin implements HostTransfer {

    private String hostIp = null;

    @Shadow @Final
    private static Logger LOGGER;

    @Shadow @Final
    private ClientConnection connection;

    @Shadow
    private MinecraftClient client;

    @Shadow @Final
    private Screen loginScreen;

    @Shadow @Final
    private static Text field_26620;

    /**
     * @author slimechips
     * @reason host_transfer=play.minesuperior.com
     */
    @Overwrite
    public void onDisconnect(DisconnectS2CPacket packet) {
        this.connection.disconnect(packet.getReason());
        LOGGER.info("Checking for Host Transfer Disconnect");
        if (packet.getReason() == null) return;
        String[] reason = packet.getReason().asString().split("=");
        if (reason[0].equals("host_transfer") && reason.length == 2)
        {
            LOGGER.info("Initiating host transfer to" + reason[1]);
            this.hostIp = reason[1];
        }
    }

    /**
     * @author slimechips
     * @reason host_transfer=play.minesuperior.com
     */
    @Overwrite
    public void onDisconnected(Text reason) {
        this.client.disconnect();
        System.out.println("Host Transfer Lul");

        if (this.loginScreen != null) {
            if (this.loginScreen instanceof RealmsScreen) {
                this.client.openScreen(new DisconnectedRealmsScreen(this.loginScreen, field_26620, reason));
            } else {
                this.client.openScreen(new DisconnectedScreen(this.loginScreen, field_26620, reason));
            }
        } else {
            this.client.openScreen(new DisconnectedScreen(new MultiplayerScreen(new TitleScreen()), field_26620, reason));
        }

        if (this.hostIp == null) return;
        this.client.openScreen(new ConnectScreen(this.loginScreen, this.client,
                new ServerInfo("host_xfer_server", this.hostIp, false)));

    }
}
