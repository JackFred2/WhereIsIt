package red.jackf.whereisit.command;

import red.jackf.jackfredlib.api.extracommandsourcedata.ExtraSourceData;
import red.jackf.whereisit.WhereIsIt;
import red.jackf.whereisit.api.criteria.Criterion;

import java.util.ArrayList;
import java.util.List;

public class CommandCriteria implements ExtraSourceData<CommandCriteria> {
    private static final Definition<CommandCriteria>  DEFINITION = new Definition<>(WhereIsIt.id("criteria"),
            CommandCriteria.class,
            CommandCriteria::new);
    private final List<Criterion> criteria = new ArrayList<>();

    @Override
    public CommandCriteria copy() {
        var copy = new CommandCriteria();
        copy.criteria.addAll(this.criteria);
        return copy;
    }
}
