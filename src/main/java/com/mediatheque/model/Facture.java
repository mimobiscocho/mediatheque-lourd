package com.mediatheque.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modèle représentant une facture émise pour un client.
 */
public class Facture {

    private int id;
    private int clientId;
    private String libelle;
    private BigDecimal montant;
    private LocalDate dateEmission;
    private String statut; // PAYEE ou IMPAYEE

    private String clientNom; // dénormalisé pour l'affichage

    public Facture() {
        this.dateEmission = LocalDate.now();
        this.statut = "IMPAYEE";
        this.montant = BigDecimal.ZERO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public LocalDate getDateEmission() {
        return dateEmission;
    }

    public void setDateEmission(LocalDate dateEmission) {
        this.dateEmission = dateEmission;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getClientNom() {
        return clientNom;
    }

    public void setClientNom(String clientNom) {
        this.clientNom = clientNom;
    }

    @Override
    public String toString() {
        return libelle + " - " + montant + " €";
    }
}
