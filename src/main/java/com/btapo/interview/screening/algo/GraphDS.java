package com.btapo.interview.screening.algo;

import java.util.*;

public class GraphDS {

    private static Map<Integer, String> nodeNameMap;
    private static Map<String, Integer> nodeNameMapReverse;

    public static void main2(String[] args) {
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

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Scanner s = new Scanner(System.in);
        String line = s.nextLine().trim();
        int noOfDevelopers = Integer.parseInt(line);
        nodeNameMap = getNodeMap(noOfDevelopers, s);
        nodeNameMapReverse = getNodeMapReverse();
        int followerRelation = Integer.parseInt(s.nextLine().trim());
        List<GraphDS.Edge> edges = new ArrayList<>();
        for (int i = 0; i < followerRelation; i++) {
            line = s.nextLine().trim();
//            GraphDS.Edge edge = getEdge2(line);
            GraphDS.Edge edge = getEdge(line);
            edges.add(edge);
        }
        GraphDS.Graph graph = new GraphDS.Graph(edges);
        graph.printGraph();
        Integer nodeA = nodeNameMapReverse.get(s.nextLine().trim());
        Integer nodeB = nodeNameMapReverse.get(s.nextLine().trim());
//        System.out.println(graph.canReach(nodeA, nodeB) ? 1 : 0);
//        System.out.println(graph.getShortestPathCost(nodeA, nodeB));
//        System.out.println(graph.getAllPossiblePathsFor(nodeA, nodeB));
        StringBuilder sb = new StringBuilder();
        for (Integer item : graph.getAllPossiblePathsFor2(nodeA, nodeB)) {
            if (sb.length() != 0) {
                sb.append(" ");
            }
            sb.append(nodeNameMap.get(item));
        }
        System.out.println(sb);
    }

    private static GraphDS.Edge getEdge(String line) {
        String[] split = line.split(" ");
        return new GraphDS.Edge(nodeNameMapReverse.get(split[0].trim()), nodeNameMapReverse.get(split[1].trim()), 0);
    }

    private static GraphDS.Edge getEdge2(String line) {
        String[] split = line.split(" ");
        return new GraphDS.Edge(nodeNameMapReverse.get(split[0].trim()), nodeNameMapReverse.get(split[1].trim()), Integer.parseInt(split[2]));
    }

    private static Map<Integer, String> getNodeMap(int noOfDevelopers, Scanner s) {
        Map<Integer, String> map = new HashMap<>();
        for (int i = 0; i < noOfDevelopers; i++) {
            map.put(i, s.nextLine());
        }
        return map;
    }

    private static Map<String, Integer> getNodeMapReverse() {
        Map<String, Integer> map = new HashMap<>();
        for (Map.Entry<Integer, String> entry : nodeNameMap.entrySet()) {
            map.put(entry.getValue(), entry.getKey());
        }
        return map;
    }

    public static class Graph {

        List<List<Node>> adjacencyList = new ArrayList<>();
        Map<Integer, Map<Integer, Node>> adjacencyMap = new HashMap<>();
        Set<Integer> vertexes = new HashSet<>();

        public Graph(List<Edge> edges) {
            for (int i = 0; i < edges.size(); i++) {
                adjacencyList.add(i, new ArrayList<>());
            }

            for (Edge e : edges) {
                vertexes.add(e.src);
                vertexes.add(e.dest);
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
            Map<Integer, Map<Integer, Set<Node>>> paths = new HashMap<>();

            for (int i = 0; i < vertexes.size(); i++) {
                for (int j = 0; j < vertexes.size(); j++) {
                    Map<Integer, Double> map = distance.computeIfAbsent(i, k -> new HashMap<>());
                    Map<Integer, Node> nodes = adjacencyMap.get(i);
                    if (nodes != null && nodes.size() > 0 && nodes.containsKey(j)) {
                        map.put(j, nodes.get(j).weight);
                        paths.computeIfAbsent(i, a -> new HashMap<>())
                                .computeIfAbsent(j, b -> new LinkedHashSet<>()).add(nodes.get(j));
                    } else {
                        map.put(j, Double.MAX_VALUE);
                    }
                }
            }

            for (int k = 0; k < vertexes.size(); k++) {
                for (int i = 0; i < vertexes.size(); i++) {
                    for (int j = 0; j < vertexes.size(); j++) {
                        double ikWeight = distance.get(i).get(k);
                        double kjWeight = distance.get(k).get(j);
                        double ijWeight = distance.get(i).get(j);
                        if (ikWeight + kjWeight < ijWeight) {
                            distance.get(i).put(j, (ikWeight + kjWeight));
                            if (adjacencyMap.getOrDefault(i, new HashMap<>()).get(k) != null) {
                                paths.computeIfAbsent(i, a -> new HashMap<>())
                                        .computeIfAbsent(k, b -> new LinkedHashSet<>()).add(adjacencyMap.get(i).get(k));
                            }
                            if (adjacencyMap.getOrDefault(k, new HashMap<>()).get(k) != null) {
                                paths.computeIfAbsent(k, a -> new HashMap<>())
                                        .computeIfAbsent(j, b -> new LinkedHashSet<>()).add(adjacencyMap.get(k).get(j));
                            }
                        }
                    }
                }
            }
            return new DistanceAndPaths(distance, null);
        }

        public DistanceAndPaths calculateAllShortestPaths2() {
            Map<Integer, Map<Integer, Double>> distance = new HashMap<>();

            for (int i = 0; i < vertexes.size(); i++) {
                for (int j = 0; j < vertexes.size(); j++) {
                    Map<Integer, Double> map = distance.computeIfAbsent(i, k -> new HashMap<>());
                    Map<Integer, Node> nodes = adjacencyMap.get(i);
                    if (nodes != null && nodes.size() > 0 && nodes.containsKey(j)) {
                        map.put(j, nodes.get(j).weight);
                    } else {
                        map.put(j, Double.MAX_VALUE);
                    }
                }
            }

            for (int k = 0; k < vertexes.size(); k++) {
                for (int i = 0; i < vertexes.size(); i++) {
                    for (int j = 0; j < vertexes.size(); j++) {
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

        public boolean breadthFirstSearchCanReach(int sourceNode, int destNode) {
            Set<Integer> visitedNodes = new HashSet<>();
            Map<Integer, Integer> level = new HashMap<>();
            return breadthFirstSearchCanReach(sourceNode, level, visitedNodes, destNode);
        }

        public boolean breadthFirstSearchCanReach(int sourceNode, Map<Integer, Integer> level, Set<Integer> visitedNodes, int destNode) {
            Queue<Integer> queue = new ArrayDeque<>();
            queue.add(sourceNode);
            level.put(sourceNode, 0);
            visitedNodes.add(sourceNode);
//            System.out.println(sourceNode + ":" + level.get(sourceNode));
            while (!queue.isEmpty()) {
                int p = queue.remove();
                for (Node node : adjacencyList.get(p)) {
                    if (node.value == destNode) {
                        return true;
                    }
                    if (!visitedNodes.contains(node.value)) {
                        level.put(node.value, level.getOrDefault(p, 0) + 1);
                        queue.add(node.value);
                        visitedNodes.add(node.value);
//                        System.out.println(node.value + ":" + level.get(node.value));
                    }
                }
            }
            return false;
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

        public boolean depthFirstSearchCanReach(int sourceNode, int destNode) {
            Set<Integer> visitedNodes = new HashSet<>();
            Map<Integer, Integer> level = new HashMap<>();
            level.put(sourceNode, 0);
            return depthFirstSearchCanReach(sourceNode, level, visitedNodes, destNode);
        }

        public boolean depthFirstSearchCanReach(int sourceNode, Map<Integer, Integer> level, Set<Integer> visitedNodes, int destNode) {
            visitedNodes.add(sourceNode);
            for (Node node : adjacencyList.get(sourceNode)) {
                if (node.value == destNode) {
                    return true;
                }
                if (!visitedNodes.contains(node.value)) {
                    level.put(node.value, level.get(sourceNode) + 1);
                    depthFirstSearchCanReach(node.value, level, visitedNodes, destNode);
                }
            }
            return false;
        }

        public boolean canReach(Integer nodeA, Integer nodeB) {
            return depthFirstSearchCanReach(nodeA, nodeB);
        }

        public int getShortestPathCost(Integer nodeA, Integer nodeB) {
            DistanceAndPaths distance = calculateAllShortestPaths();
            Map<Integer, Double> res = distance.getDistance().get(nodeA);
            if (res != null) {
                return res.get(nodeB).intValue();
            }
            return -1;
        }

        boolean matchPathsUtil(int origin, int dest, List<Integer> nodesStack, Set<Integer> visited) {
            if (visited.contains(origin)) {
                return false;
            } else {
                visited.add(origin);
            }
            if (origin == dest) {
                return true;
            } else {
                for (Node node : adjacencyMap.get(origin).values()) {
                    nodesStack.add(node.value);
                    if (!matchPathsUtil(node.value, dest, nodesStack, visited)) {
                        nodesStack.remove(node.value);
                    } else {
                        return true;
                    }
                }
            }
            return false;
        }

        List<List<Integer>> getAllPossiblePathsFor(int nodeA, int nodeB) {
            List<List<Integer>> list = new ArrayList<>();
            Set<Integer> visited = new HashSet<>();
            visited.add(nodeA);
            for (Node child : adjacencyMap.get(nodeA).values()) {
                List<Integer> nodesStack = new ArrayList<>();
                nodesStack.add(child.value);
                if (matchPathsUtil(child.value, nodeB, nodesStack, visited)) {
                    list.add(nodesStack);
                }
            }
            return list;
        }

        List<Integer> getAllPossiblePathsFor2(int nodeA, int nodeB) {
            List<Integer> list = new ArrayList<>();
            if (canReach(nodeA, nodeB)) {
                list.add(nodeA);
            }

            for (Node child : adjacencyMap.get(nodeB).values()) {
                if (canReach(nodeA, child.value)) {
                    list.add(child.value);
                }
            }
            return list;
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
