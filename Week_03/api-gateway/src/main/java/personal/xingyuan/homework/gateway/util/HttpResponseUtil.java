package personal.xingyuan.homework.gateway.util;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

public class HttpResponseUtil {

    public static final HttpResponse NOT_MODIFIED = withNoContent(HttpResponseStatus.NOT_MODIFIED);
    public static final HttpResponse BAD_REQUEST = withNoContent(HttpResponseStatus.BAD_REQUEST);
    public static final HttpResponse NOT_FOUND = withNoContent(HttpResponseStatus.NOT_FOUND);
    public static final HttpResponse INTERNAL_SERVER_ERROR = withNoContent(HttpResponseStatus.INTERNAL_SERVER_ERROR);
    public static final HttpResponse BAD_GATEWAY = withNoContent(HttpResponseStatus.BAD_GATEWAY);

    private static HttpResponse withNoContent(HttpResponseStatus statusCode) {
        HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, statusCode, Unpooled.EMPTY_BUFFER);
        response.headers().add(HttpHeaderNames.CONTENT_LENGTH, "0");
        return response;
    }
}
