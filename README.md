# workflow-dag是什么？
workflow-dag 是一个基于Java实现的DAG有向无环图。DAG有向无环图是一个特殊类型的图，它由一组节点和有向边组成。在DAG中，边具有特定的方向，表示节点之间的流动或关系。


# workflow-dag能做什么？
workflow-dag 用于解决任务调度中任务之间的依赖关系，通过DAG描述使得任务按照预期的顺序正确执行。


# workflow-dag怎么设计？
<img width="796" alt="image" src="https://github.com/AaronSheng/workflow-dag/blob/master/src/main/resources/domain.png">
- DAG(流程): 有向无环图，负责描述有向无环图依赖关系；
- Node(节点): 流程节点，负责描述单个节点；
- Task(任务): 执行任务，负责任务实现；
- DAGExecutor(执行器): DAG执行器，负责流程引擎执行。

# workflow-data怎么使用？
```
public class Main {
    public static void main(String[] args) {
        DAG<PrintTask> graph = new DefaultDAG<>();
        Node<PrintTask> nodeA = new DefaultNode<>(new PrintTask("A"));
        Node<PrintTask> nodeB = new DefaultNode<>(new PrintTask("B"));
        Node<PrintTask> nodeC = new DefaultNode<>(new PrintTask("C"));
        Node<PrintTask> nodeD = new DefaultNode<>(new PrintTask("D"));
        Node<PrintTask> nodeE = new DefaultNode<>(new PrintTask("E"));
        Node<PrintTask> nodeF = new DefaultNode<>(new PrintTask("F"));

        graph.addEdge(nodeA, nodeB);
        graph.addEdge(nodeB, nodeC);
        graph.addEdge(nodeC, nodeE);
        graph.addEdge(nodeA, nodeD);
        graph.addEdge(nodeD, nodeE);
        graph.addEdge(nodeE, nodeF);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("FlowID", graph.getId());

        DAGExecutor<PrintTask> executor = new DAGExecutor<>(graph, parameters);
        ExecOutput output = executor.execute();
        System.out.printf("Exec Flow:%s Succeed:%s\n",  output.getId(), output.isSucceed());
    }
}
```
