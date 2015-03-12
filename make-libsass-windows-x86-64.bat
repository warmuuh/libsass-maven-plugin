
pushd
cd src/libsass
set BUILD=shared
make lib/libsass.dll
copy lib\libsass.dll main\resources\win32-x86-64\sass.dll
popd