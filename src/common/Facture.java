package common;

import java.io.Serializable;
import java.util.Date;

public class Facture implements Serializable {
    private static final long serialVersionUID = 1L;

    private int idFacture;
    private Date dateFacture;
    private double montant;
    private String modePaiement;

    // 🔹 Constructeur
    public Facture(int idFacture, Date dateFacture, double montant, String modePaiement) {
        this.idFacture = idFacture;
        this.dateFacture = dateFacture;
        this.montant = montant;
        this.modePaiement = modePaiement;
    }

    // 🔹 Getters et Setters
    public int getIdFacture() { return idFacture; }
    public void setIdFacture(int idFacture) { this.idFacture = idFacture; }

    public Date getDateFacture() { return dateFacture; }
    public void setDateFacture(Date dateFacture) { this.dateFacture = dateFacture; }

    public double getMontant() { return montant; }
    public void setMontant(double montant) { this.montant = montant; }

    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }

    @Override
    public String toString() {
        return "Facture{" + "idFacture=" + idFacture + ", dateFacture=" + dateFacture +
                ", montant=" + montant + ", modePaiement='" + modePaiement + "'}";
    }
}
