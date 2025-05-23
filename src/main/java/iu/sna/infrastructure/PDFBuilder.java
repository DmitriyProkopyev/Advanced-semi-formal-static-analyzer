package iu.sna.infrastructure;

import java.io.File;
import java.io.IOException;

public class PDFBuilder {
    private final Wrapper wrapper;

    public PDFBuilder() {
        this.wrapper = new Wrapper();
    }

    public void fromMarkdown(File original, File target) throws IOException, InterruptedException {
        this.wrapper.convert(original.getAbsolutePath(), target.getAbsolutePath());
    }
}
