import type { ButtonHTMLAttributes } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger' | 'ghost' | 'outline';
  size?: 'sm' | 'md' | 'lg';
}

export default function Button({ variant = 'primary', size = 'md', className = '', ...props }: ButtonProps) {
  const variants = {
    primary:
      'bg-gradient-to-r from-accent to-accent-hover text-primary-dark font-bold hover:shadow-[0_4px_20px_rgba(255,215,0,0.25)] hover:brightness-110',

    secondary: 'bg-white/[0.06] text-slate-200 hover:bg-white/[0.12] border border-white/[0.06]',

    danger: 'bg-danger-bg text-danger hover:bg-danger/20 border border-danger-border',

    ghost: 'text-slate-400 hover:text-slate-200 hover:bg-white/[0.05]',

    outline: 'border border-accent/30 text-accent hover:bg-accent/10 hover:border-accent/60',
  };

  const sizes = {
    sm: 'px-3 py-1.5 text-xs',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-2.5 text-base',
  };

  return (
    <button
      {...props}
      className={`
        inline-flex items-center justify-center gap-2
        rounded-lg
        font-semibold
        transition-all duration-200
        disabled:opacity-50
        disabled:cursor-not-allowed
        disabled:hover:shadow-none
        ${sizes[size]}
        ${variants[variant]}
        ${className}
      `}
    />
  );
}
