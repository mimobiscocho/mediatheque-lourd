package com.mediatheque.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Modèle représentant une animation (liée à une salle et un technicien).
 */
public class Animation {

    private int id;
    private String titre;
    private String description;
    private LocalDate dateAnimation;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private int nbPlaces;
    private int salleId;
    private int technicienId;

    // Champs dénormalisés pour l'affichage (libellés joints)
    private String salleNom;
    private String technicienNom;

    public Animation() {
        this.dateAnimation = LocalDate.now();
        this.nbPlaces = 10;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDateAnimation() {
        return dateAnimation;
    }

    public void setDateAnimation(LocalDate dateAnimation) {
        this.dateAnimation = dateAnimation;
    }

    public LocalTime getHeureDebut() {
        return heureDebut;
    }

    public void setHeureDebut(LocalTime heureDebut) {
        this.heureDebut = heureDebut;
    }

    public LocalTime getHeureFin() {
        return heureFin;
    }

    public void setHeureFin(LocalTime heureFin) {
        this.heureFin = heureFin;
    }

    public int getNbPlaces() {
        return nbPlaces;
    }

    public void setNbPlaces(int nbPlaces) {
        this.nbPlaces = nbPlaces;
    }

    public int getSalleId() {
        return salleId;
    }

    public void setSalleId(int salleId) {
        this.salleId = salleId;
    }

    public int getTechnicienId() {
        return technicienId;
    }

    public void setTechnicienId(int technicienId) {
        this.technicienId = technicienId;
    }

    public String getSalleNom() {
        return salleNom;
    }

    public void setSalleNom(String salleNom) {
        this.salleNom = salleNom;
    }

    public String getTechnicienNom() {
        return technicienNom;
    }

    public void setTechnicienNom(String technicienNom) {
        this.technicienNom = technicienNom;
    }

    @Override
    public String toString() {
        return titre;
    }
}
