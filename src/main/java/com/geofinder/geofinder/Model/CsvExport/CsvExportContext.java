package com.geofinder.geofinder.Model.CsvExport;

public class CsvExportContext {
    private CsvExportStrategy strategy;

    public void setStrategy(CsvExportStrategy strategy) {
        this.strategy = strategy;
    }

    public void executeExport() {
        if (strategy != null) {
            strategy.exportToCsv();
        }
    }
}