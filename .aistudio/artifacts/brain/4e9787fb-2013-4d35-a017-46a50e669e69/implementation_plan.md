# Audit Reports, Camera QR/Barcode Scanner & Stock Distribution Charts

This plan introduces three major audit and operational enhancements to **KitabTrack**: automated PDF and CSV export generation for auditing, a camera-based QR and barcode scanner accessible across the application, and native Jetpack Compose interactive distribution charts on the dashboard.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during the interactive clarification turn and guide the architecture below:
> 
> - **Stock Distribution Charts**: Native Compose interactive bar and donut charts (built directly with Compose Canvas and interactive touch states, avoiding heavy webview overhead while delivering 60fps performance).
> - **Audit Export Formats**: Both formatted printable PDF reports (with official header, metadata, and tabular layout) and downloadable CSV exports (with UTF-8 BOM for immediate opening in Excel and Google Sheets).
> - **Scanner Placement**: Comprehensive integration across all primary touchpoints: global top bar icon, floating action button in catalog, and inline barcode scan buttons in Inbound and Outbound transaction forms.

---

## 1. Overview & Core Concept

1. **Auditing Report Generator (PDF & CSV)**:
   - **Current Inventory Summary**: Generates comprehensive snapshots of all textbooks, categorized by stage, grade, subject, current stock, minimum stock levels, unit value, and deficit/surplus status.
   - **Transaction Audit Log**: Generates chronological logs of all inbound shipments and school disbursements, including transaction identifiers, dates, recipient schools, authorized officers, and total quantities.
   - **Export Modes**: One-tap native Android PDF creation via `android.graphics.pdf.PdfDocument` and structured CSV export with instant preview, sharing, and saving intents.

2. **Camera QR & Barcode Scanner**:
   - **CameraX & Vision Engine**: Utilizes device camera with permission handling (`android.permission.CAMERA`), targeting reticle overlay, flashlight toggle, and automatic barcode / QR code parsing.
   - **Quick Action Invocations**:
     - *Top Bar*: Global lookup that instantly surfaces the matched textbook or opens the matching transaction receipt.
     - *Catalog & Dashboard FAB*: Rapid single-tap scanner.
     - *Inbound & Outbound Forms*: One-tap barcode scan that auto-selects the book and populates quantities.
     - *Interactive Simulator Fallback*: Provides quick-test sample barcodes so users on emulators or without camera permissions can immediately experience the scan flow.

3. **Dashboard Stock Distribution Charts**:
   - **Grade & Stage Bar Chart**: Visualizes inventory volume across Primary (Grades 1–6), Preparatory (Grades 7–9), and Secondary (Grades 10–12) with threshold indicators.
   - **Subject Distribution Donut Chart**: Proportional breakdown of inventory by subject (Arabic, Mathematics, Science, English, etc.) with touch-activated slices and legends.
   - **Toggle Controls**: Seamless switching between Grade Level and Subject views directly on the dashboard.

---

## 2. User Experience & Visual Design

### Key User Flows

```
[Dashboard View]
  ├── Metric Stats & Alerts
  ├── [Interactive Stock Chart] (Bar & Donut with View Toggle)
  └── Global Top Bar [📷 Scan QR/Barcode] ──► Camera Scanner Modal ──► Auto-loads Book/Voucher Detail

[Audit & Reports Screen]
  ├── [Export PDF] ──► Formatted Android Print/PDF Preview ──► Share / Save
  └── [Export CSV] ──► Spreadsheet Export ──► Open in Excel / Drive

[Inbound & Outbound Screens]
  └── Book Selection Row ──► [📷 Scan Barcode] ──► Auto-matches Book & Fills Line Item
```

### Visual Styling & Theme

- **Color Harmony**: Anchored on the established primary accent `#006F88` (Primary Teal), with `#0D9488` (Forest Green for Inbound/Surplus), `#D97706` (Amber for Caution), and `#DC2626` (Crimson for Deficits).
- **Chart Aesthetics**: Sleek rounded-corner bars with gradient fills, subtle gridlines, touch-highlight indicators with tooltip chips, and clean donut arcs with central total counter.
- **Scanner Viewport**: Semi-transparent dark viewfinder scrim with rounded glowing cyan corner brackets, animated scan line, and quick-toggle flashlight and manual barcode fallback.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Native Canvas Charts vs. External Charting Libraries**:
  - *Chosen Approach*: Lightweight, custom Jetpack Compose Canvas implementations for both the bar chart and donut chart.
  - *Why*: Zero third-party dependency bloat, instantaneous rendering matching the app's `#006F88` design tokens, and smooth Compose state animations.
  - *Alternatives Considered*: Recharts (web-only library requiring WebView), MPAndroidChart (legacy View-based system).

- **Decision 2: PDF Generation Strategy**:
  - *Chosen Approach*: Android framework `PdfDocument` with `Canvas` text and table rendering.
  - *Why*: Completely offline, zero external library overhead, generates standard `.pdf` files compatible with Android Print Manager and all PDF viewers.
  - *Alternatives Considered*: Cloud-based PDF services (fails offline-first mandate).

- **Decision 3: Camera Scanner with Simulator Fallback**:
  - *Chosen Approach*: CameraX preview integration with standard Android runtime permission flow, paired with an instant barcode simulator selector.
  - *Why*: Ensures the feature works seamlessly on physical devices with real cameras while remaining 100% testable inside browser streaming emulators.

---

## 4. Technical Architecture & Data Strategy

### System Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            KitabTrack UI Layer                              │
│                                                                             │
│  ┌───────────────────────┐  ┌───────────────────────┐  ┌─────────────────┐  │
│  │ DashboardScreen       │  │ AuditReportScreen     │  │ Inbound/Outbound│  │
│  │ ├─ Bar/Donut Charts   │  │ ├─ PDF Generator Modal│  │ ├─ Barcode Scan │  │
│  │ └─ Top Bar Scan Icon  │  │ └─ CSV Export Sheet   │  │ └─ Fast Select  │  │
│  └───────────┬───────────┘  └───────────┬───────────┘  └────────┬────────┘  │
└──────────────┼──────────────────────────┼───────────────────────┼───────────┘
               │                          │                       │
┌──────────────▼──────────────────────────▼───────────────────────▼───────────┐
│                              Services Layer                                 │
│                                                                             │
│  ┌───────────────────────┐  ┌───────────────────────┐  ┌─────────────────┐  │
│  │ StockChartRenderer    │  │ ReportExportService   │  │ CameraScanner   │  │
│  │ (Canvas Math, Arcs,   │  │ (PdfDocument builder, │  │ (CameraX /      │  │
│  │ Tooltips & Legends)   │  │  CSV UTF-8 Formatter) │  │  Barcode lookup)│  │
│  └───────────┬───────────┘  └───────────┬───────────┘  └────────┬────────┘  │
└──────────────┼──────────────────────────┼───────────────────────┼───────────┘
               │                          │                       │
┌──────────────▼──────────────────────────▼───────────────────────▼───────────┐
│                     Repository & Room Persistence Layer                     │
│                                                                             │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │ InventoryRepository (Books, Transactions, Stock Totals, Barcode DAO)  │  │
│  └──────────────────────────────────┬────────────────────────────────────┘  │
│                                     │                                       │
│  ┌──────────────────────────────────▼────────────────────────────────────┐  │
│  │ AppDatabase (SQLite / Room: books, transactions, transaction_items)  │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Component & Data Implementation Scope

1. **`ReportExportService.kt`**:
   - `generateInventoryPdf(context: Context, books: List<Book>, summary: DashboardStats): Uri`
   - `generateTransactionsPdf(context: Context, transactions: List<TransactionWithItems>): Uri`
   - `generateInventoryCsv(context: Context, books: List<Book>): Uri`
   - `generateTransactionsCsv(context: Context, transactions: List<TransactionWithItems>): Uri`

2. **`CameraBarcodeScannerDialog.kt`**:
   - CameraX integration with `PreviewView`, lifecycle binding, and scan target frame.
   - Quick-barcode simulator buttons for easy testing in cloud emulator.
   - Barcode match callback that connects directly to repository lookup.

3. **`StockDistributionCharts.kt`**:
   - `StockBarChart`: Stage and grade distribution with animated bars, baseline threshold lines, and value chips.
   - `StockDonutChart`: Interactive subject distribution chart with sweep angles, center summary, and legend.
   - `StockDistributionCard`: Master dashboard card with toggle tabs ("حسب المراحل والصفوف" / "حسب المواد الدراسية").

4. **Integration Wiring**:
   - Update `DashboardScreen.kt` with the interactive chart card and top bar scanner button.
   - Update `AuditReportScreen.kt` with PDF and CSV export action buttons.
   - Update `InboundScreen.kt` and `OutboundScreen.kt` with camera scan shortcuts.
