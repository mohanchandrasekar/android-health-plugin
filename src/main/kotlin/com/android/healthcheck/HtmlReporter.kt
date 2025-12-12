package com.android.healthcheck

import java.io.File

class HtmlReporter {

    fun write(file: File, findings: List<Finding>) {
        file.printWriter().use { out ->
            out.println("<!DOCTYPE html>")
            out.println("<html><head><meta charset=\"UTF-8\" />")
            out.println("<title>Android Health Report</title>")
            out.println(
                """                    <style>
                body { font-family: system-ui, -apple-system, Roboto, sans-serif; margin: 16px; }
                h1 { margin-bottom: 4px; }
                .summary { margin-bottom: 16px; }
                table { border-collapse: collapse; width: 100%; }
                th, td { border: 1px solid #ddd; padding: 8px; font-size: 13px; }
                th { background: #f4f4f4; text-align: left; }
                .severity-Error { color: #b71c1c; font-weight: 600; }
                .severity-Warning { color: #e65100; font-weight: 600; }
                .severity-Info { color: #1565c0; font-weight: 600; }
                .badge { display: inline-block; padding: 2px 6px; border-radius: 4px; font-size: 11px; }
                .cat-Architecture { background:#e3f2fd; }
                .cat-Security { background:#ffebee; }
                .cat-Quality { background:#e8f5e9; }
                </style>
                """.trimIndent()
            )
            out.println("</head><body>")

            out.println("<h1>Android Health Report</h1>")
            out.println("<div class='summary'>Total issues: ${findings.size}</div>")

            if (findings.isEmpty()) {
                out.println("<p>✅ No issues found by v0.1 rules.</p>")
            } else {
                out.println("<table>")
                out.println("<thead><tr><th>Rule</th><th>Category</th><th>Severity</th><th>File</th><th>Message</th></tr></thead>")
                out.println("<tbody>")
                findings.forEach { f ->
                    out.println("<tr>")
                    out.println("<td>${escapeHtml(f.ruleId)}</td>")
                    out.println("<td><span class='badge cat-${escapeHtml(f.category)}'>${escapeHtml(f.category)}</span></td>")
                    out.println("<td class='severity-${escapeHtml(f.severity)}'>${escapeHtml(f.severity)}</td>")
                    out.println("<td>${escapeHtml(f.file)}</td>")
                    out.println("<td>${escapeHtml(f.message)}</td>")
                    out.println("</tr>")
                }
                out.println("</tbody></table>")
            }

            out.println("</body></html>")
        }
    }

    private fun escapeHtml(s: String): String =
        s.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
}
