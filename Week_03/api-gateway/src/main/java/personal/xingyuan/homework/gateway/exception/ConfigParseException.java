package personal.xingyuan.homework.gateway.exception;

public class ConfigParseException extends RuntimeException {
    public ConfigParseException(Throwable cause) {
        super("配置读取失败", cause);
    }
}
