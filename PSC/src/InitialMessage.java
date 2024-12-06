import java.io.Serializable;

class InitialMessage implements Serializable {
    final private boolean fogOfWar;
    final private String version;

    InitialMessage(Boolean fogOfWar, String version) {
        this.fogOfWar = fogOfWar;
        this.version = version;
    }

    String getVersion() {
        return version;
    }

    boolean checkFogOfWar(InitialMessage message) {
        return this.fogOfWar == message.fogOfWar;
    }

    boolean checkVersion (InitialMessage message) {
        return this.version.equals(message.version);
    }

}
