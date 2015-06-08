REM you may need to modify MAKEFILE to use "LDLIBS = -static-libstdc++ -lm" instead of "LDLIBS = -lstdc++ -lm"
pushd
cd src/libsass
set BUILD=shared
make lib/libsass.dll
copy lib\libsass.dll main\resources\win32-x86-64\sass.dll
popd