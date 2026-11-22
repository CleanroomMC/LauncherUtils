## LauncherUtils

Reads and writes Minecraft launcher metadata: version JSON, rules, natives, Mojang's version manifest, asset indexes,
and MultiMC and Prism packs. Built on [PlatformUtils](https://github.com/CleanroomMC/PlatformUtils), Java 8, and
Gson 2.8.0 or newer, which consumers supply.

### Developer Usage

Maven:
```xml
<repository>
  <id>cleanroommc</id>
  <name>CleanroomMC</name>
  <url>https://maven.cleanroommc.com</url>
</repository>
```

```xml
<dependency>
    <groupId>com.cleanroommc</groupId>
    <artifactId>launcher-utils</artifactId>
    <version>0.1.0</version>
</dependency>
```

Gradle:
```groovy
repositories {
    maven {
        url = 'https://maven.cleanroommc.com'
    }
}

dependencies {
    implementation 'com.cleanroommc:launcher-utils:0.1.0'
}
```

### Reading

```java
VersionJson version = VersionJson.read(reader);
RuleContext context = RuleContext.current().feature("has_custom_resolution", true);

for (Library library : version.libraries(context)) {
    Download artifact = library.artifact();
    Download natives = library.nativeDownload(context);
}

Substitutions substitutions = Substitutions.create()
        .put(Substitutions.AUTH_PLAYER_NAME, "Developer")
        .put(Substitutions.NATIVES_DIRECTORY, nativesDirectory);
List<String> jvm = version.jvmArguments(context, substitutions);
List<String> game = version.gameArguments(context, substitutions);
```

MultiMC and Prism patches match bare system names on x86 and x64 only:

```java
MmcPatch patch = MmcPatch.read(reader);
List<Library> libraries = patch.libraries(RuleContext.current().dialect(Dialect.MMC));
```

### Writing

```java
Library library = Library.of(Coordinate.parse("org.lwjgl:lwjgl:3.3.3:natives-freebsd"))
        .artifact(new Download().path(path).sha1(sha1).size(size).url(url))
        .rule(Rule.allow().os(OperatingSystem.FREE_BSD).arch(Architecture.X64));

String json = LauncherGson.write(new VersionJson().id("example").library(library));
```

### Names

| System  | Rules and natives keys | MultiMC and Prism   |
|---------|------------------------|---------------------|
| Windows | `windows`              | `windows-x86_64`    |
| macOS   | `osx`                  | `osx-arm64`         |
| Linux   | `linux`                | `linux-arm64`       |
| FreeBSD | `freebsd`              | `freebsd-x86_64`    |

FreeBSD never matches a Linux rule or takes Linux natives.
