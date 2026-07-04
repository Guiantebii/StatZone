import { Link } from 'react-router-dom';

interface LogoProps {
  collapse?: boolean;
  to?: string;
}

export default function Logo({ collapse, to = '/' }: LogoProps) {
  return (
    <Link to={to} className="flex items-center gap-2.5 no-underline group">
      <div className="flex items-center justify-center w-9 h-9 bg-gradient-to-br from-accent to-accent-hover rounded-xl shadow-lg shadow-accent/20 group-hover:shadow-accent/30 transition-shadow">
        <span className="text-primary-dark font-extrabold text-lg leading-none tracking-tight">SZ</span>
      </div>
      {!collapse && (
        <div className="flex flex-col">
          <span className="text-white font-bold text-lg tracking-tight leading-none">StateZone</span>
          <span className="text-[10px] text-slate-600 font-medium tracking-wider uppercase leading-none mt-0.5">
            Sports Analytics
          </span>
        </div>
      )}
    </Link>
  );
}
