package me.earth.headlessmc.mc.commands;

import me.earth.headlessmc.mc.metrics.Metric;
import me.earth.headlessmc.mc.metrics.MetricsFormatter;
import me.earth.headlessmc.mc.metrics.MetricsService;
import me.earth.headlessmc.mc.util.ExtendedTable;
import picocli.CommandLine;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@CommandLine.Command(name = "metrics", description = "Displays metrics like the server TPS, ping, players or memory usage.")
public class MetricsCommand extends AbstractMinecraftCommand implements ScheduledCommand {
    @CommandLine.Parameters(
        arity = "0..*",
        paramLabel = "<metric>",
        completionCandidates = MetricNames.class,
        description = "Metrics to display, by name (server.tps), group (server) or last part of the name (tps). All if not specified.")
    private List<String> names = new ArrayList<>();

    @CommandLine.Option(names = "--list", description = "Lists all metrics and their descriptions.")
    private boolean list;

    @CommandLine.Option(names = "--json", description = "Displays the metrics as JSON.")
    private boolean json;

    public MetricsCommand(MinecraftContext ctx) {
        super(ctx);
    }

    @Override
    protected void execute() throws CommandException {
        MetricsService service = ctx.getMetricsService();
        service.loadProviders(mc, MetricsCommand.class.getClassLoader());
        List<Metric<?>> metrics = service.select(names.toArray(new String[0]));
        if (list) {
            ctx.log(new ExtendedTable<Metric<?>>()
                        .withColumn("name", Metric::getName)
                        .withColumn("description", Metric::getDescription)
                        .addAll(metrics)
                        .build());
        } else if (json) {
            ctx.log(MetricsFormatter.toJson(service.collect(metrics)));
        } else {
            ctx.log(MetricsFormatter.toText(service.collect(metrics)));
        }
    }

    /**
     * Completes the names and groups of all metrics.
     */
    public static final class MetricNames implements Iterable<String> {
        @Override
        public Iterator<String> iterator() {
            Set<String> names = new LinkedHashSet<>();
            for (Metric<?> metric : MetricsService.getInstance().getMetrics()) {
                names.add(metric.getGroup());
            }

            for (Metric<?> metric : MetricsService.getInstance().getMetrics()) {
                names.add(metric.getName());
            }

            return names.iterator();
        }
    }

}
