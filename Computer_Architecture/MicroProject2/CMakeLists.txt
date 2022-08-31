cmake_minimum_required(VERSION 3.17)
project(MicroProject2_196_Tatarinov_1)

set(CMAKE_CXX_STANDARD 20)

add_executable(MicroProject2_196_Tatarinov_1 main.cpp)

find_package (Threads REQUIRED)
target_link_libraries(MicroProject2_196_Tatarinov_1 pthread)