import { useEffect, useState } from "react";
import { Plus, Pencil, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { rolesApi, permissionsApi, usersApi } from "../api/client";
import type { UserRoleDto, PermissionDto } from "../api/types";
import { useAuth } from "../context/AuthContext";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../components/ui/table";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "../components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "../components/ui/alert-dialog";
import { Checkbox } from "../components/ui/checkbox";
import { Badge } from "../components/ui/badge";

const PERM_GROUPS: Record<string, string[]> = {
  "Расчёты": ["calculations.view", "calculations.create", "calculations.edit", "calculations.delete"],
  "Формулы": ["formulas.view", "formulas.create", "formulas.edit", "formulas.delete"],
  "Материалы": ["materials.view", "materials.create", "materials.edit", "materials.delete"],
  "Роли": ["roles.view", "roles.create", "roles.edit", "roles.delete"],
  "Пользователи": ["users.view", "users.create", "users.edit", "users.delete"],
};

const PERM_LABELS: Record<string, string> = {
  view: "Просмотр", create: "Создание", edit: "Редактирование", delete: "Удаление",
};

const ACTION_SHORT: Record<string, string> = {
  view: "просмотр", create: "создание", edit: "изменение", delete: "удаление",
};

const ALL_ACTIONS = ["view", "create", "edit", "delete"];

function formatPermissions(permissions: PermissionDto[]): { group: string; label: string }[] {
  const permNames = new Set(permissions.map((p) => p.name));
  return Object.entries(PERM_GROUPS).flatMap(([group, names]) => {
    const present = ALL_ACTIONS.filter((a) =>
      names.some((n) => n.endsWith(`.${a}`) && permNames.has(n))
    );
    if (present.length === 0) return [];
    const label = present.length === ALL_ACTIONS.length
      ? "все"
      : present.map((a) => ACTION_SHORT[a]).join(", ");
    return [{ group, label }];
  });
}


export function RolesPage() {
  const { can } = useAuth();
  const [roles, setRoles] = useState<UserRoleDto[]>([]);
  const [permissions, setPermissions] = useState<PermissionDto[]>([]);
  const [superAdminRoleIds, setSuperAdminRoleIds] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);

  const [dialog, setDialog] = useState<{ open: boolean; item?: UserRoleDto }>({ open: false });
  const [roleName, setRoleName] = useState("");
  const [selectedPerms, setSelectedPerms] = useState<Set<number>>(new Set());
  const [saving, setSaving] = useState(false);
  const [nameError, setNameError] = useState("");
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const load = async () => {
    try {
      const [r, p, u] = await Promise.all([rolesApi.getAll(), permissionsApi.getAll(), usersApi.getAll()]);
      setRoles(r);
      setPermissions(p);
      setSuperAdminRoleIds(
        new Set(u.filter((x) => x.superAdmin && x.roleId != null).map((x) => x.roleId as number))
      );
    } catch {
      toast.error("Ошибка загрузки");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const openCreate = () => {
    setRoleName(""); setSelectedPerms(new Set()); setNameError("");
    setDialog({ open: true });
  };
  const openEdit = (r: UserRoleDto) => {
    setRoleName(r.name);
    setSelectedPerms(new Set(r.permissions.map((p) => p.id)));
    setNameError("");
    setDialog({ open: true, item: r });
  };

  const togglePerm = (id: number) => {
    setSelectedPerms((prev) => {
      const next = new Set(prev);
      const perm = permissions.find((p) => p.id === id);
      if (!perm) return next;

      const dot    = perm.name.lastIndexOf(".");
      const module = perm.name.slice(0, dot);
      const action = perm.name.slice(dot + 1);

      if (next.has(id)) {
        next.delete(id);
        // снимаем view → убираем все остальные права модуля
        if (action === "view") {
          permissions
            .filter((p) => p.name.startsWith(module + ".") && p.id !== id)
            .forEach((p) => next.delete(p.id));
        }
      } else {
        next.add(id);
        // добавляем create/edit/delete → автоматически добавляем view
        if (action !== "view") {
          const viewPerm = permissions.find((p) => p.name === `${module}.view`);
          if (viewPerm) next.add(viewPerm.id);
        }
      }
      return next;
    });
  };

  const toggleGroup = (group: string) => {
    const ids = PERM_GROUPS[group].map((name) => permissions.find((p) => p.name === name)?.id).filter(Boolean) as number[];
    const allSelected = ids.every((id) => selectedPerms.has(id));
    setSelectedPerms((prev) => {
      const next = new Set(prev);
      ids.forEach((id) => (allSelected ? next.delete(id) : next.add(id)));
      return next;
    });
  };

  const save = async () => {
    if (!roleName.trim()) { setNameError("Введите название роли"); return; }
    setSaving(true);
    try {
      const data = { name: roleName.trim(), permissionIds: Array.from(selectedPerms) };
      if (dialog.item) {
        await rolesApi.update(dialog.item.id, data);
        toast.success("Роль обновлена");
      } else {
        await rolesApi.create(data);
        toast.success("Роль создана");
      }
      setDialog({ open: false });
      await load();
    } catch (e) {
      toast.error(String(e));
    } finally {
      setSaving(false);
    }
  };

  const deleteRole = async () => {
    if (!deleteId) return;
    try {
      await rolesApi.delete(deleteId);
      toast.success("Роль удалена");
      setDeleteId(null);
      await load();
    } catch (e) {
      toast.error(String(e));
    }
  };

  if (loading) return <div className="p-6 text-muted-foreground">Загрузка...</div>;

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-semibold">Роли</h1>
      {can("roles.create") && (
        <Button onClick={openCreate}>
          <Plus className="h-4 w-4" /> Добавить
        </Button>
      )}
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Название</TableHead>
              <TableHead>Права доступа</TableHead>
              <TableHead className="w-24" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {roles.length === 0 && (
              <TableRow><TableCell colSpan={3} className="text-center text-muted-foreground">Нет ролей</TableCell></TableRow>
            )}
            {roles.map((r) => (
              <TableRow key={r.id}>
                <TableCell className="font-medium">{r.name}</TableCell>
                <TableCell>
                  {r.permissions.length === 0 ? (
                    <span className="text-muted-foreground text-sm">—</span>
                  ) : (
                    <div className="flex flex-wrap gap-1">
                      {formatPermissions(r.permissions).map(({ group, label }) => (
                        <Badge key={group} variant="secondary" className="text-xs font-normal">
                          <span className="font-medium">{group}:</span>&nbsp;{label}
                        </Badge>
                      ))}
                    </div>
                  )}
                </TableCell>
                <TableCell>
                  <div className="flex gap-1 justify-end">
                    {can("roles.edit") && !superAdminRoleIds.has(r.id) && (
                      <Button variant="ghost" size="icon" onClick={() => openEdit(r)}>
                        <Pencil className="h-4 w-4" />
                      </Button>
                    )}
                    {can("roles.delete") && !superAdminRoleIds.has(r.id) && (
                      <Button variant="ghost" size="icon" onClick={() => setDeleteId(r.id)}>
                        <Trash2 className="h-4 w-4 text-destructive" />
                      </Button>
                    )}
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {/* Role dialog */}
      <Dialog open={dialog.open} onOpenChange={(open) => setDialog({ open })}>
        <DialogContent className="max-w-2xl max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{dialog.item ? "Редактировать роль" : "Новая роль"}</DialogTitle>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-1">
              <Label>Название <span className="text-destructive">*</span></Label>
              <Input
                value={roleName}
                onChange={(e) => { setRoleName(e.target.value); if (nameError) setNameError(""); }}
                className={nameError ? "border-destructive" : ""}
              />
              {nameError && <p className="text-xs text-destructive">{nameError}</p>}
            </div>
            <div className="space-y-3">
              <Label>Права доступа</Label>
              {Object.entries(PERM_GROUPS).map(([group, permNames]) => {
                const groupPerms = permNames.map((name) => permissions.find((p) => p.name === name)).filter(Boolean) as PermissionDto[];
                const groupIds = groupPerms.map((p) => p.id);
                const allChecked = groupIds.every((id) => selectedPerms.has(id));
                const someChecked = groupIds.some((id) => selectedPerms.has(id));
                return (
                  <div key={group} className="rounded-md border p-3 space-y-2">
                    <div className="flex items-center gap-2">
                      <Checkbox
                        id={`group-${group}`}
                        checked={allChecked ? true : someChecked ? "indeterminate" : false}
                        onCheckedChange={() => toggleGroup(group)}
                      />
                      <label htmlFor={`group-${group}`} className="text-sm font-medium cursor-pointer">{group}</label>
                    </div>
                    <div className="grid grid-cols-2 gap-2 pl-6">
                      {groupPerms.map((p) => {
                        const action = p.name.split(".")[1];
                        return (
                          <div key={p.id} className="flex items-center gap-2">
                            <Checkbox
                              id={`perm-${p.id}`}
                              checked={selectedPerms.has(p.id)}
                              onCheckedChange={() => togglePerm(p.id)}
                            />
                            <label htmlFor={`perm-${p.id}`} className="text-sm cursor-pointer">
                              {PERM_LABELS[action] ?? action}
                            </label>
                          </div>
                        );
                      })}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDialog({ open: false })}>Отмена</Button>
            <Button onClick={save} disabled={saving}>{saving ? "Сохранение..." : "Сохранить"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete */}
      <AlertDialog open={deleteId !== null} onOpenChange={(open) => !open && setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Удалить роль?</AlertDialogTitle>
            <AlertDialogDescription>Это действие нельзя отменить.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Отмена</AlertDialogCancel>
            <AlertDialogAction className="bg-destructive text-destructive-foreground hover:bg-destructive/90" onClick={deleteRole}>Удалить</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
