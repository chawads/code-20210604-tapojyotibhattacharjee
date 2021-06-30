package com.btapo.interview.screening.algo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class GFG {
    private static Map<Integer, String> nodeNameMap;
    private static Map<String, Integer> nodeNameMapReverse;

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        String line = s.nextLine().trim();
        int noOfDevelopers = Integer.parseInt(line);
        nodeNameMap = getNodeMap(noOfDevelopers, s);
        nodeNameMapReverse = getNodeMapReverse();
        int followerRelation = Integer.parseInt(s.nextLine().trim());
        Graph graph = new Graph(noOfDevelopers);
        for (int i = 0; i < followerRelation; i++) {
            line = s.nextLine().trim();
            graph.addEdge(nodeNameMapReverse.get(line.split(" ")[0]), nodeNameMapReverse.get(line.split(" ")[1]));
        }
        graph.computePaths();
        Integer nodeA = nodeNameMapReverse.get(s.nextLine().trim());
        Integer nodeB = nodeNameMapReverse.get(s.nextLine().trim());

        if (graph.isReachable(nodeA, nodeB))
            System.out.println("1");
        else
            System.out.println("0");
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

    static class Graph {
        int V;
        int[][] g;
        int[][] shortestDistance;

        public Graph(int V) {
            this.V = V;

            g = new int[V + 1][V + 1];
            for (int i = 0; i < V + 1; i++) {
                Arrays.fill(g[i], 0);
            }

            for (int i = 1; i <= V; i++)
                g[i][i] = 1;
        }

        void addEdge(int v, int w) {
            g[v][w] = 1;
            g[w][v] = 1;
        }

        boolean isReachable(int s, int d) {
            return g[s][d] == 1;
        }

        void computePaths() {

            for (int k = 1; k <= V; k++) {
                for (int i = 1; i <= V; i++) {
                    for (int j = 1; j <= V; j++)
                        g[i][j] = g[i][j] | ((g[i][k] != 0 && g[k][j] != 0) ? 1 : 0);
                }
            }
        }
    }
}