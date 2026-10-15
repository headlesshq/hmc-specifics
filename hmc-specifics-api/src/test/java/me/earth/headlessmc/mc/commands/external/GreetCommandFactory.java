package me.earth.headlessmc.mc.commands.external;

import me.earth.headlessmc.mc.commands.CommandFactory;
import me.earth.headlessmc.mc.commands.MinecraftContext;

public class GreetCommandFactory implements CommandFactory<GreetCommand> {
    @Override
    public Class<GreetCommand> getType() {
        return GreetCommand.class;
    }

    @Override
    public GreetCommand create(MinecraftContext ctx) {
        return new GreetCommand(ctx, "Hello");
    }

}
