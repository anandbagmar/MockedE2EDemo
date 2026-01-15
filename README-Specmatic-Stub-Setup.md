# Specmatic stub server setup (required for WebView flows)

The mobile apps in this repo load the recharge flow inside a WebView that points to a local endpoint (`http://localhost:8080`).  
To make this work on a freshly cloned repo, you must start the Specmatic proxy/stub server and wire the emulator/device networking.

## Prerequisites

1. Clone the Specmatic recordings repo:

```bash

git clone https://github.com/anandbagmar/testing-negative-scenarios-in-prod/
```

2. Download the latest **Specmatic Studio** JAR from:

- https://repo.specmatic.io/#/releases/io/specmatic/studio/specmatic-studio/

Example: `specmatic-studio-1.16.0.jar`

You can also use curl to download it directly:

```bash

VERSION=$(
  curl -s https://repo.specmatic.io/releases/io/specmatic/studio/specmatic-studio/maven-metadata.xml \
  | sed -n 's:.*<latest>\(.*\)</latest>.*:\1:p'
)

echo "Latest Specmatic Studio version: $VERSION"

curl -L -o "lib/specmatic-studio-${VERSION}.jar" \
  "https://repo.specmatic.io/releases/io/specmatic/studio/specmatic-studio/${VERSION}/specmatic-studio-${VERSION}.jar"

echo "Downloaded: lib/specmatic-studio-${VERSION}.jar"
```

3. Place the JAR in the cloned repo under:

```text
testing-negative-scenarios-in-prod/lib/
```

## Start Specmatic stub/proxy server

Run the following commands from the repo root:

> **Note**: Replace the version of the jar file appropriately

```bash

cd testing-negative-scenarios-in-prod
java -jar lib/specmatic-studio-1.16.0.jar proxy
```

This will load Specmatic mocks from:

```text
testing-negative-scenarios-in-prod/lib/proxy_recordings_examples/
```

## Android setup

1. Start an Android emulator
2. Setup ADB reverse proxy so the emulator can access your host machine:

```bash

adb reverse tcp:8080 tcp:8080
```

Now `http://localhost:8080` inside the emulator will resolve to `localhost:8080` on your laptop (where Specmatic proxy is running).
