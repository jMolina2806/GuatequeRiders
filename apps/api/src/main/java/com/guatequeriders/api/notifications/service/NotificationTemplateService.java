package com.guatequeriders.api.notifications.service;

import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class NotificationTemplateService {

    public String render(String eyebrow,
                         String title,
                         String message,
                         List<String> details,
                         String actionLabel,
                         String actionUrl) {
        StringBuilder detailRows = new StringBuilder();
        for (String detail : details) {
            if (detail == null || detail.isBlank()) {
                continue;
            }
            detailRows.append("""
                    <div style="padding:10px 0;border-bottom:1px solid #223044;color:#b7c0cc;font-size:14px;">
                    """)
                    .append(HtmlUtils.htmlEscape(detail))
                    .append("</div>");
        }

        String action = "";
        if (actionLabel != null && !actionLabel.isBlank() && actionUrl != null && !actionUrl.isBlank()) {
            action = """
                    <div style="margin-top:28px;">
                      <a href="%s" style="display:inline-block;padding:12px 18px;border-radius:8px;background:#00f0ff;color:#071019;text-decoration:none;font-weight:800;">%s</a>
                    </div>
                    """.formatted(
                    HtmlUtils.htmlEscape(actionUrl),
                    HtmlUtils.htmlEscape(actionLabel)
            );
        }

        return """
                <!doctype html>
                <html lang="es">
                <body style="margin:0;padding:0;background:#070b12;font-family:Arial,sans-serif;color:#f0f0f0;">
                  <div style="max-width:620px;margin:0 auto;padding:32px 20px;">
                    <div style="padding:26px;border:1px solid #223044;border-radius:14px;background:#0d141f;">
                      <div style="margin-bottom:20px;font-size:18px;font-weight:900;letter-spacing:1px;color:#00f0ff;">
                        GUATEQUE RIDERS
                      </div>
                      <div style="font-size:11px;font-weight:800;letter-spacing:1px;text-transform:uppercase;color:#ff00e5;">
                        %s
                      </div>
                      <h1 style="margin:10px 0 14px;font-size:28px;line-height:1.15;color:#ffffff;">%s</h1>
                      <p style="margin:0 0 18px;color:#b7c0cc;font-size:15px;line-height:1.7;">%s</p>
                      <div style="margin-top:18px;border-top:1px solid #223044;">%s</div>
                      %s
                    </div>
                    <p style="margin:18px 0 0;color:#66758a;font-size:11px;line-height:1.6;text-align:center;">
                      Este correo ha sido enviado automáticamente por Guateque Riders.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(
                HtmlUtils.htmlEscape(eyebrow),
                HtmlUtils.htmlEscape(title),
                HtmlUtils.htmlEscape(message),
                detailRows,
                action
        );
    }
}
