import { useState, useEffect, useRef } from "react";
import Editor from "@monaco-editor/react";
<<<<<<< Updated upstream
import { Play, BookOpen, ChevronDown, ChevronRight, Database } from "lucide-react";
import { toast } from "sonner";
import { sqlApi } from "../api/client";
import type { SqlResultDto, SqlSchemaDto, SqlTableInfo } from "../api/types";
import { Button } from "../components/ui/button";

=======
import { Play, BookOpen, Database, X } from "lucide-react";
import { toast } from "sonner";
import mermaid from "mermaid";
import { sqlApi } from "../api/client";
import type { SqlResultDto, SqlSchemaDto, SqlForeignKey } from "../api/types";
import { Button } from "../components/ui/button";

mermaid.initialize({
  startOnLoad: false,
  theme: "default",
  er: {
    diagramPadding: 50,
    layoutDirection: "LR",
    minEntityWidth: 160,
    entityPadding: 20,
    useMaxWidth: false,
  },
  fontSize: 16,
});

function buildErDiagram(schema: SqlSchemaDto): string {
  const lines: string[] = ["erDiagram"];

  for (const table of schema.tables) {
    lines.push(`  ${table.name} {`);
    for (const col of table.columns) {
      const safeType = col.type.replace(/\s+/g, "_");
      const nullable = col.nullable ? "nullable" : "required";
      lines.push(`    ${safeType} ${col.name} "${nullable}"`);
    }
    lines.push("  }");
  }

  const seen = new Set<string>();
  for (const fk of schema.foreignKeys) {
    const key = `${fk.fromTable}-${fk.toTable}`;
    if (!seen.has(key)) {
      seen.add(key);
      lines.push(`  ${fk.fromTable} }o--|| ${fk.toTable} : "${fk.fromColumn}"`);
    }
  }

  return lines.join("\n");
}

function SchemaModal({ schema, onClose }: { schema: SqlSchemaDto; onClose: () => void }) {
  const viewportRef = useRef<HTMLDivElement>(null);
  const [svg, setSvg] = useState<string>("");
  const [error, setError] = useState<string>("");

  // Pan + zoom state stored in refs to avoid re-renders on every mouse move
  const transform = useRef({ scale: 0.9, tx: 40, ty: 40 });
  const drag = useRef<{ active: boolean; startX: number; startY: number; startTx: number; startTy: number }>({
    active: false, startX: 0, startY: 0, startTx: 0, startTy: 0,
  });

  const applyTransform = () => {
    const canvas = viewportRef.current?.querySelector<HTMLDivElement>("#schema-canvas");
    if (canvas) {
      const { scale, tx, ty } = transform.current;
      canvas.style.transform = `translate(${tx}px, ${ty}px) scale(${scale})`;
    }
  };

  useEffect(() => {
    const diagram = buildErDiagram(schema);
    mermaid.render("er-diagram", diagram)
      .then(({ svg: rendered }) => {
        const parser = new DOMParser();
        const doc = parser.parseFromString(rendered, "image/svg+xml");
        const svgEl = doc.querySelector("svg");
        if (svgEl) {
          const vb = svgEl.getAttribute("viewBox");
          if (vb) {
            const parts = vb.split(/\s+/).map(Number);
            svgEl.setAttribute("width", String(Math.round((parts[2] ?? 0) * 2.5)));
            svgEl.setAttribute("height", String(Math.round((parts[3] ?? 0) * 2.5)));
          }
        }
        setSvg(new XMLSerializer().serializeToString(doc));
      })
      .catch((e) => setError(String(e)));
  }, [schema]);

  // Apply transform after SVG renders
  useEffect(() => { if (svg) applyTransform(); }, [svg]);

  useEffect(() => {
    const onKey = (e: KeyboardEvent) => { if (e.key === "Escape") onClose(); };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [onClose]);

  const onWheel = (e: React.WheelEvent) => {
    e.preventDefault();
    const rect = viewportRef.current!.getBoundingClientRect();
    const mouseX = e.clientX - rect.left;
    const mouseY = e.clientY - rect.top;
    const factor = e.deltaY < 0 ? 1.12 : 1 / 1.12;
    const { scale, tx, ty } = transform.current;
    const newScale = Math.min(5, Math.max(0.15, scale * factor));
    transform.current = {
      scale: newScale,
      tx: mouseX - (mouseX - tx) * (newScale / scale),
      ty: mouseY - (mouseY - ty) * (newScale / scale),
    };
    applyTransform();
  };

  const onMouseDown = (e: React.MouseEvent) => {
    drag.current = { active: true, startX: e.clientX, startY: e.clientY,
      startTx: transform.current.tx, startTy: transform.current.ty };
    viewportRef.current!.style.cursor = "grabbing";
  };

  const onMouseMove = (e: React.MouseEvent) => {
    if (!drag.current.active) return;
    transform.current.tx = drag.current.startTx + (e.clientX - drag.current.startX);
    transform.current.ty = drag.current.startTy + (e.clientY - drag.current.startY);
    applyTransform();
  };

  const onMouseUp = () => {
    drag.current.active = false;
    if (viewportRef.current) viewportRef.current.style.cursor = "grab";
  };

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm"
      onClick={(e) => { if (e.target === e.currentTarget) onClose(); }}
    >
      <div className="relative flex flex-col bg-background rounded-xl shadow-2xl border"
        style={{ width: "90vw", height: "88vh" }}>

        {/* Header */}
        <div className="flex items-center gap-3 border-b px-4 py-3 shrink-0">
          <Database className="h-4 w-4 text-primary" />
          <span className="font-semibold text-sm">Схема базы данных</span>
          <span className="text-xs text-muted-foreground">
            {schema.tables.length} таблиц · {schema.foreignKeys.length} связей
          </span>
          <span className="text-xs text-muted-foreground ml-2 opacity-60">
            Колёсико — зум · Перетащить — переместить
          </span>
          <Button variant="ghost" size="icon" className="h-7 w-7 ml-auto" onClick={onClose}>
            <X className="h-4 w-4" />
          </Button>
        </div>

        {/* Viewport */}
        <div
          ref={viewportRef}
          className="flex-1 overflow-hidden bg-muted/30"
          style={{ cursor: "grab", position: "relative" }}
          onWheel={onWheel}
          onMouseDown={onMouseDown}
          onMouseMove={onMouseMove}
          onMouseUp={onMouseUp}
          onMouseLeave={onMouseUp}
        >
          {error && <p className="text-sm text-destructive p-8">{error}</p>}
          {!error && !svg && (
            <p className="text-sm text-muted-foreground p-8">Генерация диаграммы...</p>
          )}
          {svg && (
            <div
              id="schema-canvas"
              style={{ position: "absolute", transformOrigin: "0 0", userSelect: "none" }}
              dangerouslySetInnerHTML={{ __html: svg }}
            />
          )}
        </div>
      </div>
    </div>
  );
}

>>>>>>> Stashed changes
export function SqlTerminalPage() {
  const [query, setQuery] = useState("SELECT * FROM \"user\" LIMIT 10;");
  const [result, setResult] = useState<SqlResultDto | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [schema, setSchema] = useState<SqlSchemaDto | null>(null);
<<<<<<< Updated upstream
  const [schemaLoading, setSchemaLoading] = useState(true);
  const [expandedTables, setExpandedTables] = useState<Set<string>>(new Set());
=======
  const [schemaOpen, setSchemaOpen] = useState(false);
>>>>>>> Stashed changes
  const queryRef = useRef(query);
  queryRef.current = query;

  useEffect(() => {
    sqlApi.schema()
      .then(setSchema)
<<<<<<< Updated upstream
      .catch(() => toast.error("Не удалось загрузить схему БД"))
      .finally(() => setSchemaLoading(false));
=======
      .catch(() => toast.error("Не удалось загрузить схему БД"));
>>>>>>> Stashed changes
  }, []);

  const handleExecute = async () => {
    const q = queryRef.current.trim();
    if (!q) return;
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const data = await sqlApi.execute(q);
      setResult(data);
    } catch (err: unknown) {
      const raw = err instanceof Error ? err.message : String(err);
      let message = raw;
      try {
        const parsed = JSON.parse(raw);
        if (parsed?.error) message = parsed.error;
      } catch {
<<<<<<< Updated upstream
        // not JSON, use raw
=======
        // not JSON
>>>>>>> Stashed changes
      }
      setError(message);
    } finally {
      setLoading(false);
    }
  };

<<<<<<< Updated upstream
  const toggleTable = (name: string) => {
    setExpandedTables((prev) => {
      const next = new Set(prev);
      if (next.has(name)) next.delete(name);
      else next.add(name);
      return next;
    });
  };

  return (
    <div className="flex h-full">
      {/* Schema sidebar */}
      <aside className="flex w-60 flex-col border-r bg-card overflow-hidden shrink-0">
        <div className="flex items-center gap-2 border-b px-3 py-2.5">
          <Database className="h-4 w-4 text-muted-foreground" />
          <span className="text-sm font-medium">Схема БД</span>
        </div>
        <div className="flex-1 overflow-y-auto p-1.5">
          {schemaLoading && (
            <p className="px-2 py-2 text-xs text-muted-foreground">Загрузка...</p>
          )}
          {!schemaLoading && schema?.tables.map((table: SqlTableInfo) => (
            <div key={table.name}>
              <button
                onClick={() => toggleTable(table.name)}
                className="flex w-full items-center gap-1.5 rounded px-2 py-1.5 text-left text-xs font-medium hover:bg-accent hover:text-accent-foreground transition-colors"
              >
                {expandedTables.has(table.name)
                  ? <ChevronDown className="h-3 w-3 shrink-0" />
                  : <ChevronRight className="h-3 w-3 shrink-0" />}
                {table.name}
              </button>
              {expandedTables.has(table.name) && (
                <div className="ml-4 mb-1 border-l pl-2">
                  {table.columns.map((col) => (
                    <div key={col.name} className="flex items-baseline gap-1.5 py-0.5">
                      <span className="text-xs">{col.name}</span>
                      <span className="text-[10px] text-muted-foreground">{col.type}</span>
                      {col.nullable && (
                        <span className="text-[10px] text-muted-foreground/60">?</span>
                      )}
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      </aside>

      {/* Main area */}
      <div className="flex flex-1 flex-col overflow-hidden">
        {/* Toolbar */}
        <div className="flex items-center gap-2 border-b px-4 py-2 bg-card">
          <span className="text-sm font-semibold">SQL Терминал</span>
          <div className="ml-auto flex items-center gap-2">
            <a
              href="https://www.postgresql.org/docs/current/"
              target="_blank"
              rel="noopener noreferrer"
            >
              <Button variant="outline" size="sm" className="gap-1.5">
                <BookOpen className="h-3.5 w-3.5" />
                Документация PostgreSQL
              </Button>
            </a>
            <Button size="sm" className="gap-1.5" onClick={handleExecute} disabled={loading}>
              <Play className="h-3.5 w-3.5" />
              {loading ? "Выполняется..." : "Выполнить"}
            </Button>
          </div>
        </div>

        {/* Editor */}
        <div className="border-b" style={{ height: "220px" }}>
          <Editor
            height="100%"
            defaultLanguage="sql"
            value={query}
            onChange={(val) => setQuery(val ?? "")}
            theme="vs-dark"
            options={{
              minimap: { enabled: false },
              fontSize: 13,
              lineNumbers: "on",
              scrollBeyondLastLine: false,
              wordWrap: "on",
              padding: { top: 8 },
            }}
          />
        </div>

        {/* Results */}
        <div className="flex-1 overflow-auto p-4">
          {error && (
            <div className="rounded-md border border-destructive/50 bg-destructive/10 p-3 text-sm text-destructive font-mono whitespace-pre-wrap">
              {error}
            </div>
          )}
          {result && !error && (
            <div>
              <p className="mb-2 text-xs text-muted-foreground">
                Строк: {result.rowCount}
              </p>
              {result.columns.length === 0 ? (
                <p className="text-sm text-muted-foreground">Запрос выполнен, данных нет.</p>
              ) : (
                <div className="overflow-auto rounded-md border">
                  <table className="min-w-full text-xs">
                    <thead className="bg-muted/50 sticky top-0">
                      <tr>
                        {result.columns.map((col) => (
                          <th
                            key={col}
                            className="border-b px-3 py-2 text-left font-medium text-muted-foreground whitespace-nowrap"
                          >
                            {col}
                          </th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {result.rows.map((row, i) => (
                        <tr key={i} className="border-b last:border-0 hover:bg-muted/30">
                          {result.columns.map((col) => (
                            <td key={col} className="px-3 py-1.5 font-mono whitespace-nowrap">
                              {row[col] === null ? (
                                <span className="text-muted-foreground/50 italic">null</span>
                              ) : (
                                row[col]
                              )}
                            </td>
                          ))}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          )}
          {!result && !error && !loading && (
            <p className="text-sm text-muted-foreground">
              Напишите SELECT-запрос и нажмите «Выполнить».
            </p>
          )}
        </div>
      </div>
=======
  return (
    <div className="flex flex-col h-full overflow-hidden">
      {/* Toolbar */}
      <div className="flex items-center gap-2 border-b px-4 py-2 bg-card shrink-0">
        <span className="text-sm font-semibold">SQL Терминал</span>
        <div className="ml-auto flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            className="gap-1.5"
            onClick={() => schema ? setSchemaOpen(true) : toast.error("Схема ещё не загружена")}
          >
            <Database className="h-3.5 w-3.5" />
            Схема БД
          </Button>
          <a href="https://www.postgresql.org/docs/current/" target="_blank" rel="noopener noreferrer">
            <Button variant="outline" size="sm" className="gap-1.5">
              <BookOpen className="h-3.5 w-3.5" />
              Документация PostgreSQL
            </Button>
          </a>
          <Button size="sm" className="gap-1.5" onClick={handleExecute} disabled={loading}>
            <Play className="h-3.5 w-3.5" />
            {loading ? "Выполняется..." : "Выполнить"}
          </Button>
        </div>
      </div>

      {/* Editor */}
      <div className="border-b shrink-0" style={{ height: "220px" }}>
        <Editor
          height="100%"
          defaultLanguage="sql"
          value={query}
          onChange={(val) => setQuery(val ?? "")}
          theme="vs-dark"
          options={{
            minimap: { enabled: false },
            fontSize: 13,
            lineNumbers: "on",
            scrollBeyondLastLine: false,
            wordWrap: "on",
            padding: { top: 8 },
          }}
        />
      </div>

      {/* Results */}
      <div className="flex-1 overflow-auto p-4">
        {error && (
          <div className="rounded-md border border-destructive/50 bg-destructive/10 p-3 text-sm text-destructive font-mono whitespace-pre-wrap">
            {error}
          </div>
        )}
        {result && !error && (
          <div>
            <p className="mb-2 text-xs text-muted-foreground">Строк: {result.rowCount}</p>
            {result.columns.length === 0 ? (
              <p className="text-sm text-muted-foreground">Запрос выполнен, данных нет.</p>
            ) : (
              <div className="overflow-auto rounded-md border">
                <table className="min-w-full text-xs">
                  <thead className="bg-muted/50 sticky top-0">
                    <tr>
                      {result.columns.map((col) => (
                        <th key={col} className="border-b px-3 py-2 text-left font-medium text-muted-foreground whitespace-nowrap">
                          {col}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {result.rows.map((row, i) => (
                      <tr key={i} className="border-b last:border-0 hover:bg-muted/30">
                        {result.columns.map((col) => (
                          <td key={col} className="px-3 py-1.5 font-mono whitespace-nowrap">
                            {row[col] === null
                              ? <span className="text-muted-foreground/50 italic">null</span>
                              : row[col]}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}
        {!result && !error && !loading && (
          <p className="text-sm text-muted-foreground">Напишите SELECT-запрос и нажмите «Выполнить».</p>
        )}
      </div>

      {/* Schema modal */}
      {schemaOpen && schema && (
        <SchemaModal schema={schema} onClose={() => setSchemaOpen(false)} />
      )}
>>>>>>> Stashed changes
    </div>
  );
}
