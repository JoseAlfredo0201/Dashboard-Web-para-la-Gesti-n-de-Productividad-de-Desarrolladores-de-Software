package com.exampleback.demo.repository;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import com.exampleback.demo.model.DeveloperMetric;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

@Repository
public class DeveloperMetricRepository {

    private static final String COLLECTION_NAME = "developer_metrics";

    private final String projectId;
    private final String credentialsLocation;

    public DeveloperMetricRepository(
            @Value("${firebase.project-id}") String projectId,
            @Value("${firebase.credentials.location:}") String credentialsLocation) {

        this.projectId = projectId;
        this.credentialsLocation = credentialsLocation;
    }

    public List<DeveloperMetric> findAllByOrderByMetricDateAsc() {
        try {
            Firestore firestore = getFirestore();

            return StreamSupport.stream(
                            firestore.collection(COLLECTION_NAME)
                                    .orderBy("metricDate")
                                    .get()
                                    .get()
                                    .getDocuments()
                                    .spliterator(),
                            false)
                    .map(this::toModel)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(DeveloperMetric::getMetricDate))
                    .toList();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "No se pudo leer la colección developer_metrics en Firestore",
                    exception);
        }
    }

    private synchronized Firestore getFirestore() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setProjectId(projectId)
                    .setCredentials(loadCredentials())
                    .build();

            FirebaseApp.initializeApp(options);
        }

        return FirestoreClient.getFirestore();
    }

    private GoogleCredentials loadCredentials() throws IOException {
        if (credentialsLocation == null || credentialsLocation.isBlank()) {
            return GoogleCredentials.getApplicationDefault();
        }

        try (InputStream inputStream = openCredentialStream()) {
            return GoogleCredentials.fromStream(inputStream);
        }
    }

    private InputStream openCredentialStream() throws IOException {
        if (credentialsLocation.startsWith("classpath:")) {
            String path = credentialsLocation.substring("classpath:".length());
            return new ClassPathResource(path).getInputStream();
        }

        if (credentialsLocation.startsWith("file:")) {
            return new FileInputStream(credentialsLocation.substring("file:".length()));
        }

        return new FileInputStream(credentialsLocation);
    }

    private DeveloperMetric toModel(QueryDocumentSnapshot document) {
        DeveloperMetric metric = new DeveloperMetric();
        metric.setId(document.getId());
        metric.setDeveloperName(document.getString("developerName"));
        metric.setMetricDate(document.getString("metricDate"));
        metric.setCommits(readInteger(document.getLong("commits")));
        metric.setBugsFixed(readInteger(document.getLong("bugsFixed")));
        metric.setTasksCompleted(readInteger(document.getLong("tasksCompleted")));
        metric.setStoryPoints(readInteger(document.getLong("storyPoints")));
        return metric;
    }

    private Integer readInteger(Long value) {
        return value == null ? 0 : value.intValue();
    }
}