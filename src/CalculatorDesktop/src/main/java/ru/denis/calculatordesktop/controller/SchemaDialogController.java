package ru.denis.calculatordesktop.controller;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.web.WebView;
import ru.denis.calculatordesktop.api.dto.SqlSchemaDto;
import ru.denis.calculatordesktop.util.SqlDiagramBuilder;

public class SchemaDialogController {

    @FXML private WebView webView;

    public void init(SqlSchemaDto schema) {
        String diagram = SqlDiagramBuilder.buildErDiagram(schema);
        String html = buildHtml(diagram);
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, state) -> {
            if (state == Worker.State.FAILED) {
                webView.getEngine().loadContent("<body>Ошибка загрузки диаграммы</body>");
            }
        });
        webView.getEngine().loadContent(html, "text/html");
    }

    private String buildHtml(String diagram) {
        String escaped = SqlDiagramBuilder.escapeForHtml(diagram);
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "<head>\n"
                + "  <meta charset='UTF-8'>\n"
                + "  <style>\n"
                + "    * { margin:0; padding:0; box-sizing:border-box; }\n"
                + "    html, body { width:100%; height:100%; overflow:hidden;"
                + "                 background:#f0f2f5; }\n"
                + "    #viewport { width:100%; height:100%; overflow:hidden; }\n"
                + "    #canvas { position:absolute; transform-origin:0 0; padding:40px; }\n"
                + "    .mermaid svg { max-width:none !important; display:block; }\n"
                + "    #hint { position:fixed; bottom:12px; right:16px;\n"
                + "            font:11px/1.4 sans-serif; color:#9ca3af;\n"
                + "            pointer-events:none; }\n"
                + "  </style>\n"
                + "</head>\n"
                + "<body>\n"
                + "  <div id='viewport'>\n"
                + "    <div id='canvas'>\n"
                + "      <pre class='mermaid'>" + escaped + "</pre>\n"
                + "    </div>\n"
                + "  </div>\n"
                + "  <div id='hint'>Колёсико — зум &nbsp;·&nbsp; Перетащить — переместить</div>\n"
                + "  <script src='https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js'></script>\n"
                + "  <script>\n"
                + "    mermaid.initialize({\n"
                + "      startOnLoad: true,\n"
                + "      theme: 'default',\n"
                + "      er: { layoutDirection: 'LR', minEntityWidth: 160, entityPadding: 20,\n"
                + "            diagramPadding: 40, useMaxWidth: false },\n"
                + "      fontSize: 16\n"
                + "    });\n"
                + "\n"
                + "    var vp = document.getElementById('viewport');\n"
                + "    var cv = document.getElementById('canvas');\n"
                + "    var scale = 1, tx = 40, ty = 40;\n"
                + "    var dragging = false, sx = 0, sy = 0, stx = 0, sty = 0;\n"
                + "\n"
                + "    function apply() {\n"
                + "      cv.style.transform = 'translate(' + tx + 'px,' + ty + 'px) scale(' + scale + ')';\n"
                + "    }\n"
                + "    apply();\n"
                + "\n"
                + "    vp.addEventListener('wheel', function(e) {\n"
                + "      e.preventDefault();\n"
                + "      var r = vp.getBoundingClientRect();\n"
                + "      var mx = e.clientX - r.left, my = e.clientY - r.top;\n"
                + "      var f = e.deltaY < 0 ? 1.12 : 1/1.12;\n"
                + "      var ns = Math.min(5, Math.max(0.15, scale * f));\n"
                + "      tx = mx - (mx - tx) * (ns / scale);\n"
                + "      ty = my - (my - ty) * (ns / scale);\n"
                + "      scale = ns;\n"
                + "      apply();\n"
                + "    });\n"
                + "\n"
                + "    vp.addEventListener('mousedown', function(e) {\n"
                + "      dragging = true; sx = e.clientX; sy = e.clientY; stx = tx; sty = ty;\n"
                + "      vp.style.cursor = 'grabbing';\n"
                + "    });\n"
                + "    document.addEventListener('mousemove', function(e) {\n"
                + "      if (!dragging) return;\n"
                + "      tx = stx + (e.clientX - sx); ty = sty + (e.clientY - sy);\n"
                + "      apply();\n"
                + "    });\n"
                + "    document.addEventListener('mouseup', function() {\n"
                + "      dragging = false; vp.style.cursor = 'grab';\n"
                + "    });\n"
                + "    vp.style.cursor = 'grab';\n"
                + "  </script>\n"
                + "</body>\n"
                + "</html>\n";
    }
}
