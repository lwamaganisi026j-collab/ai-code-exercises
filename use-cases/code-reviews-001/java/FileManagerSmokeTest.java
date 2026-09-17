import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class FileManagerSmokeTest {
    public static void main(String[] args) throws Exception {
        Path temp = Files.createTempDirectory("file-manager-test");
        FileManager manager = new FileManager(temp.toString());

        check(manager.saveFile("nested/input.txt", "hello"), "save failed");
        check("hello" + System.lineSeparator().equals(manager.readFile("nested/input.txt")), "read failed");
        check(manager.copyFile("nested/input.txt", "copy.txt"), "copy failed");
        check(manager.createZipArchive("archive.zip", List.of("nested/input.txt", "copy.txt")), "zip failed");
        check(Files.exists(temp.resolve("archive.zip")), "archive missing");
        check(manager.listFiles("nested").equals(List.of("input.txt")), "list failed");
        check(manager.deleteFile("copy.txt"), "delete failed");
        check(manager.getErrors().isEmpty(), "unexpected errors: " + manager.getErrors());

        try {
            manager.setBufferSize(0);
            throw new AssertionError("invalid buffer size was accepted");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        try {
            manager.readFile("../outside.txt");
            throw new AssertionError("path traversal was accepted");
        } catch (IllegalArgumentException expected) {
            // expected
        }

        System.out.println("FileManager smoke tests passed");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
