import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RasterApp extends JFrame {

    private static class Pixel {
        final int x;
        final int y;

        Pixel(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private JComboBox<String> algoCombo;
    private JLabel lblX1, lblY1, lblX2, lblY2;
    private JTextField entryX1, entryY1, entryX2, entryY2;
    private JSlider scaleSlider;
    private JLabel infoLabel;
    private RasterCanvas canvas;

    public RasterApp() {
        setTitle("Лаба №3");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(new EmptyBorder(10, 10, 5, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        gbc.gridx = 0;
        controlPanel.add(new JLabel("Алгоритм:"), gbc);

        String[] algos = {
                "Пошаговый",
                "ЦДА",
                "Брезенхем (отрезок)",
                "Брезенхем (окружность)"
        };
        algoCombo = new JComboBox<>(algos);
        algoCombo.setSelectedItem("Пошаговый");
        gbc.gridx = 1;
        controlPanel.add(algoCombo, gbc);

        lblX1 = new JLabel("X1:");
        gbc.gridx = 2;
        controlPanel.add(lblX1, gbc);

        entryX1 = new JTextField("-7", 5);
        gbc.gridx = 3;
        controlPanel.add(entryX1, gbc);

        lblY1 = new JLabel("Y1:");
        gbc.gridx = 4;
        controlPanel.add(lblY1, gbc);

        entryY1 = new JTextField("-3", 5);
        gbc.gridx = 5;
        controlPanel.add(entryY1, gbc);

        lblX2 = new JLabel("X2:");
        gbc.gridx = 6;
        controlPanel.add(lblX2, gbc);

        entryX2 = new JTextField("6", 5);
        gbc.gridx = 7;
        controlPanel.add(entryX2, gbc);

        lblY2 = new JLabel("Y2:");
        gbc.gridx = 8;
        controlPanel.add(lblY2, gbc);

        entryY2 = new JTextField("5", 5);
        gbc.gridx = 9;
        controlPanel.add(entryY2, gbc);

        JButton drawBtn = new JButton("Отрисовать");
        gbc.gridx = 10;
        controlPanel.add(drawBtn, gbc);

        gbc.gridy = 1;
        gbc.gridx = 0;
        controlPanel.add(new JLabel("Клетка (px):"), gbc);

        scaleSlider = new JSlider(10, 50, 25);
        gbc.gridx = 1;
        controlPanel.add(scaleSlider, gbc);

        infoLabel = new JLabel("Время работы: -  Пикселей: -");
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        gbc.gridx = 2;
        gbc.gridwidth = 9;
        controlPanel.add(infoLabel, gbc);
        gbc.gridwidth = 1;

        add(controlPanel, BorderLayout.NORTH);

        
        canvas = new RasterCanvas();
        JPanel canvasWrapper = new JPanel(new BorderLayout());
        canvasWrapper.setBorder(new EmptyBorder(5, 10, 10, 10));
        canvasWrapper.add(canvas, BorderLayout.CENTER);
        add(canvasWrapper, BorderLayout.CENTER);

        algoCombo.addActionListener(e -> onAlgoChange());
        drawBtn.addActionListener(e -> drawShape());
        scaleSlider.addChangeListener(e -> drawShape());

        drawShape();
    }

    private void onAlgoChange() {
        String selected = (String) algoCombo.getSelectedItem();
        if ("Брезенхем (окружность)".equals(selected)) {
            lblX1.setText("Xc:");
            lblY1.setText("Yc:");
            lblX2.setText("R:");
            lblY2.setVisible(false);
            entryY2.setVisible(false);
        } else {
            lblX1.setText("X1:");
            lblY1.setText("Y1:");
            lblX2.setText("X2:");
            lblY2.setVisible(true);
            entryY2.setVisible(true);
        }
        drawShape();
    }

    private Integer safeGet(JTextField field) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    //  Пошаговый алгоритм
    private List<Pixel> stepByStep(int x1, int y1, int x2, int y2) {
        List<Pixel> pixels = new ArrayList<>();
        int dx = x2 - x1;
        int dy = y2 - y1;

        if (Math.abs(dx) >= Math.abs(dy)) {
            if (dx == 0) {
                pixels.add(new Pixel(x1, y1));
                return pixels;
            }
            double k = (double) dy / dx;
            double b = y1 - k * x1;
            int step = x2 >= x1 ? 1 : -1;
            for (int x = x1; x != x2 + step; x += step) {
                pixels.add(new Pixel(x, (int) Math.round(k * x + b)));
            }
        } else {
            double k = (double) dx / dy;
            double b = x1 - k * y1;
            int step = y2 >= y1 ? 1 : -1;
            for (int y = y1; y != y2 + step; y += step) {
                pixels.add(new Pixel((int) Math.round(k * y + b), y));
            }
        }
        return pixels;
    }

    //  Алгоритм ЦДА
    private List<Pixel> dda(int x1, int y1, int x2, int y2) {
        List<Pixel> pixels = new ArrayList<>();
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));

        if (steps == 0) {
            pixels.add(new Pixel(x1, y1));
            return pixels;
        }

        double xInc = (double) dx / steps;
        double yInc = (double) dy / steps;
        double x = x1;
        double y = y1;

        for (int i = 0; i <= steps; i++) {
            pixels.add(new Pixel((int) Math.round(x), (int) Math.round(y)));
            x += xInc;
            y += yInc;
        }
        return pixels;
    }

    // Алгоритм Брезенхема (отрезок)
    private List<Pixel> bresenhamLine(int x1, int y1, int x2, int y2) {
        List<Pixel> pixels = new ArrayList<>();
        int x = x1;
        int y = y1;
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        int sx = x2 >= x1 ? 1 : -1;
        int sy = y2 >= y1 ? 1 : -1;

        if (dy <= dx) {
            int err = 2 * dy - dx;
            for (int i = 0; i <= dx; i++) {
                pixels.add(new Pixel(x, y));
                if (err >= 0) {
                    y += sy;
                    err -= 2 * dx;
                }
                x += sx;
                err += 2 * dy;
            }
        } else {
            int err = 2 * dx - dy;
            for (int i = 0; i <= dy; i++) {
                pixels.add(new Pixel(x, y));
                if (err >= 0) {
                    x += sx;
                    err -= 2 * dy;
                }
                y += sy;
                err += 2 * dx;
            }
        }
        return pixels;
    }

    //  Алгоритм Брезенхема (окружность)
    private List<Pixel> bresenhamCircle(int xc, int yc, int r) {
        int rad = Math.abs(r);
        Set<String> seen = new LinkedHashSet<>();
        List<Pixel> pixels = new ArrayList<>();

        int x = 0;
        int y = rad;
        int d = 3 - 2 * rad;

        addCirclePoints(xc, yc, x, y, seen, pixels);
        while (y >= x) {
            x++;
            if (d > 0) {
                y--;
                d = d + 4 * (x - y) + 10;
            } else {
                d = d + 4 * x + 6;
            }
            addCirclePoints(xc, yc, x, y, seen, pixels);
        }
        return pixels;
    }

    private void addCirclePoints(int cx, int cy, int px, int py, Set<String> seen, List<Pixel> pixels) {
        int[][] pts = {
                {cx + px, cy + py}, {cx - px, cy + py},
                {cx + px, cy - py}, {cx - px, cy - py},
                {cx + py, cy + px}, {cx - py, cy + px},
                {cx + py, cy - px}, {cx - py, cy - px}
        };
        for (int[] pt : pts) {
            String key = pt[0] + "," + pt[1];
            if (seen.add(key)) {
                pixels.add(new Pixel(pt[0], pt[1]));
            }
        }
    }

    private List<Pixel> runAlgorithm(String algo, int v1, int v2, int v3, int v4) {
        switch (algo) {
            case "Пошаговый":
                return stepByStep(v1, v2, v3, v4);
            case "ЦДА":
                return dda(v1, v2, v3, v4);
            case "Брезенхем (отрезок)":
                return bresenhamLine(v1, v2, v3, v4);
            case "Брезенхем (окружность)":
                return bresenhamCircle(v1, v2, v3);
            default:
                return new ArrayList<>();
        }
    }

    private void drawShape() {
        String algo = (String) algoCombo.getSelectedItem();
        boolean isCircle = "Брезенхем (окружность)".equals(algo);

        Integer val1 = safeGet(entryX1);
        Integer val2 = safeGet(entryY1);
        Integer val3 = safeGet(entryX2);
        Integer val4 = isCircle ? 0 : safeGet(entryY2);

        if (val1 == null || val2 == null || val3 == null || val4 == null) {
            infoLabel.setText("Время работы: - | Закрашено пикселей: -");
            canvas.clearShape();
            return;
        }

        int iterations = 1000;
        List<Pixel> pixels = new ArrayList<>();

        long t0 = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            pixels = runAlgorithm(algo, val1, val2, val3, val4);
        }
        long t1 = System.nanoTime();

        double avgUs = ((t1 - t0) / (double) iterations) / 1000.0;
        infoLabel.setText(String.format("Время работы: %.2f мкс | Закрашено пикселей: %d", avgUs, pixels.size()));

        int cell = scaleSlider.getValue();
        canvas.setData(cell, pixels, isCircle, val1, val2, val3, val4);
    }

    private static class RasterCanvas extends JPanel {
        private int cell = 25;
        private List<Pixel> pixels = new ArrayList<>();
        private boolean hasShape = false;
        private boolean isCircle = false;
        private int v1, v2, v3, v4;

        public RasterCanvas() {
            setBackground(Color.WHITE);
        }

        public void setData(int cell, List<Pixel> pixels, boolean isCircle, int v1, int v2, int v3, int v4) {
            this.cell = cell;
            this.pixels = pixels;
            this.isCircle = isCircle;
            this.v1 = v1;
            this.v2 = v2;
            this.v3 = v3;
            this.v4 = v4;
            this.hasShape = true;
            repaint();
        }

        public void clearShape() {
            this.pixels = new ArrayList<>();
            this.hasShape = false;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int cw = getWidth();
            int ch = getHeight();
            if (cw <= 1 || ch <= 1) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, cw, ch);

            int ox = cw / 2;
            int oy = ch / 2;

            g2.setColor(new Color(0xE5, 0xE5, 0xE5));
            int startX = ((ox % cell) + cell) % cell;
            for (int x = startX; x < cw; x += cell) {
                g2.drawLine(x, 0, x, ch);
            }
            int startY = ((oy % cell) + cell) % cell;
            for (int y = startY; y < ch; y += cell) {
                g2.drawLine(0, y, cw, y);
            }

            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(0, oy, cw, oy);
            g2.drawLine(ox, 0, ox, ch);

            g2.setStroke(new BasicStroke(1f));
            g2.setFont(new Font("Arial", Font.PLAIN, 11));
            int maxXGrid = cw / (2 * cell) + 1;
            for (int xg = -maxXGrid; xg <= maxXGrid; xg++) {
                if (xg != 0) {
                    int px = ox + xg * cell;
                    g2.drawLine(px, oy - 3, px, oy + 3);
                    g2.drawString(String.valueOf(xg), px - 4, oy + 15);
                }
            }

            int maxYGrid = ch / (2 * cell) + 1;
            for (int yg = -maxYGrid; yg <= maxYGrid; yg++) {
                if (yg != 0) {
                    int py = oy - yg * cell;
                    g2.drawLine(ox - 3, py, ox + 3, py);
                    g2.drawString(String.valueOf(yg), ox - 18, py + 4);
                }
            }

            g2.setFont(new Font("Arial", Font.BOLD, 11));
            g2.drawString("0", ox - 12, oy + 15);

            if (hasShape) {
                for (Pixel p : pixels) {
                    int xLeft = ox + p.x * cell - cell / 2;
                    int yTop = oy - p.y * cell - cell / 2;

                    g2.setColor(new Color(0x4A, 0x90, 0xE2));
                    g2.fillRect(xLeft, yTop, cell, cell);

                    g2.setColor(new Color(0x70, 0x70, 0x70));
                    g2.drawRect(xLeft, yTop, cell, cell);
                }

                g2.setColor(new Color(0xE7, 0x4C, 0x3C));
                g2.setStroke(new BasicStroke(2f));
                if (isCircle) {
                    int cxPx = ox + v1 * cell;
                    int cyPx = oy - v2 * cell;
                    int rPx = Math.abs(v3) * cell;
                    g2.drawOval(cxPx - rPx, cyPx - rPx, 2 * rPx, 2 * rPx);
                } else {
                    int x1Px = ox + v1 * cell;
                    int y1Px = oy - v2 * cell;
                    int x2Px = ox + v3 * cell;
                    int y2Px = oy - v4 * cell;
                    g2.drawLine(x1Px, y1Px, x2Px, y2Px);
                }
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RasterApp().setVisible(true));
    }
}