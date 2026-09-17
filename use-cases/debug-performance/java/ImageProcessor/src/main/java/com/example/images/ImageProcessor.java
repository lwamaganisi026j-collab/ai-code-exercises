package com.example.images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ImageProcessor {

    public static void main(String[] args) {
        try {
            multiplyImages("source_images", "sample_images", 100);
            processImageFolder("sample_images", "processed_images");
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
        }
    }

    public static void multiplyImages(String inputFolder, String outputFolder, int multiplicationFactor) throws IOException {
        if (multiplicationFactor < 0) {
            throw new IllegalArgumentException("multiplicationFactor must not be negative");
        }

        File folder = new File(inputFolder);
        File[] imageFiles = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
        });

        if (imageFiles == null || imageFiles.length == 0) {
            return;
        }

        File outputDir = new File(outputFolder);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Could not create output directory: " + outputFolder);
        }

        for (File imageFile : imageFiles) {
            String fileName = imageFile.getName();
            int dot = fileName.lastIndexOf('.');
            String baseName = dot > 0 ? fileName.substring(0, dot) : fileName;
            String extension = dot > 0 ? fileName.substring(dot) : "";

            for (int i = 1; i <= multiplicationFactor; i++) {
                File outputFile = new File(outputDir, String.format("%s_%d%s", baseName, i, extension));
                Files.copy(imageFile.toPath(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    /**
     * Processes one image at a time so the input and processed batches are not
     * retained simultaneously. This bounds memory usage by approximately one
     * source image plus one processed image.
     */
    public static void processImageFolder(String inputFolder, String outputFolder) throws IOException {
        File inputDir = new File(inputFolder);
        if (!inputDir.isDirectory()) {
            throw new IOException("Input folder does not exist: " + inputFolder);
        }

        File outputDir = new File(outputFolder);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            throw new IOException("Could not create output folder: " + outputFolder);
        }

        File[] imageFiles = inputDir.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
        });

        if (imageFiles == null) {
            return;
        }

        for (File input : imageFiles) {
            BufferedImage original = ImageIO.read(input);
            if (original == null) {
                continue;
            }

            BufferedImage processed = applyEffects(original);
            try {
                File output = new File(outputDir, "processed_" + input.getName());
                ImageIO.write(processed, getImageFormat(input.getName()), output);
            } finally {
                processed.flush();
                original.flush();
            }
        }
    }

    private static BufferedImage applyEffects(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();
        BufferedImage processed = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = original.getRGB(x, y);
                int red = (rgb >>> 16) & 0xff;
                int green = (rgb >>> 8) & 0xff;
                int blue = rgb & 0xff;
                int gray = (red + green + blue) / 3;
                int grayRgb = (gray << 16) | (gray << 8) | gray;
                processed.setRGB(x, y, grayRgb);
            }
        }
        return processed;
    }

    private static String getImageFormat(String filename) {
        return filename.toLowerCase().endsWith(".png") ? "png" : "jpeg";
    }
}
