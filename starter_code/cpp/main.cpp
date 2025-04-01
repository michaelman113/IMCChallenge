#include "graph.hpp"

#include <iostream>
#include <string>

void run_solver(Graph& aGraph)
{
    for (std::size_t myFirstIndex = 0; myFirstIndex < aGraph.size(); myFirstIndex++)
    {
        for (std::size_t mySecondIndex = myFirstIndex + 1; mySecondIndex < aGraph.size(); mySecondIndex++)
        {
            aGraph.add_edge(myFirstIndex, mySecondIndex);
        }
    }
}

int main(int argc, char* argv[]) { 
    if (argc > 3)
    {
        std::cout << "Program only supports two arguments:" << std::endl;
        std::cout << "1. Path to the input tsv (dataset.tsv)" << std::endl;
        std::cout << "2. Output filename to write solution" << std::endl;
    }

    std::string myTsvInputFilename = argc >= 2 ? argv[1] : "data/dataset.tsv";
    std::string myTsvOutputFilename = argc >= 3 ? argv[2] : "data/solution.tsv";
    
    Graph myGraph(myTsvInputFilename);
    run_solver(myGraph);
    std::cout << "This solution will score: " << myGraph.score() << std::endl;
    myGraph.write_output(myTsvOutputFilename);
    return 0;
}