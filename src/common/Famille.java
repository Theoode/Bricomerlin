package common;

import java.io.Serializable;

public class Famille implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idFamille;
    private String nomFamille;

    // 🔹 Constructeur
    public Famille(int idFamille, String nomFamille) {
        this.idFamille = idFamille;
        this.nomFamille = nomFamille;
    }

    // 🔹 Getters et Setters
    public int getIdFamille() { return idFamille; }
    public void setIdFamille(int idFamille) { this.idFamille = idFamille; }

    public String getNomFamille() { return nomFamille; }
    public void setNomFamille(String nomFamille) { this.nomFamille = nomFamille; }

    @Override
    public String toString() {
        return "Famille{" + "idFamille=" + idFamille + ", nomFamille='" + nomFamille + "'}";
    }
}
