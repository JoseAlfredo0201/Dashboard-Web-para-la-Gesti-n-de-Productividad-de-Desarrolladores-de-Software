package com.exampleback.demo.model;

public class DeveloperMetric {

    private String id;

    private String developerName;

    private String metricDate;

    private Integer commits;

    private Integer bugsFixed;

    private Integer tasksCompleted;

    private Integer storyPoints;

    public DeveloperMetric() {
    }

    public DeveloperMetric(
            String developerName,
            String metricDate,
            Integer commits,
            Integer bugsFixed,
            Integer tasksCompleted,
            Integer storyPoints) {

        this.developerName = developerName;
        this.metricDate = metricDate;
        this.commits = commits;
        this.bugsFixed = bugsFixed;
        this.tasksCompleted = tasksCompleted;
        this.storyPoints = storyPoints;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getMetricDate() {
        return metricDate;
    }

    public void setMetricDate(String metricDate) {
        this.metricDate = metricDate;
    }

    public Integer getCommits() {
        return commits;
    }

    public void setCommits(Integer commits) {
        this.commits = commits;
    }

    public Integer getBugsFixed() {
        return bugsFixed;
    }

    public void setBugsFixed(Integer bugsFixed) {
        this.bugsFixed = bugsFixed;
    }

    public Integer getTasksCompleted() {
        return tasksCompleted;
    }

    public void setTasksCompleted(Integer tasksCompleted) {
        this.tasksCompleted = tasksCompleted;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public void setStoryPoints(Integer storyPoints) {
        this.storyPoints = storyPoints;
    }
}