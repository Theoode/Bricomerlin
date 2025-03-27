package common;

import java.io.Serializable;

public class Article implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idReference;
    private double prixUnitaire;
    private int enStock;
    private Famille famille;  // Relation avec `Famille`

    // 🔹 Constructeur
    public Article(String idReference, double prixUnitaire, int enStock, Famille famille) {
        this.idReference = idReference;
        this.prixUnitaire = prixUnitaire;
        this.enStock = enStock;
        this.famille = famille;
    }

    // 🔹 Getters et Setters
    public String getIdReference() { return idReference; }
    public void setIdReference(String idReference) { this.idReference = idReference; }

    public double getPrixUnitaire() { return prixUnitaire; }
    public void setPrixUnitaire(double prixUnitaire) { this.prixUnitaire = prixUnitaire; }

    public int getEnStock() { return enStock; }
    public void setEnStock(int enStock) { this.enStock = enStock; }

    public Famille getFamille() { return famille; }
    public void setFamille(Famille famille) { this.famille = famille; }

    @Override
    public String toString() {
        return "Article{" + "idReference='" + idReference + "', prixUnitaire=" + prixUnitaire +
                ", enStock=" + enStock + ", famille=" + famille + "}";
    }
}
