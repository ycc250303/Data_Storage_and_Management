/**
 * Minimal Express + Neo4j backend template
 * - Uses environment variables for connection details (see .env.example)
 * - Exposes a few read-only endpoints used by the front-end:
 *   GET /api/neo4j/actor/:name
 *   GET /api/neo4j/movie/:id
 *   GET /api/neo4j/genre/:name
 *   GET /api/neo4j/relation?from=ActorA&to=ActorB&maxDepth=4
 *   GET /api/neo4j/stats/collaborations
 */
require('dotenv').config();
const express = require('express');
const neo4j = require('neo4j-driver');
const cors = require('cors');

const app = express();
app.use(express.json());
app.use(cors());

const NEO4J_URI = process.env.NEO4J_URI || 'bolt://localhost:7687';
const NEO4J_USER = process.env.NEO4J_USER || 'neo4j';
const NEO4J_PASSWORD = process.env.NEO4J_PASSWORD || 'change_me';

const driver = neo4j.driver(NEO4J_URI, neo4j.auth.basic(NEO4J_USER, NEO4J_PASSWORD));

async function runCypher(query, params = {}) {
  const session = driver.session();
  try {
    const result = await session.run(query, params);
    return result;
  } finally {
    await session.close();
  }
}

app.get('/api/neo4j/actor/:name', async (req, res) => {
  const actorName = req.params.name;
  const query = `
    MATCH (a:Actor)
    WHERE a.actor_name = $name OR a.actor_id = $name
    OPTIONAL MATCH (a)-[:ACTED]->(m:Movie)
    RETURN m { .movie_id, .movie_title, .release_year, .review_count } AS movie
    ORDER BY m.release_year DESC
    LIMIT 200
  `;
  try {
    const result = await runCypher(query, { name: actorName });
    const movies = result.records.map(r => r.get('movie'));
    res.json({ actor: actorName, movies });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/neo4j/movie/:id', async (req, res) => {
  const movieId = req.params.id;
  const query = `
    MATCH (m:Movie {movie_id:$id})
    OPTIONAL MATCH (m)<-[:ACTED]-(a:Actor)
    OPTIONAL MATCH (m)<-[:DIRECTED]-(d:Director)
    OPTIONAL MATCH (m)-[:HAS_GENRE]->(g:Genre)
    RETURN m {.movie_id, .movie_title, .release_year, .review_count} AS movie,
           collect(DISTINCT a.actor_name) AS actors,
           collect(DISTINCT d.director_name) AS directors,
           collect(DISTINCT g.genre_name) AS genres
    LIMIT 1
  `;
  try {
    const result = await runCypher(query, { id: movieId });
    if (result.records.length === 0) return res.status(404).json({ error: 'Movie not found' });
    const rec = result.records[0];
    res.json({
      movie: rec.get('movie'),
      actors: rec.get('actors'),
      directors: rec.get('directors'),
      genres: rec.get('genres')
    });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/neo4j/genre/:name', async (req, res) => {
  const genreName = req.params.name;
  const query = `
    MATCH (g:Genre {genre_name:$name})<-[:HAS_GENRE]-(m:Movie)
    RETURN m {.movie_id, .movie_title, .release_year} AS movie
    ORDER BY m.release_year DESC
    LIMIT 500
  `;
  try {
    const result = await runCypher(query, { name: genreName });
    res.json(result.records.map(r => r.get('movie')));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/neo4j/relation', async (req, res) => {
  const fromActor = req.query.from;
  const toActor = req.query.to;
  const maxDepth = Math.max(1, Math.min(10, parseInt(req.query.maxDepth || '4', 10)));
  if (!fromActor || !toActor) return res.status(400).json({ error: 'from and to query parameters required' });
  const query = `
    MATCH (a:Actor {actor_name:$from}) , (b:Actor {actor_name:$to})
    MATCH p = shortestPath((a)-[:ACTED*..$maxDepth]-(b))
    RETURN p LIMIT 1
  `;
  try {
    const result = await runCypher(query, { from: fromActor, to: toActor, maxDepth: neo4j.int(maxDepth) });
    if (result.records.length === 0) return res.status(404).json({ error: 'No path found' });
    const path = result.records[0].get('p');
    res.json({ path: path });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.get('/api/neo4j/stats/collaborations', async (req, res) => {
  const limit = parseInt(req.query.limit || '50', 10);
  const query = `
    MATCH (a:Actor)-[:ACTED]->(m:Movie)<-[:ACTED]-(b:Actor)
    WHERE a.actor_name < b.actor_name
    RETURN a.actor_name AS actor1, b.actor_name AS actor2, COUNT(m) AS collaborations
    ORDER BY collaborations DESC
    LIMIT $limit
  `;
  try {
    const result = await runCypher(query, { limit: neo4j.int(limit) });
    const rows = result.records.map(r => ({
      actor1: r.get('actor1'),
      actor2: r.get('actor2'),
      collaborations: r.get('collaborations').toNumber ? r.get('collaborations').toNumber() : r.get('collaborations')
    }));
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Health check
app.get('/api/neo4j/health', async (req, res) => {
  try {
    const session = driver.session();
    await session.run('RETURN 1');
    await session.close();
    res.json({ status: 'ok' });
  } catch (err) {
    res.status(500).json({ status: 'error', error: err.message });
  }
});

// Flexible movies list with multiple optional filters
app.get('/api/neo4j/movies', async (req, res) => {
  const {
    title, genre, actor, director, yearFrom, yearTo,
    limit = 100, offset = 0
  } = req.query;
  const params = {};
  const whereClauses = [];
  const matchClauses = ['(m:Movie)'];

  if (actor) {
    matchClauses.push('(a:Actor)-[:ACTED]->(m)');
    params.actor = actor;
    whereClauses.push('a.actor_name = $actor');
  }
  if (director) {
    matchClauses.push('(d:Director)-[:DIRECTED]->(m)');
    params.director = director;
    whereClauses.push('d.director_name = $director');
  }
  if (genre) {
    matchClauses.push('(m)-[:HAS_GENRE]->(g:Genre)');
    params.genre = genre;
    whereClauses.push('g.genre_name = $genre');
  }
  if (title) {
    params.title = title.toLowerCase();
    whereClauses.push('toLower(m.movie_title) CONTAINS $title');
  }
  if (yearFrom) {
    params.yearFrom = parseInt(yearFrom, 10);
    whereClauses.push('m.release_year >= $yearFrom');
  }
  if (yearTo) {
    params.yearTo = parseInt(yearTo, 10);
    whereClauses.push('m.release_year <= $yearTo');
  }

  params.limit = parseInt(limit, 10);
  params.limit = neo4j.int(parseInt(limit, 10));
  params.offset = neo4j.int(parseInt(offset, 10));

  const match = 'MATCH ' + matchClauses.join(',');
  const where = whereClauses.length ? ('WHERE ' + whereClauses.join(' AND ')) : '';
  const query = `
    ${match}
    ${where}
    RETURN DISTINCT m {.movie_id, .movie_title, .release_year, .review_count} AS movie
    ORDER BY movie.release_year DESC
    SKIP $offset LIMIT $limit
  `;
  try {
    const result = await runCypher(query, params);
    res.json(result.records.map(r => r.get('movie')));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Alias to support frontend route naming consistency
app.get('/api/neo4j/movies/genre/:name', (req, res, next) => {
  req.params.name && (req.query.name = req.params.name);
  return app._router.handle(req, res, next);
});

app.get('/api/neo4j/movies/director/:name', async (req, res) => {
  const director = req.params.name;
  const query = `
    MATCH (d:Director {director_name:$director})-[:DIRECTED]->(m:Movie)
    RETURN m {.movie_id, .movie_title, .release_year} AS movie
    ORDER BY m.release_year DESC
    LIMIT 500
  `;
  try {
    const result = await runCypher(query, { director });
    res.json(result.records.map(r => r.get('movie')));
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Stats by year (movie counts per year)
app.get('/api/neo4j/movies/stats', async (req, res) => {
  try {
    const query = `
      MATCH (m:Movie)
      RETURN m.release_year AS year, COUNT(m) AS num
      ORDER BY year
    `;
    const result = await runCypher(query);
    const rows = result.records.map(r => ({
      year: r.get('year'),
      num: r.get('num').toNumber ? r.get('num').toNumber() : r.get('num')
    }));
    res.json(rows);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Compare endpoint: measure execution time of a sample query
app.get('/api/neo4j/movies/compare', async (req, res) => {
  const sampleQuery = 'MATCH (m:Movie) RETURN count(m) AS c';
  try {
    const start = Date.now();
    const result = await runCypher(sampleQuery);
    const durationMs = Date.now() - start;
    const count = result.records[0].get('c').toNumber ? result.records[0].get('c').toNumber() : result.records[0].get('c');
    res.json({ engine: 'neo4j', query: 'count movies', count, durationMs });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// --- Compatibility routes used by frontend under /queries/*
// Time-based stats (wrapper for movies/stats)
app.get('/api/neo4j/queries/time', async (req, res) => {
  try {
    const result = await runCypher(`MATCH (m:Movie) RETURN m.release_year AS year, COUNT(m) AS num ORDER BY year`);
    const rows = result.records.map(r => ({ year: r.get('year'), num: r.get('num').toNumber ? r.get('num').toNumber() : r.get('num') }));
    res.json({ items: rows, total: rows.length });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Relation wrapper: returns graph { nodes:[], edges: [] }
app.get('/api/neo4j/queries/relation', async (req, res) => {
  const fromActor = req.query.from;
  const toActor = req.query.to;
  const maxDepth = Math.max(1, Math.min(10, parseInt(req.query.maxDepth || '4', 10)));
  if (!fromActor || !toActor) return res.status(400).json({ error: 'from and to required' });
  const query = `
    MATCH (a:Actor {actor_name:$from}) , (b:Actor {actor_name:$to})
    MATCH p = shortestPath((a)-[:ACTED*..$maxDepth]-(b))
    RETURN p LIMIT 1
  `;
  try {
    const result = await runCypher(query, { from: fromActor, to: toActor, maxDepth });
    if (result.records.length === 0) return res.status(404).json({ error: 'No path found' });
    const path = result.records[0].get('p');
    const nodes = [];
    const edges = [];
    // collect nodes
    path.nodes.forEach(n => {
      nodes.push({ id: n.identity.toString(), labels: n.labels, props: n.properties });
    });
    // collect relationships
    path.relationships.forEach(r => {
      edges.push({ id: r.identity.toString(), type: r.type, start: r.start.toString(), end: r.end.toString(), props: r.properties });
    });
    res.json({ graph: { nodes, edges } });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

// Complex query wrapper: accepts filters in body and returns items/total
app.post('/api/neo4j/queries/complex', express.json(), async (req, res) => {
  const filters = req.body || {};
  const { title, genre, actor, director, yearFrom, yearTo, limit = 100, offset = 0 } = filters;
  const params = {};
  const whereClauses = [];
  const matchClauses = ['(m:Movie)'];
  if (actor) {
    matchClauses.push('(a:Actor)-[:ACTED]->(m)');
    params.actor = actor;
    whereClauses.push('a.actor_name = $actor OR a.actor_id = $actor');
  }
  if (director) {
    matchClauses.push('(d:Director)-[:DIRECTED]->(m)');
    params.director = director;
    whereClauses.push('d.director_name = $director OR d.director_id = $director');
  }
  if (genre) {
    matchClauses.push('(m)-[:HAS_GENRE]->(g:Genre)');
    params.genre = genre;
    whereClauses.push('g.genre_name = $genre');
  }
  if (title) {
    params.title = title.toLowerCase();
    whereClauses.push('toLower(m.title) CONTAINS $title OR toLower(m.product_id) CONTAINS $title');
  }
  if (yearFrom) {
    params.yearFrom = parseInt(yearFrom, 10);
    whereClauses.push('m.release_year >= $yearFrom');
  }
  if (yearTo) {
    params.yearTo = parseInt(yearTo, 10);
    whereClauses.push('m.release_year <= $yearTo');
  }
  params.limit = parseInt(limit, 10);
  params.limit = neo4j.int(parseInt(limit, 10));
  params.offset = neo4j.int(parseInt(offset, 10));
  const match = 'MATCH ' + matchClauses.join(',');
  const where = whereClauses.length ? ('WHERE ' + whereClauses.join(' AND ')) : '';
  const query = `
    ${match}
    ${where}
    RETURN DISTINCT m { movie_id: m.movie_idx, movie_title: m.title, .release_year, .review_count } AS movie
    ORDER BY movie.release_year DESC
    SKIP $offset LIMIT $limit
  `;
  try {
    const result = await runCypher(query, params);
    const items = result.records.map(r => r.get('movie'));
    // try to get total count (simple count query)
    const countQuery = `${match} ${where} RETURN count(DISTINCT m) AS total`;
    const countRes = await runCypher(countQuery, params);
    const total = countRes.records[0].get('total').toNumber ? countRes.records[0].get('total').toNumber() : countRes.records[0].get('total');
    res.json({ items, total });
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});
const port = process.env.PORT || 3000;
app.listen(port, () => {
  console.log(`neo4j-backend listening on port ${port}`);
  console.log(`Neo4j URI: ${NEO4J_URI}, user: ${NEO4J_USER}`);
});

process.on('SIGINT', async () => {
  console.log('Shutting down, closing Neo4j driver');
  await driver.close();
  process.exit(0);
});


