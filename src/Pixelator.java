import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

// does the averaging. processRegion is main function used
public class Pixelator {

    // small pause after every square so the progress can be seen in the window
    private static final int DELAY_MS = 1;

    private final BufferedImage image;
    private final int squareSize;
    private final JPanel panel;

    public Pixelator(BufferedImage image, int squareSize, JPanel panel) {
        this.image = image;
        this.squareSize = squareSize;
        this.panel = panel;
    }

    // goes over the region square by square, left to right and top to bottom
    public void processRegion(int startX, int startY, int endX, int endY) throws InterruptedException {
        for (int y = startY; y < endY; y += squareSize) {
            for (int x = startX; x < endX; x += squareSize) {
                int w = Math.min(squareSize, endX - x);
                int h = Math.min(squareSize, endY - y);
                averageSquare(x, y, w, h);
                panel.repaint();
                Thread.sleep(DELAY_MS);
            }
        }
    }

    // splits the image into horizontal strips, one per thread, and runs processRegion
    public void processMultiThreaded(int threadCount) throws InterruptedException {
        int squaresY = (image.getHeight() + squareSize - 1) / squareSize;
        List<Thread> threads = new ArrayList<>();

        for (int t = 0; t < threadCount; t++) {
            int startY = squaresY * t / threadCount * squareSize;
            int endY = Math.min(squaresY * (t + 1) / threadCount * squareSize, image.getHeight());

            if (startY >= endY) {
                continue;
            }

            Thread thread = new Thread(() -> {
                try {
                    processRegion(0, startY, image.getWidth(), endY);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(thread);
            thread.start();
        }
        System.out.println("Started " + threads.size() + " threads");

        // wait until every thread is finished
        for (Thread thread : threads) {
            thread.join();
        }
    }

    // finds the average color of the square and paints the whole square with it
    private void averageSquare(int x, int y, int w, int h) {
        long red = 0;
        long green = 0;
        long blue = 0;

        for (int i = y; i < y + h; i++) {
            for (int j = x; j < x + w; j++) {
                int rgb = image.getRGB(j, i);
                red += (rgb >> 16) & 0xFF;
                green += (rgb >> 8) & 0xFF;
                blue += rgb & 0xFF;
            }
        }

        long count = (long) w * h;
        int avgRed = (int) (red / count);
        int avgGreen = (int) (green / count);
        int avgBlue = (int) (blue / count);
        int avg = (avgRed << 16) | (avgGreen << 8) | avgBlue;

        for (int i = y; i < y + h; i++) {
            for (int j = x; j < x + w; j++) {
                image.setRGB(j, i, avg);
            }
        }
    }
}
