# MongoDB setup (Atlas free tier)

This project uses **MongoDB** via Spring Data MongoDB. The intended store is **MongoDB Atlas**, the
free **M0** cluster (512 MB, no credit card). Each service owns its own database inside one shared
cluster.

## 1. Create a free Atlas account & cluster
1. Register at <https://www.mongodb.com/cloud/atlas/register> (Google login or email).
2. Create a **project** (e.g. `exim-platform`).
3. **Build a Database → M0 (Free)**. Pick a cloud + region near you (e.g. AWS `ap-south-1` Mumbai).
   Name it e.g. `exim-cluster`. Wait ~3 minutes for it to provision.
4. **Security → Database Access → Add New Database User**: password auth, username `exim_app`,
   generate a strong password (save it), role **Read and write to any database**.
5. **Security → Network Access → Add IP Address**: for local dev choose **Allow access from
   anywhere** (`0.0.0.0/0`). Tighten this for production.
6. **Database → Connect → Drivers → Java**: copy the connection string, which looks like
   ```
   mongodb+srv://exim_app:<password>@exim-cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority
   ```

> Atlas M0 is a **replica set**, so multi-document `@Transactional` operations work. (A standalone
> local `mongod` is *not* a replica set and would reject transactions — see the offline option below.)

## 2. Point the services at Atlas
All four services read a single `MONGODB_URI`; each writes to its **own database**
(`exim_identity`, `exim_verification`, `exim_catalog`, `exim_sourcing`), set in each service's
`application.yml` via `spring.data.mongodb.database`. So you only export one URI:

```bash
export MONGODB_URI='mongodb+srv://exim_app:<password>@exim-cluster.xxxxx.mongodb.net/?retryWrites=true&w=majority'
export JWT_SECRET='a-long-random-secret-of-at-least-32-bytes-please'   # same for every service
```

Then run each service (each picks its own database automatically):

```bash
mvn -pl services/identity-service     spring-boot:run   # :8081  -> exim_identity
mvn -pl services/verification-service  spring-boot:run   # :8082  -> exim_verification
mvn -pl services/catalog-service       spring-boot:run   # :8083  -> exim_catalog
mvn -pl services/sourcing-service      spring-boot:run   # :8084  -> exim_sourcing
mvn -pl gateway                        spring-boot:run   # :8080
```

Collections and databases are created on first write. Indexes (`@Indexed`) and catalog's seed HS
codes are created on startup. Verify in Atlas under **Database → Browse Collections**.

## 3. Smoke test
```bash
# Register a company + admin (identity-service), capture the JWT
curl -s -X POST http://localhost:8081/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"companyName":"Acme Exports","companyType":"EXPORTER","country":"IN",
       "adminName":"Asha","email":"asha@acme.com","password":"Passw0rd!"}'
```
A `success:true` response with a token confirms MongoDB reads/writes (and the `@DBRef` company link)
are working.

## Offline option (no Atlas)
`docker compose up -d` starts a local MongoDB configured as a **single-node replica set** (so
transactions work) plus Redis. The dev-profile default `MONGODB_URI`
(`mongodb://localhost:27017/?directConnection=true`) already points at it, so you can run the
services without exporting anything. (Requires Docker.)

## Notes
- **One secret, many databases**: `spring.data.mongodb.database` in each `application.yml` overrides
  any database named in the URI, which is why a single `MONGODB_URI` serves all services.
- **prod profile** (`SPRING_PROFILES_ACTIVE=prod`) requires `MONGODB_URI` with no localhost fallback.
- Never commit the Atlas password; keep it in `MONGODB_URI`.
