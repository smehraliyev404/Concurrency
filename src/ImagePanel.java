import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

// the panel inside the window that shows the image.
// if the image is bigger than the screen it is drawn smaller
// but the image itself is not changed. processing still works on the original
public class ImagePanel extends JPanel {

    private static final int MARGIN = 60;

    private final BufferedImage image;
    private final int shownWidth;
    private final int shownHeight;

    public ImagePanel(BufferedImage image) {
        this.image = image;

        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        double scaleX = (double) (screen.width - MARGIN) / image.getWidth();
        double scaleY = (double) (screen.height - MARGIN) / image.getHeight();
        double scale = Math.min(1.0, Math.min(scaleX, scaleY));

        shownWidth = (int) (image.getWidth() * scale);
        shownHeight = (int) (image.getHeight() * scale);
        if (scale < 1.0) {
            System.out.println("Image is bigger than the screen, showing it at " + Math.round(scale * 100) + "%");
        }
        setPreferredSize(new Dimension(shownWidth, shownHeight));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // smoother result when the image is drawn smaller
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(image, 0, 0, shownWidth, shownHeight, null);
    }
}
