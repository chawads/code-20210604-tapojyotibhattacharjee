package com.btapo.interview.screening.algo;// Java implementation of Dijkstra's Algorithm

import java.util.*;

public class DPQ {
    List<List<Node>> adj;
    private final int[] dist;
    private final Set<Integer> settled;
    private final PriorityQueue<Node> pq;
    private final int V; // Number of vertices
    private static Map<Integer, String> nodeNameMap;
    private static Map<String, Integer> nodeNameMapReverse;

    public DPQ(int V) {
        this.V = V;
        dist = new int[V];
        settled = new HashSet<Integer>();
        pq = new PriorityQueue<Node>(V, new Node());
    }

    public static void main(String[] arg) {
        int V = 5;

        List<List<Node>> adj = new ArrayList<List<Node>>();

        for (int i = 0; i < V; i++) {
            List<Node> item = new ArrayList<Node>();
            adj.add(item);
        }
        Scanner s = new Scanner(System.in);
        String line = s.nextLine().trim();
        int noOfDevelopers = Integer.parseInt(line);
        nodeNameMap = getNodeMap(noOfDevelopers, s);
        nodeNameMapReverse = getNodeMapReverse();
        int followerRelation = Integer.parseInt(s.nextLine().trim());
        for (int i = 0; i < followerRelation; i++) {
            line = s.nextLine().trim();
            String[] split = line.split(" ");
            int src = nodeNameMapReverse.get(split[0]);
            int dst = nodeNameMapReverse.get(split[1]);
            int cost = Integer.parseInt(split[2]);
            adj.get(src).add(new Node(dst, cost));
        }
        Integer nodeA = nodeNameMapReverse.get(s.nextLine().trim());
        Integer nodeB = nodeNameMapReverse.get(s.nextLine().trim());

        int source = nodeA;
        DPQ dpq = new DPQ(V);
        dpq.dijkstra(adj, source);

        System.out.println("The shorted path from node :");
        for (int i = 0; i < dpq.dist.length; i++)
            System.out.println(nodeNameMap.get(source) + " to " + i + " is "
                    + nodeNameMap.get(dpq.dist[i]));
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

    public void dijkstra(List<List<Node>> adj, int src) {
        this.adj = adj;

        for (int i = 0; i < V; i++)
            dist[i] = Integer.MAX_VALUE;

        pq.add(new Node(src, 0));

        dist[src] = 0;
        while (settled.size() != V) {
            if (pq.isEmpty())
                return;
            int u = pq.remove().node;

            settled.add(u);

            e_Neighbours(u);
        }
    }

    private void e_Neighbours(int u) {
        int edgeDistance = -1;
        int newDistance = -1;

        for (int i = 0; i < adj.get(u).size(); i++) {
            Node v = adj.get(u).get(i);

            if (!settled.contains(v.node)) {
                edgeDistance = v.cost;
                newDistance = dist[u] + edgeDistance;

                if (newDistance < dist[v.node])
                    dist[v.node] = newDistance;

                pq.add(new Node(v.node, dist[v.node]));
            }
        }
    }
}

class Node implements Comparator<Node> {
    public int node;
    public int cost;

    public Node() {
    }

    public Node(int node, int cost) {
        this.node = node;
        this.cost = cost;
    }

    @Override
    public int compare(Node node1, Node node2) {
        if (node1.cost < node2.cost)
            return -1;
        if (node1.cost > node2.cost)
            return 1;
        return 0;
    }
}