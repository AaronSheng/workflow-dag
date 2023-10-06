package com.x.workflow.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class DAGVO {
    @JsonProperty("nodes")
    private List<NodeVO> nodes;

    @JsonProperty("edges")
    private List<EdgeVO> edges;

    @JsonProperty("parameters")
    private Map<String, String> parameters;

    public List<NodeVO> getNodes() {
        return nodes;
    }

    public DAGVO setNodes(List<NodeVO> nodes) {
        this.nodes = nodes;
        return this;
    }

    public List<EdgeVO> getEdges() {
        return edges;
    }

    public DAGVO setEdges(List<EdgeVO> edges) {
        this.edges = edges;
        return this;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public DAGVO setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }
}
