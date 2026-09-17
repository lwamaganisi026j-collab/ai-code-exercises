import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileManager {
    private final Path basePath;
    private final boolean createDirectories;
    private int bufferSize = 4096;
    private int filesProcessed;
    private final List<String> errors = new ArrayList<>();

    public FileManager(String basePath) { this(basePath, true); }

    public FileManager(String basePath, boolean createDirectories) {
        this.basePath = Paths.get(Objects.requireNonNull(basePath, "basePath must not be null"))
                .toAbsolutePath().normalize();
        this.createDirectories = createDirectories;
    }

    public boolean saveFile(String fileName, String content) {
        Objects.requireNonNull(content, "content must not be null");
        try {
            Path file = resolve(fileName);
            createParentDirectories(file);
            try (BufferedWriter writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
                writer.write(content);
            }
            filesProcessed++;
            return true;
        } catch (IOException | IllegalArgumentException e) {
            recordError("Error saving file " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    public String readFile(String fileName) {
        try {
            Path file = resolve(fileName);
            if (!Files.isRegularFile(file)) {
                recordError("File does not exist: " + fileName);
                return null;
            }
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) content.append(line).append(System.lineSeparator());
            }
            filesProcessed++;
            return content.toString();
        } catch (IOException | IllegalArgumentException e) {
            recordError("Error reading file " + fileName + ": " + e.getMessage());
            return null;
        }
    }

    public boolean deleteFile(String fileName) {
        try {
            Path file = resolve(fileName);
            if (!Files.isRegularFile(file)) {
                recordError("Cannot delete - file does not exist: " + fileName);
                return false;
            }
            boolean deleted = Files.deleteIfExists(file);
            if (deleted) filesProcessed++;
            return deleted;
        } catch (IOException | IllegalArgumentException e) {
            recordError("Error deleting file " + fileName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean copyFile(String sourceFileName, String destFileName) {
        try {
            Path source = resolve(sourceFileName);
            Path destination = resolve(destFileName);
            if (!Files.isRegularFile(source)) {
                recordError("Source file does not exist: " + sourceFileName);
                return false;
            }
            createParentDirectories(destination);
            Files.copy(source, destination, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            filesProcessed++;
            return true;
        } catch (IOException | IllegalArgumentException e) {
            recordError("Error copying file from " + sourceFileName + " to " + destFileName + ": " + e.getMessage());
            return false;
        }
    }

    public boolean createZipArchive(String zipFileName, List<String> filesToInclude) {
        Objects.requireNonNull(filesToInclude, "filesToInclude must not be null");
        try {
            Path archive = resolve(zipFileName);
            createParentDirectories(archive);
            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(archive,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE))) {
                byte[] buffer = new byte[bufferSize];
                for (String fileName : filesToInclude) {
                    Path file = resolve(fileName);
                    if (!Files.isRegularFile(file)) {
                        recordError("File does not exist, skipping: " + fileName);
                        continue;
                    }
                    zos.putNextEntry(new ZipEntry(fileName.replace('\\', '/')));
                    try (var input = Files.newInputStream(file)) {
                        int length;
                        while ((length = input.read(buffer)) != -1) zos.write(buffer, 0, length);
                    } finally {
                        zos.closeEntry();
                    }
                }
            }
            filesProcessed++;
            return true;
        } catch (IOException | IllegalArgumentException e) {
            recordError("Error creating ZIP archive " + zipFileName + ": " + e.getMessage());
            return false;
        }
    }

    public List<String> listFiles(String directoryPath) {
        try {
            Path directory = resolve(directoryPath);
            if (!Files.isDirectory(directory)) {
                recordError("Directory does not exist: " + directoryPath);
                return List.of();
            }
            try (var stream = Files.list(directory)) {
                return stream.filter(Files::isRegularFile).map(path -> path.getFileName().toString()).sorted().toList();
            }
        } catch (IOException | IllegalArgumentException e) {
            recordError("Error listing directory " + directoryPath + ": " + e.getMessage());
            return List.of();
        }
    }

    public int getFilesProcessed() { return filesProcessed; }
    public List<String> getErrors() { return List.copyOf(errors); }

    public void setBufferSize(int bufferSize) {
        if (bufferSize <= 0) throw new IllegalArgumentException("bufferSize must be greater than zero");
        this.bufferSize = bufferSize;
    }

    private Path resolve(String fileName) {
        Objects.requireNonNull(fileName, "fileName must not be null");
        Path resolved = basePath.resolve(fileName).normalize();
        if (!resolved.startsWith(basePath)) throw new IllegalArgumentException("Path escapes the configured base directory");
        return resolved;
    }

    private void createParentDirectories(Path path) throws IOException {
        if (createDirectories && path.getParent() != null) Files.createDirectories(path.getParent());
    }

    private void recordError(String message) { errors.add(message); }
}
