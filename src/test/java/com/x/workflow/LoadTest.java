package com.x.workflow;

import com.x.workflow.dag.DAG;
import com.x.workflow.engine.Engine;
import com.x.workflow.engine.Result;
import com.x.workflow.task.Task;

import java.util.HashMap;
import java.util.Map;

public class LoadTest {
    public static void main(String[] args) {
        String dag = "{\n" +
                "    \"nodes\":[\n" +
                "        {\n" +
                "            \"id\":\"1\",\n" +
                "            \"task_name\":\"PrintTask\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\":\"2\",\n" +
                "            \"task_name\":\"PrintTask\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"edges\":[\n" +
                "        {\n" +
                "            \"from\":\"1\",\n" +
                "            \"to\":\"2\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"parameters\":{\n" +
                "\n" +
                "    }\n" +
                "}";

        Engine<PrintTask> engine = new Engine<>();
        engine.register(PrintTask.getTaskName(), PrintTask.class);

        DAG<PrintTask> graph = engine.load(dag);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("FlowID", graph.getId());

        Result result = engine.execute(graph, parameters);
        System.out.printf("Exec Flow:%s Succeed:%s Output:%s\n", graph.getId(), result.isSucceed(), result.getMessage());
    }
}
