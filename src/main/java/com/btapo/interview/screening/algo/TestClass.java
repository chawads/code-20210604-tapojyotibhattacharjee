package com.btapo.interview.screening.algo;

import java.io.*;
import java.util.*;
 
public class TestClass {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter wr = new PrintWriter(System.out);
        int T = Integer.parseInt(br.readLine().trim());
        for(int t_i = 0; t_i < T; t_i++)
        {
            int N = Integer.parseInt(br.readLine().trim());
            String[] arr_C = br.readLine().split(" ");
            int[] C = new int[N];
            for(int i_C = 0; i_C < arr_C.length; i_C++)
            {
            	C[i_C] = Integer.parseInt(arr_C[i_C]);
            }
 
            long out_ = numberOfWays(N, C);
            System.out.println(out_);
            
         }
 
         wr.close();
         br.close();
    }
    static long numberOfWays(int N, int[] C){
       // Write your code here
        long result = 0;
        Graph graph = new Graph(N, C);
        result = graph.getNumberOfPossibleConnections(0, N-1);
        graph.printGraph();
        return result;
    
    }
 
    public static class Graph {
        int[][] adj;
        int[][] minCost;
        int N;
        
        Graph(int N, int[] C) {
           this.N = N;
           adj = new int[N][N];
           minCost = new int[N][N];
           for (int i = 0; i < N; i++) {
               for (int j = 0; j < N; j++) {
                   if (i != j) {
                       adj[i][j] = C[j];
                   }
               }
           }
        }   
 
        void printGraph() {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N; j++) {
                    // System.out.println(i + ":" + j + ":" + adj[i][j]);
                }
            }
 
        }
 
        public void minimiseCost() {
            for (int k = 0; k < N; k++) {
               for (int i = 0; i < N; i++) {
                    for (int j = 0; j < N; j++) {
                        if (i == j || i == k || j == k) {
                            continue;
                        }
                        int ikcost = adj[i][k];
                        int jkcost = adj[j][k];
                        int ijcost = adj[i][j];
                        if (ikcost + jkcost < ijcost) {
                            minCost[i][j] = ikcost + jkcost;
                        } else {
                            minCost[i][j] = adj[i][j];
                        }
                        // System.out.println(i + ":" + j + ":" + minCost[i][j]);
                    }
                }
            }
        }
 
        public int getNumberOfPossibleConnections(int src, int dest) {
            minimiseCost();
            // System.out.println("min cost : " + src + ":" + dest + ":" + minCost[src][dest]);
            List<Integer> pathCosts = new ArrayList<>();
            boolean[] visited = new boolean[N];
            dfs(src, dest, 0, pathCosts, visited);
            int possiblePaths = 0;
            for (Integer cost : pathCosts) {
                if (cost == minCost[src][dest]) {
                    possiblePaths++;
                }
            }
            return possiblePaths;
        }
 
        public void dfs(int src, int dest, int sum, List<Integer> pathCosts, boolean[] visited) {
            visited[src] = true;
 
            for (int node = 0; node <= dest; node++) {
                sum += adj[src][node];
                if (visited[node]) {
                    continue;
                }
                if (node == dest) {
                    pathCosts.add(sum);
                }
                dfs(node, dest, sum, pathCosts, visited);
            }
        }
    }
}