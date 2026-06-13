function MetricSelector({ options, value, onChange }) {
    return (
        <div className="metric-selector">
            {options.map((option) => (
                <button
                    key={option.key}
                    type="button"
                    className={option.key === value ? "metric-selector__button metric-selector__button--active" : "metric-selector__button"}
                    onClick={() => onChange(option.key)}
                >
                    <span>{option.label}</span>
                </button>
            ))}
        </div>
    );
}

export default MetricSelector;