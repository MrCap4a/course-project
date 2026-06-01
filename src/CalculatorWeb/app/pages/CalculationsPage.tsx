import { useEffect, useState } from "react";
import { Plus, Pencil, Trash2, Info } from "lucide-react";
import { toast } from "sonner";
import { calculationsApi, formulasApi, formulaGroupsApi, materialsApi } from "../api/client";
import type { CalculationDto, FormulaDto, FormulaGroupDto, MaterialDto } from "../api/types";
import { useAuth } from "../context/AuthContext";
import { Button } from "../components/ui/button";
import { Input } from "../components/ui/input";
import { Label } from "../components/ui/label";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "../components/ui/table";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "../components/ui/dialog";
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle } from "../components/ui/alert-dialog";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "../components/ui/select";
import { Separator } from "../components/ui/separator";

interface PlaceholderRow {
  position: number;
  placeholder: string;
  isConst: boolean;
  materialId: string;
  quantity: string;
}

function extractPlaceholders(expression: string): string[] {
  return [...expression.matchAll(/\{([^}]+)\}/g)].map((m) => m[1]);
}

function buildRows(placeholders: string[]): PlaceholderRow[] {
  return placeholders.map((ph, i) => ({
    position: i,
    placeholder: ph,
    isConst: ph.toLowerCase() === "const",
    materialId: "",
    quantity: "",
  }));
}

export function CalculationsPage() {
  const { can } = useAuth();
  const [calculations, setCalculations] = useState<CalculationDto[]>([]);
  const [formulas, setFormulas] = useState<FormulaDto[]>([]);
  const [formulaGroups, setFormulaGroups] = useState<FormulaGroupDto[]>([]);
  const [materials, setMaterials] = useState<MaterialDto[]>([]);
  const [loading, setLoading] = useState(true);

  const [dialog, setDialog] = useState<{ open: boolean; item?: CalculationDto }>({ open: false });
  const [calcName, setCalcName] = useState("");
  const [formulaId, setFormulaId] = useState<string>("");
  const [rows, setRows] = useState<PlaceholderRow[]>([]);
  const [saving, setSaving] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [nameError, setNameError] = useState("");
  const [formulaError, setFormulaError] = useState("");
  const [search, setSearch] = useState("");
  const [filterGroupId, setFilterGroupId] = useState<string>("all");
  const [filterFormulaId, setFilterFormulaId] = useState<string>("all");

  const load = async () => {
    try {
      const [c, f, fg, m] = await Promise.all([
        calculationsApi.getAll(),
        formulasApi.getAll(),
        formulaGroupsApi.getAll(),
        materialsApi.getAll(),
      ]);
      setCalculations(c);
      setFormulas(f);
      setFormulaGroups(fg);
      setMaterials(m);
    } catch {
      toast.error("Ошибка загрузки");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const selectedFormula = formulas.find((f) => String(f.id) === formulaId);

  const filteredCalculations = calculations.filter((c) => {
    const matchSearch = !search || c.name.toLowerCase().includes(search.toLowerCase())
      || c.formulaName.toLowerCase().includes(search.toLowerCase());
    const matchGroup   = filterGroupId   === "all" || String(c.formulaGroupId)  === filterGroupId;
    const matchFormula = filterFormulaId === "all" || String(c.formulaId)       === filterFormulaId;
    return matchSearch && matchGroup && matchFormula;
  });

  // When group filter changes, reset formula filter if selected formula is not in this group
  const visibleFormulas = filterGroupId === "all"
    ? formulas
    : formulas.filter((f) => String(f.groupId) === filterGroupId);

  const handleFormulaChange = (newId: string) => {
    setFormulaId(newId);
    if (formulaError) setFormulaError("");
    const formula = formulas.find((f) => String(f.id) === newId);
    if (!formula) { setRows([]); return; }
    setRows(buildRows(extractPlaceholders(formula.expression)));
  };

  const openCreate = () => {
    setCalcName(""); setFormulaId(""); setRows([]);
    setNameError(""); setFormulaError("");
    setDialog({ open: true });
  };

  const openEdit = (c: CalculationDto) => {
    setCalcName(c.name);
    setFormulaId(String(c.formulaId));
    setNameError(""); setFormulaError("");

    const formula = formulas.find((f) => f.id === c.formulaId);
    if (formula) {
      const built = buildRows(extractPlaceholders(formula.expression));
      const sorted = [...c.items].sort((a, b) => a.position - b.position);
      sorted.forEach((item, idx) => {
        if (idx < built.length) {
          built[idx].quantity = String(item.quantity);
          if (!built[idx].isConst && item.materialId) {
            built[idx].materialId = String(item.materialId);
          }
        }
      });
      setRows(built);
    } else {
      setRows([]);
    }

    setDialog({ open: true, item: c });
  };

  const updateRow = (position: number, field: "materialId" | "quantity", value: string) => {
    setRows((prev) => prev.map((r) => r.position === position ? { ...r, [field]: value } : r));
  };

  const save = async () => {
    if (!calcName.trim()) { setNameError("Введите название расчёта"); return; }
    if (!formulaId)        { setFormulaError("Выберите формулу"); return; }
    if (rows.length === 0) { toast.error("Формула не содержит позиций"); return; }

    for (const row of rows) {
      if (!row.quantity.trim()) {
        toast.error(`Введите количество для позиции ${row.position + 1}`);
        return;
      }
      if (!row.isConst && !row.materialId) {
        toast.error(`Выберите материал для «${row.placeholder}» (позиция ${row.position + 1})`);
        return;
      }
    }

    const items = rows.map((row) => {
      const item: { position: number; quantity: number; materialId?: number } = {
        position: row.position,
        quantity: parseFloat(row.quantity),
      };
      if (!row.isConst) item.materialId = parseInt(row.materialId);
      return item;
    });

    setSaving(true);
    try {
      const data = { name: calcName.trim(), formulaId: parseInt(formulaId), items };
      if (dialog.item) {
        await calculationsApi.update(dialog.item.id, data);
        toast.success("Расчёт обновлён");
      } else {
        await calculationsApi.create(data);
        toast.success("Расчёт создан");
      }
      setDialog({ open: false });
      await load();
    } catch (e) {
      toast.error(String(e));
    } finally {
      setSaving(false);
    }
  };

  const deleteCalc = async () => {
    if (!deleteId) return;
    try {
      await calculationsApi.delete(deleteId);
      toast.success("Расчёт удалён");
      setDeleteId(null);
      await load();
    } catch (e) {
      toast.error(String(e));
    }
  };

  if (loading) return <div className="p-6 text-muted-foreground">Загрузка...</div>;

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-semibold">Расчёты</h1>
      <div className="flex gap-2 flex-wrap">
        <Input
          placeholder="Поиск по названию или формуле..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="max-w-xs"
        />
        <Select
          value={filterGroupId}
          onValueChange={(v) => {
            setFilterGroupId(v);
            setFilterFormulaId("all");
          }}
        >
          <SelectTrigger className="w-44">
            <SelectValue placeholder="Все группы" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Все группы</SelectItem>
            {formulaGroups.map((g) => (
              <SelectItem key={g.id} value={String(g.id)}>{g.name}</SelectItem>
            ))}
          </SelectContent>
        </Select>
        <Select value={filterFormulaId} onValueChange={setFilterFormulaId}>
          <SelectTrigger className="w-52">
            <SelectValue placeholder="Все формулы" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="all">Все формулы</SelectItem>
            {visibleFormulas.map((f) => (
              <SelectItem key={f.id} value={String(f.id)}>{f.name}</SelectItem>
            ))}
          </SelectContent>
        </Select>
        {can("calculations.create") && (
          <Button onClick={openCreate} className="ml-auto">
            <Plus className="h-4 w-4" /> Новый расчёт
          </Button>
        )}
      </div>
      <div className="rounded-md border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Название</TableHead>
              <TableHead>Формула</TableHead>
              <TableHead>Группа</TableHead>
              <TableHead>Результат</TableHead>
              <TableHead className="w-24" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredCalculations.length === 0 && (
              <TableRow><TableCell colSpan={5} className="text-center text-muted-foreground">Нет расчётов</TableCell></TableRow>
            )}
            {filteredCalculations.map((c) => (
              <TableRow key={c.id}>
                <TableCell className="font-medium">{c.name}</TableCell>
                <TableCell>
                  <div className="flex flex-col">
                    <span>{c.formulaName}</span>
                    <code className="text-xs text-muted-foreground font-mono">{c.formulaExpression}</code>
                  </div>
                </TableCell>
                <TableCell className="text-muted-foreground">{c.formulaGroupName || "—"}</TableCell>
                <TableCell className="font-semibold">
                  {c.result != null
                    ? c.result.toLocaleString("ru-RU", { minimumFractionDigits: 2, maximumFractionDigits: 2 })
                    : "—"}
                </TableCell>
                <TableCell>
                  <div className="flex gap-1 justify-end">
                    {can("calculations.edit") && (
                      <Button variant="ghost" size="icon" onClick={() => openEdit(c)}>
                        <Pencil className="h-4 w-4" />
                      </Button>
                    )}
                    {can("calculations.delete") && (
                      <Button variant="ghost" size="icon" onClick={() => setDeleteId(c.id)}>
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

      {/* Calculation dialog */}
      <Dialog open={dialog.open} onOpenChange={(open) => setDialog({ open })}>
        <DialogContent className="sm:max-w-xl max-h-[90vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>{dialog.item ? "Редактировать расчёт" : "Новый расчёт"}</DialogTitle>
          </DialogHeader>

          <div className="space-y-4">
            <div className="space-y-1">
              <Label>Название <span className="text-destructive">*</span></Label>
              <Input
                value={calcName}
                onChange={(e) => { setCalcName(e.target.value); if (nameError) setNameError(""); }}
                placeholder="Введите название"
                className={nameError ? "border-destructive" : ""}
              />
              {nameError && <p className="text-xs text-destructive">{nameError}</p>}
            </div>

            <div className="space-y-1">
              <Label>Формула <span className="text-destructive">*</span></Label>
              <Select value={formulaId} onValueChange={handleFormulaChange}>
                <SelectTrigger className={formulaError ? "border-destructive" : ""}>
                  <SelectValue placeholder="Выберите формулу" />
                </SelectTrigger>
                <SelectContent>
                  {formulas.map((f) => (
                    <SelectItem key={f.id} value={String(f.id)}>{f.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
              {formulaError && <p className="text-xs text-destructive">{formulaError}</p>}
              {selectedFormula && (
                <div className="flex items-center gap-1.5 text-xs text-muted-foreground mt-1">
                  <Info className="h-3 w-3 shrink-0" />
                  <code className="font-mono">{selectedFormula.expression}</code>
                </div>
              )}
            </div>

            {(rows.length > 0 || formulaId) && <Separator />}

            {formulaId && rows.length === 0 && (
              <p className="text-sm text-muted-foreground">Формула не содержит позиций.</p>
            )}

            {!formulaId && (
              <p className="text-sm text-muted-foreground italic">Выберите формулу — позиции заполнятся автоматически.</p>
            )}

            {rows.length > 0 && (
              <div className="space-y-2">
                <Label>Позиции расчёта</Label>
                <div className="space-y-2">
                  {rows.map((row) => (
                    <div
                      key={row.position}
                      className="flex items-center gap-3 rounded-lg border bg-muted/30 px-3 py-2.5"
                    >
                      <span className="min-w-[148px] shrink-0 text-sm font-medium">
                        {row.isConst
                          ? `Константа ${row.position + 1}`
                          : `Материал {${row.placeholder}}`}:
                      </span>

                      {!row.isConst && (
                        <>
                          <Select
                            value={row.materialId}
                            onValueChange={(v) => updateRow(row.position, "materialId", v)}
                          >
                            <SelectTrigger className="flex-1">
                              <SelectValue placeholder="Выберите материал" />
                            </SelectTrigger>
                            <SelectContent>
                              {materials
                                .filter((m) => m.groupName?.toLowerCase() === row.placeholder.toLowerCase())
                                .map((m) => (
                                  <SelectItem key={m.id} value={String(m.id)}>
                                    {m.name} ({m.price.toLocaleString("ru-RU")} / {m.units})
                                  </SelectItem>
                                ))}
                            </SelectContent>
                          </Select>
                          <span className="shrink-0 text-muted-foreground">×</span>
                        </>
                      )}

                      <Input
                        type="number"
                        min="0"
                        step="0.01"
                        placeholder="Кол-во"
                        value={row.quantity}
                        onChange={(e) => updateRow(row.position, "quantity", e.target.value)}
                        className={row.isConst ? "flex-1" : "w-24 shrink-0"}
                      />
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={() => setDialog({ open: false })}>Отмена</Button>
            <Button onClick={save} disabled={saving}>
              {saving ? "Сохранение..." : "Рассчитать и сохранить"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete */}
      <AlertDialog open={deleteId !== null} onOpenChange={(open) => !open && setDeleteId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Удалить расчёт?</AlertDialogTitle>
            <AlertDialogDescription>Это действие нельзя отменить.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Отмена</AlertDialogCancel>
            <AlertDialogAction className="bg-destructive text-destructive-foreground hover:bg-destructive/90" onClick={deleteCalc}>Удалить</AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
