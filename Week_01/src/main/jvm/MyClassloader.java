package jvm;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MyClassloader extends ClassLoader {

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        MyClassloader classloader = new MyClassloader();
        Object o = classloader.findClass("jvm.Hello").newInstance();
        System.out.println(o);
    }

    @Override
    protected Class<?> findClass(String name) {
        byte[] classByte = Objects.requireNonNull(getClassByte());
        return defineClass(classByte, 0, classByte.length);
    }

    private byte[] getClassByte() {
        try {
            List<Path> paths = findClassPath();
            byte[] bytes = Files.readAllBytes(paths.get(0));
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) (255 - bytes[i]);
            }
            return bytes;
        } catch (IOException e) {
            return null;
        }
    }

    private List<Path> findClassPath() throws IOException {
        Path path = Paths.get(System.getProperty("user.dir"));
        List<Path> paths = new ArrayList<>();
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (file.endsWith("jvm/Hello.xlass")) {
                    paths.add(file);
                    return FileVisitResult.TERMINATE;
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return paths;
    }
}
