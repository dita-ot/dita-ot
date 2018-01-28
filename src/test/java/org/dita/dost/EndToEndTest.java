package org.dita.dost;

import org.junit.Test;

import java.nio.file.Paths;

import static org.dita.dost.AbstractIntegrationTest.Transtype.*;

public class EndToEndTest extends AbstractIntegrationTest {

    @Test
    public void xhtml() throws Throwable {
        builder().name("e2e")
                .transtype(XHTML)
                .input(Paths.get("root.ditamap"))
                .run();
    }

    @Test
    public void html5() throws Throwable {
        builder().name("e2e")
                .transtype(HTML5)
                .input(Paths.get("root.ditamap"))
                .run();
    }

    @Test
    public void pdf() throws Throwable {
        builder().name("e2e")
                .transtype(PDF)
                .input(Paths.get("root.ditamap"))
                .warnCount(16)
                .run();
    }

    @Test
    public void eclipsehelp() throws Throwable {
        builder().name("e2e")
                .transtype(ECLIPSEHELP)
                .input(Paths.get("root.ditamap"))
                .run();
    }

    @Test
    public void htmlhelp() throws Throwable {
        builder().name("e2e")
                .transtype(HTMLHELP)
                .input(Paths.get("root.ditamap"))
                .run();
    }

}