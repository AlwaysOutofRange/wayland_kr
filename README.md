# Wayland-kt

A test project exploring Wayland protocol implementation in Kotlin. Currently displays an animated GIF as a proof of
concept for window creation, shared memory management, and protocol communication.

## Requirements

- Linux with Wayland compositor
- JDK 11+
- GCC for native compilation

## Building

```bash
# Compile native code
cd src/main/c
./compile.sh

# Build Kotlin code
./gradlew build
```

## Running

Place a test GIF file as `meme.gif` in the project directory and run:

```bash
./gradlew run
```

The required JVM flags for file descriptor access through reflection are configured in the Gradle build file.

## Current State

This is a work in progress testing:

- Basic Wayland protocol communication
- XDG shell window creation
- Shared memory buffer management
- Event handling and callbacks

## Todo

- Replace file descriptor reflection with proper JNI
- Improve error handling
- Add window resize support
- Clean up protocol implementations
- Proper documentation

## License

MIT License