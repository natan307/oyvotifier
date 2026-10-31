# oyvotifier

**oyvotifier** is a modernized fork of [NuVotifier](https://github.com/NuVotifier/NuVotifier) 2.7.3, a secure and
extensible replacement for the original Votifier plugin. It receives votes from server-list websites and delivers
them to your vote-listener plugins as an in-game event.

This fork is rebranded as `oyvotifier` (version `1.0.1`), updated for **Minecraft 1.21.x and 26.x**, and ships as a
single universal JAR for Bukkit-family servers and the major proxies (BungeeCord, Waterfall and Velocity).

> **Unofficial fork.** This project is not affiliated with, endorsed by, or supported by the NuVotifier or original
> Votifier teams. Please do not report issues from this fork to the upstream projects.

---

## Table of contents

- [Features](#features)
- [Supported platforms](#supported-platforms)
- [Installation](#installation)
- [Configuration](#configuration)
- [Commands and permissions](#commands-and-permissions)
- [Vote forwarding (proxy setups)](#vote-forwarding-proxy-setups)
- [Building from source](#building-from-source)
- [Changelog](#changelog)
- [Changes from NuVotifier 2.7.3](#changes-from-nuvotifier-273)
- [Migrating from Votifier / NuVotifier](#migrating-from-votifier--nuvotifier)
- [For plugin developers](#for-plugin-developers)
- [Known limitations](#known-limitations)
- [Credits and license](#credits-and-license)

---

## Features

Inherited from NuVotifier:

- **Votifier protocol v1 and v2.** v2 uses per-site tokens and is considerably more secure than the legacy RSA-only v1 protocol.
- **Token-based authentication** per voting website, in addition to the RSA key pair for v1.
- **Option to disable protocol v1** entirely for tighter security.
- **Vote forwarding** from a proxy (BungeeCord / Velocity) to backend servers, via plugin messaging or a direct Votifier connection.
- **Vote caching** so votes for offline backend servers are delivered when they come back.
- **`/testvote`** command to fire a test vote and verify your listeners.
- **Hot reload** with `/nvreload`.
- **Bundled and relocated Netty/Gson**, so it does not conflict with other plugins' copies.

Added in this fork:

- Builds and runs against **1.21.x and 26.x** APIs.
- Modern build tooling (Gradle 8.10.2, Shadow 8.1.1, Java 17 bytecode target).
- Drop-in compatibility layer for plugins that depend on `Votifier` or `NuVotifier` by name (Bukkit family).

---

## Supported platforms

One universal JAR, `oyvotifier-1.0.1.jar`, works on:

| Platform | Supported versions | Notes |
|---|---|---|
| Bukkit / Spigot / Paper | Minecraft 1.21.x and 26.x | Compiled against the Spigot API 1.21.4. Paper 26.1+ itself requires Java 25 on the server. |
| BungeeCord (and compatible forks) | 1.21.x and 26.x | Compiled against `bungeecord-api` 1.21-R0.5-SNAPSHOT. |
| Waterfall | Whatever the proxy itself supports | Uses the BungeeCord API that Waterfall implements, so the same JAR loads there. See [Waterfall notes](#waterfall-notes). |
| Velocity | 3.4+ | Compiled against `velocity-api` 3.4.0-SNAPSHOT. |

**Java:**

- Bukkit / Spigot / Paper and Velocity parts are compiled to Java 17 bytecode (Minecraft 1.21.x servers already require
  Java 21, and Paper 26.1+ requires Java 25).
- The shared core and the BungeeCord / Waterfall part are compiled to **Java 8 bytecode**, so the plugin can load on
  proxies that still run an older JVM.

### Waterfall notes

Waterfall is a fork of BungeeCord and keeps its plugin API, so oyvotifier loads on it through the normal `bungee.yml`
without any Waterfall-specific code. Please be aware of the following:

- **Waterfall is end-of-life.** PaperMC announced its end of life in March 2024 and recommends moving to Velocity.
  oyvotifier can only work as well as the proxy it runs on. Newer Minecraft versions (including 26.x) are not
  supported by Waterfall itself, and users have reported 1.21 backend servers refusing connections on old Waterfall builds.
- oyvotifier's Waterfall support is **compatibility through the BungeeCord API**. It has not been tested on a live
  Waterfall proxy by the maintainer, so please test on a staging proxy and report any problems.
- For new networks, prefer Velocity or current BungeeCord.

**Not supported:** Sponge (removed in this fork), Folia-specific scheduling (not tested), and Minecraft versions older than 1.21.

---

## Installation

1. Download `oyvotifier-1.0.1.jar` (or [build it yourself](#building-from-source)).
2. Place it in the `plugins/` folder of your Bukkit/Spigot/Paper server, BungeeCord proxy, or Velocity proxy.
3. Start the server once. oyvotifier generates its configuration folder, an RSA key pair, and a default token, and
   prints the default token to the console.
4. Open the Votifier **port** (default `8192`, TCP) in your firewall or hosting panel. Voting sites connect to this port.
5. Give each voting site your server's address, the port, and the token (or public key for v1 sites).
6. Install a **vote-listener plugin** that rewards players. oyvotifier only receives and publishes votes; it does not
   reward players by itself.

Data folder locations:

| Platform | Folder |
|---|---|
| Bukkit / Spigot / Paper | `plugins/oyvotifier/` |
| BungeeCord / Waterfall | `plugins/oyvotifier/` |
| Velocity | `plugins/oyvotifier/` |

The `rsa/` subfolder holds the v1 key pair. **Never share your private key or your tokens.** The public key
(`rsa/public.key`) is the only one you give to websites using the legacy v1 protocol.

---

## Configuration

Each platform generates its own config on first start (`config.yml` on Bukkit and BungeeCord, `config.toml` on Velocity).
Common options:

| Option | Default | Description |
|---|---|---|
| `host` | server IP or `0.0.0.0` | Interface to listen on for votes. |
| `port` | `8192` | Port voting sites connect to. A negative value disables the listener (plugin-messaging-only mode). |
| `disable-v1-protocol` | `false` | Disables the legacy v1 protocol. Recommended if all your voting sites support v2 tokens. |
| `tokens` | one `default` token | Map of website name to token. Use a separate token per site. |
| `debug` / `quiet` | debug on | Controls how noisy vote logging is. |
| `forwarding` | `none` | Vote forwarding settings, see below. |

After editing the configuration, run `/nvreload` (Velocity and BungeeCord also reload on their normal reload commands).

---

## Commands and permissions

| Command | Permission | Default | Description |
|---|---|---|---|
| `/nvreload` | `oyvotifier.reload` | op | Reloads the configuration and restarts the listener. |
| `/testvote [username] [serviceName=?] [username=?] [address=?] [localTimestamp=?] [timestamp=?]` | `oyvotifier.testvote` | op | Sends a test vote through your listeners. |

> The permission nodes were `nuvotifier.reload` and `nuvotifier.testvote` upstream. Update any permission-plugin
> entries when migrating.

---

## Vote forwarding (proxy setups)

On a network, install oyvotifier on the **proxy** (BungeeCord, Waterfall or Velocity) and on each **backend** server that should
receive votes.

Typical setup using plugin messaging:

1. On the proxy, set `forwarding.method` to `pluginMessaging` and choose a channel (default: `oyvotifier`).
2. On every backend server, set `forwarding.method` to `pluginMessaging` with the **same channel**.
3. Point voting sites at the **proxy's** address and port.
4. Install your vote-listener plugin on the backend servers.

Important: the default plugin-messaging channel is now **`oyvotifier`** (upstream used `NuVotifier` / `nuvotifier:votes`).
Every server in the chain must use the same channel name, so you cannot mix this fork with an unmodified NuVotifier
using default channel settings unless you set the channel explicitly on both sides.

---

## Building from source

Requirements:

- **JDK 17 or newer** (JDK 21 recommended). Gradle 8.10.2 does not run on JDK 25, so build with JDK 17 to 21.
- An internet connection for the first build (the wrapper downloads Gradle and the dependencies).

```bash
git clone https://github.com/natan307/oyvotifier.git
cd oyvotifier
chmod +x gradlew
./gradlew clean build
```

The output is `universal/build/libs/oyvotifier-1.0.1.jar`.

If unit tests fail on your machine, you can skip them:

```bash
./gradlew build -x test
```

### Building on Android (Termux)

```bash
pkg update && pkg upgrade -y
pkg install openjdk-21 unzip git -y     # use openjdk-17 if 21 is unavailable
git clone https://github.com/natan307/oyvotifier.git
cd oyvotifier
chmod +x gradlew
echo "org.gradle.jvmargs=-Xmx1g" > gradle.properties
./gradlew build --no-daemon -x test
```

The first build can take several minutes. If the build is killed for lack of memory, close other apps and retry.

---

## Changelog

### 1.0.1

- Plugin renamed to **oyvotifier** (previously `oyvotifer` in the 1.0.0 build). Class names, permissions,
  channel, data folder and module names follow the new name.
- Added documented **Waterfall** support (BungeeCord API compatible).
- The shared core and BungeeCord/Waterfall modules are now compiled to Java 8 bytecode for compatibility with older proxy JVMs.
  The Bukkit and Velocity modules stay on Java 17.

### 1.0.0

- First release of the fork: rebrand, 1.21.x / 26.x platform APIs, modern build tooling. See the full list below.

## Changes from NuVotifier 2.7.3

This fork is based on NuVotifier **2.7.3**. The full list of modifications:

### Branding and naming

- Plugin name changed to `oyvotifier`, version `1.0.1`.
- Bukkit main class: `NuVotifierBukkit` renamed to `OyvotifierBukkit`.
- BungeeCord main class: `NuVotifier` renamed to `Oyvotifier`.
- Velocity plugin id and name changed to `oyvotifier`.
- Gradle project and module names changed from `nuvotifier-*` to `oyvotifier-*`.
- Permission nodes changed from `nuvotifier.*` to `oyvotifier.*`.
- Default plugin-messaging channel changed to `oyvotifier`.
- User-facing console and chat messages that mentioned NuVotifier now say oyvotifier.
- Final artifact is `oyvotifier-1.0.1.jar` instead of `nuvotifier.jar`.

### Intentionally unchanged

- **Java packages** (`com.vexsoftware.votifier...`) are unchanged. Renaming them would break every vote-listener
  plugin that compiles against `VotifierEvent`.
- The **`nvreload`** and **`testvote`** command names are unchanged.
- The Votifier protocol itself is unchanged and fully wire-compatible.

### Compatibility

- `plugin.yml` now declares `provides: [Votifier, NuVotifier]` and `api-version: '1.21'`, so plugins that
  `depend` or `softdepend` on `Votifier` or `NuVotifier` still find a provider.
- Plugin metadata for BungeeCord (`bungee.yml`) and Velocity (`@Plugin`) updated to the new name and version.

### Platform support

- Bukkit API dependency moved from Spigot 1.8.8 to **Spigot API 1.21.4**.
- BungeeCord API moved from `1.8-SNAPSHOT` to **`1.21-R0.5-SNAPSHOT`**.
- Velocity API moved from `1.1.0-SNAPSHOT` to **`3.4.0-SNAPSHOT`**.
- **toml4j** is now bundled (and relocated) in the Velocity build, because newer Velocity versions are not guaranteed to provide it.
- The Bukkit forwarding sink no longer imports `org.apache.commons.lang.Validate`, which modern Spigot/Paper APIs do not provide; it uses `java.util.Objects.requireNonNull` instead.
- **Sponge module removed** (it was not part of the target platforms and its API is outdated).

### Build system

- Gradle wrapper upgraded from 7.0.2 to **8.10.2**.
- Shadow plugin upgraded to **8.1.1**; the custom Netty epoll transformer was adapted to its API.
- Java target raised to **17** for the Bukkit and Velocity modules (`options.release = 17`); the shared core and BungeeCord/Waterfall module remain on Java 8.
- **Netty** upgraded from 4.1.49.Final to **4.1.115.Final**; **Gson** from 2.8.0 to **2.10.1**.
- JUnit upgraded to 5.10.2 and Mockito to 5.11.0.
- SpotBugs and Blossom plugins removed; the Velocity version is now set directly in the `@Plugin` annotation.
- Only the universal fat JAR is produced as a deliverable (`oyvotifier-1.0.1.jar`).
- Removed the upstream `dev/` test servers, CI files (`.github`, `Jenkinsfile`) and publishing script.

---

## Migrating from Votifier / NuVotifier

1. Stop the server.
2. Remove the old Votifier or NuVotifier JAR.
3. Copy your old plugin folder's `config.yml` and `rsa/` directory into `plugins/oyvotifier/`. This keeps your
   tokens and v1 key pair, so voting sites do not need to be reconfigured.
4. Update permission entries from `nuvotifier.*` to `oyvotifier.*`.
5. If you use plugin-messaging forwarding, either keep the old channel name in the config on every server or set the new
   default (`oyvotifier`) everywhere.
6. Install `oyvotifier-1.0.1.jar` and start the server.

---

## For plugin developers

Vote listeners work exactly as they do with Votifier and NuVotifier, because the packages are unchanged.

**Bukkit / Spigot / Paper**

```java
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class VoteListener implements Listener {
    @EventHandler
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();
        String player = vote.getUsername();
        String site = vote.getServiceName();
        // reward the player
    }
}
```

**BungeeCord:** `com.vexsoftware.votifier.bungee.events.VotifierEvent`
**Velocity:** `com.vexsoftware.votifier.velocity.event.VotifierEvent`

Depend on the plugin in your `plugin.yml` with `softdepend: [Votifier]` (works through the `provides` entry), or with
`depend: [oyvotifier]` if you want to require this fork specifically. Use `compileOnly` for the API artifact
(`oyvotifier-api`) when compiling.

---

## Known limitations

- This fork has been compiled successfully, but runtime behavior on every 1.21.x and 26.x server build has **not** been
  exhaustively tested. Please test on a staging server before production, and open an issue if you find a problem.
- Waterfall is end-of-life and has not been tested live with this plugin.
- Native epoll transport is bundled for **Linux x86_64** only; other platforms automatically fall back to NIO.
- Sponge and Folia-specific features are not supported.
- Unit tests were written for the original dependency versions; if they fail with the upgraded libraries, use `-x test`
  and open an issue or pull request.

---

## Credits and license

oyvotifier is a derivative work of **NuVotifier**, which is itself based on the original **Votifier**.

- Original Votifier: Blake Beaupain (blakeman8192), Kramer Campbell, Vex Software LLC
- NuVotifier: Joe Hirschfeld (Ichbinjoe), tuxed, and contributors
- Upstream project: <https://github.com/NuVotifier/NuVotifier>

This project is licensed under the **GNU General Public License v3.0** (or later), the same license as upstream.
See [LICENSE](LICENSE). Original copyright notices are retained in the source files. If you distribute modified
versions, you must also make the corresponding source code available under the same license.
