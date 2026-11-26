# Kanban — como montar o GitHub Project

1. No GitHub, crie um **Project** (Board) ligado ao repositório, com as colunas **Backlog**, **Em andamento**, **Em revisão**, **Concluído**.
2. Importe as 15 issues prontas (`issues-missao-marte.json`) com o importador do professor:
   ```bash
   cd kanban/github-issues-importer
   python server.py
   ```
   Abra http://127.0.0.1:4173/, carregue o JSON, confira o owner/repo e use um **Personal Access Token seu** (fine-grained, com Issues e Projects: read and write). **Nunca commite o token.**
3. Atribua cada issue a um integrante e mova as issues conforme o trabalho realmente andar. Feche uma issue só depois que outro integrante revisar.
