import { useEffect, useState } from "react";
import { Line } from "react-chartjs-2";

import {
    Chart as ChartJS,
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
    } from "chart.js";

import { getAvailableMetrics, getMetricData } from "../services/metricsService";
import MetricCard from "./MetricCard";
import MetricSelector from "./MetricSelector";

ChartJS.register(
    CategoryScale,
    LinearScale,
    PointElement,
    LineElement,
    Title,
    Tooltip,
    Legend
    );

const chartColors = {
    commits: "#1d4ed8",
    bugs: "#db2777",
    tasks: "#059669",
    storyPoints: "#d97706"
};

function Dashboard() {
    const [availableMetrics, setAvailableMetrics] = useState([]);
    const [selectedMetric, setSelectedMetric] = useState("");
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const loadMetrics = async () => {
        try {
                const metrics = await getAvailableMetrics();
                setAvailableMetrics(metrics);
                setSelectedMetric(metrics[0]?.key ?? "commits");
        } catch (error) {
                setError(error.message);
                setLoading(false);
        }
    };

        loadMetrics();
    }, []);

    useEffect(() => {
        if (!selectedMetric) {
            return;
        }

        const loadData = async () => {
            setLoading(true);
            setError("");

            try {
                const result = await getMetricData(selectedMetric);
                setData(result);
            } catch (error) {
                setError(error.message);
            } finally {
                setLoading(false);
            }
        };

        loadData();
    }, [selectedMetric]);

    const total = data.reduce(
        (sum, item) => sum + item.value,
        0
    );

    const promedio =
        data.length > 0
        ? (total / data.length).toFixed(1)
        : 0;

    const maximo =
        data.length > 0
        ? Math.max(...data.map(x => x.value))
        : 0;

    const metricLabel =
        availableMetrics.find(metric => metric.key === selectedMetric)?.label ??
        "Métrica seleccionada";

    const chartColor = chartColors[selectedMetric] ?? "#1d4ed8";

    const chartData = {
        labels: data.map(item => item.label),
        datasets: [
        {
            label: metricLabel,
            data: data.map(item => item.value),
            borderColor: chartColor,
            backgroundColor: `${chartColor}33`,
            fill: true,
            tension: 0.4
        }
        ]
    };

    const chartOptions = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
        legend: {
            position: "top"
        },
        tooltip: {
            mode: "index",
            intersect: false
        }
        },
        scales: {
        y: {
            beginAtZero: true,
            ticks: {
            precision: 0
            }
        }
        }
    };

    return (
        <main className="dashboard-shell">
            <section className="dashboard-hero">
                <p className="dashboard-hero__eyebrow">Productivity Dashboard</p>
                <h1>Gestión visual de productividad de desarrolladores</h1>
                <p className="dashboard-hero__lead">
                    Consulta métricas operativas desde el backend y revisa su evolución con una vista modular, clara y responsiva.
                </p>
            </section>

            <section className="dashboard-panel dashboard-panel--controls">
                <div>
                    <h2>Selecciona una métrica</h2>
                    <p>El backend expone un catálogo de métricas para cambiar la visualización sin modificar el componente principal.</p>
                </div>
                <MetricSelector
                    options={availableMetrics}
                    value={selectedMetric}
                    onChange={setSelectedMetric}
                />
            </section>

            {error ? (
                <section className="dashboard-panel dashboard-panel--error">
                    <strong>No fue posible cargar el dashboard.</strong>
                    <p>{error}</p>
                </section>
            ) : null}

            <section className="dashboard-metrics">
                <MetricCard title={`Total ${metricLabel}`} value={total} description="Suma acumulada en el período cargado" />
                <MetricCard title="Promedio diario" value={promedio} description="Promedio por fecha registrada" />
                <MetricCard title="Máximo diario" value={maximo} description="Pico de actividad en el rango" />
            </section>

            <section className="dashboard-panel dashboard-panel--chart">
                <div className="dashboard-panel__header">
                    <div>
                        <h2>Evolución de {metricLabel}</h2>
                        <p>Gráfica lineal consumida desde la API REST.</p>
                    </div>
                    <span className="dashboard-badge">{data.length} registros</span>
                </div>

                <div className="chart-box">
                    {loading ? (
                        <div className="dashboard-state">Cargando datos...</div>
                    ) : (
                        <Line data={chartData} options={chartOptions} />
                    )}
                </div>
            </section>
        </main>
    );
    }

export default Dashboard;