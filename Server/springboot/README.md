# MEDlyon Spring Boot backend

Minimal backend for the React bridge in `Server/roulyon/src/bridge.jsx`.

## Endpoints

- `GET /api/position?Nodename=...`
- `GET /api/closest-node?lat=...&lng=...`
- `GET /api/path?slat=...&slng=...&elat=...&elng=...`

## Data source

By default, the service reads `raw_datasets/bus/lyon_tcl` from the repo root.

Override it with:

```bash
medlyon.data-dir=C:\path\to\lyon_tcl
```

or the equivalent JVM system property.

## Frontend config

Set this in `Server/roulyon/.env.local`:

```bash
VITE_JAVA_LINK=http://localhost:8080/api
```
