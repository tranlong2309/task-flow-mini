import com.company.invoice.xlsx.InvoiceConfig;
import com.company.invoice.xlsx.InvoiceProcessor;
import java.nio.file.Path;

public final class ExampleConsumer {
    private ExampleConsumer() { }

    public static void main(String[] args) {
        InvoiceProcessor.create(InvoiceConfig.builder().build()).process(Path.of(args[0]), Path.of(args[1]));
    }
}
