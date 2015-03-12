#!/usr/bin/env bash

cd src/libsass;

autoreconf --force --install

./configure --disable-tests --enable-shared 

make -j5

cd ..;

cp libsass/.libs/libsass.0.dylib main/resources/darwin/libsass.dylib
cd ..;
