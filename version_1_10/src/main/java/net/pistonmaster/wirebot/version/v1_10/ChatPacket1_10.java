package net.pistonmaster.wirebot.version.v1_10;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;

public class ChatPacket1_10 extends ClientChatPacket {
    public ChatPacket1_10(String message) {
        super(message);
    }
}
