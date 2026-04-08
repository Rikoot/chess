package websocket.messages;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ServerMessageDeserializer implements JsonDeserializer<ServerMessage> {
    @Override
    public ServerMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject serverMessage = jsonElement.getAsJsonObject();
        String serverType = serverMessage.get("serverMessageType").getAsString();

        switch (serverType) {
            case "LOAD_GAME" -> {
                return jsonDeserializationContext.deserialize(serverMessage, LoadGameMessage.class);
            }
            case "ERROR" -> {
                return jsonDeserializationContext.deserialize(serverMessage, ErrorMessage.class);
            }
            case "NOTIFICATION" -> {
                return jsonDeserializationContext.deserialize(serverMessage, NotificationMessage.class);
            }
            default -> {
                throw new JsonParseException("Unknown Type");
            }
        }
    }
}
