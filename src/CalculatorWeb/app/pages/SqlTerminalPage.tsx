import { useState, useEffect, useRef } from "react";
import Editor from "@monaco-editor/react";
import { Play, BookOpen, ChevronDown, ChevronRight, Database } from "lucide-react";
import { toast } from "sonner";
import { sqlApi } from "../api/client";
import type { SqlResultDto, SqlSchemaDto, SqlTableInfo } from "../api/types";
import { Button } from "../components/ui/button";

export function SqlTerminalPage() {
  const [query, setQuery] = useState("SELECT * FROM \"user\" LIMIT 10;");
  const [result, setResult] = useState<SqlResultDto | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [schema, setSchema] = useState<SqlSchemaDto | null>(null);
  const [schemaLoading, setSchemaLoading] = useState(true);
  const [expandedTables, setExpandedTables] = useState<Set<string>>(new Set());
  const queryRef = useRef(query);
  queryRef.current = query;

  useEffect(() => {
    sqlApi.schema()
      .then(setSchema)
      .catch(() => toast.error("Не удалось загрузить схему БД"))
      .finally(() => setSchemaLoading(false));
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
        // not JSON, use raw
      }
      setError(message);
    } finally {
      setLoading(false);
    }
  };

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
    </div>
  );
}
