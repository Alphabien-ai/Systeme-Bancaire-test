# Système Bancaire ASJA — Java Swing (OpenJDK 11)

Application de gestion bancaire complète avec interface graphique Swing **responsive**,
conforme au diagramme de classes UML fourni, avec en plus le **client VIP** qui peut
retirer directement au guichet de la banque **même si son solde est insuffisant ou déjà négatif**.

## Lancer le projet

| Système | Commande |
|---|---|
| Windows | double-cliquer sur `lancer.bat` |
| Linux / macOS | `./lancer.sh` (ou `bash lancer.sh`) |
| Manuel | `javac -encoding UTF-8 -d bin -sourcepath src src/Lancer.java` puis `java -cp bin Lancer` |

Identifiants de connexion : **admin / admin**
(ou `java -cp bin Lancer --sans-connexion` pour sauter l'écran de connexion)

Les données sont sauvegardées automatiquement dans `banque_donnees.dat` (dossier de lancement).
Supprimez ce fichier (ou utilisez *Rapports > Réinitialiser les données*) pour revenir aux données de démonstration.

**IDE (NetBeans / IntelliJ / Eclipse)** : créer un projet Java, copier le contenu de `src/`
dans le dossier source, encodage UTF-8, classe principale : `Lancer`.

## Structure (un fichier par classe)

```
src/
├── Lancer.java                     <- point d'entrée (main)
├── modele/                         <- classes du diagramme UML
│   ├── CompteBancaire.java         (abstraite)
│   ├── CompteCourant.java          (hérite de CompteBancaire)
│   ├── CompteEpargne.java          (hérite de CompteBancaire)
│   ├── Client.java
│   ├── ClientVIP.java              (hérite de Client : retrait guichet)
│   ├── Agence.java
│   ├── Banque.java
│   ├── OperationBancaire.java
│   ├── CarteBancaire.java
│   └── ServiceBancaire.java
├── exception/
│   ├── BanqueException.java
│   └── SoldeInsuffisantException.java
├── service/
│   ├── BanqueService.java          <- règles métier + validation
│   ├── Persistance.java            <- sauvegarde (sérialisation)
│   └── DonneesDemo.java            <- données de démonstration
├── util/
│   └── Formatage.java              <- montants en Ariary, dates
└── vue/                            <- interface graphique Swing
    ├── FenetreConnexion.java, FenetrePrincipale.java, BarreLaterale.java
    ├── PanneauBase.java, PanneauTableauBord.java, PanneauClients.java
    ├── PanneauComptes.java, PanneauOperations.java, PanneauCartes.java
    ├── PanneauAgences.java, PanneauServices.java, PanneauRapport.java
    ├── Theme.java, Messages.java, Rafraichissable.java
    └── composants/
        ├── GrilleResponsive.java   <- layout responsive (colonnes auto)
        ├── WrapLayout.java         <- barres d'outils qui passent à la ligne
        ├── TableModerne.java       <- table qui masque les colonnes secondaires
        ├── PanneauDefilant.java, Formulaire.java, DialogueFormulaire.java
        └── BoutonModerne.java, Carte.java, CarteStatistique.java,
            ChampTexte.java, IconeLettre.java, ModeleTable.java
```

## Règle VIP (retrait au guichet)

* Retrait standard : compte courant -> `solde - montant >= -découvert` ; compte épargne -> `solde - montant >= minimum`.
* Si le retrait standard est refusé et que le titulaire est **VIP**, l'application propose
  automatiquement le **retrait direct au guichet**.
* Le retrait guichet VIP est accepté même si le solde est déjà négatif, tant que :
  `solde - montant >= -(découvert du compte + plafond de crédit VIP)`.
* L'opération est tracée comme `RETRAIT GUICHET VIP` (couleur or dans les tableaux).

Exemple de démonstration : *Fara RAMANANTSOA* (VIP, compte CC-000007) est déjà à
-1 550 000 Ar et peut encore retirer au guichet.

## Responsive

* ≥ 1100 px : menu latéral complet — 760 à 1100 px : menu en icônes — < 760 px : menu masqué (bouton ☰ / Ctrl+M).
* Indicateurs, formulaires et panneaux se réorganisent en 1, 2, 3… colonnes selon la largeur.
* Les tables masquent automatiquement les colonnes secondaires sur les petites largeurs.
* Raccourcis : Ctrl+1 … Ctrl+8 pour changer d'écran.
