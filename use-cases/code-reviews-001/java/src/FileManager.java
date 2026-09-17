import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * File management utility for handling common file operations.
 */
public class FileManager {
    private final Path basePath;
    private final boolean createDirectories;
    private int bufferSize = 8192;
    private int filesProcessed;
    private final List<String> errors = new ArrayList<>();

    public FileManager(String basePath) {
        this(basePath, true);
    }

    public FileManager(String basePath, boolean createDirectories) {
        this.basePath = Paths.get(Objects.requireNonNull(basePath, "basePath"))
                .toAbsolutePath().normalize();
        this.createDirectories = createDirectories;
    }

    public boolean saveFile(String fileName, String content) {
        Path file = resolve(fileName);
        try {
            createParent(file);
            Files.writeString(file, Objects.requireNonNull(content, "content"));
            filesProcessed++;
            return true;
        } catch (IOException | RuntimeException e) {
            recordError("Error saving file " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    public String readFile(String fileName) {
        Path file = resolve(fileName);
        if (!Files.isRegularFile(file)) {
            recordError("File does not exist: " + fileName);
            return null;
        }
        try {
            String content = Files.readString(file);
            filesProcessed++;
            return content;
        } catch (IOException e) {
            recordError("Error reading file " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    public boolean deleteFile(String fileName) {
        Path file = resolve(fileName);
        try {
            if (!Files.deleteIfExists(file)) {
                recordError("Cannot delete - file does not exist: " + fileName);
                return false;
            }
            filesProcessed++;
            return true;
        } catch (IOException e) {
            recordError("Failed to delete file: " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean copyFile(String sourceFileName, String destFileName) {
        Path source = resolve(sourceFileName);
        Path destination = resolve(destFileName);
        if (!Files.isRegularFile(source)) {
            recordError("Source file does not exist: " + sourceFileName);
            return false;
        }
        try {
            createParent(destination);
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            filesProcessed++;
            return true;
        } catch (IOException e) {
            recordError("Error copying file: " + e.getMessage());
            return false;
        }
    }

    public boolean createZipArchive(String zipFileName, List<String> filesToInclude) {
        Path zip = resolve(zipFileName);
        try {
            createParent(zip);
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {
                byte[] buffer = new byte[bufferSize];
                for (String fileName : filesToInclude) {
                    Path file = resolve(fileName);
                    if (!Files.isRegularFile(file)) {
                        recordError("File does not exist, skipping: " + fileName);
                        continue;
                    }
                    zos.putNextEntry(new ZipEntry(fileName));
                    try (var input = Files.newInputStream(file)) {
                        int read;
                        while ((read = input.read(buffer)) != -1) {
                            zos.write(buffer, 0, read);
                        }
                    } finally {
                        zos.closeEntry();
                    }
                }
            }
            filesProcessed++;
            return true;
        } catch (IOException e) {
            recordError("Error creating ZIP archive " + zipFileName + ": " + e.getMessage());
            return false;
        }
    }

    public List<String> listFiles(String directoryPath) {
        Path directory = resolve(directoryPath);
        if (!Files.isDirectory(directory)) {
            recordError("Directory does not exist: " + directoryPath);
            return List.of();
        }
        try (var stream = Files.list(directory)) {
            return stream.map(path -> path.getFileName().toString()).toList();
        } catch (IOException e) {
            recordError("Error listing directory " + directoryPath + ": " + e.getMessage());
            return List.of();
        }
    }

    public int getFilesProcessed() {
        return filesProcessed;
    }

    public List<String> getErrors() {
        return List.copyOf(errors);
    }

    public void setBufferSize(int bufferSize) {
        if (bufferSize <= 0) {
            throw new IllegalArgumentException("bufferSize must be positive");
        }
        this.bufferSize = bufferSize;
    }

    private Path resolve(String child) {
        Path resolved = basePath.resolve(Objects.requireNonNull(child, "path")).normalize();
        if (!resolved.startsWith(basePath)) {
            throw new IllegalArgumentException("Path escapes base directory");
        }
        return resolved;
    }

    private void createParent(Path file) throws IOException {
        if (createDirectories && file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }
    }

    private void recordError(String message) {
        errors.add(message);
    }
}
