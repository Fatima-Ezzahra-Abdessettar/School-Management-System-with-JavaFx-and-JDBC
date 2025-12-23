
# 🎓 School Management System (JavaFX + JDBC)

Application desktop de gestion académique développée en **JavaFX** avec **JDBC pur**, permettant la gestion des étudiants, filières, matières et dossiers administratifs.

---

## 🔐 Credentials de connexion

| Rôle  | Username | Password |
|-------|----------|----------|
| Admin | Admin    | admin123 |

---
## 🧪 Comment tester l’application

1. Importer le projet Java dans un IDE (IntelliJ / Eclipse)
2. S’assurer que :
    - JavaFX est correctement configuré
    - MySQL est installé et en cours d’exécution
3. Créer la base de données à l’aide du script SQL fourni dans le projet
4. Vérifier les paramètres de connexion JDBC (`url`, `username`, `password`)
5. Lancer la classe `Launcher.java`
6. Se connecter avec les identifiants **ADMIN**
7. Tester les modules :
    - Gestion des filières
    - Gestion des étudiants
    - Inscription aux matières
    - Dossier administratif

## Architecture

Le projet suit une **architecture en couches** pour une meilleure séparation des responsabilités :

```

model/        → Entités métier
dao/          → Accès aux données (JDBC)
controller/   → Contrôleurs JavaFX
view/         → Interfaces FXML
util/         → Connexion DB & utilitaires
MainApp.java  → Point d’entrée

```

- **JavaFX** : interface graphique multi-vues
- **DAO Pattern** : isolation de la logique SQL
- **JDBC** : accès direct et maîtrisé à la base de données

---

## ⚙️ Choix techniques

- **Java + JavaFX** pour une application desktop robuste
- **JDBC pur** avec `PreparedStatement`
- **Architecture modulaire** facilitant maintenance et évolution
- **Relations complexes** gérées côté métier et DAO

---

## ⚠️ Difficultés rencontrées

### 🔁 Gestion des transactions
- Certaines opérations (inscription, affectation de matières) nécessitent plusieurs actions dépendantes
- Mise en place de **transactions JDBC** (`commit / rollback`) pour garantir la cohérence

### 🔗 Refonte du modèle relationnel
- Passage d’une relation **One-To-Many** à **Many-To-Many**
- Ajout de tables d’association
- Adaptation des requêtes et DAO existants

---

## ✅ Fonctionnalités principales

- CRUD Filières, Étudiants, Matières
- Inscription des étudiants aux matières
- Dossier administratif unique par étudiant
- Contraintes métier respectées
- Interface claire et interactive

---

## Auteur

**Fatima-Ezzahra Abdessettar**  
Projet académique – ENSA Tanger