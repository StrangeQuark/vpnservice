#!/bin/sh
set -eu

routerConfig=/tmp/nginx.conf
dnsResolver=$(awk '/^nameserver/{print $2; exit}' /etc/resolv.conf)
routerBindAddress=${VPN_ROUTER_BIND_ADDRESS:-${VPN_NETWORK_PREFIX}.1}
routerPort=${VPN_ROUTER_PORT:-80}

until ip addr show wg0 | grep -q "${VPN_NETWORK_PREFIX}.1"; do
    sleep 1
done

mkdir -p /tmp/client_temp /tmp/proxy_temp /tmp/fastcgi_temp /tmp/uwsgi_temp /tmp/scgi_temp

cat > "$routerConfig" <<EOF
pid /tmp/nginx.pid;
events {}
http {
    resolver ${dnsResolver} ipv6=off valid=10s;
    client_body_temp_path /tmp/client_temp;
    proxy_temp_path /tmp/proxy_temp;
    fastcgi_temp_path /tmp/fastcgi_temp;
    uwsgi_temp_path /tmp/uwsgi_temp;
    scgi_temp_path /tmp/scgi_temp;

    server {
        listen ${routerBindAddress}:${routerPort};
        server_tokens off;
EOF

appendRoute() {
    if [ "$1" != "true" ]; then
        return
    fi

    cat >> "$routerConfig" <<EOF
        location $2 {
            set \$upstream $3;
            proxy_pass \$upstream;
            proxy_set_header Host \$host;
            proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
            proxy_set_header Origin "";
        }
EOF
}

if [ "${GATEWAYSERVICE_INTEGRATION:-false}" = "true" ]; then
    appendRoute true "/" "$GATEWAY_SERVICE_URL"
else
    appendRoute "${AUTHSERVICE_INTEGRATION:-false}" "/api/auth/" "${AUTH_SERVICE_URL:-http://auth-service:6001}"
    appendRoute "${EMAILSERVICE_INTEGRATION:-false}" "/api/email/" "${EMAIL_SERVICE_URL:-http://email-service:6005}"
    appendRoute "${FILESERVICE_INTEGRATION:-false}" "/api/file/" "${FILE_SERVICE_URL:-http://file-service:6010}"
    appendRoute "${VAULTSERVICE_INTEGRATION:-false}" "/api/vault/" "${VAULT_SERVICE_URL:-http://vault-service:6020}"
    appendRoute "${TELEMETRYSERVICE_INTEGRATION:-false}" "/api/telemetry/" "${TELEMETRY_SERVICE_URL:-http://telemetry-service:6050}"
    appendRoute true "/api/vpn/" "${VPN_SERVICE_URL:-http://vpn-service:6040}"
    appendRoute "${REACTSERVICE_INTEGRATION:-false}" "/" "${REACT_SERVICE_URL:-http://react-service}"
fi

cat >> "$routerConfig" <<EOF
    }
}
EOF

nginx -c "$routerConfig" -g "daemon off;"
