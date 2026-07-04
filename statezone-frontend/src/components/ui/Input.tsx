interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  leftIcon?: React.ReactNode;
}

export default function Input({ className = '', leftIcon, ...props }: InputProps) {
  // If no aria-label or id is provided, generate one from placeholder
  const inputId =
    props.id || (props.placeholder ? `input-${props.placeholder.replace(/\s+/g, '-').toLowerCase()}` : undefined);
  return (
    <div className="relative">
      {leftIcon && <div className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-500">{leftIcon}</div>}
      <input
        id={inputId}
        {...props}
        className={`
          w-full
          bg-white/[0.04]
          border
          border-white/[0.08]
          rounded-lg
          px-3
          py-2.5
          text-sm
          text-slate-200
          placeholder-slate-600
          focus:outline-none
          focus:border-accent/40
          focus:ring-2
          focus:ring-accent/10
          focus:bg-white/[0.06]
          transition-all duration-200
          ${leftIcon ? 'pl-9' : ''}
          ${className}
        `}
      />
    </div>
  );
}
