package com.nemeles.inventario.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.nemeles.inventario.domain.Movimiento;
import com.nemeles.inventario.domain.Producto;
import com.nemeles.inventario.repo.MovimientoRepository;
import com.nemeles.inventario.repo.ProductoRepository;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Genera el comprobante PDF de un movimiento de stock, en el servidor,
 * a partir de la fuente de verdad (documento contable numerado).
 */
@Service
public class ComprobanteService {

    private static final Color TEAL = new Color(16, 163, 122);
    private static final Color INK = new Color(11, 21, 36);
    private static final Color MUTED = new Color(107, 122, 147);
    private static final Color LINE = new Color(227, 232, 240);
    private static final DateTimeFormatter FMT = DateTimeFormatter
            .ofPattern("dd 'de' MMMM 'de' yyyy, HH:mm", new Locale("es", "MX"))
            .withZone(ZoneId.systemDefault());

    private final MovimientoRepository movimientos;
    private final ProductoRepository productos;

    public ComprobanteService(MovimientoRepository movimientos, ProductoRepository productos) {
        this.movimientos = movimientos;
        this.productos = productos;
    }

    @Transactional(readOnly = true)
    public byte[] generar(Long movimientoId) {
        Movimiento m = movimientos.findById(movimientoId)
                .orElseThrow(() -> new NotFoundException("No existe el movimiento con id " + movimientoId));
        String nombreProducto = productos.findById(m.getProductoId())
                .map(Producto::getNombre).orElse(m.getSku());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 54, 54, 54, 54);
        try {
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Paragraph marca = new Paragraph("Stockly", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, TEAL));
            doc.add(marca);
            Paragraph sub = new Paragraph("Comprobante de movimiento de inventario",
                    FontFactory.getFont(FontFactory.HELVETICA, 10, MUTED));
            sub.setSpacingAfter(20);
            doc.add(sub);

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(100);
            tabla.setWidths(new float[]{1f, 2.2f});
            fila(tabla, "Folio", "#" + m.getId());
            fila(tabla, "Fecha y hora", FMT.format(m.getFecha()));
            fila(tabla, "Producto", nombreProducto + "  (" + m.getSku() + ")");
            fila(tabla, "Tipo", m.getTipo() == Movimiento.Tipo.ENTRADA ? "Entrada (+)" : "Salida (-)");
            fila(tabla, "Cantidad",
                    (m.getTipo() == Movimiento.Tipo.ENTRADA ? "+" : "-") + m.getCantidad() + " unidades");
            fila(tabla, "Stock resultante", m.getStockResultante() + " unidades");
            fila(tabla, "Motivo", (m.getMotivo() == null || m.getMotivo().isBlank()) ? "—" : m.getMotivo());
            doc.add(tabla);

            Paragraph pie = new Paragraph("Documento generado por Stockly el " + FMT.format(Instant.now()),
                    FontFactory.getFont(FontFactory.HELVETICA, 8, MUTED));
            pie.setSpacingBefore(26);
            doc.add(pie);
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el comprobante PDF", e);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
        return baos.toByteArray();
    }

    private void fila(PdfPTable tabla, String etiqueta, String valor) {
        Font fEtiqueta = FontFactory.getFont(FontFactory.HELVETICA, 9, MUTED);
        Font fValor = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, INK);
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta.toUpperCase(Locale.ROOT), fEtiqueta));
        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fValor));
        for (PdfPCell c : new PdfPCell[]{celdaEtiqueta, celdaValor}) {
            c.setBorder(Rectangle.BOTTOM);
            c.setBorderColor(LINE);
            c.setPadding(9f);
        }
        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
    }
}
