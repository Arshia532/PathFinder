# How to run PathFinder

PathFinder has two parts that run at the same time:

| Part | Folder | Runs on | What it does |
|---|---|---|---|
| Backend (Java, Spring Boot) | `backend/` | http://localhost:8080 | Runs every search, generates random maps, saves and loads map files |
| Frontend (React, Vite) | `frontend/` | http://localhost:5173 | The page you open in the browser |

You need **two terminals**: one for the backend and one for the frontend.
Start the backend first.

---

## 1. Install the tools (once)

| Tool | Version | Check with |
|---|---|---|
| JDK | 26 (for example Eclipse Temurin 26) | `java -version` |
| Node.js | 24 LTS (at least 22.12) | `node -v` |
| Git | any recent version | `git --version` |

You do **not** need to install Maven: the backend includes a Maven wrapper
(`mvnw`) that downloads the right version on first use.

If `java -version` shows an older JDK, point the current PowerShell window at
JDK 26 (replace the path with your own installation folder):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-26.0.2.101-hotspot'
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
java -version
```

This only affects that terminal window.

## 2. Get the code (once)

```bash
git clone https://github.com/Arshia532/PathFinder.git
cd PathFinder
```

---

## 3. Start the backend (terminal 1)

**Windows (PowerShell):**

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

**macOS / Linux:**

```bash
cd backend
./mvnw spring-boot:run
```

The first run downloads dependencies and takes a few minutes. It is ready when
the log shows:

```
Tomcat started on port 8080 (http)
Started PathFinderApplication in ... seconds
```

Leave this terminal running. To stop the server, press **Ctrl+C**.

> After changing any Java file, stop the server (Ctrl+C) and start it again.
> It does not reload code by itself.

## 4. Start the frontend (terminal 2)

Open a **second** terminal in the `PathFinder` folder.

**First time only**, install the packages:

```bash
cd frontend
npm install
```

**Every time**, start the dev server from the `frontend` folder:

```bash
npm run dev
```

Vite prints something like:

```
  VITE v8.x.x  ready in 300 ms

  ➜  Local:   http://localhost:5173/
  ➜  press h + enter to show help
```

## 5. Open it in the browser

Use any one of these:

- **Ctrl+click** (Cmd+click on macOS) the `http://localhost:5173/` link in the terminal.
- In the Vite terminal, type **`o`** and press **Enter** to open your default browser.
- Open a browser and go to **http://localhost:5173** yourself.

The page reloads by itself when you change frontend code.

## 6. Check that everything is connected

1. Open the browser's DevTools (**F12**) and go to the **Console** tab.
   It should say **`PathFinder API: Spring Boot backend`**.
2. In the app, choose **BFS** under Algorithm. The grid should fill with
   visited cells and an orange path, and the stats cards should show numbers.
3. Click **Generate Random**: a new map appears and the Seed field shows its seed.

---

## Running without the backend (mock mode)

To work on the frontend alone, start it with this instead of `npm run dev`:

```bash
npm run dev:mock
```

The console then says `PathFinder API: mock`. Search uses a built-in BFS;
Generate Random, Save and Load need the backend.

## Running the tests

Backend (from `backend/`):

```powershell
.\mvnw.cmd -B clean verify
```

This compiles with warnings treated as errors, runs all JUnit and HTTP tests,
and checks the Javadoc. It should end with **BUILD SUCCESS**.

Frontend (from `frontend/`):

```bash
npm run lint
npm run build
```

---

## Troubleshooting

| What you see | Cause | Fix |
|---|---|---|
| `npm error enoent Could not read package.json` | The terminal is in the repository root, not `frontend` | `cd frontend`, then run the command again. The name in brackets in Git Bash is the branch, not the folder. |
| Red box: "The server isn't responding … Is the backend running?" (502 in the Network tab) | The backend is not running | Start it (step 3) and wait for "Started PathFinderApplication" |
| Red box: "Request failed (404)." | The backend is running an older build | Stop it with Ctrl+C and start it again |
| Red box: "A* is not available yet." | That algorithm is not implemented in the backend yet | Choose BFS or DFS |
| `Port 8080 was already in use` | Another program (or an old server) uses port 8080 | Close the other server, or find it with `netstat -ano \| findstr :8080` and end that process |
| `verify.ps1 cannot be loaded because running scripts is disabled` | Windows blocks PowerShell scripts by default | Run `.\mvnw.cmd -B verify` from `backend` instead |
| `Unresolved compilation problem` in a Maven run | Leftover classes compiled by the editor | Run `.\mvnw.cmd -B clean verify` |
| Console says `PathFinder API: mock` | The frontend was started with `npm run dev:mock`, or an old `frontend/.env.local` sets mock mode | Stop it and run `npm run dev`; delete `frontend/.env.local` if you have one |

## API endpoints (for reference)

All endpoints take and return JSON. Errors return `{ "message": "..." }` with status 400 or 500.

| Endpoint | Body | Returns |
|---|---|---|
| `POST /api/search` | Map + `algorithm` + `bidirectional` | Steps, path and stats |
| `POST /api/maps/random` | `width`, `height`, `seed` | A generated map |
| `POST /api/maps/export` | A map | The map file contents |
| `POST /api/maps/import` | A map file's text | The checked map |
