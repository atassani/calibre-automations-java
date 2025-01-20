package calibreautomations;

import org.apache.commons.cli.*;

import java.io.PrintWriter;
import java.io.StringWriter;

public class AppOptions {

    private final Options options;
    private boolean readorders;
    private boolean audiobooks;
    private boolean dryRun;

    public AppOptions() {
        options = new Options();

        options.addOption("d", "dry-run", false, "Run the updater in dry-run mode");
        options.addOption("a", "audiobook", false, "Process audiobooks");
        options.addOption(Option.builder("r")
                .longOpt("readorder")
                .desc("Process read order")
                .build());
        options.addOption(Option.builder()
                .longOpt("readOrder")
                .desc("Process read order")
                .build());
        options.addOption(Option.builder()
                .longOpt("read-order")
                .desc("Process read order")
                .build());
    }

    public void parse(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd;
        cmd = parser.parse(options, args);

        // If no specific flag is set, process everything
        this.readorders = cmd.hasOption("r") || cmd.hasOption("readOrder") || cmd.hasOption("read-order") || !cmd.hasOption("a") || !cmd.hasOption("audiobook");
        this.audiobooks = cmd.hasOption("a") || !(cmd.hasOption("r") || cmd.hasOption("readOrder") || cmd.hasOption("read-order"));
        this.dryRun = cmd.hasOption("d");
    }

    public String help() {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(printWriter, 80, "CalibreUpdater", null, options, 1, 5, null);
        return stringWriter.toString();
    }

    public boolean isReadorders() {
        return readorders;
    }

    public boolean isAudiobooks() {
        return audiobooks;
    }

    public boolean isDryRun() {
        return dryRun;
    }
}
