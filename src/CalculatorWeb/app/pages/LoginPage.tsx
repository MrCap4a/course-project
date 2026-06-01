import { useState } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "../context/AuthContext";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Card, CardContent, CardHeader, CardTitle } from "../components/ui/card";
import { toast } from "sonner";

export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [loginVal, setLoginVal] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState<{ login?: string; password?: string }>({});

  const clearError = (field: keyof typeof errors) =>
    setErrors((prev) => { const e = { ...prev }; delete e[field]; return e; });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const errs: typeof errors = {};
    if (!loginVal.trim())    errs.login    = "Введите логин";
    if (!password.trim())    errs.password = "Введите пароль";
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setLoading(true);
    try {
      await login(loginVal, password);
      navigate("/", { replace: true });
    } catch {
      toast.error("Неверный логин или пароль");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/30">
      <Card className="w-full max-w-sm">
        <CardHeader>
          <CardTitle className="text-xl">Вход в систему</CardTitle>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-1">
              <Label htmlFor="login">Логин <span className="text-destructive">*</span></Label>
              <Input
                id="login"
                value={loginVal}
                onChange={(e) => { setLoginVal(e.target.value); clearError("login"); }}
                className={errors.login ? "border-destructive" : ""}
                autoFocus
                autoComplete="username"
              />
              {errors.login && <p className="text-xs text-destructive">{errors.login}</p>}
            </div>
            <div className="space-y-1">
              <Label htmlFor="password">Пароль <span className="text-destructive">*</span></Label>
              <Input
                id="password"
                type="password"
                value={password}
                onChange={(e) => { setPassword(e.target.value); clearError("password"); }}
                className={errors.password ? "border-destructive" : ""}
                autoComplete="current-password"
              />
              {errors.password && <p className="text-xs text-destructive">{errors.password}</p>}
            </div>
            <Button type="submit" className="w-full" disabled={loading}>
              {loading ? "Вход..." : "Войти"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
