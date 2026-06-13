function MetricCard({ title, value, description }) {
    return (
        <article className="metric-card">
            <p className="metric-card__title">{title}</p>
            <strong className="metric-card__value">{value}</strong>
            {description ? <p className="metric-card__description">{description}</p> : null}
        </article>
    );
}

export default MetricCard;