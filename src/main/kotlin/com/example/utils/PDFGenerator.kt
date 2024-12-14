package com.example.utils

import com.example.models.InvoiceDetails
import com.example.models.Order
import com.example.models.RestaurantDetails
import com.example.models.TableSessionInvoice

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.draw.ILineDrawer
import com.itextpdf.layout.Document
import com.itextpdf.layout.Style
import com.itextpdf.layout.element.*
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PDFGenerator {

    fun generateInvoice(invoice: InvoiceDetails): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        try {
            // Add Restaurant Header
            addRestaurantHeader(document, invoice.restaurantDetails)

            // Add Invoice Details
            addInvoiceDetails(document, invoice)

            // Add Items Table
            addItemsTable(document, invoice)

            // Add Totals
            addTotals(document, invoice)

            // Add Footer
            addFooter(document, invoice.restaurantDetails)

            document.close()
            return outputStream.toByteArray()
        } catch (e: Exception) {
            document.close()
            throw e
        }
    }

    fun generateTableSessionInvoice(invoice: TableSessionInvoice): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val writer = PdfWriter(outputStream)
        val pdf = PdfDocument(writer)
        val document = Document(pdf)

        try {
            // Add Restaurant Header
            addRestaurantHeader(document, invoice.restaurantDetails)

            // Add Table Session Details
            addTableSessionDetails(document, invoice)

            // Add Orders Section
            addOrdersSection(document, invoice.orders)

            // Add Totals
            addTotals(document, invoice)

            // Add Footer
            addFooter(document, invoice.restaurantDetails)

            document.close()
            return outputStream.toByteArray()
        } catch (e: Exception) {
            document.close()
            throw e
        }
    }

    private fun addRestaurantHeader(document: Document, details: RestaurantDetails) {
        val header = Paragraph()
            .add(Text(details.name)
                .setFontSize(20f)
            )
            .setTextAlignment(TextAlignment.CENTER)

        val contact = Paragraph()
            .add(Text(details.address + "\n"))
            .add(Text("Phone: ${details.phone}\n"))
            .add(Text("Email: ${details.email}\n"))
            .add(Text("GSTIN: ${details.taxNumber}"))
            .setTextAlignment(TextAlignment.CENTER)
            .setFontSize(10f)

        document.add(header)
        document.add(contact)
//        document.add(LineSeparator())
    }


    private fun addInvoiceDetails(document: Document, invoice: InvoiceDetails) {
        val details = Table(UnitValue.createPercentArray(2)).useAllAvailableWidth()

        details.addCell(Cell().add(Paragraph("Invoice Number:")).setBorder(null))
        details.addCell(Cell().add(Paragraph("INV-${invoice.orderId}")).setBorder(null))

        details.addCell(Cell().add(Paragraph("Date:")).setBorder(null))
        details.addCell(Cell().add(Paragraph(invoice.orderDate)).setBorder(null))

        details.addCell(Cell().add(Paragraph("Table Number:")).setBorder(null))
        details.addCell(Cell().add(Paragraph(invoice.tableNumber.toString())).setBorder(null))

        if (invoice.customerName != null) {
            details.addCell(Cell().add(Paragraph("Customer:")).setBorder(null))
            details.addCell(Cell().add(Paragraph(invoice.customerName)).setBorder(null))
        }

        document.add(details)
    }

    private fun addItemsTable(document: Document, invoice: InvoiceDetails) {
        val itemsTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 20f, 20f, 20f)))
            .useAllAvailableWidth()
            .setMarginTop(20f)

        // Add Header
        itemsTable.addHeaderCell(Cell().add(Paragraph("Item")))
        itemsTable.addHeaderCell(Cell().add(Paragraph("Quantity")))
        itemsTable.addHeaderCell(Cell().add(Paragraph("Price")))
        itemsTable.addHeaderCell(Cell().add(Paragraph("Total")))

        // Add Items
        invoice.items.forEach { item ->
            itemsTable.addCell(Cell().add(Paragraph(item.menuItemName)))
            itemsTable.addCell(Cell().add(Paragraph(item.quantity.toString())))
            itemsTable.addCell(Cell().add(Paragraph("$${item.itemPrice}")))
            itemsTable.addCell(Cell().add(Paragraph("$${item.quantity * item.itemPrice}")))
        }

        document.add(itemsTable)
    }

    private fun addTotals(document: Document, invoice: InvoiceDetails) {
        val totalsTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
            .useAllAvailableWidth()
            .setMarginTop(20f)

        totalsTable.addCell(Cell().add(Paragraph("Subtotal:").setTextAlignment(TextAlignment.RIGHT)).setBorder(null))
        totalsTable.addCell(Cell().add(Paragraph("$${invoice.subtotal}")).setBorder(null))

        totalsTable.addCell(Cell().add(Paragraph("Tax:").setTextAlignment(TextAlignment.RIGHT)).setBorder(null))
        totalsTable.addCell(Cell().add(Paragraph("$${invoice.tax}")).setBorder(null))

        totalsTable.addCell(Cell().add(Paragraph("Total:").setTextAlignment(TextAlignment.RIGHT)))
        totalsTable.addCell(Cell().add(Paragraph("$${invoice.total}")))

        if (invoice.paymentMethod != null) {
            totalsTable.addCell(Cell().add(Paragraph("Payment Method:").setTextAlignment(TextAlignment.RIGHT)).setBorder(null))
            totalsTable.addCell(Cell().add(Paragraph(invoice.paymentMethod.toString())).setBorder(null))
        }

        document.add(totalsTable)
    }

    private fun addTotals(document: Document, invoice: TableSessionInvoice) {
        val totalsTable = Table(UnitValue.createPercentArray(floatArrayOf(70f, 30f)))
            .useAllAvailableWidth()
            .setMarginTop(20f)

        // Add session totals
        totalsTable.addCell(Cell().add(Paragraph("Subtotal:").setTextAlignment(TextAlignment.RIGHT)).setBorder(null))
        totalsTable.addCell(Cell().add(Paragraph("₹${invoice.subtotal}")).setBorder(null))

        totalsTable.addCell(Cell().add(Paragraph("Tax:").setTextAlignment(TextAlignment.RIGHT)).setBorder(null))
        totalsTable.addCell(Cell().add(Paragraph("₹${invoice.tax}")).setBorder(null))

        totalsTable.addCell(Cell().add(Paragraph("Total:").setTextAlignment(TextAlignment.RIGHT)))
        totalsTable.addCell(Cell().add(Paragraph("₹${invoice.total}")))

        if (invoice.paymentMethod != null) {
            totalsTable.addCell(Cell().add(Paragraph("Payment Method:").setTextAlignment(TextAlignment.RIGHT)).setBorder(null))
            totalsTable.addCell(Cell().add(Paragraph(invoice.paymentMethod.toString())).setBorder(null))
        }

        document.add(totalsTable)
    }

    private fun addFooter(document: Document, details: RestaurantDetails) {

        document.add(Paragraph("\n"))
//        document.add(LineSeparator())
        document.add(
            Paragraph(details.footer)
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10f)
                .setMarginTop(20f)
        )
        
        // Add terms and conditions
        document.add(Paragraph("\nTerms & Conditions:")
            .setFontSize(8f)
            )
        document.add(Paragraph("""
            1. All prices are inclusive of GST
            2. Bill amount once paid is non-refundable
            3. Please check your bill before payment
        """.trimIndent())
            .setFontSize(8f))
    }

    private fun addTableSessionDetails(document: Document, invoice: TableSessionInvoice) {
        val details = Table(UnitValue.createPercentArray(2)).useAllAvailableWidth()

        details.addCell(Cell().add(Paragraph("Bill No:")).setBorder(null))
        details.addCell(Cell().add(Paragraph("BILL-${invoice.tableId}-${invoice.sessionId}")).setBorder(null))

        details.addCell(Cell().add(Paragraph("Table Number:")).setBorder(null))
        details.addCell(Cell().add(Paragraph(invoice.tableNumber.toString())).setBorder(null))

        details.addCell(Cell().add(Paragraph("Date:")).setBorder(null))
        details.addCell(Cell().add(Paragraph(invoice.startTime)).setBorder(null))

        document.add(details)
    }

    private fun addOrdersSection(document: Document, orders: List<Order>) {
        // Combine all items from all orders and group by menuItemName
        val consolidatedItems = orders.flatMap { order -> 
            order.items.map { item ->
                ConsolidatedItem(
                    menuItemName = item.menuItemName ?: "",
                    quantity = item.quantity,
                    itemPrice = item.itemPrice
                )
            }
        }.groupBy { it.menuItemName }
         .map { (name, items) ->
             ConsolidatedItem(
                 menuItemName = name,
                 quantity = items.sumOf { it.quantity },
                 itemPrice = items.first().itemPrice
             )
         }

        // Create single items table
        val itemsTable = Table(UnitValue.createPercentArray(floatArrayOf(40f, 20f, 20f, 20f)))
            .useAllAvailableWidth()
            .setMarginTop(10f)

        // Add Header
        itemsTable.addHeaderCell(Cell().add(Paragraph("Item")))
        itemsTable.addHeaderCell(Cell().add(Paragraph("Quantity")))
        itemsTable.addHeaderCell(Cell().add(Paragraph("Price")))
        itemsTable.addHeaderCell(Cell().add(Paragraph("Total")))

        // Add consolidated items
        consolidatedItems.forEach { item ->
            itemsTable.addCell(Cell().add(Paragraph(item.menuItemName)))
            itemsTable.addCell(Cell().add(Paragraph(item.quantity.toString())))
            itemsTable.addCell(Cell().add(Paragraph("₹${item.itemPrice}")))
            itemsTable.addCell(Cell().add(Paragraph("₹${item.quantity * item.itemPrice}")))
        }

        document.add(itemsTable)
    }

    private data class ConsolidatedItem(
        val menuItemName: String,
        val quantity: Int,
        val itemPrice: Double
    )
}