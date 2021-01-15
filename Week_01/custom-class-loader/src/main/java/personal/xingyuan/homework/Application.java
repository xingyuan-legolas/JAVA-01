package personal.xingyuan.homework;

import java.lang.reflect.Method;

public class Application {
    public static void main(String[] args) {
        try {
            Class<?> clazz = new CustomClassLoader().loadClass("Hello");
            Method method = clazz.getMethod("hello");
            method.invoke(clazz.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
