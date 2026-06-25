import Card from "./Card";

interface StatCardProps {
  label: string;
  value: React.ReactNode;
  sublabel?: string;
  trend?: { direction: 'up' | 'down' | 'neutral'; value: string };
}

export default function StatCard({
  label,
  value,
  sublabel,
  trend,
}: StatCardProps) {
  return (
    <Card className="p-4 min-h-[110px] flex flex-col justify-between relative overflow-hidden group">
      <div className="absolute top-0 right-0 w-24 h-24 bg-accent/[0.02] rounded-full -translate-y-1/2 translate-x-1/2 group-hover:bg-accent/[0.04] transition-colors duration-500" />
      <p className="text-[11px] uppercase tracking-widest text-slate-500 font-semibold relative">
        {label}
      </p>

      <div className="relative">
        <div className="text-3xl font-extrabold text-slate-100 font-mono tracking-tight">
          {value}
        </div>
        <div className="flex items-center gap-2 mt-1">
          {sublabel && (
            <span className="text-xs text-slate-500">{sublabel}</span>
          )}
          {trend && (
            <span className={`inline-flex items-center gap-0.5 text-xs font-semibold ${
              trend.direction === 'up' ? 'text-success' :
              trend.direction === 'down' ? 'text-danger' :
              'text-slate-400'
            }`}>
              {trend.direction === 'up' && '↑'}
              {trend.direction === 'down' && '↓'}
              {trend.direction === 'neutral' && '→'}
              {trend.value}
            </span>
          )}
        </div>
      </div>
    </Card>
  );
}
