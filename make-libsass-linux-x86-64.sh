#!/usr/bin/env bash

cd src;
BUILD="shared" make -C libsass;
cp libsass/lib/libsass.so main/resources/linux-x86-64
cd ..;