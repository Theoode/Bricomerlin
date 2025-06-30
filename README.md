🧾 Système de Gestion de Stock et de Facturation
🛠️ Description
Ce projet a pour objectif de développer un système informatique complet pour la gestion du stock de marchandises et la préparation des factures des ventes effectuées par un vendeur.

Le système est conçu en architecture client-serveur et utilise le protocole de communication RMI (Remote Method Invocation) pour permettre l’interaction entre les clients et le serveur.

🎯 Fonctionnalités
📦 Gestion des produits en stock :

Ajout, modification et suppression de produits

Consultation du stock disponible

🧾 Gestion des ventes :

Création de factures clients

Mise à jour automatique du stock lors des ventes

🗃️ Persistance des données (stock et factures)

🔄 Communication client-serveur via RMI

🧱 Architecture
Backend (Serveur) : Java avec RMI, gestion des données persistantes

Frontend (Client) : Java RMI Client (ou possibilité de GUI avec JavaFX/Swing)

Base de données : Fichier local / SGBD (au choix selon l'implémentation)

Communication : Protocole RMI

🚀 Lancement de l'application
Créer la base de données :

Une instance JDBC doit être disponible en utilisateur root, avec mot de passe vide.

Tu dois exécuter les scripts SQL fournis pour initialiser les tables nécessaires.

Démarrer les différentes parties du système :

🏬 Serveur Magasin : lancer la classe MagasinRMI

🏢 Siège : lancer la classe SiegeRMI

👤 Client : lancer la classe ClientAPP
