#!/usr/bin/env bash

cd src;

# We use:
# - BUILD="shared" to make sure that we build a shared system library
# - CXX=g++-4.6 to make sure that the library is linked against a version of 
#               libstdc++.so.6 that is old enough to be widely compatible
# - CC=gcc-4.4  same as for CXX but for libc.so.6
BUILD="shared" make -C libsass CXX=g++-4.6 CC=gcc-4.4;

cp libsass/lib/libsass.so main/resources/linux-x86-64
cd ..;