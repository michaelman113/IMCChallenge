#pragma once

#include <vector>
#include <unordered_map>

#include <fstream>
#include <sstream>
#include <limits>
#include <iostream>
#include <cassert>
#include <cstdint>

constexpr uint32_t max_latency = 10000;

class Graph {
public:
    Graph(std::string tsv_path); 

    size_t size();

    const std::vector<std::string>& nodes();

    bool add_edge(size_t node1, size_t node2);
    bool add_edge(std::string node1, std::string node2);

    bool remove_edge(size_t node1, size_t node2);
    bool remove_edge(std::string node1, std::string node2);

    int64_t score();
    void solution_from_tsv(std::string tsv_path);

    bool has_connection(size_t i, size_t j);
    uint32_t connection_cost(size_t i, size_t j);
    uint32_t connection_latency(size_t i, size_t j);
    bool connection_bought(size_t i, size_t j);

    void clear_solution();
    void set_connection_cost(size_t i, size_t j, uint32_t cost);

    void write_input(std::string tsv_path);
    void write_output(std::string tsv_path);

private:
    struct Connection {
        uint32_t latency;
        uint32_t cost;
    };
    std::vector<std::string> node_idx_to_name;
    std::unordered_map<std::string, size_t> node_name_to_idx;

    std::vector<std::vector<uint32_t>> messages;
    std::vector<std::vector<Connection>> connections;
    std::vector<std::vector<uint32_t>> latencies;
    std::vector<std::vector<size_t>> neighbors;
    std::vector<std::vector<bool>> bought;
    int64_t score_;
    bool score_dirty;
};
