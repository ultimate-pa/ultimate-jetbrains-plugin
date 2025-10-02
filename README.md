# Ultimate Automizer JetBrains Plugin
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)


A plugin that makes Ultimate Automizer available in JetBrains IDEs.

## Requirements

- JetBrains IDE 2025.1.1+ or compatible
- Java 21+ for the IDE and development
- An Ultimate WebBackend (local or remote) reachable from your machine

## Installation (from sources)

1. Run the `buildPlugin` Gradle task.
2. Locate the generated `.zip` archive in `build/distributions/` .
3. In the target IDE: Settings > Plugins > Gear icon > Install Plugin from Disk... > Select the `.zip` > Apply.


## Usage

1. Open a project with the code you want to verify.
2. Open the Ultimate tool window, located bottom left by default, and open the settings menu.
3. Change WebBackend API URL and the Ultimate config, then apply.
4. Click run and wait for the results.

## Contributing
See the [IntelliJ Platform Plugin SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html) for reference.  
Contributions that modify the UI should follow the [UI Guidelines](https://plugins.jetbrains.com/docs/intellij/ui-guidelines-welcome.html).  

For development, the `runIde` Gradle task will download and start a CLion sandbox instance with the plugin installed.  
The sandbox can be used to quickly test changes.  
