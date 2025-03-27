package common;

import java.io.Serializable;

public class ListeArticleFacture implements Serializable {
    private static final long serialVersionUID = 1L;

    private Article article;
    private Facture facture;

    // 🔹 Constructeur
    public ListeArticleFacture(Article article, Facture facture) {
        this.article = article;
        this.facture = facture;
    }

    // 🔹 Getters et Setters
    public Article getArticle() { return article; }
    public void setArticle(Article article) { this.article = article; }

    public Facture getFacture() { return facture; }
    public void setFacture(Facture facture) { this.facture = facture; }

    @Override
    public String toString() {
        return "ListeArticleFacture{" + "article=" + article + ", facture=" + facture + "}";
    }
}
