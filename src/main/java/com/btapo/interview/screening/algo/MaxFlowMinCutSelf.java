package com.btapo.interview.screening.algo;

import java.util.LinkedList;
import java.util.Queue;

public class MaxFlowMinCutSelf {

    int[][] graph;

    public MaxFlowMinCutSelf(int[][] graph) {
        this.graph = graph;
    }

    public static void main(String[] args) {
        int[][] graph = {{0, 16, 13, 0, 0, 0},
                {0, 0, 10, 12, 0, 0},
                {0, 4, 0, 0, 14, 0},
                {0, 0, 9, 0, 0, 20},
                {0, 0, 0, 7, 0, 4},
                {0, 0, 0, 0, 0, 0}
        };
        MaxFlowMinCutSelf minCutSelf = new MaxFlowMinCutSelf(graph);
        System.out.println(minCutSelf.getMaxFlow(0, graph.length - 1));
        minCutSelf.printMinCut(0, graph.length - 1);
    }

    public void printMinCut(int source, int sink) {
        MaxFlow maxFlow = runFordFulkerson(source, sink);
        boolean[] visited = new boolean[maxFlow.residualGraph.length];
        dfs(maxFlow.residualGraph, source, visited);
        System.out.println("Min cut edges");
        for (int i = 0; i < graph.length; i++) {
            for (int j = 0; j < graph.length; j++) {
                if (graph[i][j] > 0 && visited[i] && !visited[j]) {
                    System.out.println(i + " - " + j);
                }
            }
        }
    }

    private void dfs(int[][] graph, int source, boolean[] visited) {
        for (int i = 0; i < graph.length-1; i++) {
            if (graph[source][i] > 0 && !visited[i]) {
                visited[i] = true;
                dfs(graph, i, visited);
            }
        }
    }

    public int getMaxFlow(int source, int sink) {
        MaxFlow maxFlow = runFordFulkerson(source, sink);
        return maxFlow.maxFlow;
    }

    private MaxFlow runFordFulkerson(int source, int sink) {
        MaxFlow maxFlow = new MaxFlow();
        maxFlow.residualGraph = new int[graph.length][graph.length];
        for (int i = 0; i < graph.length; i++) {
            System.arraycopy(graph[i], 0, maxFlow.residualGraph[i], 0, graph.length);
        }

        maxFlow.parent = new int[graph.length];

        while (bfs(maxFlow, source, sink)) {
            int minFlowForPath = Integer.MAX_VALUE;
            int node = sink;
            while (node != source) {
                int parent = maxFlow.parent[node];
                minFlowForPath = Math.min(minFlowForPath, maxFlow.residualGraph[parent][node]);
                node = parent;
            }

            node = sink;
            while (node != source) {
                int parent = maxFlow.parent[node];
                maxFlow.residualGraph[parent][node] -= minFlowForPath;
                maxFlow.residualGraph[node][parent] += minFlowForPath;
                node = parent;
            }

            maxFlow.maxFlow += minFlowForPath;
        }

        return maxFlow;
    }

    private boolean bfs(MaxFlow maxFlow, int source, int sink) {
        boolean[] visited = new boolean[maxFlow.residualGraph.length];
        maxFlow.parent[source] = -1;

        Queue<Integer> queue = new LinkedList<>();
        queue.offer(source);
        visited[source] = true;

        while (!queue.isEmpty()) {
            int node = queue.poll();
            for (int i = 0; i < graph.length; i++) {
                if (!visited[i] && maxFlow.residualGraph[node][i] > 0) {
                    maxFlow.parent[i] = node;
                    visited[i] = true;
                    queue.offer(i);
                }
            }
        }

        return visited[sink];
    }

    static class MaxFlow {
        int[] parent;
        int[][] residualGraph;
        int maxFlow = 0;
    }
}
