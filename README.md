# Concurrency, Image Pixelation

A java program that opens a jpg image and pixelates it square by square. It finds the average color of every square and paints the whole square with it, and you can watch it happen in a window. It runs either in a single thread or in several threads at the same time, and saves the result to `result.jpg`.

## Structure

```
Concurrency/
├── README.md
├── .gitignore
└── src/
    ├── Main.java // checks the arguments, loads the image, opens the window, runs the chosen mode, saves result.jpg
    ├── ImagePanel.java // panel in the window that draws the image (smaller if it does not fit the screen)
    └── Pixelator.java // the averaging itself, and splitting the work between threads in M mode
```

## Requirements

- Java (JDK) 17 or newer. No external libraries are used, only the standard JDK:
  - `javax.imageio.ImageIO` to read and write the jpg
  - `java.awt.image.BufferedImage` to get and set the pixels
  - Swing (`JFrame`, `JPanel`) for the window
  - `java.lang.Thread` for the threads

  The averaging of the squares is written by hand, no library method does that part.
  
- A jpg image of your choice. No image comes with the repository, any `.jpg` or `.jpeg` of any size works.

Since only the JDK is used, it runs the same on macOS and Windows. You can check your java version with `java -version`.

## How to run

From the root of the repository:

```
cd src
javac *.java
java Main <file.jpg> <square size> <S|M>
```

Example:

```
java Main ../photo.jpg 20 M
```

- `file.jpg` is the path to the image (`.jpg` or `.jpeg`). If the path has spaces in it, put it in quotes, for example `java Main "C:\Users\me\My Pictures\cat.jpg" 20 S`.
- `square size` is the side of one square in pixels, a whole number bigger than 0.
- `S` is single-threaded mode, `M` is multi-threaded mode (lowercase `s` and `m` work too).

The window shows the progress while the image is processed. When it is done, the time it took is printed and the result is saved as `result.jpg` in the folder you ran the command from. The window stays open so the result can be looked at, closing it ends the program.

If something is wrong with the arguments, the program prints one line about what is wrong and stops, for example:

```
Usage: java Main <file.jpg> <square size> <S|M>
Error: file not found: cat.jpg
Error: square size has to be a whole number: abc
Error: mode has to be S or M: X
```

## How it works

### Averaging one square

Every pixel is one int with red, green and blue packed inside, so the three values are taken out with bit shifts (`(rgb >> 16) & 0xFF` and so on). For one square, the red, green and blue values of all its pixels are added up and divided by the number of pixels. The sums are `long` so they cannot overflow on big squares. Then every pixel of the square is set to that average color.

The image is first copied into a plain `TYPE_INT_RGB` image, so every pixel is in the same format no matter how the jpg was saved.

### Edges

The width or height of the image is often not a multiple of the square size. In that case the last squares on the right and at the bottom are just smaller: their size is `Math.min(squareSize, end - x)`, and their average is taken over only the pixels they really have.

### S mode

`processRegion(startX, startY, endX, endY)` goes over a region square by square, left to right and top to bottom. In S mode the main thread calls it once for the whole image.

### M mode

The number of threads is the number of CPU cores, from `Runtime.getRuntime().availableProcessors()`. The image is split into that many horizontal strips and every thread runs the same `processRegion` on its own strip, so both modes use exactly the same procedure. The main thread waits for all of them with `join()` before saving.

The strips are cut on square borders (the square rows are divided between the threads, not the pixel rows). This way no square is ever shared between two threads, every thread only reads and writes its own pixels, and there is no need for locks. If the image has fewer square rows than cores, only as many threads as there are rows are started.

I checked that S and M give exactly the same result.jpg pixel by pixel, on many image sizes and square sizes, including square sizes where the image is not an exact multiple of the square size, so the last squares at the edges are smaller.

### Showing the progress

After every square, the thread calls `repaint()` on the panel and sleeps for 5 ms (`DELAY_MS` in `Pixelator.java`). Without the pause the whole image would be done before the window could show anything. `repaint()` is safe to call from any thread, Swing then draws the panel again on its own thread.

The pause can be changed in `Pixelator.java` (`private static final int DELAY_MS = 5;`). A bigger number makes the progress slower and easier to follow, a smaller one makes it faster, and 0 turns the pause off. After changing it, compile again with `javac *.java`.

The window is opened with `SwingUtilities.invokeAndWait` before the processing starts, so the progress is visible from the first square.

### Big images

If the image is bigger than the screen, `ImagePanel` works out a scale so that the image fits the screen, and only draws it smaller. The image itself is not resized, the processing and result.jpg are always on the original size.

## Results

On my laptop (Apple M3 Pro, 11 cores), with a 3001 x 2003 image and square size 50:

- S mode: 15.76 s
- M mode with 11 threads: 1.54 s

M is about 10 times faster here. To be honest, most of this time comes from the 5 ms pause after every square (this image has 2501 squares, so about 12.5 s of the S time is just sleeping), and in M mode the threads sleep at the same time. The averaging alone is fast, but it also runs in parallel on different cores in M mode. Setting `DELAY_MS` to 0 would show the pure processing time, but then the progress cannot be seen.