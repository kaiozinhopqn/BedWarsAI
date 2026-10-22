package com.bedwarsai.bot;

import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.util.MessageDeserializer;
import net.minecraft.util.MessageDeserializer2;
import net.minecraft.util.MessageSerializer;
import net.minecraft.util.MessageSerializer2;

/**
 * Conexao falsa: descarta todos os pacotes enviados ao bot (evita fila crescendo na memoria).
 *
 * O pipeline e montado igual ao do servidor 1.8.9 porque o NetworkManager so
 * preenche o campo {@code channel} em {@code channelActive()}. Sem canal, o FML
 * (FMLNetworkHandler.makeEntitySpawnAdjustment) acessa channel() e estoura NPE
 * no primeiro tick do EntityTracker.
 */
public class FakeNetworkManager extends NetworkManager {

    public FakeNetworkManager() {
        super(EnumPacketDirection.SERVERBOUND);
        new EmbeddedChannel(
                new MessageDeserializer2(),
                new MessageDeserializer(EnumPacketDirection.SERVERBOUND),
                new MessageSerializer2(),
                new MessageSerializer(EnumPacketDirection.CLIENTBOUND),
                this);
    }

    @Override
    public void sendPacket(Packet packetIn) {
        // descarta
    }
}
