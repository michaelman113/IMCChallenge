#include "Graph.hpp"

Graph::Graph(std::string tsv_path) {
    std::ifstream tsv(tsv_path);
    std::string line;

    std::vector<std::tuple<size_t, size_t, uint32_t, uint32_t, uint32_t>> edges;
    std::string node1, node2;
    std::uint32_t latency, cost, num_messages;

    while(tsv >> node1 >> node2 >> latency >> cost >> num_messages) {
        for(auto node : {node1, node2}) {
            if(node_name_to_idx.find(node) == node_name_to_idx.end()) {
                node_name_to_idx[node] = node_idx_to_name.size();
                node_idx_to_name.push_back(node);
            }
        }

        edges.emplace_back(node_name_to_idx[node1], node_name_to_idx[node2], latency, cost, num_messages);
    }

    size_t size = node_idx_to_name.size();

    messages = {size, std::vector<uint32_t>(size)};
    connections = {size, std::vector<Connection>(size)};
    latencies = {size, std::vector<uint32_t>(size, max_latency)};
    neighbors = {size, std::vector<size_t>()};
    bought = {size, std::vector<bool>(size)};

    for(auto [n1, n2, latency, cost, num_messages] : edges) {
        messages[n1][n2] = num_messages;
        messages[n2][n1] = num_messages;

        if (latency > 0) {
            connections[n1][n2].latency = latency;
            connections[n2][n1].latency = latency;

            connections[n1][n2].cost = cost;
            connections[n2][n1].cost = cost;

            neighbors[n1].push_back(n2);
            neighbors[n2].push_back(n1);
        }
    }

    score_ = 0;
    score_dirty = false;
}

size_t Graph::size() {
    return node_idx_to_name.size();
}

const std::vector<std::string>& Graph::nodes() {
    return node_idx_to_name;
}

bool Graph::add_edge(size_t node1, size_t node2) {
    assert(node1 < size() && node2 < size());
    if(connections[node1][node2].latency && !bought[node1][node2]) {
        bought[node1][node2] = true;
        bought[node2][node1] = true;
        score_dirty = true;
        return true;
    }
    return false;
}


bool Graph::add_edge(std::string node1, std::string node2) {
    auto it1 = node_name_to_idx.find(node1);
    auto it2 = node_name_to_idx.find(node2);
    assert(it1 != node_name_to_idx.end() && it2 != node_name_to_idx.end());
    return add_edge(it1->second, it2->second);
}


bool Graph::remove_edge(size_t node1, size_t node2) {
    assert(node1 < size() && node2 < size());
    if(connections[node1][node2].latency && bought[node1][node2]) {
        bought[node1][node2] = false;
        bought[node2][node1] = false;
        score_dirty = true;
        return true;
    }
    return false;
}

bool Graph::remove_edge(std::string node1, std::string node2) {
    auto it1 = node_name_to_idx.find(node1);
    auto it2 = node_name_to_idx.find(node2);
    assert(it1 != node_name_to_idx.end() && it2 != node_name_to_idx.end());
    return remove_edge(it1->second, it2->second);
}

int64_t Graph::score() {
    if(!score_dirty) {
        return score_;
    }

    score_ = 0;
    latencies = {size(), std::vector<uint32_t>(size(), max_latency)};
    for(size_t i = 0; i < size(); i++) {
        for(size_t j = 0; j < size(); j++) {
            if(bought[i][j]) {
                latencies[i][j] = connections[i][j].latency;
                score_ -= connections[i][j].cost;
            }
        }
    }
    // Double counted the costs.
    score_ /= 2;

    // Floyd-Warshall to determine lowest latencies.
    for(size_t j = 0; j < size(); j++) {
        for(size_t i = 0; i < size(); i++) {
            for(size_t k = 0; k < size(); k++) {
                latencies[i][k] = std::min(latencies[i][k], latencies[i][j] + latencies[j][k]);
            }
        }
    }

    for(size_t i = 0; i < size(); i++) {
        for(size_t j = i + 1; j < size(); j++) {
            if(latencies[i][j] < max_latency) {
                score_ += (max_latency - latencies[i][j]) * messages[i][j];
            }
        }
    }

    score_dirty = false;
    return score_;
}

void Graph::solution_from_tsv(std::string tsv_path) {
    bought = {size(), std::vector<bool>(size())};
    score_dirty = true;

    std::ifstream tsv(tsv_path);
    std::string node1, node2;

    while(tsv >> node1 >> node2) {
        size_t node1_idx = node_name_to_idx[node1];
        size_t node2_idx = node_name_to_idx[node2];
        
        bought[node1_idx][node2_idx] = true;
        bought[node2_idx][node1_idx] = true;
    }
}

bool Graph::has_connection(size_t i, size_t j) {
    return connections[i][j].latency > 0;
}

uint32_t Graph::connection_cost(size_t i, size_t j) {
    return connections[i][j].cost;
}

uint32_t Graph::connection_latency(size_t i, size_t j) {
    return connections[i][j].latency;
}

bool Graph::connection_bought(size_t i, size_t j) {
    return has_connection(i, j) && bought[i][j];
}

void Graph::clear_solution() {
    bought = {size(), std::vector<bool>(size())};
    score_ = 0;
    score_dirty = false;
}

void Graph::set_connection_cost(size_t i, size_t j, uint32_t cost) {
    connections[i][j].cost = cost;
    connections[j][i].cost = cost;
    score_dirty = true;
}

void Graph::write_input(std::string tsv_path) {
    std::ofstream tsv(tsv_path);
    for(size_t i = 0; i < size(); i++) {
        for(size_t j = i + 1; j < size(); j++) {
            if(has_connection(i, j)) {
                tsv << node_idx_to_name[i] << "\t"
                    << node_idx_to_name[j] << "\t"
                    << connections[i][j].latency << "\t"
                    << connections[i][j].cost << "\t"
                    << messages[i][j] << std::endl;
            }
            else if (messages[i][j] > 0) {
                tsv << node_idx_to_name[i] << "\t"
                    << node_idx_to_name[j] << "\t"
                    << 0 << "\t"
                    << 0 << "\t"
                    << messages[i][j] << std::endl;
            }
        }
    }
}

void Graph::write_output(std::string tsv_path) {
    std::ofstream tsv(tsv_path);
    for(size_t i = 0; i < size(); i++) {
        for(size_t j = i + 1; j < size(); j++) {
            if(!connection_bought(i, j)) continue;

            tsv << node_idx_to_name[i] << "\t" << node_idx_to_name[j] << std::endl;
        }
    }
}
