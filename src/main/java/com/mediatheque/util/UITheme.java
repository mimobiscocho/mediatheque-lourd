package com.mediatheque.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 * Charte graphique de l'application.
 *
 * Centralise les couleurs, polices et composants stylisés pour conserver
 * une identité visuelle homogène. Le vert principal reprend exactement la
 * teinte utilisée par le client web (PHP), pour que les deux applications
 * du projet aient le même rendu.
 */
public final class UITheme {

    // ----- Couleurs : vert principal et déclinaisons --------------------

    /** Vert principal (entêtes, boutons d'action, focus). */
    public static final Color PRIMARY        = new Color(0x2D6A4F);
    /** Variante survol (hover) du vert. */
    public static final Color PRIMARY_HOVER  = new Color(0x255A42);
    /** Variante pressée (clic) du vert. */
    public static final Color PRIMARY_PRESS  = new Color(0x1F4D39);
    /** Vert très foncé (anciennement « PRIMARY_FONCE »). */
    public static final Color PRIMARY_FONCE  = new Color(0x1F4D39);
    /** Vert très clair : fond menu sélectionné, badges « OK ». */
    public static final Color PRIMARY_CLAIR  = new Color(0xDDE9E2);

    // ----- Couleurs : alertes ------------------------------------------

    public static final Color DANGER        = new Color(0xA63232);
    public static final Color DANGER_HOVER  = new Color(0x912B2B);
    public static final Color DANGER_PRESS  = new Color(0x7E2424);
    public static final Color DANGER_CLAIR  = new Color(0xF6E1E1);

    // ----- Couleurs : neutres -------------------------------------------

    public static final Color BACKGROUND   = new Color(0xF3F1EC);
    public static final Color SURFACE      = Color.WHITE;
    public static final Color WHITE        = Color.WHITE;
    /** Fond de la barre latérale (légèrement teinté du beige principal). */
    public static final Color SIDEBAR_BG   = new Color(0xFAF8F2);
    public static final Color BORDER       = new Color(0xD8D6CF);
    public static final Color BORDER_LIGHT = new Color(0xEAE8E2);
    public static final Color TEXT         = new Color(0x1F2422);
    public static final Color TEXT_MUTED   = new Color(0x6E7370);
    /** Fond de la ligne survolée / sélectionnée. */
    public static final Color ROW_SELECTED = new Color(0xD5E3DB);
    /** Fond des lignes paires dans les tableaux (alternance). */
    public static final Color ROW_ALT      = new Color(0xFAF8F2);

    // Bouton secondaire « neutre » (modifier, etc.).
    public static final Color NEUTRAL       = new Color(0x5C6660);
    public static final Color NEUTRAL_HOVER = new Color(0x4E544F);
    public static final Color NEUTRAL_PRESS = new Color(0x40443F);

    // Bouton « fantôme » (fond blanc + bordure).
    public static final Color GHOST_HOVER = new Color(0xF0EEE7);
    public static final Color GHOST_PRESS = new Color(0xE6E3D9);

    // ----- Polices ------------------------------------------------------
    // On choisit la première police disponible parmi une liste de noms
    // « modernes », avec une retombée systématique sur la police sans-serif
    // par défaut si rien n'est trouvé.

    private static final String FAMILY = chooseFamily(
            "Inter", "SF Pro Text", "Segoe UI", "Roboto",
            "Helvetica Neue", "DejaVu Sans", Font.SANS_SERIF);

    public static final Font DISPLAY    = new Font(FAMILY, Font.BOLD,  26);
    public static final Font TITLE      = new Font(FAMILY, Font.BOLD,  20);
    public static final Font H2         = new Font(FAMILY, Font.BOLD,  15);
    public static final Font H3         = new Font(FAMILY, Font.BOLD,  13);
    public static final Font NORMAL     = new Font(FAMILY, Font.PLAIN, 13);
    public static final Font BUTTON     = new Font(FAMILY, Font.BOLD,  12);
    public static final Font SMALL      = new Font(FAMILY, Font.PLAIN, 12);
    public static final Font SMALL_BOLD = new Font(FAMILY, Font.BOLD,  11);
    public static final Font MENU       = new Font(FAMILY, Font.PLAIN, 13);
    public static final Font BIG_NUMBER = new Font(FAMILY, Font.BOLD,  32);

    private static String chooseFamily(String... candidates) {
        Set<String> available = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getAvailableFontFamilyNames()));
        for (String c : candidates) {
            if (available.contains(c)) {
                return c;
            }
        }
        return Font.SANS_SERIF;
    }

    /** Classe utilitaire : pas d'instance possible. */
    private UITheme() {
    }

    // ----- Boutons ------------------------------------------------------

    /** Bouton principal : fond vert, texte blanc. */
    public static void stylePrimaryButton(JButton b) {
        styleFlat(b, PRIMARY, PRIMARY_HOVER, PRIMARY_PRESS, WHITE, null);
    }

    /** Bouton secondaire « neutre » (modifier, etc.). */
    public static void styleAccentButton(JButton b) {
        styleFlat(b, NEUTRAL, NEUTRAL_HOVER, NEUTRAL_PRESS, WHITE, null);
    }

    /** Bouton de suppression : fond rouge. */
    public static void styleDangerButton(JButton b) {
        styleFlat(b, DANGER, DANGER_HOVER, DANGER_PRESS, WHITE, null);
    }

    /** Bouton discret : fond blanc, bordure grise, texte sombre. */
    public static void styleGhostButton(JButton b) {
        styleFlat(b, SURFACE, GHOST_HOVER, GHOST_PRESS, TEXT, BORDER);
    }

    private static void styleFlat(JButton b, Color base, Color hover, Color press,
                                  Color fg, Color border) {
        b.setForeground(fg);
        b.setFont(BUTTON);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setRolloverEnabled(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(9, 18, 9, 18));
        b.setUI(new FlatButtonUI(base, hover, press, border, 8));
    }

    // ----- Libellés -----------------------------------------------------

    public static JLabel title(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(TITLE);
        l.setForeground(TEXT);
        return l;
    }

    public static JLabel subtitle(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(SMALL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    // ----- Panneaux -----------------------------------------------------

    /** Carte blanche : bord arrondi clair, padding intérieur confortable. */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        p.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER_LIGHT, 12, 1),
                new EmptyBorder(20, 22, 20, 22)));
        return p;
    }

    // ----- Champs -------------------------------------------------------

    /** Style moderne pour les champs texte (bordure arrondie + focus vert). */
    public static void styleTextField(JTextField f) {
        f.setFont(NORMAL);
        f.setForeground(TEXT);
        f.setBackground(SURFACE);
        f.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER, 7, 1),
                new EmptyBorder(7, 11, 7, 11)));
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                        new RoundedBorder(PRIMARY, 7, 2),
                        new EmptyBorder(6, 10, 6, 10)));
            }

            @Override
            public void focusLost(FocusEvent e) {
                f.setBorder(new CompoundBorder(
                        new RoundedBorder(BORDER, 7, 1),
                        new EmptyBorder(7, 11, 7, 11)));
            }
        });
    }

    // ----- Tableaux -----------------------------------------------------

    /** Mise en forme commune à tous les tableaux : sans grille verticale,
     *  lignes alternées, en-tête en small caps. */
    public static void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(NORMAL);
        table.setBackground(SURFACE);
        table.setForeground(TEXT);
        table.setGridColor(BORDER_LIGHT);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setReorderingAllowed(false);

        // En-tête sobre, sans gradient, séparé par un trait fin
        JTableHeader header = table.getTableHeader();
        header.setBackground(SURFACE);
        header.setForeground(TEXT_MUTED);
        header.setFont(SMALL_BOLD);
        header.setOpaque(true);
        header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new Dimension(0, 36));

        // Renderer commun : padding gauche + alternance de fond
        AlternatingRowRenderer renderer = new AlternatingRowRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    /** Place le tableau dans un scroll-pane avec bord arrondi clair. */
    public static JScrollPane wrapTable(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(SURFACE);
        scroll.setBorder(new RoundedBorder(BORDER_LIGHT, 10, 1));
        scroll.setBackground(SURFACE);
        return scroll;
    }

    /** Renderer « badge pilule » pour des colonnes Oui/Non, Actif/Inactif. */
    public static TableCellRenderer badgeRenderer() {
        return new BadgeRenderer();
    }

    /** Crée un label « badge » (pilule colorée) utilisable seul. */
    public static JLabel badge(String texte, boolean positif) {
        JLabel l = new JLabel(texte, SwingConstants.CENTER);
        l.setFont(SMALL_BOLD);
        l.setOpaque(true);
        l.setBackground(positif ? PRIMARY_CLAIR : DANGER_CLAIR);
        l.setForeground(positif ? PRIMARY_FONCE : DANGER);
        l.setBorder(new EmptyBorder(3, 10, 3, 10));
        return l;
    }

    // ----- Helpers internes --------------------------------------------

    /** Bordure peinte avec coins arrondis (pour cartes, champs, boutons). */
    public static final class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int radius;
        private final int thickness;

        public RoundedBorder(Color color, int radius, int thickness) {
            this.color = color;
            this.radius = radius;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            int off = thickness / 2;
            g2.drawRoundRect(x + off, y + off,
                    w - thickness, h - thickness, radius, radius);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int t = Math.max(thickness, 1);
            return new Insets(t, t, t, t);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            int t = Math.max(thickness, 1);
            insets.left = insets.right = insets.top = insets.bottom = t;
            return insets;
        }
    }

    /** Délégué d'interface plat pour bouton (coin arrondi + hover/press). */
    private static final class FlatButtonUI extends BasicButtonUI {
        private final Color base;
        private final Color hover;
        private final Color press;
        private final Color border;
        private final int radius;

        FlatButtonUI(Color base, Color hover, Color press, Color border, int radius) {
            this.base = base;
            this.hover = hover;
            this.press = press;
            this.border = border;
            this.radius = radius;
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            AbstractButton b = (AbstractButton) c;
            b.setRolloverEnabled(true);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            ButtonModel m = b.getModel();
            Color fill;
            if (!b.isEnabled()) {
                fill = blend(base, BACKGROUND, 0.6f);
            } else if (m.isPressed()) {
                fill = press;
            } else if (m.isRollover()) {
                fill = hover;
            } else {
                fill = base;
            }

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, b.getWidth(), b.getHeight(), radius, radius);
            if (border != null) {
                g2.setColor(border);
                g2.drawRoundRect(0, 0, b.getWidth() - 1, b.getHeight() - 1,
                        radius, radius);
            }
            g2.dispose();

            super.paint(g, c);
        }
    }

    /** Mélange deux couleurs (utilisé pour griser un bouton désactivé). */
    private static Color blend(Color a, Color b, float t) {
        float u = 1f - t;
        return new Color(
                Math.round(a.getRed() * u + b.getRed() * t),
                Math.round(a.getGreen() * u + b.getGreen() * t),
                Math.round(a.getBlue() * u + b.getBlue() * t));
    }

    /** Renderer par défaut : alternance de fond + padding gauche. */
    private static final class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(
                    table, value, isSelected, false, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? SURFACE : ROW_ALT);
                c.setForeground(TEXT);
            }
            if (c instanceof JLabel) {
                JLabel l = (JLabel) c;
                l.setBorder(new EmptyBorder(0, 14, 0, 14));
            }
            return c;
        }
    }

    /** Renderer « badge pilule » centré pour cellule de statut. */
    private static final class BadgeRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String txt = String.valueOf(value);
            boolean positif = txt.equalsIgnoreCase("Oui")
                    || txt.equalsIgnoreCase("Actif")
                    || txt.equalsIgnoreCase("Disponible")
                    || txt.equalsIgnoreCase("OK");

            JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            wrap.setOpaque(true);
            wrap.setBackground(isSelected ? ROW_SELECTED
                    : (row % 2 == 0 ? SURFACE : ROW_ALT));
            JLabel pill = new JLabel(txt);
            pill.setFont(SMALL_BOLD);
            pill.setOpaque(true);
            pill.setBackground(positif ? PRIMARY_CLAIR : DANGER_CLAIR);
            pill.setForeground(positif ? PRIMARY_FONCE : DANGER);
            pill.setBorder(BorderFactory.createCompoundBorder(
                    new RoundedBorder(positif ? PRIMARY_CLAIR : DANGER_CLAIR, 10, 1),
                    new EmptyBorder(2, 10, 2, 10)));
            wrap.add(pill);
            return wrap;
        }
    }
}
