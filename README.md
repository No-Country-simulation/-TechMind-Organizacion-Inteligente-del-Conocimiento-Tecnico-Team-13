# My Application

A Spring Boot + Vaadin project. Build your UI in pure Java — no HTML, no JavaScript.

> **New to Vaadin?** The 5-minute [Quickstart](https://vaadin.com/quickstart) walks you from here to your first running app, a live code change, and an AI-assisted edit with Copilot.

---

## Fastest start — no plugin needed

From the project folder:

```bash
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

Then open **http://localhost:8080**.

The first start takes ~30 seconds while Maven downloads dependencies.

> **Port 8080 already in use?** Stop the other process, or set `server.port=8081` in `src/main/resources/application.properties` and open that port instead.
>
> **To stop the app:** press `Ctrl+C` in the terminal (or the red Stop button if you launched from your IDE).

## Optional upgrade — instant hotswap

Running with `spring-boot:run` works, but Java code changes need a server restart. For **live reload** — edit Java, see it in the browser without restarting — install the **Vaadin plugin** and start the app through it:

- **IntelliJ IDEA:** install *Vaadin* from the JetBrains Marketplace → **Debug using Hotswap Agent** (dropdown next to Run). *Just installed it? Let IntelliJ finish indexing, or restart it, if the menu item isn't there yet.*
- **VS Code:** install the *Vaadin* extension → **Vaadin: Debug using Hotswap Agent** from the command palette.
- **Eclipse:** install the *Vaadin* plugin → right-click the project → **Run As → Vaadin Application**.

This is what makes the edit-and-see-it loop feel instant — and it's required for the AI edits in [Vaadin Copilot](https://vaadin.com/docs/latest/tools/copilot).

---

## Ask your AI assistant about Vaadin (optional)

If you use Claude Code, Cursor, or another AI coding assistant, connect it to the **Vaadin MCP server** so it answers against real Vaadin docs and the exact API of your installed version — instead of guessing from outdated training data.

```bash
# One-time setup — see https://vaadin.com/docs/latest/building-apps/mcp
```

A `.mcp.json` is included (commented out by default). Uncomment it, or run the setup command above, to activate.

---

## Build for production

```bash
./mvnw package
java -jar target/*.jar
```

## Run with Docker

This project now includes a portable Docker setup for easier sharing and deployment.

### Build the image

```bash
docker build -t logicore-app .
```

### Run the container

```bash
docker run -p 8080:8080 --name logicore-app logicore-app
```

### Run with Docker Compose

```bash
docker compose up --build
```

### Publish to GitHub

1. Create a new public GitHub repository.
2. Push the project to the repository.
3. Add a short description and choose the license.
4. Optionally add a GitHub Actions workflow to build the Docker image automatically.

## Learn more

- [Vaadin Quickstart](https://vaadin.com/quickstart) — the 5-minute getting-started path
- [Components](https://vaadin.com/docs/latest/components) — 50+ UI components, all callable from Java
- [Vaadin Copilot](https://vaadin.com/docs/latest/tools/copilot) — visual + AI editing in the browser
- [Full documentation](https://vaadin.com/docs)
