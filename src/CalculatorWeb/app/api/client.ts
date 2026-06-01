import type {
  TokenResponse,
  CurrentUserDto,
  UserDto,
  UserRoleDto,
  PermissionDto,
  MaterialDto,
  MaterialGroupDto,
  FormulaDto,
  FormulaGroupDto,
  CalculationDto,
  LoginRequest,
  RegisterRequest,
  UserRoleRequest,
  MaterialRequest,
  MaterialGroupRequest,
  FormulaRequest,
  FormulaGroupRequest,
  CalculationRequest,
  SpringPage,
} from "./types";

const TOKEN_KEY = "auth_token";
const REFRESH_TOKEN_KEY = "auth_refresh_token";

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY);
}

export function saveToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY);
}

export function saveRefreshToken(token: string): void {
  localStorage.setItem(REFRESH_TOKEN_KEY, token);
}

export function clearRefreshToken(): void {
  localStorage.removeItem(REFRESH_TOKEN_KEY);
}

async function tryRefresh(): Promise<string | null> {
  const refreshToken = getRefreshToken();
  if (!refreshToken) return null;
  try {
    const res = await fetch("/api/v1/auth/refresh", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });
    if (!res.ok) return null;
    const data: TokenResponse = await res.json();
    saveToken(data.token);
    saveRefreshToken(data.refreshToken);
    return data.token;
  } catch {
    return null;
  }
}

async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    "Content-Type": "application/json",
    ...((options.headers as Record<string, string>) ?? {}),
  };
  if (token) headers["Authorization"] = `Bearer ${token}`;

  const res = await fetch("/api/v1" + path, { ...options, headers });

  if (res.status === 401) {
    if (!path.startsWith("/auth/")) {
      const newToken = await tryRefresh();
      if (newToken) {
        headers["Authorization"] = `Bearer ${newToken}`;
        const retry = await fetch("/api/v1" + path, { ...options, headers });
        if (retry.status === 204) return undefined as T;
        if (retry.ok) return retry.json() as Promise<T>;
      }
    }
    clearToken();
    clearRefreshToken();
    window.location.href = "/login";
    throw new Error("Unauthorized");
  }

  if (!res.ok) {
    const text = await res.text().catch(() => `HTTP ${res.status}`);
    throw new Error(text || `HTTP ${res.status}`);
  }

  if (res.status === 204) return undefined as T;
  return res.json() as Promise<T>;
}

// Auth
export const authApi = {
  login: (data: LoginRequest) =>
    request<TokenResponse>("/auth/login", { method: "POST", body: JSON.stringify(data) }),
  refresh: (refreshToken: string) =>
    request<TokenResponse>("/auth/refresh", { method: "POST", body: JSON.stringify({ refreshToken }) }),
  logout: () =>
    request<void>("/auth/logout", { method: "POST" }),
};

// Users
export const usersApi = {
  me: () => request<CurrentUserDto>("/users/me"),
  getAll: () => request<UserDto[]>("/users"),
  create: (data: RegisterRequest) =>
    request<UserDto>("/users", { method: "POST", body: JSON.stringify(data) }),
  update: (id: number, data: RegisterRequest) =>
    request<UserDto>(`/users/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  delete: (id: number) =>
    request<void>(`/users/${id}`, { method: "DELETE" }),
};

// Roles
export const rolesApi = {
  getAll: () => request<UserRoleDto[]>("/roles"),
  create: (data: UserRoleRequest) =>
    request<UserRoleDto>("/roles", { method: "POST", body: JSON.stringify(data) }),
  update: (id: number, data: UserRoleRequest) =>
    request<UserRoleDto>(`/roles/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  delete: (id: number) =>
    request<void>(`/roles/${id}`, { method: "DELETE" }),
};

// Permissions
export const permissionsApi = {
  getAll: () => request<PermissionDto[]>("/permissions"),
};

// Materials
export const materialsApi = {
  getAll: (groupId?: number, search?: string) => {
    const params = new URLSearchParams({ size: "1000" });
    if (groupId !== undefined) params.set("groupId", String(groupId));
    if (search) params.set("search", search);
    return request<SpringPage<MaterialDto>>(`/materials?${params.toString()}`)
      .then((page) => page.content);
  },
  create: (data: MaterialRequest) =>
    request<MaterialDto>("/materials", { method: "POST", body: JSON.stringify(data) }),
  update: (id: number, data: MaterialRequest) =>
    request<MaterialDto>(`/materials/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  delete: (id: number) =>
    request<void>(`/materials/${id}`, { method: "DELETE" }),
};

// Material groups
export const materialGroupsApi = {
  getAll: () => request<MaterialGroupDto[]>("/material-groups"),
  create: (data: MaterialGroupRequest) =>
    request<MaterialGroupDto>("/material-groups", { method: "POST", body: JSON.stringify(data) }),
  update: (id: number, data: MaterialGroupRequest) =>
    request<MaterialGroupDto>(`/material-groups/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  delete: (id: number, strategy: "CASCADE" | "DEFAULT" | "MOVE", targetGroupId?: number) => {
    const params = new URLSearchParams({ strategy });
    if (targetGroupId !== undefined) params.set("targetGroupId", String(targetGroupId));
    return request<void>(`/material-groups/${id}?${params.toString()}`, { method: "DELETE" });
  },
};

// Formulas
export const formulasApi = {
  getAll: (groupId?: number) => {
    const params = new URLSearchParams({ size: "1000" });
    if (groupId !== undefined) params.set("groupId", String(groupId));
    return request<SpringPage<FormulaDto>>(`/formulas?${params.toString()}`)
      .then((page) => page.content);
  },
  create: (data: FormulaRequest) =>
    request<FormulaDto>("/formulas", { method: "POST", body: JSON.stringify(data) }),
  update: (id: number, data: FormulaRequest) =>
    request<FormulaDto>(`/formulas/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  delete: (id: number) =>
    request<void>(`/formulas/${id}`, { method: "DELETE" }),
};

// Formula groups
export const formulaGroupsApi = {
  getAll: () => request<FormulaGroupDto[]>("/formula-groups"),
  create: (data: FormulaGroupRequest) =>
    request<FormulaGroupDto>("/formula-groups", { method: "POST", body: JSON.stringify(data) }),
  update: (id: number, data: FormulaGroupRequest) =>
    request<FormulaGroupDto>(`/formula-groups/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  delete: (id: number) =>
    request<void>(`/formula-groups/${id}`, { method: "DELETE" }),
};

// Calculations
export const calculationsApi = {
  getAll: () => request<CalculationDto[]>("/calculations"),
  getById: (id: number) => request<CalculationDto>(`/calculations/${id}`),
  create: (data: CalculationRequest) =>
    request<CalculationDto>("/calculations", { method: "POST", body: JSON.stringify(data) }),
  update: (id: number, data: CalculationRequest) =>
    request<CalculationDto>(`/calculations/${id}`, { method: "PUT", body: JSON.stringify(data) }),
  delete: (id: number) =>
    request<void>(`/calculations/${id}`, { method: "DELETE" }),
};
