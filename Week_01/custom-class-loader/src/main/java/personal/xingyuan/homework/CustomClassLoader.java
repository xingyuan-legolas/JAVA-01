package personal.xingyuan.homework;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CustomClassLoader extends ClassLoader {

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {

        URL url = getClass().getClassLoader().getResource(String.format("./lib/%s.xlass", name));
        if (url == null) {
            throw new ClassNotFoundException(name);
        }

        try {
            byte[] content = Files.readAllBytes(Paths.get(url.getPath()));
            decodeInPlace(content);
            return defineClass(name, content, 0, content.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name);
        }
    }

    private void decodeInPlace(byte[] content) {
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) ~content[i];
        }
    }
}
