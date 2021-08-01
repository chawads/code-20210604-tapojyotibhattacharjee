package com.btapo.interview.screening.algo;

import java.util.Stack;

public class CDCommandChecker {

    private static CDCommandChecker service;

    private CDCommandChecker() {}

    public static synchronized CDCommandChecker instance() {
        if (service == null) {
            service = new CDCommandChecker();
        }
        return service;
    }

//    Examples, current directory: "/a/b/c", cd: "../.././d", output: "/a/d"
//    /a/b/c -> "../../b1/d/../."
    public String getCurrentPath(String inputDir, String cdCommand) {
        Stack<String> paths = new Stack<>();
        for (String s : inputDir.split("/")) {
            if (!s.isEmpty()) {
                paths.push(s);
            }
        }
        for (String s : cdCommand.split("/")) {
            if ("..".equals(s)) {
                if (!paths.isEmpty()) {
                    paths.pop();
                }
            } else {
                paths.push(s);
            }
        }
        StringBuilder finalStr = new StringBuilder();
        while (!paths.isEmpty()) {
            finalStr.insert(0, "/" + paths.pop());
        }
        return (finalStr.length() == 0) ? "/" : finalStr.toString();
    }

    public static void main(String[] args) {
        CDCommandChecker service = CDCommandChecker.instance();
        System.out.println(service.getCurrentPath("/a/b/c", "../.././d"));
        System.out.println(service.getCurrentPath("/", ".."));
    }
}
