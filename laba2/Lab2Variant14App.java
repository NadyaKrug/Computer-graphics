import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Lab2Variant14App extends JFrame {
    private BufferedImage originalImage;
    private BufferedImage processedImage;

    private JLabel originalLabel;
    private JLabel processedLabel;

    public Lab2Variant14App() {
        setTitle("Лабораторная работа №2 — Вариант 14");
        setSize(1280, 680);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel(new GridLayout(2, 1));

        JPanel row1 = new JPanel();
        JButton loadBtn = new JButton("Выбрать изображение");
        JButton bernsenBtn = new JButton("1. Порог Бернсена");
        JButton niblackBtn = new JButton("1. Порог Ниблацка");

        row1.add(loadBtn);
        row1.add(bernsenBtn);
        row1.add(niblackBtn);

        JPanel row2 = new JPanel();
        JButton pointsBtn = new JButton("2. Точки");
        JButton linesHBtn = new JButton("2. Горизонтальные линии");
        JButton linesVBtn = new JButton("2. Вертикальные линии");
        JButton linesDiag1Btn = new JButton("2. Диагональ (/)");
        JButton linesDiag2Btn = new JButton("2. Диагональ (\\)");
        JButton sobelBtn = new JButton("2. Перепады яркости (Собель)");

        row2.add(pointsBtn);
        row2.add(linesHBtn);
        row2.add(linesVBtn);
        row2.add(linesDiag1Btn);
        row2.add(linesDiag2Btn);
        row2.add(sobelBtn);

        topPanel.add(row1);
        topPanel.add(row2);
        add(topPanel, BorderLayout.NORTH);

        JPanel imagePanel = new JPanel(new GridLayout(1, 2, 10, 10));
        imagePanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        originalLabel = new JLabel("", JLabel.CENTER);
        processedLabel = new JLabel("", JLabel.CENTER);

        imagePanel.add(new JScrollPane(originalLabel));
        imagePanel.add(new JScrollPane(processedLabel));
        add(imagePanel, BorderLayout.CENTER);

        loadBtn.addActionListener(e -> loadImage());
        bernsenBtn.addActionListener(e -> applyBernsen());
        niblackBtn.addActionListener(e -> applyNiblack());

        pointsBtn.addActionListener(e -> applyPointDetection(50));

        linesHBtn.addActionListener(e -> applyLineDetection(new int[][]{
                {-1, -1, -1},
                { 2,  2,  2},
                {-1, -1, -1}
        }, 50));

        linesVBtn.addActionListener(e -> applyLineDetection(new int[][]{
                {-1,  2, -1},
                {-1,  2, -1},
                {-1,  2, -1}
        }, 50));

        linesDiag1Btn.addActionListener(e -> applyLineDetection(new int[][]{
                {-1, -1,  2},
                {-1,  2, -1},
                { 2, -1, -1}
        }, 50));

        linesDiag2Btn.addActionListener(e -> applyLineDetection(new int[][]{
                { 2, -1, -1},
                {-1,  2, -1},
                {-1, -1,  2}
        }, 50));

        sobelBtn.addActionListener(e -> applySobel());
    }

    private void loadImage() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                BufferedImage loaded = ImageIO.read(chooser.getSelectedFile());
                if (loaded == null) {
                    JOptionPane.showMessageDialog(this, "Неподдерживаемый формат файла!");
                    return;
                }
                originalImage = loaded;
                originalLabel.setIcon(new ImageIcon(scaleImage(originalImage, 560, 520)));
                processedLabel.setIcon(null);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Ошибка загрузки: " + ex.getMessage());
            }
        }
    }

    private int[][] toGrayscale(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[][] gray = new int[w][h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                gray[x][y] = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            }
        }
        return gray;
    }


    // Метод Бернсена 15*15
    private void applyBernsen() {
        if (!checkImage()) return;
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        int[][] gray = toGrayscale(originalImage);
        int radius = 50 / 2;
        int epsilon = 50;

        processedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int jLow = 255;
                int jHigh = 0;

                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                            int val = gray[nx][ny];
                            if (val < jLow) jLow = val;
                            if (val > jHigh) jHigh = val;
                        }
                    }
                }

                int contrast = jHigh - jLow;
                double threshold = contrast <= epsilon ? 128.0 : (jLow + jHigh) / 2.0;

                int outRgb = gray[x][y] > threshold ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
                processedImage.setRGB(x, y, outRgb);
            }
        }
        showResult();
    }

    // Метод Ниблацка 15*15
    private void applyNiblack() {
        if (!checkImage()) return;
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        int[][] gray = toGrayscale(originalImage);
        int radius = 15 / 2;
        double k = -0.2;

        processedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                double sum = 0.0;
                double sumSq = 0.0;
                int count = 0;

                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dx = -radius; dx <= radius; dx++) {
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                            int val = gray[nx][ny];
                            sum += val;
                            sumSq += val * val;
                            count++;
                        }
                    }
                }

                double mean = sum / count;
                double variance = Math.max(0.0, (sumSq / count) - (mean * mean));
                double stdDev = Math.sqrt(variance);
                double threshold = mean + k * stdDev;

                int outRgb = gray[x][y] > threshold ? Color.WHITE.getRGB() : Color.BLACK.getRGB();
                processedImage.setRGB(x, y, outRgb);
            }
        }
        showResult();
    }


    private void applyPointDetection(int threshold) {
        if (!checkImage()) return;
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        int[][] gray = toGrayscale(originalImage);

        int[][] mask = {
                {-1, -1, -1},
                {-1,  8, -1},
                {-1, -1, -1}
        };

        processedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int response = convolve3x3(gray, x, y, mask);
                int val = Math.abs(response) > threshold ? Math.min(255, Math.abs(response)) : 0;
                Color c = new Color(val, val, val);
                processedImage.setRGB(x, y, c.getRGB());
            }
        }
        showResult();
    }

    private void applyLineDetection(int[][] mask, int threshold) {
        if (!checkImage()) return;
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        int[][] gray = toGrayscale(originalImage);

        processedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int response = convolve3x3(gray, x, y, mask);
                int absR = Math.abs(response);
                int val = absR > threshold ? Math.min(255, absR) : 0;
                Color c = new Color(val, val, val);
                processedImage.setRGB(x, y, c.getRGB());
            }
        }
        showResult();
    }

    // Оператор Собеля евклидова норма 
    private void applySobel() {
        if (!checkImage()) return;
        int w = originalImage.getWidth();
        int h = originalImage.getHeight();
        int[][] gray = toGrayscale(originalImage);

        int[][] gx = {
                {-1, 0, 1},
                {-2, 0, 2},
                {-1, 0, 1}
        };
        int[][] gy = {
                {-1, -2, -1},
                { 0,  0,  0},
                { 1,  2,  1}
        };

        processedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int sumX = convolve3x3(gray, x, y, gx);
                int sumY = convolve3x3(gray, x, y, gy);

                double mag = Math.sqrt((double) sumX * sumX + (double) sumY * sumY);

                int val = (int) Math.min(255, Math.round(mag));
                Color c = new Color(val, val, val);
                processedImage.setRGB(x, y, c.getRGB());
            }
        }
        showResult();
    }

    private int convolve3x3(int[][] gray, int x, int y, int[][] kernel) {
        int sum = 0;
        for (int ky = -1; ky <= 1; ky++) {
            for (int kx = -1; kx <= 1; kx++) {
                sum += gray[x + kx][y + ky] * kernel[ky + 1][kx + 1];
            }
        }
        return sum;
    }

    private boolean checkImage() {
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Сначала выберите изображение!");
            return false;
        }
        return true;
    }

    private void showResult() {
        processedLabel.setIcon(new ImageIcon(scaleImage(processedImage, 560, 520)));
    }

    private Image scaleImage(Image img, int w, int h) {
        return img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Lab2Variant14App().setVisible(true));
    }
}