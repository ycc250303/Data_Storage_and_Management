/**
 * migrate_local_csv_to_neo4j.js
 *
 * Reads CSV files from ../mysql-files and imports into Neo4j using neo4j-driver.
 * Usage:
 *   - copy your CSVs into Data_Storage_and_Management/mysql-files
 *   - configure .env with NEO4J_URI/USER/PASSWORD
 *   - run: node migrate_local_csv_to_neo4j.js
 *
 * Expects file final_movie_info.csv with headers including:
 *   movie_id,movie_title,release_year,review_count,actors,directors,genres
 */
const fs = require('fs');
const path = require('path');
const { parse } = require('csv-parse');
const neo4j = require('neo4j-driver');
require('dotenv').config();

const CSV_DIR = path.join(__dirname, '..', 'mysql-files');
const MOVIES_CSV = path.join(CSV_DIR, 'final_movie_info.csv');

const NEO4J_URI = process.env.NEO4J_URI || 'bolt://localhost:7687';
const NEO4J_USER = process.env.NEO4J_USER || 'neo4j';
const NEO4J_PASSWORD = process.env.NEO4J_PASSWORD || 'change_me';

const driver = neo4j.driver(NEO4J_URI, neo4j.auth.basic(NEO4J_USER, NEO4J_PASSWORD));

async function importMoviesFromCsv(csvPath) {
  if (!fs.existsSync(csvPath)) {
    console.error(`CSV file not found: ${csvPath}`);
    process.exit(1);
  }

  const parser = fs.createReadStream(csvPath).pipe(parse({
    columns: true,
    skip_empty_lines: true,
    relax_quotes: true
  }));

  const session = driver.session({ defaultAccessMode: neo4j.session.WRITE });
  let count = 0;
  try {
    for await (const row of parser) {
      // Normalize fields
      const movie_id = row.movie_id || row.product_id || row.asin || row.movieID || row.id;
      if (!movie_id) continue;
      const movie_title = row.movie_title || row.title || '';
      const release_year = row.release_year ? parseInt(row.release_year, 10) : null;
      const review_count = row.review_count ? parseInt(row.review_count, 10) : 0;
      const actors = (row.actors || row.actor || '').split(',').map(s => s.trim()).filter(Boolean);
      const directors = (row.directors || row.director || '').split(',').map(s => s.trim()).filter(Boolean);
      const genres = (row.genres || row.genre || '').split(',').map(s => s.trim()).filter(Boolean);

      const params = {
        movie_id, movie_title, release_year, review_count,
        actors, directors, genres
      };

      const cypher = `
      MERGE (m:Movie {movie_id:$movie_id})
      SET m.movie_title = $movie_title,
          m.release_year = $release_year,
          m.review_count = $review_count
      WITH m
      UNWIND $actors AS actorName
        MERGE (a:Actor {actor_name: actorName})
        MERGE (a)-[:ACTED]->(m)
      WITH m
      UNWIND $directors AS directorName
        MERGE (d:Director {director_name: directorName})
        MERGE (d)-[:DIRECTED]->(m)
      WITH m
      UNWIND $genres AS g
        MERGE (gg:Genre {genre_name: g})
        MERGE (m)-[:HAS_GENRE]->(gg)
      RETURN m.movie_id AS id
      `;

      try {
        await session.writeTransaction(tx => tx.run(cypher, params));
        count++;
        if (count % 500 === 0) {
          console.log(`Imported ${count} movies...`);
        }
      } catch (e) {
        console.error(`Error importing movie ${movie_id}: ${e.message}`);
      }
    }
    console.log(`Finished importing ${count} movies.`);
  } finally {
    await session.close();
  }
}

async function main() {
  try {
    console.log('Starting import from', MOVIES_CSV);
    await importMoviesFromCsv(MOVIES_CSV);
  } catch (err) {
    console.error('Fatal error:', err);
  } finally {
    await driver.close();
  }
}

if (require.main === module) main();


