-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : localhost
-- Généré le : lun. 30 juin 2025 à 16:24
-- Version du serveur : 10.4.28-MariaDB
-- Version de PHP : 8.0.28

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `bricomerlin`
--

-- --------------------------------------------------------

--
-- Structure de la table `article`
--

CREATE TABLE `article` (
  `idReference` varchar(50) NOT NULL,
  `prixUnitaire` double NOT NULL,
  `enStock` int(11) DEFAULT NULL,
  `nom` varchar(100) DEFAULT NULL,
  `idFamille` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `article`
--

INSERT INTO `article` (`idReference`, `prixUnitaire`, `enStock`, `nom`, `idFamille`) VALUES
('A567', 9.99, 14, 'Ampoule LED', 2),
('C234', 7.99, 38, 'Câble électrique', 2),
('M123', 12.99, 11, 'Marteau', 1),
('P789', 22.99, 8, 'Pot de peinture', 3),
('T456', 5, 33, 'Tournevis', 1);

-- --------------------------------------------------------

--
-- Structure de la table `article_commande`
--

CREATE TABLE `article_commande` (
  `idReference` varchar(50) NOT NULL,
  `id_commande` int(11) NOT NULL,
  `quantite` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `article_commande`
--

INSERT INTO `article_commande` (`idReference`, `id_commande`, `quantite`) VALUES
('M123', 1, 3),
('A567', 1, 1),
('M123', 2, 2),
('A567', 2, 4),
('A567', 3, 2),
('C234', 3, 4),
('P789', 4, 1);

-- --------------------------------------------------------

--
-- Structure de la table `commandes`
--

CREATE TABLE `commandes` (
  `id_commande` int(11) NOT NULL,
  `total_prix` decimal(15,2) DEFAULT NULL,
  `statut_paiement` varchar(255) NOT NULL,
  `date_commande` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `commandes`
--

INSERT INTO `commandes` (`id_commande`, `total_prix`, `statut_paiement`, `date_commande`) VALUES
(1, 60.96, 'Payée', '2025-06-18 14:32:18'),
(2, 71.96, 'Payée', '2025-06-18 20:24:35'),
(3, 41.98, 'Payée', '2025-06-30 14:16:34'),
(4, 22.99, 'Payée', '2025-06-30 14:18:03');

-- --------------------------------------------------------

--
-- Structure de la table `famille`
--

CREATE TABLE `famille` (
  `idFamille` int(11) NOT NULL,
  `nomFamille` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `famille`
--

INSERT INTO `famille` (`idFamille`, `nomFamille`) VALUES
(1, 'Outillage'),
(2, 'Électricité'),
(3, 'Peinture');

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `commandes`
--
ALTER TABLE `commandes`
  ADD PRIMARY KEY (`id_commande`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `commandes`
--
ALTER TABLE `commandes`
  MODIFY `id_commande` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
