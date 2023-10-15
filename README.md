# workflow-dag是什么？
workflow-dag 是一个基于Java实现的DAG有向无环图。DAG有向无环图是一个特殊类型的图，它由一组节点和有向边组成。在DAG中，边具有特定的方向，表示节点之间的流动或关系。  
  
在任务开发场景中比如离线处理、大数据等，通常有较多的类型的任务，任务之间又有一定的执行依赖顺序。业务早期由于流程简单，可以通过代码编写等方式完成，随着业务的数量增加、流程的复杂度增加，会遇到以下问题：
- 研发成本高。业务接入和流程调整、新任务接入都需要大量的重复工作，研发成本非常高；
- 可维护性差。通过代码实现业务流程，改动和维护成本都非常高，频繁的发布部署，容易引起线上故障；
- 可观测性差。任务依赖顺序都是通过代码实现，实际的依赖顺序、执行情况无法有效地观测。  
任务编排就为了解决上述问题，任务编排是指通过对任务进行标准化，以及任务之间的关系进行建模，使得任务按照定义的方式顺序执行。DAG有向无环图则是任务编排模型中最常见的、有效的实现方式。


# workflow-dag能做什么？
workflow-dag 用于解决任务调度中任务之间的依赖关系，通过DAG描述使得任务按照预期的顺序正确执行。  
作为任务编排的DAG支持以下特征：
- 支持任务自定义；
- 支持灵活的任务依赖顺序；
- 支持配置化描述DAG；
- 支持任务间的并行执行;
- 支持度量统计等功能。

# workflow-dag怎么设计？
<img width="796" alt="image" src="https://github.com/AaronSheng/workflow-dag/blob/master/src/main/resources/domain.png">

- DAG(流程): 有向无环图，负责描述有向无环图依赖、合法性校验；
- Node(节点): 流程节点，负责描述单个节点；
- Task(任务): 执行任务，负责任务实现；
- Engine(引擎): DAG执行器，负责配置化解析、流程引擎执行。

# workflow-data怎么使用？
## 方法一：代码初加载运行
```
public class EngineTest {
    private static final Logger LOGGER = LogManager.getLogger(EngineTest.class);

    @Test
    public void testExecute() {
        DAG<Task> graph = new DefaultDAG<>();

        PrintTask printTask = new PrintTask();
        Node<Task> nodeA = new DefaultNode<>("A", printTask);
        Node<Task> nodeB = new DefaultNode<>("B", printTask);
        Node<Task> nodeC = new DefaultNode<>("C", printTask);
        Node<Task> nodeD = new DefaultNode<>("D", printTask);
        Node<Task> nodeE = new DefaultNode<>("E", printTask);
        Node<Task> nodeF = new DefaultNode<>("F", printTask);

        graph.addEdge(nodeA, nodeB);
        graph.addEdge(nodeB, nodeC);
        graph.addEdge(nodeC, nodeE);
        graph.addEdge(nodeA, nodeD);
        graph.addEdge(nodeD, nodeE);
        graph.addEdge(nodeE, nodeF);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("FlowID", graph.getId());

        Engine engine = new Engine();
        engine.register(printTask);

        Result result = engine.execute(graph, parameters);
        LOGGER.info("Exec Flow:{} Succeed:{} Output:{}", graph.getId(), result.isSucceed(), result.getOutput());
    }
}
```

## 方法二：配置化加载运行
```
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

        Map<String, String> parameters = new HashMap<>();
        parameters.put("FlowID", graph.getId());

        Result result = engine.execute(graph, parameters);
        System.out.printf("Exec Flow:%s Succeed:%s Output:%s\n", graph.getId(), result.isSucceed(), result.getMessage());
    }
}
```
