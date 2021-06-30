package com.btapo.interview.screening.algo;

import io.swagger.models.auth.In;

import java.util.*;

//https://assessment.hackerearth.com/challenges/hiring/juspay-developer-hiring-challenge-june-2021/problems/b75c0dd9f3a4489487606aae40a99372/
public class HackerEarthChallenge2 {

    private static Map<Integer, String> nodeNameMap;
    private static Map<String, Integer> nodeNameMapReverse;

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
            GraphDS.Edge edge1 = getEdge(line);
            edges.add(edge1);
        }
        GraphDS.Graph graph = new GraphDS.Graph(edges);
        graph.printGraph();
        Integer nodeA = nodeNameMapReverse.get(s.nextLine().trim());
        Integer nodeB = nodeNameMapReverse.get(s.nextLine().trim());
//        System.out.println(graph.canReach(nodeA, nodeB) ? 1 : 0);
        System.out.println(graph.getShortestPathCost(nodeA, nodeB));
    }

    private static GraphDS.Edge getEdge(String line) {
        String[] split = line.split(" ");
        return new GraphDS.Edge(nodeNameMapReverse.get(split[0].trim()), nodeNameMapReverse.get(split[1].trim()),
                Integer.parseInt(split[2]));
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


}
