import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class ColorConverterApp extends JFrame {

    private boolean isUpdating = false;

    private int currentR = 0;
    private int currentG = 0;
    private int currentB = 0;

    private JPanel colorPreviewPanel;

    private final JSlider[] cmykSliders = new JSlider[4];
    private final JTextField[] cmykFields = new JTextField[4];

    private final JSlider[] rgbSliders = new JSlider[3];
    private final JTextField[] rgbFields = new JTextField[3];

    private final JSlider[] hsvSliders = new JSlider[3];
    private final JTextField[] hsvFields = new JTextField[3];

    public ColorConverterApp() {
        setTitle("CMYK - RGB - HSV");
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        colorPreviewPanel = new JPanel();
        colorPreviewPanel.setPreferredSize(new Dimension(300, 150));
        colorPreviewPanel.setBackground(new Color(currentR, currentG, currentB));
        
        JPanel previewContainer = new JPanel(new BorderLayout(10, 10));
        previewContainer.add(colorPreviewPanel, BorderLayout.CENTER);

        JButton btnPalette = new JButton("Выбор цвета из палитры");
        btnPalette.addActionListener(e -> {
            Color chosen = JColorChooser.showDialog(this, "Выбор цвета", new Color(currentR, currentG, currentB));
            if (chosen != null) {
                currentR = chosen.getRed();
                currentG = chosen.getGreen();
                currentB = chosen.getBlue();
                updateAllModels(-1);
            }
        });
        previewContainer.add(btnPalette, BorderLayout.SOUTH);
        
        mainPanel.add(previewContainer);
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createModelPanel("CMYK", new String[]{"C", "M", "Y", "K"}, new int[]{100, 100, 100, 100}, cmykSliders, cmykFields, 0));
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createModelPanel("RGB", new String[]{"R", "G", "B"}, new int[]{255, 255, 255}, rgbSliders, rgbFields, 1));
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createModelPanel("HSV", new String[]{"H", "S", "V"}, new int[]{360, 100, 100}, hsvSliders, hsvFields, 2));

        add(new JScrollPane(mainPanel), BorderLayout.CENTER);
        updateAllModels(-1);
    }

    private JPanel createModelPanel(String title, String[] labels, int[] maxValues, JSlider[] sliders, JTextField[] fields, int modelType) {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(labels.length + 1, 1, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(title));

        for (int i = 0; i < labels.length; i++) {
            final int index = i;
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            
            JLabel lbl = new JLabel(labels[index] + ":");
            lbl.setPreferredSize(new Dimension(25, 20));
            row.add(lbl);

            sliders[index] = new JSlider(0, maxValues[index], 0);
            sliders[index].setPreferredSize(new Dimension(280, 30));
            
            fields[index] = new JTextField("0", 5);

            sliders[index].addChangeListener(e -> {
                if (isUpdating) return;
                int val = sliders[index].getValue();
                if (!fields[index].getText().equals(String.valueOf(val))) {
                    fields[index].setText(String.valueOf(val));
                }
                onValueChanged(modelType);
            });

            fields[index].getDocument().addDocumentListener(new DocumentListener() {
                private void onChange() {
                    if (isUpdating) return;
                    String text = fields[index].getText();
                    if (!text.isEmpty()) {
                        try {
                            int val = Integer.parseInt(text);
                            val = Math.max(0, Math.min(val, maxValues[index]));
                            if (sliders[index].getValue() != val) {
                                sliders[index].setValue(val);
                            }
                            onValueChanged(modelType);
                        } catch (NumberFormatException ignored) {}
                    }
                }
                @Override public void insertUpdate(DocumentEvent e) { onChange(); }
                @Override public void removeUpdate(DocumentEvent e) { onChange(); }
                @Override public void changedUpdate(DocumentEvent e) { onChange(); }
            });

            row.add(sliders[index]);
            row.add(fields[index]);
            panel.add(row);
        }
        return panel;
    }

    private void onValueChanged(int modelType) {
        if (isUpdating) return;

        if (modelType == 0) { // CMYK 
            float c = cmykSliders[0].getValue();
            float m = cmykSliders[1].getValue();
            float y = cmykSliders[2].getValue();
            float k = cmykSliders[3].getValue();
            int[] rgb = cmykToRgb(c, m, y, k);
            currentR = rgb[0];
            currentG = rgb[1];
            currentB = rgb[2];
        } else if (modelType == 1) { // RGB 
            currentR = rgbSliders[0].getValue();
            currentG = rgbSliders[1].getValue();
            currentB = rgbSliders[2].getValue();
        } else if (modelType == 2) { // HSV
            float h = hsvSliders[0].getValue();
            float s = hsvSliders[1].getValue();
            float v = hsvSliders[2].getValue();
            int[] rgb = hsvToRgb(h, s, v);
            currentR = rgb[0];
            currentG = rgb[1];
            currentB = rgb[2];
        }

        updateAllModels(modelType);
    }

    private void updateAllModels(int sourceModel) {
        isUpdating = true;
        colorPreviewPanel.setBackground(new Color(currentR, currentG, currentB));

        if (sourceModel != 0) {
            float[] cmyk = rgbToCmyk(currentR, currentG, currentB);
            setSliderAndField(cmykSliders[0], cmykFields[0], Math.round(cmyk[0]));
            setSliderAndField(cmykSliders[1], cmykFields[1], Math.round(cmyk[1]));
            setSliderAndField(cmykSliders[2], cmykFields[2], Math.round(cmyk[2]));
            setSliderAndField(cmykSliders[3], cmykFields[3], Math.round(cmyk[3]));
        }

        if (sourceModel != 1) {
            setSliderAndField(rgbSliders[0], rgbFields[0], currentR);
            setSliderAndField(rgbSliders[1], rgbFields[1], currentG);
            setSliderAndField(rgbSliders[2], rgbFields[2], currentB);
        }

        if (sourceModel != 2) {
            float[] hsv = rgbToHsv(currentR, currentG, currentB);
            setSliderAndField(hsvSliders[0], hsvFields[0], Math.round(hsv[0]));
            setSliderAndField(hsvSliders[1], hsvFields[1], Math.round(hsv[1]));
            setSliderAndField(hsvSliders[2], hsvFields[2], Math.round(hsv[2]));
        }

        isUpdating = false;
    }

    private void setSliderAndField(JSlider slider, JTextField field, int value) {
        if (slider.getValue() != value) {
            slider.setValue(value);
        }
        if (!field.getText().equals(String.valueOf(value))) {
            field.setText(String.valueOf(value));
        }
    }


    public static float[] rgbToCmyk(int r, int g, int b) {
        float rf = r / 255.0f;
        float gf = g / 255.0f;
        float bf = b / 255.0f;

        float k = 1.0f - Math.max(rf, Math.max(gf, bf));
        if (k >= 1.0f) {
            return new float[]{0f, 0f, 0f, 100f};
        }
        float c = (1.0f - rf - k) / (1.0f - k) * 100.0f;
        float m = (1.0f - gf - k) / (1.0f - k) * 100.0f;
        float y = (1.0f - bf - k) / (1.0f - k) * 100.0f;
        return new float[]{
            Math.max(0, Math.min(c, 100)), 
            Math.max(0, Math.min(m, 100)), 
            Math.max(0, Math.min(y, 100)), 
            Math.max(0, Math.min(k * 100.0f, 100))
        };
    }

    public static int[] cmykToRgb(float c, float m, float y, float k) {
        float cf = c / 100.0f;
        float mf = m / 100.0f;
        float yf = y / 100.0f;
        float kf = k / 100.0f;

        int r = Math.round(255 * (1.0f - cf) * (1.0f - kf));
        int g = Math.round(255 * (1.0f - mf) * (1.0f - kf));
        int b = Math.round(255 * (1.0f - yf) * (1.0f - kf));
        return new int[]{Math.max(0, Math.min(r, 255)), Math.max(0, Math.min(g, 255)), Math.max(0, Math.min(b, 255))};
    }

    public static float[] rgbToHsv(int r, int g, int b) {
    float rf = r / 255.0f;
    float gf = g / 255.0f;
    float bf = b / 255.0f;

    float max = Math.max(rf, Math.max(gf, bf));
    float min = Math.min(rf, Math.min(gf, bf));
    float delta = max - min;

    float h = 0.0f;
    float s = 0.0f;
    float v = max; 

    if (max > 0.00001f) {
        s = delta / max;
    } else {
        return new float[]{0f, 0f, 0f};
    }

    if (delta > 0.00001f) {
        if (max == rf) {
            h = (gf - bf) / delta;
            if (gf < bf) h += 6.0f; 
        } else if (max == gf) {
            h = (bf - rf) / delta + 2.0f;
        } else {
            h = (rf - gf) / delta + 4.0f;
        }
        h = h * 60.0f; 
    } else {
        h = 0.0f;
    }

    return new float[]{
        Math.max(0, Math.min(h, 360)),
        Math.max(0, Math.min(s * 100.0f, 100)),
        Math.max(0, Math.min(v * 100.0f, 100))
    };
}

    public static int[] hsvToRgb(float h, float s, float v) {
    float hf = h / 360.0f;
    float sf = s / 100.0f;
    float vf = v / 100.0f;

    float rf = 0, gf = 0, bf = 0;

    if (sf <= 0.0f) {
        rf = gf = bf = vf;
    } else {
        float sector = (hf * 6.0f);
        if (sector >= 6.0f) sector = 0.0f;
        
        int i = (int) Math.floor(sector);
        float f = sector - i;
        
        float p = vf * (1.0f - sf);
        float q = vf * (1.0f - (sf * f));
        float t = vf * (1.0f - (sf * (1.0f - f)));

        switch (i) {
            case 0: rf = vf; gf = t; bf = p; break;
            case 1: rf = q; gf = vf; bf = p; break;
            case 2: rf = p; gf = vf; bf = t; break;
            case 3: rf = p; gf = q; bf = vf; break;
            case 4: rf = t; gf = p; bf = vf; break;
            default: rf = vf; gf = p; bf = q; break;
        }
    }

    int r = Math.round(rf * 255.0f);
    int g = Math.round(gf * 255.0f);
    int b = Math.round(bf * 255.0f);

    return new int[]{
        Math.max(0, Math.min(r, 255)),
        Math.max(0, Math.min(g, 255)),
        Math.max(0, Math.min(b, 255))
    };
}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ColorConverterApp().setVisible(true);
        });
    }
}