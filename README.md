# MCP Spring Server - Task Management with AI 🤖

Serveur MCP (Model Context Protocol) **intelligent** pour la gestion de tâches, construit avec **Spring Boot 3.4**, **Spring AI** et **Java 21**.

## ✨ Fonctionnalités Principales

### 🛠️ Outils MCP Basiques

1. **create_task** - Créer une nouvelle tâche
2. **list_tasks** - Lister toutes les tâches (avec filtres)
3. **update_task** - Mettre à jour une tâche existante
4. **delete_task** - Supprimer une tâche
5. **search_tasks** - Rechercher des tâches par mot-clé

### 🤖 Outils AI Avancés

6. **analyze_task_sentiment** - Analyse du sentiment avec AI
7. **suggest_task_priority** - Suggestion automatique de priorité
8. **generate_task_summary** - Génération de résumés intelligents
9. **suggest_task_tags** - Suggestions de tags pertinents
10. **detect_task_risks** - Détection de risques et blocages
11. **smart_create_task** - Création avec auto-suggestions AI

### 📚 Ressources MCP

- `task://all` - Toutes les tâches (JSON)
- `task://{id}` - Tâche spécifique par ID
- `task://status/{status}` - Tâches par statut
- `task://priority/{priority}` - Tâches par priorité
- `task://summary` - Résumé global

### 💬 Prompts MCP

- **summarize_tasks** - Résumé complet avec insights
- **suggest_next_task** - Suggestion intelligente de la prochaine tâche
- **analyze_productivity** - Analyse de productivité
- **group_related_tasks** - Regroupement par thèmes

## 📋 Prérequis

- Java 21 (utilise toolchain Gradle)
- Gradle 8.x
- (Optionnel) Clé API OpenAI pour fonctionnalités AI

## 🛠️ Installation

```bash
# Compiler le projet
./gradlew build

# Lancer l'application
./gradlew bootRun
```

## 🔧 Configuration

### Application Properties

Configurez les propriétés dans `src/main/resources/application.yml`:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:demo-key}
```

### Variable d'environnement

```bash
export OPENAI_API_KEY=your-api-key
```

## 📚 Utilisation

### 1. Démarrer le serveur

```bash
./gradlew bootRun
```

### 2. Configurer le client MCP

Dans votre client MCP (comme Claude Desktop), ajoutez la configuration :

```json
{
  "mcpServers": {
    "task-management": {
      "command": "java",
      "args": [
        "-jar",
        "/chemin/vers/mcp-spring-server/build/libs/mcp-spring-server-0.0.1-SNAPSHOT.jar"
      ],
      "transport": "stdio"
    }
  }
}
```

### 3. Exemples d'utilisation

**Créer une tâche:**
```
Créer une tâche "Apprendre MCP" avec une priorité HIGH
```

**Lister les tâches:**
```
Montre-moi toutes mes tâches en cours
```

**Rechercher:**
```
Trouve toutes les tâches contenant "MCP"
```

## 🏗️ Architecture

```
src/main/java/com/example/mcpserver/
├── McpServerApplication.java          # Point d'entrée
├── config/
│   └── McpConfig.java                 # Configuration MCP
├── model/
│   └── Task.java                      # Modèle de données
├── repository/
│   └── TaskRepository.java            # Accès données
├── service/
│   └── TaskService.java               # Logique métier
└── mcp/
    └── TaskManagementTools.java       # Outils MCP
```

## 🔍 Console H2

Base de données accessible via :
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:taskdb`
- Username: `sa`
- Password: (vide)

## 🧪 Tests

```bash
# Exécuter les tests
./gradlew test

# Rapport de couverture
./gradlew jacocoTestReport
```

## 📖 Apprendre MCP

Ce projet illustre les concepts complets du protocole MCP :

### Concepts de base
- ✅ **Tools** - Déclaration avec `@McpTool` et paramètres `@McpSchema`
- ✅ **Resources** - Exposition de données avec `@McpResource` et URIs
- ✅ **Prompts** - Interactions pré-configurées avec `@McpPrompt`

### Intégrations avancées
- ✅ **Spring AI** - Chat client et modèles de langage
- ✅ **AI Analysis** - Sentiment, priorités, tags automatiques
- ✅ **Intelligence** - Détection de risques, suggestions contextuelles
- ✅ **Java 21** - Utilisation de la toolchain moderne
- ✅ **Transport stdio** - Communication MCP standard
🎯 Exemples d'utilisation AI

### Création intelligente
```
Utilise smart_create_task pour créer "Refactorer le code legacy" avec description "Le code doit être modernisé pour Java 21"
```
→ AI suggère automatiquement: priorité HIGH, tags "refactoring, java, legacy"

### Analyse de sentiment
```
Analyse le sentiment de la tâche #5
```
→ AI détecte si la description est positive, négative ou neutre

### Suggestion de prochaine tâche
```
Quelle tâche devrais-je faire maintenant ?
```
→ AI suggère basé sur priorité, statut et date de création

### Analyse de productivité
```
Montre-moi mon analyse de productivité
```
→ Statistiques, taux de complétion, insights

## 📝 Évolutions possibles

1. ✅ ~~Ajouter des Resources MCP~~ - Fait ✓
2. ✅ ~~Implémenter des Prompts MCP~~ - Fait ✓
3. ✅ ~~Ajouter analyse de sentiment~~ - Fait ✓
4. ✅ ~~Suggestions automatiques de priorités~~ - Fait ✓
5. ⬜ Sauvegarder vers PostgreSQL
6. ⬜ Notifications par webhook
7. ⬜ Intégration calendrier (Google Calendar, Outlook)
8. ⬜ Export vers Jira/Trello
9. ⬜ Graphiques de productivité
10. ⬜ Collaboration multi-utilisateurs tests

## 🔗 Ressources

- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [MCP Specification](https://modelcontextprotocol.io/)
- [Spring Boot 3.4](https://spring.io/projects/spring-boot)

## 📝 Prochaines étapes

1. Ajouter des **Resources MCP** pour exposer les tâches
2. Implémenter des **Prompts MCP** pour analyse AI
3. Ajouter analyse de sentiment avec Spring AI
4. Intégrer des suggestions automatiques de priorités
5. Sauvegarder vers PostgreSQL

## 📄 Licence

MIT
