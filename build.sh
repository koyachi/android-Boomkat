#!/bin/sh

make -C command && cp ./command/build/boomkat.arm ./assets/boomkat-cli && ant debug install
