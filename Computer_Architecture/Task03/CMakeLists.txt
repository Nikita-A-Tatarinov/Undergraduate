cmake_minimum_required(VERSION 3.17)
project(task03_196_Tatarinov_23)

set(CMAKE_CXX_STANDARD 20)

add_executable(task03_196_Tatarinov_23 main.cpp)
find_package (Threads REQUIRED)

target_link_libraries(task03_196_Tatarinov_23 pthread)