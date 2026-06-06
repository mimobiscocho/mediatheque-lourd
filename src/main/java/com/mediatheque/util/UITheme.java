package com.mediatheque.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.JTableHeader;

/**
 * Charte graphique de l'application — design system simple, moderne et
 * professionnel. Toutes les couleurs, polices et styles communs sont
 * centralisés ici.
 */
public final class UITheme {

    // --- Palette --------------------------------------------------------
    public static final Color PRIMARY        = new Color(0x1E40AF); // bleu institutionnel
    public static final Color PRIMARY_HOVER  = new Color(0x1E3A8A);
    public static final Color PRIMARY_SOFT   = new Color(0xEFF6FF);

    public static final Color ACCENT         = new Color(0xEA580C); // orange chaleureux
    public static final Color ACCENT_HOVER   = new Color(0xC2410C);
    public static final Color ACCENT_SOFT    = new Color(0xFFF7ED);

    public static final Color SUCCESS        = new Color(0x15803D);
    public static final Color SUCCESS_SOFT   = new Color(0xDCFCE7);
    public static final Color WARNING        = new Color(0xB45309);
    public static final Color WARNING_SOFT   = new Color(0xFEF3C7);
    public static final Color DANGER         = new Color(0xB91C1C);
    public static final Color DANGER_HOVER   = new Color(0x991B1B);
    public static final Color DANGER_SOFT    = new Color(0xFEE2E2);

    public static final Color BACKGROUND     = new Color(0xF8FAFC);
    public static final Color SURFACE        = Color.WHITE;
    public static final Color WHITE          = Color.WHITE;
    public static final Color BORDER         = new Color(0xE2E8F0);
    public static final Color BORDER_STRONG  = new Color(0xCBD5E1);
    public static final Color TEXT           = new Color(0x0F172A);
    public static final Color TEXT_MUTED     = new Color(0x64748B);
    public static final Color ROW_HOVER      = new Color(0xF1F5F9);
    public static final Color ROW_SELECTED   = new Color(0xDBEAFE);

    // --- Polices --------------------------------------------------------
    private static final String FONT_FAMILY = pickFontFamily(
            "Segoe UI", "SF Pro Text", "Inter", "Roboto", "Helvetica Neue", "Arial");

    public static final Font TITLE  = new Font(FONT_FAMILY, Font.BOLD,  22);
    public static final Font H2     = new Font(FONT_FAMILY, Font.BOLD,  16);
    public static final Font H3     = new Font(FONT_FAMILY, Font.BOLD,  14);
    public static final Font NORMAL = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font SMALL  = new Font(FONT_FAMILY, Font.PLAIN, 12);

    private UITheme() {
    }

    private static String pickFontFamily(String... candidates) {
        java.util.Set<String> available = new java.util.HashSet<>();
        for (String s : java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames()) {
            available.add(s);
        }
        for (String c : candidates) {
            if (available.contains(c)) {
                return c;
            }
        }
        return Font.SANS_SERIF;
    }

    // ====================================================================
    //  BOUTONS
    // ====================================================================

    /** Bouton principal (action positive : enregistrer, se connecter…). */
    public static void stylePrimaryButton(JButton b) {
        styleFilledButton(b, PRIMARY, PRIMARY_HOVER, WHITE);
    }

    /** Bouton d'accent (action secondaire). */
    public static void styleAccentButton(JButton b) {
        styleFilledButton(b, ACCENT, ACCENT_HOVER, WHITE);
    }

    /** Bouton de suppression (action destructive). */
    public static void styleDangerButton(JButton b) {
        styleFilledButton(b, DANGER, DANGER_HOVER, WHITE);
    }

    /** Bouton fantôme : fond blanc, bordure et texte sobres. */
    public static void styleGhostButton(JButton b) {
        b.setBackground(SURFACE);
        b.setForeground(TEXT);
        b.setFont(NORMAL);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(6, 14, 6, 14)));
        attachHover(b, SURFACE, ROW_HOVER);
    }

    private static void styleFilledButton(JButton b, Color bg, Color hover, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(NORMAL);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        attachHover(b, bg, hover);
    }

    private static void attachHover(JButton b, Color normal, Color hover) {
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { b.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e)  { b.setBackground(normal); }
        });
    }

    // ====================================================================
    //  LIBELLÉS
    // ====================================================================

    /** Titre de page (utilisé dans l'en-tête de chaque module). */
    public static JLabel title(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(TITLE);
        l.setForeground(TEXT);
        return l;
    }

    /** Sous-titre / libellé secondaire. */
    public static JLabel subtitle(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(SMALL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    // ====================================================================
    //  CARTES & PANNEAUX
    // ====================================================================

    /** Bordure standard d'une carte : fine + padding intérieur. */
    public static Border cardBorder() {
        return new CompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(18, 18, 18, 18));
    }

    /** Crée un panneau type « card » : surface blanche + bordure fine. */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(SURFACE);
        p.setBorder(cardBorder());
        return p;
    }

    // ====================================================================
    //  TABLEAUX
    // ====================================================================

    /** Style homogène pour un tableau de données. */
    public static void styleTable(JTable table) {
        table.setRowHeight(36);
        table.setFont(NORMAL);
        table.setBackground(SURFACE);
        table.setForeground(TEXT);
        table.setShowVerticalLines(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(BORDER);
        table.setSelectionBackground(ROW_SELECTED);
        table.setSelectionForeground(TEXT);
        table.setIntercellSpacing(new java.awt.Dimension(0, 0));
        table.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0xF1F5F9));
        header.setForeground(TEXT_MUTED);
        header.setFont(H3);
        header.setOpaque(true);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        header.setPreferredSize(new java.awt.Dimension(0, 38));
    }

    /** Encapsule un tableau dans un scroll pane avec le style « card ». */
    public static JScrollPane wrapTable(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBackground(SURFACE);
        scroll.getViewport().setBackground(SURFACE);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        return scroll;
    }

    // ====================================================================
    //  PASTILLES DE STATUT (badges)
    // ====================================================================

    /** Crée un libellé de type « badge » coloré (statut, type…). */
    public static JLabel badge(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setFont(SMALL.deriveFont(Font.BOLD));
        l.setBorder(new EmptyBorder(2, 8, 2, 8));
        return l;
    }

    // ====================================================================
    //  HELPERS DIVERS
    // ====================================================================

    /** Séparateur horizontal de la couleur de bordure. */
    public static JPanel hairline() {
        JPanel p = new JPanel();
        p.setBackground(BORDER);
        p.setPreferredSize(new java.awt.Dimension(1, 1));
        p.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, 1));
        return p;
    }

    /** Bordure compound : marge externe + ligne. */
    public static Border framed(int topPx, Color color) {
        return new MatteBorder(topPx, 0, 0, 0, color);
    }

    /** Indique qu'un composant est utilisable comme curseur main au survol. */
    public static void clickable(Component c) {
        c.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
    }
}
