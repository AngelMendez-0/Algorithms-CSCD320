# Algorithms

## Description
This repository contains my work from an Algorithms course where I implement and analyze common algorithms using JavaScript. Topics include time complexity (Big-O), sorting and searching algorithms, recursion, and problem-solving techniques

## File Descriptions

### BinaryTree
A self-balancing AVL Binary Search Tree implemented in Java, supporting insertion, deletion, and traversal with automatic rebalancing via left, right, left-right, and right-left rotations.

### DWGraph (Adaptive Directed Weighted Graph)
A directed weighted graph implemented in Java using the Facade pattern. `DWGraph` presents a single stable API while automatically switching its underlying storage between an adjacency list and an adjacency matrix based on graph density, so clients never manage the representation themselves. Supports node/edge insertion, deletion, weight lookup, density calculation, and JSON export.

### DWGraph Shortest-Path Search
An extension of the adaptive DWGraph that adds pathfinding through the Strategy pattern, selecting a search algorithm at runtime. Implements Bellman-Ford (single-pair, with negative-cycle detection), Dijkstra's algorithm, and Floyd-Warshall (all-pairs shortest paths). Also includes a DAG built as a Facade over DWGraph, enforcing acyclicity on every insertion via DFS reachability checks (throwing a CycleException) and providing topological sort.

### SequenceAlignment
A dynamic-programming solution to the sequence alignment problem in Java. Builds an m×n scoring table where each cell dp[i][j] is the best alignment score of the first i and j characters, computed as the max of its diagonal, left, and up neighbors (match/mismatch and gap cases), then traces back to reconstruct the alignment. Reduces an otherwise exponential brute-force search to O(m·n), with file-based input/output.
