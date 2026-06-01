import { useEffect, useState } from "react";
import { Plus, Pencil, Trash2, Shield } from "lucide-react";
import { toast } from "sonner";
import { usersApi, rolesApi } from "../api/client";
import type { UserDto, UserRoleDto } from "../api/types";
import { useAuth } from "../context/AuthContext";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../components/ui/table";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "../components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "../components/ui/alert-dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../components/ui/select";
import { Badge } from "../components/ui/badge";

type UserErrors = Partial<Record<"login" | "password" | "name" | "surname", string>>;

export function UsersPage() {
  const { can } = useAuth();
  const [users, setUsers] = useState<UserDto[]>([]);
  const [roles, setRoles] = useState<UserRoleDto[]>([]);
  const [loading, setLoading] = useState(true);

  const [dialog, setDialog] = useState<{ open: boolean; item?: UserDto }>({ open: false });
  const [login, setLogin] = useState("");
  const [password, setPassword] = useState("");
  const [name, setName] = useState("");
  const [surname, setSurname] = useState("");
  const [roleId, setRoleId] = useState<string>("none");
  const [saving, setSaving] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [errors, setErrors] = useState<UserErrors>({});

  const clearError = (field: keyof UserErrors) =>
    setErrors((prev) => { const e = { ...prev }; delete e[field]; return e; });

  const load = async () => {
    try {
      const [u, r] = await Promise.all([usersApi.getAll(), rolesApi.getAll()]);
      setUsers(u);
      setRoles(r);
    } catch {
      toast.error("Ошибка загрузки");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const openCreate = () => {
    setLogin(""); setPassword(""); setName(""); setSurname(""); setRoleId("none"); setErrors({});
    setDialog({ open: true });
  };
  const openEdit = (u: UserDto) => {
    setLogin(u.login); setPassword(""); setName(u.name); setSurname(u.surname);
    setRoleId(u.roleId != null ? String(u.roleId) : "none");
    setErrors({});
    setDialog({ open: true, item: u });
  };

  const save = async () => {
    const errs: UserErrors = {};
    if (!login.trim())   errs.login   = "Введите логин";
    if (!name.trim())    errs.name    = "Введите имя";
    if (!surname.trim()) errs.surname = "Введите фамилию";
    if (!dialog.item && !password.trim()) errs.password = "Введите пароль";
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setSaving(true);
    try {
      const data = {
        login: login.trim(),
        password: password.trim() || "unchanged",
        name: name.trim(),
        surname: surname.trim(),
        roleId: roleId !== "none" ? parseInt(roleId) : null,
      };
      if (dialog.item) {
        await usersApi.update(dialog.item.id, data);
        toast.success("Пользователь обновлён");
      } else {
        await usersApi.create(data);
        toast.success("Пользователь создан");
      }
      setDialog({ open: false });
      await load();
    } catch (e) {
      toast.error(String(e));
    } finally {
      setSaving(false);
    }
  };

  const deleteUser = async () => {
    if (!deleteId) return;
    try {
      await usersApi.delete(deleteId);
      toast.success("Пользователь удалён");
      setDeleteId(null);
      await load();
    } catch (e) {
      toast.error(String(e));
    }
  };

  if (loading) return <div className="p-6 text-muted-foreground">Загрузка...</div>;

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-semibold">Пользователи</h1>
      {can("users.create") && (
        <Button onClick={openCreate}>
          <Plus className="h-4 w-4" /> Добавить
        </Button>
      )}
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>ФИО</TableHead>
              <TableHead>Логин</TableHead>
              <TableHead>Роль</TableHead>
              <TableHead className="w-24" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {users.length === 0 && (
              <TableRow><TableCell colSpan={4} className="text-center text-muted-foreground">Нет пользователей</TableCell></TableRow>
            )}
            {users.map((u) => (
              <TableRow key={u.id}>
                <TableCell>
                  <div className="flex items-center gap-2">
                    {u.superAdmin && <Shield className="h-4 w-4 text-muted-foreground" />}
                    {u.surname} {u.name}
                  </div>
                </TableCell>
                <TableCell>{u.login}</TableCell>
                <TableCell>
                  {u.roleName
                    ? <Badge variant="secondary">{u.roleName}</Badge>
                    : <span className="text-muted-foreground">—</span>}
                </TableCell>
                <TableCell>
                  <div className="flex gap-1 justify-end">
                    {can("users.edit") && !u.superAdmin && (
                      <Button variant="ghost" size="icon" onClick={() => openEdit(u)}>
                        <Pencil className="h-4 w-4" />
                      </Button>
                    )}
                    {can("users.delete") && !u.superAdmin && (
                      <Button variant="ghost" size="icon" onClick={() => setDeleteId(u.id)}>
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

      {/* User dialog */}
      <Dialog open={dialog.open} onOpenChange={(open) => setDialog({ open })}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{dialog.item ? "Редактировать пользователя" : "Новый пользователь"}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label>Имя <span className="text-destructive">*</span></Label>
                <Input
                  value={name}
                  onChange={(e) => { setName(e.target.value); clearError("name"); }}
                  className={errors.name ? "border-destructive" : ""}
                />
                {errors.name && <p className="text-xs text-destructive">{errors.name}</p>}
              </div>
              <div className="space-y-1">
                <Label>Фамилия <span className="text-destructive">*</span></Label>
                <Input
                  value={surname}
                  onChange={(e) => { setSurname(e.target.value); clearError("surname"); }}
                  className={errors.surname ? "border-destructive" : ""}
                />
                {errors.surname && <p className="text-xs text-destructive">{errors.surname}</p>}
              </div>
            </div>
            <div className="space-y-1">
              <Label>Логин <span className="text-destructive">*</span></Label>
              <Input
                value={login}
                onChange={(e) => { setLogin(e.target.value); clearError("login"); }}
                className={errors.login ? "border-destructive" : ""}
                autoComplete="off"
              />
              {errors.login && <p className="text-xs text-destructive">{errors.login}</p>}
            </div>
            <div className="space-y-1">
              <Label>
                {dialog.item ? "Новый пароль (оставьте пустым, чтобы не менять)" : <>Пароль <span className="text-destructive">*</span></>}
              </Label>
              <Input
                type="password"
                value={password}
                onChange={(e) => { setPassword(e.target.value); clearError("password"); }}
                className={errors.password ? "border-destructive" : ""}
                autoComplete="new-password"
              />
              {errors.password && <p className="text-xs text-destructive">{errors.password}</p>}
            </div>
            <div className="space-y-1">
              <Label>Роль</Label>
              <Select value={roleId} onValueChange={setRoleId}>
                <SelectTrigger><SelectValue placeholder="Без роли" /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="none">Без роли</SelectItem>
                  {roles.map((r) => (
                    <SelectItem key={r.id} value={String(r.id)}>{r.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
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
            <AlertDialogTitle>Удалить пользователя?</AlertDialogTitle>
            <AlertDialogDescription>Это действие нельзя отменить.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Отмена</AlertDialogCancel>
            <AlertDialogAction className="bg-destructive text-destructive-foreground hover:bg-destructive/90" onClick={deleteUser}>Удалить</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
