import { NavLink, Outlet, useNavigate } from "react-router";
import {
  Calculator,
  FlaskConical,
  Package,
  LogOut,
  ShieldCheck,
  Users,
  ChevronRight,
  Terminal,
} from "lucide-react";
import { useAuth } from "../context/AuthContext";
import { cn } from "../lib/utils";
import { Button } from "./ui/button";
import { Separator } from "./ui/separator";
import { toast } from "sonner";

const navItems = [
  { to: "/calculations", label: "Расчёты", icon: Calculator, perm: "calculations.view" },
  { to: "/formulas", label: "Формулы", icon: FlaskConical, perm: "formulas.view" },
  { to: "/materials", label: "Материалы", icon: Package, perm: "materials.view" },
  { to: "/roles", label: "Роли", icon: ShieldCheck, perm: "roles.view" },
  { to: "/users", label: "Пользователи", icon: Users, perm: "users.view" },
  { to: "/sql", label: "SQL Терминал", icon: Terminal, perm: "sql.execute" },
];

export function Layout() {
  const { user, logout, can } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    await logout();
    toast.success("Выход выполнен");
    navigate("/login", { replace: true });
  };

  return (
    <div className="flex h-screen bg-background">
      {/* Sidebar */}
      <aside className="flex w-56 flex-col border-r bg-card">
        <div className="flex h-14 items-center px-4">
          <span className="text-sm font-semibold tracking-tight">Система расчётов</span>
        </div>
        <Separator />
        <nav className="flex-1 space-y-1 p-2">
          {navItems.map(({ to, label, icon: Icon, perm }) => {
            if (!can(perm)) return null;
            return (
              <NavLink
                key={to}
                to={to}
                className={({ isActive }) =>
                  cn(
                    "flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors",
                    isActive
                      ? "bg-primary text-primary-foreground"
                      : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
                  )
                }
              >
                <Icon className="h-4 w-4 shrink-0" />
                {label}
                <ChevronRight className="ml-auto h-3 w-3 opacity-50" />
              </NavLink>
            );
          })}
        </nav>
        <Separator />
        <div className="p-3">
          <div className="mb-2 px-1 text-xs text-muted-foreground">
            <div className="font-medium text-foreground">
              {user?.name} {user?.surname}
            </div>
            <div>{user?.login}</div>
          </div>
          <Button variant="ghost" size="sm" className="w-full justify-start gap-2" onClick={handleLogout}>
            <LogOut className="h-4 w-4" />
            Выйти
          </Button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-auto">
        <Outlet />
      </main>
    </div>
  );
}
