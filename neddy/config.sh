#!/bin/sh
swapoff -a

sysctl vm.max_map_count=1000000
sysctl kernel.threads-max=1000000
sysctl kernel.pid_max=1000000
