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
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (onClick && (e.key === 'Enter' || e.key === ' ')) {
      e.preventDefault();
      onClick();
    }
  };

  return (
    <div
      onClick={onClick}
      onKeyDown={onClick ? handleKeyDown : undefined}
      role={onClick ? 'button' : undefined}
      tabIndex={onClick ? 0 : undefined}
      className={`
        glass
        border
        border-white/[0.06]
        rounded-xl
        shadow-[0_4px_20px_rgba(0,0,0,0.25)]
        ${hover ? 'transition-all duration-300 hover:border-accent/20 hover:shadow-[0_8px_30px_rgba(255,215,0,0.08)] hover:-translate-y-0.5' : ''}
        ${onClick ? 'cursor-pointer focus:outline-none focus:ring-2 focus:ring-accent/30' : ''}
        ${className}
      `}
    >
      {children}
    </div>
  );
}
