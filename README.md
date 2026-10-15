<h1 align="center" style="font-weight: normal;"><b>HMC-Specifics</b></h1>
<p align="center">Version specific implementations of the HeadlessMc runtime.</p>
<p align="center"><a href="https://github.com/headlesshq/mc-runtime-test">Mc-Runtime-Test</a> | <a href="https://github.com/headlesshq/headlessmc">HMC</a> | HMC-Specifics | <a href="https://github.com/headlesshq/hmc-optimizations">HMC-Optimizations</a></p>

<div align="center">

[![CodeFactor](https://www.codefactor.io/repository/github/headlesshq/hmc-specifics/badge/main)](https://www.codefactor.io/repository/github/headlesshq/hmc-specifics/overview/main)
[![GitHub All Releases](https://img.shields.io/github/downloads/headlesshq/hmc-specifics/total.svg)](https://github.com/headlesshq/hmc-specifics/releases)
![GitHub](https://img.shields.io/github/license/headlesshq/hmc-specifics)
![Github last-commit](https://img.shields.io/github/last-commit/headlesshq/hmc-specifics)

</div>

Version specific implementations of the [HeadlessMc](https://github.com/headlesshq/HeadlessMc) runtime. Just place the
jar for your version in your mods folder. Then you need to find a way to send commands to your running Minecraft game.
The easiest way is probably to just launch it with HeadlessMc. The `-commands` flag is not required for HeadlessMc in
that case.

| Name       | Description                                            | Args/Flags                              |
|------------|--------------------------------------------------------|-----------------------------------------|
| gui        | Lists all currently displayed gui elements.            |                                         |
| click      | Clicks an element on Minecrafts screen.                | \<id/p\> \<x\> \<y\> \<button\> -enable |
| text       | Sets the contents of a text field.                     | \<id\> \<text\>                         |
| menu       | Opens the ingame menu.                                 |                                         |
| quit       | Quits the game.                                        |                                         |
| render     | Dumps all strings rendered by Minecrafts FontRenderer. | \<time\> -f -t                          |
| close      | Closes the menu if ingame.                             |                                         |
| connect    | Connects you to a server.                              | \<ip\> \<port\>                         |
| disconnect | Disconnects you from a server.                         |                                         |
| respawn    | Respawns you if you are dead. `respawn auto` toggles AutoRespawn. | \<now/auto/status\> \<on/off\> |
| metrics    | Displays metrics, e.g. TPS, ping, players, FPS, memory. `metrics tps ping` selects metrics by name, group or last part of the name. | \<metric...\> --list --json |
| account    | Manage accounts, alias login. Subcommands: login, list, select, remove, refresh, current, token, autorefresh | -p \<provider\> |
| msg or .   | Sends a chat message.                                  | \<message\>                             |
| /          | Sends a chat command.                                  | \<command\>                             |

You can find a good example on how to use these
commands [here](https://github.com/headlesshq/HeadlessMc/issues/8#issuecomment-1159378478).

The HMC-Specifics currently support the following Minecraft versions and modloaders:
| Version | Forge | Fabric | NeoForge |
| :-: | :-: | :-: | :-: |
| 26.3 | :white_check_mark:  | :white_check_mark:  | :white_check_mark: |
| 26.2 | :white_check_mark:  | :white_check_mark:  | :white_check_mark: |
| 26.1 - 26.1.2 | :white_check_mark:  | :white_check_mark:  | :white_check_mark: |
| 1.21 - 1.21.11 | :white_check_mark:  | :white_check_mark:  | :white_check_mark: |
| 1.20.6 | :white_check_mark:  | :white_check_mark:  | :white_check_mark:  |
| 1.19.4 | :white_check_mark:  | :white_check_mark:  | - |
| 1.18.2 | :white_check_mark:  | :white_check_mark:  | - |
| 1.17.1 | :white_check_mark:  | :white_check_mark:  | - |
| 1.16.5 | :white_check_mark:  | :white_check_mark:  | - |
| 1.12.2 | :white_check_mark:  | :warning:  | - |
| 1.8.9 | :white_check_mark:  | :warning:  | - |
| 1.7.10 | :white_check_mark:  | :warning:  | - |

Versions marked with :warning: have not been tested yet, due to not being supported by HeadlessMC, e.g. fabric legacy
versions.

### AutoRespawn
`respawn auto` toggles AutoRespawn, `respawn auto on|off` turns it on or off, and `respawn status` shows whether it is on.
To turn AutoRespawn on when the game starts, launch the game with `-Dhmc.respawn.auto=true`.

### Adding commands from your own mod
HMC-Specifics uses [picocli](https://picocli.info/) commands. To add your own, write a picocli command
and list it in a file called `META-INF/hmc-specifics-commands` in your mod jar, one class name per line.
Subcommands of existing commands are prefixed with the names of their parent commands:
```
com.example.MyCommand
account com.example.MyAccountSubCommand
```
Commands are instantiated with a constructor that takes a `me.earth.headlessmc.mc.commands.MinecraftContext`,
or with a no-arg constructor. If your command (or e.g. its `completionCandidates`) needs anything else,
implement `me.earth.headlessmc.mc.commands.CommandFactory` and register it in
`META-INF/services/me.earth.headlessmc.mc.commands.CommandFactory`.
Implement `me.earth.headlessmc.mc.commands.ScheduledCommand` if your command needs to run on Minecraft's main thread.
If HMC-Specifics is not installed, these files are simply ignored.

### Metrics
The `metrics` command displays values collected from the client, e.g. `server.tps` (estimated from the game time
the server sends every second), `server.ping`, `players.list`, `client.fps`, `client.mspt`, `world.*`, `player.*`
and `jvm.*`. Use `metrics --list` to list all metrics. Other mods can access them through
`me.earth.headlessmc.mc.metrics.MetricsService.getInstance()`, most metrics have to be collected on Minecraft's main thread:
```java
MetricsService metrics = MetricsService.getInstance();
Future<Map<String, Object>> values = minecraft.schedule(() -> metrics.collect(metrics.select("server.tps", "players")));
```
Register your own metrics with `MetricsService#register(Metric.of(name, description, supplier))`, or implement
`me.earth.headlessmc.mc.metrics.MetricsProvider` and list it in
`META-INF/services/me.earth.headlessmc.mc.metrics.MetricsProvider`.
The raw, typed values are available through `Minecraft#getMetricsSource()` and `MetricsTracker.getInstance()`.
