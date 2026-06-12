import type { LucideIcon } from 'lucide-react';
import { Area, AreaChart, ResponsiveContainer } from 'recharts';
import { formatMoney } from '../utils/format';

interface MetricCardProps {
  title: string;
  value: number;
  icon: LucideIcon;
  money?: boolean;
  trend?: string;
  tone?: 'emerald' | 'cyan' | 'amber' | 'red';
  data?: Array<{ value: number }>;
}

export default function MetricCard({
  title,
  value,
  icon: Icon,
  money = true,
  trend,
  tone = 'emerald',
  data = [{ value: 10 }, { value: 18 }, { value: 14 }, { value: 24 }, { value: 21 }, { value: 31 }],
}: MetricCardProps) {
  return (
    <article className="metric-card">
      <div className="metric-card__top">
        <div>
          <p>{title}</p>
          <strong>{money ? formatMoney(value) : value.toLocaleString()}</strong>
        </div>
        <span className={`icon-chip ${tone}`}>
          <Icon size={17} />
        </span>
      </div>
      <div className="metric-card__bottom">
        <span>{trend ?? 'Updated from live records'}</span>
        <div className="metric-sparkline">
          <ResponsiveContainer width="100%" height={34}>
            <AreaChart data={data}>
              <Area type="monotone" dataKey="value" stroke="var(--accent)" fill="var(--accent-soft)" strokeWidth={2} />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>
    </article>
  );
}
