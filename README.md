# 🧾 Système de Gestion de Stock et de Facturation

## 🛠️ Description

Ce projet a pour objectif de développer un système informatique complet pour la **gestion du stock de marchandises** et la **préparation des factures** des ventes effectuées par un vendeur.

Le système est conçu en **architecture client-serveur** et utilise le protocole de communication **RMI (Remote Method Invocation)** pour permettre l’interaction entre les clients et le serveur.

---

## 🎯 Fonctionnalités

- 📦 Gestion des produits en stock :
  - Ajout, modification et suppression de produits
  - Consultation du stock disponible
- 🧾 Gestion des ventes :
  - Création de factures clients
  - Mise à jour automatique du stock lors des ventes
- 🗃️ Persistance des données (stock et factures)
- 🔄 Communication client-serveur via RMI

---

## 🧱 Architecture

- **Backend (Serveur)** : Java avec RMI, gestion des données persistantes
- **Frontend (Client)** : Java RMI Client (ou possibilité de GUI avec JavaFX/Swing)
- **Base de données** : Fichier local / SGBD (au choix selon l'implémentation)
- **Communication** : Protocole RMI


##Prérequis Avant lancement 

- **Creer une base de données JDBC sur le port 3306 avec un outil comme XAMP
- **Après la connexion a JDBC créer les deux bases à l'aide des fichiers SQL founis dans le ZIP


## Lancement de l'application
- **Lancer le fichier SiegeRMI pour lancer le serveur du siège
- **Lancer le fichier MagasinRMI pour lancer le serveur du magasin
- **Lancer le client avec ClientApp

  Vous devriez ensuite voire l'interface du client








