interface SkeletonProps {
  className?: string;
  count?: number;
}

function SkeletonBlock({ className = '' }: { className?: string }) {
  return <div className={`rounded-lg animate-shimmer ${className}`} />;
}

export function SkeletonCard({ className = '' }: SkeletonProps) {
  return (
    <div className={`glass rounded-xl p-4 ${className}`}>
      <SkeletonBlock className="h-3 w-20 mb-3" />
      <SkeletonBlock className="h-8 w-16 mb-2" />
      <SkeletonBlock className="h-3 w-24" />
    </div>
  );
}

export function SkeletonTable({ rows = 5 }: { rows?: number }) {
  return (
    <div className="glass rounded-xl overflow-hidden">
      <div className="flex items-center justify-between px-5 py-3.5 border-b border-white/[0.04]">
        <SkeletonBlock className="h-4 w-36" />
        <SkeletonBlock className="h-7 w-32 rounded-lg" />
      </div>
      <div className="divide-y divide-white/[0.03]">
        {Array.from({ length: rows }).map((_, i) => (
          <div key={i} className="flex items-center gap-4 px-5 py-4">
            <SkeletonBlock className="h-8 w-8 rounded-lg" />
            <div className="flex-1 space-y-1.5">
              <SkeletonBlock className="h-4 w-48" />
              <SkeletonBlock className="h-3 w-32" />
            </div>
            <SkeletonBlock className="h-5 w-20 rounded-full" />
            <SkeletonBlock className="h-5 w-16" />
            <SkeletonBlock className="h-8 w-20 rounded-lg" />
          </div>
        ))}
      </div>
    </div>
  );
}

export default SkeletonBlock;
