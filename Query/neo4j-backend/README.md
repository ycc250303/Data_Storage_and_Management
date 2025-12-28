# neo4j-backend

Minimal Express + Neo4j backend template for the Data_Storage_and_Management project.

Quick start

1. Copy `.env.example` to `.env` and fill credentials (do not commit `.env`).
2. Install dependencies:

```bash
cd Data_Storage_and_Management/neo4j-backend
npm install
```

3. Start server:

```bash
npm start
# or for development:
npm run dev
```

Endpoints
- `GET /api/neo4j/actor/:name` — list movies by actor  
- `GET /api/neo4j/movie/:id` — movie details (actors, directors, genres)  
- `GET /api/neo4j/genre/:name` — movies by genre  
- `GET /api/neo4j/relation?from=ActorA&to=ActorB&maxDepth=4` — shortest collaboration path  
- `GET /api/neo4j/stats/collaborations` — top actor collaborations  

Notes
- Use this local template for development and integration with the front-end at `fronted/`.  
- Keep credentials out of source control; use environment variables.
Importing CSVs into Neo4j (example)
-----------------------------------

This project expects ETL-produced CSVs (example: `final_movie_info.csv`, `reviews.csv`) to be available for import.
Put your CSV files into the Neo4j import directory (for local Docker setup using the quickstart in this repo, that's typically `%USERPROFILE%\neo4j\import` on Windows or `~/neo4j/import` on Linux/macOS). Then run the following Cypher examples in Neo4j Browser (`http://localhost:7474`) or via a session.

Assumptions about `final_movie_info.csv` (adjust field names if your CSV differs):
- `movie_id` — unique id for the movie (string)
- `movie_title` — title
- `release_year` — integer year (or empty)
- `review_count` — integer (optional)
- `actors` — comma-separated actor names (e.g. "Tom Hanks, Meg Ryan")
- `directors` — comma-separated director names
- `genres` — comma-separated genre names

1) Import Movie nodes

```cypher
LOAD CSV WITH HEADERS FROM 'file:///final_movie_info.csv' AS row
MERGE (m:Movie {movie_id: row.movie_id})
SET m.movie_title = row.movie_title,
    m.release_year = CASE WHEN row.release_year='' OR row.release_year IS NULL THEN NULL ELSE toInteger(row.release_year) END,
    m.review_count = CASE WHEN row.review_count='' OR row.review_count IS NULL THEN 0 ELSE toInteger(row.review_count) END;
```

2) Import Actors and ACTED relationships (actors column is comma-separated)

```cypher
LOAD CSV WITH HEADERS FROM 'file:///final_movie_info.csv' AS row
WITH row, CASE WHEN row.actors IS NULL THEN [] ELSE [a IN split(row.actors, ',') | trim(a)] END AS actorNames
MERGE (m:Movie {movie_id: row.movie_id})
FOREACH (name IN actorNames |
  MERGE (a:Actor {actor_name: name})
  MERGE (a)-[:ACTED]->(m)
);
```

3) Import Directors and DIRECTED relationships

```cypher
LOAD CSV WITH HEADERS FROM 'file:///final_movie_info.csv' AS row
WITH row, CASE WHEN row.directors IS NULL THEN [] ELSE [d IN split(row.directors, ',') | trim(d)] END AS directorNames
MERGE (m:Movie {movie_id: row.movie_id})
FOREACH (name IN directorNames |
  MERGE (d:Director {director_name: name})
  MERGE (d)-[:DIRECTED]->(m)
);
```

4) Import Genres and HAS_GENRE relationships

```cypher
LOAD CSV WITH HEADERS FROM 'file:///final_movie_info.csv' AS row
WITH row, CASE WHEN row.genres IS NULL THEN [] ELSE [g IN split(row.genres, ',') | trim(g)] END AS gnames
MERGE (m:Movie {movie_id: row.movie_id})
FOREACH (g IN gnames |
  MERGE (gg:Genre {genre_name: g})
  MERGE (m)-[:HAS_GENRE]->(gg)
);
```

5) (Optional) Import Reviews as relationships (if you have `reviews.csv` with product/movie mapping)

If you produced `reviews.csv` including `product_id` (maps to `movie_id`) and `user_id`, `score`, `time`:

```cypher
LOAD CSV WITH HEADERS FROM 'file:///reviews.csv' AS row
MERGE (m:Movie {movie_id: row.product_id})
MERGE (u:User {user_id: row.user_id})
MERGE (u)-[r:RATED]->(m)
SET r.score = toFloat(row.score), r.time = row.time;
```

Troubleshooting
- CSV not found: ensure file is in Neo4j import dir visible to the server/container and use `file:///filename.csv` (case-sensitive).
- Encoding issues: ensure UTF-8 encoding; if you see weird characters, re-save CSV as UTF-8.
- Large files: consider importing in batches or using programmatic import tools.

After import you can run verification queries in Neo4j Browser, for example:

```cypher
MATCH (a:Actor {actor_name:'Tom Hanks'})-[:ACTED]->(m:Movie) RETURN m LIMIT 25;
MATCH (a:Actor)-[:ACTED]->(m:Movie)<-[:ACTED]-(b:Actor) RETURN a.actor_name, b.actor_name, COUNT(m) AS collab ORDER BY collab DESC LIMIT 20;
```

