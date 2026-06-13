const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/metrics";

async function requestJson(url) {
    const response = await fetch(url);

    if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "No se pudo obtener la información del dashboard.");
    }

    return response.json();
}

export const getAvailableMetrics = () => requestJson(API_URL);

export const getMetricData = (metric) => requestJson(`${API_URL}/${metric}`);