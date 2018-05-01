cmake_minimum_required(VERSION 3.4.1)
include_directories(${CMAKE_SOURCE_DIR}//include/librtmp)

add_library( rtmp_device
             SHARED

             rtmp_jni.cpp
             rtmp_jni.h
             rtmp_interface.cpp
             rtmp_interface.h
             librtmp/amf.c
             librtmp/amf.h
             librtmp/bytes.h
             librtmp/dh.h
             librtmp/dhgroups.h
             librtmp/handshake.h
             librtmp/hashswf.c
             librtmp/http.h
             librtmp/log.c
             librtmp/log.h
             librtmp/parseurl.c
             librtmp/rtmp.c
             librtmp/rtmp.h
             librtmp/rtmp_sys.h
            )

include_directories(src/main/cpp/librtmp)

add_definitions(-DNO_CRYPTO)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_link_libraries( # Specifies the target library.
                       rtmp_device

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
