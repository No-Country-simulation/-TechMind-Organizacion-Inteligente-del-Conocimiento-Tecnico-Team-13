package com.application.service;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DocumentExtractor {

    public record DocumentData(String title, String content) {}

    public static DocumentData extractFromBytes(byte[] bytes, String originalFilename) {
        String filename = originalFilename != null ? originalFilename : "documento";
        String lower = filename.toLowerCase();
        if (lower.endsWith(".md") || lower.endsWith(".markdown")) {
            // Treat as markdown using UTF-8
            String md = new String(bytes, StandardCharsets.UTF_8);
            return extractFromMarkdown(md, filename);
        }

        // Fallback: use Apache Tika for PDFs, DOCX and many other formats
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            BodyContentHandler handler = new BodyContentHandler(-1); // unlimited
            Metadata metadata = new Metadata();
            AutoDetectParser parser = new AutoDetectParser();
            ParseContext context = new ParseContext();

            parser.parse(is, handler, metadata, context);

            String title = metadata.get(TikaCoreProperties.TITLE);
            if (title == null || title.isBlank()) {
                title = filename.replaceAll("\\.[^.]+$", "");
            }
            String content = handler.toString().trim();
            return new DocumentData(title, content);
        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el archivo con Tika: " + e.getMessage(), e);
        }
    }

    public static DocumentData extractFromMarkdown(String markdownContent, String originalFilename) {
        String title = originalFilename != null ? originalFilename.replaceAll("\\.[^.]+$", "") : "documento";
        var matcher = java.util.regex.Pattern.compile("(?m)^#\\s+(.*)$").matcher(markdownContent);
        if (matcher.find()) {
            title = matcher.group(1).trim();
        }

        MutableDataSet options = new MutableDataSet();
        Parser parser = Parser.builder(options).build();
        HtmlRenderer renderer = HtmlRenderer.builder(options).build();

        var document = parser.parse(markdownContent);
        String html = renderer.render(document);
        String cleanText = stripHtml(html);

        return new DocumentData(title, cleanText);
    }

    // Fallback lightweight HTML tag stripper to avoid depending on an external HTML parser at runtime
    private static String stripHtml(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        // Remove tags and collapse whitespace
        String noTags = html.replaceAll("(?s)<[^>]*>", " ");
        return noTags.replaceAll("\\s+", " ").trim();
    }
}
