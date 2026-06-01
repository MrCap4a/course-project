export interface TokenResponse {
  token: string;
  tokenType: string;
}

export interface PermissionDto {
  id: number;
  name: string;
}

export interface UserRoleDto {
  id: number;
  name: string;
  permissions: PermissionDto[];
}

export interface CurrentUserDto {
  id: number;
  login: string;
  name: string;
  surname: string;
  superAdmin: boolean;
  role: UserRoleDto | null;
}

export interface UserDto {
  id: number;
  login: string;
  name: string;
  surname: string;
  superAdmin: boolean;
  roleId: number | null;
  roleName: string | null;
}

export interface MaterialGroupDto {
  id: number;
  name: string;
}

export interface MaterialDto {
  id: number;
  name: string;
  price: number;
  units: string;
  groupId: number | null;
  groupName: string | null;
}

export interface FormulaGroupDto {
  id: number;
  name: string;
}

export interface FormulaDto {
  id: number;
  name: string;
  expression: string;
  groupId: number | null;
  groupName: string | null;
}

export interface CalculationItemDto {
  id: number;
  position: number;
  quantity: number;
  materialId: number;
  materialName: string;
  materialPrice: number;
  materialUnits: string;
  isConst: boolean;
}

export interface CalculationDto {
  id: number;
  name: string;
  formulaId: number;
  formulaName: string;
  formulaExpression: string;
  formulaGroupId: number;
  formulaGroupName: string;
  items: CalculationItemDto[];
  result: number;
}

export interface LoginRequest {
  login: string;
  password: string;
}

export interface RegisterRequest {
  login: string;
  password: string;
  name: string;
  surname: string;
  roleId: number | null;
}

export interface UserRoleRequest {
  name: string;
  permissionIds: number[];
}

export interface MaterialRequest {
  name: string;
  price: number;
  units: string;
  groupId: number | null;
}

export interface MaterialGroupRequest {
  name: string;
}

export interface FormulaRequest {
  name: string;
  expression: string;
  groupId: number | null;
}

export interface FormulaGroupRequest {
  name: string;
}

export interface CalculationItemRequest {
  position: number;
  quantity: number;
  materialId?: number;
}

export interface CalculationRequest {
  name: string;
  formulaId: number;
  items: CalculationItemRequest[];
}
