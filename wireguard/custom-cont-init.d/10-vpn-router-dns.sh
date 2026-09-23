#!/bin/sh

corefile=/config/coredns/Corefile

if ! grep -q "$VPN_ROUTER_HOSTNAME" "$corefile"; then
    sed -i "/^[[:space:]]*health$/a\\
    hosts {\\
        ${VPN_NETWORK_PREFIX}.1 ${VPN_ROUTER_HOSTNAME}\\
        fallthrough\\
    }" "$corefile"
fi
