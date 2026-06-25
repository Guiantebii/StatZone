interface CardProps {
  children: React.ReactNode;
  className?: string;
  hover?: boolean;
  onClick?: () => void;
}

export default function Card({
  children,
  className = '',
  hover = false,
  onClick,
}: CardProps) {
  return (
    <div
      onClick={onClick}
      className={`
        glass
        border
        border-white/[0.06]
        rounded-xl
        shadow-[0_4px_20px_rgba(0,0,0,0.25)]
        ${hover ? 'transition-all duration-300 hover:border-accent/20 hover:shadow-[0_8px_30px_rgba(255,215,0,0.08)] hover:-translate-y-0.5' : ''}
        ${onClick ? 'cursor-pointer' : ''}
        ${className}
      `}
    >
      {children}
    </div>
  );
}
