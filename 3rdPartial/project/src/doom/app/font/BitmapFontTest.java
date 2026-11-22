package doom.app.font;

import engine.core.Pixel;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Standalone test for {@link BitmapFont}.
 * <p>
 * Part 1 - automatic validation (printed as PASS / FAIL lines, no window needed).
 * Part 2 - a visual sheet for manual inspection: all 45 glyphs in boxed cells, the alphabet, digits
 * and punctuation, confusable pairs, and the required words at scales 1, 2, 3 and 4.
 * <p>
 * Every glyph pixel is written by {@code BitmapFont} through {@code Pixel.put}. The sheet's own
 * background, grid boxes and guide lines also use {@code Pixel.put}. {@code Graphics.drawImage}
 * is used only to show the finished BufferedImage in the Swing window.
 * <p>
 * Usage:
 * <pre>
 *   BitmapFontTest                  validate, then open the window
 *   BitmapFontTest --headless       validate only; exit code 0 = all passed, 1 = failures
 *   BitmapFontTest --png out.png    also save the sheet as a PNG (combine with --headless)
 * </pre>
 */
public final class BitmapFontTest {

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 900;

    private static final Color BG = new Color(16, 16, 24);
    private static final Color FG = Color.WHITE;
    private static final Color GRID = new Color(70, 70, 90);
    private static final Color GUIDE = new Color(120, 40, 40);
    private static final Color HEADING = new Color(255, 200, 60);

    private static int passed = 0;
    private static int failed = 0;

    // ------------------------------------------------------------------ entry point

    public static void main(String[] args) throws Exception {
        boolean headless = false;
        String pngPath = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--headless")) {
                headless = true;
            } else if (args[i].equals("--png") && i + 1 < args.length) {
                pngPath = args[++i];
            }
        }

        boolean ok = runValidation();

        BufferedImage sheet = buildSheet();
        if (pngPath != null) {
            ImageIO.write(sheet, "png", new File(pngPath));
            System.out.println("Saved sheet to " + pngPath);
        }

        System.out.println();
        System.out.println(ok ? "AUTOMATIC VALIDATION: ALL PASSED" : "AUTOMATIC VALIDATION: FAILURES (see FAIL lines)");
        System.out.println("Visual readability still requires manual inspection of the sheet.");

        if (headless) {
            System.exit(ok ? 0 : 1);
        } else {
            SwingUtilities.invokeLater(() -> showWindow(sheet));
        }
    }

    // ------------------------------------------------------------------ validation

    private static void check(boolean condition, String name, String detail) {
        if (condition) {
            passed++;
            System.out.println("PASS  " + name);
        } else {
            failed++;
            System.out.println("FAIL  " + name + (detail.isEmpty() ? "" : "  -> " + detail));
        }
    }

    private static boolean runValidation() {
        BitmapFont font = new BitmapFont();
        String supported = BitmapFont.SUPPORTED;

        // --- character set ---
        check(BitmapFont.CHAR_SET_SIZE == 45, "CHAR_SET_SIZE is 45", "was " + BitmapFont.CHAR_SET_SIZE);
        check(supported.length() == 45, "SUPPORTED lists exactly 45 characters", "was " + supported.length());

        StringBuilder dupChars = new StringBuilder();
        for (int i = 0; i < supported.length(); i++) {
            if (supported.indexOf(supported.charAt(i)) != i) {
                dupChars.append(supported.charAt(i));
            }
        }
        check(dupChars.length() == 0, "no character appears twice in SUPPORTED", "duplicates: " + dupChars);

        StringBuilder expected = new StringBuilder();
        for (char c = 'A'; c <= 'Z'; c++) expected.append(c);
        for (char c = '0'; c <= '9'; c++) expected.append(c);
        expected.append(" ,-'.!?:;");
        check(expected.toString().equals(supported), "SUPPORTED matches the required set (A-Z, 0-9, space, , - ' . ! ? : ;)", "");

        // --- index mapping ---
        StringBuilder badIndex = new StringBuilder();
        for (int i = 0; i < supported.length(); i++) {
            if (BitmapFont.glyphIndexOf(supported.charAt(i)) != i) {
                badIndex.append(supported.charAt(i));
            }
        }
        check(badIndex.length() == 0, "every character maps to its own glyph index 0..44", "bad: " + badIndex);

        // --- glyph structure: 16 rows, 8 bits per row ---
        StringBuilder badStructure = new StringBuilder();
        for (char c : supported.toCharArray()) {
            int[] rows = BitmapFont.glyphRows(c);
            if (rows == null || rows.length != BitmapFont.GLYPH_HEIGHT) {
                badStructure.append(c).append("(rows) ");
                continue;
            }
            for (int r = 0; r < rows.length; r++) {
                if (rows[r] < 0 || rows[r] > 0xFF) {
                    badStructure.append(c).append("(row ").append(r).append(" not 8-bit) ");
                }
            }
        }
        check(badStructure.length() == 0, "every glyph has exactly 16 rows, each a valid 8-bit value", badStructure.toString());

        // --- design rules: blank space, ink for everything else, margin column empty ---
        int[] space = BitmapFont.glyphRows(' ');
        check(inkOf(space) == 0, "space glyph is completely empty", "");

        StringBuilder sparse = new StringBuilder();
        StringBuilder marginInk = new StringBuilder();
        for (char c : supported.toCharArray()) {
            if (c == ' ') continue;
            int[] rows = BitmapFont.glyphRows(c);
            if (inkOf(rows) < 6) sparse.append(c);
            for (int row : rows) {
                if ((row & 1) != 0) { marginInk.append(c); break; }   // column 7 must stay empty
            }
        }
        check(sparse.length() == 0, "every non-space glyph has at least 6 lit pixels (not too sparse)", "sparse: " + sparse);
        check(marginInk.length() == 0, "column 7 (side bearing) is empty in every glyph", "ink in: " + marginInk);

        // --- duplicates: no two glyphs share a bitmap ---
        Map<String, Character> seen = new HashMap<>();
        StringBuilder dupGlyphs = new StringBuilder();
        for (char c : supported.toCharArray()) {
            String key = java.util.Arrays.toString(BitmapFont.glyphRows(c));
            Character other = seen.put(key, c);
            if (other != null) dupGlyphs.append(other).append('=').append(c).append(' ');
        }
        check(dupGlyphs.length() == 0, "no two glyphs have identical bitmaps", dupGlyphs.toString());

        // --- confusable pairs must differ clearly (Hamming distance in pixels) ---
        // Letters/digits: at least 8 pixels apart. Punctuation pairs that differ only by a tail
        // (":" vs ";" and "." vs ",") are allowed to be closer, but must still differ.
        String[] pairs = {"O0", "I1", "B8", "MW", "CG", "PR", "Z2", "S5", "DO", "UV", "EF", "QO", "HN", "XY", "G6", "I!"};
        String[] tailPairs = {":;", ".,"};
        final int minDistance = 8;
        final int minTailDistance = 3;
        StringBuilder closePairs = new StringBuilder();
        StringBuilder distances = new StringBuilder();
        for (String p : pairs) {
            int d = hamming(BitmapFont.glyphRows(p.charAt(0)), BitmapFont.glyphRows(p.charAt(1)));
            distances.append(p.charAt(0)).append('/').append(p.charAt(1)).append('=').append(d).append(' ');
            if (d < minDistance) closePairs.append(p).append('=').append(d).append(' ');
        }
        for (String p : tailPairs) {
            int d = hamming(BitmapFont.glyphRows(p.charAt(0)), BitmapFont.glyphRows(p.charAt(1)));
            distances.append(p.charAt(0)).append('/').append(p.charAt(1)).append('=').append(d).append(' ');
            if (d < minTailDistance) closePairs.append(p).append('=').append(d).append(' ');
        }
        check(closePairs.length() == 0, "confusable pairs differ by >= " + minDistance + " pixels (tail-only punctuation pairs >= " + minTailDistance + ")", closePairs.toString());
        System.out.println("      pair distances: " + distances);

        // --- unsupported characters: no glyph, nothing drawn, cursor still advances ---
        char[] unsupported = {'a', 'z', '@', '#', '"', '\u00e9', '\u0000', '\u4e2d'};
        StringBuilder wrongSupport = new StringBuilder();
        for (char c : unsupported) {
            if (BitmapFont.glyphRows(c) != null || BitmapFont.glyphIndexOf(c) != -1) wrongSupport.append((int) c).append(' ');
        }
        check(wrongSupport.length() == 0, "unsupported characters have no glyph (lowercase, @, non-ASCII...)", "wrongly supported: " + wrongSupport);

        // --- rendering: pixel-exact comparison against the stored bitmap, scales 1-4 ---
        StringBuilder renderErrors = new StringBuilder();
        for (int scale = 1; scale <= 4; scale++) {
            for (char c : supported.toCharArray()) {
                String err = verifyRender(font, c, scale);
                if (err != null) renderErrors.append(err).append(' ');
            }
        }
        check(renderErrors.length() == 0, "all 45 glyphs render pixel-exact at scales 1, 2, 3, 4 (via Pixel.put)", renderErrors.toString());

        // --- spacing / advance ---
        StringBuilder spacingErrors = new StringBuilder();
        for (int scale = 1; scale <= 4; scale++) {
            BufferedImage joined = blank(120 * scale, 20 * scale);
            font.renderString(joined, 0, 0, "A@A", FG, scale);
            BufferedImage manual = blank(120 * scale, 20 * scale);
            font.renderString(manual, 0, 0, "A", FG, scale);
            font.renderString(manual, 2 * (BitmapFont.GLYPH_WIDTH + 1) * scale, 0, "A", FG, scale);
            if (!sameImage(joined, manual)) spacingErrors.append("scale ").append(scale).append(' ');
        }
        check(spacingErrors.length() == 0, "unsupported char '@' draws nothing and advances one full cell (scales 1-4)", spacingErrors.toString());

        StringBuilder advanceErrors = new StringBuilder();
        for (int scale = 1; scale <= 4; scale++) {
            BufferedImage joined = blank(120 * scale, 20 * scale);
            font.renderString(joined, 0, 0, "AB", FG, scale);
            BufferedImage manual = blank(120 * scale, 20 * scale);
            font.renderString(manual, 0, 0, "A", FG, scale);
            font.renderString(manual, (BitmapFont.GLYPH_WIDTH + 1) * scale, 0, "B", FG, scale);
            if (!sameImage(joined, manual)) advanceErrors.append("scale ").append(scale).append(' ');
        }
        check(advanceErrors.length() == 0, "glyph advance is exactly (8 + 1) * scale pixels (scales 1-4)", advanceErrors.toString());

        // --- metrics / API behaviour ---
        check(font.getHeight() == 16, "getHeight() is 16", "was " + font.getHeight());
        check(font.getStringWidth("") == 0, "getStringWidth(\"\") is 0", "was " + font.getStringWidth(""));
        check(font.getStringWidth("A") == 8, "getStringWidth(\"A\") is 8", "was " + font.getStringWidth("A"));
        check(font.getStringWidth("PLAY") == 35, "getStringWidth(\"PLAY\") is 35", "was " + font.getStringWidth("PLAY"));

        boolean npeBuffer = throwsNpe(() -> font.renderString(null, 0, 0, "A", FG, 1));
        boolean npeColor = throwsNpe(() -> font.renderString(blank(10, 10), 0, 0, "A", null, 1));
        boolean npeText = throwsNpe(() -> font.renderString(blank(10, 10), 0, 0, null, FG, 1));
        boolean iaeScale = throwsIae(() -> font.renderString(blank(10, 10), 0, 0, "A", FG, 0));
        check(npeBuffer && npeColor && npeText && iaeScale, "null buffer/color/text -> NPE, scale 0 -> IllegalArgumentException", "");

        // --- clipping: drawing partly or fully outside the buffer must not throw ---
        boolean clipOk = true;
        try {
            BufferedImage small = blank(20, 20);
            font.renderString(small, -30, -30, "NIGHTMARE", FG, 4);
            font.renderString(small, 15, 15, "ULTRA-VIOLENCE", FG, 4);
        } catch (RuntimeException e) {
            clipOk = false;
        }
        check(clipOk, "rendering partly outside the buffer is clipped without exceptions", "");

        System.out.println();
        System.out.println("Passed: " + passed + "   Failed: " + failed);
        return failed == 0;
    }

    /** Renders one character at the given scale and compares every pixel with the stored bitmap. */
    private static String verifyRender(BitmapFont font, char c, int scale) {
        int pad = 3;
        int w = BitmapFont.GLYPH_WIDTH * scale + 2 * pad;
        int h = BitmapFont.GLYPH_HEIGHT * scale + 2 * pad;
        BufferedImage img = blank(w, h);
        font.renderString(img, pad, pad, String.valueOf(c), FG, scale);

        int[] rows = BitmapFont.glyphRows(c);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                boolean lit = (img.getRGB(x, y) & 0xFFFFFF) == 0xFFFFFF;
                int gx = x - pad;
                int gy = y - pad;
                boolean expectedLit = false;
                if (gx >= 0 && gy >= 0 && gx < BitmapFont.GLYPH_WIDTH * scale && gy < BitmapFont.GLYPH_HEIGHT * scale) {
                    int col = gx / scale;
                    int row = gy / scale;
                    expectedLit = ((rows[row] >>> (BitmapFont.GLYPH_WIDTH - 1 - col)) & 1) != 0;
                }
                if (lit != expectedLit) {
                    return "'" + c + "'@" + scale + "x(" + x + "," + y + ")";
                }
            }
        }
        return null;
    }

    private static int inkOf(int[] rows) {
        int n = 0;
        for (int r : rows) n += Integer.bitCount(r & 0xFF);
        return n;
    }

    private static int hamming(int[] a, int[] b) {
        int d = 0;
        for (int i = 0; i < a.length; i++) d += Integer.bitCount((a[i] ^ b[i]) & 0xFF);
        return d;
    }

    private static BufferedImage blank(int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Pixel.put(img, x, y, Color.BLACK);
            }
        }
        return img;
    }

    private static boolean sameImage(BufferedImage a, BufferedImage b) {
        if (a.getWidth() != b.getWidth() || a.getHeight() != b.getHeight()) return false;
        for (int y = 0; y < a.getHeight(); y++) {
            for (int x = 0; x < a.getWidth(); x++) {
                if (a.getRGB(x, y) != b.getRGB(x, y)) return false;
            }
        }
        return true;
    }

    private static boolean throwsNpe(Runnable r) {
        try { r.run(); return false; } catch (NullPointerException e) { return true; } catch (RuntimeException e) { return false; }
    }

    private static boolean throwsIae(Runnable r) {
        try { r.run(); return false; } catch (IllegalArgumentException e) { return true; } catch (RuntimeException e) { return false; }
    }

    // ------------------------------------------------------------------ visual sheet

    private static BufferedImage buildSheet() {
        BufferedImage sheet = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        fillRect(sheet, 0, 0, WIDTH, HEIGHT, BG);
        BitmapFont font = new BitmapFont();

        int x = 10;
        int y = 8;

        // 1. All 45 glyphs in boxed cells (scale 3). Box = the full 8x16 cell, so the empty margins
        //    and the red baseline (row 13) / cap line (row 2) show how each glyph sits in its cell.
        font.renderString(sheet, x, y, "ALL 45 GLYPHS - CELL VIEW (SCALE 3)", HEADING, 1);
        y += 20;
        String supported = BitmapFont.SUPPORTED;
        int scale = 3;
        int cellW = BitmapFont.GLYPH_WIDTH * scale;
        int cellH = BitmapFont.GLYPH_HEIGHT * scale;
        int gap = 8;
        int perRow = 15;
        for (int i = 0; i < supported.length(); i++) {
            int cx = x + (i % perRow) * (cellW + gap);
            int cy = y + (i / perRow) * (cellH + gap);
            drawBox(sheet, cx - 1, cy - 1, cellW + 2, cellH + 2, GRID);
            hLine(sheet, cx, cx + cellW - 1, cy + 2 * scale, GUIDE);            // top of caps (row 2)
            hLine(sheet, cx, cx + cellW - 1, cy + 14 * scale - 1, GUIDE);       // baseline (end of row 13)
            font.renderString(sheet, cx, cy, String.valueOf(supported.charAt(i)), FG, scale);
        }
        y += 3 * (cellH + gap) + 6;

        // 2. Plain strings at scale 2 (tests spacing between neighbours)
        font.renderString(sheet, x, y, "CONTIGUOUS TEXT (SCALE 2)", HEADING, 1);
        y += 20;
        font.renderString(sheet, x, y, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", FG, 2);
        y += 36;
        font.renderString(sheet, x, y, "0123456789 ,-'.!?:;", FG, 2);
        y += 40;

        // 3. Confusable pairs, large
        font.renderString(sheet, x, y, "CONFUSABLE PAIRS (SCALE 4): O0 I1 B8 MW CG PR Z2 S5", HEADING, 1);
        y += 20;
        font.renderString(sheet, x, y, "O0 I1 B8 MW CG PR Z2 S5", FG, 4);
        y += 72;

        // 4. Required words at scales 1-4
        for (int s = 1; s <= 4; s++) {
            font.renderString(sheet, x, y, "SCALE " + s, HEADING, 1);
            y += 18;
            font.renderString(sheet, x, y, "PLAY  EXIT  NIGHTMARE", FG, s);
            y += 16 * s + 4;
            font.renderString(sheet, x, y, "ULTRA-VIOLENCE", FG, s);
            y += 16 * s + 8;
        }

        // 5. Unsupported-character spacing (shows gaps where '@' is)
        if (y + 40 < HEIGHT) {
            font.renderString(sheet, x, y, "N@I@G@H@T@M@A@R@E (GAPS EXPECTED)", FG, 1);
        }
        return sheet;
    }

    private static void fillRect(BufferedImage img, int x, int y, int w, int h, Color c) {
        for (int yy = y; yy < y + h; yy++) {
            for (int xx = x; xx < x + w; xx++) {
                Pixel.put(img, xx, yy, c);
            }
        }
    }

    private static void hLine(BufferedImage img, int x0, int x1, int y, Color c) {
        for (int xx = x0; xx <= x1; xx++) Pixel.put(img, xx, y, c);
    }

    private static void drawBox(BufferedImage img, int x, int y, int w, int h, Color c) {
        for (int xx = x; xx < x + w; xx++) {
            Pixel.put(img, xx, y, c);
            Pixel.put(img, xx, y + h - 1, c);
        }
        for (int yy = y; yy < y + h; yy++) {
            Pixel.put(img, x, yy, c);
            Pixel.put(img, x + w - 1, yy, c);
        }
    }

    // ------------------------------------------------------------------ window

    private static void showWindow(BufferedImage sheet) {
        JFrame frame = new JFrame("Project DOOM - BitmapFont Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(sheet, 0, 0, null);   // presentation only; pixels were made with Pixel.put
            }
        };
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.setContentPane(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
