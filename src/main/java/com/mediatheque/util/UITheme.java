package com.mediatheque.util;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;

/**
 * Charte graphique de l'application (couleurs, polices, helpers de style).
 * Respecte l'identité institutionnelle de la médiathèque.
 */
public final class UITheme {

    public static final Color PRIMARY    = new Color(0x1F4E79); // bleu institutionnel
    public static final Color PRIMARY_DK = new Color(0x163A5A);
    public static final Color ACCENT     = new Color(0xE67E22); // orange
    public static final Color BACKGROUND = new Color(0xF4F6F8);
    public static final Color WHITE      = Color.WHITE;
    public static final Color TEXT       = new Color(0x2C3E50);
    public static final Color DANGER     = new Color(0xC0392B);
    public static final Color SUCCESS    = new Color(0x27AE60);

    public static final Font TITLE  = new Font("SansSerif", Font.BOLD, 22);
    public static final Font H2     = new Font("SansSerif", Font.BOLD, 16);
    public static final Font NORMAL = new Font("SansSerif", Font.PLAIN, 14);

    private UITheme() {
    }

    /** Applique le style d'un bouton principal. */
    public static void stylePrimaryButton(JButton b) {
        styleButton(b, PRIMARY, WHITE);
    }

    /** Applique le style d'un bouton d'accent (action secondaire). */
    public static void styleAccentButton(JButton b) {
        styleButton(b, ACCENT, WHITE);
    }

    /** Applique le style d'un bouton de suppression / danger. */
    public static void styleDangerButton(JButton b) {
        styleButton(b, DANGER, WHITE);
    }

    private static void styleButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(NORMAL);
        b.setBorderPainted(false);
        b.setOpaque(true);
    }

    /** Crée un libellé de titre stylisé. */
    public static JLabel title(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(TITLE);
        l.setForeground(PRIMARY);
        return l;
    }

    /** Applique un style homogène à un tableau de données. */
    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setFont(NORMAL);
        table.setGridColor(new Color(0xDDDDDD));
        table.setSelectionBackground(new Color(0xCFE2F3));
        table.setSelectionForeground(TEXT);
        JTableHeader header = table.getTableHeader();
        header.setBackground(PRIMARY);
        // Texte blanc sur fond bleu institutionnel (contraste WCAG AA).
        header.setForeground(WHITE);
        header.setFont(H2);
        header.setOpaque(true);
    }
}
