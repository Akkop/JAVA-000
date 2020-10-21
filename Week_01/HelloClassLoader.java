package bean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HelloClassLoader extends ClassLoader {

    public static void main(String[] args) {
        try {
            Class<?> aClass = null;
            try {
                aClass = new HelloClassLoader().findClass("Hello");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            Method hello = aClass.getDeclaredMethod("hello");
            hello.setAccessible(true);
            hello.invoke(aClass.newInstance());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String filePath = this.getClass().getResource("/Hello.xlass").getPath();
        File file = new File(filePath);
        int length = (int) file.length();
        byte[] bytes = new byte[length];//一次性将文件中的值都放入缓冲中
        try {
            new FileInputStream(file).read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < length; i++) {
            bytes[i] = (byte) (255 - bytes[i]);//将字节码文件的值转换过来
        }
        return defineClass(name, bytes, 0, length);
    }
}
