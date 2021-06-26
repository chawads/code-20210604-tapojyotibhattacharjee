package com.btapo.interview.screening.algo;

import java.util.*;

public class GraphDS {

    public static void main(String[] args) {
        List<Edge> edges = Arrays.asList(
                new Edge(0, 1, 2),
                new Edge(0, 2, 4),
                new Edge(1, 4, 4),
                new Edge(2, 0, 5),
                new Edge(2, 1, 4),
                new Edge(2, 3, 3),
                new Edge(3, 2, 3),
                new Edge(3, 4, 5),
                new Edge(4, 5, 1),
                new Edge(5, 4, 3));
        Graph graph = new Graph(edges);
//        graph.printGraph();
//        System.out.println("============= BFS ==============");
//        graph.breadthFirstSearch(2);
//        System.out.println("============= DFS ==============");
//        graph.depthFirstSearch(0);
        graph.calculateAllShortestPaths();
    }

    public static class Graph {

        List<List<Node>> adjacencyList = new ArrayList<>();
        Map<Integer, Map<Integer, Node>> adjacencyMap = new HashMap<>();

        public Graph(List<Edge> edges) {
            for (int i = 0; i < edges.size(); i++) {
                adjacencyList.add(i, new ArrayList<>());
            }

            for (Edge e : edges) {
                adjacencyList.get(e.src).add(new Node(e.dest, e.weight));
                adjacencyMap.computeIfAbsent(e.src, k -> new HashMap<>()).put(e.dest, new Node(e.dest, e.weight));
            }
        }

        public void printGraph() {
            int srcVertex = 0;
            int listSize = adjacencyList.size();

            System.out.println("The contents of the graph");
            while (srcVertex < listSize) {
                for (Node edge : adjacencyList.get(srcVertex)) {
                    System.out.print("Vertex:" + srcVertex + " ==> " + edge.value +
                            " (" + edge.weight + ")\t");
                }

                System.out.println();
                srcVertex++;
            }
        }

        public DistanceAndPaths calculateAllShortestPaths() {
            Map<Integer, Map<Integer, Double>> distance = new HashMap<>();

            for (int i = 0; i < adjacencyMap.size(); i++) {
                for (int j = 0; j < adjacencyMap.size(); j++) {
                    Map<Integer, Double> map = distance.computeIfAbsent(i, k -> new HashMap<>());
                    Map<Integer, Node> nodes = adjacencyMap.get(i);
                    if (nodes != null && nodes.size() > 0 && nodes.containsKey(j)) {
                        map.put(j, nodes.get(j).weight);
                    } else {
                        map.put(j, Double.MAX_VALUE);
                    }
                }
            }

            for (int k = 0; k < adjacencyMap.size(); k++) {
                for (int i = 0; i < adjacencyMap.size(); i++) {
                    for (int j = 0; j < adjacencyMap.size(); j++) {
                        double ikWeight = distance.get(i).get(k);
                        double kjWeight = distance.get(k).get(j);
                        double ijWeight = distance.get(i).get(j);
                        if (ikWeight * kjWeight < ijWeight) {
                            distance.get(i).put(j, (ikWeight * kjWeight));
                        }
                    }
                }
            }
            return new DistanceAndPaths(distance, null);
        }

        public void breadthFirstSearch(int sourceNode) {
            Set<Integer> visitedNodes = new HashSet<>();
            Map<Integer, Integer> level = new HashMap<>();
            breadthFirstSearch(sourceNode, level, visitedNodes);
        }

        public void breadthFirstSearch(int sourceNode, Map<Integer, Integer> level, Set<Integer> visitedNodes) {
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(sourceNode);
            level.put(sourceNode, 0);
            visitedNodes.add(sourceNode);
            System.out.println(sourceNode + ":" + level.get(sourceNode));
            while (!queue.isEmpty()) {
                int p = queue.remove();
                for (Node node : adjacencyList.get(p)) {
                    if (!visitedNodes.contains(node.value)) {
                        level.put(node.value, level.getOrDefault(p, 0) + 1);
                        queue.add(node.value);
                        visitedNodes.add(node.value);
                        System.out.println(node.value + ":" + level.get(node.value));
                    }
                }
            }
        }

        public void depthFirstSearch(int sourceNode) {
            Set<Integer> visitedNodes = new HashSet<>();
            Map<Integer, Integer> level = new HashMap<>();
            level.put(sourceNode, 0);
            depthFirstSearch(sourceNode, level, visitedNodes);
            System.out.println(level);
        }

        public void depthFirstSearch(int sourceNode, Map<Integer, Integer> level, Set<Integer> visitedNodes) {
            visitedNodes.add(sourceNode);
            System.out.println(sourceNode + ":" + level.get(sourceNode));
            for (Node node : adjacencyList.get(sourceNode)) {
                if (!visitedNodes.contains(node.value)) {
                    level.put(node.value, level.get(sourceNode) + 1);
                    depthFirstSearch(node.value, level, visitedNodes);
                }
            }
        }
    }

    static class Node {
        int value;
        double weight;

        Node(int value, double weight) {
            this.value = value;
            this.weight = weight;
        }
    }

    static class Edge {
        int src, dest;
        double weight;

        Edge(int src, int dest, double weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }
    }

    static class DistanceAndPaths {
        Map<Integer, Map<Integer, Double>> distance = new HashMap<>();
        Map<Integer, Map<Integer, Set<Node>>> paths = new HashMap<>();

        public DistanceAndPaths(Map<Integer, Map<Integer, Double>> distance, Map<Integer, Map<Integer, Set<Node>>> paths) {
            this.distance = distance;
            this.paths = paths;
        }

        public Map<Integer, Map<Integer, Double>> getDistance() {
            return distance;
        }

        public void setDistance(Map<Integer, Map<Integer, Double>> distance) {
            this.distance = distance;
        }

        public Map<Integer, Map<Integer, Set<Node>>> getPaths() {
            return paths;
        }

        public void setPaths(Map<Integer, Map<Integer, Set<Node>>> paths) {
            this.paths = paths;
        }
    }
}
