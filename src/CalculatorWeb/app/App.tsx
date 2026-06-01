import { createBrowserRouter, RouterProvider, Navigate, Outlet } from "react-router";
import { AuthProvider, useAuth } from "./context/AuthContext";
import { Toaster } from "./components/ui/sonner";
import { Layout } from "./components/Layout";
import { LoginPage } from "./pages/LoginPage";
import { CalculationsPage } from "./pages/CalculationsPage";
import { FormulasPage } from "./pages/FormulasPage";
import { MaterialsPage } from "./pages/MaterialsPage";
import { RolesPage } from "./pages/RolesPage";
import { UsersPage } from "./pages/UsersPage";

function RequireAuth() {
  const { user, loading } = useAuth();
  if (loading) return <div className="flex h-screen items-center justify-center text-muted-foreground">Загрузка...</div>;
  if (!user) return <Navigate to="/login" replace />;
  return <Outlet />;
}

function RedirectIfAuth() {
  const { user, loading } = useAuth();
  if (loading) return null;
  if (user) return <Navigate to="/" replace />;
  return <Outlet />;
}

const router = createBrowserRouter([
  {
    element: <RequireAuth />,
    children: [
      {
        element: <Layout />,
        children: [
          { index: true, element: <Navigate to="/calculations" replace /> },
          { path: "calculations", element: <CalculationsPage /> },
          { path: "formulas", element: <FormulasPage /> },
          { path: "materials", element: <MaterialsPage /> },
          { path: "roles", element: <RolesPage /> },
          { path: "users", element: <UsersPage /> },
        ],
      },
    ],
  },
  {
    element: <RedirectIfAuth />,
    children: [
      { path: "login", element: <LoginPage /> },
    ],
  },
]);

export default function App() {
  return (
    <AuthProvider>
      <RouterProvider router={router} />
      <Toaster />
    </AuthProvider>
  );
}
