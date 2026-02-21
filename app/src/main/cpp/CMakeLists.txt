cmake_minimum_required(VERSION 3.22.1)

project("koder")

add_library(koder_native SHARED
    native-lib.cpp
)

find_library(log-lib log)

target_link_libraries(koder_native ${log-lib})
