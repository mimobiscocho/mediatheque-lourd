package com.mediatheque.model;

/**
 * Modèle représentant un profil d'agent (compte de connexion).
 */
public class Profil {

    private int id;
    private String login;
    private String motDePasse; // empreinte SHA-256
    private String nom;
    private String prenom;
    private String role; // ADMIN ou AGENT

    public Profil() {
    }

    public Profil(int id, String login, String nom, String prenom, String role) {
        this.id = id;
        this.login = login;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean estAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return prenom + " " + nom + " (" + role + ")";
    }
}
