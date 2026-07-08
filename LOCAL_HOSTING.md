# Hosting hirkaniexim.com from your own Mac (no server rental)

This guide answers: *"Can I point my GoDaddy domain at my local machine instead of buying
a server?"* — and walks through the way that actually works on an Indian home/mobile
connection: **Cloudflare Tunnel**.

For renting a real VM instead, see `DEPLOYMENT.md` (that is still the right choice for
production). This guide is for demos, client previews, and development.

---

## 1. Why you can't just put your IP in a GoDaddy A record

DNS is only a phone book. GoDaddy will happily let you set:

```
A    @    <any IP you type>
```

…but for visitors to reach you, that IP must (a) be *yours alone*, (b) accept inbound
connections, and (c) stay the same. On this machine's connection all three fail:

| Requirement | Your connection | Result |
|---|---|---|
| Public IPv4 that routes to you | `223.233.87.149` — Airtel range behind **CGNAT** (shared by many customers; inbound traffic is dropped by the ISP) | Visitors never reach your Mac |
| Stable IP | Consumer IP changes on every reconnect/lease renewal | Site randomly dies until DNS is updated |
| Open ports 80/443 | Needs router port-forwarding; ISPs often block port 80 | Extra breakage even without CGNAT |

> **What is CGNAT?** Carrier-Grade NAT: the ISP puts hundreds of customers behind one
> public IPv4. You can make *outbound* connections, but nobody can connect *in* to you —
> exactly like devices behind a home router, one level up. There is no setting to disable
> it; some ISPs sell a "static/public IP" add-on (₹100–200/month typically) that removes it.

**The trick that bypasses all of this:** don't accept inbound connections at all. Your Mac
dials **out** to a relay that has real public IPs, and the relay forwards visitors down the
already-open connection. That's what Cloudflare Tunnel / ngrok do.

---

## 2. Options at a glance

| Option | Cost | Own domain? | Survives IP change | Effort | Good for |
|---|---|---|---|---|---|
| **Cloudflare Tunnel** (recommended) | Free | ✅ hirkaniexim.com + HTTPS | ✅ automatic | ~1 hour once | Ongoing demo hosting from the Mac |
| ngrok | Free tier | ❌ (one `*.ngrok-free.app` URL) | ✅ | 5 minutes | Showing someone something *today* |
| Tailscale Funnel | Free | ❌ (`*.ts.net`) | ✅ | 15 min | Private team access |
| ISP static IP + port forward | ~₹150/mo | ✅ | ✅ (static) | Router + DNS fiddling | Only if you refuse relays |
| Rent a VM (`DEPLOYMENT.md`) | ~$40+/mo (16 GB for 16 JVMs) | ✅ | ✅ | Half a day | Real production |
| Cloudflare Pages / Netlify (frontend only) | Free | ✅ | n/a | 30 min | Marketing site without the API |

---

## 3. Cloudflare Tunnel — full setup

### How it will fit together

```
 Visitor ── https://hirkaniexim.com ──▶ Cloudflare edge (TLS, global)
                                            │  (tunnel — outbound-only connection
                                            ▼   opened BY your Mac, so CGNAT is irrelevant)
                                   cloudflared on your Mac
                                     ├── path /api/*  → gateway  http://localhost:8080
                                     └── everything else → frontend http://localhost:3000
```

Same one-origin layout as `DEPLOYMENT.md` (§1): the React app calls `/api/v1/...`
relatively, so there is **no CORS to configure**. Cloudflare terminates HTTPS for free.

### Step 1 — Add the domain to a free Cloudflare account

1. Create an account at https://dash.cloudflare.com (free plan).
2. **Add a domain** → enter `hirkaniexim.com` → choose the **Free** plan.
3. Cloudflare scans your existing GoDaddy DNS records and shows you **two nameservers**,
   e.g. `ada.ns.cloudflare.com` and `bob.ns.cloudflare.com`. Keep this page open.

### Step 2 — Change nameservers at GoDaddy (one-time)

1. GoDaddy → **Domain Portfolio** → `hirkaniexim.com` → **DNS** → **Nameservers** tab.
2. **Change Nameservers** → "I'll use my own nameservers" → paste the two Cloudflare
   nameservers → Save.
3. The domain **stays registered at GoDaddy** (renewals unchanged); only DNS management
   moves to Cloudflare. Propagation takes minutes to a few hours; Cloudflare emails you
   when the zone is **Active**.

> Don't use GoDaddy "Domain Forwarding" — same warning as `DEPLOYMENT.md` §3.

### Step 3 — Install and authenticate cloudflared on the Mac

```bash
brew install cloudflared          # it's a bottle; installs fine
cloudflared tunnel login          # opens the browser → pick hirkaniexim.com → authorize
```

This drops a certificate in `~/.cloudflared/cert.pem`.

### Step 4 — Create the tunnel and route DNS to it

```bash
cloudflared tunnel create hirkani
# prints a Tunnel ID (UUID) and writes ~/.cloudflared/<TUNNEL-ID>.json (the tunnel secret)

cloudflared tunnel route dns hirkani hirkaniexim.com
cloudflared tunnel route dns hirkani www.hirkaniexim.com
```

The `route dns` commands create CNAME records in Cloudflare pointing the domain at the
tunnel — you never type an IP address anywhere, which is why IP changes don't matter.

### Step 5 — Ingress config

Create `~/.cloudflared/config.yml` (replace `<TUNNEL-ID>` with the UUID from step 4):

```yaml
tunnel: <TUNNEL-ID>
credentials-file: /Users/mym2pro/.cloudflared/<TUNNEL-ID>.json

ingress:
  # API → the Spring Cloud gateway (fans out to the 15 services)
  - hostname: hirkaniexim.com
    path: /api/.*
    service: http://localhost:8080
  - hostname: www.hirkaniexim.com
    path: /api/.*
    service: http://localhost:8080
  # Everything else → the React site
  - hostname: hirkaniexim.com
    service: http://localhost:3000
  - hostname: www.hirkaniexim.com
    service: http://localhost:3000
  # Required catch-all
  - service: http_status:404
```

### Step 6 — Run the stack

```bash
# 1. MongoDB — local replica set (current setup), or Atlas once your IP is allowlisted
~/.local/mongodb/mongodb-macos-aarch64-8.0.12/bin/mongod \
  --dbpath ~/.local/mongodb/data --replSet rs0 --port 27017 \
  --fork --logpath ~/.local/mongodb/log/mongod.log

# 2. Backend — 15 services + gateway (from the repo root)
./run-all.sh        # uses set-env.sh; for local Mongo export
                    # MONGODB_URI='mongodb://localhost:27017/?directConnection=true' first

# 3. Frontend — build once, serve dist on :3000
cd frontend && npm run build && npm run preview -- --port 3000 --host 127.0.0.1 &

# 4. The tunnel
cloudflared tunnel run hirkani
```

Visit **https://hirkaniexim.com** — served from your Mac, with a valid TLS certificate.

### Step 7 — Keep it running across reboots (optional)

```bash
sudo cloudflared service install     # installs a launchd service for the tunnel
```

`vite preview` is a convenience server; if this stopgap becomes semi-permanent, serve
`frontend/dist` with a local nginx (`brew install nginx`) using the same config as
`DEPLOYMENT.md` §7 and point the tunnel's non-API ingress at it instead.

---

## 4. Before exposing this publicly — minimum hardening

The dev defaults are **not safe on the open internet**. At minimum:

1. **`JWT_SECRET`** — replace the placeholder in `set-env.sh` with a real random secret:
   `openssl rand -base64 48`. All services must share the new value; restart all.
2. **Platform admin** — change `PLATFORM_ADMIN_PASSWORD` (default `Admin@12345`) via env,
   and set a strong `ADMIN_BOOTSTRAP_SECRET` or disable bootstrap.
3. **MongoDB** — local `mongod` binds `127.0.0.1` only (good — keep it that way).
   If switching to Atlas, add your current IP to Atlas → Network Access first
   (your Atlas cluster currently rejects this machine — that's the TLS `internal_error`).
4. **Don't expose service ports.** Only the tunnel reaches `:8080`/`:3000`; ports
   8081–8095 stay on localhost. Never port-forward them on the router.
5. **Swagger** — `/swagger-ui.html` and `/v3/api-docs` are public by design; consider
   restricting at the gateway before a real audience.
6. In Cloudflare (free): enable **Always Use HTTPS** and **Bot Fight Mode**; you get
   basic DDoS protection automatically.

---

## 5. Honest limitations of hosting on your Mac

- **Uptime = your Mac + your ISP.** Sleep, reboots, Wi-Fi drops → site down.
  (System Settings → prevent sleep while plugged in, at minimum.)
- **Bandwidth = your home upload speed**, shared with everything else you do.
- **RAM**: 16 JVMs + Mongo + frontend + your normal work on one machine.
- **Cloudflare free ToS**: fine for a website/API; don't stream large media through it.
- When this stops being a demo, follow `DEPLOYMENT.md` — the DNS part gets even easier
  since the domain is already on Cloudflare (just change where the records point).

## 6. Five-minute alternative for a one-off demo (ngrok)

```bash
brew install ngrok
ngrok config add-authtoken <token from dashboard.ngrok.com>
ngrok http 3000        # or 8080 for the API — gives you https://<something>.ngrok-free.app
```

No domain/nameserver changes, but you get a random `ngrok-free.app` URL and an
interstitial page on the free tier — good for "look at this now", not for a real domain.
