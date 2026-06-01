import { useEffect, useRef, useState } from "react";
import { Plus, Pencil, Trash2, FolderPlus, FolderPen } from "lucide-react";
import { toast } from "sonner";
import { formulasApi, formulaGroupsApi, materialGroupsApi } from "../api/client";
import type { FormulaDto, FormulaGroupDto, MaterialGroupDto } from "../api/types";
import { useAuth } from "../context/AuthContext";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../components/ui/table";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "../components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "../components/ui/alert-dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../components/ui/select";
import { Badge } from "../components/ui/badge";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "../components/ui/tabs";

type FmErrors = Partial<Record<"name" | "expression", string>>;

export function FormulasPage() {
  const { can } = useAuth();
  const [formulas, setFormulas] = useState<FormulaDto[]>([]);
  const [groups, setGroups] = useState<FormulaGroupDto[]>([]);
  const [materialGroups, setMaterialGroups] = useState<MaterialGroupDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [fmSearch, setFmSearch] = useState("");
  const [fmFilterGroupId, setFmFilterGroupId] = useState<string>("all");

  // Formula dialog
  const [fmDialog, setFmDialog] = useState<{ open: boolean; item?: FormulaDto }>({ open: false });
  const [fmName, setFmName] = useState("");
  const [fmExpr, setFmExpr] = useState("");
  const [fmGroupId, setFmGroupId] = useState<string>("none");
  const [fmSaving, setFmSaving] = useState(false);
  const [fmErrors, setFmErrors] = useState<FmErrors>({});

  // Material groups search panel inside formula dialog
  const [matGroupSearch, setMatGroupSearch] = useState("");
  const exprInputRef = useRef<HTMLInputElement>(null);

  // Group dialog
  const [grpDialog, setGrpDialog] = useState<{ open: boolean; item?: FormulaGroupDto }>({ open: false });
  const [grpName, setGrpName] = useState("");
  const [grpSaving, setGrpSaving] = useState(false);
  const [grpNameError, setGrpNameError] = useState("");

  // Delete
  const [deleteFmId, setDeleteFmId] = useState<number | null>(null);
  const [deleteGrpId, setDeleteGrpId] = useState<number | null>(null);

  const clearFmError = (f: keyof FmErrors) =>
    setFmErrors((prev) => { const e = { ...prev }; delete e[f]; return e; });

  const load = async () => {
    try {
      const [f, g, mg] = await Promise.all([
        formulasApi.getAll(),
        formulaGroupsApi.getAll(),
        materialGroupsApi.getAll(),
      ]);
      setFormulas(f);
      setGroups(g);
      setMaterialGroups(mg);
    } catch {
      toast.error("Ошибка загрузки");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  // Formula CRUD
  const openFmCreate = () => {
    setFmName(""); setFmExpr(""); setFmGroupId("none"); setMatGroupSearch(""); setFmErrors({});
    setFmDialog({ open: true });
  };
  const openFmEdit = (f: FormulaDto) => {
    setFmName(f.name); setFmExpr(f.expression);
    setFmGroupId(f.groupId != null ? String(f.groupId) : "none");
    setMatGroupSearch(""); setFmErrors({});
    setFmDialog({ open: true, item: f });
  };
  const saveFormula = async () => {
    const errs: FmErrors = {};
    if (!fmName.trim()) errs.name = "Введите название";
    if (!fmExpr.trim()) errs.expression = "Введите выражение формулы";
    if (Object.keys(errs).length) { setFmErrors(errs); return; }

    setFmSaving(true);
    try {
      const data = {
        name: fmName.trim(),
        expression: fmExpr.trim(),
        groupId: fmGroupId !== "none" ? parseInt(fmGroupId) : null,
      };
      if (fmDialog.item) {
        await formulasApi.update(fmDialog.item.id, data);
        toast.success("Формула обновлена");
      } else {
        await formulasApi.create(data);
        toast.success("Формула создана");
      }
      setFmDialog({ open: false });
      await load();
    } catch (e) {
      toast.error(String(e));
    } finally {
      setFmSaving(false);
    }
  };
  const deleteFormula = async () => {
    if (!deleteFmId) return;
    try {
      await formulasApi.delete(deleteFmId);
      toast.success("Формула удалена");
      setDeleteFmId(null);
      await load();
    } catch (e) {
      toast.error(String(e));
    }
  };

  // Insert material group token at cursor position in expression field
  const insertGroup = (name: string) => {
    const toInsert = `{${name}}`;
    const input = exprInputRef.current;
    if (!input) {
      setFmExpr((prev) => prev + toInsert);
      return;
    }
    const start = input.selectionStart ?? fmExpr.length;
    const end = input.selectionEnd ?? fmExpr.length;
    const newVal = fmExpr.slice(0, start) + toInsert + fmExpr.slice(end);
    setFmExpr(newVal);
    if (fmErrors.expression) clearFmError("expression");
    requestAnimationFrame(() => {
      input.setSelectionRange(start + toInsert.length, start + toInsert.length);
      input.focus();
    });
  };

  // Group CRUD
  const openGrpCreate = () => { setGrpName(""); setGrpNameError(""); setGrpDialog({ open: true }); };
  const openGrpEdit = (g: FormulaGroupDto) => { setGrpName(g.name); setGrpNameError(""); setGrpDialog({ open: true, item: g }); };
  const saveGroup = async () => {
    if (!grpName.trim()) { setGrpNameError("Введите название группы"); return; }
    setGrpSaving(true);
    try {
      if (grpDialog.item) {
        await formulaGroupsApi.update(grpDialog.item.id, { name: grpName.trim() });
        toast.success("Группа обновлена");
      } else {
        await formulaGroupsApi.create({ name: grpName.trim() });
        toast.success("Группа создана");
      }
      setGrpDialog({ open: false });
      await load();
    } catch (e) {
      toast.error(String(e));
    } finally {
      setGrpSaving(false);
    }
  };
  const deleteGroup = async () => {
    if (!deleteGrpId) return;
    try {
      await formulaGroupsApi.delete(deleteGrpId);
      toast.success("Группа удалена");
      setDeleteGrpId(null);
      await load();
    } catch (e) {
      toast.error(String(e));
    }
  };

  const filteredFormulas = formulas.filter((f) => {
    const matchSearch = !fmSearch || f.name.toLowerCase().includes(fmSearch.toLowerCase());
    const matchGroup = fmFilterGroupId === "all" || String(f.groupId) === fmFilterGroupId;
    return matchSearch && matchGroup;
  });

  const filteredPanelGroups = materialGroups.filter((g) =>
    !matGroupSearch || g.name.toLowerCase().includes(matGroupSearch.toLowerCase())
  );

  if (loading) return <div className="p-6 text-muted-foreground">Загрузка...</div>;

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-semibold">Формулы</h1>

      <Tabs defaultValue="formulas">
        <TabsList>
          <TabsTrigger value="formulas">Формулы</TabsTrigger>
          <TabsTrigger value="groups">Группы</TabsTrigger>
        </TabsList>

        <TabsContent value="formulas" className="space-y-4">
          <div className="flex gap-2">
            <Input
              placeholder="Поиск по названию..."
              value={fmSearch}
              onChange={(e) => setFmSearch(e.target.value)}
              className="max-w-xs"
            />
            <Select value={fmFilterGroupId} onValueChange={setFmFilterGroupId}>
              <SelectTrigger className="w-44">
                <SelectValue placeholder="Все группы" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Все группы</SelectItem>
                {groups.map((g) => (
                  <SelectItem key={g.id} value={String(g.id)}>{g.name}</SelectItem>
                ))}
              </SelectContent>
            </Select>
            {can("formulas.create") && (
              <Button onClick={openFmCreate} className="ml-auto">
                <Plus className="h-4 w-4" /> Добавить
              </Button>
            )}
          </div>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Название</TableHead>
                  <TableHead>Выражение</TableHead>
                  <TableHead>Группа</TableHead>
                  <TableHead className="w-24" />
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredFormulas.length === 0 && (
                  <TableRow><TableCell colSpan={4} className="text-center text-muted-foreground">Нет данных</TableCell></TableRow>
                )}
                {filteredFormulas.map((f) => (
                  <TableRow key={f.id}>
                    <TableCell>{f.name}</TableCell>
                    <TableCell>
                      <code className="rounded bg-muted px-1.5 py-0.5 text-xs font-mono">{f.expression}</code>
                    </TableCell>
                    <TableCell>
                      {f.groupName ? <Badge variant="secondary">{f.groupName}</Badge> : <span className="text-muted-foreground">—</span>}
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-1 justify-end">
                        {can("formulas.edit") && (
                          <Button variant="ghost" size="icon" onClick={() => openFmEdit(f)}>
                            <Pencil className="h-4 w-4" />
                          </Button>
                        )}
                        {can("formulas.delete") && (
                          <Button variant="ghost" size="icon" onClick={() => setDeleteFmId(f.id)}>
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
        </TabsContent>

        <TabsContent value="groups" className="space-y-4">
          {can("formulas.create") && (
            <Button onClick={openGrpCreate}>
              <FolderPlus className="h-4 w-4" /> Добавить группу
            </Button>
          )}
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Название</TableHead>
                  <TableHead>Кол-во формул</TableHead>
                  <TableHead className="w-24" />
                </TableRow>
              </TableHeader>
              <TableBody>
                {groups.length === 0 && (
                  <TableRow><TableCell colSpan={3} className="text-center text-muted-foreground">Нет групп</TableCell></TableRow>
                )}
                {groups.map((g) => (
                  <TableRow key={g.id}>
                    <TableCell>{g.name}</TableCell>
                    <TableCell>{formulas.filter((f) => f.groupId === g.id).length}</TableCell>
                    <TableCell>
                      <div className="flex gap-1 justify-end">
                        {can("formulas.edit") && (
                          <Button variant="ghost" size="icon" onClick={() => openGrpEdit(g)}>
                            <FolderPen className="h-4 w-4" />
                          </Button>
                        )}
                        {can("formulas.delete") && (
                          <Button variant="ghost" size="icon" onClick={() => setDeleteGrpId(g.id)}>
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
        </TabsContent>
      </Tabs>

      {/* Formula dialog */}
      <Dialog open={fmDialog.open} onOpenChange={(open) => setFmDialog({ open })}>
        <DialogContent className="sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle>{fmDialog.item ? "Редактировать формулу" : "Новая формула"}</DialogTitle>
          </DialogHeader>

          <div className="flex gap-5">
            {/* Left: form fields */}
            <div className="flex-1 min-w-0 space-y-3">
              <div className="space-y-1">
                <Label>Название <span className="text-destructive">*</span></Label>
                <Input
                  value={fmName}
                  onChange={(e) => { setFmName(e.target.value); clearFmError("name"); }}
                  className={fmErrors.name ? "border-destructive" : ""}
                />
                {fmErrors.name && <p className="text-xs text-destructive">{fmErrors.name}</p>}
              </div>
              <div className="space-y-1">
                <Label>Выражение <span className="text-destructive">*</span></Label>
                <Input
                  ref={exprInputRef}
                  value={fmExpr}
                  onChange={(e) => { setFmExpr(e.target.value); clearFmError("expression"); }}
                  placeholder="({дерево} + {клей}) * {const}"
                  className={`font-mono${fmErrors.expression ? " border-destructive" : ""}`}
                />
                {fmErrors.expression
                  ? <p className="text-xs text-destructive">{fmErrors.expression}</p>
                  : <p className="text-xs text-muted-foreground">
                      <code className="font-mono">{"{название_группы}"}</code> — выбор материала,{" "}
                      <code className="font-mono">{"{const}"}</code> — произвольное число.
                    </p>
                }
              </div>
              <div className="space-y-1">
                <Label>Группа формулы</Label>
                <Select value={fmGroupId} onValueChange={setFmGroupId}>
                  <SelectTrigger><SelectValue placeholder="Без группы" /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="none">Без группы</SelectItem>
                    {groups.map((g) => (
                      <SelectItem key={g.id} value={String(g.id)}>{g.name}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            {/* Right: material groups hint panel */}
            <div className="w-48 shrink-0 space-y-2">
              <div>
                <p className="text-sm font-semibold leading-none">Группы материалов</p>
                <p className="text-xs text-muted-foreground mt-1">Нажмите — вставить в выражение</p>
              </div>
              <Input
                placeholder="Поиск..."
                value={matGroupSearch}
                onChange={(e) => setMatGroupSearch(e.target.value)}
                className="h-8 text-sm"
              />
              <div className="border rounded-md overflow-y-auto max-h-[172px]">
                {filteredPanelGroups.length === 0 && (
                  <p className="text-xs text-muted-foreground text-center py-3">Нет групп</p>
                )}
                {filteredPanelGroups.map((g) => (
                  <button
                    key={g.id}
                    type="button"
                    onMouseDown={(e) => e.preventDefault()}
                    onClick={() => insertGroup(g.name)}
                    className="w-full text-left px-3 py-1.5 text-xs font-mono text-blue-700 hover:bg-blue-50 border-b last:border-b-0 transition-colors"
                  >
                    {"{" + g.name + "}"}
                  </button>
                ))}
              </div>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setFmDialog({ open: false })}>Отмена</Button>
            <Button onClick={saveFormula} disabled={fmSaving}>
              {fmSaving ? "Сохранение..." : "Сохранить"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Group dialog */}
      <Dialog open={grpDialog.open} onOpenChange={(open) => setGrpDialog({ open })}>
        <DialogContent className="max-w-sm">
          <DialogHeader>
            <DialogTitle>{grpDialog.item ? "Редактировать группу" : "Новая группа"}</DialogTitle>
          </DialogHeader>
          <div className="space-y-1">
            <Label>Название <span className="text-destructive">*</span></Label>
            <Input
              value={grpName}
              onChange={(e) => { setGrpName(e.target.value); if (grpNameError) setGrpNameError(""); }}
              className={grpNameError ? "border-destructive" : ""}
            />
            {grpNameError && <p className="text-xs text-destructive">{grpNameError}</p>}
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setGrpDialog({ open: false })}>Отмена</Button>
            <Button onClick={saveGroup} disabled={grpSaving}>{grpSaving ? "Сохранение..." : "Сохранить"}</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete formula */}
      <AlertDialog open={deleteFmId !== null} onOpenChange={(open) => !open && setDeleteFmId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Удалить формулу?</AlertDialogTitle>
            <AlertDialogDescription>Это действие нельзя отменить.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Отмена</AlertDialogCancel>
            <AlertDialogAction className="bg-destructive text-destructive-foreground hover:bg-destructive/90" onClick={deleteFormula}>Удалить</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Delete group */}
      <AlertDialog open={deleteGrpId !== null} onOpenChange={(open) => !open && setDeleteGrpId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Удалить группу?</AlertDialogTitle>
            <AlertDialogDescription>Все формулы этой группы станут без группы.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Отмена</AlertDialogCancel>
            <AlertDialogAction className="bg-destructive text-destructive-foreground hover:bg-destructive/90" onClick={deleteGroup}>Удалить</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
