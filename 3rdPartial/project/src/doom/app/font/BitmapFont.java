package doom.app.font;

import engine.core.Pixel;

import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 * Reusable bitmap font for rendering menu text through the existing pixel-writing mechanism.
 * <p>
 * Glyphs are 8 pixels wide by 16 pixels tall, stored as 16 row-bytes per character.
 * Bit convention: bit 7 represents the leftmost pixel, bit 0 the rightmost.
 * Unsupported characters advance the cursor without drawing.
 * All rendering uses {@link Pixel#put} - no Java2D text APIs are used.
 * <p>
 * Glyph layout (shared by every letter and digit):
 * <ul>
 *   <li>Rows 0-1 and 14-15 are empty (headroom / descender space).</li>
 *   <li>Caps and digits occupy rows 2-13 (12 rows tall).</li>
 *   <li>Columns 0-6 hold the shape; column 7 is always empty (built-in side bearing).</li>
 *   <li>Strokes are 2 pixels thick.</li>
 * </ul>
 * Glyphs are authored as readable '#' / '.' pictures (see {@link #define}) and converted to
 * bytes at class-load time. The loader validates every glyph (row width, row count, duplicates,
 * index order) and fails immediately with a clear message instead of corrupting the font.
 */
public final class BitmapFont {

    /** Width of one glyph in pixels. */
    public static final int GLYPH_WIDTH = 8;

    /** Height of one glyph in pixels. */
    public static final int GLYPH_HEIGHT = 16;

    /** Number of supported characters in the font. */
    public static final int CHAR_SET_SIZE = 45;

    /**
     * The supported characters, in glyph-index order:
     * A-Z (0-25), 0-9 (26-35), space (36), then , - ' . ! ? : ; (37-44).
     */
    static final String SUPPORTED = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 ,-'.!?:;";

    /** Maps ASCII character (0-127) to glyph index, or -1 if unsupported. */
    private static final int[] CHAR_TO_GLYPH = new int[128];

    /** Glyph bitmaps: 16 row-bytes per character, bit 7 = leftmost pixel. */
    private static final int[][] GLYPHS = new int[CHAR_SET_SIZE][GLYPH_HEIGHT];

    /** Next free glyph index while the static initializer runs. */
    private static int nextIndex = 0;

    static {
        for (int i = 0; i < CHAR_TO_GLYPH.length; i++) {
            CHAR_TO_GLYPH[i] = -1;
        }

        // ---------- A-Z (indices 0-25), body rows 2-13 ----------
        define('A', 2,
                "..###...",
                ".#####..",
                "##...##.",
                "##...##.",
                "##...##.",
                "#######.",
                "#######.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.");
        define('B', 2,
                "######..",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "######..",
                "######..",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "######..");
        define('C', 2,
                ".#####..",
                "##...##.",
                "##......",
                "##......",
                "##......",
                "##......",
                "##......",
                "##......",
                "##......",
                "##......",
                "##...##.",
                ".#####..");
        define('D', 2,
                "#####...",
                "##..##..",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##..##..",
                "#####...");
        define('E', 2,
                "#######.",
                "#######.",
                "##......",
                "##......",
                "##......",
                "#####...",
                "#####...",
                "##......",
                "##......",
                "##......",
                "#######.",
                "#######.");
        define('F', 2,
                "#######.",
                "#######.",
                "##......",
                "##......",
                "##......",
                "#####...",
                "#####...",
                "##......",
                "##......",
                "##......",
                "##......",
                "##......");
        define('G', 2,
                ".#####..",
                "##...##.",
                "##......",
                "##......",
                "##......",
                "##..###.",
                "##..###.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                ".#####..");
        define('H', 2,
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "#######.",
                "#######.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.");
        define('I', 2,
                "#######.",
                "#######.",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "#######.",
                "#######.");
        define('J', 2,
                "....###.",
                "....###.",
                "....###.",
                "....###.",
                "....###.",
                "....###.",
                "....###.",
                "##..###.",
                "##..###.",
                "##..###.",
                ".#####..",
                "..###...");
        define('K', 2,
                "##...##.",
                "##..##..",
                "##.##...",
                "####....",
                "###.....",
                "###.....",
                "###.....",
                "###.....",
                "####....",
                "##.##...",
                "##..##..",
                "##...##.");
        define('L', 2,
                "##......",
                "##......",
                "##......",
                "##......",
                "##......",
                "##......",
                "##......",
                "##......",
                "##......",
                "##......",
                "#######.",
                "#######.");
        define('M', 2,
                "##...##.",
                "###.###.",
                "#######.",
                "#######.",
                "##.#.##.",
                "##.#.##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.");
        define('N', 2,
                "##...##.",
                "###..##.",
                "###..##.",
                "####.##.",
                "####.##.",
                "##.####.",
                "##.####.",
                "##..###.",
                "##..###.",
                "##...##.",
                "##...##.",
                "##...##.");
        define('O', 2,
                ".#####..",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                ".#####..");
        define('P', 2,
                "######..",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "######..",
                "######..",
                "##......",
                "##......",
                "##......",
                "##......",
                "##......");
        define('Q', 2,
                ".#####..",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##.#.##.",
                "##..###.",
                ".#####..",
                "....###.");
        define('R', 2,
                "######..",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "######..",
                "######..",
                "##.##...",
                "##..##..",
                "##...##.",
                "##...##.",
                "##...##.");
        define('S', 2,
                ".#####..",
                "##...##.",
                "##......",
                "##......",
                ".####...",
                "..####..",
                "....###.",
                ".....##.",
                ".....##.",
                "##...##.",
                "##...##.",
                ".#####..");
        define('T', 2,
                "#######.",
                "#######.",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...");
        define('U', 2,
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                ".#####..");
        define('V', 2,
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                ".##.##..",
                ".##.##..",
                ".#####..",
                "..###...",
                "..###...",
                "...#....");
        define('W', 2,
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##.#.##.",
                "##.#.##.",
                "#######.",
                "#######.",
                "###.###.");
        define('X', 2,
                "##...##.",
                "##...##.",
                ".##.##..",
                ".##.##..",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                ".##.##..",
                ".##.##..",
                "##...##.",
                "##...##.");
        define('Y', 2,
                "##...##.",
                "##...##.",
                ".##.##..",
                ".##.##..",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...");
        define('Z', 2,
                "#######.",
                "#######.",
                ".....##.",
                "....##..",
                "....##..",
                "...##...",
                "..##....",
                "..##....",
                ".##.....",
                "##......",
                "#######.",
                "#######.");

        // ---------- 0-9 (indices 26-35), body rows 2-13 ----------
        // 0 has a long vertical slit so it can never be mistaken for O.
        define('0', 2,
                ".#####..",
                "##...##.",
                "##.#.##.",
                "##.#.##.",
                "##.#.##.",
                "##.#.##.",
                "##.#.##.",
                "##.#.##.",
                "##.#.##.",
                "##.#.##.",
                "##...##.",
                ".#####..");
        // 1 has a slanted flag at the top; I has serifs at both ends.
        define('1', 2,
                "...###..",
                "..####..",
                ".#####..",
                "##.###..",
                "...###..",
                "...###..",
                "...###..",
                "...###..",
                "...###..",
                "...###..",
                "#######.",
                "#######.");
        define('2', 2,
                ".#####..",
                "##...##.",
                "##...##.",
                ".....##.",
                "....##..",
                "...##...",
                "..##....",
                ".##.....",
                "##......",
                "##......",
                "#######.",
                "#######.");
        define('3', 2,
                ".#####..",
                "##...##.",
                ".....##.",
                ".....##.",
                "..####..",
                "..####..",
                ".....##.",
                ".....##.",
                ".....##.",
                "##...##.",
                "##...##.",
                ".#####..");
        define('4', 2,
                "....###.",
                "...####.",
                "..#####.",
                ".##.###.",
                "##..###.",
                "##..###.",
                "#######.",
                "#######.",
                "....###.",
                "....###.",
                "....###.",
                "....###.");
        define('5', 2,
                "#######.",
                "#######.",
                "##......",
                "##......",
                "######..",
                "#######.",
                ".....##.",
                ".....##.",
                ".....##.",
                "##...##.",
                "##...##.",
                ".#####..");
        define('6', 2,
                "..####..",
                ".##.....",
                "##......",
                "##......",
                "######..",
                "#######.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                ".#####..");
        define('7', 2,
                "#######.",
                "#######.",
                ".....##.",
                ".....##.",
                "....##..",
                "....##..",
                "...##...",
                "...##...",
                "..##....",
                "..##....",
                "..##....",
                "..##....");
        // 8 has rounded corners and a pinched waist (hourglass); B has a flat left edge and a wide waist.
        define('8', 2,
                ".#####..",
                "##...##.",
                "##...##.",
                "##...##.",
                ".#####..",
                "..###...",
                "..###...",
                ".#####..",
                "##...##.",
                "##...##.",
                "##...##.",
                ".#####..");
        define('9', 2,
                ".#####..",
                "##...##.",
                "##...##.",
                "##...##.",
                "##...##.",
                "#######.",
                ".######.",
                ".....##.",
                ".....##.",
                ".....##.",
                "##...##.",
                ".#####..");

        // ---------- Space (index 36) ----------
        define(' ', 0, "........");

        // ---------- Punctuation (indices 37-44) ----------
        // Baseline is row 13. Comma and semicolon tails descend into rows 14-15.
        define(',', 11,
                "..###...",
                "..###...",
                "..##....",
                ".##.....");
        define('-', 7,
                "#######.",
                "#######.");
        define('\'', 2,
                "..###...",
                "..###...",
                "..##....",
                ".##.....");
        define('.', 11,
                "..###...",
                "..###...",
                "..###...");
        define('!', 2,
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "..###...",
                "........",
                "..###...",
                "..###...",
                "..###...");
        define('?', 2,
                ".#####..",
                "##...##.",
                ".....##.",
                ".....##.",
                "....##..",
                "...##...",
                "..###...",
                "..###...",
                "........",
                "..###...",
                "..###...",
                "..###...");
        define(':', 5,
                "..###...",
                "..###...",
                "..###...",
                "........",
                "........",
                "........",
                "..###...",
                "..###...",
                "..###...");
        define(';', 5,
                "..###...",
                "..###...",
                "..###...",
                "........",
                "........",
                "........",
                "..###...",
                "..###...",
                "..##....",
                ".##.....");

        if (nextIndex != CHAR_SET_SIZE) {
            throw new IllegalStateException("BitmapFont: defined " + nextIndex
                    + " glyphs, expected " + CHAR_SET_SIZE);
        }
    }

    /**
     * Registers one glyph from a '#'/'.' picture.
     * <p>
     * Validation (fails fast with the offending character in the message):
     * <ul>
     *   <li>glyphs must be defined in {@link #SUPPORTED} order (so indices are predictable),</li>
     *   <li>a character may be defined only once,</li>
     *   <li>every row must be exactly {@value #GLYPH_WIDTH} characters of '#' or '.',</li>
     *   <li>the picture must fit inside the 16-row cell at {@code topRow}.</li>
     * </ul>
     * Rows above {@code topRow} and below the picture are left empty, so every glyph always
     * ends up with exactly {@value #GLYPH_HEIGHT} row-bytes.
     *
     * @param c      the character this glyph represents
     * @param topRow the cell row (0-15) where the first picture row is placed
     * @param rows   the picture rows, each 8 characters of '#' (pixel on) or '.' (pixel off)
     */
    private static void define(char c, int topRow, String... rows) {
        if (nextIndex >= CHAR_SET_SIZE) {
            throw new IllegalStateException("BitmapFont: too many glyphs, extra '" + c + "'");
        }
        if (SUPPORTED.indexOf(c) != nextIndex) {
            throw new IllegalStateException("BitmapFont: '" + c + "' defined out of order (index "
                    + nextIndex + ", expected " + SUPPORTED.indexOf(c) + ")");
        }
        if (c >= CHAR_TO_GLYPH.length || CHAR_TO_GLYPH[c] != -1) {
            throw new IllegalStateException("BitmapFont: duplicate or non-ASCII glyph '" + c + "'");
        }
        if (topRow < 0 || topRow + rows.length > GLYPH_HEIGHT) {
            throw new IllegalStateException("BitmapFont: glyph '" + c + "' does not fit: topRow="
                    + topRow + ", rows=" + rows.length);
        }
        int[] glyph = GLYPHS[nextIndex];
        for (int r = 0; r < rows.length; r++) {
            String line = rows[r];
            if (line.length() != GLYPH_WIDTH) {
                throw new IllegalStateException("BitmapFont: glyph '" + c + "' picture row " + r
                        + " has " + line.length() + " chars, expected " + GLYPH_WIDTH);
            }
            int bits = 0;
            for (int col = 0; col < GLYPH_WIDTH; col++) {
                char p = line.charAt(col);
                if (p != '#' && p != '.') {
                    throw new IllegalStateException("BitmapFont: glyph '" + c + "' picture row " + r
                            + " has illegal char '" + p + "'");
                }
                if (p == '#') {
                    bits |= 1 << (GLYPH_WIDTH - 1 - col); // leftmost pixel = bit 7
                }
            }
            glyph[topRow + r] = bits;
        }
        CHAR_TO_GLYPH[c] = nextIndex;
        nextIndex++;
    }

    /**
     * Default constructor (public for reflection / testing).
     */
    public BitmapFont() {
    }

    /**
     * Renders the given text string into the specified buffer at the origin point,
     * using the configured color and integer scale.
     * <p>
     * Only pixels corresponding to on-bits in the glyph bitmap are written.
     * Unsupported characters advance the cursor horizontally by one glyph width
     * plus one-pixel spacing, without drawing any pixels.
     * <p>
     * The origin (x, y) represents the top-left corner of the first glyph cell.
     * Subsequent glyphs are placed to the right, with one unscaled pixel of
     * horizontal spacing between them. Scaling expands each on-pixel into a
     * scale x scale square.
     *
     * @param buffer the BufferedImage buffer to render into (must not be null)
     * @param x      the starting x-coordinate in the buffer
     * @param y      the starting y-coordinate in the buffer
     * @param text   the text string to render (must not be null)
     * @param color  the color to use for on-pixels (must not be null)
     * @param scale  the positive integer scaling factor (1, 2, 3, ...)
     * @throws NullPointerException     if buffer, text or color is null
     * @throws IllegalArgumentException if scale is not a positive integer
     */
    public void renderString(BufferedImage buffer, int x, int y,
                             String text, Color color, int scale) {
        if (buffer == null) {
            throw new NullPointerException("buffer must not be null");
        }
        if (text == null) {
            throw new NullPointerException("text must not be null");
        }
        if (color == null) {
            throw new NullPointerException("color must not be null");
        }
        if (scale <= 0) {
            throw new IllegalArgumentException("scale must be a positive integer, got: " + scale);
        }

        for (int i = 0; i < text.length(); i++) {
            int gi = charIndex(text.charAt(i));
            if (gi >= 0) {
                drawGlyph(buffer, x, y, GLYPHS[gi], color, scale);
            }
            // Advance: glyph width + 1-pixel spacing, scaled. Also done for unsupported chars.
            x += (GLYPH_WIDTH + 1) * scale;
        }
    }

    /** Draws one glyph with its top-left cell corner at (x, y). Every pixel goes through Pixel.put. */
    private static void drawGlyph(BufferedImage buffer, int x, int y,
                                  int[] glyph, Color color, int scale) {
        for (int row = 0; row < GLYPH_HEIGHT; row++) {
            int rowBits = glyph[row];
            if (rowBits == 0) {
                continue;
            }
            for (int col = 0; col < GLYPH_WIDTH; col++) {
                // Bit 7 = leftmost pixel, bit 0 = rightmost
                if (((rowBits >>> (GLYPH_WIDTH - 1 - col)) & 1) == 0) {
                    continue;
                }
                for (int sy = 0; sy < scale; sy++) {
                    for (int sx = 0; sx < scale; sx++) {
                        Pixel.put(buffer, x + col * scale + sx, y + row * scale + sy, color);
                    }
                }
            }
        }
    }

    /**
     * Returns the glyph index for the given character, or -1 if unsupported.
     *
     * @param c the character to look up
     * @return glyph index in range 0..44, or -1 if unsupported
     */
    private static int charIndex(char c) {
        return (c < CHAR_TO_GLYPH.length) ? CHAR_TO_GLYPH[c] : -1;
    }

    /**
     * Returns the width in unscaled pixels of the given text string.
     * <p>
     * Each character contributes {@code GLYPH_WIDTH + 1} pixels (glyph + spacing),
     * except that the final character does not add the trailing spacing pixel.
     * An empty string has width 0.
     *
     * @param text the text string; may be empty
     * @return the width in unscaled pixels
     * @throws NullPointerException if text is null
     */
    public int getStringWidth(String text) {
        if (text == null) {
            throw new NullPointerException("text must not be null");
        }
        if (text.isEmpty()) {
            return 0;
        }
        return (GLYPH_WIDTH + 1) * text.length() - 1;
    }

    /**
     * Returns the height of one glyph in unscaled pixels.
     *
     * @return the glyph height (always {@link #GLYPH_HEIGHT})
     */
    public int getHeight() {
        return GLYPH_HEIGHT;
    }

    /**
     * Test hook (package-private, not part of the public API): returns a copy of the 16 row-bytes
     * for {@code c}, or {@code null} if {@code c} is unsupported.
     */
    static int[] glyphRows(char c) {
        int gi = charIndex(c);
        return (gi < 0) ? null : GLYPHS[gi].clone();
    }

    /** Test hook (package-private): glyph index for {@code c}, or -1 if unsupported. */
    static int glyphIndexOf(char c) {
        return charIndex(c);
    }
}
