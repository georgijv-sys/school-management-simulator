import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.DefaultListCellRenderer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Centralised visual style for the dashboard: colour palette, typography and
 * small presentation-only components. Contains no business logic.
 */
public final class Theme {

    private Theme() {
    }

    public static final Color BACKGROUND = new Color(248, 250, 252);
    public static final Color SURFACE = Color.WHITE;
    public static final Color BORDER = new Color(226, 232, 240);
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    public static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    public static final Color TEXT_MUTED = new Color(148, 163, 184);

    public static final Color INDIGO = new Color(79, 70, 229);
    public static final Color EMERALD = new Color(5, 150, 105);
    public static final Color AMBER = new Color(217, 119, 6);
    public static final Color ROSE = new Color(225, 29, 72);
    public static final Color VIOLET = new Color(124, 58, 237);
    public static final Color CYAN = new Color(8, 145, 178);

    public static final Color ACCENT = INDIGO;
    public static final Color ACCENT_HOVER = new Color(67, 56, 202);
    public static final Color ACCENT_PRESSED = new Color(55, 48, 163);
    public static final Color ACCENT_SOFT = new Color(238, 242, 255);
    public static final Color ACCENT_DISABLED = new Color(165, 180, 252);

    public static final Color SUCCESS = EMERALD;
    public static final Color SUCCESS_SOFT = new Color(209, 250, 229);
    public static final Color WARNING = new Color(180, 83, 9);
    public static final Color WARNING_SOFT = new Color(254, 243, 199);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color DANGER_SOFT = new Color(254, 226, 226);
    public static final Color NEUTRAL = new Color(71, 85, 105);
    public static final Color NEUTRAL_SOFT = new Color(241, 245, 249);

    public static final Color CHART_PEOPLE = INDIGO;
    public static final Color CHART_COURSES = ROSE;
    public static final Color CHART_CERTIFICATES = EMERALD;
    public static final Color CHART_GRID = new Color(241, 245, 249);

    public static final Color ROW_STRIPE = new Color(248, 250, 252);
    public static final Color ROW_SELECTED = new Color(224, 231, 255);

    private static final String FONT_FAMILY = resolveFontFamily();

    private static String resolveFontFamily() {
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String preferred : new String[]{"Inter", "SF Pro Text", "Segoe UI", "Helvetica Neue"}) {
            if (available.contains(preferred)) {
                return preferred;
            }
        }
        return "SansSerif";
    }

    public static Font font(int style, int size) {
        return new Font(FONT_FAMILY, style, size);
    }

    /** Bold label font with widened letter spacing, for small uppercase headings. */
    public static Font smallCapsFont(int size) {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.TRACKING, 0.08f);
        return font(Font.BOLD, size).deriveFont(attributes);
    }

    /** Small filled circle icon used as a coloured accent next to labels. */
    public static Icon dot(Color color, int diameter) {
        return new Icon() {
            public void paintIcon(Component component, Graphics graphics, int x, int y) {
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(color);
                g.fillOval(x, y, diameter, diameter);
                g.dispose();
            }

            public int getIconWidth() {
                return diameter;
            }

            public int getIconHeight() {
                return diameter;
            }
        };
    }

    /** Pastel background and strong foreground pair for a status badge. */
    public static Color[] badgeColors(String status) {
        if (status.equals("Running") || status.equals("On track") || status.equals("Available")) {
            return new Color[]{SUCCESS_SOFT, SUCCESS};
        }
        if (status.equals("Waiting")) {
            return new Color[]{WARNING_SOFT, WARNING};
        }
        if (status.equals("At risk") || status.equals("Cancelled")) {
            return new Color[]{DANGER_SOFT, DANGER};
        }
        if (status.equals("Busy") || status.equals("Ready")) {
            return new Color[]{ACCENT_SOFT, ACCENT};
        }
        return new Color[]{NEUTRAL_SOFT, NEUTRAL};
    }

    /** White panel with rounded corners, a hairline border and a soft shadow. */
    public static class CardPanel extends JPanel {
        private static final int RADIUS = 14;
        private static final int SHADOW = 2;

        public CardPanel(LayoutManager layout, int padding) {
            super(layout);
            setOpaque(false);
            setBorder(new EmptyBorder(padding, padding, padding + SHADOW, padding));
        }

        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();

            g.setColor(new Color(15, 23, 42, 10));
            g.fillRoundRect(0, SHADOW, width, height - SHADOW, RADIUS + 2, RADIUS + 2);

            g.setColor(SURFACE);
            g.fillRoundRect(0, 0, width, height - SHADOW, RADIUS, RADIUS);

            g.setColor(BORDER);
            g.drawRoundRect(0, 0, width - 1, height - SHADOW - 1, RADIUS, RADIUS);

            g.dispose();
            super.paintComponent(graphics);
        }
    }

    /** Flat rounded button with hover, pressed, focus and disabled states. */
    public static class PillButton extends JButton {
        private static final int ARC = 18;
        private final boolean primary;

        public PillButton(String text, boolean primary) {
            super(text);
            this.primary = primary;
            setFont(font(Font.BOLD, 13));
            setForeground(primary ? Color.WHITE : TEXT_PRIMARY);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setBorder(new EmptyBorder(8, 16, 8, 16));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addFocusListener(new FocusAdapter() {
                public void focusGained(FocusEvent event) {
                    repaint();
                }

                public void focusLost(FocusEvent event) {
                    repaint();
                }
            });
        }

        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();

            g.setColor(backgroundFor(getModel()));
            g.fillRoundRect(0, 0, width, height, ARC, ARC);

            if (!primary) {
                g.setColor(BORDER);
                g.drawRoundRect(0, 0, width - 1, height - 1, ARC, ARC);
            }
            if (isFocusOwner()) {
                g.setColor(new Color(ACCENT.getRed(), ACCENT.getGreen(), ACCENT.getBlue(), 110));
                g.setStroke(new BasicStroke(2f));
                g.drawRoundRect(1, 1, width - 3, height - 3, ARC - 2, ARC - 2);
            }
            g.dispose();
            super.paintComponent(graphics);
        }

        private Color backgroundFor(ButtonModel model) {
            if (!isEnabled()) {
                return primary ? ACCENT_DISABLED : NEUTRAL_SOFT;
            }
            if (model.isPressed()) {
                return primary ? ACCENT_PRESSED : new Color(226, 232, 240);
            }
            if (model.isRollover()) {
                return primary ? ACCENT_HOVER : new Color(241, 245, 249);
            }
            return primary ? ACCENT : SURFACE;
        }
    }

    /** Table cell renderer with zebra striping, padding and no focus ring. */
    public static class StripedCellRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            setFont(font(Font.PLAIN, 13));
            setBackground(isSelected ? ROW_SELECTED : (row % 2 == 0 ? SURFACE : ROW_STRIPE));
            setForeground(TEXT_PRIMARY);
            return this;
        }
    }

    /** Table cell renderer that draws status text as a coloured pill badge. */
    public static class BadgeRenderer extends DefaultTableCellRenderer {
        private Color rowBackground = SURFACE;
        private Color badgeBackground = NEUTRAL_SOFT;
        private Color badgeForeground = NEUTRAL;

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, false, row, column);
            rowBackground = isSelected ? ROW_SELECTED : (row % 2 == 0 ? SURFACE : ROW_STRIPE);
            Color[] colors = badgeColors(getText());
            badgeBackground = colors[0];
            badgeForeground = colors[1];
            setFont(font(Font.BOLD, 11));
            return this;
        }

        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(rowBackground);
            g.fillRect(0, 0, getWidth(), getHeight());

            String text = getText();
            if (!text.isEmpty()) {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setFont(getFont());
                FontMetrics metrics = g.getFontMetrics();
                int pillHeight = metrics.getHeight() + 4;
                int pillWidth = metrics.stringWidth(text) + 18;
                int y = (getHeight() - pillHeight) / 2;

                g.setColor(badgeBackground);
                g.fillRoundRect(10, y, pillWidth, pillHeight, pillHeight, pillHeight);
                g.setColor(badgeForeground);
                g.drawString(text, 19, y + metrics.getAscent() + 2);
            }
            g.dispose();
        }
    }

    /** Wraps the look-and-feel header renderer to restyle its font and colour. */
    public static class HeaderRenderer implements TableCellRenderer {
        private final TableCellRenderer delegate;

        public HeaderRenderer(TableCellRenderer delegate) {
            this.delegate = delegate;
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            Component component = delegate.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            component.setFont(font(Font.BOLD, 12));
            component.setForeground(TEXT_SECONDARY);
            return component;
        }
    }

    /** Event log cell with padding and a hairline separator between entries. */
    public static class EventCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, false);
            setFont(font(Font.PLAIN, 13));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, CHART_GRID),
                    new EmptyBorder(6, 10, 6, 10)));
            setBackground(isSelected ? ROW_SELECTED : SURFACE);
            setForeground(TEXT_PRIMARY);
            return this;
        }
    }
}
