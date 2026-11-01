import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {

    private static ImagePanel panel;

    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.out.println("Usage: java Main <file.jpg> <square size> <S|M>");
            return;
        }

        String fileName = args[0];
        File file = new File(fileName);
        if (!file.isFile()) {
            System.out.println("Error: file not found: " + fileName);
            return;
        }
        String lower = fileName.toLowerCase();
        if (!lower.endsWith(".jpg") && !lower.endsWith(".jpeg")) {
            System.out.println("Error: the file has to be a jpg: " + fileName);
            return;
        }

        int squareSize;
        try {
            squareSize = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Error: square size has to be a whole number: " + args[1]);
            return;
        }
        if (squareSize <= 0) {
            System.out.println("Error: square size has to be bigger than 0");
            return;
        }

        String mode = args[2].toUpperCase();
        if (!mode.equals("S") && !mode.equals("M")) {
            System.out.println("Error: mode has to be S or M: " + args[2]);
            return;
        }

        System.out.println("File: " + fileName);
        System.out.println("Square size: " + squareSize);
        System.out.println("Mode: " + (mode.equals("S") ? "single-threaded" : "multi-threaded"));

        BufferedImage image;
        try {
            image = loadImage(file);
        } catch (IOException e) {
            System.out.println("Error: could not read the image: " + e.getMessage());
            return;
        }
        if (image == null) {
            System.out.println("Error: could not read the image: " + fileName);
            return;
        }
        System.out.println("Image size: " + image.getWidth() + " x " + image.getHeight());

        SwingUtilities.invokeAndWait(() -> showWindow(image, fileName));

        Pixelator pixelator = new Pixelator(image, squareSize, panel);
        if (mode.equals("S")) {
            pixelator.processRegion(0, 0, image.getWidth(), image.getHeight());
        } else {
            System.out.println("Multi-threaded mode is not done yet, running single-threaded");
            pixelator.processRegion(0, 0, image.getWidth(), image.getHeight());
        }

        File result = new File("result.jpg");
        try {
            ImageIO.write(image, "jpg", result);
        } catch (IOException e) {
            System.out.println("Error: could not save the result: " + e.getMessage());
            return;
        }
        System.out.println("Done, saved to " + result.getAbsolutePath());
    }

    // reads the jpg and copies it into a plain rgb image, so every pixel is one int in the same format
    private static BufferedImage loadImage(File file) throws IOException {
        BufferedImage original = ImageIO.read(file);
        if (original == null) {
            return null;
        }
        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        image.getGraphics().drawImage(original, 0, 0, null);
        return image;
    }

    private static void showWindow(BufferedImage image, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        panel = new ImagePanel(image);
        frame.add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
