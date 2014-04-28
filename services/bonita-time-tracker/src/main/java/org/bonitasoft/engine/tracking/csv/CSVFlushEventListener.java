package org.bonitasoft.engine.tracking.csv;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import org.bonitasoft.engine.log.technical.TechnicalLogSeverity;
import org.bonitasoft.engine.log.technical.TechnicalLoggerService;
import org.bonitasoft.engine.tracking.FlushEvent;
import org.bonitasoft.engine.tracking.FlushEventListener;
import org.bonitasoft.engine.tracking.Record;

public class CSVFlushEventListener implements FlushEventListener {

    private final String csvSeparator;

    private final String outputFolder;

    private final TechnicalLoggerService logger;

    public CSVFlushEventListener(final String outputFolder, final String csvSeparator, final TechnicalLoggerService logger) {
        super();
        this.csvSeparator = csvSeparator;
        this.outputFolder = outputFolder;
        this.logger = logger;

        final File outputFolderFile = new File(outputFolder);
        if (!outputFolderFile.exists()) {
            throw new RuntimeException("Output folder does not exist: " + outputFolder);
        }
        if (!outputFolderFile.isDirectory()) {
            throw new RuntimeException("Output folder is not a directory: " + outputFolder);
        }
    }

    @Override
    public CSVFlushResult flush(final FlushEvent flushEvent) throws Exception {
        final List<Record> records = flushEvent.getRecords();
        final List<List<String>> csvContent = new ArrayList<List<String>>();
        csvContent.add(getHeaderRow());
        for (final Record record : records) {
            final List<String> row = getRow(record);
            csvContent.add(row);
        }
        final String timestamp = CSVUtil.getFileTimestamp(System.currentTimeMillis());

        final File outputFile = new File(outputFolder, timestamp + "_bonita_timetracker_" + UUID.randomUUID().toString() + ".csv");

        if (logger.isLoggable(getClass(), TechnicalLogSeverity.INFO)) {
            logger.log(getClass(), TechnicalLogSeverity.INFO, "Generating csv file to: " + outputFile);
        }
        CSVUtil.writeCSV(outputFile, csvContent, csvSeparator);
        return new CSVFlushResult(flushEvent, outputFile);
    }

    private List<String> getRow(final Record record) {
        final long timestamp = record.getTimestamp();
        final GregorianCalendar cal = new GregorianCalendar();
        cal.setTimeInMillis(timestamp);
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH) + 1;
        final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        final int hourOfDay = cal.get(Calendar.HOUR_OF_DAY);
        final int minute = cal.get(Calendar.MINUTE);
        final int second = cal.get(Calendar.SECOND);
        final int milisecond = cal.get(Calendar.MILLISECOND);

        final List<String> row = new ArrayList<String>();
        row.add(String.valueOf(timestamp));
        row.add(String.valueOf(year));
        row.add(String.valueOf(month));
        row.add(String.valueOf(dayOfMonth));
        row.add(String.valueOf(hourOfDay));
        row.add(String.valueOf(minute));
        row.add(String.valueOf(second));
        row.add(String.valueOf(milisecond));
        row.add(String.valueOf(record.getDuration()));
        row.add(record.getName());
        row.add(record.getDescription());
        return row;
    }

    private List<String> getHeaderRow() {
        final List<String> header = new ArrayList<String>();
        header.add("timestamp");
        header.add("year");
        header.add("month");
        header.add("dayOfMonth");
        header.add("hourOfDay");
        header.add("minute");
        header.add("second");
        header.add("milisecond");
        header.add("duration");
        header.add("name");
        header.add("description");
        return header;
    }

}