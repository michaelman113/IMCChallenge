CXX = g++-14
CXXFLAGS = -Wall -std=c++17 -O2 -I/opt/homebrew/Cellar/gcc/14.2.0_1/include/c++/14.2.0

# Target executable
all: main clean_obj

# Linking step
main: graph.o main.o
	$(CXX) $(CXXFLAGS) graph.o main.o -o main

# Compile graph.cpp
graph.o: graph.cpp graph.hpp
	$(CXX) $(CXXFLAGS) -c graph.cpp -o graph.o

# Compile main.cpp
main.o: main.cpp graph.hpp
	$(CXX) $(CXXFLAGS) -c main.cpp -o main.o

# Clean all files
clean:
	rm -f *.o main

# Clean object files
clean_obj:
	rm -f *.o