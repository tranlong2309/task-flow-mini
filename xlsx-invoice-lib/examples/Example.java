import com.company.invoice.xlsx.InvoiceConfig;
import com.company.invoice.xlsx.InvoiceProcessor;
import com.company.invoice.xlsx.ProcessingResult;
import com.company.invoice.xlsx.JobContext;
import java.nio.file.Path;

/** Minimal runnable consumer example. */
public final class Example {
    private Example() { }

    public static void main(String[] arguments) {
        InvoiceProcessor processor = InvoiceProcessor.create(InvoiceConfig.builder()
                .jobLogDirectory(Path.of("logs"))
                .build());
        Path input = arguments.length > 0 ? Path.of(arguments[0]) : Path.of("sample-input.xlsx");
        Path output = arguments.length > 1 ? Path.of(arguments[1]) : Path.of("sample-output.xlsx");
        
        JobContext context = JobContext.of("example-correlation-id");
        ProcessingResult result = processor.process(input, output, context);
        System.out.println("Payable: " + result.totals().totalPayable());
        System.out.println("Skipped rows: " + result.skippedRowCount());
    }
}
