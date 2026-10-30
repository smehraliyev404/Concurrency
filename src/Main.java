import java.io.File;

public class Main {

    public static void main(String[] args) {
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
    }
}
