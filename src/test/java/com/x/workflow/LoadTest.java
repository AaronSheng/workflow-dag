package com.x.workflow;

import com.x.workflow.task.impl.PrintTask;
import com.x.workflow.dag.DAG;
import com.x.workflow.engine.Engine;
import com.x.workflow.engine.Result;
import com.x.workflow.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class LoadTest {

    @Test
    public void testLoad() {
        String dag = "{\n" +
                "    \"nodes\":[\n" +
                "        {\n" +
                "            \"id\":\"1\",\n" +
                "            \"task_name\":\"PrintTask\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\":\"2\",\n" +
                "            \"task_name\":\"PrintTask\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\":\"3\",\n" +
                "            \"task_name\":\"PrintTask\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\":\"4\",\n" +
                "            \"task_name\":\"PrintTask\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"edges\":[\n" +
                "        {\n" +
                "            \"from\":\"1\",\n" +
                "            \"to\":\"2\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"from\":\"1\",\n" +
                "            \"to\":\"3\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"from\":\"2\",\n" +
                "            \"to\":\"4\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"from\":\"3\",\n" +
                "            \"to\":\"4\"\n" +
                "        }\n" +
                "    ],\n" +
                "    \"parameters\":{\n" +
                "\n" +
                "    }\n" +
                "}";

        System.out.println(dag);

        Engine engine = new Engine();
        PrintTask printTask = new PrintTask();
        engine.register(printTask);

        DAG<Task> graph = engine.load(dag);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("FlowID", graph.getId());

        Result result = engine.execute(graph, parameters);
        System.out.printf("Exec Flow:%s Succeed:%s Output:%s\n", graph.getId(), result.isSucceed(), result.getMessage());
    }
}
