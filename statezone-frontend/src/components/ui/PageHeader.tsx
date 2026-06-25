

interface PageHeaderProps {
  title: string;
  description: string;
  actions?: React.ReactNode;
}

export default function PageHeader({
  title,
  description,
  actions,
}: PageHeaderProps) {
  return (
    <div className="flex items-start justify-between gap-4">
      <div className="space-y-1">
        <h1 className="text-2xl font-bold text-slate-100 tracking-tight">
          {title}
        </h1>
        <p className="text-sm text-slate-500">
          {description}
        </p>
      </div>

      {actions && (
        <div className="flex items-center gap-2 flex-shrink-0">
          {actions}
        </div>
      )}
    </div>
  );
}
