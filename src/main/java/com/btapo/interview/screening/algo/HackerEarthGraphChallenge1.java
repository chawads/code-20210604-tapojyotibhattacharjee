package com.btapo.interview.screening.algo;

import java.util.*;

public class HackerEarthGraphChallenge1 {
    private static Map<Integer, String> nodeNameMap;
    private static Map<String, Integer> nodeNameMapReverse;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        Scanner s = new Scanner(System.in);
        String line = s.nextLine().trim();
        nodeNameMap = getNodeMap(line);
        nodeNameMapReverse = getNodeMapReverse();

        List<GraphDS.Edge> edges = new ArrayList<>();
        for (int i = 0; i < nodeNameMap.size() - 1; i++) {
            line = s.nextLine().trim();
            GraphDS.Edge edge1 = getEdge(line);
            GraphDS.Edge edge2 = new GraphDS.Edge(edge1.dest, edge1.src, (1/edge1.weight));
            edges.add(edge1);
            edges.add(edge2);
        }
        GraphDS.Graph graph = new GraphDS.Graph(edges);
        graph.printGraph();
        StringBuilder sb = new StringBuilder();
        GraphDS.DistanceAndPaths distanceAndPaths = graph.calculateAllShortestPaths();
        for (int i = 0; i < nodeNameMap.size() - 1; i++) {
            if (i == 0) {
                sb.append("1").append(nodeNameMap.get(i));
            }
            sb.append(" = ").append(distanceAndPaths.getDistance().get(0).get(i+1).intValue()).append(nodeNameMap.get(i+1));
        }

        System.out.println(sb);
        System.out.println(System.currentTimeMillis()-start);
    }

    private static int getWeight(GraphDS.DistanceAndPaths distanceAndPaths, int i, int i1) {
        Double res = 1d;
        for (GraphDS.Node d : distanceAndPaths.paths.get(i).get(i1)) {
            res *= d.weight;
        }
        return res.intValue();
    }

    private static Map<String, Integer> getNodeMapReverse() {
        Map<String, Integer> map = new HashMap<>();
        for (Map.Entry<Integer, String> entry : nodeNameMap.entrySet()) {
            map.put(entry.getValue(), entry.getKey());
        }
        return map;
    }

    private static GraphDS.Edge getEdge(String line) {
        String[] split = line.split(" ");
        return new GraphDS.Edge(nodeNameMapReverse.get(split[0].trim()),
                nodeNameMapReverse.get(split[3].trim()), Integer.parseInt(split[2].trim()));
    }

    private static Map<Integer, String> getNodeMap(String line) {
        Map<Integer, String> map = new HashMap<>();
        int i = 0;
        for (String s : line.split(",")) {
            map.put(i++, s.trim());
        }
        return map;
    }
}
