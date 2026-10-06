import http from 'node:http';
import { readFile, stat } from 'node:fs/promises';
import { resolve, extname, sep } from 'node:path';
import { fileURLToPath } from 'node:url';

// Local preview of the production build with the same API proxy as ng serve.
const root = fileURLToPath(new URL('../dist/frontend/browser/', import.meta.url));
const backend = new URL('http://127.0.0.1:8080');
const types = { '.html': 'text/html; charset=utf-8', '.js': 'text/javascript',
  '.css': 'text/css', '.json': 'application/json', '.ico': 'image/x-icon', '.svg': 'image/svg+xml' };
await stat(resolve(root, 'index.html')).catch(() => {
  console.error('Build assente. Esegui npm run build prima di npm run preview.');
  process.exit(1);
});
http.createServer(async (req, res) => {
  if (req.url === '/api' || req.url.startsWith('/api/') || req.url.startsWith('/api?')) {
    const upstream = http.request(new URL(req.url, backend), {
      method: req.method, headers: { ...req.headers, host: backend.host }
    }, response => {
      res.writeHead(response.statusCode, response.headers);
      response.pipe(res);
    });
    upstream.on('error', () => {
      if (!res.headersSent) res.writeHead(502, { 'Content-Type': 'text/plain; charset=utf-8' });
      res.end('Backend non raggiungibile sulla porta 8080.');
    });
    req.pipe(upstream);
    return;
  }
  try {
    const pathname = decodeURIComponent(new URL(req.url, 'http://localhost').pathname);
    let path = resolve(root, '.' + pathname);
    if (path !== resolve(root) && !path.startsWith(resolve(root) + sep)) {
      res.writeHead(403); res.end(); return;
    }
    if (pathname === '/' || !extname(pathname)) path = resolve(root, 'index.html');
    const content = await readFile(path);
    res.writeHead(200, { 'Content-Type': types[extname(path)] ?? 'application/octet-stream' });
    res.end(content);
  } catch {
    res.writeHead(404); res.end('Not found');
  }
}).listen(4200, '127.0.0.1', () => console.log('Anteprima locale: http://localhost:4200'));
