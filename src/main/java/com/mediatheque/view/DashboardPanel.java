package com.mediatheque.view;

import com.mediatheque.dao.StatsDAO;
import com.mediatheque.util.UITheme;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

/**
 * Tableau de bord : premier écran après la connexion.
 * Affiche six compteurs (clients, techniciens, salles, animations,
 * réservations, factures) alimentés par une seule requête SQL (StatsDAO).
 */
public class DashboardPanel extends JPanel {

    /** Icônes Unicode associées aux libellés des compteurs. */
    private static final Map<String, String> ICONES;
    static {
        ICONES = new HashMap<>();
        ICONES.put("Clients", "C");
        ICONES.put("Techniciens", "T");
        ICONES.put("Salles", "S");
        ICONES.put("Animations", "A");
        ICONES.put("Réservations", "R");
        ICONES.put("Factures", "F");
    }

    public DashboardPanel() {
        setLayout(new BorderLayout(0, 24));
        setBackground(UITheme.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        add(creerEntete(), BorderLayout.NORTH);
        add(creerCorps(), BorderLayout.CENTER);
    }

    /** En-tête de la page : titre + sous-titre + ligne séparatrice. */
    private JPanel creerEntete() {
        JPanel entete = new JPanel();
        entete.setOpaque(false);
        entete.setLayout(new BoxLayout(entete, BoxLayout.Y_AXIS));

        JLabel titre = UITheme.title("Tableau de bord");
        titre.setAlignmentX(Component.LEFT_ALIGNMENT);
        entete.add(titre);

        JLabel sous = UITheme.subtitle("Vue d'ensemble des données de la médiathèque");
        sous.setAlignmentX(Component.LEFT_ALIGNMENT);
        sous.setBorder(new EmptyBorder(4, 0, 0, 0));
        entete.add(sous);

        return entete;
    }

    /** Corps : grille de compteurs (ou message d'erreur si la BD est injoignable). */
    private JPanel creerCorps() {
        JPanel corps = new JPanel(new BorderLayout(0, 16));
        corps.setOpaque(false);

        // Grille 2 lignes x 3 colonnes pour les 6 compteurs
        JPanel grille = new JPanel(new GridLayout(2, 3, 18, 18));
        grille.setOpaque(false);

        try {
            // On utilise LinkedHashMap pour préserver l'ordre des compteurs
            Map<String, Integer> compteurs = new StatsDAO().compteurs();
            Map<String, Integer> ordonnes = new LinkedHashMap<>(compteurs);
            for (Map.Entry<String, Integer> e : ordonnes.entrySet()) {
                grille.add(carte(e.getKey(), e.getValue()));
            }
        } catch (RuntimeException ex) {
            // Base inaccessible : on remplace la grille par un message clair
            JPanel err = UITheme.card();
            err.setLayout(new BorderLayout());
            JLabel msg = new JLabel("<html><div style='text-align:center;'>"
                    + "<b>Connexion à la base impossible.</b><br>"
                    + "Vérifiez que MySQL est démarré.</div></html>",
                    SwingConstants.CENTER);
            msg.setFont(UITheme.NORMAL);
            err.add(msg, BorderLayout.CENTER);
            corps.add(err, BorderLayout.NORTH);
            return corps;
        }

        corps.add(grille, BorderLayout.NORTH);

        // Pied de page : copyright discret
        JPanel pied = new JPanel(new BorderLayout());
        pied.setOpaque(false);
        pied.setBorder(new EmptyBorder(20, 0, 0, 0));
        JLabel sig = new JLabel(
                "Médiathèque de Bourg-la-Reine — SEBAH Nassim — BTS SIO SLAM 2026",
                SwingConstants.CENTER);
        sig.setForeground(UITheme.TEXT_MUTED);
        sig.setFont(UITheme.SMALL);
        pied.add(sig, BorderLayout.SOUTH);
        corps.add(pied, BorderLayout.SOUTH);

        // Pousse la grille vers le haut
        corps.add(Box.createVerticalGlue(), BorderLayout.CENTER);

        return corps;
    }

    /** Une carte compteur : icône, libellé, grand nombre. */
    private JPanel carte(String libelle, int valeur) {
        JPanel carte = new JPanel(new BorderLayout(0, 10));
        carte.setBackground(UITheme.SURFACE);
        carte.setBorder(new CompoundBorder(
                new UITheme.RoundedBorder(UITheme.BORDER_LIGHT, 12, 1),
                new EmptyBorder(20, 22, 20, 22)));
        carte.setPreferredSize(new Dimension(0, 130));

        // Ligne du haut : icône colorée + libellé
        JPanel haut = new JPanel(new BorderLayout(12, 0));
        haut.setOpaque(false);

        String code = ICONES.getOrDefault(libelle, libelle.substring(0, 1));
        IconPastille icone = new IconPastille(code);
        haut.add(icone, BorderLayout.WEST);

        JLabel nom = new JLabel(libelle);
        nom.setFont(UITheme.H3);
        nom.setForeground(UITheme.TEXT_MUTED);
        haut.add(nom, BorderLayout.CENTER);

        carte.add(haut, BorderLayout.NORTH);

        // Le grand nombre au centre / bas
        JLabel nombre = new JLabel(String.valueOf(valeur));
        nombre.setFont(UITheme.BIG_NUMBER);
        nombre.setForeground(UITheme.TEXT);
        carte.add(nombre, BorderLayout.CENTER);

        return carte;
    }

    /** Pastille verte arrondie contenant une lettre (icône simple). */
    private static final class IconPastille extends JComponent {
        private final String texte;

        IconPastille(String texte) {
            this.texte = texte;
            setPreferredSize(new Dimension(36, 36));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(UITheme.PRIMARY_CLAIR);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.setColor(UITheme.PRIMARY_FONCE);
            g2.setFont(UITheme.H2);
            int sw = g2.getFontMetrics().stringWidth(texte);
            int sh = g2.getFontMetrics().getAscent();
            g2.drawString(texte, (getWidth() - sw) / 2,
                    (getHeight() + sh) / 2 - 3);
            g2.dispose();
        }
    }
}
