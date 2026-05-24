## Tech Stack

Flowit main server는 Java 17, Spring Boot, MySQL, Redis, Spring REST Docs 기반으로 구성됩니다.<br>
Flowit main server is built with Java 17, Spring Boot, MySQL, Redis, and Spring REST Docs.

<details>
<summary>Tech stack details</summary>

### Backend

| Category | Technology        |
|---|-------------------|
| Language | Java 17           |
| Framework | Spring Boot       |
| API | Spring Web        |
| Realtime API | WebSocket         |
| Security | Spring Security   |
| Validation | Bean Validation   |
| ORM | Spring Data JPA   |
| Cache / Messaging | Spring Data Redis |
| Mail | Java Mail Sender  |

### Database

| Category | Technology |
|---|---|
| RDBMS | MySQL |
| Migration | Flyway Migration |

### Observability

| Category | Technology |
|---|---|
| Health Check / Metrics | Spring Boot Actuator |
| Metrics Collection | Prometheus |
| Metrics Visualization | Grafana |
| Error Monitoring | Sentry |

### Documentation

| Category | Technology |
|---|---|
| API Documentation | Spring REST Docs |

### Development

| Category | Technology |
|---|---|
| Boilerplate Reduction | Lombok |
| Developer Tooling | Spring Boot DevTools |

</details>

## API Documentation

API 문서는 애플리케이션 기동 후 `/docs`에서 확인할 수 있습니다.<br>
You can view the API documentation at `/docs` after the application starts.

```text
http://localhost:8080/docs
http://localhost:8080/docs/index.html
```

`/docs`는 API 문서 목차 페이지입니다. API별 상세 문서는 목차에서 이동하십시오.<br>
`/docs` is the API documentation index page. Use the index to navigate to each API detail page.

예시 상세 문서 경로는 아래와 같습니다.<br>
An example detail page is available at:

```text
http://localhost:8080/docs/docs-preview.html
```

문서 산출물은 Spring REST Docs 테스트와 Asciidoctor를 통해 생성되며, `localStart`, `bootRun`, `bootJar` 실행 시 애플리케이션 정적 리소스에 함께 복사됩니다.<br>
The documentation is generated through Spring REST Docs tests and Asciidoctor, then copied into application static resources when running `localStart`, `bootRun`, or `bootJar`.

<details>
<summary>API documentation generation commands</summary>

```bash
./gradlew restdocsTest
```

`restdocsTest`는 API 문서에 사용할 Spring REST Docs 스니펫을 생성합니다.<br>
`restdocsTest` generates Spring REST Docs snippets for the API documentation.

```bash
./gradlew bootJar
```

`bootJar`는 스니펫을 기반으로 HTML 문서를 생성하고, 실행 가능한 JAR에 문서를 포함합니다.<br>
`bootJar` generates HTML documentation from snippets and includes it in the executable JAR.

</details>

## Local Infrastructure Initialization

> [!WARNING]
>
> 필수: Docker Desktop을 반드시 실행하십시오.<br>
> Required: Docker Desktop must be running.
>
> 필수: JDK 17이 반드시 설치되어 있어야 합니다.<br>
> Required: JDK 17 must be installed.

IDE를 별도로 설치하지 않고 터미널에서 로컬 환경을 실행할 때는 Gradle 명령어를 우선적으로 사용하십시오.<br>
Prefer the Gradle commands when you want to run the local environment from a terminal without an IDE.

```bash
./gradlew localStart
```

`localStart`는 Docker Compose 인프라를 실행한 뒤 Spring Boot 애플리케이션을 백그라운드로 실행합니다.<br>
`localStart` starts the Docker Compose infrastructure and then starts the Spring Boot application in the background.

```bash
./gradlew localStatus
```

`localStatus`는 Spring Boot 프로세스 상태, actuator health 상태, 로그 경로, Docker Compose 서비스 상태를 출력합니다.<br>
`localStatus` shows the Spring Boot process status, actuator health status, log path, and Docker Compose service status.

```bash
./gradlew localStop
```

`localStop`은 Spring Boot 애플리케이션을 종료하고 `docker compose down`을 실행합니다. Docker 볼륨은 유지됩니다.<br>
`localStop` stops the Spring Boot application and runs `docker compose down`. Docker volumes are preserved.

`docker compose up -d`와 `./gradlew bootRun`을 따로 실행하는 방식과 `./gradlew localStart`로 실행하는 방식은 기동되는 구성에 차이가 없습니다.<br>
Running `docker compose up -d` and `./gradlew bootRun` separately starts the same local components as `./gradlew localStart`.

<details>
<summary>Additional Gradle local commands</summary>

Mac에서 `./gradlew` 실행 권한이 없다면 한 번만 아래 명령을 실행하십시오.<br>
If `./gradlew` is not executable on Mac, run the following command once.

```bash
chmod +x gradlew
```

애플리케이션 프로세스 정보와 로그는 `build/local` 아래에 기록됩니다.<br>
The application process metadata and log are written under `build/local`.

```bash
./gradlew localInfraStart
```

`localInfraStart`는 Spring Boot 애플리케이션 없이 Docker Compose 인프라만 실행합니다.<br>
`localInfraStart` starts only the Docker Compose infrastructure without the Spring Boot application.

```bash
./gradlew localInfraStop
```

`localInfraStop`은 Spring Boot 애플리케이션과 무관하게 Docker Compose 인프라만 종료합니다. Docker 볼륨은 유지됩니다.<br>
`localInfraStop` stops only the Docker Compose infrastructure, independently of the Spring Boot application. Docker volumes are preserved.

Gradle 로컬 명령어는 OS에 맞는 Docker Compose 설정을 자동으로 선택합니다.<br>
The Gradle local commands automatically select the Docker Compose configuration for the current OS.

- Windows/Mac: `compose.yaml`
- Linux: `compose.yaml` + `compose.linux.yaml`

</details>

<details>
<summary>Manual local commands</summary>

Gradle 명령어를 사용하지 않을 경우, 루트 디렉토리에서 아래 명령을 순서대로 실행하십시오.<br>
If you do not use the Gradle commands, run the following commands from the project root.

Windows 또는 Mac에서는 아래 명령을 실행하십시오.<br>
On Windows or Mac, run:

```bash
docker compose up -d
```

Linux에서는 `host.docker.internal`을 사용하기 위해 override 파일을 함께 지정하십시오.<br>
On Linux, include the override file so `host.docker.internal` is mapped correctly.

```bash
docker compose -f compose.yaml -f compose.linux.yaml up -d
```

Spring Boot 애플리케이션은 `compose.yaml`에 포함되어 있지 않으므로 별도로 실행하십시오.<br>
The Spring Boot application is not included in `compose.yaml`, so run it separately.

```bash
./gradlew bootRun
```

</details>

<details>
<summary>Local development endpoints</summary>

### Application

- Host: localhost
- Port: 8080

### Database

- Host: localhost
- Port: 3306
- Database: project_flowit
- Username: flowit_dev
- Password: flowitDevPass

### Redis

- Host: localhost
- Port: 6379
- Password: flowitLocalDev

### Actuator

- Host: localhost
- Port: 8081
- Binding: 127.0.0.1
- Exposed endpoints: health, prometheus

### Prometheus

- Host: localhost
- Port: 9090
- Binding: 127.0.0.1
- Scrape target: host.docker.internal:8081/actuator/prometheus

### Grafana

- Host: localhost
- Port: 3050
- Binding: 127.0.0.1
- Username: admin
- Password: flowitLocalAdmin

Redis 컨테이너는 로컬 개발 환경에서 캐시 및 메시징 기능을 사용하기 위해 함께 생성됩니다.<br>
The Redis container is created together for local cache and messaging features.

Prometheus 컨테이너는 Spring Boot Actuator metrics를 수집하고, Grafana 컨테이너는 Prometheus datasource가 자동 등록된 상태로 생성됩니다.<br>
The Prometheus container collects Spring Boot Actuator metrics, and the Grafana container is created with the Prometheus datasource provisioned.

MySQL, Redis, Prometheus, Grafana는 모두 `127.0.0.1`에만 바인딩되므로 로컬에서만 접근할 수 있습니다.<br>
MySQL, Redis, Prometheus, and Grafana are bound only to `127.0.0.1`, so they are accessible only from the local.

Spring Boot Actuator 역시 `127.0.0.1`에 바인딩되므로 로컬 Prometheus에서만 metrics를 수집할 수 있습니다.<br>
Spring Boot Actuator is also bound to `127.0.0.1`, so metrics can be collected only by the local Prometheus.

</details>

<details>
<summary>Container maintenance commands</summary>

### Recreate container

초기화 SQL을 다시 실행하거나 로컬 데이터베이스, Redis, Prometheus, Grafana 데이터를 초기 상태로 되돌려야 할 때 사용하십시오.<br>
Use this when you need to re-run the initialization SQL or reset the local database, Redis, Prometheus, and Grafana data.

```bash
docker compose down -v
docker compose up -d
```

`-v` 옵션은 MySQL, Redis, Prometheus, Grafana 데이터 볼륨을 삭제하므로 기존 로컬 데이터가 모두 제거됩니다.<br>
The `-v` option removes the MySQL, Redis, Prometheus, and Grafana data volumes, so all existing local data will be deleted.

### Stop container

컨테이너를 중지할 때 사용하십시오. 데이터 볼륨은 유지되므로 로컬 데이터는 보존됩니다.<br>
Use this to stop the containers. The data volumes are preserved, so local data will remain.

```bash
docker compose down
```

</details>

## Flyway Migration

> [!WARNING]
>
> DDL 및 ALTER 문은 Flyway Migration 라이브러리를 통해 애플리케이션 실행 시 데이터베이스에 자동으로 반영됩니다.<br>
> DDL and ALTER statements are automatically applied to the database through the Flyway Migration library when the application runs.
>
> 별도의 SQL 파일을 데이터베이스에 강제로 실행하지 마십시오.<br>
> Do not force-run separate SQL files directly against the database.
