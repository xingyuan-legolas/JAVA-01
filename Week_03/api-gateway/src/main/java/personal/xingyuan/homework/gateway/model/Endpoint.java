package personal.xingyuan.homework.gateway.model;

public class Endpoint {
    private final String address;
    private final int port;

    public Endpoint(String address, int port) {
        this.address = address;
        this.port = port;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
