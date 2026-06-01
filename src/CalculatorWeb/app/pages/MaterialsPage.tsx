import { useEffect, useState } from "react";
import { Plus, Pencil, Trash2, FolderPlus, FolderPen } from "lucide-react";
import { toast } from "sonner";
import { materialsApi, materialGroupsApi } from "../api/client";
import type { MaterialDto, MaterialGroupDto } from "../api/types";
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

type MatErrors = Partial<Record<"name" | "price" | "units", string>>;

export function MaterialsPage() {
  const { can } = useAuth();
  const [materials, setMaterials] = useState<MaterialDto[]>([]);
  const [groups, setGroups] = useState<MaterialGroupDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
  const [filterGroupId, setFilterGroupId] = useState<string>("all");

  // Material dialog
  const [matDialog, setMatDialog] = useState<{ open: boolean; item?: MaterialDto }>({ open: false });
  const [matName, setMatName] = useState("");
  const [matPrice, setMatPrice] = useState("");
  const [matUnits, setMatUnits] = useState("");
  const [matGroupId, setMatGroupId] = useState<string>("none");
  const [matSaving, setMatSaving] = useState(false);
  const [matErrors, setMatErrors] = useState<MatErrors>({});

  // Group dialog
  const [grpDialog, setGrpDialog] = useState<{ open: boolean; item?: MaterialGroupDto }>({ open: false });
  const [grpName, setGrpName] = useState("");
  const [grpSaving, setGrpSaving] = useState(false);
  const [grpNameError, setGrpNameError] = useState("");

  // Delete dialogs
  const [deleteMatId, setDeleteMatId] = useState<number | null>(null);
  const [deleteGrpId, setDeleteGrpId] = useState<number | null>(null);
  const [deleteGrpTarget, setDeleteGrpTarget] = useState<string>("none");

  const clearMatError = (f: keyof MatErrors) =>
    setMatErrors((prev) => { const e = { ...prev }; delete e[f]; return e; });

  const load = async () => {
    try {
      const [m, g] = await Promise.all([materialsApi.getAll(), materialGroupsApi.getAll()]);
      setMaterials(m);
      setGroups(g);
    } catch {
      toast.error("Ошибка загрузки");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const filteredMaterials = materials.filter((m) => {
    const matchSearch = !search || m.name.toLowerCase().includes(search.toLowerCase());
    const matchGroup = filterGroupId === "all" || String(m.groupId) === filterGroupId;
    return matchSearch && matchGroup;
  });

  // Material CRUD
  const openMatCreate = () => {
    setMatName(""); setMatPrice(""); setMatUnits(""); setMatGroupId("none"); setMatErrors({});
    setMatDialog({ open: true });
  };
  const openMatEdit = (m: MaterialDto) => {
    setMatName(m.name); setMatPrice(String(m.price)); setMatUnits(m.units);
    setMatGroupId(m.groupId != null ? String(m.groupId) : "none");
    setMatErrors({});
    setMatDialog({ open: true, item: m });
  };
  const saveMaterial = async () => {
    const errs: MatErrors = {};
    if (!matName.trim()) errs.name = "Введите название";
    const priceNum = parseFloat(matPrice);
    if (!matPrice || isNaN(priceNum) || priceNum < 0) errs.price = "Введите корректную цену (≥ 0)";
    if (!matUnits.trim()) errs.units = "Введите единицу измерения";
    if (Object.keys(errs).length) { setMatErrors(errs); return; }

    setMatSaving(true);
    try {
      const data = {
        name: matName.trim(),
        price: priceNum,
        units: matUnits.trim(),
        groupId: matGroupId !== "none" ? parseInt(matGroupId) : null,
      };
      if (matDialog.item) {
        await materialsApi.update(matDialog.item.id, data);
        toast.success("Материал обновлён");
      } else {
        await materialsApi.create(data);
        toast.success("Материал создан");
      }
      setMatDialog({ open: false });
      await load();
    } catch (e) {
      toast.error(String(e));
    } finally {
      setMatSaving(false);
    }
  };
  const deleteMaterial = async () => {
    if (!deleteMatId) return;
    try {
      await materialsApi.delete(deleteMatId);
      toast.success("Материал удалён");
      setDeleteMatId(null);
      await load();
    } catch (e) {
      toast.error(String(e));
    }
  };

  // Group CRUD
  const openGrpCreate = () => { setGrpName(""); setGrpNameError(""); setGrpDialog({ open: true }); };
  const openGrpEdit = (g: MaterialGroupDto) => { setGrpName(g.name); setGrpNameError(""); setGrpDialog({ open: true, item: g }); };
  const saveGroup = async () => {
    if (!grpName.trim()) { setGrpNameError("Введите название группы"); return; }
    setGrpSaving(true);
    try {
      if (grpDialog.item) {
        await materialGroupsApi.update(grpDialog.item.id, { name: grpName.trim() });
        toast.success("Группа обновлена");
      } else {
        await materialGroupsApi.create({ name: grpName.trim() });
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
  const deleteGroup = async (strategy: "CASCADE" | "DEFAULT" | "MOVE") => {
    if (!deleteGrpId) return;
    const targetId = deleteGrpTarget !== "none" ? parseInt(deleteGrpTarget) : undefined;
    const resolvedStrategy = strategy === "MOVE" && targetId === undefined ? "DEFAULT" : strategy;
    try {
      await materialGroupsApi.delete(deleteGrpId, resolvedStrategy, targetId);
      toast.success("Группа удалена");
      setDeleteGrpId(null);
      await load();
    } catch (e) {
      toast.error(String(e));
    }
  };

  if (loading) return <div className="p-6 text-muted-foreground">Загрузка...</div>;

  return (
    <div className="p-6 space-y-4">
      <h1 className="text-2xl font-semibold">Материалы</h1>

      <Tabs defaultValue="materials">
        <TabsList>
          <TabsTrigger value="materials">Материалы</TabsTrigger>
          <TabsTrigger value="groups">Группы</TabsTrigger>
        </TabsList>

        {/* Materials tab */}
        <TabsContent value="materials" className="space-y-4">
          <div className="flex gap-2">
            <Input
              placeholder="Поиск по названию..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              className="max-w-xs"
            />
            <Select value={filterGroupId} onValueChange={setFilterGroupId}>
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
            {can("materials.create") && (
              <Button onClick={openMatCreate} className="ml-auto">
                <Plus className="h-4 w-4" /> Добавить
              </Button>
            )}
          </div>
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Название</TableHead>
                  <TableHead>Цена</TableHead>
                  <TableHead>Ед. изм.</TableHead>
                  <TableHead>Группа</TableHead>
                  <TableHead className="w-24" />
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredMaterials.length === 0 && (
                  <TableRow><TableCell colSpan={5} className="text-center text-muted-foreground">Нет данных</TableCell></TableRow>
                )}
                {filteredMaterials.map((m) => (
                  <TableRow key={m.id}>
                    <TableCell>{m.name}</TableCell>
                    <TableCell>{m.price.toLocaleString("ru-RU", { minimumFractionDigits: 2 })}</TableCell>
                    <TableCell>{m.units}</TableCell>
                    <TableCell>
                      {m.groupName ? <Badge variant="secondary">{m.groupName}</Badge> : <span className="text-muted-foreground">—</span>}
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-1 justify-end">
                        {can("materials.edit") && (
                          <Button variant="ghost" size="icon" onClick={() => openMatEdit(m)}>
                            <Pencil className="h-4 w-4" />
                          </Button>
                        )}
                        {can("materials.delete") && (
                          <Button variant="ghost" size="icon" onClick={() => setDeleteMatId(m.id)}>
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

        {/* Groups tab */}
        <TabsContent value="groups" className="space-y-4">
          {can("materials.create") && (
            <Button onClick={openGrpCreate}>
              <FolderPlus className="h-4 w-4" /> Добавить группу
            </Button>
          )}
          <div className="rounded-md border">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Название</TableHead>
                  <TableHead>Кол-во материалов</TableHead>
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
                    <TableCell>{materials.filter((m) => m.groupId === g.id).length}</TableCell>
                    <TableCell>
                      <div className="flex gap-1 justify-end">
                        {can("materials.edit") && (
                          <Button variant="ghost" size="icon" onClick={() => openGrpEdit(g)}>
                            <FolderPen className="h-4 w-4" />
                          </Button>
                        )}
                        {can("materials.delete") && (
                          <Button variant="ghost" size="icon" onClick={() => { setDeleteGrpId(g.id); setDeleteGrpTarget("none"); }}>
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

      {/* Material dialog */}
      <Dialog open={matDialog.open} onOpenChange={(open) => setMatDialog({ open })}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{matDialog.item ? "Редактировать материал" : "Новый материал"}</DialogTitle>
          </DialogHeader>
          <div className="space-y-3">
            <div className="space-y-1">
              <Label>Название <span className="text-destructive">*</span></Label>
              <Input
                value={matName}
                onChange={(e) => { setMatName(e.target.value); clearMatError("name"); }}
                className={matErrors.name ? "border-destructive" : ""}
              />
              {matErrors.name && <p className="text-xs text-destructive">{matErrors.name}</p>}
            </div>
            <div className="space-y-1">
              <Label>Цена <span className="text-destructive">*</span></Label>
              <Input
                type="number"
                min="0"
                step="0.01"
                value={matPrice}
                onChange={(e) => { setMatPrice(e.target.value); clearMatError("price"); }}
                className={matErrors.price ? "border-destructive" : ""}
              />
              {matErrors.price && <p className="text-xs text-destructive">{matErrors.price}</p>}
            </div>
            <div className="space-y-1">
              <Label>Единица измерения <span className="text-destructive">*</span></Label>
              <Input
                value={matUnits}
                onChange={(e) => { setMatUnits(e.target.value); clearMatError("units"); }}
                placeholder="кг, м², шт..."
                className={matErrors.units ? "border-destructive" : ""}
              />
              {matErrors.units && <p className="text-xs text-destructive">{matErrors.units}</p>}
            </div>
            <div className="space-y-1">
              <Label>Группа</Label>
              <Select value={matGroupId} onValueChange={setMatGroupId}>
                <SelectTrigger>
                  <SelectValue placeholder="Без группы" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="none">Без группы</SelectItem>
                  {groups.map((g) => (
                    <SelectItem key={g.id} value={String(g.id)}>{g.name}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setMatDialog({ open: false })}>Отмена</Button>
            <Button onClick={saveMaterial} disabled={matSaving}>
              {matSaving ? "Сохранение..." : "Сохранить"}
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
            <Button onClick={saveGroup} disabled={grpSaving}>
              {grpSaving ? "Сохранение..." : "Сохранить"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete material */}
      <AlertDialog open={deleteMatId !== null} onOpenChange={(open) => !open && setDeleteMatId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Удалить материал?</AlertDialogTitle>
            <AlertDialogDescription>Это действие нельзя отменить.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Отмена</AlertDialogCancel>
            <AlertDialogAction className="bg-destructive text-destructive-foreground hover:bg-destructive/90" onClick={deleteMaterial}>
              Удалить
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

      {/* Delete group */}
      <AlertDialog open={deleteGrpId !== null} onOpenChange={(open) => !open && setDeleteGrpId(null)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Удалить группу?</AlertDialogTitle>
            <AlertDialogDescription>Выберите, что сделать с материалами этой группы.</AlertDialogDescription>
          </AlertDialogHeader>
          <div className="space-y-3 py-2">
            <div className="flex flex-col gap-2">
              <Button variant="outline" className="justify-start" onClick={() => deleteGroup("CASCADE")}>
                Удалить все материалы группы
              </Button>
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">Или переместить материалы в другую группу:</p>
                <div className="flex gap-2">
                  <Select value={deleteGrpTarget} onValueChange={setDeleteGrpTarget}>
                    <SelectTrigger className="flex-1">
                      <SelectValue placeholder="Выберите группу" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="none">Без группы</SelectItem>
                      {groups.filter((g) => g.id !== deleteGrpId).map((g) => (
                        <SelectItem key={g.id} value={String(g.id)}>{g.name}</SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <Button onClick={() => deleteGroup("MOVE")}>Переместить</Button>
                </div>
              </div>
            </div>
          </div>
          <AlertDialogFooter>
            <AlertDialogCancel>Отмена</AlertDialogCancel>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  );
}
