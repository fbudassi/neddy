#!/bin/sh
for i in `seq 40 55`; do ifconfig eth0:$i 192.168.1.$i up ; done
echo "1024 65535" > /proc/sys/net/ipv4/ip_local_port_range
swapoff -a